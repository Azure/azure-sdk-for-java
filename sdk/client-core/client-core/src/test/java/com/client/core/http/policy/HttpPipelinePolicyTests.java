// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.client.core.http.policy;

import com.client.core.http.HttpClient;
import com.client.core.http.HttpMethod;
import com.client.core.http.HttpPipeline;
import com.client.core.http.HttpPipelineBuilder;
import com.client.core.http.HttpPipelineCallContext;
import com.client.core.http.HttpPipelineNextPolicy;
import com.client.core.http.HttpPipelineNextSyncPolicy;
import com.client.core.http.HttpRequest;
import com.client.core.http.HttpResponse;
import com.client.core.http.clients.NoOpHttpClient;
import com.client.core.util.Context;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.client.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpPipelinePolicyTests {

    @Test
    public void verifySend() throws Exception {
        SyncAsyncPolicy policy1 = new SyncAsyncPolicy();
        SyncAsyncPolicy policy2 = new SyncAsyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, policy2)
            .build();

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

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, policy2)
            .build();


        pipeline.sendSync(new HttpRequest(HttpMethod.GET, url), Context.NONE);
        assertEquals(0, policy1.asyncCalls.get());
        assertEquals(1, policy1.syncCalls.get());
        assertEquals(0, policy2.asyncCalls.get());
        assertEquals(1, policy2.syncCalls.get());
    }

    @Test
    public void defaultImplementationShouldCallRightStack() throws Exception {
        DefaultImplementationSyncPolicy policyWithDefaultSyncImplementation = new DefaultImplementationSyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
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
        HttpPipelinePolicy badPolicy1 = (context, next) -> Mono.delay(Duration.ofMillis(10))
            .flatMap(l -> next.process());
        HttpPipelinePolicy badPolicy2 = (context, next) -> Mono.delay(Duration.ofMillis(10))
            .flatMap(l -> next.process());
        HttpClient badClient = request -> Mono.delay(Duration.ofMillis(10)).flatMap(i -> Mono.empty());
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(badClient)
            .policies(policy1, badPolicy1, badPolicy2)
            .build();

        pipeline.sendSync(new HttpRequest(HttpMethod.GET, url), Context.NONE);
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
        public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
            syncCalls.incrementAndGet();
            return next.processSync();
        }
    }

    private static class DefaultImplementationSyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger asyncCalls = new AtomicInteger();
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            asyncCalls.incrementAndGet();
            return next.process();
        }
    }
}
