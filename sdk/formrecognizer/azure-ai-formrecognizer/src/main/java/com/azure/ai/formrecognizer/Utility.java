// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Utility method class.
 */
public class Utility {
    private static final ClientLogger logger = new ClientLogger(Utility.class);

    /**
     * A utility method for converting the input stream to Flux of ByteBuffer.
     *
     * @param data The input data which needs to convert to ByteBuffer.
     *
     * @return {@link ByteBuffer} which contains the input data.
     * @throws RuntimeException When I/O error occurs.
     */
    public static Flux<ByteBuffer> convertStreamToByteBuffer(InputStream data) {
        try {
            return Flux.just(toByteArray(data));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    private static ByteBuffer toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return ByteBuffer.wrap(os.toByteArray());
    }
}
