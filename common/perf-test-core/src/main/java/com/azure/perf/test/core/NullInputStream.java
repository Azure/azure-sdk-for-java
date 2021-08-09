// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * The Null Input Stream class, applicable to JDK 8.
 */
public class NullInputStream extends InputStream {

    @Override
    public int read() throws IOException {
        return -1;
    }
}
