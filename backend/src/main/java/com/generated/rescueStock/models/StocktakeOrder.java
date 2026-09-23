package com.generated.rescueStock.models;

import com.generated.rescueStock.constants.StocktakeStatus;
import com.generated.rescueStock.constants.StocktakeVariance;

/**
 * 库存批次盘点差异复核单。
 *
 * <p>提交时对账面数量做快照（bookQuantity），审批期间批次数量保持不变；
 * 只有状态从 PENDING 复核为 APPROVED 时才把批次数量调整为 actualQuantity。</p>
 *
 * <p>同一批次同时只允许存在一张 PENDING 盘点单。</p>
 */
public class StocktakeOrder {
  private Long id;
  private Long batchId;
  /** 提交时的批次账面数量快照，作为差异计算与审批落账的基准。 */
  private int bookQuantity;
  /** 仓库员录入的实盘数。 */
  private int actualQuantity;
  /** 实盘 - 账面。正数盘盈，负数盘亏，零为一致。 */
  private int varianceQuantity;
  /** 差异方向枚举名：SURPLUS / LOSS / MATCH。 */
  private String variance;
  /** 盘点单状态枚举名：PENDING / APPROVED / REJECTED。 */
  private String status = StocktakeStatus.PENDING.name();
  /** 仓库员填写的盘盈 / 盘亏依据。 */
  private String reason;
  private String submittedBy;
  private String submittedAt;
  /** 审批员。 */
  private String reviewedBy;
  /** 审批意见（复核依据）。 */
  private String reviewNote;
  private String reviewedAt;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public int getBookQuantity() { return bookQuantity; }
  public void setBookQuantity(int bookQuantity) { this.bookQuantity = bookQuantity; }
  public int getActualQuantity() { return actualQuantity; }
  public void setActualQuantity(int actualQuantity) { this.actualQuantity = actualQuantity; }
  public int getVarianceQuantity() { return varianceQuantity; }
  public void setVarianceQuantity(int varianceQuantity) { this.varianceQuantity = varianceQuantity; }
  public String getVariance() { return variance; }
  public void setVariance(String variance) { this.variance = variance; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getReason() { return reason; }
  public void setReason(String reason) { this.reason = reason; }
  public String getSubmittedBy() { return submittedBy; }
  public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
  public String getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(String submittedAt) { this.submittedAt = submittedAt; }
  public String getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
  public String getReviewNote() { return reviewNote; }
  public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
  public String getReviewedAt() { return reviewedAt; }
  public void setReviewedAt(String reviewedAt) { this.reviewedAt = reviewedAt; }

  /** 盘盈（实盘 > 账面）。盘盈不得回补质检冻结或已过期批次。 */
  public boolean isSurplus() {
    return StocktakeVariance.SURPLUS.name().equals(variance);
  }

  /** 盘亏（实盘 < 账面）。盘亏可照常提交。 */
  public boolean isLoss() {
    return StocktakeVariance.LOSS.name().equals(variance);
  }
}
