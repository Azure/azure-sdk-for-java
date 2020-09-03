// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Amqp representation of the message.
 */
public final class AmqpAnnotatedMessage {
    private final AmqpMessageBody amqpMessageBody;
    private final Map<String, Object> applicationProperties;
    private final Map<String, Object> deliveryAnnotations;
    private final Map<String, Object> messageAnnotations;
    private final Map<String, Object> footer;
    private final AmqpMessageHeader header;
    private final AmqpMessageProperties properties;

    /**
     * Creates instance of {@link AmqpAnnotatedMessage} with given {@link AmqpMessageBody}.
     *
     * @param body to be set on amqp message.
     */
    public AmqpAnnotatedMessage(AmqpMessageBody body) {
        this.amqpMessageBody = Objects.requireNonNull(body, "'body' cannot be null.");;
        this.applicationProperties = new HashMap<>();
        this.deliveryAnnotations = new HashMap<>();
        this.messageAnnotations = new HashMap<>();
        this.footer = new HashMap<>();
        this.header = new AmqpMessageHeader();
        this.properties = new AmqpMessageProperties();
    }

    /**
     * Creates instance of {@link AmqpAnnotatedMessage} with given {@link AmqpAnnotatedMessage}.
     *
     * @param message used to create another instance of {@link AmqpAnnotatedMessage}.
     */
    public AmqpAnnotatedMessage(AmqpAnnotatedMessage message) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        this.amqpMessageBody = Objects.requireNonNull(message.getBody(), "'message.body' cannot be null.");
        this.applicationProperties = message.getApplicationProperties();
        this.deliveryAnnotations = message.getDeliveryAnnotations();
        this.messageAnnotations = message.getMessageAnnotations();
        this.footer = message.getFooter();
        this.header = message.getHeader();
        this.properties = message.getProperties();
    }

    /**
     * Gets the {@link Map} of application properties.
     * @return The application properties.
     */
    public Map<String, Object> getApplicationProperties() {
        return applicationProperties;
    }

    /**
     * Gets the {@link AmqpMessageBody}.
     *
     * @return the {@link AmqpMessageBody} object.
     */
    public AmqpMessageBody getBody() {
        return amqpMessageBody;
    }

    /**
     * Gets the {@link Map} representation of delivery annotations.
     *
     * @return the {@link Map} representation of delivery annotations.
     */
    public Map<String, Object> getDeliveryAnnotations() {
        return deliveryAnnotations;
    }

    /**
     * Gets the {@link Map} representation of footer.
     *
     * @return the {@link Map} representation of footer.
     */
    public Map<String, Object> getFooter() {
        return footer;
    }

    /**
     * Gets the {@link AmqpMessageHeader}.
     *
     * @return the {@link AmqpMessageHeader} object.
     */
    public AmqpMessageHeader getHeader() {
        return header;
    }

    /**
     * Gets the {@link Map} representation of message annotations.
     *
     * @return the {@link Map} representation of message annotations.
     */
    public Map<String, Object> getMessageAnnotations() {
        return messageAnnotations;
    }

    /**
     * Gets the {@link AmqpMessageProperties}.
     *
     * @return the {@link AmqpMessageProperties} object.
     */
    public AmqpMessageProperties getProperties() {
        return properties;
    }
}
