// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.NoOpHttpClient;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRedirectOptions;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RedirectPolicyTest {
    private static final HttpRedirectOptions DEFAULT_REDIRECT_STRATEGY = new HttpRedirectOptions(3,
        HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET, HttpMethod.HEAD));

    @Test
    public void noRedirectPolicyTest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Response<?> send(HttpRequest request) {
                    if (request.getUri().toString().equals("http://localhost/")) {
                        HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.fromString("Location"), "http://redirecthost/");
                        return new MockHttpResponse(request, 308, httpHeader);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(308, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");

                return new MockHttpResponse(request, statusCode, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectForNAttempts() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]);

            requestCount[0]++;

            return new MockHttpResponse(request, 308, httpHeader);
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(new HttpRedirectOptions(5, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET))))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectNonAllowedMethodTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(new HttpRedirectOptions(5, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET, HttpMethod.HEAD))))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.POST)) {
            // not redirected to 200
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectAllowedStatusCodesTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void alreadyAttemptedUrisTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new MockHttpResponse(request, 308, httpHeader);
            } else if (request.getUri().toString().equals("http://redirecthost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedHeader() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.fromString("Location1"), "http://redirecthost/" + requestCount[0]);

            requestCount[0]++;

            return new MockHttpResponse(request, 308, httpHeader);
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(new HttpRedirectOptions(5, HttpHeaderName.fromString("Location1"), null)))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedMethods() throws Exception {
        EnumSet<HttpMethod> allowedMethods = EnumSet.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT);
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);

                request.setHttpMethod(HttpMethod.PUT);

                requestCount[0]++;

                return new MockHttpResponse(request, 308, httpHeader);
            } else if (request.getUri().toString().equals("http://redirecthost/" + requestCount[0])
                && requestCount[0] == 2) {

                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);

                request.setHttpMethod(HttpMethod.POST);

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(new HttpRedirectOptions(5, null, allowedMethods)))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void nullRedirectUriTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                return new MockHttpResponse(request, 308);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForMultipleRequests() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response1 = sendRequest(pipeline, HttpMethod.GET)) {
            try (Response<?> response2 = sendRequest(pipeline, HttpMethod.GET)) {
                assertEquals(4, httpClient.getCount());
                // Both requests successfully redirected for same request redirect location
                assertEquals(200, response1.getStatusCode());
                // Both requests successfully redirected for same request redirect location
                assertEquals(200, response2.getStatusCode());
            }
        }
    }

    @Test
    public void nonRedirectRequest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public Response<?> send(HttpRequest request) {
                    if (request.getUri().toString().equals("http://localhost/")) {
                        return new MockHttpResponse(request, 401);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(401, response.getStatusCode());
        }
    }

    @Test
    public void defaultRedirectAuthorizationHeaderCleared() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUri().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");

                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new HttpRedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (Response<?> response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectOptionsCanConfigureStatusCodeRedirectLogic() throws IOException {
        // Only redirects on 429 responses
        HttpRedirectOptions httpRedirectOptions
            = new HttpRedirectOptions(1, HttpHeaderName.LOCATION, EnumSet.of(HttpMethod.GET))
            .setShouldRedirectCondition(
                redirectCondition -> redirectCondition.getResponse() != null
                    && redirectCondition.getResponse().getStatusCode() == 429);

        AtomicInteger attemptCount = new AtomicInteger();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new HttpRedirectPolicy(httpRedirectOptions))
            .httpClient(request -> {
                int count = attemptCount.getAndIncrement();
                if (count == 0) {
                    return new MockHttpResponse(request, 429,
                        new HttpHeaders().add(HttpHeaderName.LOCATION, "http://localhost.com"));
                } else {
                    return new MockHttpResponse(request, 200);
                }
            })
            .build();

        try (Response<?> response = pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/"))) {
            assertEquals(200, response.getStatusCode());
            assertEquals(2, attemptCount.get());
        }
    }

    private Response<?> sendRequest(HttpPipeline pipeline, HttpMethod httpMethod) {
        return pipeline.send(new HttpRequest(httpMethod, URI.create("http://localhost/")));
    }

    static class RecordingHttpClient implements HttpClient {
        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Response<?>> handler;

        RecordingHttpClient(Function<HttpRequest, Response<?>> handler) {
            this.handler = handler;
        }

        @Override
        public Response<?> send(HttpRequest httpRequest) {
            count.getAndIncrement();

            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }
    }
}
