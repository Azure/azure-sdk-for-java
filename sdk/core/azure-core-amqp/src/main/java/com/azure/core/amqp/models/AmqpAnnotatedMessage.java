// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.Map;

/**
 *
 */
public class AmqpAnnotatedMessage {
    private AmqpMessageBody body;
    private Map<String, Object> applicationProperties;
    private Map<String, Object> deliveryAnnotations;
    private Map<String, Object> MessageAnnotations;
    private Map<String, Object> footer;
    private AmqpMessageHeader header;
    private AmqpMessageProperties messageProperties;

    public AmqpAnnotatedMessage(AmqpMessageBody body) {
        this.body = body;
    }

    public AmqpAnnotatedMessage(AmqpAnnotatedMessage message) {

    }

    /**
     * Gets the {@link Map} of application properties.
     * @return The application properties.
     */
    public Map<String, Object> getApplicationProperties() {
        return applicationProperties;
    }

    public AmqpMessageBody getBody() {
        return this.body;
    }

    public Map<String, Object> getDeliveryAnnotations() {
        return this.deliveryAnnotations;
    }

    public Map<String, Object> getFooter() {
        return this.footer;
    }

    public AmqpMessageHeader getHeader() {
        return this.header;
    }

    public Map<String, Object> getMessageAnnotations() {
        return this.MessageAnnotations;
    }

    public AmqpMessageProperties getProperties() {
        return messageProperties;
    }

}
