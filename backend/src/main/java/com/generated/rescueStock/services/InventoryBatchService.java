package com.generated.rescueStock.services;

import com.generated.rescueStock.constructors.InventoryBatchDtoFactory;
import com.generated.rescueStock.repositories.InventoryBatchRepository;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class InventoryBatchService {
  private final InventoryBatchRepository repo;

  public InventoryBatchService(InventoryBatchRepository repo) {
    this.repo = repo;
  }

  public List<Map<String, Object>> list() {
    return repo.findAll().stream().map(InventoryBatchDtoFactory::toMap).toList();
  }
}
