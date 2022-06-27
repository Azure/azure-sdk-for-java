// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.SyncAsyncExtension;
import com.azure.core.SyncAsyncTest;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.util.Context;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(308, response.getStatusCode());
    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodes(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                headers.put("Authorization", "12345");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, statusCode, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        HttpResponse response = sendRequest(pipeline, HttpMethod.GET);

        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeaders().getValue("Authorization"));
    }

    @ParameterizedTest
    @ValueSource(ints = {308, 307, 301, 302})
    public void defaultRedirectExpectedStatusCodesSync(int statusCode) throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                headers.put("Authorization", "12345");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, statusCode, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        HttpResponse response = sendRequestSync(pipeline, HttpMethod.GET);

        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeaders().getValue("Authorization"));
    }

    @SyncAsyncTest
    public void redirectForNAttempts() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("Location", "http://redirecthost/" + requestCount[0]);
            HttpHeaders httpHeader = new HttpHeaders(headers);
            requestCount[0]++;
            return Mono.just(new MockHttpResponse(request, 308, httpHeader));
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5)))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(5, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @SyncAsyncTest
    public void redirectNonAllowedMethodTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5)))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.POST),
            () -> sendRequestSync(pipeline, HttpMethod.POST)
        );

        // not redirected to 200
        assertEquals(1, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @SyncAsyncTest
    public void redirectAllowedStatusCodesTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    @SyncAsyncTest
    public void alreadyAttemptedUrlsTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else if (request.getUrl().toString().equals("http://redirecthost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(2, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @SyncAsyncTest
    public void redirectForProvidedHeader() throws Exception {
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("Location1", "http://redirecthost/" + requestCount[0]);
            HttpHeaders httpHeader = new HttpHeaders(headers);
            requestCount[0]++;
            return Mono.just(new MockHttpResponse(request, 308, httpHeader));
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy(5, "Location1", null)))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(5, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @SyncAsyncTest
    public void redirectForProvidedMethods() throws Exception {
        Set<HttpMethod> allowedMethods = new HashSet<HttpMethod>() {
                {
                    add(HttpMethod.GET);
                    add(HttpMethod.PUT);
                    add(HttpMethod.POST);
                }
        };
        final int[] requestCount = {1};
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/" + requestCount[0]++);
                HttpHeaders httpHeader = new HttpHeaders(headers);
                request.setHttpMethod(HttpMethod.PUT);
                requestCount[0]++;
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else if (request.getUrl().toString().equals("http://redirecthost/" + requestCount[0])
                && requestCount[0] == 2) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/" + requestCount[0]++);
                HttpHeaders httpHeader = new HttpHeaders(headers);
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

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    @SyncAsyncTest
    public void nullRedirectUrlTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy(new DefaultRedirectStrategy()))
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(1, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @SyncAsyncTest
    public void redirectForMultipleRequests() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        HttpResponse response1 = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        HttpResponse response2 = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(4, httpClient.getCount());
        // Both requests successfully redirected for same request redirect location
        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
    }

    @SyncAsyncTest
    public void nonRedirectRequest() throws Exception {
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    if (request.getUrl().toString().equals("http://localhost/")) {
                        Map<String, String> headers = new HashMap<>();
                        HttpHeaders httpHeader = new HttpHeaders(headers);
                        return Mono.just(new MockHttpResponse(request, 401, httpHeader));
                    } else {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .policies(new RedirectPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(401, response.getStatusCode());
    }

    @SyncAsyncTest
    public void defaultRedirectAuthorizationHeaderCleared() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
            if (request.getUrl().toString().equals("http://localhost/")) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/");
                headers.put("Authorization", "12345");
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            } else {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new RedirectPolicy())
            .build();

        HttpResponse response = SyncAsyncExtension.execute(
            () -> sendRequest(pipeline, HttpMethod.GET),
            () -> sendRequestSync(pipeline, HttpMethod.GET)
        );

        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeaders().getValue("Authorization"));
    }

    private HttpResponse sendRequest(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.send(new HttpRequest(httpMethod, new URL("http://localhost/"))).block();
    }

    private HttpResponse sendRequestSync(HttpPipeline pipeline, HttpMethod httpMethod) throws MalformedURLException {
        return pipeline.sendSync(new HttpRequest(httpMethod,
            new URL("http://localhost/")), Context.NONE);
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
