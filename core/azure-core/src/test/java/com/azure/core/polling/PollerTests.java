    package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.function.Supplier;

    public class PollerTests {


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

/*
    @Test
    public void basicPollUntilDone_completeInNSecondsTest() throws Exception {

        PollResponse myPollResponse = new MyPollResponse(true,false,false,true,false);
        SerializableSupplier<PollResponse> serviceSupplier = () -> myPollResponse;

        int timeoutInMilliSeconds = 1000*60*5;
        int pollIntervalInMillis = 10;
        float poolIntervalGrowthFactor =1.0f;
        boolean operationAllowedToCancel =true;
        // Lambda Runnable
        Runnable callbackWhenDone = () -> {
            System.out.println(Thread.currentThread().getName() + " is running. Callback when done.");
        };
        Runnable callbackToCancelOperation = () -> {
            System.out.println(Thread.currentThread().getName() + " is running. Callback to cancel.");
        };
        Poller poller =  new Poller(timeoutInMilliSeconds,pollIntervalInMillis,poolIntervalGrowthFactor , serviceSupplier,callbackWhenDone,callbackToCancelOperation,operationAllowedToCancel); // 5 minutes

        Mono<PollResponse> pollResponseMono = poller.pollUntilDone();
        // now a thread to complete the operation after 5 seconds
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            PollResponse pollResponse = myPollResponse;
            @Override
            public void run() {
                // code in here
                System.out.println("Test: sleep for 5 second");
                try{Thread.sleep(1000*5);}catch (Exception e) {}

                System.out.println("Test: Manually making operation successfully complete.");
                ((MyPollResponse)myPollResponse).setOperationInProgress(false);
                ((MyPollResponse)myPollResponse).setOperationSuccessfullyComplete(true);
            }
        });
        pollResponseMono.subscribe(
            pollResponse ->  Assert.assertTrue(pollResponse.isOperationSuccessfullyComplete())  ,
            error ->  System.out.println("Error : "+error.getMessage()),
            () ->  System.out.println("Mono consumed: Operation is  complete")
        );

    }// basicPollOnceTest
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

    class MyCustomServiceResponse  {
        String response ;
        HttpResponseException error;
        public MyCustomServiceResponse(  String respone){
            this.response=respone;
        }

        public String getResponse(){
            return response;
        }



    }
}
