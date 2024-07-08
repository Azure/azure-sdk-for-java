// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link ETag}.
 */
public class ETagTests {
    private static final String ETAG_CONTENT = "12345";
    private static final String QUOTE_STRING = "\"";
    private static final String WEAK_ETAG_PREFIX_QUOTE = "W/\"";

    @Test
    public void validETag() {
        // Valid Strong ETag
        String strongETagExpect = QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING;
        ETag strongETag = new ETag(strongETagExpect);
        assertEquals(strongETagExpect, strongETag.toString());
        // Valid Weak ETag
        String weakETagExpect = WEAK_ETAG_PREFIX_QUOTE + ETAG_CONTENT + QUOTE_STRING;
        ETag weakETag = new ETag(weakETagExpect);
        assertEquals(weakETagExpect, weakETag.toString());
        // All * ETag
        String allETags = "*";
        ETag asterisk = new ETag(allETags);
        assertEquals(allETags, asterisk.toString());
        assertEquals(allETags, ETag.ALL.toString());
    }

    @Test
    public void invalidETag() {
        // Invalid Strong ETag
        String strongETagExpect = QUOTE_STRING + ETAG_CONTENT;
        assertThrows(IllegalArgumentException.class, () -> new ETag(strongETagExpect));
        // Valid Weak ETag
        String weakETagExpect = WEAK_ETAG_PREFIX_QUOTE + ETAG_CONTENT;
        assertThrows(IllegalArgumentException.class, () -> new ETag(weakETagExpect));
    }

    @Test
    public void equalsTest() {
        ETag validStrongETag = new ETag(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertFalse(validStrongETag.equals(null));
        ETag nullETag = new ETag(null);
        assertTrue(nullETag.equals(new ETag(null)));
        assertFalse(nullETag.equals(validStrongETag));
        assertFalse(validStrongETag.equals(nullETag));
        assertTrue(validStrongETag.equals(validStrongETag));

        ETag validStrongETagCopy = new ETag(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertEquals(validStrongETag, validStrongETagCopy);
    }

    @Test
    public void hashCodeTest() {
        ETag nullETag = new ETag(null);
        ETag validStrongETag = new ETag(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertEquals(0, nullETag.hashCode());
        assertNotEquals(0, validStrongETag.hashCode());
    }
}
