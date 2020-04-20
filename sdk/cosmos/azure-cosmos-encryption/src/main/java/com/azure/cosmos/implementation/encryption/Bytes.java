// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

class Bytes {

    public static final int ONE_BYTE_SIZE = 1;

    public static String toHex(byte[] input) {
        StringBuilder str = new StringBuilder();
        for (byte b : input) {
            str.append(toHex(b));
        }
        return str.toString();
    }

    public static String toHex(byte b) {
        return String.format("%02X", b);
    }
}
