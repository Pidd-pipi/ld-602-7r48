package com.generated.rescueStock.constructors;

import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StocktakeOrder;
import java.util.LinkedHashMap;
import java.util.Map;

/** 盘点单响应对象构造器：附带批次冗余字段方便库存页直接展示差异。 */
public final class StocktakeOrderDtoFactory {

  private StocktakeOrderDtoFactory() {}

  public static Map<String, Object> toMap(StocktakeOrder order, InventoryBatch batch) {
    Map<String, Object> dto = new LinkedHashMap<>();
    dto.put("id", order.getId());
    dto.put("batch_id", order.getBatchId());
    dto.put("batch_no", batch == null ? null : batch.getBatchNo());
    dto.put("warehouse_id", batch == null ? null : batch.getWarehouseId());
    dto.put("supply_item_id", batch == null ? null : batch.getSupplyItemId());
    dto.put("book_quantity", order.getBookQuantity());
    dto.put("actual_quantity", order.getActualQuantity());
    dto.put("variance_quantity", order.getVarianceQuantity());
    dto.put("variance", order.getVariance());
    dto.put("status", order.getStatus());
    dto.put("reason", order.getReason());
    dto.put("submitted_by", order.getSubmittedBy());
    dto.put("submitted_at", order.getSubmittedAt());
    dto.put("reviewed_by", order.getReviewedBy());
    dto.put("review_note", order.getReviewNote());
    dto.put("reviewed_at", order.getReviewedAt());
    return dto;
  }
}
