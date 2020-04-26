// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send an {@link ServiceBusMessage} to an Azure Service Bus using Azure Identity.
 */
public class SendMessageWithAzureIdentityAsyncSample {

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessage} to an Azure Service bus Queue.
     *
     * @param args Unused arguments to the program.
     * @throws InterruptedException If the program is unable to sleep while waiting for the operations to complete.
     */
    public static void main(String[] args) throws InterruptedException {
        ServiceBusMessage guestCheckInEvent = new ServiceBusMessage("Microsoft HQ is at Redmond.".getBytes(UTF_8))
            .setMessageId(UUID.randomUUID().toString());

        // DefaultAzureCredential checks multiple locations for credentials and determines the best one to use.
        // For the purpose of this sample, create a service principal and set the following environment variables.
        // See https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal for
        // information on how to create a service principal.
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-id>>");
        System.setProperty("AZURE_CLIENT_SECRET", "<<insert-service-principal-client-application-secret>>");
        System.setProperty("AZURE_TENANT_ID", "<<insert-service-principal-tenant-id>>");

        // DefaultAzureCredentialBuilder exists inside the azure-identity package.
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        // Create a sender.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue you created inside the Service Bus namespace.
        ServiceBusSenderAsyncClient sender = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .sender()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        sender.send(guestCheckInEvent)
            .subscribe(
                unused -> System.out.println("Sent."),
                error -> System.err.println("Error occurred while publishing message: " + error),
                () -> System.out.printf("Message was sent with id: %s%n", guestCheckInEvent.getMessageId()));

        // .subscribe is not a blocking call. We sleep here so the program does not end before the send is complete.
        TimeUnit.SECONDS.sleep(5);

        // Close the sender.
        sender.close();
    }
}
