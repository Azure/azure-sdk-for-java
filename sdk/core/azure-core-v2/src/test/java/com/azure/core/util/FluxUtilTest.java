// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.pipeline.FixedDelayOptions;
import io.clientcore.core.http.pipeline.HttpRetryOptions;
import io.clientcore.core.http.rest.Response;
import io.clientcore.core.http.rest.SimpleResponse;
import io.clientcore.core.util.ClientLogger;
import com.azure.core.v2.util.mocking.MockAsynchronousFileChannel;
import com.azure.core.v2.util.mocking.MockFileChannel;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.Channels;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static com.azure.core.CoreTestUtils.fillArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FluxUtilTest {
    @Test
    public void testCallWithContextGetSingle() {
        StepVerifier
            .create(getSingle().contextWrite(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar")))
            .assertNext(response -> assertEquals("Hello, Foo Bar", response))
            .verifyComplete();
    }

    @Test
    public void testCallWithContextGetCollection() {
        StepVerifier
            .create(
                getCollection().contextWrite(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar")))
            .assertNext(response -> assertEquals("Hello,", response))
            .assertNext(response -> assertEquals("Foo", response))
            .assertNext(response -> assertEquals("Bar", response))
            .verifyComplete();
    }

    @Test
    public void testCallWithDefaultContextGetSingle() {
        StepVerifier
            .create(getSingleWithContextAttributes().contextWrite(reactor.util.context.Context.of("FirstName", "Foo")))
            .assertNext(response -> assertEquals("Hello, Foo additionalContextValue", response))
            .verifyComplete();
    }

    @Test
    public void toReactorContextNull() {
        assertTrue(FluxUtil.toReactorContext(null).isEmpty());
    }

    @Test
    public void toReactorContextContextNone() {
        assertTrue(FluxUtil.toReactorContext(Context.none()).isEmpty());
    }

    @Test
    public void toReactorContextCleansesNullValues() {
        assertTrue(FluxUtil.toReactorContext(new Context("key", null)).isEmpty());
    }

    @Test
    public void toReactorContext() {
        Context context = new Context("key1", "value1");

        reactor.util.context.Context reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(1, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value1", reactorContext.get("key1"));

        context = context.addData("key2", "value2").addData("key1", "value3");

        reactorContext = FluxUtil.toReactorContext(context);
        assertEquals(2, reactorContext.size());
        assertTrue(reactorContext.hasKey("key1"));
        assertEquals("value3", reactorContext.get("key1"));
        assertTrue(reactorContext.hasKey("key2"));
        assertEquals("value2", reactorContext.get("key2"));
    }

    @Test
    public void testIsFluxByteBufferInvalidType() {
        assertFalse(FluxUtil.isFluxByteBuffer(Mono.class));
    }

    @Test
    public void testIsFluxByteBufferValidType() throws Exception {
        Method method = FluxUtilTest.class.getMethod("mockReturnType");
        Type returnType = method.getGenericReturnType();
        assertTrue(FluxUtil.isFluxByteBuffer(returnType));
    }

    @Test
    public void testToMono() {
        String testValue = "some value";
        Response<String> response = new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "http://www.test.com"), 202,
            new HttpHeaders(), testValue);
        StepVerifier.create(FluxUtil.toMono(response)).assertNext(val -> assertEquals(val, testValue)).verifyComplete();
    }

    @Test
    public void testMonoError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.monoError(logger, ex)).verifyErrorMessage(errMsg);
    }

    @Test
    public void testFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.fluxError(logger, ex)).verifyErrorMessage(errMsg);
    }

    @Test
    public void testPageFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.pagedFluxError(logger, ex)).verifyErrorMessage(errMsg);
    }

    @Test
    public void testWriteFile() throws Exception {
        String toReplace = "test";
        String original = "hello there";
        String target = "testo there";

        byte[] bytes = toReplace.getBytes(StandardCharsets.UTF_8);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(bytes, 0, 2), ByteBuffer.wrap(bytes, 2, 2));
        File file = createFileIfNotExist();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(original.getBytes(StandardCharsets.UTF_8));
        }

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            FluxUtil.writeFile(body, channel).block();
            byte[] outputStream = Files.readAllBytes(file.toPath());
            assertArraysEqual(outputStream, target.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testWriteFileWithPosition() throws Exception {
        String toReplace = "test";
        String original = "hello there";
        String target = "hello teste";

        byte[] bytes = toReplace.getBytes(StandardCharsets.UTF_8);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(bytes, 0, 2), ByteBuffer.wrap(bytes, 2, 2));
        File file = createFileIfNotExist();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(original.getBytes(StandardCharsets.UTF_8));
        }

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            FluxUtil.writeFile(body, channel, 6).block();
            byte[] outputStream = Files.readAllBytes(file.toPath());
            assertArraysEqual(outputStream, target.getBytes(StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testWriteWritableChannel() {
        String content = "test";

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(bytes, 0, 2), ByteBuffer.wrap(bytes, 2, 2));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        WritableByteChannel channel = Channels.newChannel(byteArrayOutputStream);

        FluxUtil.writeToWritableByteChannel(body, channel).block();
        assertArraysEqual(byteArrayOutputStream.toByteArray(), bytes);
    }

    @ParameterizedTest
    @MethodSource("writeFileDoesNotSwallowErrorSupplier")
    public void writeFileDoesNotSwallowError(Flux<ByteBuffer> data, AsynchronousFileChannel channel,
        Class<? extends Throwable> expectedException) {
        Flux<Void> writeFile = Flux.using(() -> channel, c -> FluxUtil.writeFile(data, c), c -> {
            try {
                c.close();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        });

        StepVerifier.create(writeFile).expectError(expectedException).verify(Duration.ofSeconds(30));
    }

    private static Stream<Arguments> writeFileDoesNotSwallowErrorSupplier() {
        // AsynchronousFileChannel that throws NonWritableChannelException.
        AsynchronousFileChannel nonWritableChannel = new MockAsynchronousFileChannel(new byte[4096]) {

            @Override
            public Future<Integer> write(ByteBuffer src, long position) {
                return new CompletableFuture<Integer>() {
                    @Override
                    public Integer get() throws ExecutionException {
                        throw new ExecutionException(new NonWritableChannelException());
                    }
                };
            }

            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                handler.failed(new NonWritableChannelException(), attachment);
            }
        };

        // Flux<ByteBuffer> that throws an error during processing.
        Flux<ByteBuffer> exceptionThrowingFlux = Flux.generate(() -> 0, (count, sink) -> {
            if (count == 10) {
                sink.error(new IOException());
                return count;
            }

            sink.next(ByteBuffer.allocate(16));
            return count + 1;
        });
        AsynchronousFileChannel exceptionThrowingChannel = new MockAsynchronousFileChannel() {
            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                int remaining = src.remaining();
                src.position(src.position() + remaining);
                handler.completed(remaining, attachment);
            }
        };

        // CompletionHandler that emits a writing error.
        AsynchronousFileChannel completionHandlerPropagatesError = new MockAsynchronousFileChannel(new byte[4096]) {
            @Override
            public Future<Integer> write(ByteBuffer src, long position) {
                return new CompletableFuture<Integer>() {
                    @Override
                    public Integer get() throws ExecutionException {
                        throw new ExecutionException(new FileLockInterruptionException());
                    }
                };
            }

            @Override
            public <A> void write(ByteBuffer src, long position, A attachment,
                CompletionHandler<Integer, ? super A> handler) {
                handler.failed(new FileLockInterruptionException(), attachment);
            }
        };

        return Stream.of(
            // AsynchronousFileChannel doesn't have write capabilities.
            Arguments.of(Flux.just(ByteBuffer.allocate(1)), nonWritableChannel, NonWritableChannelException.class),

            // Flux<ByteBuffer> has an exception during processing.
            Arguments.of(exceptionThrowingFlux, exceptionThrowingChannel, IOException.class),

            // AsynchronousFileChannel that has an error propagated from the CompletionHandler.
            Arguments.of(Flux.just(ByteBuffer.allocate(1)), completionHandlerPropagatesError,
                FileLockInterruptionException.class));
    }

    @Test
    public void writingRetriableStreamThatFails() throws IOException {
        byte[] data = new byte[1024 * 1024];
        fillArray(data);

        AtomicInteger errorCount = new AtomicInteger();
        Flux<ByteBuffer> retriableStream
            = FluxUtil.createRetriableDownloadFlux(() -> generateStream(data, 0, errorCount),
                (throwable, position) -> generateStream(data, position, errorCount),
                new HttpRetryOptions(new FixedDelayOptions(5, Duration.ofMillis(1))), 0L);

        Path file = Files.createTempFile("writingRetriableStreamThatFails" + UUID.randomUUID(), ".txt");
        file.toFile().deleteOnExit();

        Flux<Void> writeFile = Flux.using(() -> AsynchronousFileChannel.open(file, StandardOpenOption.WRITE),
            channel -> FluxUtil.writeFile(retriableStream, channel), channel -> {
                try {
                    channel.close();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });

        StepVerifier.create(writeFile).expectComplete().verify(Duration.ofSeconds(60));

        byte[] writtenData = Files.readAllBytes(file);
        assertArraysEqual(data, writtenData);
    }

    private Flux<ByteBuffer> generateStream(byte[] data, long offset, AtomicInteger errorCount) {
        final long[] pos = new long[] { offset };

        return Flux.push(emitter -> {
            while (pos[0] != data.length) {
                double random = Math.random();
                if (random < 0.05 && errorCount.getAndIncrement() < 5) {
                    emitter.error(new IOException());
                    return;
                }

                int readCount = (int) Math.min(16384, data.length - pos[0]);
                emitter.next(ByteBuffer.wrap(data, (int) pos[0], readCount));

                pos[0] += readCount;
            }

            emitter.complete();
        });
    }

    @Test
    public void readFile() throws IOException {
        final byte[] expectedFileBytes = new byte[10 * 1024 * 1024];
        fillArray(expectedFileBytes);

        MockAsynchronousFileChannel mockAsynchronousFileChannel
            = new MockAsynchronousFileChannel(expectedFileBytes, expectedFileBytes.length);

        try (AsynchronousFileChannel channel = mockAsynchronousFileChannel) {
            StepVerifier
                .create(FluxUtil.collectBytesInByteBufferStream(FluxUtil.readFile(channel), expectedFileBytes.length))
                .assertNext(bytes -> assertArraysEqual(expectedFileBytes, bytes))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("toFluxByteBufferSupplier")
    public void toFluxByteBuffer(InputStream inputStream, Integer chunkSize, byte[] expected) {
        Flux<ByteBuffer> conversionFlux = (chunkSize == null)
            ? FluxUtil.toFluxByteBuffer(inputStream)
            : FluxUtil.toFluxByteBuffer(inputStream, chunkSize);

        // If the stream is null or empty the Flux will only trigger complete.
        if (inputStream == null || expected.length == 0) {
            StepVerifier.create(conversionFlux).verifyComplete();
            return;
        }

        int unboxedChunkSize = (chunkSize == null) ? 4096 : chunkSize;
        AtomicLong requestCount = new AtomicLong((long) Math.ceil((double) expected.length / unboxedChunkSize));
        ByteBuffer collectionBuffer = ByteBuffer.allocate(expected.length);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(conversionFlux))
            .thenRequest(requestCount.get())
            .thenConsumeWhile(bytes -> {
                collectionBuffer.put(bytes, collectionBuffer.position(), bytes.length);

                // Check if this is the last emission expected.
                if (requestCount.decrementAndGet() == -1) {
                    assertArraysEqual(expected, collectionBuffer.array());
                    return false;
                } else {
                    return true;
                }
            })
            .verifyComplete();
    }

    private static Stream<Arguments> toFluxByteBufferSupplier() {
        byte[] emptyBuffer = new byte[0];
        byte[] singleRead = new byte[4096];
        byte[] multipleReads = new byte[8193];

        fillArray(singleRead);
        fillArray(multipleReads);

        return Stream.of(Arguments.arguments(null, null, emptyBuffer),
            Arguments.arguments(new ByteArrayInputStream(emptyBuffer), null, emptyBuffer),
            Arguments.arguments(new ByteArrayInputStream(singleRead), null, singleRead),
            Arguments.arguments(new ByteArrayInputStream(multipleReads), null, multipleReads),
            Arguments.arguments(new ByteArrayInputStream(singleRead), 8192, singleRead),
            Arguments.arguments(new ByteArrayInputStream(singleRead), 2048, singleRead),
            Arguments.arguments(new ByteArrayInputStream(multipleReads), 5432, multipleReads));
    }

    @Test
    public void toFluxByteBufferMultipleSubscriptions() {
        byte[] singleRead = new byte[4096];
        fillArray(singleRead);

        InputStream inputStream = new ByteArrayInputStream(singleRead);

        Flux<ByteBuffer> conversionFlux = FluxUtil.toFluxByteBuffer(inputStream);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(conversionFlux))
            .assertNext(actual -> assertArraysEqual(singleRead, actual))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(conversionFlux))
            .assertNext(actual -> assertArraysEqual(new byte[0], actual))
            .verifyComplete();
    }

    @Test
    public void illegalToFluxByteBufferChunkSize() {
        StepVerifier.create(FluxUtil.toFluxByteBuffer(null, 0)).verifyError(IllegalArgumentException.class);

        StepVerifier.create(FluxUtil.toFluxByteBuffer(null, -1)).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void toFluxByteBufferSinkException() {
        InputStream inputStream = new ByteArrayInputStream(new byte[0]) {
            @Override
            public synchronized int read(byte[] b, int off, int len) {
                throw new IllegalStateException("error");
            }
        };

        StepVerifier.create(FluxUtil.toFluxByteBuffer(inputStream)).verifyError(IllegalStateException.class);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS) // Test is disabled on Windows as a FileChannel isn't used.
    public void toFluxByteBufferFileInputStreamChannelCloses() throws IOException {
        AtomicInteger positionCalls = new AtomicInteger();
        AtomicInteger sizeCalls = new AtomicInteger();
        AtomicInteger implCloseChannelCalls = new AtomicInteger();

        MockFileChannel channel = new MockFileChannel() {
            @Override
            public void implCloseChannel() {
                implCloseChannelCalls.incrementAndGet();
            }

            @Override
            public long position() {
                positionCalls.incrementAndGet();
                return 0L;
            }

            @Override
            public long size() {
                sizeCalls.incrementAndGet();
                return 0L;
            }
        };

        AtomicInteger getChannelCalls = new AtomicInteger();
        FileInputStream inputStream = new FileInputStream(new FileDescriptor()) {
            @Override
            public FileChannel getChannel() {
                getChannelCalls.incrementAndGet();
                return channel;
            }
        };

        StepVerifier.create(FluxUtil.toFluxByteBuffer(inputStream)).verifyComplete();

        assertEquals(1, getChannelCalls.get());
        assertEquals(1, positionCalls.get());
        assertEquals(1, sizeCalls.get());
        assertEquals(1, implCloseChannelCalls.get());
    }

    /**
     * Verifies that the usage of {@link FluxUtil#toFluxByteBuffer(InputStream)} with a {@link FileInputStream} does
     * not prevent the file from being deleted.
     *
     * @throws IOException If an error occurs while creating the temporary file.
     */
    @RepeatedTest(10) // Repeat the test times to ensure it's not a fluke.
    public void ensureFileInputStreamFileCanBeDeletedAsConversionToFluxByteBuffer() throws IOException {
        Path file = Files.createTempFile("canBeDeleted" + CoreUtils.randomUuid(), "");
        Files.write(file, "some random data".getBytes(StandardCharsets.UTF_8));
        FileInputStream fileInputStream = new FileInputStream(file.toFile());

        Void> convertThenDeleteFile = FluxUtil.toFluxByteBuffer(fileInputStream).then(Mono.create(sink -> {
            try {
                fileInputStream.close();
                Files.delete(file);
                sink.success();
            } catch (IOException e) {
                sink.error(e);
            }
        }));

        StepVerifier.create(convertThenDeleteFile).verifyComplete();
        assertTrue(Files.notExists(file));
    }

    public Flux<ByteBuffer> mockReturnType() {
        return Flux.just(ByteBuffer.wrap(new byte[0]));
    }

    private String> getSingle() {
        return FluxUtil.withContext(this::serviceCallSingle);
    }

    private Flux<String> getCollection() {
        return FluxUtil.fluxContext(this::serviceCallCollection);
    }

    private String> getSingleWithContextAttributes() {
        return FluxUtil.withContext(this::serviceCallWithContextMetadata,
            Collections.singletonMap("additionalContextKey", "additionalContextValue"));
    }

    private String> serviceCallSingle(Context context) {
        String msg = "Hello, " + context.getData("FirstName").orElse("Stranger") + " "
            + context.getData("LastName").orElse("");
        return msg);
    }

    private Flux<String> serviceCallCollection(Context context) {
        String msg = "Hello, " + context.getData("FirstName").orElse("Stranger") + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }

    private String> serviceCallWithContextMetadata(Context context) {
        String msg = "Hello, " + context.getData("FirstName").orElse("Stranger") + " "
            + context.getData("additionalContextKey").orElse("Not found");
        return msg);
    }

    private File createFileIfNotExist() {
        String fileName = UUID.randomUUID().toString();
        File file = new File("target");
        if (!file.exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new RuntimeException("Unable to create directories: " + file.getAbsolutePath());
            }
        }

        try {
            return Files.createTempFile(file.toPath(), fileName, "").toFile();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
