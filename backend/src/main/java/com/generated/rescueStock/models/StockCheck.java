package com.generated.rescueStock.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;

/**
 * 库存批次盘点单：仓库录入实盘数后提交为 PENDING（差异保留待审），
 * 审批通过才把批次数量调整为实盘数。
 * pendingBatchId 仅在 PENDING 时等于 batchId，审批/驳回后置空，
 * 配合唯一约束保证同一批次同时只有一张待审盘点。
 */
@Entity
@Table(name = "stock_check", uniqueConstraints = {
    @UniqueConstraint(name = "uk_stock_check_pending_batch", columnNames = "pending_batch_id")
})
public class StockCheck {
  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_APPROVED = "APPROVED";
  public static final String STATUS_REJECTED = "REJECTED";

  public static final String VARIANCE_SURPLUS = "SURPLUS";
  public static final String VARIANCE_LOSS = "LOSS";
  public static final String VARIANCE_MATCHED = "MATCHED";

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "batch_id", nullable = false)
  private Long batchId;

  /** 提交时的账面数量（冻结快照，审批时据此说明差异） */
  @Column(name = "snapshot_quantity", nullable = false)
  private Integer snapshotQuantity;

  /** 仓库录入的实盘数量 */
  @Column(name = "actual_quantity", nullable = false)
  private Integer actualQuantity;

  /** 差异 = 实盘数 - 账面数（正数盘盈，负数盘亏，0 无差异） */
  @Column(nullable = false)
  private Integer variance;

  /** SURPLUS 盘盈 / LOSS 盘亏 / MATCHED 无差异 */
  @Column(name = "variance_type", nullable = false)
  private String varianceType;

  /** 盘盈依据：盘盈时必填 */
  @Column(name = "gain_basis", length = 500)
  private String gainBasis;

  /** 盘亏依据：盘亏时必填 */
  @Column(name = "loss_basis", length = 500)
  private String lossBasis;

  /** PENDING 待审 / APPROVED 通过 / REJECTED 驳回 */
  @Column(nullable = false)
  private String status;

  @Column(name = "submitted_by")
  private String submittedBy;

  @Column(name = "submitted_at")
  private LocalDateTime submittedAt;

  @Column(name = "reviewed_by")
  private String reviewedBy;

  @Column(name = "reviewed_at")
  private LocalDateTime reviewedAt;

  @Column(name = "review_comment", length = 500)
  private String reviewComment;

  /** PENDING 时等于 batchId，其余状态为 null；唯一约束防重 */
  @Column(name = "pending_batch_id")
  private Long pendingBatchId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  /** 乐观锁：并发/重复审批只成功一次 */
  @Version
  private Long version;

  public boolean isPending() { return STATUS_PENDING.equals(status); }

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getBatchId() { return batchId; }
  public void setBatchId(Long batchId) { this.batchId = batchId; }
  public Integer getSnapshotQuantity() { return snapshotQuantity; }
  public void setSnapshotQuantity(Integer snapshotQuantity) { this.snapshotQuantity = snapshotQuantity; }
  public Integer getActualQuantity() { return actualQuantity; }
  public void setActualQuantity(Integer actualQuantity) { this.actualQuantity = actualQuantity; }
  public Integer getVariance() { return variance; }
  public void setVariance(Integer variance) { this.variance = variance; }
  public String getVarianceType() { return varianceType; }
  public void setVarianceType(String varianceType) { this.varianceType = varianceType; }
  public String getGainBasis() { return gainBasis; }
  public void setGainBasis(String gainBasis) { this.gainBasis = gainBasis; }
  public String getLossBasis() { return lossBasis; }
  public void setLossBasis(String lossBasis) { this.lossBasis = lossBasis; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getSubmittedBy() { return submittedBy; }
  public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
  public LocalDateTime getSubmittedAt() { return submittedAt; }
  public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
  public String getReviewedBy() { return reviewedBy; }
  public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
  public LocalDateTime getReviewedAt() { return reviewedAt; }
  public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
  public String getReviewComment() { return reviewComment; }
  public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
  public Long getPendingBatchId() { return pendingBatchId; }
  public void setPendingBatchId(Long pendingBatchId) { this.pendingBatchId = pendingBatchId; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }

  /** 便于服务层按提交日期判断批次是否已过期，与实体字段无关 */
  public static boolean isExpired(LocalDate expireAt, LocalDate today) {
    return expireAt != null && expireAt.isBefore(today);
  }
}
