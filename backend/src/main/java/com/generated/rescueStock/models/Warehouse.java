package com.generated.rescueStock.models;

import jakarta.persistence.*;

@Entity
@Table(name = "warehouse")
public class Warehouse {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String district;
  private String address;
  @Column(name = "manager_id")
  private String managerId;
  @Column(name = "capacity_level")
  private String capacityLevel;
  @Column(name = "contact_phone")
  private String contactPhone;
  private String status;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getDistrict() { return district; }
  public void setDistrict(String district) { this.district = district; }
  public String getAddress() { return address; }
  public void setAddress(String address) { this.address = address; }
  public String getManagerId() { return managerId; }
  public void setManagerId(String managerId) { this.managerId = managerId; }
  public String getCapacityLevel() { return capacityLevel; }
  public void setCapacityLevel(String capacityLevel) { this.capacityLevel = capacityLevel; }
  public String getContactPhone() { return contactPhone; }
  public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
