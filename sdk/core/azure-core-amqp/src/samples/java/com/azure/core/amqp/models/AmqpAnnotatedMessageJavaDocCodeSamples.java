// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Class contains sample code snippets that will be used in javadocs.
 */
public class AmqpAnnotatedMessageJavaDocCodeSamples {
    /**
     * Get message body from {@link AmqpAnnotatedMessage}.
     */
    public void checkBodyType() {
        AmqpAnnotatedMessage amqpAnnotatedMessage = null;
        // BEGIN: com.azure.core.amqp.models.AmqpBodyType.checkBodyType
        // If client do not check `AmqpMessageBody.getBodyType()` and payload is not of type `AmqpMessageBodyType.DATA`,
        // calling `getFirstData()` or `getData()` on `AmqpMessageBody` will throw Runtime exception.
        // https://github.com/Azure/azure-sdk-for-java/issues/17614 : This issue tracks additional AMQP body type
        // support in future.

        byte[] payload = null;
        AmqpMessageBodyType bodyType = amqpAnnotatedMessage.getBody().getBodyType();
        switch (bodyType) {
            case DATA:
                payload = amqpAnnotatedMessage.getBody().getFirstData();
                break;
            case SEQUENCE:
            case VALUE:
                throw new RuntimeException("Body type not supported yet.");
            default:
                throw new RuntimeException("Body type not valid.");
        }
        System.out.println(new String(payload));
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
