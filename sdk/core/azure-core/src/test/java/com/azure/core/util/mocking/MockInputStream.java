// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream used for mocking in tests.
 */
public class MockInputStream extends InputStream {
    @Override
    public int read() throws IOException {
        return 0;
    }
}
