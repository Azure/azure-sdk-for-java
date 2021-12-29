// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.support.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 */
public class EventHubsMessageConverter extends AbstractAzureMessageConverter<EventData, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageConverter.class);

    private static final Set<String> SYSTEM_HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        EventHubsHeaders.PARTITION_KEY,
        EventHubsHeaders.ENQUEUED_TIME,
        EventHubsHeaders.OFFSET,
        EventHubsHeaders.SEQUENCE_NUMBER)));

    private final ObjectMapper objectMapper;

    /**
     * Construct the message converter with default {@code ObjectMapper}.
     */
    public EventHubsMessageConverter() {
        this.objectMapper = OBJECT_MAPPER;
    }

    /**
     * Construct the message converter with customized {@code ObjectMapper}.
     * @param objectMapper the object mapper.
     */
    public EventHubsMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    protected byte[] getPayload(EventData azureMessage) {
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
        result.put(EventHubsHeaders.ENQUEUED_TIME, azureMessage.getEnqueuedTime());
        result.put(EventHubsHeaders.OFFSET, azureMessage.getOffset());
        result.put(EventHubsHeaders.SEQUENCE_NUMBER, azureMessage.getSequenceNumber());
        result.put(EventHubsHeaders.PARTITION_KEY, azureMessage.getPartitionKey());
        return result;
    }
}
