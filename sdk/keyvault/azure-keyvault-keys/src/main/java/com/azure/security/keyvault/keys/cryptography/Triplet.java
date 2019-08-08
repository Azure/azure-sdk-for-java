// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Triplet<T, U, V> {

    private final T left;
    private final U middle;
    private final V right;

    Triplet(T left, U middle, V right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public T getLeft() {
        return left;
    }

    public U getMiddle() {
        return middle;
    }

    public V getRight() {
        return right;
    }
}
