package com.generated.rescueStock.models;

import com.generated.rescueStock.constants.QualityStatus;

/**
 * 库存批次。数量调整只能由盘点审批通过等受控动作触发，
 * 仓库员提交盘点时只登记实盘数，不得直接改 quantity。
 */
public class InventoryBatch {
  private Long id;
  private Long warehouseId;
  private Long supplyItemId;
  private String batchNo;
  private int quantity;
  private String expireAt;
  private String inboundSource;
  private String qualityStatus = QualityStatus.NORMAL.name();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Long getWarehouseId() { return warehouseId; }
  public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
  public Long getSupplyItemId() { return supplyItemId; }
  public void setSupplyItemId(Long supplyItemId) { this.supplyItemId = supplyItemId; }
  public String getBatchNo() { return batchNo; }
  public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
  public int getQuantity() { return quantity; }
  public void setQuantity(int quantity) { this.quantity = quantity; }
  public String getExpireAt() { return expireAt; }
  public void setExpireAt(String expireAt) { this.expireAt = expireAt; }
  public String getInboundSource() { return inboundSource; }
  public void setInboundSource(String inboundSource) { this.inboundSource = inboundSource; }
  public String getQualityStatus() { return qualityStatus; }
  public void setQualityStatus(String qualityStatus) { this.qualityStatus = qualityStatus; }
}
