// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.json;

import java.util.Arrays;
import java.util.Objects;

/** An explicit native Cosmos binary scalar. */
final class CosmosBinary {
    private final byte[] value;

    private CosmosBinary(byte[] value) {
        this.value = value;
    }

    static CosmosBinary fromBytes(byte[] value) {
        Objects.requireNonNull(value, "value");
        return new CosmosBinary(value.clone());
    }

    byte[] toByteArray() {
        return value.clone();
    }

    int size() {
        return value.length;
    }

    byte[] unsafeValue() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CosmosBinary && Arrays.equals(value, ((CosmosBinary) other).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return "CosmosBinary[" + value.length + " bytes]";
    }
}
