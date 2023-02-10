// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ContainerRegistryRedirectPolicyTests {
    private static final String REDIRECT_URL = "http://localhost/redirect";
    private static final String FINAL_URL = "http://localhost/final";
    @SyncAsyncTest
    public void oneRedirectSync() throws Exception {
        HttpClient httpClient = createClient(1);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new ContainerRegistryRedirectPolicy())
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(REDIRECT_URL));
        HttpResponse response = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(UtilsImpl.DOCKER_DIGEST_HEADER_NAME));
    }

    @SyncAsyncTest
    public void noRedirects() throws Exception {
        HttpClient httpClient = createClient(0);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new ContainerRegistryRedirectPolicy())
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(FINAL_URL));
        HttpResponse response = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request));

        assertEquals(200, response.getStatusCode());
        assertNull(response.getHeaders().getValue(UtilsImpl.DOCKER_DIGEST_HEADER_NAME));
    }

    @SyncAsyncTest
    public void tooManyRedirects() throws Exception {
        HttpClient httpClient = createClient(3);

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new ContainerRegistryRedirectPolicy())
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(REDIRECT_URL));
        HttpResponse response = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request));

        assertEquals(308, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(UtilsImpl.DOCKER_DIGEST_HEADER_NAME));
    }

    @SyncAsyncTest
    public void cycle() throws Exception {
        AtomicInteger attempt = new AtomicInteger(0);
        HttpClient httpClient = new MockHttpClient(request -> {
            attempt.incrementAndGet();
            return new MockHttpResponse(request, 308, new HttpHeaders()
                .add("Location", REDIRECT_URL)
                .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, "sha:somevalue"));
        });

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(new ContainerRegistryRedirectPolicy())
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(REDIRECT_URL));
        HttpResponse response = SyncAsyncExtension.execute(() -> pipeline.sendSync(request, Context.NONE),
            () -> pipeline.send(request));

        assertEquals(2, attempt.get());
        assertEquals(308, response.getStatusCode());
        assertNotNull(response.getHeaders().getValue(UtilsImpl.DOCKER_DIGEST_HEADER_NAME));
    }

    public static HttpClient createClient(int redirects) {
        AtomicInteger redirectsSoFar = new AtomicInteger(0);
        return new MockHttpClient(request -> {
            if (request.getUrl().toString().startsWith(REDIRECT_URL))  {
                String url = FINAL_URL;
                if (redirectsSoFar.incrementAndGet() < redirects) {
                    url = REDIRECT_URL + redirectsSoFar.get();
                }

                return new MockHttpResponse(request, 308, new HttpHeaders()
                    .add("Location", url)
                    .add(UtilsImpl.DOCKER_DIGEST_HEADER_NAME, "sha:somevalue"));

            }

            return new MockHttpResponse(request, 200)
                .addHeader("some", "run");
        });
    }

    public static String getRequestUrl(HttpRequest request) {
        return request.getHttpMethod() + request.getUrl().toString();
    }

    static class MockHttpClient implements HttpClient {
        private final Function<HttpRequest, HttpResponse> requestToResponse;
        MockHttpClient(Function<HttpRequest, HttpResponse> requestToResponse) {
            this.requestToResponse = requestToResponse;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest httpRequest) {
            return Mono.just(requestToResponse.apply(httpRequest));
        }

        @Override
        public HttpResponse sendSync(HttpRequest httpRequest, Context context) {
            return requestToResponse.apply(httpRequest);
        }
    }
}
