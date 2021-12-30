// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.eventhubs.support.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A converter to turn a {@link com.azure.messaging.eventhubs.models.EventBatchContext} to
 * {@link Message} and vice versa.
 */
public class EventHubBatchMessageConverter extends AbstractAzureMessageConverter<EventBatchContext, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBatchMessageConverter.class);

    private static final Set<String> SYSTEM_HEADERS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
        EventHubsHeaders.PARTITION_KEY,
        EventHubsHeaders.ENQUEUED_TIME,
        EventHubsHeaders.OFFSET,
        EventHubsHeaders.SEQUENCE_NUMBER)));

    private final ObjectMapper objectMapper;

    /**
     * Construct the message converter with default {@code ObjectMapper}.
     */
    public EventHubBatchMessageConverter() {
        this.objectMapper = OBJECT_MAPPER;
    }

    /**
     * Construct the message converter with customized {@code ObjectMapper}.
     * @param objectMapper the object mapper.
     */
    public EventHubBatchMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected ObjectMapper getObjectMapper() {
        return objectMapper;
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
    protected Object getPayload(EventBatchContext azureMessage) {
        return azureMessage.getEvents().stream().map(EventData::getBody).collect(Collectors.toList());
    }

    /**
     * adapt the payload and header for the target message
     *
     * @param azureMessage the context holding the original message payload
     * @param headers headers of original message
     * @param targetPayloadClass the type of target message
     * @param <U> targetPayloadType
     * @return the target message
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <U> Message<?> internalToMessage(EventBatchContext azureMessage, Map<String, Object> headers, Class<U> targetPayloadClass) {
        List<byte[]> payload = (List<byte[]>) getPayload(azureMessage);
        Assert.isTrue(payload != null, "payload must not be null");
        if (targetPayloadClass.isInstance(azureMessage)) {
            return MessageBuilder.withPayload(azureMessage).copyHeaders(headers).build();
        }

        if (targetPayloadClass == String.class) {
            List<String> payLoadList = payload.stream().map(bytes -> new String(bytes, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
            return MessageBuilder.withPayload(payLoadList).copyHeaders(headers).build();
        }

        if (targetPayloadClass == byte[].class) {
            return MessageBuilder.withPayload(payload).copyHeaders(headers).build();
        }
        List<U> payLoadList = payload.stream().map(bytes -> fromPayload(bytes, targetPayloadClass))
            .collect(Collectors.toList());
        return MessageBuilder.withPayload(payLoadList).copyHeaders(headers).build();
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventBatchContext azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);

        List<EventData> events = azureMessage.getEvents();
        List<Object> enqueueTimeList = new ArrayList<>();
        List<Object> offSetList = new ArrayList<>();
        List<Object> sequenceNumberList = new ArrayList<>();
        List<Object> partitionKeyList = new ArrayList<>();
        List<Object> batchConvertedSystemProperties = new ArrayList<>();
        List<Object> batchConvertedApplicationProperties = new ArrayList<>();

        for (EventData event : events) {
            enqueueTimeList.add(event.getEnqueuedTime());
            offSetList.add(event.getOffset());
            sequenceNumberList.add(event.getSequenceNumber());
            partitionKeyList.add(event.getPartitionKey());
            batchConvertedSystemProperties.add(event.getSystemProperties());
            batchConvertedApplicationProperties.add(event.getProperties());
        }
        headers.put(EventHubsHeaders.ENQUEUED_TIME, enqueueTimeList);
        headers.put(EventHubsHeaders.OFFSET, offSetList);
        headers.put(EventHubsHeaders.SEQUENCE_NUMBER, sequenceNumberList);
        headers.put(EventHubsHeaders.PARTITION_KEY, partitionKeyList);
        headers.put(EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES, batchConvertedSystemProperties);
        headers.put(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES, batchConvertedApplicationProperties);

        return headers;
    }

}
