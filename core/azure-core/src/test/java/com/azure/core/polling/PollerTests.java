package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class PollerTests {

/*
    @Test
    public void basicManualPollOnceTest() throws Exception {
        MyCustomServiceResponse myCustomPollResponse = new MyCustomServiceResponse("service data");
        PollResponse<MyCustomServiceResponse> myPollResponse = new PollResponse<MyCustomServiceResponse>(PollResponse.OperationStatus.IN_PROGRESS,myCustomPollResponse);
        Supplier<PollResponse> serviceSupplier = () -> myPollResponse;

        // Lambda Runnable
        Runnable callbackToCancelOperation = () -> {
            System.out.println(Thread.currentThread().getName() + " is running. Callback to cancel.");
        };
        int timeoutInMilliSeconds = 1000*60*5;
        int pollIntervalInMillis = 10;
        float poolIntervalGrowthFactor =1.0f;

        PollerOptions pollerOptions =  new PollerOptions(timeoutInMilliSeconds,pollIntervalInMillis,poolIntervalGrowthFactor);
        Poller<MyCustomServiceResponse> poller =  new Poller( pollerOptions, serviceSupplier,callbackToCancelOperation); // 5 minutes

        Mono<MyCustomServiceResponse> pollResponseMono = poller.pollOnce();

        pollResponseMono.subscribe(
             pollResponse -> Assert.assertTrue(poller.status() == PollResponse.OperationStatus.IN_PROGRESS),
             error ->  System.out.println("Error : "+error.getMessage()),
                () ->  System.out.println("Mono consumed: Operation is not complete yet.")
        );

        System.out.println("Test: Manually making operation successfully complete.");
        myPollResponse.setStatus(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED);

        pollResponseMono = poller.pollOnce();
        pollResponseMono.subscribe(
            pollResponse ->  Assert.assertTrue(poller.status() == PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED)  ,
            error ->  System.out.println("Error : "+error.getMessage()),
            () ->  System.out.println("Mono consumed: Operation is now complete.")
        );

    }// basicPollOnceTest
*/

    @Test
    public void basicPollUntilDone_completeInNSecondsTest() throws Exception {

        CreateCertificateResponse createCertificateResponse = new CreateCertificateResponse("Starting : Cert A");
        PollResponse<CreateCertificateResponse> myPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS,createCertificateResponse);
        int timeoutInMilliSeconds = 1000 * 3; // 3 seconds
        int pollIntervalInMillis = 10;
        float poolIntervalGrowthFactor = 1.0f;

        PollerOptions pollerOptions = new PollerOptions(timeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation = new Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>>() {
            LocalDateTime timeToReturnSuccess= LocalDateTime.now().plusSeconds(10);
            @Override
            public PollResponse<CreateCertificateResponse> apply(PollResponse<CreateCertificateResponse> pollResponse) {

                if (pollResponse.status() == PollResponse.OperationStatus.IN_PROGRESS && LocalDateTime.now().isBefore(timeToReturnSuccess)) {
                    System.out.println(LocalDateTime.now() + " Test pollOperation Triggered. Returning " + pollResponse.status().toString());
                    return pollResponse;
                } else {
                    PollResponse<CreateCertificateResponse> myNewPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
                    System.out.println(LocalDateTime.now()+" Test pollOperation Triggered. Returning "+myNewPollResponse.status().toString());
                    return myNewPollResponse;
                }
            }
        };
        Poller<CreateCertificateResponse> createCertPoller = new Poller(pollerOptions,pollOperation );

        Flux<PollResponse<CreateCertificateResponse>> myCreateCertPollResponseFlux = createCertPoller.poll();

        myCreateCertPollResponseFlux.subscribe(
            pollResponse -> System.out.println(new Date() + "  Test : Got poll Response with status " + pollResponse.status().toString()),
            error -> System.out.println(new Date() + "Error : " + error.getMessage()),
            () -> System.out.println(new Date() + " Mono consumed: Operation is  complete")
        );

    }// basicPollUntilDone_completeInNSecondsTest

/*
    @Test
    public void basicPollUntilDone_cancelInNSecondsTest() throws Exception {

        PollResponse myPollResponse = new MyPollResponse(true,false,false,true,false);
        SerializableSupplier<PollResponse> serviceSupplier = () -> myPollResponse;

        int timeoutInMilliSeconds = 1000*60*5;
        int pollIntervalInMillis = 10;
        float poolIntervalGrowthFactor =1.0f;
        boolean operationAllowedToCancel =true;
        // Lambda Runnable
        Runnable callbackWhenDone = () -> {
            System.out.println(Thread.currentThread().getName() + " is running callback when done.");
        };
        Runnable callbackToCancelOperation = () -> {
            System.out.println(Thread.currentThread().getName() + " is running callback to cancel.");
        };
        Poller poller =  new Poller(timeoutInMilliSeconds,pollIntervalInMillis,poolIntervalGrowthFactor , serviceSupplier,callbackWhenDone,callbackToCancelOperation,operationAllowedToCancel); // 5 minutes

        Mono<PollResponse> pollResponseMono = poller.pollUntilDone();
        // now a thread to complete the operation after 5 seconds
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            PollResponse pollResponse = myPollResponse;
            @Override
            public void run() {
                // code in here
                System.out.println("Test: sleep for 5 second and complete in error");
                try{Thread.sleep(1000*2);}catch (Exception e) {}

                System.out.println("Test: Manually making operation complete in error.");
                ((MyPollResponse)pollResponse).setOperationCancelled(new HttpResponseException("Error", null) );

                ((MyPollResponse)pollResponse).setOperationInProgress(false);
            }
        });
        pollResponseMono.subscribe(
            pollResponse ->  Assert.assertTrue(pollResponse.isOperationCancelled())  ,
            error ->  System.out.println("Error : "+error.getMessage()),
            () ->  System.out.println("Mono consumed: Operation is cancelled")
        );

    }// basicPollOnceTest

 */

    class CreateCertificateResponse {
        String response;
        HttpResponseException error;

        public CreateCertificateResponse(String respone) {
            this.response = respone;
        }

        public String getResponse() {
            return response;
        }
        public void setResponse(String st) {
            response = st;
        }
}
}
