// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    public void testToStringShouldBeRepresentingKeyEqualsignValue() {
        final HttpHeaders headers = new HttpHeaders();
        headers.put("key1", "value1");
        headers.put("key2", "value2");
        headers.put("key3", "value3");

        assertEquals("key1=value1, key2=value2, key3=value3", headers.toString());
    }
}
