package com.pm.patientservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "patients")
public class Patient {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "patient_id", updatable = false, nullable = false)
  private UUID id;

  @NotBlank(message = "Firstname is required")
  @Size(min = 2, max = 100, message = "Firstname must be between 2 and 100 characters")
  @Column(name = "firstname", nullable = false)
  private String firstname;

  @NotBlank(message = "Lastname is required")
  @Size(min = 2, max = 100, message = "Lastname must be between 2 and 100 characters")
  @Column(name = "lastname", nullable = false)
  private String lastname;

  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @NotBlank(message = "Phone number is required")
  @Size(max = 15, message = "Phone number must not exceed 15 characters")
  @ToString.Exclude
  @Column(name = "phone_number", nullable = false)
  private String phoneNumber;

  @NotNull
  @Column(name = "birthdate", nullable = false)
  private LocalDate birthDate;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
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

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public void setBirthDate(LocalDate birthDate) {
    this.birthDate = birthDate;
  }
}
