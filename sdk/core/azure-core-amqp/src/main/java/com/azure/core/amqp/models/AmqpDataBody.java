// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;

/**
 *
 */
public class AmqpDataBody implements AmqpMessageBody {
    private final AmqpBodyType bodyType;
    private final IterableStream<BinaryData> data;

    /**
     *
     * @param data to be set.
     */
    public AmqpDataBody(Iterable<BinaryData> data) {
        this.data = new IterableStream<>(data);
        this.bodyType = AmqpBodyType.DATA;
    }

    @Override
    public AmqpBodyType getBodyType() {
        return bodyType;
    }

    /**
     *
     * @return data.
     */
    public IterableStream<BinaryData> getData() {
        return data;
    }
}
