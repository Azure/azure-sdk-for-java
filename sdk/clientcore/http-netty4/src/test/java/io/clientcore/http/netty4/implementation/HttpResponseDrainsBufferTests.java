// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.shared.LocalTestServer;
import io.clientcore.core.utils.IOExceptionCheckedConsumer;
import io.clientcore.core.utils.SharedExecutorService;
import io.clientcore.http.netty4.NettyHttpClientProvider;
import io.clientcore.http.netty4.TestUtils;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetectorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;

import javax.servlet.ServletException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.LONG_BODY;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.LONG_BODY_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests that closing the {@link Response} drains the network buffers.
 * <p>
 * These tests are isolated from other {@link Response} tests as they require running the garbage collector to force
 * the JVM to destroy buffers that no longer have pointers to them.
 */
@Timeout(value = 3, unit = TimeUnit.MINUTES)
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class HttpResponseDrainsBufferTests {
    private static ResourceLeakDetector.Level originalLevel;
    private static String url;
    private static LocalTestServer server;

    private ResourceLeakDetectorFactory originalLeakDetectorFactory;
    private final TestResourceLeakDetectorFactory testResourceLeakDetectorFactory
        = new TestResourceLeakDetectorFactory();

    @BeforeAll
    public static void startTestServer() {
        server = new LocalTestServer(HttpProtocolVersion.HTTP_1_1, false, (req, resp, requestBody) -> {
            if ("GET".equalsIgnoreCase(req.getMethod()) && LONG_BODY_PATH.equals(req.getServletPath())) {
                resp.setStatus(200);
                resp.setContentType("application/octet-stream");
                resp.setContentLength(LONG_BODY.length);
                try {
                    resp.getOutputStream().write(LONG_BODY);
                    resp.flushBuffer();
                } catch (IOException e) {
                    throw new ServletException(e);
                }
            } else {
                resp.sendError(404, "Endpoint not found.");
            }
        });

        server.start();
        url = server.getUri() + LONG_BODY_PATH;

        originalLevel = ResourceLeakDetector.getLevel();
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
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
    public static void stopTestServer() {
        if (server != null) {
            server.stop();
        }

        if (originalLevel != null) {
            ResourceLeakDetector.setLevel(originalLevel);
        }
    }

    @Test
    public void closeHttpResponseWithoutConsumingBody() {
        runScenario(Response::close);
    }

    @Test
    public void closeHttpResponseWithConsumingPartialBody() {
        runScenario(response -> {
            response.getValue().toStream().read(new byte[1024]);
            response.close();
        });
    }

    @Test
    public void closeHttpResponseWithConsumingPartialWrite() {
        RuntimeException ex = assertThrows(RuntimeException.class,
            () -> runScenario(response -> response.getValue().writeTo(new ThrowingWritableByteChannel())));
        assertInstanceOf(ExecutionException.class, ex.getCause());
        assertInstanceOf(CoreException.class, ex.getCause().getCause());
        assertEquals(0, testResourceLeakDetectorFactory.getTotalReportedLeakCount());
    }

    private static final class ThrowingWritableByteChannel implements WritableByteChannel {
        private boolean open = true;
        int writeCount = 0;

        @Override
        public int write(ByteBuffer src) throws IOException {
            if (writeCount++ < 3) {
                int remaining = src.remaining();
                src.position(src.position() + remaining);
                return remaining;
            } else {
                throw new IOException();
            }
        }

        @Override
        public boolean isOpen() {
            return open;
        }

        @Override
        public void close() {
            open = false;
        }
    }

    @Test
    public void closeHttpResponseWithConsumingFullBody() {
        runScenario(response -> {
            response.getValue().toBytes();
            response.close();
        });
    }

    @Test
    public void consumeBodyUsingInputStreamFromBinaryData() {
        runScenario(response -> {
            try (InputStream stream = response.getValue().toStream()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int readCount = 0;

                while ((readCount = stream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readCount);
                }

                TestUtils.assertArraysEqual(LONG_BODY, outputStream.toByteArray());
            }
        });
    }

    private void runScenario(IOExceptionCheckedConsumer<Response<BinaryData>> responseConsumer) {
        try {
            HttpClient httpClient = new NettyHttpClientProvider().getSharedInstance();

            Semaphore limiter = new Semaphore(Runtime.getRuntime().availableProcessors() - 1);
            List<Future<Void>> futures = SharedExecutorService.getInstance()
                .invokeAll(IntStream.range(0, 1).mapToObj(ignored -> (Callable<Void>) () -> {
                    try {
                        limiter.acquire();
                        responseConsumer
                            .accept(httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(url)));
                    } finally {
                        limiter.release();
                    }

                    return null;
                }).collect(Collectors.toList()));

            for (Future<Void> future : futures) {
                future.get();
            }

            // GC twice to ensure full cleanup.
            Thread.sleep(1000);
            Runtime.getRuntime().gc();

            Thread.sleep(1000);
            Runtime.getRuntime().gc();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void closingHttpResponseIsIdempotent() throws InterruptedException {
        HttpClient httpClient = new NettyHttpClientProvider().getSharedInstance();

        Response<BinaryData> response = httpClient.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(url));
        response.close();
        Thread.sleep(1_000);
        response.close();
        Thread.sleep(1_000);
    }

    private static final class TestResourceLeakDetectorFactory extends ResourceLeakDetectorFactory {
        private final Collection<TestResourceLeakDetector<?>> createdDetectors = new ConcurrentLinkedDeque<>();

        @Override
        @SuppressWarnings("deprecation") // API is deprecated but abstract
        public <T> ResourceLeakDetector<T> newResourceLeakDetector(Class<T> resource, int samplingInterval,
            long maxActive) {
            TestResourceLeakDetector<T> leakDetector
                = new TestResourceLeakDetector<>(resource, samplingInterval, maxActive);
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
