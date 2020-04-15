// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.ReceiveAsyncOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**Receive message from one named session id**/
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
        ServiceBusReceiverAsyncClient receiverAsyncClient = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .receiver()
            .receiveMode(ReceiveMode.PEEK_LOCK)
            .queueName("<<queue-name>>")
            .sessionId("<< session-id >>") // one named session and no roll-over to next available session
            .buildAsyncClient();

        final ReceiveAsyncOptions options = new ReceiveAsyncOptions()
            .setEnableAutoComplete(false) // user want to settle the message
            .setMaxAutoRenewDuration(Duration.ofSeconds(60));

        //User maintain all the session id here
        AtomicReference<List<String>> openSessionIdList =  new AtomicReference<>();

        Disposable subscription = receiverAsyncClient.receive(options)
            .flatMap(receivedMessage -> {

                if (!openSessionIdList.get().contains((receivedMessage.getSessionId()))) {
                    openSessionIdList.get().add(receivedMessage.getSessionId());
                    Disposable lockRenewer = receiverAsyncClient.renewSessionLock(
                        receivedMessage.getSessionId())
                        .flatMap(time -> Mono.delay(Duration.ofMillis(time.toEpochMilli() - System.currentTimeMillis())))
                        .repeat() // user can repeat for N times
                        .subscribe();
                }

                boolean messageProcessed =  false;

                // Process the message here.
                // Change the `messageProcessed` according to you business logic and if you are able to process the
                // message successfully.

                if (messageProcessed) {
                    return receiverAsyncClient.complete(receivedMessage).then();
                } else {
                    return receiverAsyncClient.abandon(receivedMessage).then();
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
