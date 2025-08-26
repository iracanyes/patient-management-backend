package com.pm.patientservice.kafka;

import com.pm.patientservice.events.proto.PatientEvent;
import com.pm.patientservice.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducer.class);
  private final KafkaTemplate<String, byte[]> kafkaTemplate;

  public KafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendEvent(String topic, String eventType, Patient patient) {
    PatientEvent patientEvent = PatientEvent.newBuilder()
        .setPatientId(patient.getId().toString())
        .setFirstname(patient.getFirstname())
        .setLastname(patient.getLastname())
        .setEmail(patient.getEmail())
        .setEventType(eventType)
        .build();

    try{
      // Send the serialized PatientEvent to the specified Kafka topic
      kafkaTemplate.send(topic, patientEvent.toByteArray());
    } catch (Exception e) {
      LOGGER.error("Error while sending message to topic {}: {}", topic, e.getMessage());
    }
  }
}
