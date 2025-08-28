package com.pm.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiException {
  private int status;
  private String message;
}
