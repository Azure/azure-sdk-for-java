// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.polling.PollResponse.OperationStatus;

import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

public class PollerTests {

    private boolean debug = true;

    private Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> createPollOperation(
        PollResponse<CreateCertificateResponse> intermediateProgressPollResponse,
        PollResponse<CreateCertificateResponse> finalPollResponse,
        long sendFinalResponseInMillis
    ) {
        return new Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>>() {
            // Will return success after this time.
            LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));//plusSeconds(sendFinalResponseInSeconds);

            @Override
            public PollResponse<CreateCertificateResponse> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    debug(" Service poll function called ", " returning intermediate response ");
                    return intermediateProgressPollResponse;
                } else {
                    debug("   Service poll function called ", " returning final response ");
                    return finalPollResponse;
                }
            }
        };
    }

    /*
     * Test where SDK Client is subscribed all responses.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in-progress response.
     **/
    @Test
    public void autoStartPollingAndSuccessfullyComplete() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis/20);

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis/2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);

        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            new Thread().sleep(pollInterval.toMillis());
        }
        
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in-progress response.
     **/
   @Test
    public void subscribeToAllPollEventSuccessfullyCompleteInNSecondsTest() throws Exception {

       PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
       PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

       long totalTimeoutInMillis = 1000 * 1;
       Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis/20);

       Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
           createPollOperation(inProgressPollResponse,
               successPollResponse, totalTimeoutInMillis/2);

       PollerOptions pollerOptions = new PollerOptions(pollInterval);

       Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
       createCertPoller.setAutoPollingEnabled(false);
        StepVerifier.create(createCertPoller.getObserver())
            .recordWith(ArrayList::new)
            .thenConsumeWhile(PollResponse -> PollResponse.getStatus() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertTrue(results.contains(successPollResponse));
                assertTrue(results.size() <= totalTimeoutInMillis / pollInterval.toMillis() + 1);
            })
            .verifyComplete();

        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());
        Assert.assertTrue(createCertPoller.block() == successPollResponse);
    }

    /* Test where SDK Client is subscribed to only final/last response.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in progress response.
     * But the subscriber is only interested in last response, The test will ensure subscriber
     * only gets last PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED . */

    @Test
    public void subscribeToOnlyFinalEventSuccessfullyCompleteInNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis/20);

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis/2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        assertTrue(createCertPoller.block() == successPollResponse);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and subscriber should never get final successful response.
     **/
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 2;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis/20);

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis/2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3*pollInterval.toMillis() );
                    createCertPoller.setAutoPollingEnabled(false);
                } catch (Exception e) {
                }
            }
        };
        t.start();
       /*StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.getStatus() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertFalse(results.contains(successPollResponse));
                assertTrue(results.size() == 1); //
            })
            .verifyComplete();
        */
        Thread.sleep(totalTimeoutInMillis);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling in between. The client will miss few in progress response and
     * subscriber will get get final successful response.
     **/
    /*@Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds - pollIntervalInMillis) / 1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    debug("Thread .. Sleeping ");
                    Thread.sleep(pollIntervalInMillis + (pollIntervalInMillis / 2));
                    debug("Thread wake up and stop polling. ");
                    createCertPoller.stopPolling();
                    Thread.sleep(2000);
                    debug("Thread to enable Polling .. Sleeping ");
                    createCertPoller.enablePolling();
                } catch (Exception e) {
                }
            }
        };
        t.start();
        debug("Poll and wait for it to complete  ");
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
    }*/

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. The sdk client will stop auto polling. It
     * will subscribe and start receiving responses .The subscriber will get final successful response.
     **/
    @Test
    public void subscribeToStopAutoPollAndSubscribeItselfTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis/20);

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis/2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        createCertPoller.setAutoPollingEnabled(false);
        debug("Going to create subscriber ");
        createCertPoller.getObserver().subscribe(createCertificateResponsePollResponse -> {
            debug(" got Response " + createCertificateResponsePollResponse.getStatus().toString());
        });

        new Thread().sleep(totalTimeoutInMillis);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());

    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress responses. But the sdk client will cancel polling in between
     * and subscriber should never get final successful response.
     **/
    /*@Test
    public void subscribeToAllPollEventCancelPollingAfterNSecondsTest() throws Exception {

        PollResponse<CreateCertificateResponse> cancelPollResponse = new PollResponse<>(OperationStatus.USER_CANCELLED, new CreateCertificateResponse("Cancelled Created : Cert A "));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        int totalTimeoutInMilliSeconds = 1000 * 8;
        int pollIntervalInMillis = 1000 * 1;
        float poolIntervalGrowthFactor = 1.0f;

        Function<PollResponse<CreateCertificateResponse>, PollResponse<CreateCertificateResponse>> pollOperation =
            createPollOperation(inProgressPollResponse,
                cancelPollResponse,
                pollIntervalInMillis,
                (totalTimeoutInMilliSeconds - pollIntervalInMillis) / 1000);

        PollerOptions pollerOptions = new PollerOptions(totalTimeoutInMilliSeconds, pollIntervalInMillis, poolIntervalGrowthFactor);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(pollIntervalInMillis + (pollIntervalInMillis / 2));
                    createCertPoller.cancelOperation();
                } catch (Exception e) {
                }
            }
        };
        t.start();
        StepVerifier.create(createCertPoller.poll())
            .recordWith(ArrayList<PollResponse<CreateCertificateResponse>>::new)
            .thenConsumeWhile(PollResponse -> PollResponse.status() == OperationStatus.IN_PROGRESS)
            .consumeRecordedWith(results -> {
                assertTrue(results.contains(inProgressPollResponse));
                assertTrue(results.contains(cancelPollResponse));
                assertTrue(results.size() > 2); //
            })
            .verifyComplete();
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
        Assert.assertFalse(createCertPoller.isPollingStopped());
    }*/

    private void showResults(Collection<PollResponse<CreateCertificateResponse>> al) {
        for (PollResponse<CreateCertificateResponse> pr : al) {
            debug("done response status, data=  ", pr.getStatus().toString(), " , " + pr.getStatus().toString());
        }
    }

    private void debug(String... messages) {
        if (debug) {
            StringBuffer sb = new StringBuffer(new Date().toString()).append(" ").append(getClass().getName()).append(" ");
            for (String m : messages) {
                sb.append(m);
            }
            System.out.println(sb.toString());
        }
    }

    class CreateCertificateResponse {
        String response;
        HttpResponseException error;

        CreateCertificateResponse(String respone) {
            this.response = respone;
        }

        public void setResponse(String st) {
            response = st;
        }
    }
}
