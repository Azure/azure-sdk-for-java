// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.implementation.models.ContentType;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    // using 4K as default buffer size: https://stackoverflow.com/a/237495/1473510
    private static final int BYTE_BUFFER_CHUNK_SIZE = 4096;

    private Utility() {
    }

    /**
     * Automatically detect byte buffer's content type.
     * Given the source: <a href="https://en.wikipedia.org/wiki/Magic_number_(programming)#Magic_numbers_in_files"/>.
     *
     * @param buffer The byte buffer input.
     *
     * @return The {@link ContentType} content type.
     */
    public static ContentType getContentType(ByteBuffer buffer) {
        final byte[] bytes = buffer.array();
        if (bytes.length < 4) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("Invalid input. Expect more than 4 bytes of data"));
        }

        if (bytes[0] == (byte) 0x25 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x44 && bytes[3] == (byte) 0x46) {
            return ContentType.APPLICATION_PDF;
        } else if (bytes[0] == (byte) 0xff && bytes[1] == (byte) 0xd8) {
            return ContentType.IMAGE_JPEG;
        } else if (bytes[0] == (byte) 0x89 &&  bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4e
            && bytes[3] == (byte) 0x47) {
            return ContentType.IMAGE_PNG;
        } else if (
            // little-endian
            (bytes[0] == (byte) 0x49 && bytes[1] == (byte) 0x49 && bytes[2] == (byte) 0x2a && bytes[3] == (byte) 0x0)
                // big-endian
                || (bytes[0] == (byte) 0x4d && bytes[1] == (byte) 0x4d && bytes[2] == (byte) 0x0
                    && bytes[3] == (byte) 0x2a)) {
            return ContentType.IMAGE_TIFF;
        } else {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Content type could not be detected. Should use other overload API that takes content type."));
        }
    }

    /**
     * Creates a Flux of ByteBuffer, with each ByteBuffer wrapping bytes read from the given
     * InputStream.
     *
     * @param inputStream InputStream to back the Flux
     * @return Flux of ByteBuffer backed by the InputStream
     */
    public static Flux<ByteBuffer> toFluxByteBuffer(InputStream inputStream) {
        Pair pair = new Pair();
        return Flux.just(true)
            .repeat()
            .map(ignore -> {
                byte[] buffer = new byte[BYTE_BUFFER_CHUNK_SIZE];
                try {
                    int numBytes = inputStream.read(buffer);
                    if (numBytes > 0) {
                        return pair.buffer(ByteBuffer.wrap(buffer, 0, numBytes)).readBytes(numBytes);
                    } else {
                        return pair.buffer(null).readBytes(numBytes);
                    }
                } catch (IOException ioe) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(ioe));
                }
            })
            .takeUntil(p -> p.readBytes() == -1)
            .filter(p -> p.readBytes() > 0)
            .map(Pair::buffer);
    }

    private static class Pair {
        private ByteBuffer byteBuffer;
        private int readBytes;

        ByteBuffer buffer() {
            return this.byteBuffer;
        }

        int readBytes() {
            return this.readBytes;
        }

        Pair buffer(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
            return this;
        }

        Pair readBytes(int cnt) {
            this.readBytes = cnt;
            return this;
        }
    }

    /**
     * Extracts the result ID from the URL.
     *
     * @param operationLocation The URL specified in the 'Operation-Location' response header containing the
     * resultId used to track the progress and obtain the result of the analyze operation.
     *
     * @return The resultId used to track the progress.
     */
    public static String parseModelId(String operationLocation) {
        if (!CoreUtils.isNullOrEmpty(operationLocation)) {
            int lastIndex = operationLocation.lastIndexOf('/');
            if (lastIndex != -1) {
                return operationLocation.substring(lastIndex + 1);
            }
        }
        throw LOGGER.logExceptionAsError(
            new RuntimeException("Failed to parse operation header for result Id from: " + operationLocation));
    }
}
