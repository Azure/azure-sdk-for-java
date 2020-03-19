// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.Disposable;

import java.time.Duration;

/**
 * Sample demonstrates how to peek an {@link ServiceBusReceivedMessage} from an Azure Service Bus using Azure Identity.
 */
public class PeekMessageWithAzureIdentity {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(20);

    /**
     * Main method to invoke this demo on how to send an {@link ServiceBusMessage} to an Azure Service Bus
     * queue or topic.
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

        // Create the client.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-or-topic-name>>" will be the name of the Service Bus queue or topic instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .credential(
                "<<fully-qualified-namespace>>",
                "<<queue-or-topic-name>>",
                credential)
            .buildAsyncReceiverClient();

        Disposable disposable = receiverAsyncClient
            .peek()
            .doOnNext(message -> {
                log(" Received Message Id :" + message.getMessageId());
                log(" Received Message :" + new String(message.getBody()));
            })
            .subscribe();

        //wait for client to finish processing.
        try {
            Thread.sleep(OPERATION_TIMEOUT.toMillis());
        } catch (InterruptedException ignored) {

        }
        log("Closing the receiver");
        disposable.dispose();
        log("End!! ");
    }

    private static void log(String message) {
        System.out.println(message);
    }
}
