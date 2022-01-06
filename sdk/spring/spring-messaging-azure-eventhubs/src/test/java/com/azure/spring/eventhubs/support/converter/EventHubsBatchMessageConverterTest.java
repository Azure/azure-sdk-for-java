// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.support.converter;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.support.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

@SuppressWarnings("unchecked")
public class EventHubsBatchMessageConverterTest {

    private final String headerProperties = "headerProperties";
    private final String payload1 = new String(new char[10000]).replace("\0", "a");
    private final String payload2 = new String(new char[10000]).replace("\0", "b");
    private final byte[] payloadBytes1 = payload1.getBytes(UTF_8);
    private final byte[] payloadBytes2 = payload2.getBytes(UTF_8);
    private final User payloadPojo1 = new User(payload1);
    private final User payloadPojo2 = new User(payload2);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventHubsBatchMessageConverter batchConverter = new EventHubsBatchMessageConverter();
    private final EventHubsMessageConverter recordConverter = new EventHubsMessageConverter();

    private final PartitionContext partitionContext = new PartitionContext("TEST_NAMESPACE",
        "TEST_EVENT_HUB", "TEST_DEFAULT_GROUP", "TEST_TEST_ID");
    private final LastEnqueuedEventProperties lastEnqueuedEventProperties = new LastEnqueuedEventProperties(1035L,
        100L, Instant.ofEpochSecond(1608315301L), Instant.ofEpochSecond(1609315301L));
    private final List<EventData> events = new ArrayList<>();

    @Mock
    private CheckpointStore checkpointStore;

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void receivePayloadAsByte() throws JsonProcessingException {
        setupEventDataListByPayload(payloadBytes1, payloadBytes2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, byte[].class);
        List<byte[]> convertedPayload = (List<byte[]>) message.getPayload();
        assertEventBatchPayloadEqual(convertedPayload);
    }

    @Test
    public void receivePayloadAsString() throws JsonProcessingException {
        setupEventDataListByPayload(payload1, payload2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, String.class);
        List<byte[]> convertedPayload = ((List<String>) message.getPayload()).stream().map(String::getBytes).collect(Collectors.toList());
        assertEventBatchPayloadEqual(convertedPayload);
    }

    @Test
    public void receivePayloadAsPojo() throws JsonProcessingException {
        setupEventDataListByPayload(objectMapper.writeValueAsBytes(payloadPojo1), objectMapper.writeValueAsBytes(payloadPojo2));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.batchConverter.toMessage(eventBatchContext, User.class);
        List<byte[]> convertedPayload = new ArrayList<>();
        for (User user : ((List<User>) message.getPayload())) {
            byte[] bytes = objectMapper.writeValueAsBytes(user);
            convertedPayload.add(bytes);
        }
        assertEventBatchPayloadEqual(convertedPayload);
    }


    @Test
    public void testNativeHeadersFromEventBatchContext() throws JsonProcessingException {
        setupEventDataListByPayload(payloadBytes1, payloadBytes2);
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
        setupEventDataListByPayload(payloadBytes1, payloadBytes2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Map<String, Object> headerHeadersMap = batchConverter.buildCustomHeaders(eventBatchContext);

        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.ENQUEUED_TIME));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.ENQUEUED_TIME)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.OFFSET));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.OFFSET)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.SEQUENCE_NUMBER));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.SEQUENCE_NUMBER)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.PARTITION_KEY));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.PARTITION_KEY)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES)).size(), 2);

        List<Map<String, Object>> headers =
            (List<Map<String, Object>>) headerHeadersMap.get(EventHubsHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES);
        headers.forEach(map -> assertEquals(map.get(headerProperties), headerProperties));
    }

    private <U> EventData convertToEventData(U payload) {
        Message<U> message = MessageBuilder.withPayload(payload).setHeader(headerProperties, headerProperties).build();
        EventData azureMessage = recordConverter.fromMessage(message, EventData.class);
        return azureMessage;
    }

    private void setupEventDataListByPayload(Object payload1, Object payload2) throws JsonProcessingException {
        events.add(convertToEventData(payload1));
        events.add(convertToEventData(payload2));
    }

    private void assertEventBatchPayloadEqual(List<byte[]> convertedPayload) {
        assertEquals(convertedPayload.size(), events.size());
        for (int i = 0; i < convertedPayload.size(); i++) {
            assertArrayEquals(convertedPayload.get(i), events.get(i).getBody());
        }
    }

}
