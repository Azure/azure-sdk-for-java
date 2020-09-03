// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;

/**
 *
 */
public class AmqpDataBody implements AmqpMessageBody {
    private AmqpBodyType bodyType;
    private final Iterable<BinaryData> data;
    private final IterableStream<BinaryData> dataStream;

    /**
     *
     * @param data to be set.
     */
    public AmqpDataBody(Iterable<BinaryData> data) {
        this.data = data;
        this.dataStream = new IterableStream<>(data);
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
        return dataStream;
    }
}
