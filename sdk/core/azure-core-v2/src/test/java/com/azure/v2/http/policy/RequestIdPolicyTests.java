// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.azure.core.CoreTestUtils.createUrl;

public class RequestIdPolicyTests {

    private final HttpResponse mockResponse = new HttpResponse(null) {
        @Override
        public int getStatusCode() {
            return 500;
        }

        @Override
        @Deprecated
        public String getHeaderValue(String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public byte[]> getBodyAsByteArray() {
            return null;
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public String> getBodyAsString() {
            return null;
        }

        @Override
        public String> getBodyAsString(Charset charset) {
            return null;
        }
    };

    @SyncAsyncTest
    public void newRequestIdForEachCall() throws Exception {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            String firstRequestId = null;

            @Override
            public Response<?>> send(HttpRequest request) {
                if (firstRequestId != null) {
                    String newRequestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                    Assertions.assertNotNull(newRequestId);
                    Assertions.assertNotEquals(newRequestId, firstRequestId);
                }

                firstRequestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                if (firstRequestId == null) {
                    Assertions.fail();
                }
                return mockResponse);
            }
        }).policies(new RequestIdPolicy()).build();

        SyncAsyncExtension.execute(() -> pipeline.send(createHttpRequest("https://www.bing.com"), Context.none()),
            () -> pipeline.send(createHttpRequest("https://www.bing.com")));
    }

    @SyncAsyncTest
    public void sameRequestIdForRetry() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            String firstRequestId = null;

            @Override
            public Response<?>> send(HttpRequest request) {
                if (firstRequestId != null) {
                    String newRequestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                    Assertions.assertNotNull(newRequestId);
                    Assertions.assertEquals(newRequestId, firstRequestId);
                }
                firstRequestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                if (firstRequestId == null) {
                    Assertions.fail();
                }
                return mockResponse);
            }
        })
            .policies(new RequestIdPolicy(), new RetryPolicy(new FixedDelay(1, Duration.of(0, ChronoUnit.SECONDS))))
            .build();

        SyncAsyncExtension.execute(() -> pipeline.send(createHttpRequest("https://www.bing.com"), Context.none()),
            () -> pipeline.send(createHttpRequest("https://www.bing.com")));
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, createUrl(url));
    }
}
