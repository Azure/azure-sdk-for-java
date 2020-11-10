// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.experimental.models;

import java.util.Objects;

/**
 * This represents amqp address information.
 */
public class AmqpAddress {

    private final String address;

    /**
     * Creates the {@link AmqpAddress} with given {@code address}.
     *
     * @param address to use.
     */
    public AmqpAddress(String address) {
        this.address = Objects.requireNonNull(address, "'address' cannot be null.");
    }

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

        if (!address.equals(((AmqpAddress) other).toString())) {
            return false;
        }

        return true;
    }

    /**
     * Gets string representation of the address.
     *
     * @return string representation of the address.
     */
    @Override
    public String toString() {
        return this.address;
    }
}
