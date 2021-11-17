// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.tracing.sleuth.implementation;

import javax.annotation.concurrent.Immutable;

import static com.azure.spring.tracing.sleuth.implementation.TraceContextUtil.isValidBase16String;

@Immutable
public final class TraceId {
    private static final ThreadLocal<char[]> CHAR_BUFFER = new ThreadLocal<>();

    private static final int BYTES_LENGTH = 16;
    private static final int HEX_LENGTH = 2 * BYTES_LENGTH;
    private static final String INVALID = "00000000000000000000000000000000";

    /**
     * Returns the length of the lowercase hex (base16) representation of the {@code TraceId}.
     *
     * @return the length of the lowercase hex (base16) representation of the {@code TraceId}.
     */
    public static int getLength() {
        return HEX_LENGTH;
    }

    /**
     * Returns the invalid {@code TraceId} in lowercase hex (base16) representation. All characters are "0".
     *
     * @return the invalid {@code TraceId} in lowercase hex (base16) representation.
     */
    public static String getInvalid() {
        return INVALID;
    }

    /**
     * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 32 character hex String, where at
     * least one of the characters is not a '0'.
     *
     * @return {@code true} if the {@code TraceId} is valid.
     */
    public static boolean isValid(CharSequence traceId) {
        return traceId != null
            && traceId.length() == HEX_LENGTH
            && !INVALID.contentEquals(traceId)
            && isValidBase16String(traceId);
    }
}
