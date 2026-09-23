export const ERROR_MESSAGES = {
  AUTH_REQUIRED: "请先登录后再继续操作",
  RBAC_DENIED: "当前角色没有执行该动作的权限",
  VALIDATION_FAILED: "表单字段缺失或格式错误",
  RATE_LIMITED: "请求过于频繁，请稍后再试",
  RESOURCE_NOT_FOUND: "资源不存在或已被处理",
  STOCK_CHECK_DUPLICATE: "该批次已存在待审盘点单，同一批次同时只能有一张待审盘点",
  STOCK_CHECK_GAIN_BLOCKED: "盘盈不得回补质检冻结或已过期批次",
  STOCK_CHECK_NOT_PENDING: "盘点单已完成复核，重复或并发审批只有一次生效"
};
