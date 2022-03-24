/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Class used to represent an object with four elements.
 *
 */
public class Quadruple<W, X, Y, Z> {
    final W w;
    final X x;
    final Y y;
    final Z z;

    /**
     * Creates an object with four elements.
     *
     * @param w
     *        element 1
     * @param x
     *        element 2
     * @param y
     *        element 3
     * @param z
     *        element 4
     */
    public Quadruple(W w, X x, Y y, Z z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
