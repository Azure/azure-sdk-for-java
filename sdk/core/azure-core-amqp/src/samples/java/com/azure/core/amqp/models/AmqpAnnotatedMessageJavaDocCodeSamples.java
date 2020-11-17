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
        AmqpMessageBodyType bodyType = amqpAnnotatedMessage.getBody().getBodyType();
        AmqpMessageBody messageBody = null;
        switch (bodyType) {
            case DATA:
                messageBody = AmqpMessageBody.fromData(amqpAnnotatedMessage.getBody().getFirstData());
                break;
            case SEQUENCE:
            case VALUE:
                throw new RuntimeException("Body type not supported yet.");
            default:
                throw new RuntimeException("Body type not valid.");
        }
        // END: com.azure.core.amqp.models.AmqpBodyType.checkBodyType
    }
}
