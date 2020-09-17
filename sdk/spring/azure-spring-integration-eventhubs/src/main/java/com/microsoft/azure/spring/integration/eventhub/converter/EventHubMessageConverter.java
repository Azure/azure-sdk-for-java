// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.eventhub.converter;

import com.azure.messaging.eventhubs.EventData;
import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.NativeMessageHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * A converter to turn a {@link Message} to {@link EventData} and vice versa.
 *
 * @author Warren Zhu
 */
public class EventHubMessageConverter extends AbstractAzureMessageConverter<EventData> {

    @Override
    protected byte[] getPayload(EventData azureMessage) {
        return azureMessage.getBody();
    }

    @Override
    protected EventData fromString(String payload) {
        return new EventData(payload.getBytes(Charset.defaultCharset()));
    }

    @Override
    protected EventData fromByte(byte[] payload) {
        return new EventData(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, EventData azureMessage) {
        super.setCustomHeaders(headers, azureMessage);
        headers.forEach((key, value) -> {
            if (key.equals(NativeMessageHeaderAccessor.NATIVE_HEADERS)
                    && value instanceof LinkedMultiValueMap) {
                azureMessage.getProperties().put(key, toJson(value));
            } else {
                azureMessage.getProperties().put(key, value.toString());
            }
        });
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(EventData azureMessage) {
        Map<String, Object> headers = super.buildCustomHeaders(azureMessage);
        Map<String, Object> properties = azureMessage.getProperties();
        if (properties.containsKey(NativeMessageHeaderAccessor.NATIVE_HEADERS)
                && isValidJson(properties.get(NativeMessageHeaderAccessor.NATIVE_HEADERS))) {
            String nativeHeader = (String) properties.remove(NativeMessageHeaderAccessor.NATIVE_HEADERS);
            properties.put(NativeMessageHeaderAccessor.NATIVE_HEADERS,
                    readValue(nativeHeader, LinkedMultiValueMap.class));
        }
        headers.putAll(azureMessage.getProperties());
        return headers;
    }
}
