package com.microsoft.rest.v3.http;

import com.microsoft.rest.v3.http.policy.PortPolicy;
import com.microsoft.rest.v3.http.policy.ProtocolPolicy;
import com.microsoft.rest.v3.http.policy.RequestIdPolicy;
import com.microsoft.rest.v3.http.policy.RetryPolicy;
import com.microsoft.rest.v3.http.policy.UserAgentPolicy;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class HttpPipelineTests {
    @Test
    public void constructorWithNoArguments() {
        HttpPipeline pipeline = new HttpPipeline();
        assertEquals(0, pipeline.pipelinePolicies().length);
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withRequestPolicy() {
        HttpPipeline pipeline = new HttpPipeline(new PortPolicy(80),
                new ProtocolPolicy("ftp"),
                new RetryPolicy());

        assertEquals(3, pipeline.pipelinePolicies().length);
        assertEquals(PortPolicy.class, pipeline.pipelinePolicies()[0].getClass());
        assertEquals(ProtocolPolicy.class, pipeline.pipelinePolicies()[1].getClass());
        assertEquals(RetryPolicy.class, pipeline.pipelinePolicies()[2].getClass());
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withRequestOptions() throws MalformedURLException {
        HttpPipeline pipeline = new HttpPipeline(new HttpPipelineOptions(null),
                new PortPolicy(80),
                new ProtocolPolicy("ftp"),
                new RetryPolicy());

        HttpPipelineCallContext context = pipeline.newContext(new HttpRequest(HttpMethod.GET, new URL("http://foo.com")));
        assertNotNull(context);
        assertNotNull(context.requestPolicyOptions());
        assertNotNull(pipeline.httpClient());
        assertTrue(pipeline.httpClient() instanceof ReactorNettyClient);
    }

    @Test
    public void withNoRequestPolicies() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com");
        final HttpPipeline httpPipeline = new HttpPipeline(new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(0, request.headers().size());
                assertEquals(expectedHttpMethod, request.httpMethod());
                assertEquals(expectedUrl, request.url());
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
            }
        }, new HttpPipelineOptions(null));

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void withUserAgentRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com/1");
        final String expectedUserAgent = "my-user-agent";
        final HttpClient httpClient = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                assertEquals(1, request.headers().size());
                assertEquals(expectedUserAgent, request.headers().value("User-Agent"));
                assertEquals(expectedHttpMethod, request.httpMethod());
                assertEquals(expectedUrl, request.url());
                return Mono.<HttpResponse>just(new MockHttpResponse(request, 200));
            }
        };

        final HttpPipeline httpPipeline = new HttpPipeline(httpClient,
                new HttpPipelineOptions(null),
                new UserAgentPolicy(expectedUserAgent));

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }

    @Test
    public void withRequestIdRequestPolicy() throws MalformedURLException {
        final HttpMethod expectedHttpMethod = HttpMethod.GET;
        final URL expectedUrl = new URL("http://my.site.com/1");
        final HttpPipeline httpPipeline = new HttpPipeline(new HttpClient() {
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
            },
            new HttpPipelineOptions(null),
            new RequestIdPolicy());

        final HttpResponse response = httpPipeline.send(new HttpRequest(expectedHttpMethod, expectedUrl)).block();
        assertNotNull(response);
        assertEquals(200, response.statusCode());
    }
}
