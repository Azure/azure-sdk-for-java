// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.clients.NoOpHttpClient;
import com.azure.core.test.junit.extensions.SyncAsyncExtension;
import com.azure.core.test.junit.extensions.annotation.SyncAsyncTest;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class HttpPipelinePolicyTests {

    @SyncAsyncTest
    public void shouldCallSyncOrAsync() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        URL url = new URL("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, policy2)
            .build();

        SyncAsyncExtension.execute(
            () -> {
                pipeline.sendSynchronously(new HttpRequest(HttpMethod.GET, url));
                assertEquals(0, policy1.asyncCalls.get());
                assertEquals(1, policy1.syncCalls.get());
                assertEquals(0, policy2.asyncCalls.get());
                assertEquals(1, policy2.syncCalls.get());
            },
            () -> {
                pipeline.send(new HttpRequest(HttpMethod.GET, url)).block();
                assertEquals(1, policy1.asyncCalls.get());
                assertEquals(0, policy1.syncCalls.get());
                assertEquals(1, policy2.asyncCalls.get());
                assertEquals(0, policy2.syncCalls.get());
            }
        );
    }

    @SyncAsyncTest
    public void defaultImplementationShouldCallRightStack() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        HttpPipelinePolicy policyWithDefaultSyncImplementation = (context, next) -> next.process();
        URL url = new URL("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, policyWithDefaultSyncImplementation, policy2)
            .build();

        SyncAsyncExtension.execute(
            () -> {
                pipeline.sendSynchronously(new HttpRequest(HttpMethod.GET, url));
                assertEquals(0, policy1.asyncCalls.get());
                assertEquals(1, policy1.syncCalls.get());
                assertEquals(0, policy2.asyncCalls.get());
                assertEquals(1, policy2.syncCalls.get());
            },
            () -> {
                pipeline.send(new HttpRequest(HttpMethod.GET, url)).block();
                assertEquals(1, policy1.asyncCalls.get());
                assertEquals(0, policy1.syncCalls.get());
                assertEquals(1, policy2.asyncCalls.get());
                assertEquals(0, policy2.syncCalls.get());
            }
        );
    }

    @Test
    public void throwsIfAsyncPolicyCallsIntoSyncInAsyncContext() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        HttpPipelinePolicy badPolicy = (context, next) -> Mono.fromCallable(next::processSynchronously);
        URL url = new URL("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, badPolicy, policy2)
            .build();

        assertThrows(IllegalStateException.class, () -> pipeline.send(new HttpRequest(HttpMethod.GET, url)).block());
    }


    private static class SyncAsyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger asyncCalls = new AtomicInteger();
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            asyncCalls.incrementAndGet();
            return next.process();
        }

        @Override
        public HttpResponse processSynchronously(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();
            return next.processSynchronously();
        }
    }
}
