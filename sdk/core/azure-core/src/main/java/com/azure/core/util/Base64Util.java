// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import java.util.Base64;

/**
 * Utility type exposing Base64 encoding and decoding methods.
 */
public final class Base64Util {
    /**
     * Encodes a byte array to base64.
     * @param src the byte array to encode
     * @return the base64 encoded bytes
     */
    public static byte[] encode(byte[] src) {
        return src == null ? null : Base64.getEncoder().encode(src);
    }

    /**
     * Encodes a byte array to base64 URL format.
     * @param src the byte array to encode
     * @return the base64 URL encoded bytes
     */
    public static byte[] encodeURLWithoutPadding(byte[] src) {
        return src == null ? null : Base64.getUrlEncoder().withoutPadding().encode(src);
    }

    /**
     * Encodes a byte array to a base 64 string.
     * @param src the byte array to encode
     * @return the base64 encoded string
     */
    public static String encodeToString(byte[] src) {
        return src == null ? null : Base64.getEncoder().encodeToString(src);
    }

    /**
     * Decodes a base64 encoded byte array.
     * @param encoded the byte array to decode
     * @return the decoded byte array
     */
    public static byte[] decode(byte[] encoded) {
        return encoded == null ? null : Base64.getDecoder().decode(encoded);
    }

    /**
     * Decodes a byte array in base64 URL format.
     * @param src the byte array to decode
     * @return the decoded byte array
     */
    public static byte[] decodeURL(byte[] src) {
        return src == null ? null : Base64.getUrlDecoder().decode(src);
    }

    /**
     * Decodes a base64 encoded string.
     * @param encoded the string to decode
     * @return the decoded byte array
     */
    public static byte[] decodeString(String encoded) {
        return encoded == null ? null : Base64.getDecoder().decode(encoded);
    }

    // Private Ctr
    private Base64Util() {
    }
}
