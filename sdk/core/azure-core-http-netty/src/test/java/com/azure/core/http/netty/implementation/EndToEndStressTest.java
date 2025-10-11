// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.http.policy.RetryPolicy;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.core.http.netty.implementation.NettyHttpClientLocalTestServer.SHOULD_THROTTLE;
import static com.azure.core.http.netty.implementation.NettyHttpClientLocalTestServer.STRESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * End-to-end stress test for 429 and 503 retries to ensure no resource leaks.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class EndToEndStressTest {
    private static ResourceLeakDetector.Level originalLevel;
    private static final String URL = NettyHttpClientLocalTestServer.getServer().getHttpUri() + STRESS;

    private ResourceLeakDetectorFactory originalLeakDetectorFactory;
    private final TestResourceLeakDetectorFactory resourceLeakDetectorFactory = new TestResourceLeakDetectorFactory();

    @BeforeAll
    public static void startTestServer() {
        originalLevel = ResourceLeakDetector.getLevel();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
    }

    @BeforeEach
    public void setupLeakDetectorFactory() {
        originalLeakDetectorFactory = ResourceLeakDetectorFactory.instance();
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(resourceLeakDetectorFactory);
    }

    @AfterEach
    public void resetLeakDetectorFactory() {
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(originalLeakDetectorFactory);
    }

    @AfterAll
    public static void stopTestServer() {
        ResourceLeakDetector.setLevel(originalLevel);
    }

    @Test
    public void stressServerThrottlingWithRetry() {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(new NettyAsyncHttpClientProvider().createInstance())
                .policies(new RetryPolicy(new RetryOptions(new FixedDelayOptions(3, Duration.ofMillis(10)))),
                    new ShouldThrottlePipelinePolicy())
                .build();

        Mono<Void> requestMaker = Flux.generate(() -> 0, (callCount, sink) -> {
            if (callCount == 500) {
                sink.complete();
                return callCount;
            }

            sink.next(callCount);
            return callCount + 1;
        })
            .concatMap(ignored -> pipeline.send(new HttpRequest(HttpMethod.GET, URL))
                .flatMap(response -> Mono.fromRunnable(response::close)))
            .parallel(10)
            .runOn(Schedulers.boundedElastic())
            .then();

        StepVerifier.create(requestMaker).verifyComplete();

        try {
            // GC twice to ensure full cleanup.
            Thread.sleep(1000);
            Runtime.getRuntime().gc();

            Thread.sleep(1000);
            Runtime.getRuntime().gc();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        assertEquals(0, resourceLeakDetectorFactory.getTotalReportedLeakCount());
    }

    private static class ShouldThrottlePipelinePolicy implements HttpPipelinePolicy {
        private final AtomicInteger totalCallCount = new AtomicInteger();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
            if (totalCallCount.getAndIncrement() <= 1000) {
                // Only throttle the first 1000 calls randomly to allow the test to complete in a reasonable time.
                context.getHttpRequest().getHeaders().set(SHOULD_THROTTLE, (Math.random() > 0.5) ? "true" : "false");
            }

            return next.process();
        }
    }
}
