package com.pm.patientservice.dto;

import java.util.UUID;

public class PatientIdRequestDTO {
  private UUID id;

  public PatientIdRequestDTO() {
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }
}
