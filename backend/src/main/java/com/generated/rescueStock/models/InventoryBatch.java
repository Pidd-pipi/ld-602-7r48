package com.generated.rescueStock.models;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "inventory_batch")
public class InventoryBatch {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "warehouse_id")
  private Long warehouseId;
  @Column(name = "supply_item_id")
  private Long supplyItemId;
  @Column(name = "batch_no")
  private String batchNo;
  private Integer quantity;
  @Column(name = "expire_at")
  private LocalDate expireAt;
  @Column(name = "inbound_source")
  private String inboundSource;
  @Column(name = "quality_status")
  private String qualityStatus;

  /** 乐观锁：审批调整批次数量时保证只生效一次 */
  @Version
  private Long version;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getWarehouseId() { return warehouseId; }
  public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
  public Long getSupplyItemId() { return supplyItemId; }
  public void setSupplyItemId(Long supplyItemId) { this.supplyItemId = supplyItemId; }
  public String getBatchNo() { return batchNo; }
  public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
  public Integer getQuantity() { return quantity; }
  public void setQuantity(Integer quantity) { this.quantity = quantity; }
  public LocalDate getExpireAt() { return expireAt; }
  public void setExpireAt(LocalDate expireAt) { this.expireAt = expireAt; }
  public String getInboundSource() { return inboundSource; }
  public void setInboundSource(String inboundSource) { this.inboundSource = inboundSource; }
  public String getQualityStatus() { return qualityStatus; }
  public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }
  public Long getVersion() { return version; }
  public void setVersion(Long version) { this.version = version; }
}
