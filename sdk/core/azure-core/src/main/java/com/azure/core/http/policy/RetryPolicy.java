// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.logging.LoggingKeys;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * The {@code RetryPolicy} class is an implementation of the {@link HttpPipelinePolicy} interface. This policy handles
 * HTTP retries by determining if an HTTP request should be retried based on the received {@link HttpResponse}.
 *
 * <p>This class is useful when you need to handle HTTP retries in a pipeline. It uses a {@link RetryStrategy} to
 * decide if a request should be retried. By default, it uses the {@link ExponentialBackoff} strategy, which uses
 * a delay duration that exponentially increases with each retry attempt until an upper bound is reached.</p>
 *
 * <p><strong>Code sample:</strong></p>
 *
 * <p>In this example, a {@code RetryPolicy} is created which can then be added to the pipeline. For the request then
 * sent by the pipeline, if the server responds with a status code that indicates a transient error, the request will be
 * retried according to the {@link RetryStrategy} used by the {@code RetryPolicy}.</p>
 *
 * <!-- src_embed com.azure.core.http.policy.RetryPolicy.constructor -->
 * <pre>
 * RetryPolicy retryPolicy = new RetryPolicy&#40;&#41;;
 * </pre>
 * <!-- end com.azure.core.http.policy.RetryPolicy.constructor -->
 *
 * @see com.azure.core.http.policy
 * @see com.azure.core.http.policy.HttpPipelinePolicy
 * @see com.azure.core.http.HttpPipeline
 * @see com.azure.core.http.HttpRequest
 * @see com.azure.core.http.HttpResponse
 * @see com.azure.core.http.policy.RetryStrategy
 * @see com.azure.core.http.policy.DefaultRedirectStrategy
 */
public class RetryPolicy implements HttpPipelinePolicy {
    // RetryPolicy is a commonly used policy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);

    private final RetryStrategy retryStrategy;
    private final HttpHeaderName retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    /**
     * Creates {@link RetryPolicy} using {@link ExponentialBackoff#ExponentialBackoff()} as the {@link RetryStrategy}.
     */
    public RetryPolicy() {
        this(new ExponentialBackoff(), null, null);
    }

    /**
     * Creates {@link RetryPolicy} using {@link ExponentialBackoff#ExponentialBackoff()} as the {@link RetryStrategy}
     * and uses {@code retryAfterHeader} to look up the wait period in the returned {@link HttpResponse} to calculate
     * the retry delay when a recoverable HTTP error is returned.
     *
     * @param retryAfterHeader The HTTP header, such as {@code Retry-After} or {@code x-ms-retry-after-ms}, to lookup
     * for the retry delay. If the value is null, {@link RetryStrategy#calculateRetryDelay(int)} will compute the delay
     * and ignore the delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. Null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     * @throws NullPointerException When {@code retryAfterTimeUnit} is null and {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this(new ExponentialBackoff(), retryAfterHeader, retryAfterTimeUnit);
    }

    /**
     * Creates {@link RetryPolicy} with the provided {@link RetryStrategy} and default {@link ExponentialBackoff} as
     * {@link RetryStrategy}. It will use provided {@code retryAfterHeader} in {@link HttpResponse} headers for
     * calculating retry delay.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @param retryAfterHeader The HTTP header, such as 'Retry-After' or 'x-ms-retry-after-ms', to lookup for the retry
     * delay. If the value is null, {@link RetryPolicy} will use the retry strategy to compute the delay and ignore the
     * delay provided in response header.
     * @param retryAfterTimeUnit The time unit to use when applying the retry delay. null is valid if, and only if,
     * {@code retryAfterHeader} is null.
     * @throws NullPointerException If {@code retryStrategy} is null or when {@code retryAfterTimeUnit} is null and
     * {@code retryAfterHeader} is not null.
     */
    public RetryPolicy(RetryStrategy retryStrategy, String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        this.retryStrategy = Objects.requireNonNull(retryStrategy, "'retryStrategy' cannot be null.");
        this.retryAfterHeader = HttpHeaderName.fromString(retryAfterHeader);
        this.retryAfterTimeUnit = retryAfterTimeUnit;
        if (!isNullOrEmpty(retryAfterHeader)) {
            Objects.requireNonNull(retryAfterTimeUnit, "'retryAfterTimeUnit' cannot be null.");
        }
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryStrategy}.
     *
     * @param retryStrategy The {@link RetryStrategy} used for retries.
     * @throws NullPointerException If {@code retryStrategy} is null.
     */
    public RetryPolicy(RetryStrategy retryStrategy) {
        this(retryStrategy, null, null);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryOptions}.
     *
     * @param retryOptions The {@link RetryOptions} used to configure this {@link RetryPolicy}.
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    public RetryPolicy(RetryOptions retryOptions) {
        this(ImplUtils.getRetryStrategyFromOptions(retryOptions), null, null);
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        // Create a buffered version of the request that will be used each retry.
        // The buffering is done here as once the request body has been buffered once it doesn't need to be buffered
        // again as it will be buffered as a read-only buffer.
        HttpRequest originalHttpRequest = context.getHttpRequest();
        BinaryData originalRequestBody = originalHttpRequest.getBodyAsBinaryData();
        if (retryStrategy.getMaxRetries() > 0 && originalRequestBody != null && !originalRequestBody.isReplayable()) {
            return originalRequestBody.toReplayableBinaryDataAsync().flatMap(replayableBody -> {
                context.getHttpRequest().setBody(replayableBody);
                return attemptAsync(context, next, originalHttpRequest, 0, null);
            });
        }

        return attemptAsync(context, next, originalHttpRequest, 0, null);
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        // Create a buffered version of the request that will be used each retry.
        // The buffering is done here as once the request body has been buffered once it doesn't need to be buffered
        // again as it will be buffered as a read-only buffer.
        HttpRequest originalHttpRequest = context.getHttpRequest();
        BinaryData originalRequestBody = originalHttpRequest.getBodyAsBinaryData();
        if (retryStrategy.getMaxRetries() > 0 && originalRequestBody != null && !originalRequestBody.isReplayable()) {
            context.getHttpRequest().setBody(context.getHttpRequest().getBodyAsBinaryData().toReplayableBinaryData());
        }

        return attemptSync(context, next, originalHttpRequest, 0, null);
    }

    private Mono<HttpResponse> attemptAsync(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
        HttpRequest originalHttpRequest, int tryCount, List<Throwable> suppressed) {
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
        // Clone the original request to ensure that each try starts with the original (unmutated) request.
        context.setHttpRequest(originalHttpRequest.copy());

        return next.clone().process().flatMap(httpResponse -> {
            if (shouldRetry(retryStrategy, httpResponse, tryCount, suppressed)) {
                final Duration delayDuration = determineDelayDuration(httpResponse, tryCount, retryStrategy,
                    retryAfterHeader, retryAfterTimeUnit);
                logRetry(tryCount, delayDuration);

                httpResponse.close();

                return addBackoffDelay(attemptAsync(context, next, originalHttpRequest, tryCount + 1, suppressed),
                    delayDuration);
            } else {
                if (tryCount >= retryStrategy.getMaxRetries()) {
                    logRetryExhausted(tryCount);
                }
                return Mono.just(httpResponse);
            }
        }).onErrorResume(Exception.class, err -> {
            if (shouldRetryException(retryStrategy, err, tryCount, suppressed)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);
                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;
                suppressedLocal.add(err);
                return addBackoffDelay(attemptAsync(context, next, originalHttpRequest, tryCount + 1, suppressedLocal),
                    retryStrategy.calculateRetryDelay(tryCount));
            } else {
                logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", err);
                if (suppressed != null) {
                    suppressed.forEach(err::addSuppressed);
                }
                return Mono.error(err);
            }
        });
    }

    private Mono<HttpResponse> addBackoffDelay(Mono<HttpResponse> retryOperation, Duration delayDuration) {
        return (!delayDuration.isNegative() && !delayDuration.isZero())
            ? retryOperation.delaySubscription(delayDuration)
            : retryOperation;
    }

    private HttpResponse attemptSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
        HttpRequest originalHttpRequest, int tryCount, List<Throwable> suppressed) {
        HttpResponse httpResponse;
        try {
            context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
            // Clone the original request to ensure that each try starts with the original (unmutated) request.
            context.setHttpRequest(originalHttpRequest.copy());

            httpResponse = next.clone().processSync();
        } catch (RuntimeException err) {
            if (shouldRetryException(retryStrategy, err, tryCount, suppressed)) {
                logRetryWithError(LOGGER.atVerbose(), tryCount, "Error resume.", err);
                Duration delayDuration = retryStrategy.calculateRetryDelay(tryCount);
                if (!delayDuration.isNegative() && !delayDuration.isZero()) {
                    try {
                        Thread.sleep(delayDuration.toMillis());
                    } catch (InterruptedException ex) {
                        err.addSuppressed(ex);
                        throw LOGGER.logExceptionAsError(err);
                    }
                }

                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;
                suppressedLocal.add(err);
                return attemptSync(context, next, originalHttpRequest, tryCount + 1, suppressedLocal);
            } else {
                logRetryWithError(LOGGER.atError(), tryCount, "Retry attempts have been exhausted.", err);
                if (suppressed != null) {
                    suppressed.forEach(err::addSuppressed);
                }

                throw LOGGER.logExceptionAsError(err);
            }
        }

        if (shouldRetry(retryStrategy, httpResponse, tryCount, suppressed)) {
            final Duration delayDuration
                = determineDelayDuration(httpResponse, tryCount, retryStrategy, retryAfterHeader, retryAfterTimeUnit);
            logRetry(tryCount, delayDuration);

            httpResponse.close();

            if (!delayDuration.isNegative() && !delayDuration.isZero()) {
                try {
                    Thread.sleep(delayDuration.toMillis());
                } catch (InterruptedException ie) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ie));
                }
            }
            return attemptSync(context, next, originalHttpRequest, tryCount + 1, suppressed);
        } else {
            if (tryCount >= retryStrategy.getMaxRetries()) {
                logRetryExhausted(tryCount);
            }
            return httpResponse;
        }
    }

    private static boolean shouldRetry(RetryStrategy retryStrategy, HttpResponse response, int tryCount,
        List<Throwable> retriedExceptions) {
        return tryCount < retryStrategy.getMaxRetries()
            && retryStrategy
                .shouldRetryCondition(new RequestRetryCondition(response, null, tryCount, retriedExceptions));
    }

    private static boolean shouldRetryException(RetryStrategy retryStrategy, Throwable throwable, int tryCount,
        List<Throwable> retriedExceptions) {
        // Check if there are any retry attempts still available.
        if (tryCount >= retryStrategy.getMaxRetries()) {
            return false;
        }

        // Unwrap the throwable.
        Throwable causalThrowable = Exceptions.unwrap(throwable);
        RequestRetryCondition requestRetryCondition
            = new RequestRetryCondition(null, causalThrowable, tryCount, retriedExceptions);

        // Check all causal exceptions in the exception chain.
        while (causalThrowable != null) {
            if (retryStrategy.shouldRetryCondition(requestRetryCondition)) {
                return true;
            }

            causalThrowable = causalThrowable.getCause();
            requestRetryCondition = new RequestRetryCondition(null, causalThrowable, tryCount, retriedExceptions);
        }

        // Finally just return false as this can't be retried.
        return false;
    }

    private static void logRetry(int tryCount, Duration delayDuration) {
        LOGGER.atVerbose()
            .addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount)
            .addKeyValue(LoggingKeys.DURATION_MS_KEY, delayDuration.toMillis())
            .log("Retrying.");
    }

    private static void logRetryExhausted(int tryCount) {
        LOGGER.atInfo().addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount).log("Retry attempts have been exhausted.");
    }

    private static void logRetryWithError(LoggingEventBuilder loggingEventBuilder, int tryCount, String format,
        Throwable throwable) {
        loggingEventBuilder.addKeyValue(LoggingKeys.TRY_COUNT_KEY, tryCount).log(format, throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
        HttpHeaderName retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (retryAfterHeader == null) {
            return getWellKnownRetryDelay(response.getHeaders(), tryCount, retryStrategy, OffsetDateTime::now);
        }

        String retryHeaderValue = response.getHeaderValue(retryAfterHeader);

        // Retry header is missing or empty, return the default delay duration.
        if (isNullOrEmpty(retryHeaderValue)) {
            return retryStrategy.calculateRetryDelay(tryCount);
        }

        // Use the response delay duration, the server returned it for a reason.
        return Duration.of(Integer.parseInt(retryHeaderValue), retryAfterTimeUnit);
    }

    /*
     * Determines the delay duration that should be waited before retrying using the well-known retry headers.
     */
    static Duration getWellKnownRetryDelay(HttpHeaders responseHeaders, int tryCount, RetryStrategy retryStrategy,
        Supplier<OffsetDateTime> nowSupplier) {
        Duration retryDelay = ImplUtils.getRetryAfterFromHeaders(responseHeaders, nowSupplier);
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return the default delay duration.
        return retryStrategy.calculateRetryDelay(tryCount);
    }
}
