package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

public class PollerTests {


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
    public void basicPollUntilDoneTest() throws Exception {

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

        Mono<PollResponse> pollResponseMono = poller.pollUntilDone();

        pollResponseMono.subscribe(
            pollResponse ->  Assert.assertFalse(pollResponse.isOperationSuccessfullyComplete())  ,
            error ->  System.out.println("Error : "+error.getMessage()),
            () ->  System.out.println("Mono consumed: Operation is not complete")
        );
        System.out.println("Test: sleep for 1 second");
        Thread.sleep(1000);

        System.out.println("Test: Manually making operation successfully complete.");
        ((MyPollResponse)myPollResponse).setOperationSuccessfullyComplete(true);

        //pollResponseMono = poller.pollUntilDone();
       // pollResponseMono.subscribe(
         //   pollResponse ->  Assert.assertTrue(pollResponse.isOperationSuccessfullyComplete())  ,
           // error ->  System.out.println("Error : "+error.getMessage()),
            //() ->  System.out.println("Mono consumed: Operation is now complete.")
        //);

    }// basicPollOnceTest

    class MyPollResponse implements  PollResponse {
        boolean operationSuccessfullyComplete;
        boolean operationInProgress;
        boolean operationCancelled;
        boolean operationFailed;
        boolean operationStarted;
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
            return null;
        }
    }
}
