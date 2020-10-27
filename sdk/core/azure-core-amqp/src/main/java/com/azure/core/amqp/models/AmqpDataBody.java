// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.experimental.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * This is amqp message body which represents {@link AmqpBodyType#DATA} type.
 */
public final class AmqpDataBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpDataBody.class);
    private final BinaryData binaryData;

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link Iterable} of byte array. As of now,  We support
     * only one element in the given iterable.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     * @throws IllegalArgumentException if elements in 'data' is zero or greater than one.
     */
    public AmqpDataBody(Iterable<byte[]> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        BinaryData payload = null;
        for (byte[] bytes : data) {
            if (payload != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of byte array is allowed in 'data'."));
            }
            payload = BinaryData.fromBytes(bytes);
        }

        if (payload == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'data' can not be empty."));
        } else {
            this.binaryData = payload;
        }
    }

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link List} of {@link BinaryData}. As of now,  We support
     * only one element in the given collection.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     * @throws IllegalArgumentException if size of 'data' is zero or greater than one.
     */
    public AmqpDataBody(List<BinaryData> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");

        if (data.size() < 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'data' can not be empty."));
        } else if (data.size() > 1) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "Only one element is supported in 'data'."));
        }

        this.binaryData = data.get(0);
    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.DATA;
    }

    /**
     * Gets byte array set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public IterableStream<byte[]> getData() {
        return new IterableStream<>(Collections.singleton(binaryData.toBytes()));
    }

    /**
     * Gets {@link IterableStream} of {@link BinaryData} set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public IterableStream<BinaryData> getDataAsBinaryData() {
        return new IterableStream<>(Collections.singleton(binaryData));
    }
}
