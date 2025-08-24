package com.pm.patientservice.grpc;

import com.pm.billingservice.proto.BillingServiceGrpc;
import com.pm.billingservice.proto.CreateBillingAccountRequest;
import com.pm.billingservice.proto.CreateBillingAccountResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingServiceGrpcClient {
  // Create a variable to hold the gRPC stub
  private final BillingServiceGrpc.BillingServiceBlockingStub billingServiceBlockingStub;
  private static final Logger LOGGER = LoggerFactory.getLogger(BillingServiceGrpcClient.class.getName());
  /**
   * Constructor to initialize the gRPC client with the server address and port
   * Spring IOC will automatically inject the values from application.yml
   * and the gRPC stub bean
   *
   * gRPC endpoints possible:
   *   - local development: localhost:9090/Billingservice/CreateBillingAccount
   *   - docker compose: billing-service:9090/Billingservice/CreateBillingAccount
   *   - kubernetes: billing-service.default.svc.cluster.local:9090/Billingservice/CreateBillingAccount
   *   - AWS ECS: billing-service.aws.grpc:9090/Billingservice/CreateBillingAccount
   *
   * @param serverAddress
   * @param port
   */
  public BillingServiceGrpcClient(
    @Value("${billing.service.address:localhost}") String serverAddress,
    @Value("${billing.service.port:9001}") int port
  ) {
    LOGGER.info("Connecting to Billing gRPC service at {}:{}", serverAddress, port);

    // Initialize the gRPC stub using the server address and port
    ManagedChannel channel = ManagedChannelBuilder.forAddress(serverAddress, port)
      .usePlaintext() // Disable TLS for simplicity; in production, consider using TLS
      .build();

    billingServiceBlockingStub = BillingServiceGrpc.newBlockingStub(channel);
  }

  public CreateBillingAccountResponse createBillingAccount(
    String patientId,
    String firstname,
    String lastname,
    String email
  ) {
    LOGGER.info("Creating billing account for patient ID: {}", patientId);

    // Build the gRPC request
    CreateBillingAccountRequest request = CreateBillingAccountRequest.newBuilder()
      .setPatientId(patientId)
      .setFirstname(firstname)
      .setLastname(lastname)
      .setEmail(email)
      .build();

    // Call the gRPC service and get the response
    CreateBillingAccountResponse response;

    try {
      response = billingServiceBlockingStub.createBillingAccount(request);
      LOGGER.info("Received response from Billing Service: {}", response);
    } catch (Exception e) {
      LOGGER.error("Error while calling Billing Service gRPC: {}", e.getMessage());
      throw new RuntimeException("Failed to create billing account", e);
    }

    // Map the gRPC response to a local DTO
    return response;
  }
}
