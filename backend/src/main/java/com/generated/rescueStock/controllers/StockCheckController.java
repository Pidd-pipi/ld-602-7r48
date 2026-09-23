package com.generated.rescueStock.controllers;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.generated.rescueStock.services.StockCheckService;
import com.generated.rescueStock.types.StockCheckReviewPayload;
import com.generated.rescueStock.types.StockCheckSubmitPayload;

/**
 * 库存批次盘点差异复核：
 * POST   /api/stock-check               仓库录入实盘数并提交（差异保留待审）
 * GET    /api/stock-check               查看差异单（可按批次、状态过滤）
 * GET    /api/stock-check/{id}          差异明细（刷新后回读）
 * POST   /api/stock-check/{id}/approve  复核通过，批次数量调整为实盘数
 * POST   /api/stock-check/{id}/reject   复核驳回，批次数量不变
 */
@RestController
@RequestMapping("/api/stock-check")
public class StockCheckController {
  private final StockCheckService service;

  public StockCheckController(StockCheckService service) {
    this.service = service;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> submit(@RequestBody StockCheckSubmitPayload payload) {
    return service.submit(payload);
  }

  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(required = false) Long batchId,
                                        @RequestParam(required = false) String status) {
    return service.list(batchId, status);
  }

  @GetMapping("/{id}")
  public Map<String, Object> get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping("/{id}/approve")
  public Map<String, Object> approve(@PathVariable Long id,
                                     @RequestBody(required = false) StockCheckReviewPayload payload) {
    return service.approve(id, payload);
  }

  @PostMapping("/{id}/reject")
  public Map<String, Object> reject(@PathVariable Long id,
                                    @RequestBody(required = false) StockCheckReviewPayload payload) {
    return service.reject(id, payload);
  }
}
