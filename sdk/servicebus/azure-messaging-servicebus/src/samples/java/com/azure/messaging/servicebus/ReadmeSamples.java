// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
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
            .buildSenderClientBuilder()
            .entityName("<< QUEUE OR TOPIC NAME >>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver.
     */
    public void createAsynchronousServiceBusReceiver() {
        String connectionString = "<< CONNECTION STRING FOR THE SERVICE BUS NAMESPACE >>";
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .buildReceiverClientBuilder()
            .queueName("<< QUEUE NAME >>")
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
            .buildReceiverClientBuilder()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
    }

    /**
     * Code sample for creating an asynchronous Service Bus receiver using environment variable and
     * {@link DefaultAzureCredentialBuilder}.
     */
    public void createAsynchronousServiceBusReceiverWithEnvVariableAzureIdentity() {
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-id>>");
        System.setProperty("AZURE_CLIENT_SECRET", "<<insert-service-principal-client-application-secret>>");
        System.setProperty("AZURE_TENANT_ID", "<<insert-service-principal-tenant-id>>");
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        ServiceBusReceiverAsyncClient receiver = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .buildReceiverClientBuilder()
            .queueName("<<queue-name>>")
            .buildAsyncClient();
    }
}
