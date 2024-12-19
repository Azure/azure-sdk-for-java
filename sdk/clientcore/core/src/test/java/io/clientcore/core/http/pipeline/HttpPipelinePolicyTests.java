// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.NoOpHttpClient;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.HttpResponse;
import io.clientcore.core.http.models.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpPipelinePolicyTests {
    @Test
    public void verifySend() throws IOException {
        SyncPolicy policy1 = new SyncPolicy();
        SyncPolicy policy2 = new SyncPolicy();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .addPolicyBefore(policy1, HttpPipelinePosition.RETRY)
            .addPolicyBefore(policy2, HttpPipelinePosition.RETRY)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();

        assertEquals(1, policy1.syncCalls.get());
        assertEquals(1, policy2.syncCalls.get());
    }

    @Test
    public void defaultImplementationShouldCallRightStack() throws IOException {
        DefaultImplementationSyncPolicy policyWithDefaultSyncImplementation = new DefaultImplementationSyncPolicy();

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .addPolicyBefore(policyWithDefaultSyncImplementation, HttpPipelinePosition.RETRY)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();

        assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
        assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
    }

    /**
     * This is to cover case when reactor could complain about blocking on non-blocking thread.
     */
    @Test
    public void doesNotThrowThatThreadIsNonBlocking() throws IOException {
        SyncPolicy policy1 = new SyncPolicy();
        HttpPipelinePolicy badPolicy1 = new HttpPipelinePolicy() {
            @Override
            public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return next.process();
            }

            @Override
            public String getName() {
                return "badPolicy1";
            }
        };

        HttpPipelinePolicy badPolicy2 = new HttpPipelinePolicy() {
            @Override
            public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return next.process();
            }

            @Override
            public String getName() {
                return "badPolicy2";
            }
        };

        HttpClient badClient = (request) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return new HttpResponse<>(request, 200, new HttpHeaders(), null);
        };

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(badClient)
            .addPolicyBefore(policy1, HttpPipelinePosition.RETRY)
            .addPolicyBefore(badPolicy1, HttpPipelinePosition.RETRY)
            .addPolicyBefore(badPolicy2, HttpPipelinePosition.RETRY)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, "http://localhost/")).close();
    }

    private static class SyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();
        final String name = new String(new SecureRandom().generateSeed(24));

        @Override
        public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();

            return next.process();
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static class DefaultImplementationSyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Response<?> process(HttpRequest httpRequest, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();

            return next.process();
        }
    }
}
