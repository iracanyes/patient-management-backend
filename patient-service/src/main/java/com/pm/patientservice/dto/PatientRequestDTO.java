package com.pm.patientservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.ToString;


@ToString
public class PatientRequestDTO {

  private String id;

  @NotBlank(message = "Firstname is required")
  @Size(min = 2, max = 100, message = "Firstname must be between 2 and 50 characters")
  private String firstname;

  @Size(min = 2, max = 100, message = "Lastname must be between 2 and 50 characters")
  @NotBlank(message = "Lastname is required")
  private String lastname;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;

  @NotBlank(message = "Phone number is required")
  private String phoneNumber;

  @NotBlank(message = "Birth date is required")
  private String birthDate;

  public PatientRequestDTO() {
  }

  public PatientRequestDTO(String firstname, String lastname, String email, String phoneNumber, String birthDate) {
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.birthDate = birthDate;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(String birthDate) {
    this.birthDate = birthDate;
  }
}
