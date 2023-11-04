// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.policy;

import com.generic.core.http.NoOpHttpClient;
import com.generic.core.http.client.HttpClient;
import com.generic.core.http.models.HttpMethod;
import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.http.pipeline.HttpPipeline;
import com.generic.core.http.pipeline.HttpPipelineBuilder;
import com.generic.core.http.pipeline.HttpPipelineCallContext;
import com.generic.core.http.pipeline.HttpPipelineNextPolicy;
import com.generic.core.http.pipeline.HttpPipelinePolicy;
import com.generic.core.models.Context;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.generic.core.CoreTestUtils.createUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpPipelinePolicyTests {

    @Test
    public void verifySend() throws Exception {
        SyncPolicy policy1 = new SyncPolicy();
        SyncPolicy policy2 = new SyncPolicy();
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(new NoOpHttpClient())
            .policies(policy1, policy2)
            .build();


        pipeline.send(new HttpRequest(HttpMethod.GET, url, null), Context.NONE);
        assertEquals(1, policy1.syncCalls.get());
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

        pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.NONE);
        assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
        assertEquals(1, policyWithDefaultSyncImplementation.syncCalls.get());
    }

    /**
     * This is to cover case when reactor could complain about blocking on non blocking thread.
     *
     * @throws MalformedURLException ignored.
     */
    @Test
    public void doesntThrowThatThreadIsNonBlocking() throws MalformedURLException {
        SyncPolicy policy1 = new SyncPolicy();
        HttpPipelinePolicy badPolicy1 = (context, next) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return next.process();
        };
        HttpPipelinePolicy badPolicy2 = (context, next) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return next.process();
        };

        HttpClient badClient = (request, context) -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
        URL url = createUrl("http://localhost/");

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(badClient)
            .policies(policy1, badPolicy1, badPolicy2)
            .build();

        pipeline.send(new HttpRequest(HttpMethod.GET, url), Context.NONE);
    }


    private static class SyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();
            return next.process();
        }
    }

    private static class DefaultImplementationSyncPolicy implements HttpPipelinePolicy {
        final AtomicInteger syncCalls = new AtomicInteger();

        @Override
        public HttpResponse process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            syncCalls.incrementAndGet();
            return next.process();
        }
    }
}
