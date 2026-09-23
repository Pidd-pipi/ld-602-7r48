package com.generated.rescueStock.exceptions;

import com.generated.rescueStock.constants.ErrorCodes;

public class NotFoundException extends ApiException {
  public NotFoundException(String message) {
    super(ErrorCodes.RESOURCE_NOT_FOUND, message, 404);
  }
}
