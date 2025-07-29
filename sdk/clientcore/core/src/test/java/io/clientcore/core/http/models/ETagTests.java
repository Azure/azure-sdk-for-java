// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link ETag}.
 */
public class ETagTests {
    private static final String ETAG_CONTENT = "12345";
    private static final String QUOTE_STRING = "\"";

    @Test
    public void validETag() {
        // Valid Strong ETag
        String strongETagExpect = QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING;
        ETag strongETag = ETag.fromString(strongETagExpect);
        assertEquals(strongETagExpect, strongETag.toString());
        // Valid Weak ETag
        String weakETagExpect = ETag.WEAK_ETAG_PREFIX_QUOTE + ETAG_CONTENT + QUOTE_STRING;
        ETag weakETag = ETag.fromString(weakETagExpect);
        assertEquals(weakETagExpect, weakETag.toString());
        // All * ETag
        String allETags = ETag.ASTERISK;
        ETag asterisk = ETag.fromString(allETags);
        assertEquals(allETags, asterisk.toString());
        assertEquals(allETags, ETag.ALL.toString());
        assertSame(ETag.ALL, asterisk);
    }

    @Test
    public void invalidETag() {
        // Invalid Strong ETag
        String strongETagExpect = QUOTE_STRING + ETAG_CONTENT;
        assertThrows(IllegalArgumentException.class, () -> ETag.fromString(strongETagExpect));
        // Valid Weak ETag
        String weakETagExpect = ETag.WEAK_ETAG_PREFIX_QUOTE + ETAG_CONTENT;
        assertThrows(IllegalArgumentException.class, () -> ETag.fromString(weakETagExpect));
    }

    @Test
    public void equalsTest() {
        ETag validStrongETag = ETag.fromString(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertNotEquals(null, validStrongETag);

        ETag nullETag = ETag.fromString(null);
        assertSame(nullETag, ETag.fromString(null));
        assertNotEquals(nullETag, validStrongETag);

        ETag validStrongETagCopy = ETag.fromString(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertEquals(validStrongETag, validStrongETagCopy);
    }

    @Test
    public void hashCodeTest() {
        ETag nullETag = ETag.fromString(null);
        ETag validStrongETag = ETag.fromString(QUOTE_STRING + ETAG_CONTENT + QUOTE_STRING);
        assertEquals(0, nullETag.hashCode());
        assertNotEquals(0, validStrongETag.hashCode());
    }
}
