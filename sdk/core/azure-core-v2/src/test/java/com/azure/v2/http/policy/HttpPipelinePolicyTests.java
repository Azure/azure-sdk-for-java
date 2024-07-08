// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.policy;

import io.clientcore.core.http.HttpClient;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import io.clientcore.core.http.pipeline.HttpPipelineNextPolicy;
import io.clientcore.core.http.pipeline.HttpPipelinePolicy;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.clients.NoOpHttpClient;
import io.clientcore.core.util.Context;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpPipelinePolicyTests {

    @Test
    public void verifySend() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).policies(policy1, policy2).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, url)).block();
        assertEquals(1, policy1.asyncCalls.get());
        assertEquals(0, policy1.syncCalls.get());
        assertEquals(1, policy2.asyncCalls.get());
        assertEquals(0, policy2.syncCalls.get());
    }

    @Test
    public void verifySendSync() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).policies(policy1, policy2).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.none());
        assertEquals(0, policy1.asyncCalls.get());
        assertEquals(1, policy1.syncCalls.get());
        assertEquals(0, policy2.asyncCalls.get());
        assertEquals(1, policy2.syncCalls.get());
    }

    @Test
    public void defaultImplementationShouldCallRightStack() throws Exception {
        DefaultImplementationSyncPolicy policyWithDefaultSyncImplementation = new DefaultImplementationSyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient())
            .policies(policyWithDefaultSyncImplementation)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, url)).block();
        assertEquals(1, policyWithDefaultSyncImplementation.asyncCalls.get());
        assertEquals(0, policyWithDefaultSyncImplementation.syncCalls.get());
        assertEquals(1, policyWithDefaultSyncImplementation.asyncCalls.get());
        assertEquals(0, policyWithDefaultSyncImplementation.syncCalls.get());
    }

    /**
     * This is to cover case when reactor could complain about blocking on non blocking thread.
     *
     * @throws MalformedURLException ignored.
     */
    @Test
    public void doesntThrowThatThreadIsNonBlocking() throws MalformedURLException {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        HttpPipelinePolicy badPolicy1
            = (context, next) -> Mono.delay(Duration.ofMillis(10)).flatMap(l -> next.process());
        HttpPipelinePolicy badPolicy2
            = (context, next) -> Mono.delay(Duration.ofMillis(10)).flatMap(l -> next.process());
        HttpClient badClient = request -> Mono.delay(Duration.ofMillis(10)).flatMap(i -> Mono.empty());
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(badClient).policies(policy1, badPolicy1, badPolicy2).build();

        pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.none());
    }

    private static class SyncAsyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger asyncCalls = new AtomicInteger();
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Response<?>> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            asyncCalls.incrementAndGet();
            return next.process();
        }

        @Override
        public Response<?> processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            syncCalls.incrementAndGet();
            return next.processSync();
        }
    }

    private static class DefaultImplementationSyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger asyncCalls = new AtomicInteger();
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Response<?>> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            asyncCalls.incrementAndGet();
            return next.process();
        }
    }
}
