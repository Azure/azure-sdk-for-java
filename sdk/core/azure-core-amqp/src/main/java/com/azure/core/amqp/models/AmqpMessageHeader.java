// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import com.azure.core.annotation.Fluent;

import java.time.Duration;

/**
 * Represents Header from Amqp message.
 */
@Fluent
public class AmqpMessageHeader {

    private Integer deliveryCount;
    private Boolean durable;
    private Boolean firstAcquirer;
    private Byte priority;
    private Duration timeToLive;

    AmqpMessageHeader() {
        // This class does not have any public constructors, and is not able to be instantiated using 'new'.
    }

    /**
     * Gets delivery count.
     *
     * @return delivery count.
     */
    public Integer getDeliveryCount() {
        return deliveryCount;
    }

    /**
     * Sets the given {@code deliveryCount} value on {@link AmqpMessageHeader} object.
     *
     * @param deliveryCount to be set.
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setDeliveryCount(Integer deliveryCount) {
        this.deliveryCount = deliveryCount;
        return this;
    }

    /**
     * Gets durable boolean flag.
     *
     * @return The durable.
     */
    public Boolean getDurable() {
        return this.durable;
    }

    /**
     * Sets the given {@code durable} value on {@link AmqpMessageHeader} object.
     *
     * @param durable to set on {@link AmqpMessageHeader}.
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setDurable(Boolean durable) {
        this.durable = durable;
        return this;
    }

    /**
     * Gets boolean flag for {@code firstAcquirer}
     *
     * @return The {@code firstAcquirer}.
     */
    public Boolean getFirstAcquirer() {
        return this.firstAcquirer;
    }

    /**
     * Sets the given {@code firstAcquirer} value on {@link AmqpMessageHeader} object.
     *
     * @param firstAcquirer to set on {@link AmqpMessageHeader}.
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setFirstAcquirer(Boolean firstAcquirer) {
        this.firstAcquirer = firstAcquirer;
        return this;
    }

    /**
     * Gets the priority on {@code amqpMessage}.
     * @return the priority.
     */
    public Byte getPriority() {
        return this.priority;
    }

    /**
     * Sets the given {@code priority} value on {@link AmqpMessageHeader} object.
     *
     * @param priority to set on {@link AmqpMessageHeader}.
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setPriority(Byte priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets {@code timeToLive}.
     * @return {@code timeToLive}.
     */
    public Duration getTimeToLive() {
        return this.timeToLive;
    }

    /**
     * Sets the given {@code timeToLive} value on {@link AmqpMessageHeader} object.
     *
     * @param timeToLive to set on {@link AmqpMessageHeader}.
     * @return updated {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader setTimeToLive(Duration timeToLive) {
        this.timeToLive =  timeToLive;
        return this;
    }
}
