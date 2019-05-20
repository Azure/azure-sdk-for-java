// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class StringUtil {
    public static final String EMPTY = "";
    private static final Charset UTF8_CHAR_SET = StandardCharsets.UTF_8;

    public static boolean isNullOrEmpty(String string) {
        return (string == null || string.isEmpty());
    }

    public static boolean isNullOrWhiteSpace(String string) {
        if (string == null) {
            return true;
        }

        for (int index = 0; index < string.length(); index++) {
            if (!Character.isWhitespace(string.charAt(index))) {
                return false;
            }
        }

        return true;
    }

    public static String getShortRandomString() {
        return getRandomString().substring(0, 6);
    }

    public static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    static String convertBytesToString(byte[] bytes) {
        return new String(bytes, UTF8_CHAR_SET);
    }

    static byte[] convertStringToBytes(String string) {
        return string.getBytes(UTF8_CHAR_SET);
    }
}
