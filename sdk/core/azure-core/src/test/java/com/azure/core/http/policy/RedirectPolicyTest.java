// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpClient;
import com.azure.core.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RedirectPolicyTest {

    @Test
    public void retryWith308Test() throws Exception {
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
            .policies(new RedirectPolicy(308))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(2, httpClient.getCount());
        assertEquals(200, response.getStatusCode());
    }

    // @Test
    // public void retryMaxTest() throws Exception {
    //     RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
    //         Map<String, String> headers = new HashMap<>();
    //         headers.put("Location", "http://redirecthost/");
    //         HttpHeaders httpHeader = new HttpHeaders(headers);
    //         return Mono.just(new MockHttpResponse(request, 308, httpHeader));
    //     });
    //
    //     HttpPipeline pipeline = new HttpPipelineBuilder()
    //         .httpClient(httpClient)
    //         .policies(new AzureMonitorRedirectPolicy())
    //         .build();
    //
    //     HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
    //         new URL("http://localhost/"))).block();
    //     // redirect is captured only 3 times
    //     assertEquals(11, httpClient.getCount());
    //     assertEquals(308, response.getStatusCode());
    // }
    //
    // @Test
    // public void retryWith308MultipleRequestsTest() throws Exception {
    //     RecordingHttpClient httpClient = new RecordingHttpClient(request -> {
    //         if (request.getUrl().toString().equals("http://localhost/")) {
    //             Map<String, String> headers = new HashMap<>();
    //             headers.put("Location", "http://redirecthost/");
    //             HttpHeaders httpHeader = new HttpHeaders(headers);
    //             return Mono.just(new MockHttpResponse(request, 308, httpHeader));
    //         } else {
    //             return Mono.just(new MockHttpResponse(request, 200));
    //         }
    //     });
    //
    //     HttpPipeline pipeline = new HttpPipelineBuilder()
    //         .httpClient(httpClient)
    //         .policies(new AzureMonitorRedirectPolicy())
    //         .build();
    //
    //     assertEquals(0, httpClient.getCount());
    //     HttpResponse response1 = pipeline.send(new HttpRequest(HttpMethod.GET,
    //         new URL("http://localhost/"))).block();
    //     assertEquals(200, response1.getStatusCode());
    //     assertEquals(2, httpClient.getCount());
    //
    //     httpClient.resetCount();
    //     HttpResponse response2 = pipeline.send(new HttpRequest(HttpMethod.GET,
    //         new URL("http://localhost/"))).block();
    //     assertEquals(200, response2.getStatusCode());
    //     //Make sure the future requests are sent directly to http://redirecthost/
    //     assertEquals(1, httpClient.getCount());
    // }

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
