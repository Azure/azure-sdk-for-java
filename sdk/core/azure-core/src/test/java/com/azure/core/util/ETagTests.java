// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
}
