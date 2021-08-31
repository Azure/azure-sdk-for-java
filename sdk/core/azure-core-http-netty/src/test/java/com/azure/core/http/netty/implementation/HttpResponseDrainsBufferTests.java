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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that closing the {@link HttpResponse} drains the network buffers.
 * <p>
 * These tests are isolated from other {@link HttpResponse} tests as they require running the garbage collector to force
 * the JVM to destroy buffers that no longer have pointers to them.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class HttpResponseDrainsBufferTests {
    private static ResourceLeakDetector.Level originalLevel;
    private static WireMockServer wireMockServer;

    private static final String LONG_BODY_PATH = "/long";
    private static final byte[] LONG_BODY = new byte[16 * 1024 * 1024]; // 16 MB

    static {
        new SecureRandom().nextBytes(LONG_BODY);
    }

    @BeforeAll
    public static void setupMockServer() {
        originalLevel = ResourceLeakDetector.getLevel();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);

        wireMockServer = new WireMockServer(wireMockConfig()
            .dynamicPort()
            .disableRequestJournal()
            .gzipDisabled(true));

        wireMockServer.stubFor(get(LONG_BODY_PATH).willReturn(aResponse().withBody(LONG_BODY)));
        wireMockServer.start();
    }

    @AfterAll
    public static void tearDownMockServer() {
        ResourceLeakDetector.setLevel(originalLevel);
        if (wireMockServer != null) {
            wireMockServer.shutdown();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void closeHttpResponseBeforeConsumingBody() throws InterruptedException {
        Collection<TestResourceLeakDetector<?>> leakDetectors = new ConcurrentLinkedDeque<>();
        ResourceLeakDetectorFactory.setResourceLeakDetectorFactory(new ResourceLeakDetectorFactory() {
            @Override
            public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval,
                long maxActive) {
                TestResourceLeakDetector<T> leakDetector = new TestResourceLeakDetector<>(resource, samplingInterval,
                    maxActive);
                leakDetectors.add(leakDetector);
                return leakDetector;
            }
        });

        HttpClient httpClient = new NettyAsyncHttpClientProvider().createInstance();

        StepVerifier.create(httpClient.send(new HttpRequest(HttpMethod.GET, url(wireMockServer)))
            .flatMap(response -> {
                assertNotNull(response.getHeaders());
                return Mono.fromRunnable(response::close);
            }))
            .verifyComplete();

        // GC twice to ensure full cleanup.
        Thread.sleep(2000);
        Runtime.getRuntime().gc();

        Thread.sleep(2000);
        Runtime.getRuntime().gc();

        assertEquals(0, leakDetectors.stream().mapToInt(TestResourceLeakDetector::getReportedLeakCount).sum());
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
            super.reportTracedLeak(resourceType, records);
        }

        @Override
        protected void reportUntracedLeak(String resourceType) {
            super.reportUntracedLeak(resourceType);
        }

        public int getReportedLeakCount() {
            return reportTracedLeakCount.get() + reportUntracedLeakCount.get();
        }
    }

    private static URL url(WireMockServer server) {
        try {
            return new URL("http://localhost:" + server.port() + LONG_BODY_PATH);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
