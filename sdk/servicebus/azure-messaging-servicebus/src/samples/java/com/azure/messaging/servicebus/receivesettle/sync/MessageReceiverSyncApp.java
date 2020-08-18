package com.azure.messaging.servicebus.receivesettle.sync;

import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.*;

import java.time.Duration;

/**
 * Sample Application that uses sync API to receive and settle messages.
 */
public class MessageReceiverSyncApp {
    private final ServiceBusReceiverClient receiverClient;
    private final MessageReceiverSyncWorker messageReceiverWorker;

    public MessageReceiverSyncApp(ServiceBusReceiverClient receiverClient, MessageReceiverSyncWorker messageReceiverWorker) {
        this.receiverClient = receiverClient;
        this.messageReceiverWorker = messageReceiverWorker;
    }

    /**
     * Keep receiving messages and process them one by one.
     */
    public void processMessageOneByOne() {
        while(true) {
            IterableStream<ServiceBusReceivedMessageContext> received = receiverClient.receiveMessages(10, Duration.ofSeconds(3));
            for (ServiceBusReceivedMessageContext messageContext : received) {
                messageReceiverWorker.processMessageToOrder(messageContext);
            }
        }
    }

    /**
     * Keep receiving messages and process them in batches.
     */
    public void processMessageInBatch() {
        while(true) {
            IterableStream<ServiceBusReceivedMessageContext> messageContextStream = receiverClient.receiveMessages(10, Duration.ofSeconds(3));
            messageReceiverWorker.processMessageToOrderInBatch(messageContextStream);
        }
    }

    public static void main(String[] args) {
        ClientLogger logger = new ClientLogger(MessageReceiverSyncApp.class);
        logger.info("Start of MessageReceiverSyncApp");
        String serviceBusConnectionString = System.getenv("SERVICE_BUS_CONNECTION_STR");
        String queueName = System.getenv("SERVICE_BUS_QUEUE_NAME");

        ServiceBusReceiverClient receiverClient = new ServiceBusClientBuilder()
            .connectionString(serviceBusConnectionString)
            .receiver()
            .queueName(queueName)
            .buildClient();
        OrderSyncService orderService = new OrderSyncService();
        MessageReceiverSyncWorker messageReceiverSyncWorker = new MessageReceiverSyncWorker(receiverClient, orderService);
        MessageReceiverSyncApp app = new MessageReceiverSyncApp(receiverClient, messageReceiverSyncWorker);

        try (receiverClient) {
            app.processMessageOneByOne();
            //app.processMessageInBatch();
        }
        logger.info("End of MessageReceiverSyncApp");
    }
}
