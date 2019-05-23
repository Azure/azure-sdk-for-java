// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpMethod;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UnexpectedLengthException;
import com.microsoft.rest.v2.http.UrlBuilder;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for retrying a given HTTP request. The request
 * that is retried will be identical each time it is reissued. In most cases, it is sufficient to configure a {@link
 * RequestRetryOptions} object and set those as a field on a {@link PipelineOptions} object to configure a default
 * pipeline. Retries will try against a secondary if one is specified and the type of operation/error indicates that the
 * secondary can handle the request. Exponential and fixed backoff are supported. The factory and policy must only be
 * used directly when creating a custom pipeline.
 */
public final class RequestRetryFactory implements RequestPolicyFactory {

    private final RequestRetryOptions requestRetryOptions;

    /**
     * Creates a factory capable of generating RequestRetry policies for the {@link HttpPipeline}.
     *
     * @param requestRetryOptions
     *         {@link RequestRetryOptions}
     */
    public RequestRetryFactory(RequestRetryOptions requestRetryOptions) {
        this.requestRetryOptions = requestRetryOptions == null ? new RequestRetryOptions() : requestRetryOptions;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RequestRetryPolicy(next, this.requestRetryOptions);
    }

    private static final class RequestRetryPolicy implements RequestPolicy {

        private final RequestPolicy nextPolicy;

        private final RequestRetryOptions requestRetryOptions;

        private RequestRetryPolicy(RequestPolicy nextPolicy, RequestRetryOptions requestRetryOptions) {
            this.nextPolicy = nextPolicy;
            this.requestRetryOptions = requestRetryOptions;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest httpRequest) {
            boolean considerSecondary = (httpRequest.httpMethod().equals(HttpMethod.GET)
                    || httpRequest.httpMethod().equals(HttpMethod.HEAD))
                    && (this.requestRetryOptions.secondaryHost() != null);

            return this.attemptAsync(httpRequest, 1, considerSecondary, 1);
        }

        // This is to log for debugging purposes only. Comment/uncomment as necessary for releasing/debugging.
        private void logf(String s, Object... args) {
            //System.out.println(String.format(s, args));
        }

        /**
         * This method actually attempts to send the request and determines if we should attempt again and, if so, how
         * long to wait before sending out the next request.
         * <p>
         * Exponential retry algorithm: ((2 ^ attempt) - 1) * delay * random(0.8, 1.2) When to retry: connection failure
         * or an HTTP status code of 500 or greater, except 501 and 505 If using a secondary: Odd tries go against
         * primary; even tries go against the secondary For a primary wait ((2 ^ primaryTries - 1) * delay * random(0.8,
         * 1.2) If secondary gets a 404, don't fail, retry but future retries are only against the primary When retrying
         * against a secondary, ignore the retry count and wait (.1 second * random(0.8, 1.2))
         *
         * @param httpRequest
         *         The request to try.
         * @param primaryTry
         *         This indicates how man tries we've attempted against the primary DC.
         * @param considerSecondary
         *         Before each try, we'll select either the primary or secondary URL if appropriate.
         * @param attempt
         *         This indicates the total number of attempts to send the request.
         *
         * @return A single containing either the successful response or an error that was not retryable because either
         * the maxTries was exceeded or retries will not mitigate the issue.
         */
        private Single<HttpResponse> attemptAsync(final HttpRequest httpRequest, final int primaryTry,
                final boolean considerSecondary,
                final int attempt) {
            logf("\n=====> Try=%d\n", attempt);

            // Determine which endpoint to try. It's primary if there is no secondary or if it is an odd number attempt.
            final boolean tryingPrimary = !considerSecondary || (attempt % 2 != 0);

            // Select the correct host and delay.
            long delayMs;
            if (tryingPrimary) {
                // The first attempt returns 0 delay.
                delayMs = this.requestRetryOptions.calculateDelayInMs(primaryTry);
                logf("Primary try=%d, Delay=%d\n", primaryTry, delayMs);
            } else {
                // Delay with some jitter before trying the secondary.
                delayMs = (long) ((ThreadLocalRandom.current().nextFloat() / 2 + 0.8) * 1000); // Add jitter
                logf("Secondary try=%d, Delay=%d\n", attempt - primaryTry, delayMs);
            }

            /*
             Clone the original request to ensure that each try starts with the original (unmutated) request. We cannot
             simply call httpRequest.buffer() because although the body will start emitting from the beginning of the
             stream, the buffers that were emitted will have already been consumed (their position set to their limit),
             so it is not a true reset. By adding the map function, we ensure that anything which consumes the
             ByteBuffers downstream will only actually consume a duplicate so the original is preserved. This only
             duplicates the ByteBuffer object, not the underlying data.
             */
            HttpHeaders bufferedHeaders = new HttpHeaders(httpRequest.headers());
            Flowable<ByteBuffer> bufferedBody = httpRequest.body() == null
                    ? null : httpRequest.body().map(ByteBuffer::duplicate);
            final HttpRequest requestCopy = new HttpRequest(httpRequest.callerMethod(), httpRequest.httpMethod(),
                    httpRequest.url(), bufferedHeaders, bufferedBody, httpRequest.responseDecoder());
            if (!tryingPrimary) {
                UrlBuilder builder = UrlBuilder.parse(requestCopy.url());
                builder.withHost(this.requestRetryOptions.secondaryHost());
                try {
                    requestCopy.withUrl(builder.toURL());
                } catch (MalformedURLException e) {
                    return Single.error(e);
                }
            }
            requestCopy.withContext(httpRequest.context());

            // Deadline stuff

            /*
             We want to send the request with a given timeout, but we don't want to kickoff that timeout-bound operation
             until after the retry backoff delay, so we call delaySubscription.
             */
            return this.nextPolicy.sendAsync(requestCopy)
                    .timeout(this.requestRetryOptions.tryTimeout(), TimeUnit.SECONDS)
                    .delaySubscription(delayMs, TimeUnit.MILLISECONDS)
                    .flatMap(response -> {
                        boolean newConsiderSecondary = considerSecondary;
                        String action;
                        int statusCode = response.statusCode();

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

                        logf("Action=%s\n", action);
                        if (action.charAt(0) == 'R' && attempt < requestRetryOptions.maxTries()) {
                            /*
                            We increment primaryTry if we are about to try the primary again (which is when we
                            consider the secondary and tried the secondary this time (tryingPrimary==false) or
                            we do not consider the secondary at all (considerSecondary==false)). This will
                            ensure primaryTry is correct when passed to calculate the delay.
                             */
                            int newPrimaryTry = !tryingPrimary || !considerSecondary
                                    ? primaryTry + 1 : primaryTry;
                            return attemptAsync(httpRequest, newPrimaryTry, newConsiderSecondary,
                                    attempt + 1);
                        }
                        return Single.just(response);
                    })
                    .onErrorResumeNext(throwable -> {
                        /*
                        It is likely that many users will not realize that their Flowable must be replayable and
                        get an error upon retries when the provided data length does not match the length of the exact
                        data. We cannot enforce the desired Flowable behavior, so we provide a hint when this is likely
                        the root cause.
                         */
                        if (throwable instanceof UnexpectedLengthException && attempt > 1) {
                            return Single.error(new IllegalStateException("The request failed because the "
                                    + "size of the contents of the provided Flowable did not match the provided "
                                    + "data size upon attempting to retry. This is likely caused by the Flowable "
                                    + "not being replayable. To support retries, all Flowables must produce the "
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



                        logf("Action=%s\n", action);
                        if (action.charAt(0) == 'R' && attempt < requestRetryOptions.maxTries()) {
                            /*
                            We increment primaryTry if we are about to try the primary again (which is when we
                            consider the secondary and tried the secondary this time (tryingPrimary==false) or
                            we do not consider the secondary at all (considerSecondary==false)). This will
                            ensure primaryTry is correct when passed to calculate the delay.
                             */
                            int newPrimaryTry = !tryingPrimary || !considerSecondary
                                    ? primaryTry + 1 : primaryTry;
                            return attemptAsync(httpRequest, newPrimaryTry, considerSecondary,
                                    attempt + 1);
                        }
                        return Single.error(throwable);
                    });
        }
    }
}
