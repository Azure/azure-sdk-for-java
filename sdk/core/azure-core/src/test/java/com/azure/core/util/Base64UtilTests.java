// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Base64UtilTests {
    @Test
    public void testEncodeAndDecode() {
        byte[] src = new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 };
        byte[] dst = Base64Util.encode(src);
        assertTrue(Arrays.equals(Base64Util.decode(dst), src));
    }

    @Test
    public void testEncodeNullValue() {
        assertNull(Base64Util.encode(null));
    }

    @Test
    public void testDecodeNullValue() {
        assertNull(Base64Util.decode(null));
    }

    @Test
    public void testDecodeString() {
        byte[] src = new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 };
        String dstString = Base64Util.encodeToString(src);
        assertTrue(Arrays.equals(Base64Util.decodeString(dstString), src));
    }

    @Test
    public void testDecodeStringNullValue() {
        assertNull(Base64Util.decodeString(null));
    }

    @Test
    public void testEncodeURLWithoutPaddingNullValue() {
        assertNull(Base64Util.encodeURLWithoutPadding(null));
    }

    @Test
    public void testDecodeURLNullValue() {
        assertNull(Base64Util.decodeURL(null));
    }
}
