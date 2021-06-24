// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.Strings;

import java.nio.ByteBuffer;

public class HexConvert {
    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    final private static byte[] emptyByteArray = new byte[0];

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(ByteBuffer byteBuffer) {
        return bytesToHex(byteBuffer, false);
    }

    public static String bytesToHex(ByteBuffer byteBuffer, boolean useDelimiter) {
        char[] hexChars = useDelimiter ?
            new char[byteBuffer.limit() * 2 + byteBuffer.limit() - 1] :
            new char[byteBuffer.limit() * 2];
        for (int j = 0; j < byteBuffer.limit(); j++) {
            int v = byteBuffer.array()[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            if (useDelimiter && j < (byteBuffer.limit() - 1)) {
                hexChars[j * 3 + 2] = '-';
            }
        }

        return new String(hexChars);
    }

    public static byte[] hexToByteBuffer(String hexString) {
        if (Strings.isNullOrEmpty(hexString)) {
            return emptyByteArray;
        }
        String str = hexString.replace("-", "");
        if(str.startsWith("0x")) { // Get rid of potential prefix
            str = str.substring(2);
        }

        if(str.length() % 2 != 0) { // If string is not of even length
            str = '0' + str; // Assume leading zeroes were left out
        }

        byte[] result = new byte[str.length() / 2];
        for(int i = 0; i < str.length(); i += 2) {
            String nextByte = str.charAt(i) + "" + str.charAt(i + 1);
            // To avoid overflow, parse as int and truncate:
            result[i / 2] = (byte) Integer.parseInt(nextByte, 16);
        }
        return result;
    }
}