// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import java.io.File;

/**
 * Implementation of {@link File} used for mocking.
 */
public class MockFile extends File {
    private final long length;
    private final byte[] data;

    public MockFile(String pathname, byte[] data, long length) {
        super(pathname);

        this.data = data;
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long length() {
        return length;
    }
}
