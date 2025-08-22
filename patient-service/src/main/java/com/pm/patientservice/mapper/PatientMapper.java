package com.pm.patientservice.mapper;

import com.pm.patientservice.dto.PatientRequestDTO;
import com.pm.patientservice.dto.PatientResponseDTO;
import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDate;
import java.util.UUID;

public class PatientMapper {
  private static final Logger LOGGER = LoggerFactory.getLogger(PatientMapper.class);

  public static PatientResponseDTO toPatientResponseDTO(Patient p) {
    if (p == null) {
      throw new IllegalArgumentException("patient object is null");
    }
    PatientResponseDTO dto = new PatientResponseDTO();
    dto.setId(p.getId().toString());
    dto.setFirstname(p.getFirstname());
    dto.setLastname(p.getLastname());
    dto.setEmail(p.getEmail());
    dto.setPhoneNumber(p.getPhoneNumber());
    dto.setBirthDate(p.getBirthDate().toString()); // Assuming birthDate is a LocalDate
    return dto;
  }

  public static Patient toPatient(PatientRequestDTO dto) {
    if (dto == null) {
      throw new IllegalArgumentException("PatientResponseDTO object is null");
    }
    LOGGER.info("Mapping PatientRequestDTO to Patient: {}", dto);

    Patient p = new Patient();

    try{
      if(dto.getId() != null) {
        p.setId(UUID.fromString(dto.getId()));
      }

      p.setFirstname(HtmlUtils.htmlEscape(dto.getFirstname()));
      p.setLastname(HtmlUtils.htmlEscape(dto.getLastname()));
      p.setEmail(HtmlUtils.htmlEscape(dto.getEmail()));
      p.setPhoneNumber(HtmlUtils.htmlEscape(dto.getPhoneNumber()));
      p.setBirthDate(LocalDate.parse(HtmlUtils.htmlEscape(dto.getBirthDate()))); // Assuming birthDate is a LocalDate
    } catch (Exception e) {

      LOGGER.error("Error mapping PatientResponseDTO to Patient: {}", e.getMessage());
      throw new RuntimeException(e);
    }

    LOGGER.info("Mapped PatientRequestDTO to Patient: {}", p);

    return p;
  }
}
