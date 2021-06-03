// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link RequestContent}.
 */
public class RequestContentTests {
    @ParameterizedTest
    @MethodSource("expectedContentSupplier")
    public void expectedContent(RequestContent content, boolean checkLength, byte[] expected) {
        if (checkLength) {
            assertEquals(expected.length, content.getLength());
        }

        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(content.asFluxByteBuffer()))
            .assertNext(bytes -> assertArrayEquals(expected, bytes))
            .verifyComplete();
    }

    private static Stream<Arguments> expectedContentSupplier() throws IOException {
        byte[] emptyBytes = new byte[0];

        SecureRandom random = new SecureRandom();
        byte[] randomBytes = new byte[1024 * 1024];
        random.nextBytes(randomBytes);

        String emptyString = new String(emptyBytes, StandardCharsets.UTF_8);
        String randomString = new String(randomBytes, StandardCharsets.UTF_8);
        // This is done as using random bytes to make a String may result in the String being longer than the initial
        // byte array as missing code point bytes are packed.
        byte[] randomStringBytes = randomString.getBytes(StandardCharsets.UTF_8);

        Path emptyTempFile = Files.createTempFile("emptyTempFile", ".txt");
        emptyTempFile.toFile().deleteOnExit();
        Files.write(emptyTempFile, emptyBytes);

        Path randomTempFile = Files.createTempFile("randomTempFile", ".txt");
        randomTempFile.toFile().deleteOnExit();
        Files.write(randomTempFile, randomBytes);

        Flux<ByteBuffer> emptyFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(emptyBytes)));
        Flux<ByteBuffer> randomFlux = Flux.defer(() -> Flux.just(ByteBuffer.wrap(randomBytes)));
        Flux<ByteBuffer> chunkedRandomFlux = Flux.generate(() -> 0, (offset, sink) -> {
            if (offset == randomBytes.length) {
                sink.complete();
                return offset;
            }

            int nextLength = Math.min(1024, randomBytes.length - offset);
            sink.next(ByteBuffer.wrap(Arrays.copyOfRange(randomBytes, offset, offset + nextLength)));
            return offset + nextLength;
        });

        return Stream.of(
            Arguments.of(RequestContent.fromBytes(emptyBytes), true, emptyBytes),
            Arguments.of(RequestContent.fromBytes(emptyBytes, 0, 0), true, emptyBytes),

            Arguments.of(RequestContent.fromBytes(randomBytes), true, randomBytes),
            Arguments.of(RequestContent.fromBytes(randomBytes, 0, randomBytes.length), true, randomBytes),
            Arguments.of(RequestContent.fromBytes(randomBytes, 1024, 1024), true,
                Arrays.copyOfRange(randomBytes, 1024, 2048)),

            Arguments.of(RequestContent.fromString(emptyString), true, emptyBytes),
            Arguments.of(RequestContent.fromString(randomString), true, randomStringBytes),

            Arguments.of(RequestContent.fromFile(emptyTempFile), true, emptyBytes),
            Arguments.of(RequestContent.fromFile(emptyTempFile, 0, 0), true, emptyBytes),

            Arguments.of(RequestContent.fromFile(randomTempFile), true, randomBytes),
            Arguments.of(RequestContent.fromFile(randomTempFile, 0, randomBytes.length), true, randomBytes),
            Arguments.of(RequestContent.fromFile(randomTempFile, 1024, 1024), true,
                Arrays.copyOfRange(randomBytes, 1024, 2048)),

            Arguments.of(RequestContent.fromObject(emptyString), false, "\"\"".getBytes(StandardCharsets.UTF_8)),

            Arguments.of(RequestContent.fromFlux(emptyFlux), false, emptyBytes),
            Arguments.of(RequestContent.fromFlux(emptyFlux, 0), true, emptyBytes),

            Arguments.of(RequestContent.fromFlux(randomFlux), false, randomBytes),
            Arguments.of(RequestContent.fromFlux(randomFlux, randomBytes.length), true, randomBytes),

            Arguments.of(RequestContent.fromFlux(chunkedRandomFlux), false, randomBytes),
            Arguments.of(RequestContent.fromFlux(chunkedRandomFlux, randomBytes.length), true, randomBytes),

            Arguments.of(RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(emptyFlux)), false, emptyBytes),
            Arguments.of(RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(emptyFlux), 0), true, emptyBytes),

            Arguments.of(RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(randomFlux)), false, randomBytes),
            Arguments.of(RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(randomFlux), randomBytes.length),
                true, randomBytes),

            Arguments.of(RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(chunkedRandomFlux)), false,
                randomBytes),
            Arguments.of(
                RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(chunkedRandomFlux), randomBytes.length),
                true, randomBytes)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidArgumentSupplier")
    public void invalidArgument(Executable requestContentSupplier, Class<? extends Throwable> expectedException) {
        assertThrows(expectedException, requestContentSupplier);
    }

    private static Stream<Arguments> invalidArgumentSupplier() {
        byte[] dummyBytes = new byte[0];

        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(0L);

        Path mockPath = mock(Path.class);
        when(mockPath.toFile()).thenReturn(mockFile);

        return Stream.of(
            // bytes cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromBytes(null)), NullPointerException.class),
            Arguments.of(createExecutable(() -> RequestContent.fromBytes(null, 0, 0)), NullPointerException.class),

            // offset cannot be negative
            Arguments.of(createExecutable(() -> RequestContent.fromBytes(dummyBytes, -1, 0)),
                IllegalArgumentException.class),

            // length cannot be negative
            Arguments.of(createExecutable(() -> RequestContent.fromBytes(dummyBytes, 0, -1)),
                IllegalArgumentException.class),

            // offset + length cannot be greater than bytes.length
            Arguments.of(createExecutable(() -> RequestContent.fromBytes(dummyBytes, 0, 1)),
                IllegalArgumentException.class),

            // content cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromString(null)), NullPointerException.class),

            // content cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromBinaryData(null)), NullPointerException.class),

            // file cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromFile(null)), NullPointerException.class),
            Arguments.of(createExecutable(() -> RequestContent.fromFile(null, 0, 0)), NullPointerException.class),

            // offset cannot be negative
            Arguments.of(createExecutable(() -> RequestContent.fromFile(mockPath, -1, 0)),
                IllegalArgumentException.class),

            // length cannot be negative
            Arguments.of(createExecutable(() -> RequestContent.fromFile(mockPath, 0, -1)),
                IllegalArgumentException.class),

            // offset + length cannot be greater than file size
            Arguments.of(createExecutable(() -> RequestContent.fromFile(mockPath, 0, 1)),
                IllegalArgumentException.class),

            // serializer cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromObject(null, null)), NullPointerException.class),

            // content cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromFlux(null)), NullPointerException.class),

            // length cannot be negative
            Arguments.of(createExecutable(() -> RequestContent.fromFlux(Flux.empty(), -1)),
                IllegalArgumentException.class),

            // content cannot be null
            Arguments.of(createExecutable(() -> RequestContent.fromBufferedFlux(null)), NullPointerException.class),

            // length cannot be negative
            Arguments.of(
                createExecutable(() -> RequestContent.fromBufferedFlux(new BufferedFluxByteBuffer(Flux.empty()), -1)),
                IllegalArgumentException.class)
        );
    }

    private static Executable createExecutable(Runnable runnable) {
        return runnable::run;
    }
}
