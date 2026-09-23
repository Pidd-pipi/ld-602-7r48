package com.generated.rescueStock.constants;

public final class ErrorCodes {
  public static final String AUTH_REQUIRED = "AUTH_REQUIRED";
  public static final String RBAC_DENIED = "RBAC_DENIED";

  /** 盘点单：批次不存在。 */
  public static final String STOCKTAKE_BATCH_NOT_FOUND = "STOCKTAKE_BATCH_NOT_FOUND";
  /** 盘点单：同一批次已存在待审盘点，禁止重复提交。 */
  public static final String STOCKTAKE_PENDING_EXISTS = "STOCKTAKE_PENDING_EXISTS";
  /** 盘点单：实盘数非法（为空或为负）。 */
  public static final String STOCKTAKE_QUANTITY_INVALID = "STOCKTAKE_QUANTITY_INVALID";
  /** 盘点单：盘盈不得回补质检冻结或已过期批次。 */
  public static final String STOCKTAKE_SURPLUS_FORBIDDEN = "STOCKTAKE_SURPLUS_FORBIDDEN";
  /** 盘点单：盘盈 / 盘亏依据未填写。 */
  public static final String STOCKTAKE_REASON_REQUIRED = "STOCKTAKE_REASON_REQUIRED";
  /** 盘点单：盘点单不存在。 */
  public static final String STOCKTAKE_ORDER_NOT_FOUND = "STOCKTAKE_ORDER_NOT_FOUND";
  /** 盘点单：待审单已被审批或驳回，重复 / 并发审批只成功一次。 */
  public static final String STOCKTAKE_NOT_PENDING = "STOCKTAKE_NOT_PENDING";
  /** 盘点单：审批意见（复核依据）缺失。 */
  public static final String STOCKTAKE_APPROVAL_NOTE_REQUIRED = "STOCKTAKE_APPROVAL_NOTE_REQUIRED";

  private ErrorCodes() {}
}
