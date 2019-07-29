// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.http.policy.PortPolicy;
import com.azure.core.http.policy.ProtocolPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HttpPipelineTests {
    @Test
    public void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        assertEquals(0, pipeline.getPolicyCount());
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withRequestPolicy() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(80, true),
                new ProtocolPolicy("ftp", true),
                new RetryPolicy())
            .build();

        assertEquals(3, pipeline.getPolicyCount());
        assertEquals(PortPolicy.class, pipeline.getPolicy(0).getClass());
        assertEquals(ProtocolPolicy.class, pipeline.getPolicy(1).getClass());
        assertEquals(RetryPolicy.class, pipeline.getPolicy(2).getClass());
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withRequestOptions() throws MalformedURLException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(80, true),
                new ProtocolPolicy("ftp", true),
                new RetryPolicy())
            .build();

        HttpPipelineCallContext context = new HttpPipelineCallContext(new HttpRequest(HttpMethod.GET, new URL("http://foo.com")));
        assertNotNull(context);
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withNoRequestPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(new MockHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    assertEquals(0, request.headers().size());
                    assertEquals(expectedHttpMethod, request.httpMethod());
                    assertEquals(expectedUrl, request.url());
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            })
            .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void withUserAgentRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com/1");
        final String expectedUserAgent = "my-user-agent";
        final HttpClient httpClient = new MockHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(1, request.headers().size());
                assertEquals(expectedUserAgent, request.headers().value("User-Agent"));
                assertEquals(expectedHttpMethod, request.httpMethod());
                assertEquals(expectedUrl, request.url());
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new UserAgentPolicy(expectedUserAgent))
            .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void withRequestIdRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com/1");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(new MockHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    assertEquals(1, request.headers().size());
                    final String requestId = request.headers().value("x-ms-client-request-id");
                    assertNotNull(requestId);
                    assertFalse(requestId.isEmpty());

                    assertEquals(expectedHttpMethod, request.httpMethod());
                    assertEquals(expectedUrl, request.url());
                    return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
                }
            })
            .policies(new RequestIdPolicy())
            .build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    private abstract static class MockHttpClient implements HttpClient {

        @Override
        public abstract Mono<HttpResponse> send(HttpRequest request);

        @Override
        public HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
            throw new IllegalStateException("MockHttpClient.proxy");
        }

        @Override
        public HttpClient wiretap(boolean enableWiretap) {
            throw new IllegalStateException("MockHttpClient.wiretap");
        }

        @Override
        public HttpClient port(int port) {
            throw new IllegalStateException("MockHttpClient.port");
        }
    }
}
