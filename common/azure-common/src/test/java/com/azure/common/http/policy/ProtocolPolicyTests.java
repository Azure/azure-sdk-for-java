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

public class ProtocolPolicyTests {

    @Test
    public void withOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", "ftp://www.bing.com");
        pipeline.send(createHttpRequest("http://www.bing.com"));
    }

    @Test
    public void withNoOverwrite() throws MalformedURLException {
        final HttpPipeline pipeline = createPipeline("ftp", false, "https://www.bing.com");
        pipeline.send(createHttpRequest("https://www.bing.com"));
    }
    private static HttpPipeline createPipeline(String protocol, String expectedUrl) {
        return new HttpPipeline(new MockHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.empty(); // NOP
            }
        },
        new ProtocolPolicy(protocol, true),
            (context, next) -> {
                assertEquals(expectedUrl, context.httpRequest().url().toString());
                return next.process();
            });
    }

    private static HttpPipeline createPipeline(String protocol, boolean overwrite, String expectedUrl) {
        return new HttpPipeline(new MockHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.empty(); // NOP
            }
        },
        new ProtocolPolicy(protocol, overwrite),
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
