// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.DescribedType;

/**
 * Use described type to send OffsetDatetime, Duration and URI on the wire.
 */
public abstract class ServiceBusDescribedType implements DescribedType {

    private final Object descriptor;

    private final Object described;

    /**
     * Set descriptor and described in described type.
     * @param descriptor Amqp symbol to define what kind of described type it is.
     * @param described real value convert to a primitive type.
     */
    public ServiceBusDescribedType(Object descriptor, Object described) {
        this.descriptor = descriptor;
        this.described = described;
    }

    @Override
    public Object getDescriptor() {
        return this.descriptor;
    }

    @Override
    public Object getDescribed() {
        return this.described;
    }

    /**
     * Get the size of described type, the value is descriptor byte size plus described byte size.
     * @return the size of current type.
     */
    public abstract int size();
}
