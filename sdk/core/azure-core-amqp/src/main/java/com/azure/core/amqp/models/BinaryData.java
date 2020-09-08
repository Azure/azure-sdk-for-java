// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Arrays;
import java.util.Objects;

/**
 * Binary representation of amqp message body.
 */
public final class BinaryData {
    private final byte[] data;

    /**
     * Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     */
    public BinaryData(byte[] data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Gets the data.
     *
     * @return byte array representing {@link BinaryData}.
     */

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
