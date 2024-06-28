// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;


import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Contexts;
import com.azure.core.util.ProgressReporter;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

/**
 * This is a request policy in an {@link com.azure.core.http.HttpPipeline} for retrying a given HTTP request. The
 * request that is retried will be identical each time it is reissued.  Retries will try against a secondary if one is
 * specified and the type of operation/error indicates that the secondary can handle the request. Exponential and fixed
 * backoff are supported. The policy must only be used directly when creating a custom pipeline.
 */
public final class RequestRetryPolicy implements HttpPipelinePolicy {
    private static final ClientLogger LOGGER = new ClientLogger(RequestRetryPolicy.class);
    private final RequestRetryOptions requestRetryOptions;
    private static final HttpHeaderName X_MS_COPY_SOURCE_ERROR_CODE = HttpHeaderName.fromString("x-ms-copy-source-error-code");

    /**
     * Constructs the policy using the retry options.
     *
     * @param requestRetryOptions Retry options for the policy.
     */
    public RequestRetryPolicy(RequestRetryOptions requestRetryOptions) {
        this.requestRetryOptions = requestRetryOptions;
    }

    @Override
    public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
        boolean considerSecondary = (this.requestRetryOptions.getSecondaryHost() != null)
            && (HttpMethod.GET.equals(context.getHttpRequest().getHttpMethod())
            || HttpMethod.HEAD.equals(context.getHttpRequest().getHttpMethod()));

        // Create a buffered version of the request that will be used each retry.
        // The buffering is done here as once the request body has been buffered once it doesn't need to be buffered
        // again as it will be buffered as a read-only buffer.
        HttpRequest originalHttpRequest = context.getHttpRequest();
        BinaryData originalRequestBody = originalHttpRequest.getBodyAsBinaryData();
        if (requestRetryOptions.getMaxTries() > 1 && originalRequestBody != null
            && !originalRequestBody.isReplayable()) {
            context.getHttpRequest().setBody(context.getHttpRequest().getBodyAsBinaryData().toReplayableBinaryData());
        }

        return this.attemptSync(context, next, originalHttpRequest, considerSecondary, 1, 1, null);
    }
    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        boolean considerSecondary = (this.requestRetryOptions.getSecondaryHost() != null)
            && (HttpMethod.GET.equals(context.getHttpRequest().getHttpMethod())
            || HttpMethod.HEAD.equals(context.getHttpRequest().getHttpMethod()));

        // Create a buffered version of the request that will be used each retry.
        // The buffering is done here as once the request body has been buffered once it doesn't need to be buffered
        // again as it will be buffered as a read-only buffer.
        HttpRequest originalHttpRequest = context.getHttpRequest();
        BinaryData originalRequestBody = originalHttpRequest.getBodyAsBinaryData();
        if (requestRetryOptions.getMaxTries() > 1 && originalRequestBody != null
            && !originalRequestBody.isReplayable()) {
            // Replayable bodies don't require this transformation.
            // TODO (kasobol-msft) Remove this transformation in favor of
            // BinaryData.toReplayableBinaryData()
            // But this should be done together with removal of buffering in chunked uploads.
            Flux<ByteBuffer> bufferedBody = context.getHttpRequest().getBody().map(ByteBuffer::duplicate);
            context.getHttpRequest().setBody(bufferedBody);
        }

        return this.attemptAsync(context, next, context.getHttpRequest(), considerSecondary, 1, 1, null);
    }

    /**
     * This method actually attempts to send the request and determines if we should attempt again and, if so, how long
     * to wait before sending out the next request.
     * <p>
     * Exponential retry algorithm: ((2 ^ attempt) - 1) * delay * random(0.8, 1.2) When to retry: connection failure or
     * an HTTP status code of 500 or greater, except 501 and 505 If using a secondary: Odd tries go against primary;
     * even tries go against the secondary For a primary wait ((2 ^ primaryTries - 1) * delay * random(0.8, 1.2) If
     * secondary gets a 404, don't fail, retry but future retries are only against the primary When retrying against a
     * secondary, ignore the retry count and wait (.1 second * random(0.8, 1.2))
     *
     * @param context The request to try.
     * @param next The next policy to apply to the request.
     * @param originalRequest The unmodified original request.
     * @param considerSecondary Before each try, we'll select either the primary or secondary URL if appropriate.
     * @param primaryTry Number of attempts against the primary DC.
     * @param attempt This indicates the total number of attempts to send the request.
     * @param suppressed The list of throwables that has been suppressed.
     * @return A single containing either the successful response or an error that was not retryable because either the
     * {@code maxTries} was exceeded or retries will not mitigate the issue.
     */
    private Mono<HttpResponse> attemptAsync(HttpPipelineCallContext context, HttpPipelineNextPolicy next,
        HttpRequest originalRequest, boolean considerSecondary, int primaryTry, int attempt,
        List<Throwable> suppressed) {
        // Determine which endpoint to try. It's primary if there is no secondary or if it is an odd number attempt.
        final boolean tryingPrimary = !considerSecondary || (attempt % 2 != 0);

        // Select the correct host and delay.
        long delayMs = getDelayMs(primaryTry, tryingPrimary);

        context.setHttpRequest(originalRequest.copy());

        try {
            updateUrlToSecondaryHost(tryingPrimary, this.requestRetryOptions.getSecondaryHost(), context);
        } catch (IllegalArgumentException e) {
            return Mono.error(e);
        }

        updateRetryCountContext(context, attempt);
        resetProgress(context);

        // We want to send the request with a given timeout, but we don't want to kick off that timeout-bound operation
        // until after the retry backoff delay, so we call delaySubscription.
        Mono<HttpResponse> responseMono = next.clone().process();

        // Default try timeout is Integer.MAX_VALUE seconds, if it's that don't set a timeout as that's about 68 years
        // and would likely never complete.
        // TODO (alzimmer): Think about not adding this if it's over a certain length, like 1 year.
        if (this.requestRetryOptions.getTryTimeoutDuration().getSeconds() != Integer.MAX_VALUE) {
            responseMono = responseMono.timeout(this.requestRetryOptions.getTryTimeoutDuration());
        }

        // Only add delaySubscription if there is going to be a delay.
        if (delayMs > 0) {
            responseMono = responseMono.delaySubscription(Duration.ofMillis(delayMs));
        }

        return responseMono.flatMap(response -> {
            boolean newConsiderSecondary = considerSecondary;
            int statusCode = response.getStatusCode();

            //boolean retry = shouldResponseBeRetried(statusCode, tryingPrimary, response);
            boolean retry = shouldStatusCodeBeRetried(statusCode, tryingPrimary);
            if (!tryingPrimary && statusCode == 404) {
                newConsiderSecondary = false;
            }

            if (retry && attempt < requestRetryOptions.getMaxTries()) {
                /*
                 * We increment primaryTry if we are about to try the primary again (which is when we consider the
                 * secondary and tried the secondary this time (tryingPrimary==false) or we do not consider the
                 * secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when passed to
                 * calculate the delay.
                 */
                int newPrimaryTry = getNewPrimaryTry(considerSecondary, primaryTry, tryingPrimary);

                Flux<ByteBuffer> responseBody = response.getBody();
                response.close();

                if (responseBody == null) {
                    return attemptAsync(context, next, originalRequest, newConsiderSecondary, newPrimaryTry,
                        attempt + 1, suppressed);
                } else {
                    return responseBody
                        .ignoreElements()
                        .then(attemptAsync(context, next, originalRequest, newConsiderSecondary, newPrimaryTry,
                            attempt + 1, suppressed));
                }

            }
            return Mono.just(response);
        }).onErrorResume(throwable -> {
            /*
             * It is likely that many users will not realize that their Flux must be replayable and get an error upon
             * retries when the provided data length does not match the length of the exact data. We cannot enforce the
             * desired Flux behavior, so we provide a hint when this is likely the root cause.
             */
            if (throwable instanceof IllegalStateException && attempt > 1) {
                return Mono.error(new IllegalStateException("The request failed because the size of the contents of "
                    + "the provided Flux did not match the provided data size upon attempting to retry. This is likely "
                    + "caused by the Flux not being replayable. To support retries, all Fluxes must produce the same "
                    + "data for each subscriber. Please ensure this behavior.", throwable));
            }

            /*
             * IOException is a catch-all for IO related errors. Technically it includes many types which may not be
             * network exceptions, but we should not hit those unless there is a bug in our logic. In either case, it is
             * better to optimistically retry instead of failing too soon. A Timeout Exception is a client-side timeout
             * coming from Rx.
             */
            ExceptionRetryStatus exceptionRetryStatus = shouldErrorBeRetried(throwable, attempt,
                requestRetryOptions.getMaxTries());

            if (exceptionRetryStatus.canBeRetried) {
                /*
                 * We increment primaryTry if we are about to try the primary again (which is when we consider the
                 * secondary and tried the secondary this time (tryingPrimary==false) or we do not consider the
                 * secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when passed to
                 * calculate the delay.
                 */
                int newPrimaryTry = getNewPrimaryTry(considerSecondary, primaryTry, tryingPrimary);
                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;
                suppressedLocal.add(exceptionRetryStatus.unwrappedThrowable);
                return attemptAsync(context, next, originalRequest, considerSecondary, newPrimaryTry, attempt + 1,
                    suppressedLocal);
            }

            if (suppressed != null) {
                suppressed.forEach(throwable::addSuppressed);
            }

            return Mono.error(throwable);
        });
    }

    private HttpResponse attemptSync(final HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next,
        final HttpRequest originalRequest, final boolean considerSecondary, final int primaryTry, final int attempt,
        final List<Throwable> suppressed) {
        // Determine which endpoint to try. It's primary if there is no secondary or if it is an odd number attempt.
        final boolean tryingPrimary = !considerSecondary || (attempt % 2 != 0);

        // Select the correct host and delay.
        long delayMs = getDelayMs(primaryTry, tryingPrimary);

        context.setHttpRequest(originalRequest.copy());

        updateUrlToSecondaryHost(tryingPrimary, this.requestRetryOptions.getSecondaryHost(), context);
        updateRetryCountContext(context, attempt);
        resetProgress(context);

        try {
            // Only add delaySubscription if there is going to be a delay.
            if (delayMs > 0) {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ie));
                }
            }

            /*
             * We want to send the request with a given timeout, but we don't want to kickoff that timeout-bound
             * operation until after the retry backoff delay, so we call delaySubscription.
             */
            Mono<HttpResponse> httpResponseMono = Mono.fromCallable(() -> next.clone().processSync());

            // Default try timeout is Integer.MAX_VALUE seconds, if it's that don't set a timeout as that's about 68 years
            // and would likely never complete.
            // TODO (alzimmer): Think about not adding this if it's over a certain length, like 1 year.
            if (this.requestRetryOptions.getTryTimeoutDuration().getSeconds() != Integer.MAX_VALUE) {
                httpResponseMono = httpResponseMono.timeout(this.requestRetryOptions.getTryTimeoutDuration());
            }

            HttpResponse response = httpResponseMono.block();

            boolean newConsiderSecondary = considerSecondary;
            int statusCode = response.getStatusCode();
            boolean retry = shouldStatusCodeBeRetried(statusCode, tryingPrimary);
            //boolean retry = shouldResponseBeRetried(statusCode, tryingPrimary, response);
            if (!tryingPrimary && statusCode == 404) {
                newConsiderSecondary = false;
            }

            if (retry && attempt < requestRetryOptions.getMaxTries()) {
                int newPrimaryTry = getNewPrimaryTry(considerSecondary, primaryTry, tryingPrimary);

                if (response.getBody() != null) {
                    response.getBodyAsBinaryData().toByteBuffer();
                }
                response.close();
                return attemptSync(context, next, originalRequest, newConsiderSecondary, newPrimaryTry,
                        attempt + 1, suppressed);

            }
            return response;
        } catch (RuntimeException throwable) {
            /*
             * It is likely that many users will not realize that their Flux must be replayable and get an error upon
             * retries when the provided data length does not match the length of the exact data. We cannot enforce the
             * desired Flux behavior, so we provide a hint when this is likely the root cause.
             */
            if (throwable instanceof IllegalStateException && attempt > 1) {
                throw LOGGER.logExceptionAsError((new IllegalStateException("The request failed because the size of the contents of "
                    + "the provided data did not match the provided data size upon attempting to retry. This is likely "
                    + "caused by the data not being replayable. To support retries, all Fluxes must produce the same "
                    + "data for each subscriber. Please ensure this behavior.", throwable)));
            }

            ExceptionRetryStatus exceptionRetryStatus = shouldErrorBeRetried(throwable, attempt,
                requestRetryOptions.getMaxTries());

            if (exceptionRetryStatus.canBeRetried) {
                /*
                 * We increment primaryTry if we are about to try the primary again (which is when we consider the
                 * secondary and tried the secondary this time (tryingPrimary==false) or we do not consider the
                 * secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when passed to
                 * calculate the delay.
                 */
                int newPrimaryTry = getNewPrimaryTry(considerSecondary, primaryTry, tryingPrimary);
                List<Throwable> suppressedLocal = suppressed == null ? new LinkedList<>() : suppressed;
                suppressedLocal.add(exceptionRetryStatus.unwrappedThrowable);
                return attemptSync(context, next, originalRequest, considerSecondary, newPrimaryTry, attempt + 1,
                    suppressedLocal);
            }

            if (suppressed != null) {
                suppressed.forEach(throwable::addSuppressed);
            }
            throw LOGGER.logExceptionAsError(throwable);
        }
    }

    /*
     * Update the RETRY_COUNT_CONTEXT to log retries.
     */
    private static void updateRetryCountContext(HttpPipelineCallContext context, int attempt) {
        context.setData(HttpLoggingPolicy.RETRY_COUNT_CONTEXT, attempt);
    }

    // Reset progress if progress is tracked.
    private static void resetProgress(HttpPipelineCallContext context) {
        ProgressReporter progressReporter = Contexts.with(context.getContext()).getHttpRequestProgressReporter();
        if (progressReporter != null) {
            progressReporter.reset();
        }
    }

    /*
     * Update secondary host on request URL if not trying primary URL.
     */
    private static void updateUrlToSecondaryHost(boolean tryingPrimary, String secondaryHost, HttpPipelineCallContext context) {
        if (!tryingPrimary) {
            UrlBuilder builder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            builder.setHost(secondaryHost);
            try {
                context.getHttpRequest().setUrl(builder.toUrl());
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsWarning(new IllegalArgumentException("'url' must be a valid URL", e));
            }
        }
    }

    static ExceptionRetryStatus shouldErrorBeRetried(Throwable error, int attempt, int maxAttempts) {
        Throwable unwrappedThrowable = Exceptions.unwrap(error);

        // Check if there are any attempts remaining.
        if (attempt >= maxAttempts) {
            return new ExceptionRetryStatus(false, unwrappedThrowable);
        }

        // Check if the unwrapped error is an IOException or TimeoutException.
        if (unwrappedThrowable instanceof IOException || unwrappedThrowable instanceof TimeoutException) {
            return new ExceptionRetryStatus(true, unwrappedThrowable);
        }

        // Check the causal exception chain for this exception being caused by an IOException or TimeoutException.
        Throwable causalException = unwrappedThrowable.getCause();
        while (causalException != null) {
            if (causalException instanceof IOException || causalException instanceof TimeoutException) {
                return new ExceptionRetryStatus(true, unwrappedThrowable);
            }

            causalException = causalException.getCause();
        }

        // Finally all exceptions have been checked and none can be retried.
        return new ExceptionRetryStatus(false, unwrappedThrowable);
    }


    //static boolean shouldResponseBeRetried(int statusCode, boolean isPrimary, HttpResponse response) {
        /*
         * Retry the request if the server had an error (500), was unavailable (503), or requested a backoff (429),
         * or if the secondary was being tried and the resources didn't exist there (404). Only the secondary can retry
         * if the resource wasn't found as there may be a delay in replication from the primary.
         */
        //boolean headerRetry = false;
        //boolean statusCodeRetry = (statusCode == 429 || statusCode == 500 || statusCode == 503) || (!isPrimary && statusCode == 404);
        //if (response != null && response.getHeaders() != null) {
            //String headerValue = response.getHeaders().getValue(X_MS_COPY_SOURCE_ERROR_CODE);
            //if (headerValue != null) {
                //headerRetry = ("429".equals(headerValue) || "500".equals(headerValue) || "503".equals(headerValue))
                    //|| (!isPrimary && "404".equals(headerValue));
            //}

        //}
        //return statusCodeRetry || headerRetry;
    //}

    static boolean shouldStatusCodeBeRetried(int statusCode, boolean isPrimary) {
        return (statusCode == 429 || statusCode == 500 || statusCode == 503)
            || (!isPrimary && statusCode == 404);
    }


    static final class ExceptionRetryStatus {
        final boolean canBeRetried;
        final Throwable unwrappedThrowable;

        ExceptionRetryStatus(boolean canBeRetried, Throwable unwrappedThrowable) {
            this.canBeRetried = canBeRetried;
            this.unwrappedThrowable = unwrappedThrowable;
        }
    }

    private long getDelayMs(int primaryTry, boolean tryingPrimary) {
        long delayMs;
        if (tryingPrimary) {
            // The first attempt returns 0 delay.
            delayMs = this.requestRetryOptions.calculateDelayInMs(primaryTry);
        } else {
            // Delay with some jitter before trying the secondary.
            delayMs = (long) ((ThreadLocalRandom.current().nextFloat() / 2 + 0.8) * 1000); // Add jitter
        }
        return delayMs;
    }

    private static int getNewPrimaryTry(boolean considerSecondary, int primaryTry, boolean tryingPrimary) {
        /*
         * We increment primaryTry if we are about to try the primary again (which is when we consider the
         * secondary and tried the secondary this time (tryingPrimary==false) or we do not consider the
         * secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when passed to
         * calculate the delay.
         */
        return (!tryingPrimary || !considerSecondary) ? primaryTry + 1 : primaryTry;
    }
}
