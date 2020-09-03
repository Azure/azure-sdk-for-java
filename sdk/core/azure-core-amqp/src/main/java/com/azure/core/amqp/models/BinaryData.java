// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Binary representation of amqp message body.
 */
public class BinaryData {
    private final byte[] data;
    /**
     *Create {@link BinaryData} instance with given byte array data.
     *
     * @param data to use.
     */
    public BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     *
     * @return byte array representing {@link BinaryData}.
     */

    public byte[] getData() {
        return data;
    }
}
