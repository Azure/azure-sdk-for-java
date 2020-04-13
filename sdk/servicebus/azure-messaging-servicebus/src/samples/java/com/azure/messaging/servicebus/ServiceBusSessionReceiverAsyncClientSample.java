// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import com.azure.messaging.servicebus.models.ServiceBusErrorContext;
import com.azure.messaging.servicebus.models.ServiceBusSessionException;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ServiceBusSessionReceiverAsyncClientSample {
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
        ServiceBusSessionReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiverSession()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .sessionId("<< session-id >>")
            .buildAsyncClient();

        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false)
            .setMaxAutoRenewDuration(Duration.ofSeconds(2));

        Disposable subscription = receiverAsyncClient.receive(options)
            .onErrorContinue(ServiceBusSessionException.class, (throwable, o) -> {
                // can log the session id which error out
            })
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
