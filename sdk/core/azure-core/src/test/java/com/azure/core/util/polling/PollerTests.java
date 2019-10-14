// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.polling.PollResponse.OperationStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PollerTests {
    @Mock
    private Function<PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation;

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void afterTest() {
        Mockito.framework().clearInlineMocks();
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER responses and finally successfully completed response.
     * The sdk client will only subscribe for a specific OTHER response and final successful response.
     **/
    @Test
    public void subscribeToSpecificOtherOperationStatusTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(100);
        final Duration pollInterval = Duration.ofMillis(250);
        final List<PollResponse<Response>> responses = new ArrayList<>();
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("0"), retryAfter));
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("1"), retryAfter));
        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_1"), new Response("2")));
        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_2"), new Response("3")));
        responses.add(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("4"), retryAfter));

        when(pollOperation.apply(any())).thenReturn(
            Mono.just(responses.get(0)),
            Mono.just(responses.get(1)),
            Mono.just(responses.get(2)),
            Mono.just(responses.get(3)),
            Mono.just(responses.get(4)));

        // Act
        final Poller<Response> pollerObserver = new Poller<>(pollInterval, pollOperation);

        // Assert
        StepVerifier.create(pollerObserver.getObserver())
            .expectNext(responses.get(0))
            .expectNext(responses.get(1))
            .expectNext(responses.get(2))
            .expectNext(responses.get(3))
            .expectNext(responses.get(4))
            .verifyComplete();

        Assert.assertEquals(pollerObserver.getStatus(), OperationStatus.SUCCESSFULLY_COMPLETED);
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER status responses and finally successfully completed response.
     * The sdk client will block for a specific OTHER status.
     **/
    @Test
    public void blockForCustomOperationStatusTest() {
        final OperationStatus expected = OperationStatus.fromString("OTHER_2");
        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"));
        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
        PollResponse<Response> other1PollResponse = new PollResponse<>(OperationStatus.fromString("OTHER_1"), new Response("Starting : Cert A"));
        PollResponse<Response> other2PollResponse = new PollResponse<>(expected, new Response("Starting : Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse),
            Mono.just(inProgressPollResponse), Mono.just(other1PollResponse), Mono.just(other2PollResponse),
            Mono.just(successPollResponse));

        // Act
        final Poller<Response> createCertPoller = new Poller<>(Duration.ofMillis(100), pollOperation);
        final PollResponse<Response> pollResponse = createCertPoller.blockUntil(expected);

        // Assert
        Assert.assertEquals(pollResponse.getStatus(), expected);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling in between. The client will miss few in progress response and
     * subscriber will get get final successful response.
     **/
    @Ignore("When auto-subscription is turned off, the observer still polls. https://github.com/Azure/azure-sdk-for-java/issues/5805")
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() {
        // Arrange
        final PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"), Duration.ofSeconds(1));
        final PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
        final Duration pollInterval = Duration.ofMillis(100);

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));

        // Act
        final Poller<Response> poller = new Poller<>(pollInterval, pollOperation);

        // Assert
        StepVerifier.create(poller.getObserver())
            .expectNext(inProgressPollResponse)
            .then(() -> poller.setAutoPollingEnabled(false))
            .expectNoEvent(Duration.ofSeconds(3))
            .then(() -> poller.setAutoPollingEnabled(true))
            .expectNext(successPollResponse)
            .verifyComplete();

        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
        Assert.assertTrue(poller.isAutoPollingEnabled());
    }

    /*
     * The test is setup where user will disable auto polling after creating poller.
     * The user will enable polling after LRO is expected to complete.
     * We want to ensure that if user enable polling after LRO is complete, user can
     * final polling status.
     */
    @Test
    public void disableAutoPollAndEnableAfterCompletionSuccessfullyDone() {
        // Arrange
        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting: Cert A"));
        PollResponse<Response> initial = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("First: Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(initial), Mono.just(inProgress), Mono.just(success));

        Poller<Response> poller = new Poller<>(Duration.ofSeconds(1), pollOperation);

        // Act & Assert
        poller.setAutoPollingEnabled(false);

        StepVerifier.create(poller.getObserver())
            .then(() -> poller.setAutoPollingEnabled(true))
            .expectNext(inProgress)
            .expectNext(success)
            .verifyComplete();
        Assert.assertSame(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
        Assert.assertTrue(poller.isAutoPollingEnabled());
    }

    /*
     * Test where SDK Client is subscribed all responses.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in-progress response.
     **/
    @Test
    public void autoStartPollingAndSuccessfullyComplete() throws Exception {
        // Arrange
        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));

        Duration pollInterval = Duration.ofSeconds(1);

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));

        Poller<Response> createCertPoller = new Poller<>(pollInterval, pollOperation);

        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            Thread.sleep(pollInterval.toMillis());
        }

        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.getStatus());
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /* Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and subscriber should never get final successful response.
     **/
    @Ignore("https://github.com/Azure/azure-sdk-for-java/issues/5809")
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsTest() throws Exception {
        // Assert
        Duration pollInterval = Duration.ofSeconds(1);
        Duration waitTime = Duration.ofSeconds(3);
        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgress = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS,new Response("Starting : Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgress), Mono.just(inProgress), Mono.just(success));

        // Act
        Poller<Response> poller = new Poller<>(pollInterval, pollOperation, null,
            ignored -> new PollResponse<Response>(OperationStatus.USER_CANCELLED, null));

        // Assert
        StepVerifier.create(poller.getObserver())
            .expectNext(inProgress)
            .then(() -> poller.setAutoPollingEnabled(false))
            .expectNoEvent(waitTime)
            .then(() -> poller.cancelOperation())
            .verifyError();

        Assert.assertEquals(OperationStatus.IN_PROGRESS, poller.getStatus());
        Assert.assertFalse(poller.isAutoPollingEnabled());
    }

//
//    /* Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate successful response returned
//     * after few in-progress response. The sdk client will stop auto polling. It
//     * will subscribe and start receiving responses .The subscriber will get final successful response.
//     **/
//    @Test
//    public void stopAutoPollAndManualPoll() throws Exception {
//
//        PollResponse<Response> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"));
//        PollResponse<Response> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);
//
//        Function<PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                successPollResponse, totalTimeoutInMillis / 2);
//
//        Poller<Response> createCertPoller = new Poller<>(pollInterval, pollOperation);
//        createCertPoller.setAutoPollingEnabled(false);
//        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
//            PollResponse<Response> pollResponse = createCertPoller.poll().block();
//            Thread.sleep(pollInterval.toMillis());
//        }
//
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());
//
//    }
//
//    /* Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate user cancelled response returned
//     * after few in-progress response. The sdk client will wait for it to cancel get final USER_CANCELLED response.
//     **/
//    @Test
//    public void subscribeToAllPollEventCancelOperatopnTest() throws Exception {
//
//        PollResponse<Response> cancelPollResponse = new PollResponse<>(OperationStatus.USER_CANCELLED, new Response("Cancelled : Cert A"));
//        PollResponse<Response> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);
//
//        Function<PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                cancelPollResponse, totalTimeoutInMillis - pollInterval.toMillis());
//
//        Poller<Response> createCertPoller = new Poller<>(pollInterval, pollOperation);
//        createCertPoller.getObserver().subscribe(pr -> {
//            debug("Got Response " + pr.getStatus().toString() + " " + pr.getValue().response);
//        });
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(totalTimeoutInMillis / 2);
//                    debug("Cancelling operation");
//                    createCertPoller.cancelOperation();
//                } catch (Exception e) {
//                }
//            }
//        };
//        t.start();
//
//        Assert.assertTrue(createCertPoller.block().getStatus() == OperationStatus.USER_CANCELLED);
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }

    public static class Response {
        private final String response;

        public Response(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }
    }
}
