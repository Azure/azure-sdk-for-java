// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.io.ByteArrayOutputStream;

/**
 * This class is an extension of {@link ByteArrayOutputStream} which allows access to the backing {@code byte[]} without
 * requiring a copying of the data. The only use of this class is for internal purposes where we know it is safe to
 * directly access the {@code byte[]} without copying.
 */
public class AccessibleByteArrayOutputStream extends ByteArrayOutputStream {
    @Override
    public synchronized byte[] toByteArray() {
        return buf;
    }
}
