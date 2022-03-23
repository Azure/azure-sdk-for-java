// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

// This is the base implementation of ReferenceManager, there is another Java 9 specific implementation in
// /src/main/java9 for multi-release JARs.

import com.azure.core.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link StreamUtils}.
 */
public class StreamUtilsImpl implements StreamUtils {

    @Override
    public byte[] readAllBytes(InputStream inputStream) throws IOException {
        return inputStream.readAllBytes();
    }

    static int getJavaImplementationMajorVersion() {
        return 9;
    }
}
