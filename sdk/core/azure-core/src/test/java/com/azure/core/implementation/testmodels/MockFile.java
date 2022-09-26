// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.testmodels;

import java.io.File;

/**
 * Implementation of {@link File} used for mocking without Mockito.
 */
public class MockFile extends File {
    private final long length;

    public MockFile(String pathname, long length) {
        super(pathname);

        this.length = length;
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
