// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;

import static com.azure.core.util.CoreUtils.isNullOrEmpty;

/**
 * A pipeline policy that retries when a recoverable HTTP error or exception occurs.
 */
public class RetryPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(RetryPolicy.class);

    private static final String FIXED_RETRY_MODE = "fixed";
    private static final String EXPONENTIAL_RETRY_MODE = "exponential";

    private final RetryStrategy retryStrategy;
    private final String retryAfterHeader;
    private final ChronoUnit retryAfterTimeUnit;

    private static final ConfigurationProperty<String> RETRY_MODE_PROPERTY = ConfigurationProperty.stringPropertyBuilder("http-retry.mode")
        .defaultValue(EXPONENTIAL_RETRY_MODE)
        .global(true)
        .canLogValue(true)
        .build();

    private static final ConfigurationProperty<String> RETRY_AFTER_HEADER_PROPERTY = ConfigurationProperty.stringPropertyBuilder("http-retry.retry-after-header")
        .global(true)
        .canLogValue(true)
        .build();

    private static final ConfigurationProperty<ChronoUnit> RETRY_AFTER_TIME_UNIT_PROPERTY= new ConfigurationPropertyBuilder<>("http-retry.retry-after-time-unit", (value) -> ChronoUnit.valueOf(value))
        .global(true)
        .build();

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
        this.retryAfterHeader = retryAfterHeader;
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

    public static RetryPolicy fromConfiguration(Configuration configuration) {
        Objects.requireNonNull(configuration, "'configuration' cannot be null");
        if (configuration == Configuration.NONE) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'configuration' cannot be 'Configuration.NONE'."));
        }

        String retryMode = configuration.get(RETRY_MODE_PROPERTY);
        RetryStrategy retryStrategy;
        if (FIXED_RETRY_MODE.equals(retryMode)) {
            // fixed strategy does not have defaults, it will throw if required properties are missing
            retryStrategy = FixedDelay.fromConfiguration(configuration);
        } else if (EXPONENTIAL_RETRY_MODE.equals(retryMode)) {
            retryStrategy = ExponentialBackoff.fromConfiguration(configuration);
        } else {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Unknown retry mode: " + retryMode));
        }

        String retryAfterHeader = configuration.get(RETRY_AFTER_HEADER_PROPERTY);
        ChronoUnit retryAfterUnit = configuration.get(RETRY_AFTER_TIME_UNIT_PROPERTY);

        return new RetryPolicy(retryStrategy, retryAfterHeader, retryAfterUnit);
    }

    /**
     * Creates a {@link RetryPolicy} with the provided {@link RetryOptions}.
     *
     * @param retryOptions The {@link RetryOptions} used to configure this {@link RetryPolicy}.
     * @throws NullPointerException If {@code retryOptions} is null.
     */
    public RetryPolicy(RetryOptions retryOptions) {
        this(
            getRetryStrategyFromOptions(
                Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.")),
            Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.").getRetryAfterHeader(),
            Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.").getRetryAfterTimeUnit());
    }

    private static RetryStrategy getRetryStrategyFromOptions(RetryOptions retryOptions) {
        if (retryOptions.getExponentialBackoffOptions() != null) {
            return new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
        } else if (retryOptions.getFixedDelayOptions() != null) {
            return new FixedDelay(retryOptions.getFixedDelayOptions());
        } else {
            // This should never happen.
            throw new IllegalArgumentException("'retryOptions' didn't define any retry strategy options");
        }
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        return attemptAsync(context, next, context.getHttpRequest(), 0);
    }

    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, final HttpPipelineNextPolicy next,
        final HttpRequest originalHttpRequest, final int tryCount) {
        context.setHttpRequest(originalHttpRequest.copy());
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, tryCount + 1);
        return next.clone().process()
            .flatMap(httpResponse -> {
                if (shouldRetry(httpResponse, tryCount)) {
                    final Duration delayDuration = determineDelayDuration(httpResponse, tryCount, retryStrategy,
                        retryAfterHeader, retryAfterTimeUnit);
                    LOGGER.atVerbose()
                        .addKeyValue("tryCount", tryCount)
                        .addKeyValue("delaySec", delayDuration.getSeconds())
                        .log("Retrying.");

                    Flux<ByteBuffer> responseBody = httpResponse.getBody();
                    if (responseBody == null) {
                        return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                            .delaySubscription(delayDuration);
                    } else {
                        return httpResponse.getBody()
                            .ignoreElements()
                            .then(attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                                .delaySubscription(delayDuration));
                    }
                } else {
                    if (tryCount >= retryStrategy.getMaxRetries()) {
                        LOGGER.atInfo()
                            .addKeyValue("tryCount", tryCount)
                            .log("Retry attempts have been exhausted.");
                    }
                    return Mono.just(httpResponse);
                }
            })
            .onErrorResume(err -> {
                if (shouldRetryException(err, tryCount)) {
                    LOGGER.atVerbose()
                        .addKeyValue("tryCount", tryCount)
                        .log("Error Resume.", err);

                    return attemptAsync(context, next, originalHttpRequest, tryCount + 1)
                        .delaySubscription(retryStrategy.calculateRetryDelay(tryCount));
                } else {
                    LOGGER.atInfo()
                        .addKeyValue("tryCount", tryCount)
                        .log("Retry attempts have been exhausted.", err);
                    return Mono.error(err);
                }
            });
    }

    private boolean shouldRetry(HttpResponse response, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetry(response);
    }

    private boolean shouldRetryException(Throwable throwable, int tryCount) {
        return tryCount < retryStrategy.getMaxRetries() && retryStrategy.shouldRetryException(throwable);
    }

    /*
     * Determines the delay duration that should be waited before retrying.
     */
    static Duration determineDelayDuration(HttpResponse response, int tryCount, RetryStrategy retryStrategy,
        String retryAfterHeader, ChronoUnit retryAfterTimeUnit) {
        // If the retry after header hasn't been configured, attempt to look up the well-known headers.
        if (isNullOrEmpty(retryAfterHeader)) {
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
