package com.generated.rescueStock.constants;

public final class ErrorCodes {
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  public static final String RBAC_DENIED = "RBAC_DENIED";
  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";
  public static final String RATE_LIMITED = "RATE_LIMITED";
  public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";

  /** 同一批次已存在待审盘点，重复提交被拒绝 */
  public static final String STOCK_CHECK_DUPLICATE = "STOCK_CHECK_DUPLICATE";
  /** 盘盈回补了质检冻结或已过期批次 */
  public static final String STOCK_CHECK_GAIN_BLOCKED = "STOCK_CHECK_GAIN_BLOCKED";
  /** 盘点单不是待审状态，重复/并发审批只成功一次 */
  public static final String STOCK_CHECK_NOT_PENDING = "STOCK_CHECK_NOT_PENDING";
}
