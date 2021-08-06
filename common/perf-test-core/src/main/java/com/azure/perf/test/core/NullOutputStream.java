// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.OutputStream;

/**
 * The Null Output Stream class, applicable to JDK 8.
 */
public class NullOutputStream extends OutputStream {
    @Override
    public void write(int b) { }

    @Override
    public void write(byte[] b) { }

    @Override
    public void write(byte[] b, int off, int len) { }
}
