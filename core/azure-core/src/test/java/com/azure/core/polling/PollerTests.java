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
                    return intermediateProgressPollResponse;
                } else {
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
                pollIntervalInMillis / 1000 + 1);

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

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 6;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                2);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                }
                createCertPoller.stopPolling();
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
            })
            .verifyComplete();

        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
    }

    private void showResults(Collection<PollResponse<CreateCertificateResponse>> al) {
        for (PollResponse<CreateCertificateResponse> pr : al) {
            System.out.println(" response status, data=  " + pr.status().toString() + " , " + pr.getResult().response);

        }

    }

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
