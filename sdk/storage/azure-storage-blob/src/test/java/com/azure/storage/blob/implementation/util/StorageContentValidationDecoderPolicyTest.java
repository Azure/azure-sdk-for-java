// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.storage.common.DownloadContentValidationOptions;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StorageContentValidationDecoderPolicy}.
 */
public class StorageContentValidationDecoderPolicyTest {

    @Test
    public void shouldNotApplyDecodingWhenContextKeyNotPresent() throws MalformedURLException {
        // Arrange
        StorageContentValidationDecoderPolicy policy = new StorageContentValidationDecoderPolicy();
        HttpPipelineCallContext context = createMockContext(null, null);
        HttpPipelineNextPolicy nextPolicy = createMockNextPolicy();

        // Act
        Mono<HttpResponse> result = policy.process(context, nextPolicy);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotApplyDecodingWhenContextKeyIsFalse() throws MalformedURLException {
        // Arrange
        StorageContentValidationDecoderPolicy policy = new StorageContentValidationDecoderPolicy();
        Context ctx = new Context(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, false);
        HttpPipelineCallContext context = createMockContext(ctx, null);
        HttpPipelineNextPolicy nextPolicy = createMockNextPolicy();

        // Act
        Mono<HttpResponse> result = policy.process(context, nextPolicy);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    @Test
    public void shouldApplyDecodingWhenContextKeyIsTrue() throws MalformedURLException {
        // Arrange
        StorageContentValidationDecoderPolicy policy = new StorageContentValidationDecoderPolicy();
        DownloadContentValidationOptions validationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
        
        Context ctx = new Context(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true)
            .addData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY, validationOptions);
        
        HttpPipelineCallContext context = createMockContext(ctx, 1024L);
        HttpPipelineNextPolicy nextPolicy = createMockNextPolicy();

        // Act
        Mono<HttpResponse> result = policy.process(context, nextPolicy);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                // Verify it's a DecodedResponse
                assertTrue(response instanceof StorageContentValidationDecoderPolicy.DecodedResponse);
            })
            .verifyComplete();
    }

    @Test
    public void shouldNotApplyDecodingForNonDownloadResponse() throws MalformedURLException {
        // Arrange
        StorageContentValidationDecoderPolicy policy = new StorageContentValidationDecoderPolicy();
        DownloadContentValidationOptions validationOptions = new DownloadContentValidationOptions()
            .setStructuredMessageValidationEnabled(true);
        
        Context ctx = new Context(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY, true)
            .addData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY, validationOptions);
        
        HttpPipelineCallContext context = createMockContext(ctx, null);
        // Create a non-GET request (POST)
        HttpRequest request = new HttpRequest(HttpMethod.POST, new URL("https://test.blob.core.windows.net/container/blob"));
        when(context.getHttpRequest()).thenReturn(request);
        
        HttpPipelineNextPolicy nextPolicy = mock(HttpPipelineNextPolicy.class);
        HttpResponse mockResponse = createMockHttpResponse(request, null);
        when(nextPolicy.process()).thenReturn(Mono.just(mockResponse));

        // Act
        Mono<HttpResponse> result = policy.process(context, nextPolicy);

        // Assert
        StepVerifier.create(result)
            .assertNext(response -> {
                assertNotNull(response);
                assertEquals(200, response.getStatusCode());
                // Should not be a DecodedResponse
                assertFalse(response instanceof StorageContentValidationDecoderPolicy.DecodedResponse);
            })
            .verifyComplete();
    }

    private HttpPipelineCallContext createMockContext(Context ctx, Long contentLength) throws MalformedURLException {
        HttpPipelineCallContext context = mock(HttpPipelineCallContext.class);
        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://test.blob.core.windows.net/container/blob"));
        
        when(context.getHttpRequest()).thenReturn(request);
        
        if (ctx != null) {
            when(context.getData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY))
                .thenReturn(ctx.getData(Constants.STRUCTURED_MESSAGE_DECODING_CONTEXT_KEY));
            when(context.getData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY))
                .thenReturn(ctx.getData(Constants.STRUCTURED_MESSAGE_VALIDATION_OPTIONS_CONTEXT_KEY));
        } else {
            when(context.getData(any())).thenReturn(java.util.Optional.empty());
        }
        
        return context;
    }

    private HttpPipelineNextPolicy createMockNextPolicy() throws MalformedURLException {
        return createMockNextPolicy(1024L);
    }

    private HttpPipelineNextPolicy createMockNextPolicy(Long contentLength) throws MalformedURLException {
        HttpPipelineNextPolicy nextPolicy = mock(HttpPipelineNextPolicy.class);
        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL("https://test.blob.core.windows.net/container/blob"));
        HttpResponse mockResponse = createMockHttpResponse(request, contentLength);
        when(nextPolicy.process()).thenReturn(Mono.just(mockResponse));
        return nextPolicy;
    }

    private HttpResponse createMockHttpResponse(HttpRequest request, Long contentLength) {
        HttpResponse response = mock(HttpResponse.class);
        HttpHeaders headers = new HttpHeaders();
        if (contentLength != null) {
            headers.set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(contentLength));
        }
        
        when(response.getRequest()).thenReturn(request);
        when(response.getStatusCode()).thenReturn(200);
        when(response.getHeaders()).thenReturn(headers);
        when(response.getHeaderValue(HttpHeaderName.CONTENT_LENGTH.toString()))
            .thenReturn(contentLength != null ? String.valueOf(contentLength) : null);
        
        // Mock body
        ByteBuffer buffer = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        Flux<ByteBuffer> body = Flux.just(buffer);
        when(response.getBody()).thenReturn(body);
        
        return response;
    }
}
