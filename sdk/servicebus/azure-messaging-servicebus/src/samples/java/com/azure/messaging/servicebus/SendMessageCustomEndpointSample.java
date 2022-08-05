// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.BinaryData;

/**
 * Sample demonstrates how to use an intermediary service to connect to Azure Service Bus queue. In this demo, an application
 * gateway.
 */
public class SendMessageCustomEndpointSample {
    /**
     * Main method to invoke this demo about how to use an intermediary service to connect to Azure Service Bus.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "queueName" will be the name of the Service Bus queue instance you created
        //    inside the Service Bus namespace.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";
        String queueName = "<<queue-name>>";

        // The address of our intermediary service.
        String customEndpoint = "<< https://my-application-gateway.cloudapp.azure.com >>";

        // Instantiate a client that will be used to call the service.
        ServiceBusSenderClient sender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .customEndpointAddress(customEndpoint)
            .sender()
            .queueName(queueName)
            .buildClient();

        // Create a message to send.
        ServiceBusMessage message = new ServiceBusMessage(BinaryData.fromString("Microsoft HQ is at Redmond."));

        // Send that message.
        sender.sendMessage(message);

        // Close the sender.
        sender.close();
    }
}
