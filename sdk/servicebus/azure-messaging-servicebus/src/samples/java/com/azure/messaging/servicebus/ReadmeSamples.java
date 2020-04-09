// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    /**
     * Code sample for creating an asynchronous Service Bus sender.
     */
    public void createAsynchronousServiceBusSender() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("<< QUEUE NAME >>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .topicName("<< TOPIC NAME >>")
            .subscriptionName("<< SUBSCRIPTION NAME >>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using {@link DefaultAzureCredentialBuilder}.
     */
    public void createAsynchronousServiceBusReceiverWithAzureIdentity() {
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using Aad.
     */
    public void createAsynchronousServiceBusReceiverWithAad() {
        ClientSecretCredential credential = new ClientSecretCredentialBuilder()
            .clientId("<< APPLICATION (CLIENT) ID >>")
            .clientSecret("<< APPLICATION SECRET >>")
            .tenantId("<< TENANT ID >>")
            .build();

        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
    }
}
