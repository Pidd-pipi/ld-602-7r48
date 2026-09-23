package com.generated.rescueStock.repositories;

import com.generated.rescueStock.constants.StocktakeStatus;
import com.generated.rescueStock.models.StocktakeOrder;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import org.springframework.stereotype.Repository;

/**
 * 盘点单数据访问层。
 *
 * <p>维护 batchId -> 待审盘点单 的唯一索引：同一批次同时只能有一张待审盘点。
 * 状态流转使用条件更新（CAS），重复提交、重复审批或并发审批只成功一次。</p>
 */
@Repository
public class StocktakeOrderRepository {
  private final ConcurrentMap<Long, StocktakeOrder> store = new ConcurrentHashMap<>();
  /** 当前仍处于 PENDING 的盘点单索引，审批 / 驳回后移除。 */
  private final ConcurrentMap<Long, Long> pendingByBatch = new ConcurrentHashMap<>();
  private final AtomicLong idSequence = new AtomicLong(0);

  public Long nextId() {
    return idSequence.incrementAndGet();
  }

  /**
   * 原子占位：若该批次尚无待审盘点则登记 stocktakeId 并返回 true；
   * 已存在待审盘点时返回 false（重复提交拦截，盘点单与批次均不变）。
   */
  public boolean tryMarkPending(Long batchId, Long stocktakeId) {
    return pendingByBatch.putIfAbsent(batchId, stocktakeId) == null;
  }

  public void save(StocktakeOrder order) {
    store.put(order.getId(), order);
  }

  public Optional<StocktakeOrder> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  public Optional<StocktakeOrder> findPendingByBatch(Long batchId) {
    Long pendingId = pendingByBatch.get(batchId);
    return pendingId == null ? Optional.empty() : findById(pendingId);
  }

  public List<StocktakeOrder> findAll() {
    return store.values().stream()
        .sorted(Comparator.comparing(StocktakeOrder::getId))
        .toList();
  }

  public List<StocktakeOrder> findByBatch(Long batchId) {
    return store.values().stream()
        .filter(order -> order.getBatchId().equals(batchId))
        .sorted(Comparator.comparing(StocktakeOrder::getId))
        .toList();
  }

  /**
   * 条件流转（驳回用）：仅当盘点单当前仍为 PENDING 时，原子地更新为目标状态并释放待审占位。
   * 并发 / 重复审批只有一个线程成功；失败者数据不变。
   */
  public boolean tryFinishIfPending(Long stocktakeId, String targetStatus) {
    return tryFinishIfPending(stocktakeId, targetStatus, order -> true);
  }

  /**
   * 带额外条件的条件流转（审批通过用）：仅当仍为 PENDING 且 {@code extraCondition} 成立时，
   * 才在同一临界区内完成状态翻转并释放占位；任一条件不满足则状态与占位保持原样，
   * 保证“落账失败后盘点单与批次均不变”。
   */
  public boolean tryFinishIfPending(Long stocktakeId, String targetStatus,
                                    Predicate<StocktakeOrder> extraCondition) {
    StocktakeOrder order = store.get(stocktakeId);
    if (order == null) {
      return false;
    }
    synchronized (order) {
      if (!StocktakeStatus.PENDING.name().equals(order.getStatus())
          || !extraCondition.test(order)) {
        return false;
      }
      order.setStatus(targetStatus);
    }
    // 仅当占位仍指向本单时才释放，避免误删后续新建的待审单。
    pendingByBatch.remove(order.getBatchId(), stocktakeId);
    return true;
  }
}
