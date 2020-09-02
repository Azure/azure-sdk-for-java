// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 *
 */
public class BinaryData {
    private byte[] data;
    /**
     *
     * @param data
     */
    public BinaryData(byte[] data) {
        this.data = data;
    }

    /**
     *
     * @return
     */

    public byte[] getData() {
        return data;
    }
}
