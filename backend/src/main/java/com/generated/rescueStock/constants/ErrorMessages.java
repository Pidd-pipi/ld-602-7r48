package com.generated.rescueStock.constants;

public final class ErrorMessages {
  public static final String AUTH_REQUIRED = "missing token";
  public static final String RBAC_DENIED = "role denied";

  public static final String BATCH_NOT_FOUND = "库存批次不存在";
  public static final String STOCK_CHECK_NOT_FOUND = "盘点单不存在";
  public static final String ACTUAL_QUANTITY_REQUIRED = "实盘数必须为不小于 0 的整数";
  public static final String GAIN_BASIS_REQUIRED = "盘盈必须写明盘盈依据";
  public static final String LOSS_BASIS_REQUIRED = "盘亏必须写明盘亏依据";
  public static final String DUPLICATE_PENDING_CHECK = "该批次已存在待审盘点单，同一批次同时只能有一张待审盘点";
  public static final String GAIN_ON_BLOCKED_BATCH = "盘盈不得回补质检冻结或已过期批次";
  public static final String CHECK_NOT_PENDING = "盘点单已完成复核，重复或并发审批只有一次生效";
}
