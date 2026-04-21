// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FluxInputStreamTests {
    private static final int KB = 1024;
    private static final int MB = KB * KB;

    /* Generates deterministic test data for FluxInputStream unit tests. */
    private Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
        }
        return Flux.fromIterable(buffers);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 100, KB, MB })
    public void fluxInputStreamMin(int byteCount) throws IOException {
        final int expected = byteCount;

        try (InputStream is = new FluxInputStream(generateData(byteCount))) {
            byte[] bytes = new byte[expected];
            int totalRead = 0;
            int bytesRead = 0;
            int remaining = expected;

            while (bytesRead != -1 && totalRead < expected) {
                bytesRead = is.read(bytes, totalRead, remaining);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    remaining -= bytesRead;
                }
            }

            assertEquals(expected, totalRead);
            for (int i = 0; i < expected; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @Test
    public void fluxInputStreamWithEmptyByteBuffers() throws IOException {
        final int expected = KB;
        List<ByteBuffer> buffers = new ArrayList<>(expected * 2);
        for (int i = 0; i < expected; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
            buffers.add(ByteBuffer.wrap(new byte[0]));
        }

        try (InputStream is = new FluxInputStream(Flux.fromIterable(buffers))) {
            byte[] bytes = new byte[expected];
            int totalRead = 0;
            int bytesRead = 0;
            int remaining = expected;

            while (bytesRead != -1 && totalRead < expected) {
                bytesRead = is.read(bytes, totalRead, remaining);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    remaining -= bytesRead;
                }
            }

            assertEquals(expected, totalRead);
            for (int i = 0; i < expected; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("fluxInputStreamErrorSupplier")
    public void fluxInputStreamError(RuntimeException exception) {
        assertThrows(IOException.class, () -> {
            try (InputStream is = new FluxInputStream(Flux.error(exception))) {
                is.read();
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static Stream<RuntimeException> fluxInputStreamErrorSupplier() {
        HttpResponse httpResponse = new HttpResponse(null) {
            @Override
            public int getStatusCode() {
                return 404;
            }

            @Override
            public String getHeaderValue(String name) {
                return "";
            }

            @Override
            public HttpHeaders getHeaders() {
                return null;
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return null;
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString() {
                return null;
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return null;
            }
        };
        return Stream.of(new IllegalArgumentException("Mock illegal argument exception."),
            new HttpResponseException("Mock exception", httpResponse, null),
            new UncheckedIOException(new IOException("Mock IO Exception.")));
    }
}
