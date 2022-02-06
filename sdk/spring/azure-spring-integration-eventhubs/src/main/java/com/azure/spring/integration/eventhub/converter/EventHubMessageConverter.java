// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubMessageConverter extends AbstractAzureMessageConverter<EventData, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubMessageConverter.class);

    @Override
    protected Object getPayload(EventData azureMessage) {
        return azureMessage.getBody();
    }

    @Override
    protected EventData fromString(String payload) {
        return new EventData(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected EventData fromByte(byte[] payload) {
        return new EventData(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, EventData azureMessage) {
        super.setCustomHeaders(headers, azureMessage);
        headers.forEach((key, value) -> {
            if (SYSTEM_HEADERS.contains(key)) {
                LOGGER.warn("System property {}({}) is not allowed to be defined and will be ignored.",
                    key, value);
            } else {
                azureMessage.getProperties().put(key, value.toString());
            }
        });
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventData azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);

        headers.putAll(getSystemProperties(azureMessage));
        headers.putAll(azureMessage.getProperties());
        return headers;
    }

    private Map<String, Object> getSystemProperties(EventData azureMessage) {
        Map<String, Object> result = new HashMap<>();
        result.putAll(azureMessage.getSystemProperties());
        result.put(EventHubHeaders.ENQUEUED_TIME, azureMessage.getEnqueuedTime());
        result.put(EventHubHeaders.OFFSET, azureMessage.getOffset());
        result.put(EventHubHeaders.SEQUENCE_NUMBER, azureMessage.getSequenceNumber());
        result.put(EventHubHeaders.PARTITION_KEY, azureMessage.getPartitionKey());
        return result;
    }
}
