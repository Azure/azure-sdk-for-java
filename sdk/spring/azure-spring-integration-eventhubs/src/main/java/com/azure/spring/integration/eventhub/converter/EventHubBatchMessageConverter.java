// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A converter to turn a {@link com.azure.messaging.eventhubs.models.EventBatchContext} to {@link Message} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubBatchMessageConverter extends AbstractAzureMessageConverter<EventBatchContext, EventData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubBatchMessageConverter.class);

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

    protected Object getPayload(EventBatchContext azureMessage) {
        List<EventData> events = azureMessage.getEvents();

        List<byte[]> payloadList = new ArrayList<>();
        for (EventData event : events) {
            payloadList.add(event.getBody());
        }
        return payloadList;
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
        headers.put(EventHubHeaders.ENQUEUED_TIME, enqueueTimeList);
        headers.put(EventHubHeaders.OFFSET, offSetList);
        headers.put(EventHubHeaders.SEQUENCE_NUMBER, sequenceNumberList);
        headers.put(EventHubHeaders.PARTITION_KEY, partitionKeyList);
        headers.put(EventHubHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES, batchConvertedSystemProperties);
        headers.put(EventHubHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES, batchConvertedApplicationProperties);

        return headers;
    }

}
