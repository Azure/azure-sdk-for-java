// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for StorageContentValidationDecoderPolicy.
 */
public class StorageContentValidationDecoderPolicyTests {

    @Test
    public void parseRetryStartOffsetFromValidMessage() {
        String message
            = "Incomplete structured message: decoded 512 of 1081 bytes. RETRY-START-OFFSET=287. Stream ended";
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(message);
        assertEquals(287, offset);
    }

    @Test
    public void parseRetryStartOffsetFromMessageWithLargeOffset() {
        String message = "RETRY-START-OFFSET=9999999999";
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(message);
        assertEquals(9999999999L, offset);
    }

    @Test
    public void parseRetryStartOffsetFromMessageWithZeroOffset() {
        String message = "Some error. RETRY-START-OFFSET=0. Details";
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(message);
        assertEquals(0, offset);
    }

    @Test
    public void parseRetryStartOffsetReturnsNegativeOneForNullMessage() {
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(null);
        assertEquals(-1, offset);
    }

    @Test
    public void parseRetryStartOffsetReturnsNegativeOneForMissingToken() {
        String message = "Some error without retry offset";
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(message);
        assertEquals(-1, offset);
    }

    @Test
    public void parseRetryStartOffsetReturnsNegativeOneForEmptyMessage() {
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset("");
        assertEquals(-1, offset);
    }

    @Test
    public void parseRetryStartOffsetReturnsNegativeOneForMalformedToken() {
        String message = "RETRY-START-OFFSET=abc";
        long offset = StorageContentValidationDecoderPolicy.parseRetryStartOffset(message);
        assertEquals(-1, offset);
    }
}
