package com.generated.rescueStock.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.constructors.StockCheckDtoFactory;
import com.generated.rescueStock.exceptions.ConflictException;
import com.generated.rescueStock.exceptions.NotFoundException;
import com.generated.rescueStock.exceptions.ValidationException;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StockCheck;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.StockCheckRepository;
import com.generated.rescueStock.types.StockCheckReviewPayload;
import com.generated.rescueStock.types.StockCheckSubmitPayload;
import com.generated.rescueStock.constants.ErrorCodes;

@Service
public class StockCheckService {
  private static final Logger log = LoggerFactory.getLogger(StockCheckService.class);

  private final StockCheckRepository checkRepository;
  private final InventoryBatchRepository batchRepository;
  private final StockCheckDtoFactory dtoFactory;
  private final TransactionTemplate transactionTemplate;

  /** 每个批次一把锁：同批次的提交/复核在单进程内串行，避免并发穿透。 */
  private final ConcurrentHashMap<Long, ReentrantLock> batchLocks = new ConcurrentHashMap<>();

  public StockCheckService(StockCheckRepository checkRepository,
                           InventoryBatchRepository batchRepository,
                           StockCheckDtoFactory dtoFactory,
                           org.springframework.transaction.PlatformTransactionManager transactionManager) {
    this.checkRepository = checkRepository;
    this.batchRepository = batchRepository;
    this.dtoFactory = dtoFactory;
    this.transactionTemplate = new TransactionTemplate(transactionManager);
  }

  private ReentrantLock lockForBatch(Long batchId) {
    return batchLocks.computeIfAbsent(batchId, key -> new ReentrantLock());
  }

  /** 仓库录入实盘数并提交；差异保留待审，不立即调整批次数量。 */
  public Map<String, Object> submit(StockCheckSubmitPayload payload) {
    if (payload == null || payload.batchId() == null) {
      throw new ValidationException("缺少盘点批次");
    }
    Integer actual = payload.actualQuantity();
    if (actual == null || actual < 0) {
      throw new ValidationException(ErrorMessages.ACTUAL_QUANTITY_REQUIRED);
    }
    String gainBasis = trim(payload.gainBasis());
    String lossBasis = trim(payload.lossBasis());

    Long batchId = payload.batchId();
    ReentrantLock lock = lockForBatch(batchId);
    lock.lock();
    try {
      InventoryBatch batch = batchRepository.findById(batchId)
          .orElseThrow(() -> new NotFoundException(ErrorMessages.BATCH_NOT_FOUND));

      // 同一批次同时只能有一张待审盘点
      if (checkRepository.existsByPendingBatchId(batchId)) {
        logConflict("submit duplicate batch#" + batchId);
        throw new ConflictException(ErrorCodes.STOCK_CHECK_DUPLICATE, ErrorMessages.DUPLICATE_PENDING_CHECK);
      }

      int snapshot = batch.getQuantity() == null ? 0 : batch.getQuantity();
      int variance = actual - snapshot;
      String varianceType = variance > 0 ? StockCheck.VARIANCE_SURPLUS
          : variance < 0 ? StockCheck.VARIANCE_LOSS : StockCheck.VARIANCE_MATCHED;

      // 盘盈/盘亏分别写明依据
      if (StockCheck.VARIANCE_SURPLUS.equals(varianceType) && gainBasis.isEmpty()) {
        throw new ValidationException(ErrorMessages.GAIN_BASIS_REQUIRED);
      }
      if (StockCheck.VARIANCE_LOSS.equals(varianceType) && lossBasis.isEmpty()) {
        throw new ValidationException(ErrorMessages.LOSS_BASIS_REQUIRED);
      }

      boolean expired = StockCheck.isExpired(batch.getExpireAt(), LocalDate.now());
      boolean frozen = QualityStatus.isFrozen(batch.getQualityStatus());
      // 盘盈不得回补质检冻结或已过期批次；盘亏可照常提交
      if (StockCheck.VARIANCE_SURPLUS.equals(varianceType) && (frozen || expired)) {
        logConflict("gain blocked batch#" + batchId + " frozen=" + frozen + " expired=" + expired);
        throw new ConflictException(ErrorCodes.STOCK_CHECK_GAIN_BLOCKED, ErrorMessages.GAIN_ON_BLOCKED_BATCH);
      }

      LocalDateTime now = LocalDateTime.now();
      StockCheck check = new StockCheck();
      check.setBatchId(batchId);
      check.setSnapshotQuantity(snapshot);
      check.setActualQuantity(actual);
      check.setVariance(variance);
      check.setVarianceType(varianceType);
      check.setGainBasis(gainBasis.isEmpty() ? null : gainBasis);
      check.setLossBasis(lossBasis.isEmpty() ? null : lossBasis);
      check.setStatus(StockCheck.STATUS_PENDING);
      check.setSubmittedBy(optionalText(payload.submittedBy(), "仓库员"));
      check.setSubmittedAt(now);
      check.setCreatedAt(now);
      check.setPendingBatchId(batchId); // 唯一约束兜底：并发插入只有一张成功

      StockCheck saved = checkRepository.save(check);
      log.info(LogTemplates.STOCK_CHECK_SUBMIT.formatted(batchId, snapshot, actual, variance, varianceType));
      return dtoFactory.toView(saved);
      // 唯一约束冲突（多实例并发）由全局异常处理转成 409，批次与盘点单不变
    } finally {
      lock.unlock();
    }
  }

  /** 审批通过：把批次数量调整为实盘数，待审槽位释放。重复/并发审批只成功一次。 */
  public Map<String, Object> approve(Long id, StockCheckReviewPayload payload) {
    StockCheck check = checkRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorMessages.STOCK_CHECK_NOT_FOUND));
    Long batchId = check.getBatchId();
    ReentrantLock lock = lockForBatch(batchId);
    lock.lock();
    try {
      if (!check.isPending()) {
        logConflict("approve non-pending check#" + id);
        throw new ConflictException(ErrorCodes.STOCK_CHECK_NOT_PENDING, ErrorMessages.CHECK_NOT_PENDING);
      }
      StockCheck done = transactionTemplate.execute(state -> {
        // 事务内二次确认：并发审批只有一个事务能看到 PENDING
        StockCheck locked = checkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.STOCK_CHECK_NOT_FOUND));
        if (!locked.isPending()) {
          throw new ConflictException(ErrorCodes.STOCK_CHECK_NOT_PENDING, ErrorMessages.CHECK_NOT_PENDING);
        }
        InventoryBatch batch = batchRepository.findById(batchId)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.BATCH_NOT_FOUND));

        int before = batch.getQuantity() == null ? 0 : batch.getQuantity();
        batch.setQuantity(locked.getActualQuantity()); // 审批通过才调整为实盘数
        locked.setStatus(StockCheck.STATUS_APPROVED);
        locked.setReviewedBy(optionalText(payload == null ? null : payload.reviewer(), "审批员"));
        locked.setReviewedAt(LocalDateTime.now());
        locked.setReviewComment(trim(payload == null ? null : payload.comment()));
        locked.setPendingBatchId(null); // 释放待审槽位；@Version 保证并发提交只生效一次
        batchRepository.save(batch);
        return checkRepository.save(locked);
      });
      log.info(LogTemplates.STOCK_CHECK_APPROVE.formatted(id, batchId,
          check.getSnapshotQuantity(), check.getActualQuantity()));
      return dtoFactory.toView(done);
    } finally {
      lock.unlock();
    }
  }

  /** 复核驳回：差异单留存但不调整任何批次数量。 */
  public Map<String, Object> reject(Long id, StockCheckReviewPayload payload) {
    StockCheck check = checkRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorMessages.STOCK_CHECK_NOT_FOUND));
    Long batchId = check.getBatchId();
    ReentrantLock lock = lockForBatch(batchId);
    lock.lock();
    try {
      if (!check.isPending()) {
        logConflict("reject non-pending check#" + id);
        throw new ConflictException(ErrorCodes.STOCK_CHECK_NOT_PENDING, ErrorMessages.CHECK_NOT_PENDING);
      }
      StockCheck done = transactionTemplate.execute(state -> {
        StockCheck locked = checkRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessages.STOCK_CHECK_NOT_FOUND));
        if (!locked.isPending()) {
          throw new ConflictException(ErrorCodes.STOCK_CHECK_NOT_PENDING, ErrorMessages.CHECK_NOT_PENDING);
        }
        locked.setStatus(StockCheck.STATUS_REJECTED);
        locked.setReviewedBy(optionalText(payload == null ? null : payload.reviewer(), "审批员"));
        locked.setReviewedAt(LocalDateTime.now());
        locked.setReviewComment(trim(payload == null ? null : payload.comment()));
        locked.setPendingBatchId(null); // 驳回后批次可以重新发起盘点
        return checkRepository.save(locked);
      });
      log.info(LogTemplates.STOCK_CHECK_REJECT.formatted(id, batchId));
      return dtoFactory.toView(done);
    } finally {
      lock.unlock();
    }
  }

  public List<Map<String, Object>> list(Long batchId, String status) {
    List<StockCheck> rows = (batchId == null)
        ? checkRepository.findAllByOrderByCreatedAtDescIdDesc()
        : checkRepository.findByBatchIdOrderByCreatedAtDesc(batchId);
    return rows.stream()
        .filter(row -> status == null || status.isBlank() || status.equals(row.getStatus()))
        .map(dtoFactory::toView)
        .toList();
  }

  public Map<String, Object> get(Long id) {
    StockCheck check = checkRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorMessages.STOCK_CHECK_NOT_FOUND));
    return dtoFactory.toView(check);
  }

  public Optional<StockCheck> findPendingOfBatch(Long batchId) {
    return checkRepository.findByPendingBatchId(batchId);
  }

  private static String trim(String value) {
    return value == null ? "" : value.trim();
  }

  private static String optionalText(String value, String fallback) {
    String text = trim(value);
    return text.isEmpty() ? fallback : text;
  }

  private static void logConflict(String detail) {
    log.warn(LogTemplates.STOCK_CHECK_CONFLICT.formatted(detail));
  }
}
