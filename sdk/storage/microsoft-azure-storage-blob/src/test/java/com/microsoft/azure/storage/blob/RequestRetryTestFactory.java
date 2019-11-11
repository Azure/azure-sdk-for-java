// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.blob;

import com.microsoft.azure.storage.blob.models.StorageErrorException;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UnexpectedLengthException;
import com.microsoft.rest.v2.http.UrlBuilder;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.StrictMath.pow;

public class RequestRetryTestFactory implements RequestPolicyFactory {
    public static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS = 1;

    public static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES = 2;

    public static final int RETRY_TEST_SCENARIO_NON_RETRYABLE = 3;

    public static final int RETRY_TEST_SCENARIO_NON_RETRYABLE_SECONDARY = 4;

    public static final int RETRY_TEST_SCENARIO_NETWORK_ERROR = 5;

    public static final int RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING = 6;

    public static final int RETRY_TEST_SCENARIO_FIXED_TIMING = 7;

    public static final int RETRY_TEST_SCENARIO_TRY_TIMEOUT = 8;

    public static final int RETRY_TEST_SCENARIO_NON_REPLAYABLE_FLOWABLE = 9;

    // Cancelable

    public static final String RETRY_TEST_PRIMARY_HOST = "PrimaryDC";

    public static final String RETRY_TEST_SECONDARY_HOST = "SecondaryDC";
    public static final ByteBuffer RETRY_TEST_DEFAULT_DATA = ByteBuffer.wrap("Default data".getBytes());
    private static final String RETRY_TEST_HEADER = "TestHeader";
    private static final String RETRY_TEST_QUERY_PARAM = "TestQueryParam";
    private static final Single<HttpResponse> RETRY_TEST_OK_RESPONSE =
            Single.just(new RetryTestResponse(200));

    /*
    We wrap the response in a StorageErrorException to mock the HttpClient. Any responses that the HttpClient receives
    that is not an expected response is wrapped in a StorageErrorException.
     */
    private static final Single<HttpResponse> RETRY_TEST_TEMPORARY_ERROR_RESPONSE =
            Single.just(new RetryTestResponse(503));

    private static final Single<HttpResponse> RETRY_TEST_TIMEOUT_ERROR_RESPONSE =
            Single.just(new RetryTestResponse(500));

    private static final Single<HttpResponse> RETRY_TEST_NON_RETRYABLE_ERROR =
            Single.just(new RetryTestResponse(400));

    private static final Single<HttpResponse> RETRY_TEST_NOT_FOUND_RESPONSE =
            Single.just(new RetryTestResponse(404));

    private int retryTestScenario;

    private RequestRetryOptions options;

    /*
    It is atypical and not recommended to have mutable state on the factory itself. However, the tests will need to
    be able to validate the number of tries, and the tests will not have access to the policies, so we break our own
    rule here.
     */
    private int tryNumber;

    private OffsetDateTime time;

    public RequestRetryTestFactory(int scenario, RequestRetryOptions options) {
        this.retryTestScenario = scenario;
        this.options = options;
    }

    public int getTryNumber() {
        return this.tryNumber;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RetryTestPolicy(this);
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
        public Flowable<ByteBuffer> body() {
            return null;
        }

        @Override
        public Single<byte[]> bodyAsByteArray() {
            return null;
        }

        @Override
        public Single<String> bodyAsString() {
            return null;
        }
    }

    private final class RetryTestPolicy implements RequestPolicy {
        private RequestRetryTestFactory factory;

        RetryTestPolicy(RequestRetryTestFactory parent) {
            this.factory = parent;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
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
                if (!((this.factory.retryTestScenario == RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS
                        && this.factory.tryNumber > 4)
                        || (this.factory.retryTestScenario
                                == RequestRetryTestFactory.RETRY_TEST_SCENARIO_EXPONENTIAL_TIMING
                                && this.factory.tryNumber > 2))) {
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
            if (FlowableUtil.collectBytesInBuffer(request.body()).blockingGet()
                    .compareTo(RETRY_TEST_DEFAULT_DATA) != 0) {
                throw new IllegalArgumentException(("Body not reset."));
            }

            /*
            Modify the request as policies downstream of the retry policy are likely to do. These must be reset on each
            try.
             */
            request.headers().set(RETRY_TEST_HEADER, "testheader");
            UrlBuilder builder = UrlBuilder.parse(request.url());
            builder.setQueryParameter(RETRY_TEST_QUERY_PARAM, "testquery");
            try {
                request.withUrl(builder.toURL());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("The URL has been mangled");
            }

            switch (this.factory.retryTestScenario) {
                case RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS:
                    switch (this.factory.tryNumber) {
                        case 1:
                            /*
                            The timer is set with a timeout on the Single used to make the request. If the single
                            doesn't return success fast enough, it will throw a TimeoutException. We can short circuit
                            the waiting by simply returning an error. We will validate the time parameter later. Here,
                            we just test that a timeout is retried.
                             */
                            return Single.error(new TimeoutException());
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
                    switch (this.factory.tryNumber) {
                        case 1:
                            return RETRY_TEST_NON_RETRYABLE_ERROR;
                        default:
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
                            return Single.error(new IOException());
                        case 3:
                            return RETRY_TEST_OK_RESPONSE;
                        default:
                            throw new IllegalArgumentException("Continued retrying after success.");
                    }

                case RETRY_TEST_SCENARIO_TRY_TIMEOUT:
                    switch (this.factory.tryNumber) {
                        case 1:
                            return RETRY_TEST_OK_RESPONSE.delay(options.tryTimeout() + 1, TimeUnit.SECONDS);
                        case 2:
                            return RETRY_TEST_OK_RESPONSE.delay(options.tryTimeout() + 1, TimeUnit.SECONDS);
                        case 3:
                            return RETRY_TEST_OK_RESPONSE.delay(options.tryTimeout() - 1, TimeUnit.SECONDS);
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
                            return testDelayBounds(1, false,
                                    RETRY_TEST_NOT_FOUND_RESPONSE);
                        case 3:
                            return testDelayBounds(2, true,
                                    RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 4:
                            return testDelayBounds(3, true,
                                    RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
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
                            return testDelayBounds(1, false,
                                    RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
                        case 3:
                            return testDelayBounds(2, true,
                                    RETRY_TEST_TEMPORARY_ERROR_RESPONSE);
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
                            return Single.error(new UnexpectedLengthException("Unexpected length", 5, 6));
                        default:
                            throw new IllegalArgumentException("Retries continued on non retryable error.");
                    }
                default:
                    throw new IllegalArgumentException("Invalid retry test scenario.");
            }
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
                    return tryNumber == 1 ? 0 :(long) Math.ceil(this.factory.options.retryDelayInMs() / 1000);
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

        private Single<HttpResponse> testDelayBounds(int primaryTryNumber, boolean tryingPrimary,
                Single<HttpResponse> response) {
            /*
            We have to return a new Single so that the calculation for time is performed at the correct time, i.e. when
            the Single is actually subscribed to. This mocks an HttpClient because the requests are made only when
            the Single is subscribed to, not when all the infrastructure around it is put in place, and we care about
            the delay before the request itself.
             */
            return new Single<HttpResponse>() {
                @Override
                protected void subscribeActual(SingleObserver<? super HttpResponse> observer) {
                    try {
                        if (OffsetDateTime.now().isAfter(calcUpperBound(factory.time, primaryTryNumber, tryingPrimary))
                                || OffsetDateTime.now()
                                .isBefore(calcLowerBound(factory.time, primaryTryNumber, tryingPrimary))) {
                            throw new IllegalArgumentException("Delay was not within jitter bounds");
                        }
                        factory.time = OffsetDateTime.now();
                        /*
                        We can blocking get because it's not actually an IO call. Everything returned here returns
                        Single.just(response).
                        */
                        HttpResponse unwrappedResponse = response.blockingGet();
                        observer.onSuccess(unwrappedResponse);
                    } catch (StorageErrorException | IllegalArgumentException e) {
                        observer.onError(e);
                    }
                }
            };
        }

        private Single<HttpResponse> testMaxDelayBounds(Single<HttpResponse> response) {
            return new Single<HttpResponse>() {
                @Override
                protected void subscribeActual(SingleObserver<? super HttpResponse> observer) {
                    try {
                        if (OffsetDateTime.now().isAfter(factory.time.plusSeconds(
                                (long) Math.ceil((factory.options.maxRetryDelayInMs() / 1000) + 1)))) {
                            throw new IllegalArgumentException("Max retry delay exceeded");
                        } else if (OffsetDateTime.now().isBefore(factory.time.plusSeconds(
                                (long) Math.ceil((factory.options.maxRetryDelayInMs() / 1000) - 1)))) {
                            throw new IllegalArgumentException("Retry did not delay long enough");
                        }

                        factory.time = OffsetDateTime.now();
                        HttpResponse unwrappedResponse = response.blockingGet();
                        observer.onSuccess(unwrappedResponse);
                    } catch (StorageErrorException | IllegalArgumentException e) {
                        observer.onError(e);
                    }
                }
            };
        }
    }
}
