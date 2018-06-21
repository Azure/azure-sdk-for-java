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

package com.microsoft.azure.storage;

import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpRequest;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.http.UrlBuilder;
import com.microsoft.rest.v2.policy.RequestPolicy;
import com.microsoft.rest.v2.policy.RequestPolicyFactory;
import com.microsoft.rest.v2.policy.RequestPolicyOptions;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

public class RequestRetryTestFactory implements RequestPolicyFactory{
    public static final int  RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS = 1;

    public static final int  RETRY_TEST_SCENARIO_RETRY_UNTIL_OPERATION_CANCEL = 2;

    public static final int RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES = 3;

    // Scenario to actually validate the waiting time and other parameters

    // Scenario non retryable

    // Scenario timeout the whole operation

    // Scenario for retry delay and maxRetry delay

    // 400 against secondary should not retry

    // return an IOException to represent a network error

    private static final String RETRY_TEST_PRIMARY_HOST = "PrimaryDC";

    private static final String RETRY_TEST_SECONDARY_HOST = "SecondaryDC";

    private static final String RETRY_TEST_HEADER = "TestHeader";

    private static final String RETRY_TEST_QUERY_PARAM = "TestQueryParam";

    public static final ByteBuffer RETRY_TEST_DEFAULT_DATA = ByteBuffer.wrap("Default data".getBytes());

    private int retryTestScenario;

    private int maxRetries;

    private int tryNumber;

    public RequestRetryTestFactory(int scenario, int maxRetries) {
        this.retryTestScenario = scenario;
        this.maxRetries = maxRetries;
    }

    @Override
    public RequestPolicy create(RequestPolicy next, RequestPolicyOptions options) {
        return new RetryTestPolicy(this);
    }

    private final class RetryTestPolicy implements RequestPolicy{
        private RequestRetryTestFactory factory;

        public RetryTestPolicy(RequestRetryTestFactory parent) {
            this.factory = parent;
        }

        @Override
        public Single<HttpResponse> sendAsync(HttpRequest request) {
            this.factory.tryNumber++;
            if (this.factory.tryNumber > this.factory.maxRetries) {
                throw new IllegalArgumentException("Try number has exceeded max tries");
            }

            // Validate the expected preconditions for each try: The correct host is used.
            String expectedHost = RETRY_TEST_PRIMARY_HOST;
            if (this.factory.tryNumber%2 == 0) {
                /*
                 The retry until success scenario fail's on the 4th try with a 404 on the secondary, so we never expect
                 it to check the secondary after that. All other tests should continue to check the secondary.
                 */
                if (this.factory.retryTestScenario != RequestRetryTestFactory.RETRY_TEST_SCENARIO_RETRY_UNTIL_SUCCESS ||
                        this.factory.tryNumber <= 4) {
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
            /*if (request.headers().value(RETRY_TEST_HEADER) != null ||
                    (request.url().getQuery() != null && request.url().getQuery().contains(RETRY_TEST_QUERY_PARAM)) ||
                    FlowableUtil.collectBytesInBuffer(request.body()).blockingGet()
                            .compareTo(RETRY_TEST_DEFAULT_DATA) != 0) {
                throw new IllegalArgumentException("The request was not reset from the previous retry");
            }*/
            if (request.headers().value(RETRY_TEST_HEADER) != null) {
                throw new IllegalArgumentException("Headers not reset");
            }
            if ((request.url().getQuery() != null && request.url().getQuery().contains(RETRY_TEST_QUERY_PARAM))) {
                throw new IllegalArgumentException ("Query params");
            }
            if (FlowableUtil.collectBytesInBuffer(request.body()).blockingGet().compareTo(RETRY_TEST_DEFAULT_DATA) != 0) {
                throw new IllegalArgumentException(("body"));
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
                            return Single.just(new RetryTestResponse(500));
                        case 3:
                            return Single.just(new RetryTestResponse(503));
                        case 4:
                            /*
                            By returning 404 when we should be testing against the secondary, we exercise the logic
                            that should prevent further tries to secondary when the secondary evidently doesn't have the
                            data.
                             */
                            return Single.just(new RetryTestResponse(404));
                        case 5:
                            // Just to get to a sixth try where we ensure we should not be trying the secondary again.
                            return Single.just(new RetryTestResponse(500));
                        case 6:
                            return Single.just(new RetryTestResponse(200));
                        default:
                            throw new IllegalArgumentException("Continued trying after success");
                    }
                case RETRY_TEST_SCENARIO_RETRY_UNTIL_MAX_RETRIES:
                    return Single.just(new RetryTestResponse(500)); // Keep retrying until max retries hit.
            }
            return Single.error(new IllegalArgumentException("Invalid scenario"));
        }
    }

    // The retry factory only really cares about the status code.
    private final class RetryTestResponse extends HttpResponse {

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
}
