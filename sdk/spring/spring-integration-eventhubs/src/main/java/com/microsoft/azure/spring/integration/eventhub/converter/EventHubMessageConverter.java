/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubMessageConverter extends AbstractAzureMessageConverter<EventData> {

    @Override
    protected byte[] getPayload(EventData azureMessage) {
        return azureMessage.getBody();
    }

    @Override
    protected EventData fromString(String payload) {
        return new EventData(payload.getBytes(Charset.defaultCharset()));
    }

    @Override
    protected EventData fromByte(byte[] payload) {
        return new EventData(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, EventData azureMessage) {
        super.setCustomHeaders(headers, azureMessage);
        headers.forEach((key, value) -> azureMessage.getProperties().put(key, value.toString()));
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventData azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);
        headers.putAll(azureMessage.getProperties());
        return headers;
    }
}
