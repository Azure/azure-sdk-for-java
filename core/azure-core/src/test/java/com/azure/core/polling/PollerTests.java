// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.polling;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.polling.PollResponse.OperationStatus;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

public class PollerTests {

    private boolean debug = true;
    int count;

    private Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> createPollOperation(
        PollResponse<CreateCertificateResponse> intermediateProgressPollResponse,
        PollResponse<CreateCertificateResponse> finalPollResponse,
        long sendFinalResponseInMillis
    ) {
        return new Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>>() {

            // Will return success after this time.
            LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));

            @Override
            public Mono<PollResponse<CreateCertificateResponse>> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
                ++count;
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    debug(" Service poll function called ", " returning intermediate response " + intermediateProgressPollResponse.getValue().response);
                    return Mono.just(intermediateProgressPollResponse);
                } else {
                    debug(" Service poll function called ", " returning final response " + finalPollResponse.getValue().response);
                    return Mono.just(finalPollResponse);
                }
            }
        };
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling in between. The client will miss few in progress response and
     * subscriber will get get final successful response.
     **/
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 2;
        Duration pollInterval = Duration.ofMillis(100);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
            successPollResponse, 800);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        createCertPoller.getObserver().subscribe(pr -> {
            debug("Got Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    debug("Thread .. Sleeping ");
                    Thread.sleep(pollInterval.toMillis() + (pollInterval.toMillis() / 2));
                    debug("Thread wake up and stop polling. ");
                    createCertPoller.setAutoPollingEnabled(false);
                    Thread.sleep(1000);
                    debug("Thread to enable Polling .. Sleeping ");
                    createCertPoller.setAutoPollingEnabled(true);
                } catch (Exception e) {
                }
            }
        };
        t.start();

        debug("Poll and wait for it to complete  ");
        Thread.sleep(totalTimeoutInMillis);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /*
     * The test is setup where user will disable auto polling after creating poller.
     * The user will enable polling after LRO is expected to complete.
     * We want to ensure that if user enable polling after LRO is complete, user can
     * final polling status.
     */
    @Test
    public void disableAutoPollAndEnableAfterCompletionSuccessfullyDone() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
        
        int totalTileInSeconds = 5;
        long totalTimeoutInMillis = 1000 * totalTileInSeconds;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, 1800);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);

        new Thread().sleep(6 * pollInterval.toMillis());
        debug("Try to disable autopolling..");
        createCertPoller.setAutoPollingEnabled(false);
        
        Thread.sleep(totalTimeoutInMillis);
        debug("Try to enable autopolling..");
        createCertPoller.setAutoPollingEnabled(true);
        Thread.sleep(5 * pollInterval.toMillis());
        debug(createCertPoller.getStatus().toString());
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
        Thread.sleep(5 * pollInterval.toMillis());
        Assert.assertTrue(createCertPoller.block().getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);

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
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis / 2);

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
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, pollInterval.toMillis() * 2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread.sleep(totalTimeoutInMillis);
        debug("Calling poller.block ");
        Assert.assertTrue(createCertPoller.block().getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
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

        long totalTimeoutInMillis = 1000 * 10;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis / 2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);
       
        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        
        assertTrue(createCertPoller.block().getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
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

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(100);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(totalTimeoutInMillis / 2);
                    createCertPoller.setAutoPollingEnabled(false);
                } catch (Exception e) {
                }
            }
        };
        t.start();
        Thread.sleep(totalTimeoutInMillis);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());
    }


    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. The sdk client will stop auto polling. It
     * will subscribe and start receiving responses .The subscriber will get final successful response.
     **/
    @Test
    public void stopAutoPollAndManualPoll() throws Exception {

        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                successPollResponse, totalTimeoutInMillis / 2);

        PollerOptions pollerOptions = new PollerOptions(pollInterval);

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(pollerOptions, pollOperation);
        createCertPoller.setAutoPollingEnabled(false);
        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            PollResponse<CreateCertificateResponse> pollResponse = createCertPoller.poll().block();
            Thread.sleep(pollInterval.toMillis());
        }

        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());

    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate user cancelled response returned
     * after few in-progress response. The sdk client will wait for it to cancel get final USER_CANCELLED response.
     **/
    @Test
    public void subscribeToAllPollEventCancelOperatopnTest() throws Exception {

        PollResponse<CreateCertificateResponse> cancelPollResponse = new PollResponse<>(OperationStatus.USER_CANCELLED, new CreateCertificateResponse("Cancelled : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));

        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponse,
                cancelPollResponse, totalTimeoutInMillis - pollInterval.toMillis());

        Poller<CreateCertificateResponse> createCertPoller = new Poller<>(new PollerOptions(pollInterval), pollOperation);
        createCertPoller.getObserver().subscribe(pr -> {
            debug("Got Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(totalTimeoutInMillis / 2);
                    debug("Cancelling operation");
                    createCertPoller.cancelOperation();
                } catch (Exception e) {
                }
            }
        };
        t.start();

        Assert.assertTrue(createCertPoller.block().getStatus() == OperationStatus.USER_CANCELLED);
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    private void showResults(Collection<PollResponse<CreateCertificateResponse>> al) {
        for (PollResponse<CreateCertificateResponse> pr : al) {
            debug("done response status, data=  ", pr.getValue().response, " , " + pr.getStatus().toString());
        }
    }

    private void debug(String... messages) {
        if (debug) {
            StringBuffer sb = new StringBuffer(new Date().toString()).append(" ").append(getClass().getName()).append(" ").append(count).append(" ");
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
        public String toString() {
            return response;
        }
    }
}
