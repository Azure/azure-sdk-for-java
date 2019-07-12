// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.ProxyOptions;
import com.azure.core.util.logging.ClientLogger;
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
        return HttpPipeline.builder()
            .httpClient(new MockHttpClient())
            .policies(new HostPolicy(host),
                (context, next) -> {
                    assertEquals(expectedUrl, context.httpRequest().url().toString());
                    return next.process();
                })
            .build();
    }

    private static HttpRequest createHttpRequest(String url) throws MalformedURLException {
        return new HttpRequest(HttpMethod.GET, new URL(url));
    }

    private static class MockHttpClient implements HttpClient {
        private final ClientLogger logger = new ClientLogger(MockHttpClient.class);

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.empty(); // NOP
        }

        @Override
        public HttpClient proxy(Supplier<ProxyOptions> proxyOptions) {
            logger.logAndThrow(new IllegalStateException("MockHttpClient.proxy"));
            return null;
        }

        @Override
        public HttpClient wiretap(boolean enableWiretap) {
            logger.logAndThrow(new IllegalStateException("MockHttpClient.wiretap"));
            return null;
        }

        @Override
        public HttpClient port(int port) {
            logger.logAndThrow(new IllegalStateException("MockHttpClient.port"));
            return null;
        }
    }
}
