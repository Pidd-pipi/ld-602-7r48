package com.generated.rescueStock.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.generated.rescueStock.models.InventoryBatch;

public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
  List<InventoryBatch> findByWarehouseIdOrderByIdAsc(Long warehouseId);
  List<InventoryBatch> findAllByOrderByIdAsc();
}
