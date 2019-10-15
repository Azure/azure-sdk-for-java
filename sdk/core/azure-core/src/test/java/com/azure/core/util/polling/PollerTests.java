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
import java.util.function.Supplier;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class PollerTests {
    private static final String OUTPUT_NAME = "Bar";
    @Mock
    private Function<PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation;

    @Mock
    private Function<Poller<Response, CertificateOutput>, Mono<Response>> cancelOperation;

    @Before
    public void beforeTest() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void afterTest() {
        Mockito.framework().clearInlineMocks();
    }

    /**
     * Verify we cannot pass in poll duration of {@link Duration#ZERO}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorPollIntervalZero() {
        new Poller<Response, CertificateOutput>(Duration.ZERO, pollOperation,
            createFetchResultOperation("Bar"), () -> Mono.just(new Response("Foo")), cancelOperation);
    }

    /**
     * Verify we cannot pass in poll negative duration.
     */
    @Test(expected = IllegalArgumentException.class)
    public void constructorPollIntervalNegative() {
        new Poller<>(Duration.ofSeconds(-1), pollOperation, createFetchResultOperation("Bar"),
            () -> Mono.just(new Response("Foo")), cancelOperation);
    }

    /**
     * Verify we cannot pass in null pollInterval.
     */
    @Test(expected = NullPointerException.class)
    public void constructorPollIntervalNull() {
        new Poller<>(null, pollOperation, createFetchResultOperation("Bar"),
            () -> Mono.just(new Response("Foo")), cancelOperation);
    }

    /**
     * Verify we cannot pass in null pollInterval.
     */
    @Test(expected = NullPointerException.class)
    public void constructorPollOperationNull() {
        new Poller<>(Duration.ofSeconds(1), null, createFetchResultOperation("Bar"),
            () -> Mono.just(new Response("Foo")), cancelOperation);
    }

    /**
     * Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER responses and finally successfully completed response.
     * The sdk client will only subscribe for a specific OTHER response and final successful response.
     */
    @Test
    public void subscribeToSpecificOtherOperationStatusTest() {
        // Arrange
        final Duration retryAfter = Duration.ofMillis(100);
        final Duration pollInterval = Duration.ofMillis(250);
        final List<PollResponse<Response>> responses = new ArrayList<>();
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("0"), retryAfter));
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("1"), retryAfter));
        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_1", false), new Response("2")));
        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_2", false), new Response("3")));
        responses.add(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("4"), retryAfter));

        when(pollOperation.apply(any())).thenReturn(
            Mono.just(responses.get(0)),
            Mono.just(responses.get(1)),
            Mono.just(responses.get(2)),
            Mono.just(responses.get(3)),
            Mono.just(responses.get(4)));

        // Act
        final Poller<Response, CertificateOutput> pollerObserver = new Poller<Response, CertificateOutput>(pollInterval,
            pollOperation, createFetchResultOperation("Bar"));

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

    /**
     * Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate few in-progress response followed by few OTHER status responses and finally successfully completed response.
     * The sdk client will block for a specific OTHER status.
     */
    @Test
    public void blockForCustomOperationStatusTest() {
        final OperationStatus expected = OperationStatus.fromString("OTHER_2", false);
        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"));
        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
        PollResponse<Response> other1PollResponse = new PollResponse<>(OperationStatus.fromString("OTHER_1", false), new Response("Starting : Cert A"));
        PollResponse<Response> other2PollResponse = new PollResponse<>(expected, new Response("Starting : Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse),
            Mono.just(inProgressPollResponse), Mono.just(other1PollResponse), Mono.just(other2PollResponse),
            Mono.just(successPollResponse));

        // Act
        final Poller<Response, CertificateOutput> createCertPoller = new Poller<>(Duration.ofMillis(100), pollOperation,
            createFetchResultOperation("Bar"));
        final PollResponse<Response> pollResponse = createCertPoller.blockUntil(expected);

        // Assert
        Assert.assertEquals(pollResponse.getStatus(), expected);
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /**
     * Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and activate polling in between. The client will miss few in progress response and
     * subscriber will get get final successful response.
     */
    @Ignore("When auto-subscription is turned off, the observer still polls. https://github.com/Azure/azure-sdk-for-java/issues/5805")
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() {
        // Arrange
        final PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"), Duration.ofSeconds(1));
        final PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
        final Duration pollInterval = Duration.ofMillis(100);

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));

        // Act
        final Poller<Response, CertificateOutput> poller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation("Bar"));

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

        Poller<Response, CertificateOutput> poller = new Poller<>(Duration.ofSeconds(1), pollOperation, createFetchResultOperation("Bar"));

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
     * The last response in this case will be OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in-progress response.
     **/
    @Test
    public void autoStartPollingAndSuccessfullyComplete() throws Exception {
        // Arrange
        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));

        Duration pollInterval = Duration.ofSeconds(1);

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));

        Poller<Response, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation("Bar"));

        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            Thread.sleep(pollInterval.toMillis());
        }

        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.getStatus());
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /** Test where SDK Client is subscribed to only final/last response.
     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
     * This scenario is setup where source will generate successful response returned after few in progress response.
     * But the subscriber is only interested in last response, The test will ensure subscriber
     * only gets last PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED.
     */
    @Test
    public void subscribeToOnlyFinalEventSuccessfullyCompleteInNSecondsTest() {
        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));

        Duration pollInterval = Duration.ofMillis(500);

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgress), Mono.just(success));

        Poller<Response, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(OUTPUT_NAME));

        Assert.assertEquals(OUTPUT_NAME, createCertPoller.block().getName());
        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.getStatus());
        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
    }

    /**
     * Test where SDK Client is subscribed all responses.
     * This scenario is setup where source will generate successful response returned
     * after few in-progress response. But the sdk client will stop polling in between
     * and subscriber should never get final successful response.
     */
    @Ignore("https://github.com/Azure/azure-sdk-for-java/issues/5809")
    @Test
    public void subscribeToAllPollEventStopPollingAfterNSecondsTest() {
        // Assert
        Duration pollInterval = Duration.ofSeconds(1);
        Duration waitTime = Duration.ofSeconds(3);
        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgress), Mono.just(inProgress), Mono.just(success));

        // Act
        Poller<Response, CertificateOutput> poller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(OUTPUT_NAME), null,
            ignored -> Mono.just(new Response("Foo")));

        // Assert
        StepVerifier.create(poller.getObserver())
            .expectNext(inProgress)
            .then(() -> poller.setAutoPollingEnabled(false))
            .expectNoEvent(waitTime)
            .thenCancel() // Cancel our subscription. This does not affect upstream poller.
            .verify();

        Assert.assertEquals(OperationStatus.IN_PROGRESS, poller.getStatus());
        Assert.assertFalse(poller.isAutoPollingEnabled());
    }

    /**
     * Test where SDK Client is subscribed all responses. This scenario is setup where source will generate successful
     * response returned after few in-progress response. The sdk client will stop auto polling. It will subscribe and
     * start receiving responses .The subscriber will get final successful response.
     */
    @Test
    public void stopAutoPollAndManualPoll() {
        // Arrange
        final List<PollResponse<Response>> responses = new ArrayList<>();
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A")));
        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Middle: Cert A")));
        responses.add(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A")));

        long totalTimeoutInMillis = 1000;
        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);

        when(pollOperation.apply(any())).thenReturn(Mono.just(responses.get(0)), Mono.just(responses.get(1)), Mono.just(responses.get(2)));

        Poller<Response, CertificateOutput> poller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(OUTPUT_NAME));
        poller.setAutoPollingEnabled(false);

        // Act & Assert
        int counter = 0;
        while (poller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
            counter++;

            PollResponse<Response> pollResponse = poller.poll().block();
            Assert.assertSame("Counter: " + counter + " did not match.", responses.get(counter), pollResponse);
        }

        Assert.assertSame(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
        Assert.assertFalse(poller.isAutoPollingEnabled());
    }

    /**
     * Test where SDK Client is subscribed all responses. This scenario is setup where source will generate user
     * cancelled response returned after few in-progress response. The sdk client will wait for it to cancel get final
     * USER_CANCELLED response.
     */
    @Test
    public void subscribeToAllPollEventCancelOperationTest() {
        Duration pollInterval = Duration.ofMillis(500);
        PollResponse<Response> cancellation = new PollResponse<>(OperationStatus.USER_CANCELLED, new Response("Created : Cert A"));
        PollResponse<Response> first = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting: Cert A"));

        when(pollOperation.apply(any())).thenReturn(Mono.just(first), Mono.just(cancellation));

        // Act
        Poller<Response, CertificateOutput> poller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(OUTPUT_NAME), null, cancelOperation);

        // Assert
        StepVerifier.create(poller.getObserver())
            .expectNext(first)
            .then(() -> poller.cancelOperation())
            .expectNext(cancellation)
            .thenCancel() // cancel this actual subscriber, this does not affect the parent operation.
            .verify();

        StepVerifier.create(poller.result())
            .verifyErrorSatisfies(ex -> assertException(ex, IllegalAccessException.class));
        Assert.assertEquals(OperationStatus.USER_CANCELLED, poller.getStatus());
        Assert.assertTrue(poller.isAutoPollingEnabled());

        verify(cancelOperation, Mockito.times(1)).apply(poller);
    }


    private <T> void assertException(Throwable exception, Class<T> expectedExceptionType) {
        assertEquals(expectedExceptionType, exception.getClass());
    }

    private Supplier<Mono<CertificateOutput>> createFetchResultOperation(String certName) {
        return () -> Mono.defer(() -> Mono.just(new CertificateOutput(certName)));
    }

    public static class Response {
        private final String response;

        public Response(String response) {
            this.response = response;
        }

        public String getResponse() {
            return response;
        }

        @Override
        public String toString() {
            return "Response: " + response;
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



//// Copyright (c) Microsoft Corporation. All rights reserved.
//// Licensed under the MIT License.
//
//package com.azure.core.util.polling;
//
//import com.azure.core.http.rest.Response;
//import com.azure.core.util.polling.PollResponse.OperationStatus;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Ignore;
//import org.junit.Test;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.MockitoAnnotations;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.function.Function;
//import java.util.function.Supplier;
//
//import static org.junit.Assert.*;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//@SuppressWarnings("unchecked")
//public class PollerTests {
//    @Mock
//    private Function<PollResponse<Response>, Mono<PollResponse<Response>>> pollOperation;
//
//    @Mock
//    private Consumer<Poller<Response>> cancelOperation;
//
//    @Before
//    public void beforeTest() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @After
//    public void afterTest() {
//        Mockito.framework().clearInlineMocks();
//    }
//
//    /**
//     * Verify we cannot pass in poll duration of {@link Duration#ZERO}.
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void constructorPollIntervalZero() {
//        new Poller<>(Duration.ZERO, pollOperation, () -> Mono.just(new Response("Foo")), cancelOperation);
//    }
//
//    private final ClientLogger logger = new ClientLogger(PollerTests.class);
//    private boolean debug = true;
//    int count;
//    private final String CERTIFICATE_NAME = "CERTIFICATE_NAME";
//
//    private Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> createPollOperation(
//        PollResponse<CreateCertificateResponse> intermediateProgressPollResponse,
//        PollResponse<CreateCertificateResponse> finalPollResponse,
//        long sendFinalResponseInMillis
//    ) {
//        return new Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>>() {
//
//            // Will return success after this time.
//            LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));
//
//            @Override
//            public Mono<PollResponse<CreateCertificateResponse>> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
//                ++count;
//                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
//                    debug(" Service poll function called ", " returning intermediate response " + intermediateProgressPollResponse.getValue().response);
//                    return Mono.just(intermediateProgressPollResponse);
//                } else {
//                    debug(" Service poll function called ", " returning final response " + finalPollResponse.getValue().response);
//                    return Mono.just(finalPollResponse);
//                }
//            }
//        };
//    }
//
//    private Supplier<Mono<CertificateOutput>>
//    createFetchResultOperation(String certName) {
//        return () -> Mono.defer(() -> Mono.just(new CertificateOutput(certName)));
//    }
//
//    private Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> createPollOperation(
//
//        final List<PollResponse<CreateCertificateResponse>> intermediateOtherPollResponseList,
//        final PollResponse<CreateCertificateResponse> finalPollResponse,
//        long sendFinalResponseInMillis
//    ) {
//        return new Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>>() {
//            // Will return success after this time.
//            LocalDateTime timeToReturnFinalResponse = LocalDateTime.now().plus(Duration.ofMillis(sendFinalResponseInMillis));
//            @Override
//            public Mono<PollResponse<CreateCertificateResponse>> apply(PollResponse<CreateCertificateResponse> prePollResponse) {
//                ++count;
//                if (LocalDateTime.now().isBefore(timeToReturnFinalResponse)) {
//                    int indexForIntermediateResponse = prePollResponse.getValue() == null || prePollResponse.getValue().intermediateResponseIndex >= intermediateOtherPollResponseList.size() ? 0 : prePollResponse.getValue().intermediateResponseIndex;
//                    PollResponse<CreateCertificateResponse> intermediatePollResponse = intermediateOtherPollResponseList.get(indexForIntermediateResponse);
//                    debug(" Service poll function called ", " returning intermediate response status, otherstatus, value " + intermediatePollResponse.getStatus().toString() + "," + intermediatePollResponse.getValue().response);
//                    intermediatePollResponse.getValue().intermediateResponseIndex = indexForIntermediateResponse + 1;
//                    return Mono.just(intermediatePollResponse);
//                } else {
//                    debug(" Service poll function called ", " returning final response " + finalPollResponse.getValue().response);
//                    return Mono.just(finalPollResponse);
//                }
//            }
//        };
//=======
//    /**
//     * Verify we cannot pass in poll negative duration.
//     */
//    @Test(expected = IllegalArgumentException.class)
//    public void constructorPollIntervalNegative() {
//        new Poller<>(Duration.ofSeconds(-1), pollOperation, () -> Mono.just(new Response("Foo")), cancelOperation);
//    }
//
//    /**
//     * Verify we cannot pass in null pollInterval.
//     */
//    @Test(expected = NullPointerException.class)
//    public void constructorPollIntervalNull() {
//        new Poller<>(null, pollOperation, () -> Mono.just(new Response("Foo")), cancelOperation);
//    }
//
//    /**
//     * Verify we cannot pass in null pollInterval.
//     */
//    @Test(expected = NullPointerException.class)
//    public void constructorPollOperationNull() {
//        new Poller<>(Duration.ofSeconds(1), null, () -> Mono.just(new Response("Foo")), cancelOperation);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate few in-progress response followed by few OTHER responses and finally successfully completed response.
//     * The sdk client will only subscribe for a specific OTHER response and final successful response.
//     */
//    @Test
//<<<<<<< HEAD
//    public void subscribeToSpecificOtherOperationStatusTest() throws Exception {
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//        PollResponse<CreateCertificateResponse> other1PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_1"), new CreateCertificateResponse("Starting : Cert A"));
//        PollResponse<CreateCertificateResponse> other2PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_2"), new CreateCertificateResponse("Starting : Cert A"));
//
//        ArrayList<PollResponse<CreateCertificateResponse>> inProgressPollResponseList = new ArrayList<>();
//        inProgressPollResponseList.add(inProgressPollResponse);
//        inProgressPollResponseList.add(inProgressPollResponse);
//        inProgressPollResponseList.add(other1PollResponse);
//        inProgressPollResponseList.add(other2PollResponse);
//        long totalTimeoutInMillis = 1000 * 2;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponseList,
//                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        Flux<PollResponse<CreateCertificateResponse>> fluxPollResp =  createCertPoller.getObserver();
//        fluxPollResp.subscribe(pr -> {
//            debug("0 Got Observer() Response " + pr.getStatus().toString() + " " + pr.getValue().response);
//        });
//
//        createCertPoller.getObserver().subscribe(x -> {
//            debug("1 Got Observer() Response " + x.getStatus().toString() + " " + x.getStatus() + " " + x.getValue().response);
//        });
//
//        // get Specific Event Observer
//        List<OperationStatus> observeOperationStates = new ArrayList<>();
//        observeOperationStates.add(OperationStatus.SUCCESSFULLY_COMPLETED);
//        observeOperationStates.add(OperationStatus.fromString("OTHER_1"));
//        observeOperationStates.add(OperationStatus.fromString("OTHER_2"));
//
//        Flux<PollResponse<CreateCertificateResponse>> fluxPollRespFiltered = fluxPollResp.filterWhen(tPollResponse -> matchesState(tPollResponse, observeOperationStates));
//        fluxPollResp.subscribe(pr -> {
//            debug("1 Got Observer() Response " + pr.getStatus().toString() + " " + pr.getValue().response);
//        });
//        fluxPollRespFiltered.subscribe(pr -> {
//            debug("2 Got Observer(SUCCESSFULLY_COMPLETED, OTHER_1,2) Response " + pr.getStatus().toString() + " " + pr.getValue().response);
//        });
//
//        Thread.sleep(totalTimeoutInMillis + 3 * pollInterval.toMillis());
//        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//=======
//    public void subscribeToSpecificOtherOperationStatusTest() {
//        // Arrange
//        final Duration retryAfter = Duration.ofMillis(100);
//        final Duration pollInterval = Duration.ofMillis(250);
//        final List<PollResponse<Response>> responses = new ArrayList<>();
//        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("0"), retryAfter));
//        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("1"), retryAfter));
//        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_1"), new Response("2")));
//        responses.add(new PollResponse<>(OperationStatus.fromString("OTHER_2"), new Response("3")));
//        responses.add(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("4"), retryAfter));
//
//        when(pollOperation.apply(any())).thenReturn(
//            Mono.just(responses.get(0)),
//            Mono.just(responses.get(1)),
//            Mono.just(responses.get(2)),
//            Mono.just(responses.get(3)),
//            Mono.just(responses.get(4)));
//
//        // Act
//        final Poller<Response> pollerObserver = new Poller<>(pollInterval, pollOperation);
//
//        // Assert
//        StepVerifier.create(pollerObserver.getObserver())
//            .expectNext(responses.get(0))
//            .expectNext(responses.get(1))
//            .expectNext(responses.get(2))
//            .expectNext(responses.get(3))
//            .expectNext(responses.get(4))
//            .verifyComplete();
//
//        Assert.assertEquals(pollerObserver.getStatus(), OperationStatus.SUCCESSFULLY_COMPLETED);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate few in-progress response followed by few OTHER status responses and finally successfully completed response.
//     * The sdk client will block for a specific OTHER status.
//     */
//    @Test
//<<<<<<< HEAD
//    public void blockForCustomOperationStatusTest() throws Exception {
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//        PollResponse<CreateCertificateResponse> other1PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_1"), new CreateCertificateResponse("Starting : Cert A"));
//        PollResponse<CreateCertificateResponse> other2PollResponse = new PollResponse<>(PollResponse.OperationStatus.fromString("OTHER_2"), new CreateCertificateResponse("Starting : Cert A"));
//
//        ArrayList<PollResponse<CreateCertificateResponse>> inProgressPollResponseList = new ArrayList<>();
//        inProgressPollResponseList.add(inProgressPollResponse);
//        inProgressPollResponseList.add(inProgressPollResponse);
//        inProgressPollResponseList.add(other1PollResponse);
//        inProgressPollResponseList.add(other2PollResponse);
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponseList,
//                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        PollResponse<CreateCertificateResponse> pollResponse = createCertPoller.blockUntil(PollResponse.OperationStatus.fromString("OTHER_2"));
//        Assert.assertEquals(pollResponse.getStatus(), PollResponse.OperationStatus.fromString("OTHER_2"));
//=======
//    public void blockForCustomOperationStatusTest() {
//        final OperationStatus expected = OperationStatus.fromString("OTHER_2");
//        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"));
//        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//        PollResponse<Response> other1PollResponse = new PollResponse<>(OperationStatus.fromString("OTHER_1"), new Response("Starting : Cert A"));
//        PollResponse<Response> other2PollResponse = new PollResponse<>(expected, new Response("Starting : Cert A"));
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse),
//            Mono.just(inProgressPollResponse), Mono.just(other1PollResponse), Mono.just(other2PollResponse),
//            Mono.just(successPollResponse));
//
//        // Act
//        final Poller<Response> createCertPoller = new Poller<>(Duration.ofMillis(100), pollOperation);
//        final PollResponse<Response> pollResponse = createCertPoller.blockUntil(expected);
//
//        // Assert
//        Assert.assertEquals(pollResponse.getStatus(), expected);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate successful response returned
//     * after few in-progress response. But the sdk client will stop polling in between
//     * and activate polling in between. The client will miss few in progress response and
//     * subscriber will get get final successful response.
//     */
//    @Ignore("When auto-subscription is turned off, the observer still polls. https://github.com/Azure/azure-sdk-for-java/issues/5805")
//    @Test
//<<<<<<< HEAD
//    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() throws Exception {
//
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 2;
//        Duration pollInterval = Duration.ofMillis(100);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                successPollResponse, 1500);
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        createCertPoller.getObserver().subscribe();
//
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    debug("Thread .. Sleeping ");
//                    Thread.sleep(pollInterval.toMillis() + (pollInterval.toMillis() / 2));
//                    debug("Thread wake up and stop polling. ");
//                    createCertPoller.setAutoPollingEnabled(false);
//                    Thread.sleep(1000);
//                    debug("Thread to enable Polling .. Sleeping ");
//                    createCertPoller.setAutoPollingEnabled(true);
//
//                } catch (Exception e) {
//                }
//            }
//        };
//        t.start();
//
//        debug("Poll and wait for it to complete  ");
//        Thread.sleep(totalTimeoutInMillis);
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//=======
//    public void subscribeToAllPollEventStopPollingAfterNSecondsAndRestartedTest() {
//        // Arrange
//        final PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A"), Duration.ofSeconds(1));
//        final PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//        final Duration pollInterval = Duration.ofMillis(100);
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));
//
//        // Act
//        final Poller<Response> poller = new Poller<>(pollInterval, pollOperation);
//
//        // Assert
//        StepVerifier.create(poller.getObserver())
//            .expectNext(inProgressPollResponse)
//            .then(() -> poller.setAutoPollingEnabled(false))
//            .expectNoEvent(Duration.ofSeconds(3))
//            .then(() -> poller.setAutoPollingEnabled(true))
//            .expectNext(successPollResponse)
//            .verifyComplete();
//
//        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
//        Assert.assertTrue(poller.isAutoPollingEnabled());
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    /*
//     * The test is setup where user will disable auto polling after creating poller.
//     * The user will enable polling after LRO is expected to complete.
//     * We want to ensure that if user enable polling after LRO is complete, user can
//     * final polling status.
//     */
//    @Test
//<<<<<<< HEAD
//    public void disableAutoPollAndEnableAfterCompletionSuccessfullyDone() throws Exception {
//
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//
//        int totalTileInSeconds = 5;
//        long totalTimeoutInMillis = 1000 * totalTileInSeconds;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                successPollResponse, 1800);
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//
//        Thread.sleep(6 * pollInterval.toMillis());
//        debug("Try to disable autopolling..");
//        createCertPoller.setAutoPollingEnabled(false);
//
//        Thread.sleep(totalTimeoutInMillis);
//        debug("Try to enable autopolling..");
//        createCertPoller.setAutoPollingEnabled(true);
//        Thread.sleep(5 * pollInterval.toMillis());
//        debug(createCertPoller.getStatus().toString());
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//        Thread.sleep(5 * pollInterval.toMillis());
//        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
//
//=======
//    public void disableAutoPollAndEnableAfterCompletionSuccessfullyDone() {
//        // Arrange
//        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
//        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting: Cert A"));
//        PollResponse<Response> initial = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("First: Cert A"));
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(initial), Mono.just(inProgress), Mono.just(success));
//
//        Poller<Response> poller = new Poller<>(Duration.ofSeconds(1), pollOperation);
//
//        // Act & Assert
//        poller.setAutoPollingEnabled(false);
//
//        StepVerifier.create(poller.getObserver())
//            .then(() -> poller.setAutoPollingEnabled(true))
//            .expectNext(inProgress)
//            .expectNext(success)
//            .verifyComplete();
//        Assert.assertSame(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
//        Assert.assertTrue(poller.isAutoPollingEnabled());
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    /*
//     * Test where SDK Client is subscribed all responses.
//     * The last response in this case will be OperationStatus.SUCCESSFULLY_COMPLETED
//     * This scenario is setup where source will generate successful response returned after few in-progress response.
//     **/
//    @Test
//    public void autoStartPollingAndSuccessfullyComplete() throws Exception {
//        // Arrange
//        PollResponse<Response> successPollResponse = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
//        PollResponse<Response> inProgressPollResponse = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//
//        Duration pollInterval = Duration.ofSeconds(1);
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgressPollResponse), Mono.just(successPollResponse));
//
//<<<<<<< HEAD
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//=======
//        Poller<Response> createCertPoller = new Poller<>(pollInterval, pollOperation);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//
//        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
//            Thread.sleep(pollInterval.toMillis());
//        }
//
//<<<<<<< HEAD
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }
//
//    /* Test where SDK Client is subscribed all responses.
//     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
//     * This scenario is setup where source will generate successful response returned after few in-progress response.
//     **/
//    @Test
//    public void subscribeToAllPollEventSuccessfullyCompleteInNSecondsTest() throws Exception {
//
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                successPollResponse, pollInterval.toMillis() * 2);
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        Thread.sleep(totalTimeoutInMillis);
//        debug("Calling poller.block ");
//        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//=======
//        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.getStatus());
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }
//
//    /** Test where SDK Client is subscribed to only final/last response.
//     * The last response in this case will be PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED
//     * This scenario is setup where source will generate successful response returned after few in progress response.
//     * But the subscriber is only interested in last response, The test will ensure subscriber
//     * only gets last PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED.
//     */
//    @Test
//    public void subscribeToOnlyFinalEventSuccessfullyCompleteInNSecondsTest() {
//        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
//        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//
//        Duration pollInterval = Duration.ofMillis(500);
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgress), Mono.just(success));
//
//<<<<<<< HEAD
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//
//        Assert.assertTrue(createCertPoller.block().getName().equals(CERTIFICATE_NAME));
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.SUCCESSFULLY_COMPLETED);
//=======
//        Poller<Response> createCertPoller = new Poller<>(pollInterval, pollOperation);
//
//        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.block().getStatus());
//        Assert.assertEquals(OperationStatus.SUCCESSFULLY_COMPLETED, createCertPoller.getStatus());
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses.
//     * This scenario is setup where source will generate successful response returned
//     * after few in-progress response. But the sdk client will stop polling in between
//     * and subscriber should never get final successful response.
//     */
//    @Ignore("https://github.com/Azure/azure-sdk-for-java/issues/5809")
//    @Test
//<<<<<<< HEAD
//    public void subscribeToAllPollEventStopPollingAfterNSecondsTest() throws Exception {
//
//        PollResponse<CreateCertificateResponse> successPollResponse = new PollResponse<>(PollResponse.OperationStatus.SUCCESSFULLY_COMPLETED, new CreateCertificateResponse("Created : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(100);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                successPollResponse, totalTimeoutInMillis - pollInterval.toMillis());
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(totalTimeoutInMillis / 2);
//                    createCertPoller.setAutoPollingEnabled(false);
//                } catch (Exception e) {
//                }
//            }
//        };
//        t.start();
//        Thread.sleep(totalTimeoutInMillis);
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.IN_PROGRESS);
//        Assert.assertFalse(createCertPoller.isAutoPollingEnabled());
//=======
//    public void subscribeToAllPollEventStopPollingAfterNSecondsTest() {
//        // Assert
//        Duration pollInterval = Duration.ofSeconds(1);
//        Duration waitTime = Duration.ofSeconds(3);
//        PollResponse<Response> success = new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created: Cert A"));
//        PollResponse<Response> inProgress = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A"));
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(inProgress), Mono.just(inProgress), Mono.just(success));
//
//        // Act
//        Poller<Response> poller = new Poller<>(pollInterval, pollOperation, null,
//            ignored -> new PollResponse<Response>(OperationStatus.USER_CANCELLED, null));
//
//        // Assert
//        StepVerifier.create(poller.getObserver())
//            .expectNext(inProgress)
//            .then(() -> poller.setAutoPollingEnabled(false))
//            .expectNoEvent(waitTime)
//            .thenCancel() // Cancel our subscription. This does not affect upstream poller.
//            .verify();
//
//        Assert.assertEquals(OperationStatus.IN_PROGRESS, poller.getStatus());
//        Assert.assertFalse(poller.isAutoPollingEnabled());
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses. This scenario is setup where source will generate successful
//     * response returned after few in-progress response. The sdk client will stop auto polling. It will subscribe and
//     * start receiving responses .The subscriber will get final successful response.
//     */
//    @Test
//    public void stopAutoPollAndManualPoll() {
//        // Arrange
//        final List<PollResponse<Response>> responses = new ArrayList<>();
//        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting : Cert A")));
//        responses.add(new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Middle: Cert A")));
//        responses.add(new PollResponse<>(OperationStatus.SUCCESSFULLY_COMPLETED, new Response("Created : Cert A")));
//
//        long totalTimeoutInMillis = 1000;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 20);
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(responses.get(0)), Mono.just(responses.get(1)), Mono.just(responses.get(2)));
//
//        Poller<Response> poller = new Poller<>(pollInterval, pollOperation);
//        poller.setAutoPollingEnabled(false);
//
//        // Act & Assert
//        int counter = 0;
//        while (poller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
//            counter++;
//
//<<<<<<< HEAD
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        createCertPoller.setAutoPollingEnabled(false);
//        while (createCertPoller.getStatus() != OperationStatus.SUCCESSFULLY_COMPLETED) {
//            PollResponse<CreateCertificateResponse> pollResponse = createCertPoller.poll().block();
//            Thread.sleep(pollInterval.toMillis());
//=======
//            PollResponse<Response> pollResponse = poller.poll().block();
//            Assert.assertSame("Counter: " + counter + " did not match.", responses.get(counter), pollResponse);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//        }
//
//        Assert.assertSame(OperationStatus.SUCCESSFULLY_COMPLETED, poller.getStatus());
//        Assert.assertFalse(poller.isAutoPollingEnabled());
//    }
//
//    /**
//     * Test where SDK Client is subscribed all responses. This scenario is setup where source will generate user
//     * cancelled response returned after few in-progress response. The sdk client will wait for it to cancel get final
//     * USER_CANCELLED response.
//     */
//    @Test
//<<<<<<< HEAD
//    public void subscribeToAllPollEventCancelOperatopnTest() throws Exception {
//
//        PollResponse<CreateCertificateResponse> cancelPollResponse = new PollResponse<>(OperationStatus.USER_CANCELLED, new CreateCertificateResponse("Cancelled : Cert A"));
//        PollResponse<CreateCertificateResponse> inProgressPollResponse = new PollResponse<>(PollResponse.OperationStatus.IN_PROGRESS, new CreateCertificateResponse("Starting : Cert A"));
//
//        long totalTimeoutInMillis = 1000 * 1;
//        Duration pollInterval = Duration.ofMillis(totalTimeoutInMillis / 10);
//
//        Function<PollResponse<CreateCertificateResponse>, Mono<PollResponse<CreateCertificateResponse>>> pollOperation =
//            createPollOperation(inProgressPollResponse,
//                cancelPollResponse, totalTimeoutInMillis - pollInterval.toMillis());
//
//        Poller<CreateCertificateResponse, CertificateOutput> createCertPoller = new Poller<CreateCertificateResponse, CertificateOutput>(pollInterval, pollOperation, createFetchResultOperation(CERTIFICATE_NAME));
//        createCertPoller.getObserver().subscribe(pr -> {
//            debug("Got Response " + pr.getStatus().toString() + " " + pr.getValue().response);
//        });
//        Thread t = new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(totalTimeoutInMillis / 2);
//                    debug("Cancelling operation");
//                    createCertPoller.cancelOperation().block();
//                } catch (Exception e) {
//                }
//            }
//        };
//        t.start();
//        Thread.sleep(totalTimeoutInMillis * 2);
//
//        StepVerifier.create(createCertPoller.result())
//            .verifyErrorSatisfies(ex -> assertException(ex, IllegalAccessException.class));
//        Assert.assertTrue(createCertPoller.getStatus() == OperationStatus.USER_CANCELLED);
//        Assert.assertTrue(createCertPoller.isAutoPollingEnabled());
//    }
//
//    private <T> void assertException(Throwable exception, Class<T> expectedExceptionType) {
//        assertEquals(expectedExceptionType, exception.getClass());
//    }
//
//    private void debug(String... messages) {
//        if (debug) {
//            StringBuilder sb =
//                new StringBuilder(new Date().toString()).append(" ").append(getClass().getName()).append(" ").append(count).append(" ");
//            for (String m : messages) {
//                sb.append(m);
//            }
//            logger.info(sb.toString());
//        }
//=======
//    public void subscribeToAllPollEventCancelOperationTest() {
//        Duration pollInterval = Duration.ofMillis(500);
//        PollResponse<Response> cancellation = new PollResponse<>(OperationStatus.USER_CANCELLED, new Response("Created : Cert A"));
//        PollResponse<Response> first = new PollResponse<>(OperationStatus.IN_PROGRESS, new Response("Starting: Cert A"));
//
//        when(pollOperation.apply(any())).thenReturn(Mono.just(first), Mono.just(cancellation));
//
//        // Act
//        Poller<Response> poller = new Poller<>(pollInterval, pollOperation, null, cancelOperation);
//
//        // Assert
//        StepVerifier.create(poller.getObserver())
//            .expectNext(first)
//            .then(() -> poller.cancelOperation())
//            .thenCancel() // cancel this actual subscriber, this does not affect the parent operation.
//            .verify();
//
//        Assert.assertEquals(OperationStatus.USER_CANCELLED, poller.block().getStatus());
//        Assert.assertEquals(OperationStatus.USER_CANCELLED, poller.getStatus());
//        Assert.assertTrue(poller.isAutoPollingEnabled());
//
//        verify(cancelOperation, Mockito.times(1)).accept(poller);
//>>>>>>> 1e7a5ffe8422700b4888662f74a83467861af89e
//    }
//
//    public static class Response {
//        private final String response;
//
//        public Response(String response) {
//            this.response = response;
//        }
//
//        public String getResponse() {
//            return response;
//        }
//
//        @Override
//        public String toString() {
//            return "Response: " + response;
//        }
//    }
//
//    public class CertificateOutput {
//        String name;
//
//        public CertificateOutput(String certName) {
//            name = certName;
//        }
//
//        public String getName() {
//            return name;
//        }
//    }
//}
