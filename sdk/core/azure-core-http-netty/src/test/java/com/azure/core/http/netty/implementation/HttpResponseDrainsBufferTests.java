// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.github.tomakehurst.wiremock.WireMockServer;
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

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that closing the {@link HttpResponse} drains the network buffers.
 * <p>
 * These tests are isolated from other {@link HttpResponse} tests as they require running the garbage collector to force
 * the JVM to destroy buffers that no longer have pointers to them.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class HttpResponseDrainsBufferTests {
    private static final String LONG_BODY_PATH = "/long";
    private static final byte[] LONG_BODY = new byte[4 * 1024 * 1024]; // 4 MB

    private static ResourceLeakDetector.Level originalLevel;
    private static WireMockServer wireMockServer;
    private static String url;

    static {
        new SecureRandom().nextBytes(LONG_BODY);
    }

    private ResourceLeakDetectorFactory originalLeakDetectorFactory;
    private final TestResourceLeakDetectorFactory testResourceLeakDetectorFactory =
        new TestResourceLeakDetectorFactory();

    @BeforeAll
    public static void setupMockServer() {
        originalLevel = ResourceLeakDetector.getLevel();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        wireMockServer = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .disableRequestJournal()
            .asynchronousResponseEnabled(true)
            .gzipDisabled(true));

        wireMockServer.stubFor(get(LONG_BODY_PATH).willReturn(aResponse().withBody(LONG_BODY)));
        wireMockServer.start();

        url = wireMockServer.baseUrl() + LONG_BODY_PATH;
    }

    @BeforeEach
    public void setupLeakDetectorFactory() {
        originalLeakDetectorFactory = ResourceLeakDetectorFactory.instance();
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(testResourceLeakDetectorFactory);
    }

    @AfterEach
    public void resetLeakDetectorFactory() {
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(originalLeakDetectorFactory);
    }

    @AfterAll
    public static void tearDownMockServer() {
        ResourceLeakDetector.setLevel(originalLevel);
        if (wireMockServer != null) {
            wireMockServer.shutdown();
        }
    }

    @Test
    public void closeHttpResponseWithoutConsumingBody() {
        runScenario(response -> Mono.fromRunnable(response::close));
    }

    @Test
    public void closeHttpResponseWithConsumingPartialBody() {
        runScenario(response -> response.getBody().next().flatMap(ignored -> Mono.fromRunnable(response::close)));
    }

    @Test
    public void closeHttpResponseWithConsumingFullBody() {
        runScenario(response -> response.getBodyAsByteArray().flatMap(ignored -> Mono.fromRunnable(response::close)));
    }

    private void runScenario(Function<HttpResponse, Mono<Void>> responseConsumer) {
        HttpClient httpClient = new NettyAsyncHttpClientProvider().createInstance();
        Mono<Void> requestMaker = Flux.generate(() -> 0, (callCount, sink) -> {
            if (callCount == 100) {
                sink.complete();
                return callCount;
            }

            sink.next(callCount);
            return callCount + 1;
        }).concatMap(ignored -> httpClient.send(new HttpRequest(HttpMethod.GET, url)).flatMap(responseConsumer))
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

        assertEquals(0, testResourceLeakDetectorFactory.getTotalReportedLeakCount());
    }

    @Test
    public void closingHttpResponseIsIdempotent() {
        HttpClient httpClient = new NettyAsyncHttpClientProvider().createInstance();
        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, url))
                .flatMap(response -> Mono.fromRunnable(response::close).thenReturn(response))
                .delayElement(Duration.ofSeconds(1))
                .flatMap(response -> Mono.fromRunnable(response::close))
                .delayElement(Duration.ofSeconds(1))
                .then())
            .verifyComplete();
    }

    private static final class TestResourceLeakDetectorFactory extends ResourceLeakDetectorFactory {
        private final Collection<TestResourceLeakDetector<?>> createdDetectors = new ConcurrentLinkedDeque<>();

        @Override
        @SuppressWarnings("deprecation") // API is deprecated but abstract
        public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval,
            long maxActive) {
            TestResourceLeakDetector<T> leakDetector = new TestResourceLeakDetector<>(resource, samplingInterval,
                maxActive);
            createdDetectors.add(leakDetector);
            return leakDetector;
        }

        public int getTotalReportedLeakCount() {
            return createdDetectors.stream().mapToInt(TestResourceLeakDetector::getReportedLeakCount).sum();
        }
    }

    @SuppressWarnings("deprecation")
    private static final class TestResourceLeakDetector<T> extends ResourceLeakDetector<T> {
        private final AtomicInteger reportTracedLeakCount = new AtomicInteger();
        private final AtomicInteger reportUntracedLeakCount = new AtomicInteger();

        TestResourceLeakDetector(Class<T> resource, int samplingInterval, long maxActive) {
            super(resource, samplingInterval, maxActive);
        }

        @Override
        protected void reportTracedLeak(String resourceType, String records) {
            reportTracedLeakCount.incrementAndGet();
            super.reportTracedLeak(resourceType, records);
        }

        @Override
        protected void reportUntracedLeak(String resourceType) {
            reportUntracedLeakCount.incrementAndGet();
            super.reportUntracedLeak(resourceType);
        }

        public int getReportedLeakCount() {
            return reportTracedLeakCount.get() + reportUntracedLeakCount.get();
        }
    }
}
