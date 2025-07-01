// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class RedirectPolicyTest {
    private static final HttpRedirectOptions DEFAULT_REDIRECT_STRATEGY
        = new HttpRedirectOptions(3, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET, HttpMethod.HEAD));

    private final boolean isAsync;

    public RedirectPolicyTest(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @Test
    public void noRedirectPolicyTest() {
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        }).build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(308, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 308, 307, 301, 302 })
    public void defaultRedirectExpectedStatusCodes(int statusCode) {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");

                return new Response<>(request, statusCode, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectForNAttempts() {
        final int[] requestCount = { 1 };
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader
                = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]);

            requestCount[0]++;

            return new Response<>(request, 308, httpHeader, BinaryData.empty());
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(
                new HttpRedirectPolicy(new HttpRedirectOptions(5, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET))))
            .build();

        try (Response<?> response = sendRequest(pipeline, isAsync)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectNonAllowedMethodTest() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(
                new HttpRedirectOptions(5, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET, HttpMethod.HEAD))))
            .build();

        HttpRequest request = new HttpRequest().setMethod(HttpMethod.POST).setUri(URI.create("http://localhost/"));
        try (Response<BinaryData> response = sendRequest(pipeline, request, isAsync)) {
            // not redirected to 200
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectAllowedStatusCodesTest() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void alreadyAttemptedUrisTest() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else if (request.getUri().toString().equals("http://redirecthost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedHeader() {
        final int[] requestCount = { 1 };
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.fromString("Location1"),
                "http://redirecthost/" + requestCount[0]);

            requestCount[0]++;

            return new Response<>(request, 308, httpHeader, BinaryData.empty());
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(new HttpRedirectOptions(5, HttpHeaderName.fromString("Location1"), null)))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedMethods() {
        EnumSet<HttpMethod> allowedMethods = EnumSet.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT);
        final int[] requestCount = { 1 };
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader
                    = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);

                request.setMethod(HttpMethod.PUT);

                requestCount[0]++;

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else if (request.getUri().toString().equals("http://redirecthost/" + requestCount[0])
                && requestCount[0] == 2) {

                HttpHeaders httpHeader
                    = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);

                request.setMethod(HttpMethod.POST);

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(new HttpRedirectOptions(5, null, allowedMethods)))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void nullRedirectUriTest() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                return new Response<>(request, 308, new HttpHeaders(), BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForMultipleRequests() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response1 = sendRequest(pipeline, isAsync);
            Response<BinaryData> response2 = sendRequest(pipeline, isAsync)) {
            assertEquals(4, httpClient.getCount());
            // Both requests successfully redirected for same request redirect location
            assertEquals(200, response1.getStatusCode());
            // Both requests successfully redirected for same request redirect location
            assertEquals(200, response2.getStatusCode());
        }
    }

    @Test
    public void nonRedirectRequest() {
        final HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                return new Response<>(request, 401, new HttpHeaders(), BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        }).addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY)).build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(401, response.getStatusCode());
        }
    }

    @Test
    public void defaultRedirectAuthorizationHeaderCleared() {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");

                return new Response<>(request, 308, httpHeader, BinaryData.empty());
            } else {
                return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .addPolicy(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<BinaryData> response = sendRequest(pipeline, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectOptionsCanConfigureStatusCodeRedirectLogic() {
        // Only redirects on 429 responses
        HttpRedirectOptions httpRedirectOptions
            = new HttpRedirectOptions(1, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET))
                .setShouldRedirectCondition(redirectCondition -> redirectCondition.getResponse() != null
                    && redirectCondition.getResponse().getStatusCode() == 429);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline
            = new HttpPipelineBuilder().addPolicy(new HttpRedirectPolicy(httpRedirectOptions)).httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new Response<>(request, 429,
                        new HttpHeaders().add(HttpHeaderName.LOCATION, "http://localhost.com"), BinaryData.empty());
                } else {
                    return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                }
            }).build();

        try (Response<?> response = sendRequest(pipeline, isAsync)) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, attemptCount.get());
        }
    }

    static class RecordingHttpClient implements HttpClient {
        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Response<BinaryData>> handler;

        RecordingHttpClient(Function<HttpRequest, Response<BinaryData>> handler) {
            this.handler = handler;
        }

        @Override
        public Response<BinaryData> send(HttpRequest httpRequest) {
            count.getAndIncrement();

            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }
    }
}
