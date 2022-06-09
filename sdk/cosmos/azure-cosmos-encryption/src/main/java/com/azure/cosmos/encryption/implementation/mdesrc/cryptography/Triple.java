/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Class used to represent an object with three elements.
 *
 */
public class Triple<X, Y, Z> {
    final X x;
    final Y y;
    final Z z;

    /**
     * Creates an object with three elements.
     *
     * @param x
     *        element 1
     * @param y
     *        element 2
     * @param z
     *        element 3
     */
    public Triple(X x, Y y, Z z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
