// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.polling;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.management.implementation.polling.FooWithProvisioningState;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SyncPoller continuation token support.
 */
@SuppressWarnings("deprecation")
public class SyncPollerContinuationTokenTest {

    private static final SerializerAdapter SERIALIZER = SerializerFactory.createDefaultManagementSerializerAdapter();
    private static final Duration POLL_INTERVAL = Duration.ofMillis(50);

    @Test
    public void testSerializeAndResumeContinuationToken() throws Exception {
        // Arrange - create a mock HTTP client that simulates an LRO
        AtomicInteger callCount = new AtomicInteger(0);
        String asyncOpUrl
            = "https://management.azure.com/subscriptions/sub1/providers/Microsoft.Compute/locations/westus/operations/op1";

        HttpClient mockHttpClient = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.fromCallable(() -> createMockResponse(request, callCount));
            }
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(mockHttpClient).build();

        // Create initial LRO response
        Response<BinaryData> initialResponse = createInitialResponse(asyncOpUrl);

        // Act - create poller and poll once
        SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> originalPoller
            = SyncPollerFactory.create(SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL, () -> initialResponse, Context.NONE);

        // Poll once to update state
        PollResponse<PollResult<FooWithProvisioningState>> firstPoll = originalPoller.poll();
        assertEquals(LongRunningOperationStatus.IN_PROGRESS, firstPoll.getStatus(),
            "First poll should show IN_PROGRESS");

        // Serialize to continuation token
        String token = originalPoller.serializeContinuationToken();
        assertNotNull(token, "Continuation token should not be null");
        assertFalse(token.isEmpty(), "Continuation token should not be empty");

        // Act - resume from token in a "new" poller
        SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> resumedPoller
            = SyncPollerFactory.resumeFromToken(token, SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL, Context.NONE);

        // Poll the resumed poller
        PollResponse<PollResult<FooWithProvisioningState>> resumedPoll = resumedPoller.poll();
        assertNotNull(resumedPoll, "Resumed poll should not be null");

        // Continue polling until completion
        PollResponse<PollResult<FooWithProvisioningState>> finalResponse = resumedPoller.waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, finalResponse.getStatus(),
            "Polling should complete successfully");

        // Verify we can get poll result (note: final result would require a GET to the resource URL, which we're not mocking)
        assertNotNull(finalResponse.getValue(), "Final poll result should not be null");
    }

    @Test
    public void testContinuationTokenMultipleResumes() throws Exception {
        // Arrange
        AtomicInteger callCount = new AtomicInteger(0);
        String asyncOpUrl
            = "https://management.azure.com/subscriptions/sub1/providers/Microsoft.Storage/locations/westus/operations/op2";

        HttpClient mockHttpClient = request -> Mono.fromCallable(() -> createMockResponse(request, callCount));
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(mockHttpClient).build();
        Response<BinaryData> initialResponse = createInitialResponse(asyncOpUrl);

        // Create original poller
        SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> poller1
            = SyncPollerFactory.create(SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL, () -> initialResponse, Context.NONE);

        poller1.poll(); // Poll once
        String token1 = poller1.serializeContinuationToken();

        // Resume from token1
        SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> poller2
            = SyncPollerFactory.resumeFromToken(token1, SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL, Context.NONE);

        poller2.poll(); // Poll once more
        String token2 = poller2.serializeContinuationToken();

        // Resume from token2 (second resume)
        SyncPoller<PollResult<FooWithProvisioningState>, FooWithProvisioningState> poller3
            = SyncPollerFactory.resumeFromToken(token2, SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL, Context.NONE);

        // Final polling
        PollResponse<PollResult<FooWithProvisioningState>> finalResponse = poller3.waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, finalResponse.getStatus(),
            "Multiple resumes should still lead to completion");
    }

    @Test
    public void testContinuationTokenWithInvalidToken() {
        // Arrange
        HttpPipeline pipeline = new HttpPipelineBuilder().build();
        String invalidToken = "invalid-base64-token!@#";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            SyncPollerFactory.resumeFromToken(invalidToken, SERIALIZER, pipeline, FooWithProvisioningState.class,
                FooWithProvisioningState.class, POLL_INTERVAL);
        }, "Should throw exception for invalid token");
    }

    // Helper method to create initial LRO response
    private Response<BinaryData> createInitialResponse(String asyncOpUrl) throws IOException {
        FooWithProvisioningState foo = new FooWithProvisioningState("Creating");

        String responseBody = SERIALIZER.serialize(foo, SerializerEncoding.JSON);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaderName.AZURE_ASYNCOPERATION, asyncOpUrl);
        headers.set(HttpHeaderName.RETRY_AFTER, "1");

        HttpRequest request = new HttpRequest(HttpMethod.PUT,
            "https://management.azure.com/subscriptions/sub1/resourceGroups/rg1/providers/Microsoft.Compute/vms/vm1");

        return new SimpleResponse<>(request, 201, headers, BinaryData.fromString(responseBody));
    }

    // Helper method to create mock HTTP responses for polling
    private HttpResponse createMockResponse(HttpRequest request, AtomicInteger callCount) throws IOException {
        int count = callCount.incrementAndGet();

        // Simulate polling progression: Creating -> Updating -> Succeeded
        String provisioningState;
        int statusCode = 200;

        if (count <= 2) {
            provisioningState = "InProgress";
        } else if (count <= 4) {
            provisioningState = "Updating";
        } else {
            provisioningState = "Succeeded";
        }

        String responseBody = String.format("{\"status\":\"%s\"}", provisioningState);

        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                if (HttpHeaderName.RETRY_AFTER.getCaseSensitiveName().equalsIgnoreCase(name)) {
                    return "1";
                }
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaderName.RETRY_AFTER, "1");
                return headers;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(ByteBuffer.wrap(responseBody.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(responseBody.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(responseBody);
            }

            @Override
            public Mono<String> getBodyAsString(java.nio.charset.Charset charset) {
                return Mono.just(responseBody);
            }

            @Override
            public BinaryData getBodyAsBinaryData() {
                return BinaryData.fromString(responseBody);
            }
        };
    }
}
