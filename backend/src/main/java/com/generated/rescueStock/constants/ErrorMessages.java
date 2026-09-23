package com.generated.rescueStock.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";

  /** 注意：占位符与服务层 String.format 参数顺序保持一致，字段变更需同步调用处。 */
  public static final String STOCKTAKE_BATCH_NOT_FOUND = "inventory batch not found: batchId=%s";
  public static final String STOCKTAKE_PENDING_EXISTS = "pending stocktake already exists: batchId=%s, stocktakeId=%s";
  public static final String STOCKTAKE_QUANTITY_INVALID = "actual quantity must be a non-negative integer: actual=%s";
  public static final String STOCKTAKE_SURPLUS_FORBIDDEN = "surplus cannot restore frozen or expired batch: batchId=%s, qualityStatus=%s";
  public static final String STOCKTAKE_REASON_REQUIRED = "variance reason is required for %s: stocktakeId=%s";
  public static final String STOCKTAKE_ORDER_NOT_FOUND = "stocktake order not found: stocktakeId=%s";
  public static final String STOCKTAKE_NOT_PENDING = "stocktake order is no longer pending: stocktakeId=%s, status=%s";
  public static final String STOCKTAKE_APPROVAL_NOTE_REQUIRED = "approval note is required: stocktakeId=%s";

  private ErrorMessages() {}
}
