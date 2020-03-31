// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility method class.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    private Utility() {
    }

    /**
     * A utility method for converting the input stream to Flux of ByteBuffer.
     *
     * @param data The input data which needs to convert to ByteBuffer.
     *
     * @return {@link ByteBuffer} which contains the input data.
     * @throws RuntimeException When I/O error occurs.
     */
    public static Flux<ByteBuffer> convertStreamToByteBuffer(InputStream data) {
        return Flux.just(toByteArray(data))
            .doOnError(error -> LOGGER.warning("Failed to convert stream to byte array - {}", error));
    }

    private static ByteBuffer toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;

            // read bytes from the input stream and store them in buffer
            while ((len = in.read(buffer)) != -1) {
                // write bytes from the buffer into output stream
                os.write(buffer, 0, len);
            }
            return ByteBuffer.wrap(os.toByteArray());
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
