// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.converter.AzureMessageConverter;
import com.azure.spring.integration.test.support.AzureMessageConverterTest;
import org.junit.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.LinkedMultiValueMap;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.messaging.support.NativeMessageHeaderAccessor.NATIVE_HEADERS;

public class EventHubMessageConverterTest extends AzureMessageConverterTest<EventData> {

    private static final String EVENT_DATA = "event-hub-test-string";

    private static final String PARTITION_KEY = "abc";
    private static final Instant ENQUEUED_TIME = Instant.now().minus(1, ChronoUnit.DAYS);
    private static final Long OFFSET = 1234567890L;
    private static final Long SEQUENCE_NUMBER = 123456L;


    @Override
    protected EventData getInstance() {
        return new EventData(this.payload.getBytes());
    }

    @Override
    public AzureMessageConverter<EventData> getConverter() {
        return new EventHubMessageConverter();
    }

    @Override
    protected Class<EventData> getTargetClass() {
        return EventData.class;
    }

    private static class MyEventHubMessageConverter extends EventHubMessageConverter {

        public void setCustomHeaders(MessageHeaders headers, EventData azureMessage) {
            super.setCustomHeaders(headers, azureMessage);
        }

        public Map<String, Object> buildCustomHeaders(EventData azureMessage) {
            return super.buildCustomHeaders(azureMessage);
        }

        @Override
        protected String toJson(Object value) {
            return super.toJson(value);
        }
    }

    @Test
    public void testConvertCustomHeadersToEventData() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("fake-header", "fake-value");
        MessageHeaders headers = new MessageHeaders(headerMap);

        EventData eventData = new EventData(EVENT_DATA);

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        converter.setCustomHeaders(headers, eventData);

        assertEquals(eventData.getProperties().get("fake-header"), "fake-value");
        assertEquals(eventData.getBodyAsString(), EVENT_DATA);
    }

    @Test
    public void testConvertNativeHeadersToEventData() {
        Map<String, Object> headerMap = new HashMap<>();
        LinkedMultiValueMap<String, String> nativeHeaders = new LinkedMultiValueMap<>();
        nativeHeaders.put("spanId", Arrays.asList("spanId-1", "spanId-2"));
        nativeHeaders.put("spanTraceId", Arrays.asList("spanTraceId-1", "spanTraceId-2"));
        headerMap.put(NATIVE_HEADERS, nativeHeaders);
        MessageHeaders headers = new MessageHeaders(headerMap);

        EventData eventData = new EventData(EVENT_DATA);

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        converter.setCustomHeaders(headers, eventData);

        assertEquals(eventData.getProperties().get(NATIVE_HEADERS).getClass(), String.class);
        assertEquals(eventData.getProperties().get(NATIVE_HEADERS), converter.toJson(nativeHeaders));
    }

    @Test
    public void testCustomHeadersFromEventData() {
        EventData eventData = new EventData(EVENT_DATA);
        eventData.getProperties().put("fake-header", "fake-value");

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        Map<String, Object> headerHeadersMap = converter.buildCustomHeaders(eventData);
        assertEquals(headerHeadersMap.get("fake-header"), "fake-value");
        assertEquals(eventData.getBodyAsString(), EVENT_DATA);
    }

    @Test
    public void testNativeHeadersFromEventData() {
        EventData eventData = new EventData(EVENT_DATA);
        String nativeHeadersString = "{\"spanId\":[\"spanId-1\", \"spanId-2\"],\"spanTraceId\":[\"spanTraceId-1\", \"spanTraceId-2\"]}";
        eventData.getProperties().put(NATIVE_HEADERS, nativeHeadersString);

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        Map<String, Object> headerHeadersMap = converter.buildCustomHeaders(eventData);
        assertEquals(headerHeadersMap.get(NATIVE_HEADERS).getClass(), LinkedMultiValueMap.class);
    }

    @Test
    public void testSystemPropertiesScreenedOut() {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(EventHubHeaders.PARTITION_KEY, PARTITION_KEY);
        headerMap.put(EventHubHeaders.ENQUEUED_TIME, ENQUEUED_TIME);
        headerMap.put(EventHubHeaders.OFFSET, OFFSET);
        headerMap.put(EventHubHeaders.SEQUENCE_NUMBER, SEQUENCE_NUMBER);
        MessageHeaders headers = new MessageHeaders(headerMap);

        EventData eventData = new EventData(EVENT_DATA);

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        converter.setCustomHeaders(headers, eventData);

        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.PARTITION_KEY));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.ENQUEUED_TIME));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.OFFSET));
        assertFalse(eventData.getProperties().containsKey(EventHubHeaders.SEQUENCE_NUMBER));
    }

    @Test
    public void testSystemPropertiesConvertedFromEventData() {
        EventData eventData = new EventData(EVENT_DATA);

        MyEventHubMessageConverter converter = new MyEventHubMessageConverter();
        Map<String, Object> headerHeadersMap = converter.buildCustomHeaders(eventData);

        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.ENQUEUED_TIME));
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.OFFSET));
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.SEQUENCE_NUMBER));
        assertTrue(headerHeadersMap.containsKey(EventHubHeaders.PARTITION_KEY));
    }
}
