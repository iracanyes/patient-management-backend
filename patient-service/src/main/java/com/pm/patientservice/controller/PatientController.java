package com.pm.patientservice.controller;

import com.pm.patientservice.dto.PatientIdRequestDTO;
import com.pm.patientservice.exception.ApiException;
import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/patients")
@Tag(name = "Patients", description = "Patient management operations")
public class PatientController {
  private static final Logger LOGGER = LoggerFactory.getLogger(PatientController.class);
  private final PatientService patientService;

  public PatientController(PatientService patientService) {
    this.patientService = patientService;
    LOGGER.info("PatientController initialized with PatientService");
  }

  @GetMapping("")
  @Operation(summary = "Get all patients", description = "Fetches a list of all patients")
  public List<PatientResponseDTO> findAll() {
    try{
      return Objects.requireNonNull(patientService.list(), "Patient list should not be null");
    } catch (Exception e) {
      // Log the error and return an empty list or handle it as needed
      LOGGER.error("Error fetching patients: {}", e.getMessage());
      return List.of(); // Return an empty list in case of error
    }
  }

  @PostMapping("/by-email")
  @Operation(summary = "Get patient by email", description = "Fetches a patient by their email address")
  public ResponseEntity getPatientByEmail(@RequestBody PatientRequestDTO body) {
    LOGGER.info("Fetching patient by email: {}", body.getEmail());

    try {
      Patient patient = patientService.getPatientByEmail(Objects.requireNonNull(body.getEmail(), "Email must not be null"));

      if (patient == null) {
        LOGGER.warn("No patient found with email: {}", body.getEmail());
        return new ResponseEntity<>(
          new ApiException(404, "No patient found with the provided email"),
          HttpStatus.NOT_FOUND
        ); // or throw an exception, or return a specific response
      }
      return ResponseEntity.ok().body(patient);
    } catch (Exception e) {
      LOGGER.error("Error while searching patient by email: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Optional.of(new ApiException(400, e.getMessage()))); // Handle this as per your application's error handling strategy
    }

  }

  @PostMapping("/create")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity create(@Valid @RequestBody PatientRequestDTO body) {
    LOGGER.info("Creating patient : {}", body);

    try {
      // Validate the request body
      Patient patient = PatientMapper.toPatient(body);

      PatientResponseDTO createdPatient = patientService.create(patient);

      return ResponseEntity.status(HttpStatus.CREATED)
        .body(createdPatient);

    } catch (Exception e) {
      LOGGER.error("Error creating patient: {}", e.getMessage());

      // Return a 400 Bad Request response with the error message
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiException(400, e.getMessage()));
    }
  }

  @PutMapping("/update")
  public ResponseEntity update(@Valid @RequestBody PatientRequestDTO body) {
    LOGGER.info("Updating patient : {}", body);

    try {
      // Validate the request body
      Patient patient = PatientMapper.toPatient(body);

      PatientResponseDTO updatedPatient = patientService.update(patient);

      return ResponseEntity.ok(updatedPatient);

    } catch (Exception e) {
      LOGGER.error("Error updating patient: {}", e.getMessage());

      // Return a 400 Bad Request response with the error message
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiException(400, e.getMessage()));
    }
  }
  /**
   * Deletes a patient by ID.
   *
   * @param body the ID of the patient to delete
   * @return a ResponseEntity with no content if the deletion was successful
   */
  // Show the usage of @PathVariable to extract the ID from the URL
  // and @DeleteMapping to handle DELETE requests
  // For security reasons, it's better to pass the ID as body in a DELETE request
//  @DeleteMapping("/delete/{id}")
//  public ResponseEntity delete(@PathVariable String id) {
//    LOGGER.info("Deleting patient with ID: {}", id);
//
//    patientService.delete(UUID.fromString(id));
//
//    return ResponseEntity.noContent().build(); // Return 204 No Content on successful deletion
//  }

  @DeleteMapping("/delete")
  public ResponseEntity delete(@RequestBody PatientIdRequestDTO body) {
    LOGGER.info("Deleting patient with ID: {}", body.getId());

    try {
      //
      //UUID id = UUID.fromString(body.getId());
      //patientService.delete(id);

      patientService.delete(body.getId());
      return ResponseEntity.noContent().build(); // Return 204 No Content on successful deletion
    } catch (Exception e) {
      LOGGER.error("Error deleting patient: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(new ApiException(400, e.getMessage()));
    }
  }
}
