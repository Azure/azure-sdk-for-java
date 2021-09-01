// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.MockHttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import org.junit.jupiter.api.Test;
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

public class RedirectPolicyTest {

    @Test
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void defaultRedirectWhen308() throws Exception {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        // assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void redirectForNAttempts() throws MalformedURLException {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(5, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @Test
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.POST,
            new URL("http://localhost/"))).block();

        // not redirected to 200
        assertEquals(1, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @Test
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    @Test
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(2, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void redirectForProvidedHeader() throws MalformedURLException {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(5, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void redirectForProvidedMethods() throws MalformedURLException {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void nullRedirectUrlTest() throws MalformedURLException {
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

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(1, httpClient.getCount());
        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void redirectForMultipleRequests() throws MalformedURLException {
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

        HttpResponse response1 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        HttpResponse response2 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(4, httpClient.getCount());
        // Both requests successfully redirected for same request redirect lcoation
        assertEquals(200, response1.getStatusCode());
        assertEquals(200, response2.getStatusCode());
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
