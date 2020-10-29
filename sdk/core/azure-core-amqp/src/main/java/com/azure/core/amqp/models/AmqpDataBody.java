// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.experimental.util.BinaryData;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.Objects;

/**
 * This represent amqp payload of {@link AmqpBodyType#DATA} type.
 */
public final class AmqpDataBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpDataBody.class);
    private final BinaryData binaryData;

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link Iterable} of {@link BinaryData}. Please note that this
     * version of the SDK supports only one element in given {@link Iterable} of {@link BinaryData}.
     *
     * <p>The {@link BinaryData} wraps byte array and is an abstraction over many different ways it can be represented.
     * It provides many convenience API including APIs to serialize/deserialize object.
     *
     * @param data to be set on amqp body.
     *
     * @throws NullPointerException if {@code data} is null.
     * @throws IllegalArgumentException if size of 'data' is zero or greater than one.
     *
     * @see BinaryData
     */
    public AmqpDataBody(Iterable<BinaryData> data) {
        Objects.requireNonNull(data, "'data' cannot be null.");
        BinaryData payload = null;
        for (BinaryData binaryData : data) {
            if (payload != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of byte array is allowed in 'data'."));
            }
            payload = binaryData;
        }

        if (payload == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'data' can not be empty."));
        } else {
            this.binaryData = payload;
        }
    }

    /**
     * Creates instance of {@link AmqpDataBody} with given {@link AmqpDataBody} instance.
     *
     * @param data used to create another instance of {@link AmqpDataBody}.
     *
     * @throws NullPointerException if {@code data} or {@link AmqpDataBody#getData() body} is null.
     */
    public AmqpDataBody(AmqpDataBody data) {
        this(Objects.requireNonNull(data, "'data' cannot be null.").getData());
    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.DATA;
    }

    /**
     * Gets {@link IterableStream} of {@link BinaryData} set on this {@link AmqpDataBody}.
     *
     * @return data set on {@link AmqpDataBody}.
     */
    public IterableStream<BinaryData> getData() {
        return new IterableStream<>(Collections.singleton(binaryData));
    }
}
