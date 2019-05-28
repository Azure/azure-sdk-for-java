    package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;

public class PollerTests {

/*
    @Test
    public void basicManualPollOnceTest() throws Exception {

        PollResponse myPollResponse = new MyPollResponse(true,false,false,false,false);
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

        Mono<PollResponse> pollResponseMono = poller.pollOnce();

        pollResponseMono.subscribe(
                pollResponse ->  Assert.assertFalse(pollResponse.isOperationSuccessfullyComplete())  ,
                error ->  System.out.println("Error : "+error.getMessage()),
                () ->  System.out.println("Mono consumed: Operation is not complete yet.")
        );

        System.out.println("Test: Manually making operation successfully complete.");
        ((MyPollResponse)myPollResponse).setOperationSuccessfullyComplete(true);

        pollResponseMono = poller.pollOnce();
        pollResponseMono.subscribe(
            pollResponse ->  Assert.assertTrue(pollResponse.isOperationSuccessfullyComplete())  ,
            error ->  System.out.println("Error : "+error.getMessage()),
            () ->  System.out.println("Mono consumed: Operation is now complete.")
        );

    }// basicPollOnceTest


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
*/
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

    class MyPollResponse implements  PollResponse {
        boolean operationSuccessfullyComplete;
        boolean operationInProgress;
        boolean operationCancelled;
        boolean operationFailed;
        boolean operationStarted;
        HttpResponseException error;
        public MyPollResponse(  boolean operationStarted
                                , boolean operationFailed
                                , boolean operationSuccessfullyComplete
                                , boolean operationInProgress
                                , boolean operationCancelled){
            this.operationSuccessfullyComplete=operationSuccessfullyComplete;
            this.operationInProgress=operationInProgress;
            this.operationCancelled=operationCancelled;;
            this.operationFailed=operationFailed;
            this.operationStarted=operationStarted;
        }

        public void setOperationSuccessfullyComplete (boolean b){
            operationSuccessfullyComplete =b;
        }

        public void setOperationCancelled (HttpResponseException err){
            this.operationCancelled =true;
            this.error = err;
        }


        public void setOperationInProgress (boolean b){
            operationInProgress =b;
        }
        @Override
        public boolean isOperationSuccessfullyComplete() {
            return operationSuccessfullyComplete;
        }

        @Override
        public boolean isOperationCancelled() {
            return operationCancelled;
        }

        @Override
        public boolean isOperationFailed() {
            return operationFailed;
        }


        @Override
        public boolean isOperationStarted() {
            return operationStarted;
        }
        @Override
        public boolean isOperationInProgress() {
            return operationInProgress;
        }

        @Override
        public HttpResponseException error() {
            return error;
        }
    }
}
