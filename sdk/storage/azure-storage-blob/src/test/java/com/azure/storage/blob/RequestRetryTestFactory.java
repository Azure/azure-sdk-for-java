// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RequestRetryPolicy;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static java.lang.StrictMath.pow;

class RequestRetryTestFactory {
    static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS = 1;

    static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES = 2;

    static final int RETRY_TEST_SCENARIO_NON_RETRYABLE = 3;

    static final int RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY = 4;

    static final int RETRY_TEST_SCENARIO_NETWORK_ERROR = 5;

    static final int RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING = 6;

    static final int RETRY_TEST_SCENARIO_FIXED_TIMING = 7;

    static final int RETRY_TEST_SCENARIO_TRY_TIMEOUT = 8;

    static final int RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE = 9;

    // Cancelable

    static final String RETRY_TEST_PRIMARY_HOST = "PrimaryDC";

    static final String RETRY_TEST_SECONDARY_HOST = "SecondaryDC";
    static final ByteBuf RETRY_TEST_DEFAULT_DATA = Unpooled.wrappedBuffer("Default data".getBytes());
    private static final String RETRY_TEST_HEADER = "TestHeader";
    private static final String RETRY_TEST_QUERY_PARAM = "TestQueryParam";
    private static final Mono<HttpResponse> RETRY_TEST_OK_RESPONSE = Mono.just(new RetryTestResponse(200));

    /*
    We wrap the response in a StorageErrorException to mock the HttpClient. Any responses that the HttpClient receives
    that is not an expected response is wrapped in a StorageErrorException.
     */
    private static final Mono<HttpResponse> RETRY_TEST_TEMPORARY_ERROR_RESPONSE = Mono.just(new RetryTestResponse(503));

    private static final Mono<HttpResponse> RETRY_TEST_TIMEOUT_ERROR_RESPONSE = Mono.just(new RetryTestResponse(500));

    private static final Mono<HttpResponse> RETRY_TEST_NON_RETRYABLE_ERROR = Mono.just(new RetryTestResponse(400));

    private static final Mono<HttpResponse> RETRY_TEST_NOT_FOUND_RESPONSE = Mono.just(new RetryTestResponse(404));

    private int retryTestScenario;

    private RequestRetryOptions options;

    /*
    It is atypical and not recommended to have mutable state on the factory itself. However, the tests will need to
    be able to validate the number of tries, and the tests will not have access to the policies, so we break our own
    rule here.
     */
    private int tryNumber;

    private OffsetDateTime time;

    RequestRetryTestFactory(int scenario, RequestRetryOptions options) {
        this.retryTestScenario = scenario;
        this.options = options;
    }

    Mono<HttpResponse> send(URL url) {
        return new HttpPipelineBuilder()
            .policies(new RequestRetryPolicy(this.options))
            .httpClient(new RetryTestClient(this))
            .build()
            .send(new HttpRequest(HttpMethod.GET, url).body(Flux.just(RETRY_TEST_DEFAULT_DATA)));
    }

    int getTryNumber() {
        return this.tryNumber;
    }

    // The retry factory only really cares about the status code.
    private static final class RetryTestResponse extends HttpResponse {
        int statusCode;

        RetryTestResponse(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public int statusCode() {
            return this.statusCode;
        }

        @Override
        public String headerValue(String headerName) {
            return null;
        }

        @Override
        public HttpHeaders headers() {
            return null;
        }

        @Override
        public Flux<ByteBuf> body() {
            return null;
        }

        @Override
        public Mono<byte[]> bodyAsByteArray() {
            return null;
        }

        @Override
        public Mono<String> bodyAsString() {
            return null;
        }

        @Override
        public Mono<String> bodyAsString(Charset charset) {
            return null;
        }
    }

    private final class RetryTestClient implements HttpClient {
        private RequestRetryTestFactory factory;

        RetryTestClient(RequestRetryTestFactory parent) {
            this.factory = parent;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            this.factory.tryNumber++;
            if (this.factory.tryNumber > this.factory.options.maxTries()) {
                throw new IllegalArgumentException("Try number has exceeded max tries");
            }

            // Validate the expected preconditions for each try: The correct host is used.
            String expectedHost = RETRY_TEST_PRIMARY_HOST;
            if (this.factory.tryNumber % 2 == 0) {
                /*
                 Special cases: retry until success scenario fail's on the 4th try with a 404 on the secondary, so we
                 never expect it to check the secondary after that. All other tests should continue to check the
                 secondary.
                 Exponential timing only tests secondary backoff once but uses the rest of the retries to hit the max
                 delay.
                 */
                if (!((this.factory.retryTestScenario == RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS && this.factory.tryNumber > 4)
                    || (this.factory.retryTestScenario == RequestRetryTestFactory.RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING && this.factory.tryNumber > 2))) {
                    expectedHost = RETRY_TEST_SECONDARY_HOST;
                }
            }

            if (!request.url().getHost().equals(expectedHost)) {
                throw new IllegalArgumentException("The host does not match the expected host");
            }

            /*
             This policy will add test headers and query parameters. Ensure they are removed/reset for each retry.
             The retry policy should be starting with a fresh copy of the request for every try.
             */
            if (request.headers().value(RETRY_TEST_HEADER) != null) {
                throw new IllegalArgumentException("Headers not reset.");
            }
            if ((request.url().getQuery() != null && request.url().getQuery().contains(RETRY_TEST_QUERY_PARAM))) {
                throw new IllegalArgumentException("Query params not reset.");
            }

            // Subscribe and block until all information is read to prevent a blocking on another thread exception from Reactor.
            ByteBuf buf = Unpooled.buffer();
            Disposable disposable = request.body().subscribe(buf::writeBytes);
            while (!disposable.isDisposed()) {
                System.out.println("Waiting for Flux to finish to prevent blocking on another thread exception");
            }
            if (RETRY_TEST_DEFAULT_DATA.compareTo(buf) != 0) {
                throw new IllegalArgumentException(("Body not reset."));
            }

            /*
            Modify the request as policies downstream of the retry policy are likely to do. These must be reset on each
            try.
             */
            request.headers().put(RETRY_TEST_HEADER, "testheader");
            UrlBuilder builder = UrlBuilder.parse(request.url());
            builder.setQueryParameter(RETRY_TEST_QUERY_PARAM, "testquery");
            try {
                request.url(builder.toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("The URL has been mangled");
            }

            switch (this.factory.retryTestScenario) {
                case RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS:
                    switch (this.factory.tryNumber) {
                        case 1:
                            /*
                            The timer is set with a timeout on the Mono used to make the request. If the Mono
                            doesn't return success fast enough, it will throw a TimeoutException. We can short circuit
                            the waiting by simply returning an error. We will validate the time parameter later. Here,
                            we just test that a timeout is retried.
                             */
                            return Mono.error(new TimeoutException());
                        case 2:
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 3:
                            return RETRY_TEST_TIMEOUT_ERROR_RESPONSE;
                        case 4:
                            /*
                            By returning 404 when we should be testing against the secondary, we exercise the logic
                            that should prevent further tries to secondary when the secondary evidently doesn't have the
                            data.
                             */
                            return RETRY_TEST_NOT_FOUND_RESPONSE;
                        case 5:
                            // Just to get to a sixth try where we ensure we should not be trying the secondary again.
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 6:
                            return RETRY_TEST_OK_RESPONSE;
                        default:
                            throw new IllegalArgumentException("Continued trying after success.");
                    }

                case RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES:
                    return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;

                case RETRY_TEST_SCENARIO_NON_RETRYABLE:
                    if (this.factory.tryNumber == 1) {
                        return RETRY_TEST_NON_RETRYABLE_ERROR;
                    } else {
                        throw new IllegalArgumentException("Continued trying after non retryable error.");
                    }

                case RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY:
                    switch (this.factory.tryNumber) {
                        case 1:
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 2:
                            return RETRY_TEST_NON_RETRYABLE_ERROR;
                        default:
                            throw new IllegalArgumentException("Continued trying after non retryable error.");
                    }

                case RETRY_TEST_SCENARIO_NETWORK_ERROR:
                    switch (this.factory.tryNumber) {
                        case 1:
                            // fall through
                        case 2:
                            return Mono.error(new IOException());
                        case 3:
                            return RETRY_TEST_OK_RESPONSE;
                        default:
                            throw new IllegalArgumentException("Continued retrying after success.");
                    }

                case RETRY_TEST_SCENARIO_TRY_TIMEOUT:
                    switch (this.factory.tryNumber) {
                        case 1:
                        case 2:
                            return RETRY_TEST_OK_RESPONSE.delaySubscription(Duration.ofSeconds(options.tryTimeout() + 1));
                        case 3:
                            return RETRY_TEST_OK_RESPONSE.delaySubscription(Duration.ofSeconds(options.tryTimeout() - 1));
                        default:
                            throw new IllegalArgumentException("Continued retrying after success");
                    }

                case RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING:
                    switch (this.factory.tryNumber) {
                        case 1:
                            this.factory.time = OffsetDateTime.now();
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 2:
                            /*
                            Calculation for secondary is always the same, so we don't need to keep testing it. Not
                            trying the secondary any more will also speed up the test.
                             */
                            return testDelayBounds(1, false, RETRY_TEST_NOT_FOUND_RESPONSE);
                        case 3:
                            return testDelayBounds(2, true, RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 4:
                            return testDelayBounds(3, true, RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 5:
                            /*
                            With the current configuration in RetryTest, the maxRetryDelay should be reached upon the
                            fourth try to the primary.
                             */
                            return testMaxDelayBounds(RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 6:
                            return testMaxDelayBounds(RETRY_TEST_OK_RESPONSE);
                        default:
                            throw new IllegalArgumentException("Max retries exceeded/continued retrying after success");
                    }

                case RETRY_TEST_SCENARIO_FIXED_TIMING:
                    switch (this.factory.tryNumber) {
                        case 1:
                            this.factory.time = OffsetDateTime.now();
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 2:
                            return testDelayBounds(1, false, RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 3:
                            return testDelayBounds(2, true, RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 4:
                            /*
                            Fixed backoff means it's always the same and we never hit the max, no need to keep testing.
                             */
                            return RETRY_TEST_OK_RESPONSE;
                        default:
                            throw new IllegalArgumentException("Retries continued after success.");
                    }

                case RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE:
                    switch (this.factory.tryNumber) {
                        case 1:
                            return RETRY_TEST_TEMPORARY_ERROR_RESPONSE;
                        case 2:
                            return Mono.error(new UnexpectedLengthException("Unexpected length", 5, 6));
                        default:
                            throw new IllegalArgumentException("Retries continued on non retryable error.");
                    }
                default:
                    throw new IllegalArgumentException("Invalid retry test scenario.");
            }
        }

        @Override
        public HttpClient proxy(Supplier<ProxyOptions> supplier) {
            return null;
        }

        @Override
        public HttpClient wiretap(boolean b) {
            return null;
        }

        @Override
        public HttpClient port(int i) {
            return null;
        }

        /*
         Calculate the delay in seconds. Round up to ensure we include the maximum value and some offset for the code
         executing between the original calculation in the retry policy and this check.
         */
        private long calcPrimaryDelay(int tryNumber) {
            switch (this.factory.retryTestScenario) {
                case RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING:
                    return (long) Math.ceil(
                        ((pow(2L, tryNumber - 1) - 1L) * this.factory.options.retryDelayInMs()) / 1000);
                case RETRY_TEST_SCENARIO_FIXED_TIMING:
                    return (long) Math.ceil(this.factory.options.retryDelayInMs() / 1000);
                default:
                    throw new IllegalArgumentException("Invalid test scenario");
            }
        }

        private OffsetDateTime calcUpperBound(OffsetDateTime start, int primaryTryNumber, boolean tryingPrimary) {
            if (tryingPrimary) {
                return start.plus(calcPrimaryDelay(primaryTryNumber) * 1000 + 500, ChronoUnit.MILLIS);
            } else {
                return start.plus(1400, ChronoUnit.MILLIS);
            }
        }

        private OffsetDateTime calcLowerBound(OffsetDateTime start, int primaryTryNumber, boolean tryingPrimary) {
            if (tryingPrimary) {
                return start.plus(calcPrimaryDelay(primaryTryNumber) * 1000 - 500, ChronoUnit.MILLIS);
            } else {
                return start.plus(700, ChronoUnit.MILLIS);
            }
        }

        private Mono<HttpResponse> testDelayBounds(int primaryTryNumber, boolean tryingPrimary, Mono<HttpResponse> response) {
            /*
            We have to return a new Mono so that the calculation for time is performed at the correct time, i.e. when
            the Mono is actually subscribed to. This mocks an HttpClient because the requests are made only when
            the Mono is subscribed to, not when all the infrastructure around it is put in place, and we care about
            the delay before the request itself.
             */
            return Mono.defer(() -> Mono.fromCallable(() -> {
                OffsetDateTime now = OffsetDateTime.now();
                if (now.isAfter(calcUpperBound(factory.time, primaryTryNumber, tryingPrimary))
                    || now.isBefore(calcLowerBound(factory.time, primaryTryNumber, tryingPrimary))) {
                    throw new IllegalArgumentException("Delay was not within jitter bounds");
                }

                factory.time = now;
                return response.block();
            }));
        }

        private Mono<HttpResponse> testMaxDelayBounds(Mono<HttpResponse> response) {
            return Mono.defer(() -> Mono.fromCallable(() -> {
                OffsetDateTime now = OffsetDateTime.now();
                if (now.isAfter(factory.time.plusSeconds((long) Math.ceil((factory.options.maxRetryDelayInMs() / 1000) + 1)))) {
                    throw new IllegalArgumentException("Max retry delay exceeded");
                } else if (now.isBefore(factory.time.plusSeconds((long) Math.ceil((factory.options.maxRetryDelayInMs() / 1000) - 1)))) {
                    throw new IllegalArgumentException("Retry did not delay long enough");
                }

                factory.time = now;
                return response.block();
            }));
        }
    }
}
