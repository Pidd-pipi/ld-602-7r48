CREATE TABLE IF NOT EXISTS warehouse (
  id INTEGER PRIMARY KEY,
  name TEXT,
  district TEXT,
  address TEXT,
  manager_id TEXT,
  capacity_level TEXT,
  contact_phone TEXT,
  status TEXT
);

CREATE TABLE IF NOT EXISTS supply_item (
  id INTEGER PRIMARY KEY,
  sku_code TEXT,
  name TEXT,
  category TEXT,
  unit TEXT,
  safety_stock TEXT,
  expire_days TEXT,
  storage_requirement TEXT
);

CREATE TABLE IF NOT EXISTS inventory_batch (
  id INTEGER PRIMARY KEY,
  warehouse_id TEXT,
  supply_item_id TEXT,
  batch_no TEXT,
  quantity TEXT,
  expire_at TEXT,
  inbound_source TEXT,
  quality_status TEXT
);

CREATE TABLE IF NOT EXISTS stock_check (
  id INTEGER PRIMARY KEY,
  batch_id INTEGER NOT NULL,
  snapshot_quantity INTEGER NOT NULL,
  actual_quantity INTEGER NOT NULL,
  variance INTEGER NOT NULL,
  variance_type TEXT NOT NULL,
  gain_basis TEXT,
  loss_basis TEXT,
  status TEXT NOT NULL,
  submitted_by TEXT,
  submitted_at TEXT,
  reviewed_by TEXT,
  reviewed_at TEXT,
  review_comment TEXT,
  pending_batch_id INTEGER,
  created_at TEXT
);

-- 同一批次同时只能有一张待审盘点：NULL 不参与唯一约束，审批/驳回后置空即可重新发起
CREATE UNIQUE INDEX IF NOT EXISTS uk_stock_check_pending_batch
  ON stock_check (pending_batch_id);

CREATE TABLE IF NOT EXISTS shelter (
  id INTEGER PRIMARY KEY,
  name TEXT,
  district TEXT,
  capacity TEXT,
  current_population TEXT,
  contact_person TEXT,
  risk_level TEXT,
  open_status TEXT
);

CREATE TABLE IF NOT EXISTS dispatch_order (
  id INTEGER PRIMARY KEY,
  event_id TEXT,
  source_warehouse_id TEXT,
  shelter_id TEXT,
  priority TEXT,
  status TEXT,
  requested_by TEXT,
  approved_by TEXT,
  dispatched_at TEXT
);

CREATE TABLE IF NOT EXISTS audit_log (
  id INTEGER PRIMARY KEY,
  actor TEXT,
  action TEXT,
  target_type TEXT,
  target_id TEXT,
  created_at TEXT
);
