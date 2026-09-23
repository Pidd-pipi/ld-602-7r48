package com.generated.rescueStock.controllers;

import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.exceptions.StocktakeBusinessException;
import com.generated.rescueStock.services.StocktakeOrderService;
import com.generated.rescueStock.types.StocktakeReviewPayload;
import com.generated.rescueStock.types.StocktakeSubmitPayload;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 库存批次盘点差异复核：发起、待审列表、复核（通过 / 驳回）、差异详情。 */
@RestController
@RequestMapping("/api/stocktake-order")
public class StocktakeOrderController {
  private final StocktakeOrderService service;

  public StocktakeOrderController(StocktakeOrderService service) {
    this.service = service;
  }

  /** 仓库员录入实盘数并提交盘点。 */
  @PostMapping("/submit")
  public Map<String, Object> submit(@RequestBody(required = false) StocktakeSubmitPayload payload) {
    return service.submit(payload);
  }

  /** 差异列表，可按批次过滤；库存页刷新后据此回读。 */
  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(value = "batchId", required = false) Long batchId) {
    return service.list(batchId);
  }

  @GetMapping("/{id}")
  public Map<String, Object> detail(@PathVariable Long id) {
    return service.get(id);
  }

  /** 审批员复核：approve 通过并落账，否则驳回。 */
  @PostMapping("/{id}/review")
  public Map<String, Object> review(@PathVariable Long id,
                                    @RequestBody(required = false) StocktakeReviewPayload payload) {
    return service.review(id, payload);
  }

  /** Controller 层包装：同一批次重复待审、盘盈回补冻结批次等冲突返回 409。 */
  @ExceptionHandler(StocktakeBusinessException.class)
  public ResponseEntity<Map<String, String>> handleBusiness(StocktakeBusinessException ex) {
    HttpStatus status = switch (ex.getCode()) {
      case ErrorCodes.STOCKTAKE_BATCH_NOT_FOUND, ErrorCodes.STOCKTAKE_ORDER_NOT_FOUND -> HttpStatus.NOT_FOUND;
      case ErrorCodes.STOCKTAKE_PENDING_EXISTS,
           ErrorCodes.STOCKTAKE_NOT_PENDING,
           ErrorCodes.STOCKTAKE_SURPLUS_FORBIDDEN -> HttpStatus.CONFLICT;
      default -> HttpStatus.BAD_REQUEST;
    };
    return ResponseEntity.status(status).body(Map.of(
        "code", ex.getCode(),
        "message", ex.getMessage()
    ));
  }
}
