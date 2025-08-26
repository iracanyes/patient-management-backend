package com.pm.analyticsservice.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.patientservice.events.proto.PatientEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

  /**
   * Method to consume events from the "Patient" topic.
   * {@code @KafkaListener} annotation is used to specify the topic and group ID.
   * Spring will automatically register this method as a listener for the specified topic.
   *
   * @param event
   */
  @KafkaListener(topics = "Patient", groupId = "analytics-service")
  public void consumeEvent(byte[] event) {
    // Process the event (e.g., update analytics data)
    LOGGER.info("Received event: " + new String(event));

    try {
      PatientEvent patientEvent = PatientEvent.parseFrom(event);

      LOGGER.info("Received patient event: " + patientEvent);
    } catch (InvalidProtocolBufferException e) {
      LOGGER.error("Error while parsing event", e);
      throw new RuntimeException(e);
    }
  }
}
