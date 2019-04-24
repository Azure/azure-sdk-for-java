// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HttpHeaderTests {
    @Test
    public void addValue() {
        final HttpHeader header = new HttpHeader("a", "b");
        header.addValue("c");
        assertEquals("a:b,c", header.toString());
    }
}
