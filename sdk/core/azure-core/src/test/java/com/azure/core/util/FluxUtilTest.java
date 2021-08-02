// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.FileLockInterruptionException;
import java.nio.channels.NonWritableChannelException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FluxUtilTest {
    @Test
    public void testCallWithContextGetSingle() {
        StepVerifier.create(getSingle()
                .contextWrite(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar")))
            .assertNext(response -> assertEquals("Hello, Foo Bar", response))
            .verifyComplete();
    }

    @Test
    public void testCallWithContextGetCollection() {
        StepVerifier.create(getCollection()
                .contextWrite(reactor.util.context.Context.of("FirstName", "Foo", "LastName", "Bar")))
            .assertNext(response -> assertEquals("Hello,", response))
            .assertNext(response -> assertEquals("Foo", response))
            .assertNext(response -> assertEquals("Bar", response))
            .verifyComplete();
    }

    @Test
    public void testCallWithDefaultContextGetSingle() {
        StepVerifier.create(getSingleWithContextAttributes()
                .contextWrite(reactor.util.context.Context.of("FirstName", "Foo")))
            .assertNext(response -> assertEquals("Hello, Foo additionalContextValue", response))
            .verifyComplete();
    }

    @Test
    public void toReactorContextNull() {
        assertTrue(FluxUtil.toReactorContext(null).isEmpty());
    }

    @Test
    public void toReactorContextContextNone() {
        assertTrue(FluxUtil.toReactorContext(Context.NONE).isEmpty());
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

        context = context.addData("key2", "value2")
            .addData("key1", "value3");

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
        Response<String> response = new SimpleResponse<>(new HttpRequest(HttpMethod.GET, "http://www.test.com"),
            202, new HttpHeaders(), testValue);
        StepVerifier.create(FluxUtil.toMono(response))
            .assertNext(val -> assertEquals(val, testValue))
            .verifyComplete();
    }

    @Test
    public void testMonoError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.monoError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.fluxError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testPageFluxError() {
        String errMsg = "It is an error message";
        RuntimeException ex = new RuntimeException(errMsg);
        ClientLogger logger = new ClientLogger(FluxUtilTest.class);
        StepVerifier.create(FluxUtil.pagedFluxError(logger, ex))
            .verifyErrorMessage(errMsg);
    }

    @Test
    public void testWriteFile() throws Exception {
        String toReplace = "test";
        String original = "hello there";
        String target = "testo there";

        Flux<ByteBuffer> body = Flux.just(ByteBuffer.wrap(toReplace.getBytes(StandardCharsets.UTF_8)));
        File file = createFileIfNotExist();

        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(original.getBytes(StandardCharsets.UTF_8));
        }

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.WRITE)) {
            FluxUtil.writeFile(body, channel).block();
            byte[] outputStream = Files.readAllBytes(file.toPath());
            assertArrayEquals(outputStream, target.getBytes(StandardCharsets.UTF_8));
        }
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

        StepVerifier.create(writeFile)
            .expectError(expectedException)
            .verify(Duration.ofSeconds(30));
    }

    private static Stream<Arguments> writeFileDoesNotSwallowErrorSupplier() throws IOException {
        // AsynchronousFileChannel that throws NonWritableChannelException.
        Path nonWritableFile = Files.createTempFile("nonWritableFile" + UUID.randomUUID(), ".txt");
        nonWritableFile.toFile().deleteOnExit();
        AsynchronousFileChannel nonWritableChannel = AsynchronousFileChannel.open(nonWritableFile,
            StandardOpenOption.READ);

        // Flux<ByteBuffer> that throws an error during processing.
        Flux<ByteBuffer> exceptionThrowingFlux = Flux.generate(() -> 0, (count, sink) -> {
            if (count == 10) {
                sink.error(new IOException());
                return count;
            }

            sink.next(ByteBuffer.allocate(16));
            return count + 1;
        });
        Path exceptionThrowingFile = Files.createTempFile("exceptionThrowingFile" + UUID.randomUUID(), ".txt");
        exceptionThrowingFile.toFile().deleteOnExit();
        AsynchronousFileChannel exceptionThrowingChannel = AsynchronousFileChannel.open(exceptionThrowingFile,
            StandardOpenOption.WRITE);

        // Improper Flux<ByteBuffer> implementation that ignores downstream requests.
        Flux<ByteBuffer> ignoresRequestFlux = new Flux<ByteBuffer>() {
            @Override
            public void subscribe(CoreSubscriber<? super ByteBuffer> actual) {
                final byte[] data = new byte[16 * 4096];
                new SecureRandom().nextBytes(data);

                actual.onSubscribe(new Subscription() {
                    @Override
                    public void request(long n) {
                        for (int i = 0; i < data.length - 4095; i += 4096) {
                            actual.onNext(ByteBuffer.wrap(data, i, 4096));
                        }

                        actual.onComplete();
                    }

                    @Override
                    public void cancel() {
                    }
                });
            }
        };
        Path ignoresRequestFile = Files.createTempFile("ignoresRequestFile" + UUID.randomUUID(), ".txt");
        ignoresRequestFile.toFile().deleteOnExit();
        AsynchronousFileChannel ignoresRequestChannel = AsynchronousFileChannel.open(ignoresRequestFile,
            StandardOpenOption.WRITE);

        // CompletionHandler that emits a writing error.
        AsynchronousFileChannel completionHandlerPropagatesError = mock(AsynchronousFileChannel.class);
        doAnswer(invocation -> {
            CompletionHandler<Integer, ByteBuffer> completionHandler = invocation.getArgument(3);

            // Returning an esoteric error.
            completionHandler.failed(new FileLockInterruptionException(), invocation.getArgument(0));
            return null;
        }).when(completionHandlerPropagatesError).write(any(), anyLong(), any(), any());

        return Stream.of(
            // AsynchronousFileChannel doesn't have write capabilities.
            Arguments.of(Flux.just(ByteBuffer.allocate(0)), nonWritableChannel, NonWritableChannelException.class),

            // Flux<ByteBuffer> has an exception during processing.
            Arguments.of(exceptionThrowingFlux, exceptionThrowingChannel, IOException.class),

            // Flux<ByteBuffer> that ignores onNext request.
            Arguments.of(ignoresRequestFlux, ignoresRequestChannel, IllegalStateException.class),

            // AsynchronousFileChannel that has an error propagated from the CompletionHandler.
            Arguments.of(Flux.just(ByteBuffer.allocate(0)), completionHandlerPropagatesError,
                FileLockInterruptionException.class)
        );
    }

    @Test
    public void writingRetriableStreamThatFails() throws IOException {
        byte[] data = new byte[4 * 1024 * 1024];
        new SecureRandom().nextBytes(data);

        AtomicInteger errorCount = new AtomicInteger();
        Flux<ByteBuffer> retriableStream = FluxUtil.createRetriableDownloadFlux(
            () -> generateStream(data, 0, errorCount),
            (throwable, position) -> generateStream(data, position, errorCount), 5);

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

        StepVerifier.create(writeFile)
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        byte[] writtenData = Files.readAllBytes(file);
        assertArrayEquals(data, writtenData);
    }

    private Flux<ByteBuffer> generateStream(byte[] data, long offset, AtomicInteger errorCount) {
        final long[] pos = new long[]{offset};

        return Flux.push(emitter -> {
            while (pos[0] != data.length) {
                double random = Math.random();
                if (random < 0.05 && errorCount.getAndIncrement() < 5) {
                    emitter.error(new IOException());
                    return;
                }

                int readCount = (int) Math.min(4096, data.length - pos[0]);
                emitter.next(ByteBuffer.wrap(data, (int) pos[0], readCount));

                pos[0] += readCount;
            }

            emitter.complete();
        });
    }

    @Test
    public void readFile() throws IOException {
        final byte[] expectedFileBytes = new byte[10 * 1024 * 1024];
        final SecureRandom random = new SecureRandom();
        random.nextBytes(expectedFileBytes);

        File file = createFileIfNotExist();
        try (FileOutputStream stream = new FileOutputStream(file)) {
            stream.write(expectedFileBytes);
        }

        try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(FluxUtil.readFile(channel),
                    expectedFileBytes.length))
                .assertNext(bytes -> assertArrayEquals(expectedFileBytes, bytes))
                .verifyComplete();
        }

        Files.deleteIfExists(file.toPath());
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
                    assertArrayEquals(expected, collectionBuffer.array());
                    return false;
                } else {
                    return true;
                }
            }).verifyComplete();
    }

    private static Stream<Arguments> toFluxByteBufferSupplier() {
        byte[] emptyBuffer = new byte[0];
        byte[] singleRead = new byte[4096];
        byte[] multipleReads = new byte[8193];

        SecureRandom random = new SecureRandom();
        random.nextBytes(singleRead);
        random.nextBytes(multipleReads);

        return Stream.of(
            Arguments.arguments(null, null, emptyBuffer),
            Arguments.arguments(new ByteArrayInputStream(emptyBuffer), null, emptyBuffer),
            Arguments.arguments(new ByteArrayInputStream(singleRead), null, singleRead),
            Arguments.arguments(new ByteArrayInputStream(multipleReads), null, multipleReads),
            Arguments.arguments(new ByteArrayInputStream(singleRead), 8192, singleRead),
            Arguments.arguments(new ByteArrayInputStream(singleRead), 2048, singleRead),
            Arguments.arguments(new ByteArrayInputStream(multipleReads), 5432, multipleReads)
        );
    }

    @Test
    public void toFluxByteBufferMultipleSubscriptions() {
        byte[] singleRead = new byte[4096];
        new SecureRandom().nextBytes(singleRead);

        InputStream inputStream = new ByteArrayInputStream(singleRead);

        Flux<ByteBuffer> conversionFlux = FluxUtil.toFluxByteBuffer(inputStream);

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(conversionFlux))
            .assertNext(actual -> assertArrayEquals(singleRead, actual))
            .verifyComplete();

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(conversionFlux))
            .assertNext(actual -> assertArrayEquals(new byte[0], actual))
            .verifyComplete();
    }

    @Test
    public void illegalToFluxByteBufferChunkSize() {
        StepVerifier.create(FluxUtil.toFluxByteBuffer(null, 0))
            .verifyError(IllegalArgumentException.class);

        StepVerifier.create(FluxUtil.toFluxByteBuffer(null, -1))
            .verifyError(IllegalArgumentException.class);
    }

    @Test
    public void toFluxByteBufferSinkException() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        when(inputStream.read(any(), anyInt(), anyInt())).thenThrow(new IOException("error"));

        StepVerifier.create(FluxUtil.toFluxByteBuffer(inputStream))
            .verifyError(IOException.class);
    }

    public Flux<ByteBuffer> mockReturnType() {
        return Flux.just(ByteBuffer.wrap(new byte[0]));
    }

    private Mono<String> getSingle() {
        return FluxUtil.withContext(this::serviceCallSingle);
    }

    private Flux<String> getCollection() {
        return FluxUtil.fluxContext(this::serviceCallCollection);
    }

    private Mono<String> getSingleWithContextAttributes() {
        return FluxUtil.withContext(this::serviceCallWithContextMetadata,
            Collections.singletonMap("additionalContextKey", "additionalContextValue"));
    }

    private Mono<String> serviceCallSingle(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");
        return Mono.just(msg);
    }

    private Flux<String> serviceCallCollection(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("LastName").orElse("");

        return Flux.just(msg.split(" "));
    }

    private Mono<String> serviceCallWithContextMetadata(Context context) {
        String msg = "Hello, "
            + context.getData("FirstName").orElse("Stranger")
            + " "
            + context.getData("additionalContextKey").orElse("Not found");
        return Mono.just(msg);
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
