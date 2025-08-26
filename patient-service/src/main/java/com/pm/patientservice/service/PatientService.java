package com.pm.patientservice.service;

import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.exception.EmailAlreadyExistsException;
import com.pm.patientservice.exception.NotFoundApiException;
import com.pm.patientservice.exception.PatientNotFoundException;
import com.pm.patientservice.grpc.BillingServiceGrpcClient;
import com.pm.patientservice.kafka.KafkaProducer;
import com.pm.patientservice.mapper.PatientMapper;
import com.pm.patientservice.model.Patient;
import com.pm.patientservice.repository.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PatientService {
  private static final Logger LOGGER = LoggerFactory.getLogger(PatientService.class);
  private final PatientRepository patientRepository;
  private final BillingServiceGrpcClient billingServiceGrpcClient;
  private final KafkaProducer kafkaProducer;

  public PatientService(
    PatientRepository patientRepository,
    BillingServiceGrpcClient billingServiceGrpcClient,
    KafkaProducer kafkaProducer
  ) {
    this.patientRepository = patientRepository;
    this.billingServiceGrpcClient = billingServiceGrpcClient;
    this.kafkaProducer = kafkaProducer;
    LOGGER.info("PatientService initialized with PatientRepository");
  }

  public List<PatientResponseDTO> list(){
    List<PatientResponseDTO> patients = null;
    try{
      List<Patient> result = (List<Patient>) patientRepository.findAll();


      patients = result.stream()
          .filter(Objects::nonNull) // Filter out null patients
          .map(PatientMapper::toPatientResponseDTO)
          .toList();
    }catch (Exception e){
      LOGGER.error(e.getMessage());
    }

    return patients;
  }

  public Patient getPatientByEmail(String email) {
    LOGGER.info("Fetching patient by email: {}", email);
    Patient patient = null;

    try{
      patient = patientRepository.findByEmail(email);
      if(patient != null)
        LOGGER.info("Fetching patient by email: {}", patient.toString());

    } catch (Exception e) {
      LOGGER.error("Error while searching patient by mail : \n {}", e.getMessage());
      throw new RuntimeException(e);
    }

    return patient;
  }

  public PatientResponseDTO create(Patient patient) {
    Patient savedPatient = null;
    LOGGER.info("Creating patient: {}", patient);
    if (patientRepository.existsByEmail(patient.getEmail())) {
      throw new EmailAlreadyExistsException("Patient email already exists: " + patient.getEmail());
    }

    try{
      savedPatient = patientRepository.save(patient);

      // Call the Billing Service via gRPC to create a billing account
      billingServiceGrpcClient.createBillingAccount(
        savedPatient.getId().toString(),
        savedPatient.getFirstname(),
        savedPatient.getLastname(),
        savedPatient.getEmail()
      );

      // Produce a Kafka event for the new patient creation
      kafkaProducer.sendEvent("Patient", "PATIENT_CREATED", savedPatient);

      LOGGER.info("Patient created with ID: {}", savedPatient.getId());

      return PatientMapper.toPatientResponseDTO(savedPatient);

    } catch (Exception e) {
      LOGGER.error("Error while saving patient : {}", e.getMessage());
      throw new RuntimeException(e);
    }
  }

  public PatientResponseDTO update(Patient patient) {
    PatientResponseDTO updatedPatient = null;

    Patient existingPatient = patientRepository.findById(patient.getId())
      .orElseThrow(() -> new PatientNotFoundException("Patient with ID " + patient.getId() + " does not exist"));

    // Check if the email is already in use by another patient
    if(patientRepository.existsByEmail(patient.getEmail()) &&
      !existingPatient.getEmail().equals(patient.getEmail())) {
      throw new EmailAlreadyExistsException("Patient email already exists: " + patient.getEmail());
    }

    existingPatient.setFirstname(patient.getFirstname());
    existingPatient.setLastname(patient.getLastname());
    existingPatient.setPhoneNumber(patient.getPhoneNumber());
    existingPatient.setBirthDate(patient.getBirthDate());

    patientRepository.flush();

    // Produce a Kafka event for the patient update
    kafkaProducer.sendEvent("Patient", "PATIENT_UPDATED", existingPatient);

    updatedPatient = PatientMapper.toPatientResponseDTO(existingPatient);

    return updatedPatient;


  }


  public void delete(UUID id) {
    LOGGER.info("Deleting patient with ID: {}", id);
    patientRepository.deleteById(id);
  }
}
