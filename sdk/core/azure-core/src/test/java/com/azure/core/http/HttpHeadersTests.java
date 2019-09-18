// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpHeadersTests {
    @Test
    public void testSet() {
        final HttpHeaders headers = new HttpHeaders();

        headers.put("a", "b");
        assertEquals("b", headers.getValue("a"));

        headers.put("a", "c");
        assertEquals("c", headers.getValue("a"));

        headers.put("a", null);
        assertNull(headers.getValue("a"));

        headers.put("A", "");
        assertEquals("", headers.getValue("a"));

        headers.put("A", "b");
        assertEquals("b", headers.getValue("A"));

        headers.put("a", null);
        assertNull(headers.getValue("a"));
    }
}
