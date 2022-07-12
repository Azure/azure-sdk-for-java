// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.params.provider.Arguments;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A utility class for retry policy tests suppliers.
 */
public class RetryPolicyTestUtil {
    static Stream<Arguments> customRetryPolicyCanDetermineRetryStatusCodesSupplier() {
        RetryStrategy onlyRetries429And503 = createStatusCodeRetryStrategy(429, 503);
        RetryStrategy onlyRetries409And412 = createStatusCodeRetryStrategy(409, 412);

        return Stream.of(
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {429, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {503, 404}, 404),
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 503}, 503),
            Arguments.of(onlyRetries429And503, new int[] {429, 503, 429}, 429),

            Arguments.of(onlyRetries409And412, new int[] {409, 412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {409, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {412, 404}, 404),
            Arguments.of(onlyRetries409And412, new int[] {409, 412, 409}, 409),
            Arguments.of(onlyRetries409And412, new int[] {409, 412, 412}, 412)
        );
    }

    static RetryStrategy createExceptionRetryStrategy(List<Class<? extends Throwable>> retriableExceptions) {
        return new RetryStrategy() {
            @Override
            public int getMaxRetries() {
                return 2;
            }

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofMillis(1);
            }

            @Override
            public boolean shouldRetryException(Throwable throwable) {
                return retriableExceptions.stream()
                    .anyMatch(retriableException -> retriableException.isAssignableFrom(throwable.getClass()));
            }
        };
    }

    static HttpResponse sendRequest(HttpPipeline pipeline) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).block();
    }

    static HttpResponse sendRequestSync(HttpPipeline pipeline) {
        return pipeline.sendSync(new HttpRequest(HttpMethod.GET, "http://localhost/"), Context.NONE);
    }

    static RetryStrategy createStatusCodeRetryStrategy(int... retriableErrorCodes) {
        return new RetryStrategy() {
            @Override
            public int getMaxRetries() {
                return 2;
            }

            @Override
            public Duration calculateRetryDelay(int retryAttempts) {
                return Duration.ofMillis(1);
            }

            @Override
            public boolean shouldRetry(HttpResponse httpResponse) {
                return Arrays.stream(retriableErrorCodes)
                    .anyMatch(retriableErrorCode -> httpResponse.getStatusCode() == retriableErrorCode);
            }
        };
    }

    static Stream<Arguments> getWellKnownRetryDelaySupplier() {
        RetryStrategy retryStrategy = mock(RetryStrategy.class);
        when(retryStrategy.calculateRetryDelay(anyInt())).thenReturn(Duration.ofSeconds(1));

        return Stream.of(
            // No well-known headers, fallback to the default.
            Arguments.of(new HttpHeaders(), retryStrategy, Duration.ofSeconds(1)),

            // x-ms-retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "10"), retryStrategy, Duration.ofMillis(10)),

            // x-ms-retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("x-ms-retry-after-ms", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // retry-after-ms should be respected as milliseconds.
            Arguments.of(new HttpHeaders().set("retry-after-ms", "64"), retryStrategy, Duration.ofMillis(64)),

            // retry-after-ms wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("retry-after-ms", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("retry-after-ms", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After should be respected as seconds.
            Arguments.of(new HttpHeaders().set("Retry-After", "10"), retryStrategy, Duration.ofSeconds(10)),

            // Retry-After wasn't a valid number, fallback to the default.
            Arguments.of(new HttpHeaders().set("Retry-After", "-10"), retryStrategy, Duration.ofSeconds(1)),
            Arguments.of(new HttpHeaders().set("Retry-After", "ten"), retryStrategy, Duration.ofSeconds(1)),

            // Retry-After was before the current time, fallback to the default.
            Arguments.of(new HttpHeaders().set("Retry-After", OffsetDateTime.now().minusMinutes(1)
                .atZoneSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.RFC_1123_DATE_TIME)), retryStrategy, Duration.ofSeconds(1))
        );
    }

    static Stream<Throwable> defaultRetryPolicyRetriesAllExceptionsSupplier() {
        return Stream.of(
            new MalformedURLException(),
            new RuntimeException(),
            new IllegalStateException(),
            new TimeoutException()
        );
    }

    static Stream<Throwable> defaultRetryPolicyDoesNotRetryErrorsSupplier() {
        // Don't use specific types of Error as it leads to the JVM issues with JUnit, such as ThreadDeath killing the
        // JUnit test runner thread.
        return Stream.of(
            new Throwable(),
            new Error()
        );
    }

    static Stream<Arguments> customRetryPolicyCanDetermineRetryExceptionsSupplier() {
        RetryStrategy onlyRetriesIOExceptions = createExceptionRetryStrategy(
            Collections.singletonList(IOException.class));
        RetryStrategy onlyRetriesTimeoutAndRuntimeExceptions = createExceptionRetryStrategy(
            Arrays.asList(TimeoutException.class, RuntimeException.class));

        return Stream.of(
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new IOException(),
                new RuntimeException()}, RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new RuntimeException()},
                RuntimeException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new TimeoutException()},
                TimeoutException.class),
            Arguments.of(onlyRetriesIOExceptions, new Throwable[]{new IOException(), new IOException(),
                new IOException()}, IOException.class),

            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new RuntimeException(),
                new IOException()}, IOException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new TimeoutException()}, TimeoutException.class),
            Arguments.of(onlyRetriesTimeoutAndRuntimeExceptions, new Throwable[]{new TimeoutException(),
                new RuntimeException(), new RuntimeException()}, RuntimeException.class)
        );
    }

}
