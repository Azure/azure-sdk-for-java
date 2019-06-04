package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.polling.PollResponse.OperationStatus;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class PollerTests {


    private Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> createPollOperation(
        PollResponse<CreateCertificateResponse> intermediateProgressPollResponse,
        PollResponse<CreateCertificateResponse> finalPollResponse,
        int pollIntervalInMillis,
        int sendFinalResponseInSeconds
    ) {
        return new Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>>() {
            // Will return success after this time.
            LocalDateTime timeToReturnSuccess = LocalDateTime.now().plusSeconds(sendFinalResponseInSeconds);

            @Override
            public PollResponse<CreateCertificateResponse> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
                if (LocalDateTime.now().isBefore(timeToReturnSuccess)) {
                    System.out.println(new Date()+" Test: Service poll function called .. returning intermediate response ");
                    return intermediateProgressPollResponse;
                } else {
                    System.out.println(new Date()+" Test: Service poll function called .. returning final response ");
                    return finalPollResponse;
                }
            }
        };
    }


    /* Test where SDK Client is subscribed all responses.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in-progress response.
     **/
    @Test
    public void subscribeToAllPollEvent_SuccessfullyCompleteInNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 6;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                totalTimeoutInMilliSeconds / 1000 + 1);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);

        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertTrue(results.contains(successPollResponse));
                assertTrue(results.size() <= totalTimeoutInMilliSeconds / pollIntervalInMillis + 1);
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
    }

    /* Test where SDK Client is subscribed to only final/last response.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in progress response.
     * But the subscriber is only interested in last response, The test will ensure subscriber
     * only gets last PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED . */

    @Test
    public void subscribeToOnlyFinalEvent_SuccessfullyCompleteInNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 6;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                pollIntervalInMillis / 1000 + 1);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);

        StepVerifier.create(createCertPoller.block())
            .recordWith(ArrayList::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertFalse(results.contains(inProgressPollResponse));
                assertTrue(results.contains(successPollResponse));
                assertTrue(results.size() == 1);
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and subscriber should never get final successful response.
     **/
    @Test
    public void subscribeToAllPollEvent_StopPollingAfterNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds-pollIntervalInMillis)/1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(pollIntervalInMillis + (pollIntervalInMillis/2) );
                    createCertPoller.stopPolling();
                } catch (Exception e) {
                }

            }

            ;
        };
        t.start();
        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertFalse(results.contains(successPollResponse));
                assertTrue(results.size() == 1); //
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
        Assert.assertTrue(createCertPoller.isPollingStopped());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling in between. The client will miss few in progress response and
     * subscriber will get get final successful response.
     **/
    @Test
    public void subscribeToAllPollEvent_StopPollingAfterNSecondsAndRestartedTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds-pollIntervalInMillis)/1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(new Date()+" Test: Thread .. Sleeping ");
                    Thread.sleep(pollIntervalInMillis + (pollIntervalInMillis/2) );
                    System.out.println(new Date()+" Test: Thread wake up and stop polling. ");
                    createCertPoller.stopPolling();
                    Thread.sleep(2000);
                    System.out.println(new Date()+" Test: Thread to enable Polling .. Sleeping ");
                    createCertPoller.enablePolling();
                } catch (Exception e) {
                }

            }

            ;
        };
        t.start();
        System.out.println(new Date()+" Test: Poll and wait for it to complete  ");
        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertTrue(results.contains(successPollResponse));
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertFalse(createCertPoller.isPollingStopped());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling after timeout. The client will miss few in progress response and
     * subscriber will not get final successful response.
     **/
    @Test
    public void subscribeToAllPollEvent_StopPollingAfterNSecondsAndRestartedAfterTimeoutTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds-pollIntervalInMillis)/1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println(new Date()+" Test: Thread .. Sleeping ");
                    long sleepTimeBeforeStopPollMillis=pollIntervalInMillis + (pollIntervalInMillis/2);
                    Thread.sleep(sleepTimeBeforeStopPollMillis );
                    System.out.println(new Date()+" Test: Thread wake up and stop polling. ");
                    createCertPoller.stopPolling();
                    Thread.sleep(totalTimeoutInMilliSeconds  );
                    System.out.println(new Date()+" Test: Thread to enable Polling  ");
                    createCertPoller.enablePolling();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
        t.start();
        System.out.println(new Date()+" Test: Poll and wait for it to complete  ");
        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertFalse(results.contains(successPollResponse));
            })
            .verifyComplete();
        System.out.println(new Date()+" Test: Subscriber for Terminal event");
        Thread.sleep(pollIntervalInMillis);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
        Assert.assertFalse(createCertPoller.isPollingStopped());

    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress responses. But the sdk client will cancel polling in between
     * and subscriber should never get final successful response.
     **/
    @Test
    public void subscribeToAllPollEvent_CancelPollingAfterNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> cancelPollResponse = new PollResponse<>(OperationStatus.USER_CANCELLED, new CreateCertificateResponse("Cancelled Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                cancelPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds-pollIntervalInMillis)/1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(pollIntervalInMillis + (pollIntervalInMillis/2) );
                    createCertPoller.cancelOperation();
                } catch (Exception e) {
                }

            }

            ;
        };
        t.start();
        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertTrue(results.contains(cancelPollResponse));
                assertTrue(results.size() >2 ); //
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
        Assert.assertFalse(createCertPoller.isPollingStopped());
    }

    private void showResults(Collection<PollResponse<CreateCertificateResponse>> al) {
        for (PollResponse<CreateCertificateResponse> pr : al) {
            System.out.println(new Date()+" Test: done response status, data=  " + pr.status().toString() + " , " + pr.getResult().response);

        }

    }

    class CreateCertificateResponse {
        String response;
        HttpResponseException error;
        public CreateCertificateResponse(String respone) {
            this.response = respone;
        }

        public void setResponse(String st) {
            response = st;
        }
    }
}
