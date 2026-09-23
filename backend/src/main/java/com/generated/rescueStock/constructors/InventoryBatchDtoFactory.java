package com.generated.rescueStock.constructors;

import com.generated.rescueStock.models.InventoryBatch;
import java.util.LinkedHashMap;
import java.util.Map;

/** 批次响应对象构造器：控制器 / 服务不得散写字段结构。 */
public final class InventoryBatchDtoFactory {

  private InventoryBatchDtoFactory() {}

  public static Map<String, Object> create() {
    return toMap(new InventoryBatch());
  }

  public static Map<String, Object> toMap(InventoryBatch batch) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", batch.getId());
    dto.put("warehouse_id", batch.getWarehouseId());
    dto.put("supply_item_id", batch.getSupplyItemId());
    dto.put("batch_no", batch.getBatchNo());
    dto.put("quantity", batch.getQuantity());
    dto.put("expire_at", batch.getExpireAt());
    dto.put("inbound_source", batch.getInboundSource());
    dto.put("quality_status", batch.getQualityStatus());
    return dto;
  }
}
