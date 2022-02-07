// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.CheckpointStore;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.test.support.pojo.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.spring.integration.core.EventHubHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

@SuppressWarnings("unchecked")
public class EventHubBatchMessageConverterTest {

    private static final String PARTITION_KEY = "abc";
    private static final Instant ENQUEUED_TIME = Instant.now().minus(1, ChronoUnit.DAYS);
    private static final Long OFFSET = 1234567890L;
    private static final Long SEQUENCE_NUMBER = 123456L;

    private final String headerProperties = "headerProperties";
    private final String payload1 = new String(new char[10000]).replace("\0", "a");
    private final String payload2 = new String(new char[10000]).replace("\0", "b");
    private final byte[] payloadBytes1 = payload1.getBytes(UTF_8);
    private final byte[] payloadBytes2 = payload2.getBytes(UTF_8);
    private final User payloadPojo1 = new User(payload1);
    private final User payloadPojo2 = new User(payload2);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventHubBatchMessageConverter converter = new EventHubBatchMessageConverter();

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
    public void sendPayloadAsByte() {
        EventData azureMessage = convertToEventData(payloadBytes1);
        assertNotNull(azureMessage);
        assertArrayEquals(azureMessage.getBody(), payloadBytes1);
        assertEventDataHeadersEqual(azureMessage);
    }

    @Test
    public void sendPayloadAsString() {
        EventData azureMessage = convertToEventData(payload1);
        assertNotNull(azureMessage);
        assertEquals(azureMessage.getBodyAsString(), payload1);
        assertEventDataHeadersEqual(azureMessage);
    }

    @Test
    public void sendPayloadAsPojoClass() throws JsonProcessingException {
        EventData azureMessage = convertToEventData(payloadPojo1);
        assertNotNull(azureMessage);
        assertArrayEquals(azureMessage.getBody(), objectMapper.writeValueAsBytes(payloadPojo1));
        assertEventDataHeadersEqual(azureMessage);
    }

    @Test
    public void testNonUtf8DecodingPayload() {
        String utf16Payload = new String(payload1.getBytes(), StandardCharsets.UTF_16);
        Message<String> message = MessageBuilder.withPayload(utf16Payload).build();
        EventData azureMessage = converter.fromMessage(message, EventData.class);
        assertEquals(utf16Payload, azureMessage.getBodyAsString());
        assertNotEquals(payload1, azureMessage.getBodyAsString());
    }

    @Test
    public void testConvertCustomHeadersToEventData() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("fake-header", "fake-value");
        MessageHeaders headers = new MessageHeaders(headerMap);

        EventData eventData = new EventData(payload1);

        EventHubBatchMessageConverter converter = new EventHubBatchMessageConverter();
        converter.setCustomHeaders(headers, eventData);

        assertEquals(eventData.getProperties().get("fake-header"), "fake-value");
        assertEquals(eventData.getBodyAsString(), payload1);
    }

    @Test
    public void testSystemPropertiesScreenedOut() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(EventHubHeaders.PARTITION_KEY, PARTITION_KEY);
        headerMap.put(EventHubHeaders.ENQUEUED_TIME, ENQUEUED_TIME);
        headerMap.put(EventHubHeaders.OFFSET, OFFSET);
        headerMap.put(EventHubHeaders.SEQUENCE_NUMBER, SEQUENCE_NUMBER);
        MessageHeaders headers = new MessageHeaders(headerMap);

        EventData eventData = new EventData(payload1);

        EventHubBatchMessageConverter converter = new EventHubBatchMessageConverter();
        converter.setCustomHeaders(headers, eventData);

        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.PARTITION_KEY));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.ENQUEUED_TIME));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.OFFSET));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.SEQUENCE_NUMBER));
    }

    @Test
    public void receivePayloadAsByte() throws JsonProcessingException {
        setupEventDataListByPayload(payloadBytes1, payloadBytes2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.converter.toMessage(eventBatchContext, byte[].class);
        List<byte[]> convertedPayload = (List<byte[]>) message.getPayload();
        assertEventBatchPayloadEqual(convertedPayload);
    }

    @Test
    public void receivePayloadAsString() throws JsonProcessingException {
        setupEventDataListByPayload(payload1, payload2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.converter.toMessage(eventBatchContext, String.class);
        List<byte[]> convertedPayload = ((List<String>) message.getPayload()).stream().map(String::getBytes).collect(Collectors.toList());
        assertEventBatchPayloadEqual(convertedPayload);
    }

    @Test
    public void receivePayloadAsPojo() throws JsonProcessingException {
        setupEventDataListByPayload(objectMapper.writeValueAsBytes(payloadPojo1), objectMapper.writeValueAsBytes(payloadPojo2));
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Message<?> message = this.converter.toMessage(eventBatchContext, User.class);
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
        Map<String, Object> headerHeadersMap = converter.buildCustomHeaders(eventBatchContext);
        assertNotNull(headerHeadersMap.get(BATCH_CONVERTED_APPLICATION_PROPERTIES));
        List<Map<String, Object>> headers = (List<Map<String, Object>>) headerHeadersMap.get(BATCH_CONVERTED_APPLICATION_PROPERTIES);
        headers.forEach(map -> assertEquals(map.get(NATIVE_HEADERS).getClass(), String.class));
    }

    @Test
    public void testEventBatchContextHeaders() throws JsonProcessingException {
        setupEventDataListByPayload(payloadBytes1, payloadBytes2);
        EventBatchContext eventBatchContext = new EventBatchContext(partitionContext, events, checkpointStore,
            lastEnqueuedEventProperties);
        Map<String, Object> headerHeadersMap = converter.buildCustomHeaders(eventBatchContext);

        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.ENQUEUED_TIME));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.ENQUEUED_TIME)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.OFFSET));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.OFFSET)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.SEQUENCE_NUMBER));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.SEQUENCE_NUMBER)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.PARTITION_KEY));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.PARTITION_KEY)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.BATCH_CONVERTED_SYSTEM_PROPERTIES)).size(), 2);
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES));
        assertEquals(((List<Map<String, Object>>) headerHeadersMap.get(EventHubHeaders.BATCH_CONVERTED_APPLICATION_PROPERTIES)).size(), 2);

        List<Map<String, Object>> headers = (List<Map<String, Object>>) headerHeadersMap.get(BATCH_CONVERTED_APPLICATION_PROPERTIES);
        headers.forEach(map -> assertEquals(map.get(headerProperties), headerProperties));
    }

    private <U> EventData convertToEventData(U payload) {
        Message<U> message = MessageBuilder.withPayload(payload).setHeader(headerProperties, headerProperties).build();
        EventData azureMessage = converter.fromMessage(message, EventData.class);
        return azureMessage;
    }

    private void assertEventDataHeadersEqual(EventData azureMessage) {
        Assertions.assertNotNull(azureMessage.getSystemProperties());
        Assertions.assertNotNull(azureMessage.getBody());
        Assertions.assertNotNull(azureMessage.getProperties());
        assertEquals(azureMessage.getProperties().get(headerProperties), headerProperties);
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
