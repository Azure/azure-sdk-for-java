// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.support.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
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

import static com.azure.spring.messaging.implementation.converter.ObjectMapperHolder.OBJECT_MAPPER;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 */
public class EventHubsMessageConverter extends AbstractAzureMessageConverter<EventData, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsMessageConverter.class);

    private static final Set<String> IGNORED_SPRING_MESSAGE_HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        AzureHeaders.PARTITION_KEY,
        AzureHeaders.BATCH_CONVERTED_PARTITION_KEY,
        EventHubsHeaders.ENQUEUED_TIME,
        EventHubsHeaders.BATCH_CONVERTED_ENQUEUED_TIME,
        EventHubsHeaders.OFFSET,
        EventHubsHeaders.BATCH_CONVERTED_OFFSET,
        EventHubsHeaders.SEQUENCE_NUMBER,
        EventHubsHeaders.BATCH_CONVERTED_SEQUENCE_NUMBER,
        EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES,
        EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES
        )));

    private final ObjectMapper objectMapper;

    /**
     * Construct the message converter with default {@code ObjectMapper}.
     */
    public EventHubsMessageConverter() {
        this(OBJECT_MAPPER);
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
        Set<String> ignoredHeaders = new HashSet<>();
        headers.forEach((key, value) -> {
            if (IGNORED_SPRING_MESSAGE_HEADERS.contains(key)) {
                ignoredHeaders.add(key);
            } else {
                azureMessage.getProperties().put(key, value.toString());
            }
        });

        ignoredHeaders.forEach(header -> LOGGER.info("Message headers {} is not supported to be set and will be "
            + "ignored.", header));
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventData azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);

        headers.putAll(getSystemProperties(azureMessage));
        headers.putAll(azureMessage.getProperties());
        return headers;
    }

    private Map<String, Object> getSystemProperties(EventData azureMessage) {
        Map<String, Object> result = new HashMap<>(azureMessage.getSystemProperties());
        result.put(EventHubsHeaders.ENQUEUED_TIME, azureMessage.getEnqueuedTime());
        result.put(EventHubsHeaders.OFFSET, azureMessage.getOffset());
        result.put(EventHubsHeaders.SEQUENCE_NUMBER, azureMessage.getSequenceNumber());
        result.put(AzureHeaders.PARTITION_KEY, azureMessage.getPartitionKey());
        return result;
    }
}
