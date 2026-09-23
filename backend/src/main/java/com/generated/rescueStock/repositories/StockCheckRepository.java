package com.generated.rescueStock.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.generated.rescueStock.models.StockCheck;

public interface StockCheckRepository extends JpaRepository<StockCheck, Long> {

  /** 同一批次是否已有待审盘点（pendingBatchId 唯一约束的查询侧） */
  boolean existsByPendingBatchId(Long pendingBatchId);

  Optional<StockCheck> findByPendingBatchId(Long pendingBatchId);

  List<StockCheck> findAllByOrderByCreatedAtDescIdDesc();

  List<StockCheck> findByBatchIdOrderByCreatedAtDesc(Long batchId);

  long countByPendingBatchId(Long pendingBatchId);
}
