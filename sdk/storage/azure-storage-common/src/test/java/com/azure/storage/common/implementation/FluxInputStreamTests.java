// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FluxInputStreamTests {

    /* Network tests to be performed by implementors of the FluxInputStream. */
    Flux<ByteBuffer> generateData(int num) {
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[]{(byte) i}));
        }
        return Flux.fromIterable(buffers);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 100, Constants.KB, Constants.MB})
    void fluxISMin(int num) throws IOException {
        byte[] bytes = new byte[num];

        int totalRead = 0;
        int bytesRead = 0;

        try (InputStream is = new FluxInputStream(generateData(num))) {
            while (bytesRead != -1 && totalRead < num) {
                bytesRead = is.read(bytes, totalRead, num);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    num -= bytesRead;
                }
            }
        }

        for (int i = 0; i < num; i++) {
            assertEquals((byte) i, bytes[i]);
        }
    }

    @Test
    void fluxISWithEmptyByteBuffers() throws IOException {
        int num = Constants.KB;
        List<ByteBuffer> buffers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            buffers.add(ByteBuffer.wrap(new byte[]{(byte) i}));
            buffers.add(ByteBuffer.wrap(new byte[0]));
        }
        Flux<ByteBuffer> data = Flux.fromIterable(buffers);
        byte[] bytes = new byte[num];
        int totalRead = 0;
        int bytesRead = 0;

        try (InputStream is = new FluxInputStream(data)) {
            while (bytesRead != -1 && totalRead < num) {
                bytesRead = is.read(bytes, totalRead, num);
                if (bytesRead != -1) {
                    totalRead += bytesRead;
                    num -= bytesRead;
                }
            }
        }

        for (int i = 0; i < num; i++) {
            assertEquals((byte) i, bytes[i]);
        }
    }

    @Test
    void fluxISError() {
        Exception[] exceptions = {
            new IllegalArgumentException("Mock illegal argument exception."),
            new HttpResponseException("Mock storage exception", new MockHttpResponse(null, 404), null),
            new IOException("Mock IO Exception.")
        };

        for (Exception exception : exceptions) {
            InputStream is = new FluxInputStream(Flux.error(exception));
            assertThrows(IOException.class, is::read);
        }
    }
}
