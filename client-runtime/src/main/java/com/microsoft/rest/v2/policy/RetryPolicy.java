/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;

import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;
import rx.functions.Func1;

import static java.net.HttpURLConnection.HTTP_CLIENT_TIMEOUT;
import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_NOT_IMPLEMENTED;
import static java.net.HttpURLConnection.HTTP_VERSION;

/**
 * An instance of this interceptor placed in the request pipeline handles retriable errors.
 */
public final class RetryPolicy implements RequestPolicy {
    /**
     * Factory which instantiates RetryPolicy.
     */
    public static class Factory implements RequestPolicy.Factory {
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
        public RequestPolicy create(RequestPolicy next) {
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
                && (code == HTTP_CLIENT_TIMEOUT
                    || (code >= HTTP_INTERNAL_ERROR
                        && code != HTTP_NOT_IMPLEMENTED
                        && code != HTTP_VERSION));
    }

    @Override
    public Single<HttpResponse> sendAsync(final HttpRequest request) {
        Single<? extends HttpResponse> asyncResponse = next.sendAsync(request);
        return asyncResponse.flatMap(new Func1<HttpResponse, Single<? extends HttpResponse>>() {
            @Override
            public Single<? extends HttpResponse> call(HttpResponse httpResponse) {
                if (shouldRetry(httpResponse)) {
                    tryCount++;
                    return sendAsync(request);
                } else {
                    return Single.just(httpResponse);
                }
            }
        });
    }
}
