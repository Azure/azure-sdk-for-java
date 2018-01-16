/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import io.reactivex.Single;
import io.reactivex.functions.Function;

import java.net.HttpURLConnection;

/**
 * Creates a RequestPolicy which retries when a recoverable HTTP error occurs.
 */
public class RetryPolicyFactory implements RequestPolicyFactory {
    private static final int DEFAULT_MAX_RETRIES = 3;
    private final int maxRetries;

    /**
     * Creates a Factory with the default number of retry attempts.
     */
    public RetryPolicyFactory() {
        maxRetries = DEFAULT_MAX_RETRIES;
    }

    /**
     * Creates a Factory.
     * @param maxRetries The maximum number of retries to attempt.
     */
    public RetryPolicyFactory(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RetryPolicy(next);
    }

    private final class RetryPolicy implements RequestPolicy {
        private final RequestPolicy next;
        private RetryPolicy(RequestPolicy next) {
            this.next = next;
        }

        @Override
        public Single<HttpResponse> sendAsync(final HttpRequest request) {
            return attemptAsync(request, 0);
        }

        private Single<HttpResponse> attemptAsync(final HttpRequest request, final int tryCount) {
            Single<HttpResponse> result = next.sendAsync(request.buffer())
                    .flatMap(new Function<HttpResponse, Single<? extends HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> apply(HttpResponse httpResponse) throws Exception {
                            Single<HttpResponse> result;
                            if (shouldRetry(httpResponse, tryCount)) {
                                result = attemptAsync(request, tryCount + 1);
                            } else {
                                result = Single.just(httpResponse);
                            }
                            return result;
                        }
                    });
            return result;
        }

        private boolean shouldRetry(HttpResponse response, int tryCount) {
            int code = response.statusCode();
            return tryCount < maxRetries
                    && (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                    || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                    && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                    && code != HttpURLConnection.HTTP_VERSION));
        }
    }
}