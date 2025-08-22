package com.pm.patientservice.exception;

public class NotFoundApiException extends ApiException {
  public NotFoundApiException() {
    super(404, "Resource not found");
  }

  public NotFoundApiException(String message) {
    super(404, message);
  }
}
