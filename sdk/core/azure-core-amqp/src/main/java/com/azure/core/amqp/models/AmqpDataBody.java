// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.IterableStream;

import java.util.Objects;

/**
 * This is amqp message body which represents {@link AmqpBodyType#DATA} type.
 */
public final class AmqpDataBody implements AmqpMessageBody {
    private final IterableStream<BinaryData> data;
    private final BinaryData binaryData;

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link Iterable} of {@link BinaryData}.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public AmqpDataBody(Iterable<BinaryData> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        this.data = new IterableStream<>(data);
        this.binaryData = null;
    }

    /**
     * Creates instance of {@link AmqpDataBody} with given instance of {@link BinaryData}.
     *
     * @param binaryData to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public AmqpDataBody(BinaryData binaryData) {
        this.binaryData  = Objects.requireNonNull(binaryData, "'binaryData' cannot be null.");
        this.data = null;
    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.DATA;
    }

    /**
     * Gets {@link BinaryData} set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public IterableStream<BinaryData> getData() {
        return data;
    }

    /**
     * Gets {@link BinaryData} set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public BinaryData getBinaryData() {
        return binaryData;
    }
}
