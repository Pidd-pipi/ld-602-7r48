package com.generated.rescueStock.exceptions;

import com.generated.rescueStock.constants.ErrorCodes;

public class ValidationException extends ApiException {
  public ValidationException(String message) {
    super(ErrorCodes.VALIDATION_FAILED, message, 400);
  }
}
