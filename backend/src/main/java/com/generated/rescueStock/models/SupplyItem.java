package com.generated.rescueStock.models;

import jakarta.persistence.*;

@Entity
@Table(name = "supply_item")
public class SupplyItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "sku_code")
  private String skuCode;
  private String name;
  private String category;
  private String unit;
  @Column(name = "safety_stock")
  private Integer safetyStock;
  @Column(name = "expire_days")
  private Integer expireDays;
  @Column(name = "storage_requirement")
  private String storageRequirement;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getSkuCode() { return skuCode; }
  public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getUnit() { return unit; }
  public void setUnit(String unit) { this.unit = unit; }
  public Integer getSafetyStock() { return safetyStock; }
  public void setSafetyStock(Integer safetyStock) { this.safetyStock = safetyStock; }
  public Integer getExpireDays() { return expireDays; }
  public void setExpireDays(Integer expireDays) { this.expireDays = expireDays; }
  public String getStorageRequirement() { return storageRequirement; }
  public void setStorageRequirement(String storageRequirement) { this.storageRequirement = storageRequirement; }
}
