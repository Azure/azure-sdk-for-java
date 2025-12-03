// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    public void parseDecoderOffsetsFromEnrichedMessage() {
        String message = "Invalid segment size [decoderOffset=523,lastCompleteSegment=287]";
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(message);
        assertArrayEquals(new long[] { 523, 287 }, offsets);
    }

    @Test
    public void parseDecoderOffsetsWithZeroValues() {
        String message = "Header error [decoderOffset=0,lastCompleteSegment=0]";
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(message);
        assertArrayEquals(new long[] { 0, 0 }, offsets);
    }

    @Test
    public void parseDecoderOffsetsWithLargeValues() {
        String message = "Error [decoderOffset=9999999999,lastCompleteSegment=8888888888]";
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(message);
        assertArrayEquals(new long[] { 9999999999L, 8888888888L }, offsets);
    }

    @Test
    public void parseDecoderOffsetsReturnsNullForMissingPattern() {
        String message = "Error without decoder offset information";
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(message);
        assertNull(offsets);
    }

    @Test
    public void parseDecoderOffsetsReturnsNullForNullMessage() {
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(null);
        assertNull(offsets);
    }

    @Test
    public void parseDecoderOffsetsReturnsNullForMalformedPattern() {
        String message = "[decoderOffset=abc,lastCompleteSegment=xyz]";
        long[] offsets = StorageContentValidationDecoderPolicy.parseDecoderOffsets(message);
        assertNull(offsets);
    }
}
