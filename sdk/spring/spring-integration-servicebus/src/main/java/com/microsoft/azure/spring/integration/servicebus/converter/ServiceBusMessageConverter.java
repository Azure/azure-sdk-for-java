/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.converter;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.servicebus.IMessage;
import com.microsoft.azure.servicebus.Message;
import com.microsoft.azure.servicebus.MessageBody;
import com.microsoft.azure.spring.integration.core.AzureHeaders;
import com.microsoft.azure.spring.integration.core.converter.AbstractAzureMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A converter to turn a {@link org.springframework.messaging.Message} to {@link IMessage}
 * and vice versa.
 *
 * @author Warren Zhu
 */
public class ServiceBusMessageConverter extends AbstractAzureMessageConverter<IMessage> {
    private static final Logger log = LoggerFactory.getLogger(ServiceBusMessageConverter.class);

    @Override
    protected byte[] getPayload(IMessage azureMessage) {
        MessageBody messageBody = azureMessage.getMessageBody();
        if (messageBody == null) {
            return null;
        }

        switch (messageBody.getBodyType()) {
            case BINARY:
                return messageBody.getBinaryData().stream().findFirst().orElse(null);
            case VALUE:
                return String.valueOf(messageBody.getValueData()).getBytes();
            case SEQUENCE:
                return toPayload(messageBody.getSequenceData().stream().findFirst().orElse(null));
            default:
                return null;
        }
    }

    @Override
    protected IMessage fromString(String payload) {
        return new Message(payload);
    }

    @Override
    protected IMessage fromByte(byte[] payload) {
        return new Message(payload);
    }

    @Override
    protected void setCustomHeaders(MessageHeaders headers, IMessage serviceBusMessage) {

        if (headers.containsKey(MessageHeaders.CONTENT_TYPE)) {
            Object contentType = headers.get(MessageHeaders.CONTENT_TYPE);

            if (contentType instanceof MimeType) {
                serviceBusMessage.setContentType(((MimeType) contentType).toString());
            } else {
                serviceBusMessage.setContentType((String) contentType);
            }
        }

        if (headers.containsKey(MessageHeaders.ID)) {
            serviceBusMessage.setMessageId(String.valueOf(headers.get(MessageHeaders.ID, UUID.class)));
        }

        if (headers.containsKey(MessageHeaders.REPLY_CHANNEL)) {
            serviceBusMessage.setReplyTo(headers.get(MessageHeaders.REPLY_CHANNEL, String.class));
        }

        if (headers.containsKey(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE)) {
            serviceBusMessage.setScheduledEnqueueTimeUtc(Instant.now().plus(Duration.ofMillis(
                    headers.get(AzureHeaders.SCHEDULED_ENQUEUE_MESSAGE, Integer.class))));
        }

        headers.forEach((key, value) -> serviceBusMessage.getProperties().put(key, value.toString()));
    }

    @Override
    protected Map<String, Object> buildCustomHeaders(IMessage serviceBusMessage) {
        Map<String, Object> headers = new HashMap<>();

        if (StringUtils.hasText(serviceBusMessage.getMessageId())) {
            headers.put(AzureHeaders.RAW_ID, serviceBusMessage.getMessageId());
        }

        if (StringUtils.hasText(serviceBusMessage.getContentType())) {
            String contentType = serviceBusMessage.getContentType();
            try {
                MimeType mimeType = MimeType.valueOf(contentType);
                headers.put(MessageHeaders.CONTENT_TYPE, mimeType.toString());
            } catch (InvalidMimeTypeException e) {
                log.warn("Invalid mimeType '{}' from service bus message.", contentType);
            }
        }

        if (StringUtils.hasText(serviceBusMessage.getReplyTo())) {
            headers.put(MessageHeaders.REPLY_CHANNEL, serviceBusMessage.getReplyTo());
        }

        headers.putAll(serviceBusMessage.getProperties());

        return Collections.unmodifiableMap(headers);
    }
}
