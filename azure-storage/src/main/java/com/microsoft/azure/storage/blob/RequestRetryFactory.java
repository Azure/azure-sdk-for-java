/*
 * Copyright Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.storage.blob;

import com.microsoft.rest.v2.http.*;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * This is a factory which creates policies in an {@link HttpPipeline} for retrying a given HTTP request. The request
 * that is retried will be identical each time it is reissued. In most cases, it is sufficient to configure a
 * {@link RequestRetryOptions} object and set those as a field on a {@link PipelineOptions} object to configure a
 * default pipeline. Retries will try against a secondary if one is specified and the type of operation/error indicates
 * that the secondary can handle the request. Exponential and fixed backoff are supported. The factory and policy must
 * only be used directly when creating a custom pipeline.
 */
//TODO: This was retrying when I got the xml bom issue. Investigate that
public final class RequestRetryFactory implements RequestPolicyFactory {

    private final RequestRetryOptions requestRetryOptions;

    /**
     * Creates a factory capable of generating RequestRetry policies for the {@link HttpPipeline}.
     *
     * @param requestRetryOptions
     *      {@link RequestRetryOptions}
     */
    public RequestRetryFactory(RequestRetryOptions requestRetryOptions) {
        this.requestRetryOptions = requestRetryOptions == null ? RequestRetryOptions.DEFAULT : requestRetryOptions;
    }

    private final class RequestRetryPolicy implements RequestPolicy {

        private final RequestPolicy nextPolicy;

        private final RequestRetryOptions requestRetryOptions;

        // TODO: It looked like there was some stuff in here to log how long the operation took. Do we want that?

        private RequestRetryPolicy(RequestPolicy nextPolicy, RequestRetryOptions requestRetryOptions) {
            this.nextPolicy = nextPolicy;
            this.requestRetryOptions = requestRetryOptions;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest httpRequest) {
            boolean considerSecondary = (httpRequest.httpMethod().equals(HttpMethod.GET) ||
                    httpRequest.httpMethod().equals(HttpMethod.HEAD))
                    && (this.requestRetryOptions.getSecondaryHost() != null);

            return this.attemptAsync(httpRequest, 1, considerSecondary, 1);
        }

        // This is to log for debugging purposes only. Comment/uncomment as necessary for releasing/debugging.
        private void logf(String s, Object... args) {
            //System.out.println(String.format(s, args));
        }

        /**
         * This method actually attempts to send the request and determines if we should attempt again and, if so, how
         * long to wait before sending out the next request.
         *
         * Exponential retry algorithm: ((2 ^ attempt) - 1) * delay * random(0.8, 1.2)
         * When to retry: connection failure or an HTTP status code of 500 or greater, except 501 and 505
         * If using a secondary:
         *    Odd tries go against primary; even tries go against the secondary
         *    For a primary wait ((2 ^ primaryTries - 1) * delay * random(0.8, 1.2)
         *    If secondary gets a 404, don't fail, retry but future retries are only against the primary
         *    When retrying against a secondary, ignore the retry count and wait (.1 second * random(0.8, 1.2))
         *
         * @param httpRequest
         *      The request to try.
         * @param primaryTry
         *      This indicates how man tries we've attempted against the primary DC.
         * @param considerSecondary
         *      Before each try, we'll select either the primary or secondary URL if appropriate.
         * @param attempt
         *      This indicates the total number of attempts to send the request.
         * @return
         *      A single containing either the successful response or an error that was not retryable because either
         *      the maxTries was exceeded or retries will not mitigate the issue.
         */
        private Single<HttpResponse> attemptAsync(final HttpRequest httpRequest, final int primaryTry,
                                                  final boolean considerSecondary,
                                                  final int attempt) {
            logf("\n=====> Try=%d\n", attempt);

            // Determine which endpoint to try. It's primary if there is no secondary or if it is an odd number attempt.
            final boolean tryingPrimary = !considerSecondary || (attempt%2 == 1);

            // Select the correct host and delay.
            long delayMs;
            if(tryingPrimary) {
                // The first attempt returns 0 delay.
                delayMs = this.requestRetryOptions.calculatedDelayInMs(primaryTry);
                logf("Primary try=%d, Delay=%d\n", primaryTry, delayMs);
            }
            else {
                // Delay with some jitter before trying the secondary.
                delayMs = (long)((ThreadLocalRandom.current().nextFloat()/2+0.8) * 1000); // Add jitter
                logf("Secondary try=%d, Delay=%d\n", attempt-primaryTry, delayMs);
            }

            // Clone the original request to ensure that each try starts with the original (unmutated) request.
            // buffer() will also reset to the beginning of the stream.
            final HttpRequest requestCopy = httpRequest.buffer();
            if(!tryingPrimary) {
                UrlBuilder builder = UrlBuilder.parse(requestCopy.url());
                builder.withHost(this.requestRetryOptions.getSecondaryHost());
                try {
                    requestCopy.withUrl(builder.toURL());
                } catch (MalformedURLException e) {
                    return Single.error(e);
                }
            }

            // Deadline stuff

            // Delay before the calculated time, then call the next policy to send out the request (again) with
            // the specified timeout.
            return Completable.complete().delay(delayMs, TimeUnit.MILLISECONDS)
                    .andThen(this.nextPolicy.sendAsync(requestCopy)
                    .timeout(this.requestRetryOptions.getTryTimeout(), TimeUnit.SECONDS)
                    .flatMap(new Function<HttpResponse, Single<? extends HttpResponse>>() {
                @Override
                public Single<? extends HttpResponse> apply(HttpResponse httpResponse) throws Exception {
                    boolean newConsiderSecondary = considerSecondary;
                    String action;

                    // If attempt was against the secondary & it returned a StatusNotFound (404), then
                    // the resource was not found. This may be due to replication delay. So, in this
                    // case, we'll never try the secondary again for this operation.
                    if(!tryingPrimary && httpResponse.statusCode() == 404) {
                        newConsiderSecondary = false;
                        action = "Retry: Secondary URL returned 404";
                    }
                    else if(httpResponse.statusCode() == 503 || httpResponse.statusCode() == 500) {
                        action = "Retry: Temporary error or timeout";
                    }
                    else {
                        action = "NoRetry: Successful HTTP request";
                    }

                    logf("Action=%s\n", action);

                    if(action.charAt(0)=='R' && attempt < requestRetryOptions.getMaxTries()) {
                        // We increment primaryTry if we are about to try the primary again (which is when we consider
                        // the secondary and tried the secondary this time (tryingPrimary==false) or we do not consider
                        // the secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when
                        // passed to calculate the delay.
                        int newPrimaryTry = !tryingPrimary || !considerSecondary ? primaryTry+1 : primaryTry;
                        return attemptAsync(httpRequest, newPrimaryTry, newConsiderSecondary, attempt+1);
                    }
                    return Single.just(httpResponse);
                }
            }).onErrorResumeNext(new Function<Throwable, SingleSource<? extends HttpResponse>>() {
                @Override
                public SingleSource<? extends HttpResponse> apply(Throwable throwable) throws Exception {
                    if (throwable instanceof IOException && attempt < requestRetryOptions.getMaxTries()) {
                        // We increment primaryTry if we are about to try the primary again (which is when we consider
                        // the secondary and tried the secondary this time (tryingPrimary==false) or we do not consider
                        // the secondary at all (considerSecondary==false)). This will ensure primaryTry is correct when
                        // passed to calculate the delay.
                        int newPrimaryTry = !tryingPrimary || !considerSecondary ? primaryTry+1 : primaryTry;
                        return attemptAsync(httpRequest, newPrimaryTry, considerSecondary, attempt+1);
                    }
                    return Single.error(throwable);
                }
            }));
        }
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RequestRetryPolicy(next, this.requestRetryOptions);
    }
}
