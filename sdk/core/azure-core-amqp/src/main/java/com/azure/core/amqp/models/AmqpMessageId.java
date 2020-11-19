// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Objects;

/**
 * This represents Message id. Amqp specification support message id in various types. This class only implements
 * {@link String} representation at present.
 *
 * <p><strong>Create and retrieve message id </strong></p>
 * {@codesnippet com.azure.core.amqp.models.AmqpMessageId.createAndGet}
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-message-id-string" target="_blank">
 *     String </a>
 */
public final class AmqpMessageId {
    private final String messageId;

    /**
     * Creates the {@link AmqpMessageId} with given {@code messageId}.
     *
     * @param messageId representing id of the message.
     * @throws NullPointerException if {@code messageId} is null.
     */
    public AmqpMessageId(String messageId) {
        this.messageId = Objects.requireNonNull(messageId, "'messageId' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return messageId.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }

        if (this.getClass() != other.getClass()) {
            return false;
        }

        if (this == other) {
            return true;
        }

        return messageId.equals(other.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.messageId;
    }
}
