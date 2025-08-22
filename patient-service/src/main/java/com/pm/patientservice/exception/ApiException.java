package com.pm.patientservice.exception;

public class ApiException{
  private int statusCode;
  private String message;
  private Object data;

  public ApiException() {
  }

  public ApiException(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
    this.data = null;
  }

  public ApiException(int statusCode, String message, Object data) {
    this.statusCode = statusCode;
    this.message = message;
    this.data = data;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Object getData() {
    return data;
  }

  public void setData(Object data) {
    this.data = data;
  }
}
