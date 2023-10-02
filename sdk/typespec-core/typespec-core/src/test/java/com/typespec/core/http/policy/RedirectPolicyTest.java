// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.http.policy;

import com.typespec.core.SyncAsyncExtension;
import com.typespec.core.SyncAsyncTest;
import com.typespec.core.http.HttpClient;
import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.HttpMethod;
import com.typespec.core.http.HttpPipeline;
import com.typespec.core.http.HttpPipelineBuilder;
import com.typespec.core.http.HttpRequest;
import com.typespec.core.http.HttpResponse;
import com.typespec.core.http.MockHttpResponse;
import com.typespec.core.http.clients.NoOpHttpClient;
import com.typespec.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.typespec.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RedirectPolicyTest {
    @SyncAsyncTest
    public void noRedirectPolicyTest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (request.getUrl().toString().equals("http://localhost/")) {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Location", "http://redirecthost/");
                        HttpHeaders httpHeader = new HttpHeaders(headers);
                        return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                    } else {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(308, response.getStatusCode());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return Mono.just(new MockHttpResponse(request, statusCode, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
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
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return Mono.just(new MockHttpResponse(request, statusCode, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        try (HttpResponse response = sendRequestSync(pipeline, HttpMethod.GET)) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    @SyncAsyncTest
    public void redirectForNAttempts() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader = new HttpHeaders()
                .set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]);
            requestCount[0]++;
            return Mono.just(new MockHttpResponse(request, 308, httpHeader));
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5)))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void redirectNonAllowedMethodTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5)))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.POST),
            () -> sendRequest(pipeline, HttpMethod.POST)
        )) {
            // not redirected to 200
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void redirectAllowedStatusCodesTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void alreadyAttemptedUrlsTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else if (request.getUrl().toString().equals("http://redirecthost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(2, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void redirectForProvidedHeader() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            HttpHeaders httpHeader = new HttpHeaders()
                .set(HttpHeaderName.fromString("Location1"), "http://redirecthost/" + requestCount[0]);
            requestCount[0]++;
            return Mono.just(new MockHttpResponse(request, 308, httpHeader));
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, "Location1", null)))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(5, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void redirectForProvidedMethods() throws Exception {
        Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.PUT, HttpMethod.POST));
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);
                request.setHttpMethod(HttpMethod.PUT);
                requestCount[0]++;
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else if (request.getUrl().toString().equals("http://redirecthost/" + requestCount[0])
                && requestCount[0] == 2) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/" + requestCount[0]++);
                request.setHttpMethod(HttpMethod.POST);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, null, allowedMethods)))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(2, httpClient.getCount());
            assertEquals(200, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void nullRedirectUrlTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                return Mono.just(new MockHttpResponse(request, 308, new HttpHeaders()));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(1, httpClient.getCount());
            assertEquals(308, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void redirectForMultipleRequests() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        try (HttpResponse response1 = SyncAsyncExtension.execute(
                () -> sendRequestSync(pipeline, HttpMethod.GET),
                () -> sendRequest(pipeline, HttpMethod.GET)
            );
             HttpResponse response2 = SyncAsyncExtension.execute(
                 () -> sendRequestSync(pipeline, HttpMethod.GET),
                 () -> sendRequest(pipeline, HttpMethod.GET)
             )) {
            assertEquals(4, httpClient.getCount());
            // Both requests successfully redirected for same request redirect location
            assertEquals(200, response1.getStatusCode());
            assertEquals(200, response2.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void nonRedirectRequest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (request.getUrl().toString().equals("http://localhost/")) {
                        return Mono.just(new MockHttpResponse(request, 401, new HttpHeaders()));
                    } else {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .policies(new RedirectPolicy())
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(401, response.getStatusCode());
        }
    }

    @SyncAsyncTest
    public void defaultRedirectAuthorizationHeaderCleared() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                HttpHeaders httpHeader = new HttpHeaders()
                    .set(HttpHeaderName.LOCATION, "http://redirecthost/")
                    .set(HttpHeaderName.AUTHORIZATION, "12345");
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        try (HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequestSync(pipeline, HttpMethod.GET),
            () -> sendRequest(pipeline, HttpMethod.GET)
        )) {
            assertEquals(200, response.getStatusCode());
            assertNull(response.getRequest().getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        }
    }

    private HttpResponse sendRequest(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.send(new HttpRequest(httpMethod, createUrl("http://localhost/"))).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.sendSync(new HttpRequest(httpMethod, createUrl("http://localhost/")), Context.NONE);
    }

    static class RecordingHttpClient implements HttpClient {

        private final AtomicInteger count = new AtomicInteger();
        private final Function<HttpRequest, Mono<HttpResponse>> handler;

        RecordingHttpClient(Function<HttpRequest, Mono<HttpResponse>> handler) {
            this.handler = handler;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
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
