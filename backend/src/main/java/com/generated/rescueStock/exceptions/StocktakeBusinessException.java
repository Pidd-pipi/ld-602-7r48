package com.generated.rescueStock.exceptions;

/**
 * 盘点复核业务异常。Service 层抛出，Controller 层分别包装为错误响应，
 * 不依赖全局异常处理器吞掉全部异常。
 */
public class StocktakeBusinessException extends RuntimeException {
  private final String code;

  public StocktakeBusinessException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
