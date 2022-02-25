// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.DescribedType;

/**
 * Use described type to send OffsetDatetime, Duration, URI etc. on the wire.
 */
public abstract class ServiceBusDescribedType implements DescribedType {

    private final Object descriptor;

    private final Object described;

    /**
     * Set descriptor and described to describe data in described type.
     * @param descriptor use symbolic type in service bus described type.
     * @param described real value in the described type.
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
     * All symbols only contain ASCII, no need to get length by getBytes(StandardCharsets.UTF_8).length, just use length.
     * @return the size of current type to allocate buffer for sending message on the wire.
     */
    public abstract int size();
}
