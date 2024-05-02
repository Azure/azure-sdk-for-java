// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.logging.LogLevel;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RetryTests extends BlobTestBase {
    private static URL retryTestURL;

    static {
        try {
            retryTestURL = new URL("https://" + RequestRetryTestFactory.RETRY_TEST_PRIMARY_HOST);
        } catch (MalformedURLException e) {
            LOGGER.log(LogLevel.VERBOSE, () -> "Failed to create URL for retry tests.", e);
        }
    }

    private static final RequestRetryOptions REQUEST_RETRY_OPTIONS = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL,
        6, 2, 1000L, 4000L, RequestRetryTestFactory.RETRY_TEST_SECONDARY_HOST);

    protected void retryScenario(Runnable runnable) {
        int retry = 0;
        while (retry < 3) {
            try {
                runnable.run();
                break;
            } catch (Exception ex) {
                retry++;
                sleepIfRunningAgainstService(1000);
            }
        }
    }

    @Test
    public void retriesUntilSuccess() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(
                    RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS, REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono)
                .assertNext(it -> {
                    assertEquals(it.getStatusCode(), 200);
                    assertEquals(6, retryTestFactory.getTryNumber());
                }).verifyComplete();
        });
    }

    @Test
    public void syncRetriesUntilSuccessWithPrimaryAndSecondaryHostScenario() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(
                    RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS,
                    REQUEST_RETRY_OPTIONS);

            try (HttpResponse response = retryTestFactory.sendSync(retryTestURL)) {
                assertEquals(response.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 6);
            }
        });
    }

    @Test
    public void retriesUntilMaxRetries() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(
                    RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES,
                    REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono)
                .assertNext(it -> {
                    assertEquals(it.getStatusCode(), 503);
                    assertEquals(retryTestFactory.getTryNumber(), REQUEST_RETRY_OPTIONS.getMaxTries());
                }).verifyComplete();
        });
    }

    @Test
    public void retriesUntilMaxRetriesWithException() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES_WITH_EXCEPTION,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).expectErrorSatisfies(it -> {
                assertInstanceOf(IOException.class, it);
                assertEquals(it.getMessage(), "Exception number " + REQUEST_RETRY_OPTIONS.getMaxTries());
                assertNotNull(it.getSuppressed());
                assertInstanceOf(IOException.class, it.getSuppressed()[0]);
                assertEquals("Exception number 1", it.getSuppressed()[0].getMessage());
                assertInstanceOf(IOException.class, it.getSuppressed()[1]);
                assertEquals("Exception number 2", it.getSuppressed()[1].getMessage());
                assertInstanceOf(IOException.class, it.getSuppressed()[2]);
                assertEquals("Exception number 3", it.getSuppressed()[2].getMessage());
                assertInstanceOf(IOException.class, it.getSuppressed()[3]);
                assertEquals("Exception number 4", it.getSuppressed()[3].getMessage());
                assertInstanceOf(IOException.class, it.getSuppressed()[4]);
                assertEquals("Exception number 5", it.getSuppressed()[4].getMessage());
            }).verify();
        });
    }

    @Test
    public void retriesNonRetryable() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(
                    RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE,
                    REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono)
                .assertNext(it -> {
                    assertEquals(it.getStatusCode(), 400);
                    assertEquals(retryTestFactory.getTryNumber(), 1);
                }).verifyComplete();
        });
    }

    @Test
    public void retriesNonRetryableSecondary() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(
                    RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY,
                    REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 400);
                assertEquals(retryTestFactory.getTryNumber(), 2);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesNetworkError() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_NETWORK_ERROR,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 3);
            }).verifyComplete();
        });
    }


    @Test
    public void retriesWrappedNetworkError() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_WRAPPED_NETWORK_ERROR,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 3);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesWrappedTimeoutError() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory =
                new RequestRetryTestFactory(RequestRetryTestFactory.RETRY_TEST_SCENARIO_WRAPPED_TIMEOUT_ERROR,
                    REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 3);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesTryTimeout() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_TRY_TIMEOUT,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 3);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesExponentialDelay() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_TRY_TIMEOUT,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 3);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesFixedDelay() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_FIXED_TIMING,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).assertNext(it -> {
                assertEquals(it.getStatusCode(), 200);
                assertEquals(retryTestFactory.getTryNumber(), 4);
            }).verifyComplete();
        });
    }

    @Test
    public void retriesNonReplyableFlux() {
        retryScenario(() -> {
            RequestRetryTestFactory retryTestFactory = new RequestRetryTestFactory(
                RequestRetryTestFactory.RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE,
                REQUEST_RETRY_OPTIONS);

            Mono<HttpResponse> responseMono = Mono.defer(() -> retryTestFactory.send(retryTestURL));

            StepVerifier.create(responseMono).verifyErrorMatches(it -> {
                assertInstanceOf(IllegalStateException.class, it);
                assertTrue(it.getMessage().startsWith("The request failed because"));
                assertInstanceOf(UnexpectedLengthException.class, it.getCause());
                return true;
            });
        });
    }

    @ParameterizedTest
    @MethodSource("retriesOptionsInvalidSupplier")
    public void retriesOptionsInvalid(Integer maxTries, Integer tryTimeout, Long retryDelayInMs,
        Long maxRetryDelayInMs) {
        retryScenario(() -> assertThrows(IllegalArgumentException.class, () ->
            new RequestRetryOptions(null, maxTries, tryTimeout, retryDelayInMs, maxRetryDelayInMs, null)));
    }

    private static Stream<Arguments> retriesOptionsInvalidSupplier() {
        return Stream.of(
            Arguments.of(0, null, null, null),
            Arguments.of(null, 0, null, null),
            Arguments.of(null, null, 0L, 1L),
            Arguments.of(null, null, 1L, 0L),
            Arguments.of(null, null, null, 1L),
            Arguments.of(null, null, 1L, null),
            Arguments.of(null, null, 5L, 4L)
        );
    }

}
