///**
// * Copyright (c) Microsoft Corporation. All rights reserved.
// * Licensed under the MIT License. See License.txt in the project root for
// * license information.
// */
//
//package com.azure.azconfig;
//
//import com.microsoft.rest.v3.http.HttpRequest;
//import com.microsoft.rest.v3.http.HttpResponse;
//import com.microsoft.rest.v3.policy.RequestPolicy;
//import com.microsoft.rest.v3.policy.RequestPolicyFactory;
//import com.microsoft.rest.v3.policy.RequestPolicyOptions;
//import reactor.core.publisher.Mono;
//
//import java.net.HttpURLConnection;
//import java.time.Duration;
//import java.time.temporal.ChronoUnit;
//
///**
// * Creates a RequestPolicy which retries when a recoverable HTTP error occurs.
// */
//public class RequestRetryPolicyFactory implements RequestPolicyFactory {
//    private static final int DEFAULT_MAX_RETRIES = 3;
//    private static final int DEFAULT_DELAY = 0;
//    private static final String RETRY_AFTER_HEADER = "retry-after-ms";
//    private static final int TOO_MANY_REQUESTS_CODE = 429;
//    private final int maxRetries;
//    private final long delayTime;
//
//    /**
//     * Creates a RetryPolicyFactory with the default number of retry attempts and delay between retries.
//     */
//    public RequestRetryPolicyFactory() {
//        maxRetries = DEFAULT_MAX_RETRIES;
//        delayTime = DEFAULT_DELAY;
//    }
//
//    /**
//     * Creates a RetryPolicyFactory.
//     * @param maxRetries The maximum number of retries to attempt.
//     * @param delayTimeInMs the delay between retries in milliseconds
//     */
//    public RequestRetryPolicyFactory(int maxRetries, long delayTimeInMs) {
//        this.maxRetries = maxRetries;
//        this.delayTime = delayTimeInMs;
//    }
//
//    @Override
//    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
//        return new RetryPolicy(next);
//    }
//
//    private final class RetryPolicy implements RequestPolicy {
//        private final RequestPolicy next;
//        private RetryPolicy(RequestPolicy next) {
//            this.next = next;
//        }
//
//        @Override
//        public Mono<HttpResponse> sendAsync(final HttpRequest request) {
//            return attemptAsync(request, 0);
//        }
//
//        private Mono<HttpResponse> attemptAsync(final HttpRequest request, final int tryCount) {
//            return next.sendAsync(request.buffer())
//                    .flatMap(httpResponse -> {
//                        if (shouldRetry(httpResponse, tryCount)) {
//                            long delayTimeInMs = calculateDelayInMs(httpResponse);
//                            return attemptAsync(request, tryCount + 1).delaySubscription(Duration.of(delayTimeInMs, ChronoUnit.MILLIS));
//                        } else {
//                            return Mono.just(httpResponse);
//                        }
//                    }).onErrorResume(err ->
//                            tryCount < maxRetries
//                                    ? attemptAsync(request, tryCount + 1).delaySubscription(Duration.of(delayTime, ChronoUnit.MILLIS))
//                                    : Mono.error(err));
//        }
//
//        private boolean shouldRetry(HttpResponse response, int tryCount) {
//            int code = response.statusCode();
//            return tryCount < maxRetries
//                    && (code == HttpURLConnection.HTTP_CLIENT_TIMEOUT
//                    || code == TOO_MANY_REQUESTS_CODE
//                    || (code >= HttpURLConnection.HTTP_INTERNAL_ERROR
//                    && code != HttpURLConnection.HTTP_NOT_IMPLEMENTED
//                    && code != HttpURLConnection.HTTP_VERSION));
//        }
//
//        private long calculateDelayInMs(HttpResponse response) {
//            if (response.statusCode() == TOO_MANY_REQUESTS_CODE) {
//                try {
//                    long delayInMs = Long.valueOf(response.headerValue(RETRY_AFTER_HEADER));
//                    if (delayInMs > 0) { // if not return delayTime
//                        return delayInMs;
//                    }
//                } catch (NumberFormatException ex) {
//                    //ignore and default to pre-set delayTime
//                }
//            }
//            return delayTime;
//        }
//    }
//}
