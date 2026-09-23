package com.generated.rescueStock.constants;

public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  /** 库存批次盘点相关操作日志模板 */
  public static final String STOCK_CHECK_SUBMIT = "盘点单提交：batch#%s 账面 %d 实盘 %d 差异 %d(%s)";
  public static final String STOCK_CHECK_APPROVE = "盘点单审批通过：check#%s batch#%s 数量 %d -> %d";
  public static final String STOCK_CHECK_REJECT = "盘点单驳回：check#%s batch#%s 差异保留待审未调整库存";
  public static final String STOCK_CHECK_CONFLICT = "盘点操作冲突（重复提交/重复审批/并发审批）：%s";
}
