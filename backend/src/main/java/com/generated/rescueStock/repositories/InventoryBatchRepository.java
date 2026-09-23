package com.generated.rescueStock.repositories;

import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.models.InventoryBatch;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/**
 * 库存批次数据访问层。
 *
 * <p>本地内存存储 + 种子数据（无第三方数据源）。每个批次持有一把锁，
 * 盘点提交与审批落账在批次锁内串行，配合盘点单条件更新保证并发只成功一次。</p>
 */
@Repository
public class InventoryBatchRepository {
  private final Map<Long, InventoryBatch> store = new ConcurrentHashMap<>();
  private final Map<Long, Object> batchLocks = new ConcurrentHashMap<>();

  public InventoryBatchRepository() {
    save(seed(1L, 1L, 1L, "W2026-0511-瓶装水", 120, "2027-06-11T09:00:00Z", "市级应急采购入库", QualityStatus.NORMAL));
    save(seed(2L, 1L, 2L, "W2026-0302-急救包", 60, "2027-03-02T09:00:00Z", "红十字会捐赠入库", QualityStatus.FROZEN));
    save(seed(3L, 2L, 1L, "W2025-1201-瓶装水", 80, "2026-01-15T09:00:00Z", "区级应急采购入库", QualityStatus.EXPIRED));
    save(seed(4L, 2L, 3L, "W2026-0820-折叠床", 40, "2028-08-20T09:00:00Z", "街道自购入库", QualityStatus.NORMAL));
  }

  private static InventoryBatch seed(Long id, Long warehouseId, Long supplyItemId, String batchNo,
      int quantity, String expireAt, String inboundSource, QualityStatus quality) {
    InventoryBatch batch = new InventoryBatch();
    batch.setId(id);
    batch.setWarehouseId(warehouseId);
    batch.setSupplyItemId(supplyItemId);
    batch.setBatchNo(batchNo);
    batch.setQuantity(quantity);
    batch.setExpireAt(expireAt);
    batch.setInboundSource(inboundSource);
    batch.setQualityStatus(quality.name());
    return batch;
  }

  private void save(InventoryBatch batch) {
    store.put(batch.getId(), batch);
    batchLocks.put(batch.getId(), new Object());
  }

  public List<InventoryBatch> findAll() {
    return store.values().stream()
        .sorted(Comparator.comparing(InventoryBatch::getId))
        .toList();
  }

  public Optional<InventoryBatch> findById(Long id) {
    return Optional.ofNullable(store.get(id));
  }

  /** 盘点提交 / 审批落账期间串行化同一批次上的并发操作。 */
  public Object lockOf(Long batchId) {
    return batchLocks.computeIfAbsent(batchId, key -> new Object());
  }

  /** 条件更新：仅当当前数量仍等于 expectedQuantity 时才调整，防止陈旧覆盖。 */
  public boolean adjustQuantityIfMatches(Long batchId, int expectedQuantity, int newQuantity) {
    InventoryBatch batch = store.get(batchId);
    if (batch == null || batch.getQuantity() != expectedQuantity) {
      return false;
    }
    batch.setQuantity(newQuantity);
    return true;
  }
}
