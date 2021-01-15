// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * The representation of message header as defined by AMQP protocol.
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-messaging-v1.0-os.html#section-message-format" target="_blank">
 * Amqp Message Format.</a>
 */
@Fluent
public class AmqpMessageHeader {

    private Long deliveryCount;
    private Boolean durable;
    private Boolean firstAcquirer;
    private Short priority;
    private Duration timeToLive;

    AmqpMessageHeader() {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
    }

    /**
     * Gets the delivery count from amqp message header.
     *
     * @return the delivery count value.
     */
    public Long getDeliveryCount() {
        return deliveryCount;
    }

    /**
     * Sets the given {@code deliveryCount} value on {@link AmqpMessageHeader} object.
     * @param deliveryCount to be set.
     *
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setDeliveryCount(Long deliveryCount) {
        this.deliveryCount = deliveryCount;
        return this;
    }

    /**
     * Gets durable boolean flag from amqp message header.
     * @return the durable flag.
     */
    public Boolean isDurable() {
        return durable;
    }

    /**
     * Sets the given {@code durable} value on {@link AmqpMessageHeader} object.
     * @param durable to set on {@link AmqpMessageHeader}.
     *
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setDurable(Boolean durable) {
        this.durable = durable;
        return this;
    }

    /**
     * Gets boolean flag for {@code firstAcquirer} from amqp message header.
     * @return the {@code firstAcquirer} value.
     */
    public Boolean isFirstAcquirer() {
        return this.firstAcquirer;
    }

    /**
     * Sets the given {@code firstAcquirer} value on {@link AmqpMessageHeader} object.
     * @param firstAcquirer to set on {@link AmqpMessageHeader}.
     *
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setFirstAcquirer(Boolean firstAcquirer) {
        this.firstAcquirer = firstAcquirer;
        return this;
    }

    /**
     * Gets the priority on {@code amqpMessage} from amqp message header.
     * @return the {@code priority} value.
     */
    public Short getPriority() {
        return priority;
    }

    /**
     * Sets the given {@code priority} value on {@link AmqpMessageHeader} object.
     * @param priority to set on {@link AmqpMessageHeader}.
     *
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setPriority(Short priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets {@code timeToLive} from amqp message header.
     *
     * @return the {@code timeToLive} value.
     */
    public Duration getTimeToLive() {
        return timeToLive;
    }

    /**
     * Sets the given {@code timeToLive} value on {@link AmqpMessageHeader} object.
     * @param timeToLive to set on {@link AmqpMessageHeader}.
     *
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive;
        return this;
    }
}
