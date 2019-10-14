// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.PollResponse.OperationStatus;

import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class PollerTests {

    private final ClientLogger logger = new ClientLogger(PollerTests.class);
    private boolean debug = true;
    int count;
    private final String CERTIFICATE_NAME = "CERTIFICATE_NAME";

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

    private Supplier<Mono<CertificateOutput>>
    createFetchResultOperation(String certName) {
        return () -> Mono.defer(() -> Mono.just(new CertificateOutput(certName)));
    }

    private Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> createPollOperation(

        final List<PollResponse<CreateCertificateResponse>> intermediateOtherPollResponseList,
        final PollResponse<CreateCertificateResponse> finalPollResponse,
        long sendFinalResponseInMillis
    ) {
        return new Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>>() {
            // Will return success after this time.
            LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));
            @Override
            public Mono<PollResponse<CreateCertificateResponse>> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
                ++count;
                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
                    int indexForIntermediateResponse = prePollResponse.getValue() == null || prePollResponse.getValue().intermediateResponseIndex >= intermediateOtherPollResponseList.size() ? 0 : prePollResponse.getValue().intermediateResponseIndex;
                    PollResponse<CreateCertificateResponse> intermediatePollResponse = intermediateOtherPollResponseList.get(indexForIntermediateResponse);
                    debug(" Service poll function called ", " returning intermediate response status, otherstatus, value " + intermediatePollResponse.getStatus().toString() + "," + intermediatePollResponse.getValue().response);
                    intermediatePollResponse.getValue().intermediateResponseIndex = indexForIntermediateResponse + 1;
                    return Mono.just(intermediatePollResponse);
                } else {
                    debug(" Service poll function called ", " returning final response " + finalPollResponse.getValue().response);
                    return Mono.just(finalPollResponse);
                }
            }
        };
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER responses and finally successfully completed response.
     * The sdk client will only subscribe for a specific OTHER response and final successful response.
     **/
    @Test
    public void subscribeToSpecificOtherOperationStatusTest() throws Exception {
        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
        PollResponse<CreateCertificateResponse> other1PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_1"), new CreateCertificateResponse("Starting : Cert A"));
        PollResponse<CreateCertificateResponse> other2PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_2"), new CreateCertificateResponse("Starting : Cert A"));

        ArrayList<PollResponse<CreateCertificateResponse>> inProgressPollResponseList = new ArrayList<>();
        inProgressPollResponseList.add(inProgressPollResponse);
        inProgressPollResponseList.add(inProgressPollResponse);
        inProgressPollResponseList.add(other1PollResponse);
        inProgressPollResponseList.add(other2PollResponse);
        long totalTimeoutInMillis = 1000 * 2;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponseList,
                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
        Flux<PollResponse<CreateCertificateResponse>> fluxPollResp =  createCertPoller.getObserver();
        fluxPollResp.subscribe(pr -> {
            debug("0 Got Observer() Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });

        createCertPoller.getObserver().subscribe(x -> {
            debug("1 Got Observer() Response " + x.getStatus().toString() + " " + x.getStatus() + " " + x.getValue().response);
        });

        // get Specific Event Observer
        List<OperationStatus> observeOperationStates = new ArrayList<>();
        observeOperationStates.add(OperationStatus.SUCCESSFULLY_COMPLETED);
        observeOperationStates.add(OperationStatus.fromString("OTHER_1"));
        observeOperationStates.add(OperationStatus.fromString("OTHER_2"));

        Flux<PollResponse<CreateCertificateResponse>> fluxPollRespFiltered = fluxPollResp.filterWhen(tPollResponse -> matchesState(tPollResponse, observeOperationStates));
        fluxPollResp.subscribe(pr -> {
            debug("1 Got Observer() Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });
        fluxPollRespFiltered.subscribe(pr -> {
            debug("2 Got Observer(SUCCESSFULLY_COMPLETED, OTHER_1,2) Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });

        Thread.sleep(totalTimeoutInMillis + 3 * pollInterval.toMillis());
        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER status responses and finally successfully completed response.
     * The sdk client will block for a specific OTHER status.
     **/
    @Test
    public void blockForCustomOperationStatusTest() throws Exception {
        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
        PollResponse<CreateCertificateResponse> other1PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_1"), new CreateCertificateResponse("Starting : Cert A"));
        PollResponse<CreateCertificateResponse> other2PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_2"), new CreateCertificateResponse("Starting : Cert A"));

        ArrayList<PollResponse<CreateCertificateResponse>> inProgressPollResponseList = new ArrayList<>();
        inProgressPollResponseList.add(inProgressPollResponse);
        inProgressPollResponseList.add(inProgressPollResponse);
        inProgressPollResponseList.add(other1PollResponse);
        inProgressPollResponseList.add(other2PollResponse);
        long totalTimeoutInMillis = 1000 * 1;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
            createPollOperation(inProgressPollResponseList,
                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
        PollResponse<CreateCertificateResponse> pollResponse = createCertPoller.blockUntil(PollResponse.OperationStatus.fromString("OTHER_2"));
        Assert.assertEquals(pollResponse.getStatus(), PollResponse.OperationStatus.fromString("OTHER_2"));
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    private Mono<Boolean> matchesState(PollResponse<CreateCertificateResponse> currentPollResponse, List<OperationStatus> observeAllOperationStates) {

        if (observeAllOperationStates.contains(currentPollResponse.getStatus())) {
            return Mono.just(true);
        }
        return Mono.just(false);
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
                successPollResponse, 1500);

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
        createCertPoller.getObserver().subscribe();

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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));

        Thread.sleep(6 * pollInterval.toMillis());
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
        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));

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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));

        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            Thread.sleep(pollInterval.toMillis());
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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
        Thread.sleep(totalTimeoutInMillis);
        debug("Calling poller.block ");
        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));

        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
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

        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
        createCertPoller.getObserver().subscribe(pr -> {
            debug("Got Response " + pr.getStatus().toString() + " " + pr.getValue().response);
        });
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(totalTimeoutInMillis / 2);
                    debug("Cancelling operation");
                    createCertPoller.cancelOperation().block();
                } catch (Exception e) {
                }
            }
        };
        t.start();
        Thread.sleep(totalTimeoutInMillis * 2);

        StepVerifier.create(createCertPoller.result())
            .verifyErrorSatisfies(ex -> assertException(ex, IllegalAccessException.class));
        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    private <T> void assertException(Throwable exception, Class<T> expectedExceptionType) {
        assertEquals(expectedExceptionType, exception.getClass());
    }

    private void debug(String... messages) {
        if (debug) {
            StringBuilder sb =
                new StringBuilder(new Date().toString()).append(" ").append(getClass().getName()).append(" ").append(count).append(" ");
            for (String m : messages) {
                sb.append(m);
            }
            logger.info(sb.toString());
        }
    }

    public class CreateCertificateResponse {
        String response;
        HttpResponseException error;
        int intermediateResponseIndex;

        public CreateCertificateResponse(String respone) {
            this.response = respone;
        }

        public void setResponse(String st) {
            response = st;
        }

        public String toString() {
            return response;
        }
    }

    public class CertificateOutput {
        String name;

        public CertificateOutput(String certName) {
            name = certName;
        }

        public String getName() {
            return name;
        }
    }
}
