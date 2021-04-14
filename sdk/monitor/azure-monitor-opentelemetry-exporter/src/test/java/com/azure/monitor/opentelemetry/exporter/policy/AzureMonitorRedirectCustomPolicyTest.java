// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.policy;

import com.azure.core.http.*;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureMonitorRedirectCustomPolicyTest {

    @Test
    public void retryWith308Test() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient() {
            @Override
            public Mono<HttpResponse> internalSend(HttpRequest request) {
                if (request.getUrl().toString().equals("http://localhost/")) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Location", "http://redirecthost/");
                    HttpHeaders httpHeader = new HttpHeaders(headers);
                    return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            }
        };
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new AzureMonitorRedirectCustomPolicy())
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        
        assertEquals(2, httpClient.httpRequestsCaptured.size());
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void retryMaxTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient() {
            @Override
            public Mono<HttpResponse> internalSend(HttpRequest request) {
                Map<String, String> headers = new HashMap<>();
                headers.put("Location", "http://redirecthost/" + httpRequestsCaptured.size());
                HttpHeaders httpHeader = new HttpHeaders(headers);
                return Mono.just(new MockHttpResponse(request, 308, httpHeader));
            }
        };
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new AzureMonitorRedirectCustomPolicy(3))
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        // redirect is captured only 3 times
        assertEquals(4, httpClient.httpRequestsCaptured.size());
        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void retryWith308MultipleRequestsTest() throws Exception {
        RecordingHttpClient httpClient = new RecordingHttpClient() {
            @Override
            public Mono<HttpResponse> internalSend(HttpRequest request) {
                if (request.getUrl().toString().equals("http://localhost/")) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Location", "http://redirecthost/");
                    HttpHeaders httpHeader = new HttpHeaders(headers);
                    return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                } else {
                    return Mono.just(new MockHttpResponse(request, 200));
                }
            }
        };
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new AzureMonitorRedirectCustomPolicy())
            .build();

        assertEquals(0,httpClient.httpRequestsCaptured.size());
        HttpResponse response1 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        assertEquals(2,httpClient.httpRequestsCaptured.size());
        HttpResponse response2 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        assertEquals(3,httpClient.httpRequestsCaptured.size());
        assertEquals(200, response1.getStatusCode());
        //Make sure the future requests are sent to http://redirecthost/
        assertEquals(200, response2.getStatusCode());

    }

    static abstract class RecordingHttpClient implements HttpClient {

        final List<HttpRequest> httpRequestsCaptured = new ArrayList<>();

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            httpRequestsCaptured.add(httpRequest);
            return internalSend(httpRequest);
        }

        abstract Mono<HttpResponse> internalSend(HttpRequest httpRequest);
    }

}
