// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Objects;

/**
 * This represents Message id in various forms.
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/amqp-core-messaging-v1.0.html#type-message-id-ulong">
 *     Amqp Message Id.</a>
 */
public final class AmqpMessageId {
    private final String messageId;

    /**
     * Creates the {@link AmqpMessageId} with given {@code messageId}.
     *
     * @param messageId representing id of the message.
     */
    public AmqpMessageId(String messageId) {
        this.messageId = Objects.requireNonNull(messageId, "'messageId' cannot be null.");
    }

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

        if (!messageId.equals(((AmqpMessageId) other).toString())) {
            return false;
        }

        return true;
    }

    /**
     * Gets string representation of the message id.
     *
     * @return string representation of the message id.
     */
    @Override
    public String toString() {
        return this.messageId;
    }
}
