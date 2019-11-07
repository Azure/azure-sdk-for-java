// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Encodes and decodes using Base64 URL encoding.
 */
public final class Base64Url {
    /**
     * The Base64Url encoded bytes.
     */
    private final byte[] bytes;

    /**
     * Creates a new Base64Url object with the specified encoded string.
     *
     * @param string The encoded string.
     */
    public Base64Url(String string) {
        if (string == null) {
            this.bytes = null;
        } else {
            string = unquote(string);
            this.bytes = string.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Creates a new Base64Url object with the specified encoded bytes.
     *
     * @param bytes The encoded bytes.
     */
    public Base64Url(byte[] bytes) {
        this.bytes = unquote(bytes);
    }

    private static byte[] unquote(byte[] bytes) {
        if (bytes != null && bytes.length > 1) {
            bytes = unquote(new String(bytes, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
        }
        return bytes;
    }

    private static String unquote(String string) {
        if (string != null && !string.isEmpty()) {
            final char firstCharacter = string.charAt(0);
            if (firstCharacter == '\"' || firstCharacter == '\'') {
                final int base64UrlStringLength = string.length();
                final char lastCharacter = string.charAt(base64UrlStringLength - 1);
                if (lastCharacter == firstCharacter) {
                    string = string.substring(1, base64UrlStringLength - 1);
                }
            }
        }
        return string;
    }

    /**
     * Encodes a byte array into Base64Url encoded bytes.
     *
     * @param bytes The byte array to encode.
     * @return A new Base64Url instance.
     */
    public static Base64Url encode(byte[] bytes) {
        if (bytes == null) {
            return new Base64Url((String) null);
        } else {
            return new Base64Url(Base64Util.encodeURLWithoutPadding(bytes));
        }
    }

    /**
     * Returns the underlying encoded byte array.
     *
     * @return The underlying encoded byte array.
     */
    public byte[] encodedBytes() {
        return CoreUtils.clone(bytes);
    }

    /**
     * Decode the bytes and returns its value.
     *
     * @return The decoded byte array.
     */
    public byte[] decodedBytes() {
        if (this.bytes == null) {
            return null;
        }

        return Base64Util.decodeURL(bytes);
    }

    @Override
    public String toString() {
        return bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Base64Url)) {
            return false;
        }

        Base64Url rhs = (Base64Url) obj;
        return Arrays.equals(this.bytes, rhs.encodedBytes());
    }
}
