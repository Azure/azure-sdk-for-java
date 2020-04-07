// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;

import java.time.Duration;

/**
 * Sample demonstrates how to receive an {@link ServiceBusReceivedMessage} from an Azure Service Bus Queue and settle
 * it. Settling of message include accept, defer and abandon the message as needed.
 */
public class ReceiveMessageAndSettleAsyncSample {

    /**
     * Main method to invoke this demo on how to receive an {@link ServiceBusMessage} from an Azure Service Bus Queue
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {

        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        String connectionString = "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
            + "SharedAccessKey={key}";

        // Create a receiver.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<queue-name>>" will be the name of the Service Bus queue instance you created
        // inside the Service Bus namespace.
        ServiceBusReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .buildAsyncClient();
        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(Duration.ofSeconds(2));

        Disposable subscription = receiverAsyncClient.receive(options)
            .flatMap(message -> {
                boolean messageProcessed =  false;
                // Process the message here.
                // Change the `messageProcessed` according to you business logic and if you are able to process the
                // message successfully.

                if (messageProcessed) {
                    return receiverAsyncClient.complete(message).then();
                } else {
                    return receiverAsyncClient.abandon(message).then();
                }
            }).subscribe();

        // Subscribe is not a blocking call so we sleep here so the program does not end.
        try {
            Thread.sleep(Duration.ofSeconds(60).toMillis());
        } catch (InterruptedException ignored) {
        }
        // Disposing of the subscription will cancel the receive() operation.
        subscription.dispose();

        // Close the receiver.
        receiverAsyncClient.close();
    }
}
