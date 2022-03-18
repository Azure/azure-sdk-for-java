// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

    static int getJavaImplementationMajorVersion() {
        return 8;
    }
}
