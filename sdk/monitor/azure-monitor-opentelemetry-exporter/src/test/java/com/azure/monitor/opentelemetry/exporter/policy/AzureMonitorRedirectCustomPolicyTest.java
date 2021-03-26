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
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureMonitorRedirectCustomPolicyTest {

    @Test
    public void retryWith308Test() throws Exception {
        final int maxRetries = 1;
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", "http://redirecthost/");
        HttpHeaders httpHeader = new HttpHeaders(headers);
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    if(request.getUrl().toString().equals("http://localhost/")) {
                        return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                    } else {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .policies(new AzureMonitorRedirectCustomPolicy())
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void retryMaxTest() throws Exception {
        final int maxRetries = 10;
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Location", "http://redirecthost/"+count);
                    HttpHeaders httpHeader = new HttpHeaders(headers);
                    Assertions.assertTrue(count++ < maxRetries);
                    return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                }
            })
            .policies(new AzureMonitorRedirectCustomPolicy())
            .build();

        HttpResponse response = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(308, response.getStatusCode());
    }

    @Test
    public void retryWith308MultipleRequestsTest() throws Exception {
        final int maxRetries = 2;
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", "http://redirecthost/");
        HttpHeaders httpHeader = new HttpHeaders(headers);
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    if(request.getUrl().toString().equals("http://localhost/")) {
                        return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                    } else {
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .policies(new AzureMonitorRedirectCustomPolicy())
            .build();

        HttpResponse response1 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        HttpResponse response2 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();

        assertEquals(200, response1.getStatusCode());
        //Make sure the future requests are sent to http://redirecthost/
        assertEquals(200, response2.getStatusCode());
    }

    @Test
    public void retryWith308And500Test() throws Exception {
        final int maxRetries = 3;
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", "http://redirecthost/");
        HttpHeaders httpHeader = new HttpHeaders(headers);
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient() {
                int count = -1;

                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    Assertions.assertTrue(count++ < maxRetries);
                    if(count == 0) {
                        System.out.println("Unavailable");
                        return Mono.just(new MockHttpResponse(request, 500));
                    } else if(request.getUrl().toString().equals("http://localhost/")) {
                        System.out.println("Redirect");
                        return Mono.just(new MockHttpResponse(request, 308, httpHeader));
                    } else {
                        System.out.println("Success");
                        return Mono.just(new MockHttpResponse(request, 200));
                    }
                }
            })
            .policies(new AzureMonitorRedirectCustomPolicy())
            .policies(new RetryPolicy(new FixedDelay(3, Duration.of(0, ChronoUnit.MILLIS))))
            .build();

        HttpResponse response1 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        HttpResponse response2 = pipeline.send(new HttpRequest(HttpMethod.GET,
            new URL("http://localhost/"))).block();
        assertEquals(200, response1.getStatusCode());
        //Make sure the future requests are sent to http://redirecthost/
        assertEquals(200, response2.getStatusCode());
    }




}
