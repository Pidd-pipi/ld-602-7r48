package com.generated.rescueStock.exceptions;

import com.generated.rescueStock.constants.ErrorCodes;

/** 重复提交、重复审批或并发冲突：状态 409，批次与盘点单保持不变。 */
public class ConflictException extends ApiException {
  public ConflictException(String code, String message) {
    super(code, message, 409);
  }
}
