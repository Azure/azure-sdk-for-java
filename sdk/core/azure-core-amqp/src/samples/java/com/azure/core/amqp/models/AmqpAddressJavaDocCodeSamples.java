// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.models;

/**
 * Class contains sample code snippets that will be used in javadocs.
 */
public class AmqpAddressJavaDocCodeSamples {
    /**
     * Get message body from {@link AmqpAddress}.
     */
    public void address() {
        // BEGIN: com.azure.core.amqp.models.AmqpAddress.createAndGet
        AmqpAddress amqpAddress = new AmqpAddress("my-address");
        // Retrieve Adderss
        String address = amqpAddress.toString();
        System.out.println("Address " + address);
        // END: com.azure.core.amqp.models.AmqpAddress.createAndGet
    }
}
