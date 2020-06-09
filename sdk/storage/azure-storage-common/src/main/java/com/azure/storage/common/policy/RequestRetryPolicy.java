// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;


import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;

/**
 * This is a request policy in an {@link com.azure.core.http.HttpPipeline} for retrying a given HTTP request. The
 * request that is retried will be identical each time it is reissued.  Retries will try against a secondary if one is
 * specified and the type of operation/error indicates that the secondary can handle the request. Exponential and fixed
 * backoff are supported. The policy must only be used directly when creating a custom pipeline.
 */
public final class RequestRetryPolicy implements HttpPipelinePolicy {
    private final RequestRetryOptions requestRetryOptions;

    /**
     * Constructs the policy using the retry options.
     *
     * @param requestRetryOptions Retry options for the policy.
     */
    public RequestRetryPolicy(RequestRetryOptions requestRetryOptions) {
        this.requestRetryOptions = requestRetryOptions;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        boolean considerSecondary = (this.requestRetryOptions.getSecondaryHost() != null)
            && (HttpMethod.GET.equals(context.getHttpRequest().getHttpMethod())
            || HttpMethod.HEAD.equals(context.getHttpRequest().getHttpMethod()));

        return this.attemptAsync(context, next, context.getHttpRequest(), considerSecondary, 1, 1);
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
     * @return A single containing either the successful response or an error that was not retryable because either the
     * {@code maxTries} was exceeded or retries will not mitigate the issue.
     */
    private Mono<HttpResponse> attemptAsync(final HttpPipelineCallContext context, HttpPipelineNextPolicy next,
                                            final HttpRequest originalRequest, final boolean considerSecondary,
                                            final int primaryTry, final int attempt) {
        // Determine which endpoint to try. It's primary if there is no secondary or if it is an odd number attempt.
        final boolean tryingPrimary = !considerSecondary || (attempt % 2 != 0);

        // Select the correct host and delay.
        long delayMs;
        if (tryingPrimary) {
            // The first attempt returns 0 delay.
            delayMs = this.requestRetryOptions.calculateDelayInMs(primaryTry);
        } else {
            // Delay with some jitter before trying the secondary.
            delayMs = (long) ((ThreadLocalRandom.current().nextFloat() / 2 + 0.8) * 1000); // Add jitter
        }

        /*
         Clone the original request to ensure that each try starts with the original (unmutated) request. We cannot
         simply call httpRequest.buffer() because although the body will start emitting from the beginning of the
         stream, the buffers that were emitted will have already been consumed (their position set to their limit),
         so it is not a true reset. By adding the map function, we ensure that anything which consumes the
         ByteBuffers downstream will only actually consume a duplicate so the original is preserved. This only
         duplicates the ByteBuffer object, not the underlying data.
         */
        context.setHttpRequest(originalRequest.copy());
        Flux<ByteBuffer> bufferedBody = (context.getHttpRequest().getBody() == null)
            ? null
            : context.getHttpRequest().getBody().map(ByteBuffer::duplicate);
        context.getHttpRequest().setBody(bufferedBody);
        if (!tryingPrimary) {
            UrlBuilder builder = UrlBuilder.parse(context.getHttpRequest().getUrl());
            builder.setHost(this.requestRetryOptions.getSecondaryHost());
            try {
                context.getHttpRequest().setUrl(builder.toUrl());
            } catch (MalformedURLException e) {
                return Mono.error(e);
            }
        }

        /*
         We want to send the request with a given timeout, but we don't want to kickoff that timeout-bound operation
         until after the retry backoff delay, so we call delaySubscription.
         */
        return next.clone().process()
            .timeout(Duration.ofSeconds(this.requestRetryOptions.getTryTimeout()))
            .delaySubscription(Duration.ofMillis(delayMs))
            .flatMap(response -> {
                boolean newConsiderSecondary = considerSecondary;
                String action;
                int statusCode = response.getStatusCode();

                    /*
                    If attempt was against the secondary & it returned a StatusNotFound (404), then the
                    resource was not found. This may be due to replication delay. So, in this case,
                    we'll never try the secondary again for this operation.
                     */
                if (!tryingPrimary && statusCode == 404) {
                    newConsiderSecondary = false;
                    action = "Retry: Secondary URL returned 404";
                } else if (statusCode == 503 || statusCode == 500) {
                    action = "Retry: Temporary error or server timeout";
                } else {
                    action = "NoRetry: Successful HTTP request";
                }

                if (action.charAt(0) == 'R' && attempt < requestRetryOptions.getMaxTries()) {
                        /*
                        We increment primaryTry if we are about to try the primary again (which is when we
                        consider the secondary and tried the secondary this time (tryingPrimary==false) or
                        we do not consider the secondary at all (considerSecondary==false)). This will
                        ensure primaryTry is correct when passed to calculate the delay.
                         */
                    int newPrimaryTry = (!tryingPrimary || !considerSecondary) ? primaryTry + 1 : primaryTry;
                    return attemptAsync(context, next, originalRequest, newConsiderSecondary, newPrimaryTry,
                        attempt + 1);
                }
                return Mono.just(response);
            }).onErrorResume(throwable -> {
                    /*
                    It is likely that many users will not realize that their Flux must be replayable and
                    get an error upon retries when the provided data length does not match the length of the exact
                    data. We cannot enforce the desired Flux behavior, so we provide a hint when this is likely
                    the root cause.
                     */
                if (throwable instanceof IllegalStateException && attempt > 1) {
                    return Mono.error(new IllegalStateException("The request failed because the "
                        + "size of the contents of the provided Flux did not match the provided "
                        + "data size upon attempting to retry. This is likely caused by the Flux "
                        + "not being replayable. To support retries, all Fluxes must produce the "
                        + "same data for each subscriber. Please ensure this behavior.", throwable));
                }

                    /*
                    IOException is a catch-all for IO related errors. Technically it includes many types which may
                    not be network exceptions, but we should not hit those unless there is a bug in our logic. In
                    either case, it is better to optimistically retry instead of failing too soon.
                    A Timeout Exception is a client-side timeout coming from Rx.
                     */
                String action;
                if (throwable instanceof IOException) {
                    action = "Retry: Network error";
                } else if (throwable instanceof TimeoutException) {
                    action = "Retry: Client timeout";
                } else {
                    action = "NoRetry: Unknown error";
                }

                if (action.charAt(0) == 'R' && attempt < requestRetryOptions.getMaxTries()) {
                        /*
                        We increment primaryTry if we are about to try the primary again (which is when we
                        consider the secondary and tried the secondary this time (tryingPrimary==false) or
                        we do not consider the secondary at all (considerSecondary==false)). This will
                        ensure primaryTry is correct when passed to calculate the delay.
                         */
                    int newPrimaryTry = (!tryingPrimary || !considerSecondary) ? primaryTry + 1 : primaryTry;
                    return attemptAsync(context, next, originalRequest, considerSecondary, newPrimaryTry, attempt + 1);
                }
                return Mono.error(throwable);
            });
    }
}
