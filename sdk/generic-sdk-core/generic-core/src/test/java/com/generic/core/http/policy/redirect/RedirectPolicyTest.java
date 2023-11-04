// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy.redirect;

import com.generic.core.http.MockHttpResponse;
import com.generic.core.http.NoOpHttpClient;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.implementation.http.policy.redirect.DefaultRedirectStrategy;
import com.generic.core.implementation.http.policy.redirect.RedirectPolicy;
import com.generic.core.models.Context;
import com.generic.core.models.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.generic.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RedirectPolicyTest {

    private static final DefaultRedirectStrategy DEFAULT_REDIRECT_STRATEGY = new DefaultRedirectStrategy(3,
        HttpHeaderName.LOCATION.toString(), EnumSet.of(HttpMethod.GET, HttpMethod.HEAD));

    @Test
    public void noRedirectPolicyTest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                @Override
                public HttpResponse send(HttpRequest request, Context context) {
                    if (request.getUrl().toString().equals("http://localhost/")) {
                        Headers httpHeader = new Headers().set(HttpHeaderName.fromString("Location"), "http://redirecthost/");
                        return new MockHttpResponse(request, 308, httpHeader);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(308, response.getStatusCode());
        }

    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return new MockHttpResponse(request, statusCode, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodesSync(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return new MockHttpResponse(request, statusCode, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @Test
    public void redirectForNAttempts() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]);
            requestCount[0]++;
            return new MockHttpResponse(request, 308, httpHeader);
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, HttpHeaderName.LOCATION.toString(), EnumSet.of(HttpMethod.GET))))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectNonAllowedMethodTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, HttpHeaderName.LOCATION.toString(), EnumSet.of(HttpMethod.GET, HttpMethod.HEAD))))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.POST)) {
            // not redirected to 200
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectAllowedStatusCodesTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void alreadyAttemptedUrlsTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new MockHttpResponse(request, 308, httpHeader);
            } else if (request.getUrl().toString().equals("http://redirecthost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedHeader() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            Headers httpHeader = new Headers().set(HttpHeaderName.fromString("Location1"), "http://redirecthost/" + requestCount[0]);
            requestCount[0]++;
            return new MockHttpResponse(request, 308, httpHeader);
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, "Location1", null)))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForProvidedMethods() throws Exception {
        Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST));
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);
                request.setHttpMethod(HttpMethod.PUT);
                requestCount[0]++;
                return new MockHttpResponse(request, 308, httpHeader);
            } else if (request.getUrl().toString().equals("http://redirecthost/" + requestCount[0])
                && requestCount[0] == 2) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);
                request.setHttpMethod(HttpMethod.POST);
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, null, allowedMethods)))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @Test
    public void nullRedirectUrlTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                return new MockHttpResponse(request, 308);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @Test
    public void redirectForMultipleRequests() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response1 = sendRequest(pipeline, HttpMethod.GET)) {
            try (HttpResponse response2 = sendRequest(pipeline, HttpMethod.GET)) {
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
                public HttpResponse send(HttpRequest request, Context context) {
                    if (request.getUrl().toString().equals("http://localhost/")) {
                        return new MockHttpResponse(request, 401);
                    } else {
                        return new MockHttpResponse(request, 200);
                    }
                }
            })
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(401, response.getStatusCode());
        }
    }

    @Test
    public void defaultRedirectAuthorizationHeaderCleared() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Headers httpHeader = new Headers()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return new MockHttpResponse(request, 308, httpHeader);
            } else {
                return new MockHttpResponse(request, 200);
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(DEFAULT_REDIRECT_STRATEGY))
            .build();

        try (HttpResponse response = sendRequest(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    private HttpResponse sendRequest(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.send(new HttpRequest(httpMethod, createUrl("http://localhost/")), Context.NONE);
    }

    static class RecordingHttpClient implements HttpClient {

        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, HttpResponse> handler;

        RecordingHttpClient(Function<HttpRequest, HttpResponse> handler) {
            this.handler = handler;
        }

        @Override
        public HttpResponse send(HttpRequest httpRequest, Context context) {
            count.getAndIncrement();
            return handler.apply(httpRequest);
        }

        int getCount() {
            return count.get();
        }

        void resetCount() {
            count.set(0);
        }
    }
}
