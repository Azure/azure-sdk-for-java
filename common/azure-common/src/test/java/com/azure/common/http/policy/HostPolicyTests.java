// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http.policy;

import com.azure.common.http.HttpClient;
import com.azure.common.http.HttpMethod;
import com.azure.common.http.HttpPipeline;
import com.azure.common.http.HttpRequest;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.ProxyOptions;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;

public class HostPolicyTests {
    @Test
    public void withNoPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost");
        pipeline.send(createHttpRequest("ftp://www.example.com")).block();
    }

    @Test
    public void withPort() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("localhost", "ftp://localhost:1234");
        pipeline.send(createHttpRequest("ftp://www.example.com:1234"));
    }

    private static HttpPipeline createPipeline(String host, String expectedUrl) {
        return new HttpPipeline(new MockHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.empty(); // NOP
            }
        },
        new HostPolicy(host),
            (context, next) -> {
                assertEquals(expectedUrl, context.httpRequest().url().toString());
                return next.process();
            });
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
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
