package com.generated.rescueStock.services;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StockCheck;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.StockCheckRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;

@Service
public class InventoryBatchService {
  private final InventoryBatchRepository repo;
  private final StockCheckRepository checkRepository;
  private final WarehouseRepository warehouseRepository;
  private final SupplyItemRepository supplyItemRepository;

  public InventoryBatchService(InventoryBatchRepository repo,
                               StockCheckRepository checkRepository,
                               WarehouseRepository warehouseRepository,
                               SupplyItemRepository supplyItemRepository) {
    this.repo = repo;
    this.checkRepository = checkRepository;
    this.warehouseRepository = warehouseRepository;
    this.supplyItemRepository = supplyItemRepository;
  }

  public List<Map<String, Object>> list(Long warehouseId) {
    List<InventoryBatch> rows = (warehouseId == null)
        ? repo.findAllByOrderByIdAsc()
        : repo.findByWarehouseIdOrderByIdAsc(warehouseId);
    return rows.stream().map(this::toView).toList();
  }

  private Map<String, Object> toView(InventoryBatch batch) {
    Map<String, Object> view = new LinkedHashMap<>();
    view.put("id", batch.getId());
    view.put("warehouse_id", batch.getWarehouseId());
    view.put("supply_item_id", batch.getSupplyItemId());
    view.put("batch_no", batch.getBatchNo());
    view.put("quantity", batch.getQuantity());
    view.put("expire_at", batch.getExpireAt());
    view.put("inbound_source", batch.getInboundSource());
    view.put("quality_status", batch.getQualityStatus());
    view.put("frozen", QualityStatus.isFrozen(batch.getQualityStatus()));
    view.put("expired", StockCheck.isExpired(batch.getExpireAt(), LocalDate.now()));

    warehouseRepository.findById(batch.getWarehouseId())
        .ifPresent(warehouse -> view.put("warehouse_name", warehouse.getName()));
    supplyItemRepository.findById(batch.getSupplyItemId()).ifPresent(item -> {
      view.put("supply_item_name", item.getName());
      view.put("unit", item.getUnit());
    });

    checkRepository.findByPendingBatchId(batch.getId())
        .map(StockCheck::getId)
        .ifPresent(pendingId -> view.put("pending_check_id", pendingId));
    return view;
  }
}
