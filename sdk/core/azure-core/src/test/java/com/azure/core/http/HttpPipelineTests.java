// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.http.policy.PortPolicy;
import com.azure.core.http.policy.ProtocolPolicy;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.validation.http.MtlsTestCertificates;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpPipelineTests {
    private static final ClientCertificateBinding BINDING
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);

    @Test
    public void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                // do nothing
                return null;
            }
        }).build();
        assertEquals(0, pipeline.getPolicyCount());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withRequestPolicy() {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(80, true), new ProtocolPolicy("ftp", true), new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    // do nothing
                    return null;
                }
            })
            .build();

        assertEquals(3, pipeline.getPolicyCount());
        assertEquals(PortPolicy.class, pipeline.getPolicy(0).getClass());
        assertEquals(ProtocolPolicy.class, pipeline.getPolicy(1).getClass());
        assertEquals(RetryPolicy.class, pipeline.getPolicy(2).getClass());
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withRequestOptions() throws MalformedURLException {
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new PortPolicy(80, true), new ProtocolPolicy("ftp", true), new RetryPolicy())
            .httpClient(new NoOpHttpClient() {
                @Override
                public Mono<HttpResponse> send(HttpRequest request) {
                    // do nothing
                    return null;
                }
            })
            .build();

        HttpPipelineCallContext context
            = new HttpPipelineCallContext(new HttpRequest(HttpMethod.GET, createUrl("http://foo.com")));
        assertNotNull(context);
        assertNotNull(pipeline.getHttpClient());
    }

    @Test
    public void withNoRequestPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(0, request.getHeaders().getSize());
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void withUserAgentRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com/1");
        final String expectedUserAgent = "my-user-agent";
        final HttpClient httpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                assertEquals(expectedUserAgent, request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline
            = new HttpPipelineBuilder().httpClient(httpClient).policies(new UserAgentPolicy(expectedUserAgent)).build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void withRequestIdRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com/1");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                final String requestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                assertNotNull(requestId);
                assertFalse(requestId.isEmpty());

                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).policies(new RequestIdPolicy()).build();

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void sendSyncWithUserAgentPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final String expectedUserAgent = "my-user-agent";
        final URL expectedUrl = createUrl("http://my.site.com/1");
        final HttpClient httpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                assertEquals(expectedUserAgent, request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .policies((new UserAgentPolicy(expectedUserAgent)))
            .build();

        final HttpResponse response
            = httpPipeline.sendSync(new HttpRequest(expectedHttpMethod, expectedUrl), Context.NONE);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void sendSyncWithPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(0, request.getHeaders().getSize());
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).build();

        final HttpResponse response
            = httpPipeline.sendSync(new HttpRequest(expectedHttpMethod, expectedUrl), Context.NONE);
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void boundRequestFailsBeforeSendingWhenClientCantBind() {
        AtomicInteger sendCount = new AtomicInteger();
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                sendCount.incrementAndGet();
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).build();
        Context context = Contexts.empty().setClientCertificateBinding(BINDING).getContext();

        StepVerifier.create(httpPipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com"), context))
            .verifyError(IllegalStateException.class);
        assertThrows(IllegalStateException.class,
            () -> httpPipeline.sendSync(new HttpRequest(HttpMethod.GET, "https://example.com"), context));
        assertEquals(0, sendCount.get());
    }

    @Test
    public void clearedBindingIsSentByClientThatCantBind() {
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(new MockHttpResponse(request, 200));
            }
        }).build();
        Context context
            = Contexts.empty().setClientCertificateBinding(BINDING).setClientCertificateBinding(null).getContext();

        StepVerifier.create(httpPipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com"), context))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void boundRequestReachesClientThatCanBind() {
        List<ClientCertificateBinding> received = new CopyOnWriteArrayList<>();
        HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return send(request, Context.NONE);
            }

            @Override
            public Mono<HttpResponse> send(HttpRequest request, Context context) {
                received.add(Contexts.with(context).getClientCertificateBinding());
                return Mono.just(new MockHttpResponse(request, 200));
            }

            @Override
            public boolean supportsClientCertificateBinding() {
                return true;
            }
        }).build();
        Context context = Contexts.empty().setClientCertificateBinding(BINDING).getContext();

        StepVerifier.create(httpPipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com"), context))
            .assertNext(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
        assertEquals(200,
            httpPipeline.sendSync(new HttpRequest(HttpMethod.GET, "https://example.com"), context).getStatusCode());
        assertEquals(Arrays.asList(BINDING, BINDING), received);
    }
}
