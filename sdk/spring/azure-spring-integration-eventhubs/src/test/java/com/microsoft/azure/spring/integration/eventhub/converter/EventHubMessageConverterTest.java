// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.converter.AzureMessageConverter;
import com.microsoft.azure.spring.integration.test.support.AzureMessageConverterTest;
import org.junit.Test;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class EventHubMessageConverterTest extends AzureMessageConverterTest<EventData> {

    private static final String EVENT_DATA = "event-hub-test-string";
    private static final int PARTITION_ID = 1;
    private static final String NATIVE_HEADERS_SPAN_ID_KEY = "spanId";
    private static final List<String> NATIVE_HEADERS_SPAN_ID_VALUE = Arrays.asList("spanId-1", "spanId-2");
    private static final String NATIVE_HEADERS_SPAN_TRACE_ID_KEY = "spanTraceId";
    private static final List<String> NATIVE_HEADERS_SPAN_TRACE_ID_VALUE = Arrays
            .asList("spanTraceId-1", "spanTraceId-2");

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
    }

    @Test
    public void testSetCustomHeadersWithCommon() {
        EventData eventData = new EventData(EVENT_DATA);
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put(AzureHeaders.RAW_PARTITION_ID, PARTITION_ID);
        MessageHeaders headers = new MessageHeaders(headerMap);

        MyEventHubMessageConverter convert = new MyEventHubMessageConverter();
        convert.setCustomHeaders(headers, eventData);
        assertEquals(Integer.parseInt(String.valueOf(eventData.getProperties()
                .get(AzureHeaders.RAW_PARTITION_ID))), PARTITION_ID);
        assertEquals(eventData.getBodyAsString(), EVENT_DATA);
    }

    @Test
    public void testSetCustomHeadersWithNativesHeader() {
        EventData eventData = new EventData(EVENT_DATA);
        Map<String, Object> headerMap = new HashMap<>();
        LinkedMultiValueMap<String, String> linkedMultiValueMap = new LinkedMultiValueMap<>();
        linkedMultiValueMap.put(NATIVE_HEADERS_SPAN_ID_KEY, NATIVE_HEADERS_SPAN_ID_VALUE);
        linkedMultiValueMap.put(NATIVE_HEADERS_SPAN_TRACE_ID_KEY, NATIVE_HEADERS_SPAN_TRACE_ID_VALUE);
        headerMap.put(NativeMessageHeaderAccessor.NATIVE_HEADERS, linkedMultiValueMap);
        MessageHeaders headers = new MessageHeaders(headerMap);

        MyEventHubMessageConverter convert = new MyEventHubMessageConverter();
        convert.setCustomHeaders(headers, eventData);
        assertSame(eventData.getProperties().get(NativeMessageHeaderAccessor.NATIVE_HEADERS).getClass(),
                String.class);
    }

    @Test
    public void testBuildCustomHeadersWithCommon() {
        EventData eventData = new EventData(EVENT_DATA);
        eventData.getProperties().put(AzureHeaders.RAW_PARTITION_ID, PARTITION_ID);
        MyEventHubMessageConverter convert = new MyEventHubMessageConverter();
        Map<String, Object> headerHeadersMap = convert.buildCustomHeaders(eventData);
        assertEquals(headerHeadersMap.get(AzureHeaders.RAW_PARTITION_ID), PARTITION_ID);
        assertEquals(eventData.getBodyAsString(), EVENT_DATA);
    }

    @Test
    public void testBuildCustomHeadersWithNativeHeaders() {
        EventData eventData = new EventData(EVENT_DATA);
        String nativeHeadersString = "{\"spanId\":[\"spanId-1\", \"spanId-2\"],"
            + "\"spanTraceId\":[\"spanTraceId-1\", \"spanTraceId-2\"]}";
        eventData.getProperties().put(NativeMessageHeaderAccessor.NATIVE_HEADERS, nativeHeadersString);

        MyEventHubMessageConverter convert = new MyEventHubMessageConverter();
        Map<String, Object> headerHeadersMap = convert.buildCustomHeaders(eventData);
        assertSame(headerHeadersMap.get(NativeMessageHeaderAccessor.NATIVE_HEADERS).getClass(),
                LinkedMultiValueMap.class);
    }
}
