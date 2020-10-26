// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.experimental.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.Objects;

/**
 * This is amqp message body which represents {@link AmqpBodyType#DATA} type.
 */
public final class AmqpDataBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpDataBody.class);
    private final BinaryData data;

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link Iterable} of byte array.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     */
    public AmqpDataBody(Iterable<BinaryData> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");

        BinaryData payload = null;
        for (BinaryData binaryData : data) {
            if (payload != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of 'data' is allowed in iterator."));
            }
            payload = binaryData;
        }

        if (payload == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'data' can not be empty."));
        } else {
            this.data = payload;
        }
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
        return new IterableStream<>(Collections.singleton(data.toBytes()));
    }

    /**
     * Gets byte array set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public IterableStream<BinaryData> getBinaryData() {
        return new IterableStream<>(Collections.singleton(data));
    }
}
