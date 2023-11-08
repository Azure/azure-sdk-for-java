// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream used for mocking in tests.
 */
public class MockOutputStream extends OutputStream {
    @Override
    public void write(int b) throws IOException {

    }
}
