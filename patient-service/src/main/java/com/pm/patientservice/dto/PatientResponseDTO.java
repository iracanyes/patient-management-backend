package com.pm.patientservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PatientResponseDTO {
  private String id;
  @NotNull
  private String firstname;

  @NotNull
  private String lastname;

  @NotNull
  private String email;

  private String phoneNumber;

  @NotNull
  private String birthDate;
}
