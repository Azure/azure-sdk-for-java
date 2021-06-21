// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;

/**
 * Class contains sample code snippets that will be used in javadocs.
 */
public class AmqpAnnotatedMessageJavaDocCodeSamples {
    /**
     * Get message body from {@link AmqpAnnotatedMessage}.
     */
    @Test
    public void checkBodyType() {
        AmqpAnnotatedMessage amqpAnnotatedMessage =
            new AmqpAnnotatedMessage(AmqpMessageBody.fromData("my-amqp-message".getBytes(StandardCharsets.UTF_8)));
        // BEGIN: com.azure.core.amqp.models.AmqpBodyType.checkBodyType
        Object amqpValue;
        AmqpMessageBodyType bodyType = amqpAnnotatedMessage.getBody().getBodyType();

        switch (bodyType) {
            case DATA:
                byte[] payload = amqpAnnotatedMessage.getBody().getFirstData();
                System.out.println(new String(payload));
                break;
            case SEQUENCE:
                List<Object> sequenceData = amqpAnnotatedMessage.getBody().getSequence();
                sequenceData.forEach(System.out::println);
                break;
            case VALUE:
                amqpValue = amqpAnnotatedMessage.getBody().getValue();
                System.out.println(amqpValue);
                break;
            default:
                throw new RuntimeException(String.format(Locale.US, "Body type [%s] is not valid.", bodyType));
        }
        // END: com.azure.core.amqp.models.AmqpBodyType.checkBodyType
    }

    public void address() {
        // BEGIN: com.azure.core.amqp.models.AmqpAddress.createAndGet
        AmqpAddress amqpAddress = new AmqpAddress("my-address");
        // Retrieve Adderss
        String address = amqpAddress.toString();
        System.out.println("Address " + address);
        // END: com.azure.core.amqp.models.AmqpAddress.createAndGet
    }

    /**
     * Get message body from {@link AmqpMessageId}.
     */
    public void messageId() {
        // BEGIN: com.azure.core.amqp.models.AmqpMessageId.createAndGet
        AmqpMessageId messageId = new AmqpMessageId("my-message-id");
        // Retrieve Message id
        String id = messageId.toString();
        System.out.println("Message Id " + id);
        // END: com.azure.core.amqp.models.AmqpMessageId.createAndGet
    }

}
