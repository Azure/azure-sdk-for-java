// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.support.converter;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.support.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

@SuppressWarnings("unchecked")
public class EventHubsBatchMessageConverterTests {

    private final String headerProperties = "headerProperties";
    private final String payload1 = new String(new char[10000]).replace("\0", "a");
    private final String payload2 = new String(new char[10000]).replace("\0", "b");
    private final byte[] payloadBytes1 = payload1.getBytes(UTF_8);
    private final byte[] payloadBytes2 = payload2.getBytes(UTF_8);
    private final User payloadPojo1 = new User(payload1);
    private final User payloadPojo2 = new User(payload2);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventHubsBatchMessageConverter batchConverter = new EventHubsBatchMessageConverter();

    private final PartitionContext partitionContext = new PartitionContext("TEST_NAMESPACE",
        "TEST_EVENT_HUB", "TEST_DEFAULT_GROUP", "TEST_TEST_ID");
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties = new LastEnqueuedEventProperties(1035L,
        100L, Instant.ofEpochSecond(1608315301L), Instant.ofEpochSecond(1609315301L));

    @Mock
    private CheckpointStore checkpointStore;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void receivePayloadAsByte() throws JsonProcessingException {
        List<EventData> events = setupEventDataListByPayload(Arrays.asList(payloadBytes1, payloadBytes2));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, byte[].class);
        List<byte[]> convertedPayload = (List<byte[]>) message.getPayload();
        assertEventBatchPayloadEqual(convertedPayload, events);
    }

    @Test
    public void receivePayloadAsString() throws JsonProcessingException {
        List<EventData> events = setupEventDataListByPayload(Arrays.asList(payload1.getBytes(UTF_8), payload2.getBytes(UTF_8)));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, String.class);
        List<byte[]> convertedPayload = ((List<String>) message.getPayload()).stream().map(String::getBytes).collect(Collectors.toList());
        assertEventBatchPayloadEqual(convertedPayload, events);
    }

    @Test
    public void receivePayloadAsPojo() throws JsonProcessingException {
        List<EventData> events = setupEventDataListByPayload(Arrays.asList(objectMapper.writeValueAsBytes(payloadPojo1), objectMapper.writeValueAsBytes(payloadPojo2)));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, User.class);
        List<byte[]> convertedPayload = new ArrayList<>();
        for (User user : ((List<User>) message.getPayload())) {
            byte[] bytes = objectMapper.writeValueAsBytes(user);
            convertedPayload.add(bytes);
        }
        assertEventBatchPayloadEqual(convertedPayload, events);
    }


    @Test
    public void testNativeHeadersFromEventBatchContext() throws JsonProcessingException {
        List<EventData> events = setupEventDataListByPayload(Arrays.asList(payloadBytes1, payloadBytes2));
        String nativeHeadersString = "{\"spanId\":[\"spanId-1\", \"spanId-2\"],\"spanTraceId\":[\"spanTraceId-1\", \"spanTraceId-2\"]}";
        events.forEach(eventData -> eventData.getProperties().put(NATIVE_HEADERS, nativeHeadersString));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Map<String, Object> headerHeadersMap = batchConverter.buildCustomHeaders(eventBatchContext);
        assertNotNull(headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES));
        List<Map<String, Object>> headers =
            (List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES);
        headers.forEach(map -> assertEquals(map.get(NATIVE_HEADERS).getClass(), String.class));
    }

    @Test
    public void testEventBatchContextHeaders() throws JsonProcessingException {
        List<EventData> events = setupEventDataListByPayload(Arrays.asList(payloadBytes1, payloadBytes2));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Map<String, Object> headerHeadersMap = batchConverter.buildCustomHeaders(eventBatchContext);

        assertFalse(headerHeadersMap.containsKey(EventHubsHeaders.ENQUEUED_TIME));
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_ENQUEUED_TIME));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_ENQUEUED_TIME)).size(), 2);
        assertFalse(headerHeadersMap.containsKey(EventHubsHeaders.OFFSET));
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_OFFSET));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_OFFSET)).size(), 2);
        assertFalse(headerHeadersMap.containsKey(EventHubsHeaders.SEQUENCE_NUMBER));
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_SEQUENCE_NUMBER));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_SEQUENCE_NUMBER)).size(), 2);
        assertFalse(headerHeadersMap.containsKey(EventHubsHeaders.PARTITION_KEY));
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_PARTITION_KEY));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_PARTITION_KEY)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES)).size(), 2);

        List<Map<String, Object>> headers =
            (List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES);
        headers.forEach(map -> assertEquals(map.get(headerProperties), headerProperties));
    }

    private List<EventData> setupEventDataListByPayload(List<byte[]> payloads) throws JsonProcessingException {
        List<EventData> events = new ArrayList<>();
        payloads.forEach(payload -> {
            EventData event = new EventData(payload);
            event.getProperties().put(headerProperties, headerProperties);
            events.add(event);
        });
        return events;
    }

    private void assertEventBatchPayloadEqual(List<byte[]> convertedPayload, List<EventData> events) {
        assertEquals(convertedPayload.size(), events.size());
        for (int i = 0; i < convertedPayload.size(); i++) {
            assertArrayEquals(convertedPayload.get(i), events.get(i).getBody());
        }
    }

}
