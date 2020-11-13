// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Class contains sample code snippets that will be used in javadocs.
 */
public class AmqpMessageIdJavaDocCodeSamples {
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
