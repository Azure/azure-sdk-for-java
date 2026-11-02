// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

/**
 * Hexadecimal formatting selected for the running Java version.
 */
final class HexEncoding {
    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    private HexEncoding() {
    }

    static String toHexString(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        }

        char[] result = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            result[i * 2] = DIGITS[value >>> 4];
            result[i * 2 + 1] = DIGITS[value & 0x0f];
        }
        return new String(result);
    }
}
