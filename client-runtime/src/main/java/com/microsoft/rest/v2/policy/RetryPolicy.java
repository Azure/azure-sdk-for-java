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

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * An instance of this interceptor placed in the request pipeline handles retriable errors.
 */
public final class RetryPolicy implements RequestPolicy {
    /**
     * Factory which instantiates RetryPolicy.
     */
    public static class Factory implements RequestPolicyFactory {
        private static final int DEFAULT_NUMBER_OF_ATTEMPTS = 3;
        final int maxRetries;

        /**
         * Creates a Factory with the default number of retry attempts.
         */
        public Factory() {
            maxRetries = DEFAULT_NUMBER_OF_ATTEMPTS;
        }

        /**
         * Creates a Factory.
         * @param maxRetries The maximum number of retries to attempt.
         */
        public Factory(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        @Override
        public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
            return new RetryPolicy(maxRetries, next);
        }
    }

    private final int maxRetries;
    private final RequestPolicy next;
    private int tryCount = 0;

    private RetryPolicy(int maxRetries, RequestPolicy next) {
        this.maxRetries = maxRetries;
        this.next = next;
    }

    private boolean shouldRetry(HttpResponse response) {
        int code = response.statusCode();
        return tryCount < maxRetries
                && (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
                    || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
                        && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
                        && code != HttpURLConnection.HTTP_VERSION));
    }

    @Override
    public Single<HttpResponse> sendAsync(final HttpRequest request) {
        Single<HttpResponse> result;
        try {
            final HttpRequest bufferedRequest = request.buffer();
            result = next.sendAsync(request)
                    .flatMap(new Function<HttpResponse, Single<? extends HttpResponse>>() {
                        @Override
                        public Single<HttpResponse> apply(HttpResponse httpResponse) {
                            Single<HttpResponse> result;
                            if (shouldRetry(httpResponse)) {
                                tryCount++;
                                try {
                                    result = sendAsync(bufferedRequest.buffer());
                                } catch (IOException e) {
                                    result = Single.error(e);
                                }
                            } else {
                                result = Single.just(httpResponse);
                            }
                            return result;
                        }
                    });
        } catch (IOException e) {
            result = Single.error(e);
        }
        return result;
    }
}
