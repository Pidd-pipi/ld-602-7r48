package com.generated.rescueStock.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.generated.rescueStock.constants.QualityStatus;
import com.generated.rescueStock.models.InventoryBatch;
import com.generated.rescueStock.models.StockCheck;
import com.generated.rescueStock.models.SupplyItem;
import com.generated.rescueStock.models.Warehouse;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import com.generated.rescueStock.repositories.StockCheckRepository;
import com.generated.rescueStock.repositories.SupplyItemRepository;
import com.generated.rescueStock.repositories.WarehouseRepository;

/** 首次启动写入演示数据：正常批次、已过期批次、质检冻结批次与一张待审盘点单。 */
@Component
@Order(0)
public class DataSeeder implements CommandLineRunner {
  private final WarehouseRepository warehouseRepository;
  private final SupplyItemRepository supplyItemRepository;
  private final InventoryBatchRepository batchRepository;
  private final StockCheckRepository checkRepository;

  public DataSeeder(WarehouseRepository warehouseRepository,
                    SupplyItemRepository supplyItemRepository,
                    InventoryBatchRepository batchRepository,
                    StockCheckRepository checkRepository) {
    this.warehouseRepository = warehouseRepository;
    this.supplyItemRepository = supplyItemRepository;
    this.batchRepository = batchRepository;
    this.checkRepository = checkRepository;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (warehouseRepository.count() > 0) {
      return; // 已有数据（刷新/重启后回读），不重复播种
    }

    Warehouse central = warehouse("朝阳中心应急仓库", "朝阳区", "朝阳路 88 号", "李建国", "LEVEL_1");
    Warehouse north = warehouse("城北储备库", "海淀区", "北清路 210 号", "王敏", "LEVEL_2");
    warehouseRepository.save(central);
    warehouseRepository.save(north);

    SupplyItem water = item("W-0001", "瓶装饮用水", "WATER", "箱", 50, 365, "阴凉避光");
    SupplyItem mask = item("M-0002", "医用口罩", "MEDICAL", "盒", 100, 730, "干燥常温");
    SupplyItem kit = item("M-0003", "应急急救包", "MEDICAL", "个", 30, 540, "常温防潮");
    supplyItemRepository.save(water);
    supplyItemRepository.save(mask);
    supplyItemRepository.save(kit);

    LocalDate today = LocalDate.now();
    InventoryBatch b1 = batch(central.getId(), water.getId(), "W-2026-031", 100, today.plusDays(180), "市级采购入库", QualityStatus.QUALIFIED);
    InventoryBatch b2 = batch(central.getId(), mask.getId(), "M-2025-118", 80, today.minusDays(10), "区级调拨入库", QualityStatus.QUALIFIED);
    InventoryBatch b3 = batch(north.getId(), kit.getId(), "M-2026-007", 50, today.plusDays(60), "社会捐赠入库", QualityStatus.FROZEN);
    InventoryBatch b4 = batch(north.getId(), water.getId(), "W-2026-028", 200, today.plusDays(200), "市级采购入库", QualityStatus.QUALIFIED);
    InventoryBatch b5 = batch(central.getId(), kit.getId(), "M-2026-015", 60, today.plusDays(90), "社会捐赠入库", QualityStatus.QUALIFIED);
    batchRepository.save(b1);
    batchRepository.save(b2);
    batchRepository.save(b3);
    batchRepository.save(b4);
    batchRepository.save(b5);

    // b5 已有一张待审盘点：演示“同一批次同时只能有一张待审盘点”
    StockCheck pending = new StockCheck();
    pending.setBatchId(b5.getId());
    pending.setSnapshotQuantity(60);
    pending.setActualQuantity(65);
    pending.setVariance(5);
    pending.setVarianceType(StockCheck.VARIANCE_SURPLUS);
    pending.setGainBasis("捐赠箱数清点多出 5 个，随箱捐赠清单可查");
    pending.setStatus(StockCheck.STATUS_PENDING);
    pending.setSubmittedBy("赵仓库");
    LocalDateTime now = LocalDateTime.now();
    pending.setSubmittedAt(now);
    pending.setCreatedAt(now);
    pending.setPendingBatchId(b5.getId());
    checkRepository.save(pending);
  }

  private Warehouse warehouse(String name, String district, String address, String manager, String level) {
    Warehouse warehouse = new Warehouse();
    warehouse.setName(name);
    warehouse.setDistrict(district);
    warehouse.setAddress(address);
    warehouse.setManagerId(manager);
    warehouse.setCapacityLevel(level);
    warehouse.setContactPhone("010-8800" + (1000 + name.length() * 7));
    warehouse.setStatus("ACTIVE");
    return warehouse;
  }

  private SupplyItem item(String sku, String name, String category, String unit, int safety, int expireDays, String storage) {
    SupplyItem item = new SupplyItem();
    item.setSkuCode(sku);
    item.setName(name);
    item.setCategory(category);
    item.setUnit(unit);
    item.setSafetyStock(safety);
    item.setExpireDays(expireDays);
    item.setStorageRequirement(storage);
    return item;
  }

  private InventoryBatch batch(Long warehouseId, Long itemId, String batchNo, int quantity,
                               LocalDate expireAt, String source, String qualityStatus) {
    InventoryBatch batch = new InventoryBatch();
    batch.setWarehouseId(warehouseId);
    batch.setSupplyItemId(itemId);
    batch.setBatchNo(batchNo);
    batch.setQuantity(quantity);
    batch.setExpireAt(expireAt);
    batch.setInboundSource(source);
    batch.setQualityStatus(qualityStatus);
    return batch;
  }
}
