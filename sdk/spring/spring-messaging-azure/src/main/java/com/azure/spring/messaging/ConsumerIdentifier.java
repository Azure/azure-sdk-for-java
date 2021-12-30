// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * The class to describe the consumer identifier according to the consumer destination and group.
 */
public class ConsumerIdentifier {

    private final String destination;
    private final String group;

    /**
     * Construct an instance via the consumer destination, which is used for Service Bus Queue.
     * @param destination the consumer destination, should be a Service Bus Queue name.
     */
    public ConsumerIdentifier(String destination) {
        this(destination, null);
    }

    /**
     * Construct an instance via the consumer destination and group, which is used for Event Hubs and Service Bus Topic.
     * @param destination the consumer destination.
     * @param group the group.
     */
    public ConsumerIdentifier(String destination, String group) {
        Assert.notNull(destination, "Destination must not be null!");

        this.destination = destination;
        this.group = group;
    }

    /**
     * Get the destination.
     * @return the destination.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Get the group.
     * @return the group.
     */
    public String getGroup() {
        return group;
    }

    /**
     * Return whether the consumer is in a group or not.
     * @return true if the consumer is in a group.
     */
    public boolean hasGroup() {
        return group != null;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ConsumerIdentifier)) {
            return false;
        }

        ConsumerIdentifier consumerIdentifier = (ConsumerIdentifier) o;

        if (!ObjectUtils.nullSafeEquals(destination, consumerIdentifier.destination)) {
            return false;
        }

        return ObjectUtils.nullSafeEquals(group, consumerIdentifier.group);
    }

    @Override
    public int hashCode() {
        int result = ObjectUtils.nullSafeHashCode(destination);
        result = 31 * result + ObjectUtils.nullSafeHashCode(group);
        return result;
    }
}
