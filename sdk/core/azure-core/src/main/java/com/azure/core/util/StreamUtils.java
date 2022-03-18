// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.util.StreamUtilsImpl;

import java.io.IOException;
import java.io.InputStream;

/**
 * This interface represents utils for streams that can utilize some of APIs that has been introduced to stream
 * apis in later Java.
 * <p>
 * Expected usage of this is through {@link #INSTANCE}.
 */
public interface StreamUtils {
    /**
     * The global instance of {@link StreamUtils} that should be used to maintain object references.
     */
    StreamUtils INSTANCE = new StreamUtilsImpl();

    /**
     * Reads all the bytes from the input stream into byte array.
     * It calls into to {@code InputStream.readAllBytes} on Java 9+.
     * @param inputStream source.
     * @throws IOException if an I/O error occurs.
     * @return a byte array containing the bytes read from this input stream.
     */
    byte[] readAllBytes(InputStream inputStream) throws IOException;
}
