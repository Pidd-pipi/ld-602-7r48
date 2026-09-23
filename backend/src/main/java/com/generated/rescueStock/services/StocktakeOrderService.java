package com.generated.rescueStock.services;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.constants.ErrorMessages;
import com.generated.rescueStock.constants.LogTemplates;
import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.constants.StocktakeStatus;
import com.generated.rescueStock.constants.StocktakeVariance;
import com.generated.rescueStock.constructors.StocktakeOrderDtoFactory;
import com.generated.rescueStock.exceptions.StocktakeBusinessException;
import com.generated.rescueStock.middlewares.AuditLogMiddleware;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StocktakeOrder;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.StocktakeOrderRepository;
import com.generated.rescueStock.types.StocktakeReviewPayload;
import com.generated.rescueStock.types.StocktakeSubmitPayload;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * 库存批次盘点差异复核服务。
 *
 * <p>规则：
 * <ul>
 *   <li>仓库员录入实盘数并提交，差异（盘盈 / 盘亏 / 一致）保留待审；</li>
 *   <li>只有审批通过才把批次数量调整为实盘数，盘盈盘亏分别写明依据；</li>
 *   <li>盘盈不得回补质检冻结或已过期批次，盘亏可照常提交；</li>
 *   <li>同一批次同时只能有一张待审盘点；重复提交、重复审批、并发审批只成功一次，
 *       失败后批次与盘点单不变。</li>
 * </ul>
 */
@Service
public class StocktakeOrderService {
  private final StocktakeOrderRepository stocktakeRepo;
  private final InventoryBatchRepository batchRepo;
  private final AuditLogMiddleware auditLog;

  public StocktakeOrderService(StocktakeOrderRepository stocktakeRepo,
                               InventoryBatchRepository batchRepo,
                               AuditLogMiddleware auditLog) {
    this.stocktakeRepo = stocktakeRepo;
    this.batchRepo = batchRepo;
    this.auditLog = auditLog;
  }

  /** 仓库员发起盘点：录入实盘数并提交，差异保留待审。 */
  public Map<String, Object> submit(StocktakeSubmitPayload payload) {
    if (payload == null) {
      throw batchNotFound(null);
    }
    if (payload.batchId() == null) {
      throw batchNotFound(null);
    }
    Integer actual = payload.actualQuantity();
    if (actual == null || actual < 0) {
      throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_QUANTITY_INVALID,
          String.format(ErrorMessages.STOCKTAKE_QUANTITY_INVALID, String.valueOf(actual)));
    }
    String actor = blankToDefault(payload.submittedBy(), "warehouse-keeper");
    String reason = payload.reason() == null ? "" : payload.reason().trim();

    InventoryBatch batch = batchRepo.findById(payload.batchId())
        .orElseThrow(() -> batchNotFound(payload.batchId()));

    // 同一批次上的提交 / 审批落账在批次锁内串行，杜绝并发产生两张待审单。
    synchronized (batchRepo.lockOf(batch.getId())) {
      StocktakeOrder existing = stocktakeRepo.findPendingByBatch(batch.getId()).orElse(null);
      if (existing != null) {
        auditLog.record(actor, LogTemplates.STOCKTAKE_DUPLICATE_BLOCKED,
            "StocktakeOrder", String.valueOf(existing.getId()),
            String.format(LogTemplates.STOCKTAKE_DUPLICATE_BLOCKED,
                actor, batch.getId(), existing.getId()));
        throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_PENDING_EXISTS,
            String.format(ErrorMessages.STOCKTAKE_PENDING_EXISTS, batch.getId(), existing.getId()));
      }

      int book = batch.getQuantity();
      int varianceQuantity = actual - book;
      StocktakeVariance variance = varianceQuantity > 0 ? StocktakeVariance.SURPLUS
          : varianceQuantity < 0 ? StocktakeVariance.LOSS : StocktakeVariance.MATCH;

      // 盘盈不得回补质检冻结或已过期批次；盘亏可照常提交。
      if (variance == StocktakeVariance.SURPLUS && !canReceiveSurplus(batch)) {
        throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_SURPLUS_FORBIDDEN,
            String.format(ErrorMessages.STOCKTAKE_SURPLUS_FORBIDDEN,
                batch.getId(), batch.getQualityStatus()));
      }

      // 盘盈盘亏分别写明依据；数量一致不强制。
      if (variance != StocktakeVariance.MATCH && reason.isEmpty()) {
        throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_REASON_REQUIRED,
            String.format(ErrorMessages.STOCKTAKE_REASON_REQUIRED,
                variance == StocktakeVariance.SURPLUS ? "surplus" : "loss", "<pending>"));
      }

      StocktakeOrder order = new StocktakeOrder();
      Long orderId = stocktakeRepo.nextId();
      order.setId(orderId);
      order.setBatchId(batch.getId());
      order.setBookQuantity(book);
      order.setActualQuantity(actual);
      order.setVarianceQuantity(varianceQuantity);
      order.setVariance(variance.name());
      order.setStatus(StocktakeStatus.PENDING.name());
      order.setReason(reason.isEmpty() ? null : reason);
      order.setSubmittedBy(actor);
      order.setSubmittedAt(Instant.now().toString());

      // 先原子登记待审占位再落库：登记失败说明并发越界（已存在待审单），
      // 此时不写入盘点单、不动批次数量，满足“失败后批次与盘点单不变”。
      if (!stocktakeRepo.tryMarkPending(batch.getId(), orderId)) {
        throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_PENDING_EXISTS,
            String.format(ErrorMessages.STOCKTAKE_PENDING_EXISTS, batch.getId(), "unknown"));
      }
      stocktakeRepo.save(order);

      auditLog.record(actor, LogTemplates.STOCKTAKE_SUBMIT,
          "StocktakeOrder", String.valueOf(order.getId()),
          String.format(LogTemplates.STOCKTAKE_SUBMIT,
              actor, order.getId(), batch.getId(), book, actual, variance.name()));
      return StocktakeOrderDtoFactory.toMap(order, batch);
    }
  }

  /** 审批复核：通过则把批次数量调整为实盘数，驳回则保留账面不动。重复 / 并发审批只成功一次。 */
  public Map<String, Object> review(Long stocktakeId, StocktakeReviewPayload payload) {
    StocktakeOrder order = stocktakeRepo.findById(stocktakeId)
        .orElseThrow(() -> new StocktakeBusinessException(ErrorCodes.STOCKTAKE_ORDER_NOT_FOUND,
            String.format(ErrorMessages.STOCKTAKE_ORDER_NOT_FOUND, stocktakeId)));
    if (payload == null) {
      throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_APPROVAL_NOTE_REQUIRED,
          String.format(ErrorMessages.STOCKTAKE_APPROVAL_NOTE_REQUIRED, stocktakeId));
    }
    String note = payload.reviewNote() == null ? "" : payload.reviewNote().trim();
    if (note.isEmpty()) {
      throw new StocktakeBusinessException(ErrorCodes.STOCKTAKE_APPROVAL_NOTE_REQUIRED,
          String.format(ErrorMessages.STOCKTAKE_APPROVAL_NOTE_REQUIRED, stocktakeId));
    }
    String actor = blankToDefault(payload.reviewedBy(), "approver");

    InventoryBatch batch = batchRepo.findById(order.getBatchId())
        .orElseThrow(() -> batchNotFound(order.getBatchId()));

    // 审批落账必须持批次锁：与该批次后续新盘点的提交互斥，避免旧单覆盖新账面。
    synchronized (batchRepo.lockOf(batch.getId())) {
      if (payload.approve()) {
        int oldQuantity = batch.getQuantity();

        // PENDING -> APPROVED 的条件翻转与批次数量落账放在同一临界区：
        // 重复 / 并发审批，或账面已偏离提交快照，都不会改动盘点单与批次。
        boolean finished = stocktakeRepo.tryFinishIfPending(
            order.getId(), StocktakeStatus.APPROVED.name(),
            pending -> batchRepo.adjustQuantityIfMatches(
                batch.getId(), pending.getBookQuantity(), pending.getActualQuantity()));
        if (!finished) {
          auditLog.record(actor, LogTemplates.STOCKTAKE_REAPPROVE_BLOCKED,
              "StocktakeOrder", String.valueOf(order.getId()),
              String.format(LogTemplates.STOCKTAKE_REAPPROVE_BLOCKED,
                  actor, order.getId(), order.getStatus()));
          throw notPending(order);
        }

        order.setReviewedBy(actor);
        order.setReviewNote(note);
        order.setReviewedAt(Instant.now().toString());
        stocktakeRepo.save(order);
        auditLog.record(actor, LogTemplates.STOCKTAKE_APPROVE,
            "StocktakeOrder", String.valueOf(order.getId()),
            String.format(LogTemplates.STOCKTAKE_APPROVE,
                actor, order.getId(), batch.getId(), oldQuantity, order.getActualQuantity()));
      } else {
        boolean finished = stocktakeRepo.tryFinishIfPending(
            order.getId(), StocktakeStatus.REJECTED.name());
        if (!finished) {
          auditLog.record(actor, LogTemplates.STOCKTAKE_REAPPROVE_BLOCKED,
              "StocktakeOrder", String.valueOf(order.getId()),
              String.format(LogTemplates.STOCKTAKE_REAPPROVE_BLOCKED,
                  actor, order.getId(), order.getStatus()));
          throw notPending(order);
        }
        order.setReviewedBy(actor);
        order.setReviewNote(note);
        order.setReviewedAt(Instant.now().toString());
        stocktakeRepo.save(order);
        auditLog.record(actor, LogTemplates.STOCKTAKE_REJECT,
            "StocktakeOrder", String.valueOf(order.getId()),
            String.format(LogTemplates.STOCKTAKE_REJECT,
                actor, order.getId(), batch.getId(), note));
      }
      return StocktakeOrderDtoFactory.toMap(order, batch);
    }
  }

  public List<Map<String, Object>> list(Long batchId) {
    return stocktakeRepo.findAll().stream()
        .filter(order -> batchId == null || batchId.equals(order.getBatchId()))
        .map(order -> StocktakeOrderDtoFactory.toMap(order, batchRepo.findById(order.getBatchId()).orElse(null)))
        .toList();
  }

  public Map<String, Object> get(Long stocktakeId) {
    StocktakeOrder order = stocktakeRepo.findById(stocktakeId)
        .orElseThrow(() -> new StocktakeBusinessException(ErrorCodes.STOCKTAKE_ORDER_NOT_FOUND,
            String.format(ErrorMessages.STOCKTAKE_ORDER_NOT_FOUND, stocktakeId)));
    return StocktakeOrderDtoFactory.toMap(order, batchRepo.findById(order.getBatchId()).orElse(null));
  }

  /** 质检冻结或已过期批次不得盘盈回补；过期同时以质检标记和到期时间判定。 */
  private boolean canReceiveSurplus(InventoryBatch batch) {
    if (QualityStatus.FROZEN.name().equals(batch.getQualityStatus())
        || QualityStatus.EXPIRED.name().equals(batch.getQualityStatus())) {
      return false;
    }
    String expireAt = batch.getExpireAt();
    if (expireAt != null && !expireAt.isBlank()) {
      try {
        return !Instant.parse(expireAt).isBefore(Instant.now());
      } catch (RuntimeException ignored) {
        return true;
      }
    }
    return true;
  }

  private static StocktakeBusinessException batchNotFound(Long batchId) {
    return new StocktakeBusinessException(ErrorCodes.STOCKTAKE_BATCH_NOT_FOUND,
        String.format(ErrorMessages.STOCKTAKE_BATCH_NOT_FOUND, String.valueOf(batchId)));
  }

  private static StocktakeBusinessException notPending(StocktakeOrder order) {
    return new StocktakeBusinessException(ErrorCodes.STOCKTAKE_NOT_PENDING,
        String.format(ErrorMessages.STOCKTAKE_NOT_PENDING, order.getId(), order.getStatus()));
  }

  private static String blankToDefault(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }
}
