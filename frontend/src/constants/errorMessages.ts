export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  STOCKTAKE_BATCH_NOT_FOUND: "盘点失败：库存批次不存在",
  STOCKTAKE_PENDING_EXISTS: "该批次已有一张待审盘点，同一批次不能重复提交",
  STOCKTAKE_QUANTITY_INVALID: "实盘数必须为不小于 0 的整数",
  STOCKTAKE_SURPLUS_FORBIDDEN: "盘盈不得回补质检冻结或已过期批次",
  STOCKTAKE_REASON_REQUIRED: "盘盈 / 盘亏必须分别写明依据后才能提交",
  STOCKTAKE_ORDER_NOT_FOUND: "盘点单不存在或已被移除",
  STOCKTAKE_NOT_PENDING: "盘点单已复核，重复或并发审批只成功一次，数据未变更",
  STOCKTAKE_APPROVAL_NOTE_REQUIRED: "复核意见必填：请写明通过或驳回的依据"
} as const;
