package com.generated.rescueStock.constants;

/** 每个实体至少 4 条日志模板；盘点单的全部写操作都在此留痕，禁止在调用处散写。 */
public final class LogTemplates {
  public static final String CREATE = "create";
  public static final String UPDATE = "update";
  public static final String STATUS = "status";
  public static final String EXPORT = "export";

  /** 盘点单提交：参数顺序 actor, stocktakeId, batchId, bookQty, actualQty, variance。 */
  public static final String STOCKTAKE_SUBMIT = "stocktake submit by %s: stocktakeId=%s batchId=%s bookQty=%s actualQty=%s variance=%s";
  /** 盘点单重复提交被拦截：actor, batchId, existingStocktakeId。 */
  public static final String STOCKTAKE_DUPLICATE_BLOCKED = "stocktake duplicate submit blocked for %s: batchId=%s pendingStocktakeId=%s";
  /** 盘点单审批通过并落账：actor, stocktakeId, batchId, oldQty, newQty。 */
  public static final String STOCKTAKE_APPROVE = "stocktake approved by %s: stocktakeId=%s batchId=%s quantity %s -> %s";
  /** 盘点单驳回：actor, stocktakeId, batchId, note。 */
  public static final String STOCKTAKE_REJECT = "stocktake rejected by %s: stocktakeId=%s batchId=%s note=%s";
  /** 盘点单重复 / 并发审批只成功一次，失败者不改数据：actor, stocktakeId, status。 */
  public static final String STOCKTAKE_REAPPROVE_BLOCKED = "stocktake re-approval blocked for %s: stocktakeId=%s currentStatus=%s";

  private LogTemplates() {}
}
