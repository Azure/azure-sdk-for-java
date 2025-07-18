// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.atomic.AtomicInteger;

import static io.clientcore.core.http.pipeline.PipelineTestHelpers.sendRequest;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ParameterizedClass(name = "isAsync={0}")
@ValueSource(booleans = { false, true })
public class HttpPipelinePolicyTests {
    private final boolean isAsync;

    public HttpPipelinePolicyTests(boolean isAsync) {
        this.isAsync = isAsync;
    }

    @SuppressWarnings("try")
    @Test
    public void verifySend() {
        SyncPolicy policy1 = new SyncPolicy();
        SyncPolicy policy2 = new SyncPolicy();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> new Response<>(request, 200, new HttpHeaders(), null))
                .addPolicy(policy1)
                .addPolicy(policy2)
                .build();

        try (Response<BinaryData> ignored = sendRequest(pipeline, isAsync)) {
            assertEquals(1, policy1.syncCalls.get());
            assertEquals(1, policy2.syncCalls.get());
        }
    }

    @SuppressWarnings("try")
    @Test
    public void defaultImplementationShouldCallRightStack() {
        DefaultImplementationSyncPolicy policyWithDefaultSyncImplementation = new DefaultImplementationSyncPolicy();

        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(request -> new Response<>(request, 200, new HttpHeaders(), null))
                .addPolicy(policyWithDefaultSyncImplementation)
                .build();

        try (Response<BinaryData> ignored = sendRequest(pipeline, isAsync)) {
            assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
            assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
        }
    }

    /**
     * This is to cover case when reactor could complain about blocking on non-blocking thread.
     */
    @Test
    public void doesNotThrowThatThreadIsNonBlocking() {
        SyncPolicy policy1 = new SyncPolicy();
        HttpPipelinePolicy badPolicy1 = (ignored, next) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return next.process();
        };

        HttpPipelinePolicy badPolicy2 = (ignored, next) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            return next.process();
        };

        HttpClient badClient = (request) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new Response<>(request, 200, new HttpHeaders(), null);
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(badClient)
            .addPolicy(policy1)
            .addPolicy(badPolicy1)
            .addPolicy(badPolicy2)
            .build();

        assertDoesNotThrow(() -> sendRequest(pipeline, isAsync).close());
    }

    private static class SyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();

            return next.process();
        }
    }

    private static class DefaultImplementationSyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Response<BinaryData> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();

            return next.process();
        }
    }
}
