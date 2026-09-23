export const mockData = {
  "warehouse": [
    {
      "id": 1,
      "name": "name 1",
      "district": "district 1",
      "address": "address 1",
      "manager_id": 1,
      "capacity_level": 92,
      "contact_phone": "13800000001",
      "status": "SUBMITTED"
    },
    {
      "id": 2,
      "name": "name 2",
      "district": "district 2",
      "address": "address 2",
      "manager_id": 2,
      "capacity_level": 104,
      "contact_phone": "13800000002",
      "status": "APPROVED"
    },
    {
      "id": 3,
      "name": "name 3",
      "district": "district 3",
      "address": "address 3",
      "manager_id": 3,
      "capacity_level": 116,
      "contact_phone": "13800000003",
      "status": "DRAFT"
    }
  ],
  "supplyItem": [
    {
      "id": 1,
      "sku_code": "sku code 1",
      "name": "name 1",
      "category": "WATER",
      "unit": "unit 1",
      "safety_stock": "safety stock 1",
      "expire_days": "expire days 1",
      "storage_requirement": "storage requirement 1"
    },
    {
      "id": 2,
      "sku_code": "sku code 2",
      "name": "name 2",
      "category": "MEDICAL",
      "unit": "unit 2",
      "safety_stock": "safety stock 2",
      "expire_days": "expire days 2",
      "storage_requirement": "storage requirement 2"
    },
    {
      "id": 3,
      "sku_code": "sku code 3",
      "name": "name 3",
      "category": "SHELTER",
      "unit": "unit 3",
      "safety_stock": "safety stock 3",
      "expire_days": "expire days 3",
      "storage_requirement": "storage requirement 3"
    }
  ],
  "inventoryBatch": [
    {
      "id": 1,
      "warehouse_id": 1,
      "supply_item_id": 1,
      "batch_no": "W-2026-031",
      "quantity": 100,
      "expire_at": "2027-03-22",
      "inbound_source": "市级采购入库",
      "quality_status": "QUALIFIED"
    },
    {
      "id": 2,
      "warehouse_id": 1,
      "supply_item_id": 2,
      "batch_no": "M-2025-118",
      "quantity": 80,
      "expire_at": "2026-09-13",
      "inbound_source": "区级调拨入库",
      "quality_status": "QUALIFIED"
    },
    {
      "id": 3,
      "warehouse_id": 2,
      "supply_item_id": 3,
      "batch_no": "M-2026-007",
      "quantity": 50,
      "expire_at": "2026-11-22",
      "inbound_source": "社会捐赠入库",
      "quality_status": "FROZEN"
    }
  ],
  "stockCheck": [
    {
      "id": 1,
      "batch_id": 1,
      "snapshot_quantity": 100,
      "actual_quantity": 96,
      "variance": -4,
      "variance_type": "LOSS",
      "loss_basis": "搬运破损 3 箱、抽检留样 1 箱",
      "status": "PENDING",
      "submitted_by": "赵仓库",
      "submitted_at": "2026-09-22T10:00:00",
      "batch_no": "W-2026-031",
      "warehouse_id": 1,
      "warehouse_name": "name 1",
      "supply_item_name": "name 1",
      "current_quantity": 100
    }
  ],
  "shelter": [
    {
      "id": 1,
      "name": "name 1",
      "district": "district 1",
      "capacity": 92,
      "current_population": 92,
      "contact_person": "contact person 1",
      "risk_level": "LOW",
      "open_status": "SUBMITTED"
    },
    {
      "id": 2,
      "name": "name 2",
      "district": "district 2",
      "capacity": 104,
      "current_population": 104,
      "contact_person": "contact person 2",
      "risk_level": "MEDIUM",
      "open_status": "APPROVED"
    },
    {
      "id": 3,
      "name": "name 3",
      "district": "district 3",
      "capacity": 116,
      "current_population": 116,
      "contact_person": "contact person 3",
      "risk_level": "HIGH",
      "open_status": "DRAFT"
    }
  ],
  "dispatchOrder": [
    {
      "id": 1,
      "event_id": 1,
      "source_warehouse_id": 1,
      "shelter_id": 1,
      "priority": "priority 1",
      "status": "SUBMITTED",
      "requested_by": "requested by 1",
      "approved_by": "approved by 1",
      "dispatched_at": "2026-06-11T09:00:00Z"
    },
    {
      "id": 2,
      "event_id": 2,
      "source_warehouse_id": 2,
      "shelter_id": 2,
      "priority": "priority 2",
      "status": "APPROVED",
      "requested_by": "requested by 2",
      "approved_by": "approved by 2",
      "dispatched_at": "2026-06-12T09:00:00Z"
    },
    {
      "id": 3,
      "event_id": 3,
      "source_warehouse_id": 3,
      "shelter_id": 3,
      "priority": "priority 3",
      "status": "DRAFT",
      "requested_by": "requested by 3",
      "approved_by": "approved by 3",
      "dispatched_at": "2026-06-13T09:00:00Z"
    }
  ]
} as const;
