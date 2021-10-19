// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.LinkedMultiValueMap;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

/**
 * A converter to turn a {@link com.azure.messaging.eventhubs.models.EventBatchContext} to {@link Message} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubBatchMessageConverter extends AbstractAzureMessageConverter<EventBatchContext, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBatchMessageConverter.class);

    @Override
    protected byte[] getPayload(EventBatchContext azureMessage) {
        List<EventData> events = azureMessage.getEvents();

        List<byte[]> payloadList = new ArrayList<>();
        for (EventData event : events) {
            payloadList.add(event.getBody());
        }
        return payloadList
            .stream()
            .collect(
                () -> new ByteArrayOutputStream(),
                (b, e) -> b.write(e, 0, e.length),
                (a, b) -> {})
            .toByteArray();
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
            if (key.equals(NATIVE_HEADERS) && value instanceof LinkedMultiValueMap) {
                azureMessage.getProperties().put(key, toJson(value));
            } else {
                if (SYSTEM_HEADERS.contains(key)) {
                    LOGGER.warn("System property {}({}) is not allowed to be defined and will be ignored.",
                        key, value);
                } else {
                    azureMessage.getProperties().put(key, value.toString());
                }
            }
        });
    }

    @Override
    public <U> Message<U> toMessage(@NonNull EventBatchContext azureMessage,
                                    Map<String, Object> headers,
                                    @NonNull Class<U> targetPayloadClass) {
        Map<String, Object> mergedHeaders = new HashMap<>();
        mergedHeaders.putAll(buildCustomHeaders(azureMessage));
        mergedHeaders.putAll(headers);
        return (Message<U>) internalToMessage(azureMessage, mergedHeaders, targetPayloadClass);

    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventBatchContext azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);

        List<EventData> events = azureMessage.getEvents();
        List<Object> enqueueTimeList = new ArrayList<>();
        List<Object> offSetList = new ArrayList<>();
        List<Object> sequeneceNumberList = new ArrayList<>();
        List<Object> partitionKeyList = new ArrayList<>();
        List<Object> batchConvertedSystemProperties = new ArrayList<>();
        List<Object> batchConvertedApplicationProperties = new ArrayList<>();

        for (EventData event : events) {
            enqueueTimeList.add(event.getEnqueuedTime());
            offSetList.add(event.getOffset());
            sequeneceNumberList.add(event.getSequenceNumber());
            partitionKeyList.add(event.getPartitionKey());
            batchConvertedSystemProperties.add(event.getSystemProperties());
            Map<String, Object> applicationProperties = event.getProperties();
            convertNativeHeadersIfNeeded(applicationProperties);
            batchConvertedApplicationProperties.add(event.getProperties());
        }
        headers.put(EventHubHeaders.ENQUEUED_TIME, enqueueTimeList);
        headers.put(EventHubHeaders.OFFSET, offSetList);
        headers.put(EventHubHeaders.SEQUENCE_NUMBER, sequeneceNumberList);
        headers.put(EventHubHeaders.PARTITION_KEY, partitionKeyList);
        headers.put(EventHubHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES, batchConvertedSystemProperties);
        headers.put(EventHubHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES, batchConvertedApplicationProperties);

        return headers;
    }

}
