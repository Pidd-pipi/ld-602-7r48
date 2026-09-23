package com.generated.rescueStock.constructors;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StockCheck;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;

/** 盘点单 DTO 工厂：在差异单上回填批次号、物资、仓库等展示字段。 */
@Component
public class StockCheckDtoFactory {
  private final InventoryBatchRepository batchRepository;
  private final SupplyItemRepository supplyItemRepository;
  private final WarehouseRepository warehouseRepository;

  public StockCheckDtoFactory(InventoryBatchRepository batchRepository,
                              SupplyItemRepository supplyItemRepository,
                              WarehouseRepository warehouseRepository) {
    this.batchRepository = batchRepository;
    this.supplyItemRepository = supplyItemRepository;
    this.warehouseRepository = warehouseRepository;
  }

  public Map<String, Object> toView(StockCheck check) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("id", check.getId());
    view.put("batch_id", check.getBatchId());
    view.put("snapshot_quantity", check.getSnapshotQuantity());
    view.put("actual_quantity", check.getActualQuantity());
    view.put("variance", check.getVariance());
    view.put("variance_type", check.getVarianceType());
    view.put("gain_basis", check.getGainBasis());
    view.put("loss_basis", check.getLossBasis());
    view.put("status", check.getStatus());
    view.put("submitted_by", check.getSubmittedBy());
    view.put("submitted_at", check.getSubmittedAt());
    view.put("reviewed_by", check.getReviewedBy());
    view.put("reviewed_at", check.getReviewedAt());
    view.put("review_comment", check.getReviewComment());
    view.put("created_at", check.getCreatedAt());

    InventoryBatch batch = batchRepository.findById(check.getBatchId()).orElse(null);
    if (batch != null) {
      view.put("batch_no", batch.getBatchNo());
      view.put("warehouse_id", batch.getWarehouseId());
      view.put("supply_item_id", batch.getSupplyItemId());
      view.put("quality_status", batch.getQualityStatus());
      view.put("expire_at", batch.getExpireAt());
      view.put("current_quantity", batch.getQuantity());
      warehouseRepository.findById(batch.getWarehouseId())
          .ifPresent(warehouse -> view.put("warehouse_name", warehouse.getName()));
      supplyItemRepository.findById(batch.getSupplyItemId()).ifPresent(item -> {
        view.put("supply_item_name", item.getName());
        view.put("unit", item.getUnit());
      });
      LocalDate today = LocalDate.now();
      view.put("expired", StockCheck.isExpired(batch.getExpireAt(), today));
      view.put("frozen", com.generated.rescueStock.constants.QualityStatus.isFrozen(batch.getQualityStatus()));
    }
    return view;
  }
}
