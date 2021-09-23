// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import javax.annotation.concurrent.Immutable;

import static com.azure.spring.tracing.sleuth.implementation.TraceContextUtil.isValidBase16String;

/**
 * Helper methods for dealing with a span identifier. A valid span identifier is a 16 character lowercase hex (base16)
 * String, where at least one of the characters is not a "0".
 *
 * <p>There are two more other representation that this class helps with:
 *
 * <ul>
 *   <li>Bytes: a 8-byte array, where valid means that at least one of the bytes is not `\0`.
 *   <li>Long: a {@code long} value, where valid means that the value is non-zero.
 * </ul>
 */
@Immutable
public final class SpanId {
    private static final ThreadLocal<char[]> CHAR_BUFFER = new ThreadLocal<>();

    private static final int BYTES_LENGTH = 8;
    private static final int HEX_LENGTH = 2 * BYTES_LENGTH;
    private static final String INVALID = "0000000000000000";

    private SpanId() {
    }

    /**
     * Returns the length of the lowercase hex (base16) representation of the {@code SpanId}.
     *
     * @return the length of the lowercase hex (base16) representation of the {@code SpanId}.
     */
    public static int getLength() {
        return HEX_LENGTH;
    }

    /**
     * Returns the invalid {@code SpanId} in lowercase hex (base16) representation. All characters are "0".
     *
     * @return the invalid {@code SpanId} lowercase in hex (base16) representation.
     */
    public static String getInvalid() {
        return INVALID;
    }

    /**
     * Returns whether the span identifier is valid. A valid span identifier is a 16 character hex String, where at
     * least one of the characters is not a '0'.
     *
     * @return {@code true} if the span identifier is valid.
     */
    public static boolean isValid(CharSequence spanId) {
        return spanId != null
            && spanId.length() == HEX_LENGTH
            && !INVALID.contentEquals(spanId)
            && isValidBase16String(spanId);
    }
}
