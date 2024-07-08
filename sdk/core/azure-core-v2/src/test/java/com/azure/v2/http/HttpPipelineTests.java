// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.http;

import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.http.pipeline.PortPolicy;
import io.clientcore.core.http.pipeline.ProtocolPolicy;
import io.clientcore.core.http.pipeline.RequestIdPolicy;
import io.clientcore.core.http.pipeline.HttpRetryPolicy;
import io.clientcore.core.http.pipeline.UserAgentPolicy;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpPipelineTests {
    @Test
    public void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
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
                public Response<?>> send(HttpRequest request) {
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
                public Response<?>> send(HttpRequest request) {
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
            public Response<?>> send(HttpRequest request) {
                assertEquals(0, request.getHeaders().getSize());
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return new MockHttpResponse(request, 200));
            }
        }).build();

        final Response<?> response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
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
            public Response<?>> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                assertEquals(expectedUserAgent, request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline
            = new HttpPipelineBuilder().httpClient(httpClient).policies(new UserAgentPolicy(expectedUserAgent)).build();

        final Response<?> response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void withRequestIdRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com/1");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                final String requestId = request.getHeaders().getValue(HttpHeaderName.X_MS_CLIENT_REQUEST_ID);
                assertNotNull(requestId);
                assertFalse(requestId.isEmpty());

                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return new MockHttpResponse(request, 200));
            }
        }).policies(new RequestIdPolicy()).build();

        final Response<?> response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
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
            public Response<?>> send(HttpRequest request) {
                assertEquals(1, request.getHeaders().getSize());
                assertEquals(expectedUserAgent, request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(httpClient)
            .policies((new UserAgentPolicy(expectedUserAgent)))
            .build();

        final Response<?> response
            = httppipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl), Context.none());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void sendSyncWithPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = createUrl("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient() {
            @Override
            public Response<?>> send(HttpRequest request) {
                assertEquals(0, request.getHeaders().getSize());
                assertEquals(expectedHttpMethod, request.getHttpMethod());
                assertEquals(expectedUrl, request.getUrl());
                return new MockHttpResponse(request, 200));
            }
        }).build();

        final Response<?> response
            = httppipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl), Context.none());
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
    }
}
