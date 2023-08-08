/*
 * Copyright (c) Microsoft Corporation. All rights reserved. Licensed under the MIT License.
 */

package com.azure.cosmos.encryption.implementation.mdesrc.cryptography;

/**
 * Class used to represent an object with two elements.
 *
 */
public class Tuple<Y, Z> {
    final Y y;
    final Z z;

    /**
     * Creates an object with two elements.
     *
     * @param y
     *        element 1
     * @param z
     *        element 2
     */
    public Tuple(Y y, Z z) {
        this.y = y;
        this.z = z;
    }
}
