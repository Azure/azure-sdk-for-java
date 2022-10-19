// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.trace.sleuth;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.CustomerProvidedKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SleuthHttpPolicyTests {

    @Mock
    private Tracer tracer;

    @Mock
    private HttpPipelineCallContext httpPipelineCallContext;

    @Mock
    private HttpPipelineNextPolicy httpPipelineNextPolicy;

    private MockHttpResponse successResponse = spy(new MockHttpResponse(mock(HttpRequest.class), 200));

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void addPolicyForBlobServiceClientBuilder() {
        SleuthHttpPolicy sleuthHttpPolicy = new SleuthHttpPolicy(tracer);
        // key is test-key
        CustomerProvidedKey providedKey = new CustomerProvidedKey("dGVzdC1rZXk=");
        TokenCredential tokenCredential = new ClientSecretCredentialBuilder()
            .clientSecret("dummy-secret")
            .clientId("dummy-client-id")
            .tenantId("dummy-tenant-id")
            .build();
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
            .customerProvidedKey(providedKey)
            .credential(tokenCredential)
            .addPolicy(sleuthHttpPolicy)
            .endpoint("https://test.blob.core.windows.net/")
            .buildClient();

        HttpPipeline pipeline = blobServiceClient.getHttpPipeline();
        assertEquals(10, pipeline.getPolicyCount());
        assertEquals(SleuthHttpPolicy.class, pipeline.getPolicy(6).getClass());
    }

    @Test
    void processWhenDisableTracingKey() {
        SleuthHttpPolicy sleuthHttpPolicy = spy(new SleuthHttpPolicy(tracer));
        when(httpPipelineCallContext.getData(com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY))
            .thenReturn(Optional.of(true));
        sleuthHttpPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy);
        verify(httpPipelineCallContext, times(1))
            .getData(com.azure.core.util.tracing.Tracer.DISABLE_TRACING_KEY);
        verify(httpPipelineCallContext, times(0))
            .getData(com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY);
    }

    @Test
    void processAndSetParent() throws MalformedURLException {
        SleuthHttpPolicy sleuthHttpPolicy = spy(new SleuthHttpPolicy(tracer));
        Span parentSpan = mock(Span.class);
        Span.Builder spanBuilder = mock(Span.Builder.class);
        HttpRequest request = mock(HttpRequest.class);
        commonProcess(parentSpan, spanBuilder, request);

        Span span = mock(Span.class);
        when(spanBuilder.start()).thenReturn(span);
        when(span.isNoop()).thenReturn(true);

        when(httpPipelineNextPolicy.process()).thenReturn(Mono.just(successResponse));
        sleuthHttpPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy);
        verify(spanBuilder, times(1)).setParent(any());
        verify(spanBuilder, times(1)).kind(Span.Kind.CLIENT);
    }

    @Test
    void processAndPutTag() throws MalformedURLException {
        SleuthHttpPolicy sleuthHttpPolicy = spy(new SleuthHttpPolicy(tracer));
        Span parentSpan = mock(Span.class);
        Span.Builder spanBuilder = mock(Span.Builder.class);
        HttpRequest request = mock(HttpRequest.class);
        commonProcess(parentSpan, spanBuilder, request);

        Span span = mock(Span.class);
        when(spanBuilder.start()).thenReturn(span);

        when(httpPipelineNextPolicy.process()).thenReturn(Mono.just(successResponse));

        setSpanHeaders(request);

        sleuthHttpPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy);
        verify(span, times(3)).tag(any(), any());
    }

    private void setSpanHeaders(HttpRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("User-Agent", "test");
        headers.add("x-ms-request-id", "test-request-id");
        when(request.getHeaders()).thenReturn(headers);
        when(request.getHttpMethod()).thenReturn(HttpMethod.POST);
    }

    @Test
    void processAndHandleResponse() throws MalformedURLException {
        SleuthHttpPolicy sleuthHttpPolicy = spy(new SleuthHttpPolicy(tracer));
        Span parentSpan = mock(Span.class);
        Span.Builder spanBuilder = mock(Span.Builder.class);
        HttpRequest request = mock(HttpRequest.class);
        commonProcess(parentSpan, spanBuilder, request);

        Span span = mock(Span.class);
        when(spanBuilder.start()).thenReturn(span);

        setSpanHeaders(request);
        when(httpPipelineNextPolicy.process()).thenReturn(Mono.just(successResponse));
        when(successResponse.getHeaderValue("x-ms-request-id")).thenReturn("test-request-id");

        sleuthHttpPolicy.process(httpPipelineCallContext, httpPipelineNextPolicy).block();
        verify(span, times(1)).end();
        verify(span, times(1)).tag("x-ms-request-id", "test-request-id");
        verify(span, times(1)).tag("http.status_code", "200");
    }

    private void commonProcess(Span parentSpan, Span.Builder spanBuilder, HttpRequest request) throws MalformedURLException {
        when(tracer.currentSpan()).thenReturn(parentSpan);
        when(tracer.spanBuilder()).thenReturn(spanBuilder);
        when(httpPipelineCallContext.getHttpRequest()).thenReturn(request);
        when(request.getUrl()).thenReturn(new URL("https://test.blob.core.windows.net/"));
        when(spanBuilder.name(anyString())).thenReturn(spanBuilder);
    }

    @Test
    void addAfterPolicyForHttpPipeline() {
        final HttpPipeline pipeline = createHttpPipeline();
        assertEquals(1, pipeline.getPolicyCount());
        assertEquals(SleuthHttpPolicy.class, pipeline.getPolicy(0).getClass());
    }

    private HttpPipeline createHttpPipeline() {
        final HttpClient httpClient = HttpClient.createDefault();
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new SleuthHttpPolicy(tracer));
        final HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .build();
        return httpPipeline;
    }
}
