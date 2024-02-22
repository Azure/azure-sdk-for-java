// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FluxInputStreamTests {
    /* Network tests to be performed by implementors of the FluxInputStream. */
    Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
        }
        return Flux.fromIterable(buffers);
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 100, Constants.KB, Constants.MB })
    public void fluxISMin(int num) throws IOException {
        try (InputStream is = new FluxInputStream(generateData(num))) {
            byte[] bytes = new byte[num];
            int totalRead = 0;
            int bytesRead = 0;

            while (bytesRead != -1 && totalRead < num) {
                bytesRead = is.read(bytes, totalRead, num);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    num -= bytesRead;
                }
            }

            for (int i = 0; i < num; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @Test
    public void fluxISWithEmptyByteBuffers() throws IOException {
        int num = Constants.KB;
        List<ByteBuffer> buffers = new ArrayList<>(num * 2);
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[] { (byte) i }));
            buffers.add(ByteBuffer.wrap(new byte[0]));
        }

        try (InputStream is = new FluxInputStream(Flux.fromIterable(buffers))) {
            byte[] bytes = new byte[num];
            int totalRead = 0;
            int bytesRead = 0;

            while (bytesRead != -1 && totalRead < num) {
                bytesRead = is.read(bytes, totalRead, num);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    num -= bytesRead;
                }
            }

            for (int i = 0; i < num; i++) {
                assertEquals((byte) i, bytes[i]);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("fluxISErrorSupplier")
    public void fluxISError(RuntimeException exception) {
        assertThrows(IOException.class, () -> {
            InputStream is = new FluxInputStream(Flux.error(exception));
            is.read();
            is.close();
        });
    }

    private static Stream<RuntimeException> fluxISErrorSupplier() {
        return Stream.of(new IllegalArgumentException("Mock illegal argument exception."),
            new HttpResponseException("Mock storage exception", new MockHttpResponse(null, 404), null),
            new UncheckedIOException(new IOException("Mock IO Exception.")));
    }
}
