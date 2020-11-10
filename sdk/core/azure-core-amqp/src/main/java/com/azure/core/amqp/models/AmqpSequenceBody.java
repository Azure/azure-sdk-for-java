// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents amqp sequence body type.
 */
public final class AmqpSequenceBody implements AmqpMessageBody {
    private final ClientLogger logger = new ClientLogger(AmqpSequenceBody.class);
    private final List<Object> sequenceData;

    /**
     * Creates {@link AmqpSequenceBody} with given {@code sequenceData}.
     * @param sequenceData to use.
     * @throws IllegalArgumentException if size of 'sequenceData' is zero or greater than one.
     */
    public AmqpSequenceBody(Iterable<List<Object>> sequenceData) {
        Objects.requireNonNull(sequenceData, "'sequenceData' cannot be null.");
        List<Object> payload = null;
        for (List<Object> binaryData : sequenceData) {
            if (payload != null) {
                throw logger.logExceptionAsError(new IllegalArgumentException(
                    "Only one instance of List is allowed in 'sequenceData'."));
            }
            payload = binaryData;
        }

        if (payload == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'sequenceData' can not be empty."));
        } else {
            this.sequenceData = payload;
        }

    }

    @Override
    public AmqpBodyType getBodyType() {
        return AmqpBodyType.SEQUENCE;
    }

    /**
     * Gets an list containing only one  element of type {@link List} set on {@link AmqpSequenceBody}.
     * @return data set on {@link AmqpSequenceBody}.
     */
    public List<List<Object>> getSequence() {
        return Collections.singletonList(sequenceData);
    }
}
