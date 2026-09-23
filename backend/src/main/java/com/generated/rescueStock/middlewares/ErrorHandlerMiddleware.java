package com.generated.rescueStock.middlewares;

import java.time.LocalDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.generated.rescueStock.constants.ErrorCodes;
import com.generated.rescueStock.exceptions.ApiException;

/**
 * 全局错误处理：盘点的重复提交（唯一约束冲突）与并发审批（乐观锁失败）
 * 统一转成 409，调用方看到失败后批次与盘点单均保持原状。
 */
@RestControllerAdvice
public class ErrorHandlerMiddleware {
  private static final Logger log = LoggerFactory.getLogger(ErrorHandlerMiddleware.class);

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
    return ResponseEntity.status(ex.getStatus()).body(body(ex.getCode(), ex.getMessage()));
  }

  @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
  public ResponseEntity<Map<String, Object>> handleOptimistic(ObjectOptimisticLockingFailureException ex) {
    log.warn("optimistic lock conflict: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(body(ErrorCodes.STOCK_CHECK_NOT_PENDING, "盘点单已完成复核，重复或并发审批只有一次生效"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(body(ErrorCodes.STOCK_CHECK_DUPLICATE, "该批次已存在待审盘点单，同一批次同时只能有一张待审盘点"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleOther(Exception ex) {
    log.error("unhandled error", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(body("INTERNAL_ERROR", "服务暂时不可用"));
  }

  private Map<String, Object> body(String code, String message) {
    return Map.of("code", code, "message", message, "timestamp", LocalDateTime.now().toString());
  }
}
