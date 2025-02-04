// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.structuredmessage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StructuredMessageFlagsTests {
    @Test
    public void testNoneFlag() {
        StructuredMessageFlags flag = StructuredMessageFlags.NONE;
        assertEquals(0, flag.getValue());
    }

    @Test
    public void testStorageCrc64Flag() {
        StructuredMessageFlags flag = StructuredMessageFlags.STORAGE_CRC64;
        assertEquals(1, flag.getValue());
    }

    @Test
    public void testFromStringValid() {
        assertEquals(StructuredMessageFlags.NONE, StructuredMessageFlags.fromString("0"));
        assertEquals(StructuredMessageFlags.STORAGE_CRC64, StructuredMessageFlags.fromString("1"));
    }

    @Test
    public void testFromStringInvalid() {
        assertNull(StructuredMessageFlags.fromString("2"));
        assertNull(StructuredMessageFlags.fromString(null));
        assertThrows(NumberFormatException.class, () -> {
            StructuredMessageFlags.fromString("invalid");
        });
    }
}
