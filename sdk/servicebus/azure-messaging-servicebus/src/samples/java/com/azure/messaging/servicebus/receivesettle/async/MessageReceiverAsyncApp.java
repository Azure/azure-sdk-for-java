// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.receivesettle.async;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.receivesettle.sync.MessageReceiverSyncApp;
import reactor.core.Disposable;

import java.time.Duration;

/**
 * Sample Application that uses async API to receive and settle messages.
 * You can concurrently run multiple MessageReceiverAsyncApp and/or MessageReceiverSyncApp
 * to receive from a service bus queue.
 */
public class MessageReceiverAsyncApp {
    private final ServiceBusReceiverAsyncClient receiverClient;
    private final MessageReceiverAsyncWorker messageReceiverWorker;

    public MessageReceiverAsyncApp(ServiceBusReceiverAsyncClient receiverClient, MessageReceiverAsyncWorker messageReceiverWorker) {
        this.receiverClient = receiverClient;
        this.messageReceiverWorker = messageReceiverWorker;
    }

    /**
     * Keep receiving messages and process them one by one.
     * @return The disposable
     */
    public Disposable processMessageOneByOne() {
        return receiverClient.receiveMessages().flatMap(messageReceiverWorker::processMessageOneByOne).subscribe();
    }

    /**
     * Keep receiving messages and process them in batches.
     * @return The disposable
     */
    public Disposable processMessageInBatch() {
        return receiverClient.receiveMessages().bufferTimeout(5, Duration.ofSeconds(2)).flatMap(messageReceiverWorker::processMessageInBatch).subscribe();
    }

    public static void main(String[] args) {
        ClientLogger logger = new ClientLogger(MessageReceiverSyncApp.class);
        logger.info("Start of MessageReceiverAsyncApp");
        String serviceBusConnectionString = System.getenv("SERVICE_BUS_CONNECTION_STR");
        // A connection string is like
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        String queueName = System.getenv("SERVICE_BUS_QUEUE_NAME");

        ServiceBusReceiverAsyncClient receiverClient = new ServiceBusClientBuilder()
            .connectionString(serviceBusConnectionString)
            .receiver()
            .maxAutoLockRenewalDuration(Duration.ofSeconds(10))
            .queueName(queueName)
            .buildAsyncClient();
        OrderAsyncService orderService = new OrderAsyncService();
        MessageReceiverAsyncWorker messageReceiverSyncWorker = new MessageReceiverAsyncWorker(receiverClient, orderService);
        MessageReceiverAsyncApp app = new MessageReceiverAsyncApp(receiverClient, messageReceiverSyncWorker);
        try {
            Disposable disposable = app.processMessageOneByOne();
            //Disposable disposable = app.processMessageInBatch(); // use this one if you want to process in batches.
            System.out.println("Press ENTER to end");
            System.in.read();
            disposable.dispose();
        } catch (Throwable e) {
            logger.logThrowableAsError(e);
        } finally {
            receiverClient.close();
            logger.info("End of MessageReceiverAsyncApp");
        }
    }
}
