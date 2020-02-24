// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;


public class Bytes {

    public static void reverse(byte[] bytes, int offset, int endIndex) {
        for(int i = offset,  j = endIndex - 1; i < j; --j, i++) {
            byte aux = bytes[i];
            bytes[i] = bytes[j];
            bytes[j] = aux;
        }
    }

    public static void reverse(byte[] bytes) {
        Bytes.reverse(bytes, 0, bytes.length);
    }
}
