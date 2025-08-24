package com.pm.billingservice.grpc;

import com.pm.billingservice.proto.BillingServiceGrpc;
import com.pm.billingservice.proto.CreateBillingAccountRequest;
import com.pm.billingservice.proto.CreateBillingAccountResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

@GrpcService
public class BillingGrpcService extends BillingServiceGrpc.BillingServiceImplBase {
  private static final Logger LOGGER = LoggerFactory.getLogger(BillingGrpcService.class);
  /**
   * {@code StreamObserver} allow us to send a response back to the client asynchronously, using bidirectional streaming, or in a single response.
   * @param billingAccountRequest
   * @param responseObserver
   */
  @Override
  public void createBillingAccount(CreateBillingAccountRequest billingAccountRequest, StreamObserver<CreateBillingAccountResponse> responseObserver) {
    LOGGER.info("Billing Service gRPC : createBillingAccount for ID : {}", billingAccountRequest.getPatientId());
    // Implement the logic to create a billing account


    // For now, we will just return a dummy response
    CreateBillingAccountResponse response = CreateBillingAccountResponse.newBuilder()
        .setAccountId("a9e92f60-e466-4d30-9b1a-1602bc790fe9")
        .setStatus(Integer.toString(204))
        .setMessage("Billing account created successfully")
        .build();

    // Here we use the responseObserver to send the response back to the client
    responseObserver.onNext(response);

    // Finally, we complete the response observer to indicate that we are done sending responses
    responseObserver.onCompleted();
  }


}
