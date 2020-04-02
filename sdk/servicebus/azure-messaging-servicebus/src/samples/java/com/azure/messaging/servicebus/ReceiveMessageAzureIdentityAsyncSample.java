// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.Disposable;

import java.time.Duration;

/**
 * Sample demonstrates how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue using
 * Azure Identity.
 */
public class ReceiveMessageAzureIdentityAsyncSample {
    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusMessage} from an Azure Service Bus
     * Queue
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

        // The default azure credential checks multiple locations for credentials and determines the best one to use.
        // For the purpose of this sample, create a service principal and set the following environment variables.
        // See https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal for
        // information on how to create a service principal.
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-id>>");
        System.setProperty("AZURE_CLIENT_SECRET", "<<insert-service-principal-client-application-secret>>");
        System.setProperty("AZURE_TENANT_ID", "<<insert-service-principal-tenant-id>>");

        // DefaultAzureCredentialBuilder exists inside the azure-identity package.
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .credential("<<fully-qualified-namespace>>", credential)
            .receiver()
            .queueName("<<queue-name>>")
            .buildAsyncClient();

        Disposable subscription = receiverAsyncClient.receive()
            .subscribe(message -> {
                System.out.println("Received Message Id:" + message.getMessageId());
                System.out.println("Received Message:" + new String(message.getBody()));
            }, error -> System.err.println("Error occurred while receiving message: " + error),
                () -> System.out.println("Receiving complete."));


        // Receiving messages from the queue for a duration of 20 seconds.
        // Subscribe is not a blocking call so we sleep here so the program does not end.
        try {
            Thread.sleep(Duration.ofSeconds(20).toMillis());
        } catch (InterruptedException ignored) {
        }

        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiverAsyncClient.close();
    }
}
