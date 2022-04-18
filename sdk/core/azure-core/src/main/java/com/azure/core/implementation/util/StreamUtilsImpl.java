// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

// This is the base implementation of ReferenceManager, there is another Java 9 specific implementation in
// /src/main/java9 for multi-release JARs.
/**
 * Implementation of {@link StreamUtils}.
 */
public class StreamUtilsImpl implements StreamUtils {

    @Override
    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        // TODO (kasobol-msft) see if we can have better implementation based on what Java 9+ is doing.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int nRead;
        byte[] buffer = new byte[8192];
        while ((nRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            outputStream.write(buffer, 0, nRead);
        }
        return outputStream.toByteArray();
    }

    // TODO (kasobol-msft) think about detecting InputStream subtype and port optimizations for most common subtypes.
    @Override
    public long transfer(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer, 0, 8192)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
        return transferred;
    }

    static int getJavaImplementationMajorVersion() {
        return 8;
    }
}
