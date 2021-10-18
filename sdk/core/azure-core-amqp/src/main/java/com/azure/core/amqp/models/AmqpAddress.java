// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Objects;

/**
 * This represents amqp address information. This will be used in populating information like 'To', 'ReplyTo' etc.
 *
 * <p><strong>Create and retrieve address</strong></p>
 * <!-- src_embed com.azure.core.amqp.models.AmqpAddress.createAndGet -->
 * <pre>
 * AmqpAddress amqpAddress = new AmqpAddress&#40;&quot;my-address&quot;&#41;;
 * &#47;&#47; Retrieve Adderss
 * String address = amqpAddress.toString&#40;&#41;;
 * System.out.println&#40;&quot;Address &quot; + address&#41;;
 * </pre>
 * <!-- end com.azure.core.amqp.models.AmqpAddress.createAndGet -->
 *
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#type-address-string">
 *     Address type Format.</a>
 */
public final class AmqpAddress {

    private final String address;

    /**
     * Creates the {@link AmqpAddress} with given {@code address}.
     *
     * @param address The address to set for this instance.
     * @throws NullPointerException if {@code address} is null.
     */
    public AmqpAddress(String address) {
        this.address = Objects.requireNonNull(address, "'address' cannot be null.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return address.hashCode();
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

        return Objects.equals(address, other.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.address;
    }
}
