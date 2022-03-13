// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.implementation;

public class ByteExtensions {
    /**
     * Creates a copy of the source array.
     * @param source The Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }

        byte[] copy = new byte[source.length];

        System.arraycopy(source, 0, copy, 0, source.length);

        return copy;
    }
}
