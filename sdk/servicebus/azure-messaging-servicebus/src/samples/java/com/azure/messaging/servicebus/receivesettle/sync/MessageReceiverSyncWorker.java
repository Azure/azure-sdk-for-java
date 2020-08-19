// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.receivesettle.sync;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import com.azure.messaging.servicebus.receivesettle.NetworkFailureException;
import com.azure.messaging.servicebus.receivesettle.Order;
import com.azure.messaging.servicebus.receivesettle.OrderServiceFailureException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MessageReceiverSyncApp receives messages then use one or multiple MessageReceiverSyncWorker
 * to process the messages.
 * This sample code uses only one MessageReceiverSyncWorker.
 */
public class MessageReceiverSyncWorker {
    private final OrderSyncService orderService;
    private final ServiceBusReceiverClient receiverClient;

    public MessageReceiverSyncWorker(ServiceBusReceiverClient receiverClient, OrderSyncService orderService) {
        this.orderService = orderService;
        this.receiverClient = receiverClient;
    }

    /**
     * Convert a message to order and call the order service to process it.
     * Handle exceptions thrown from the order service.
     *
     * @param messageContext The message context that includes the received message.
     */
    public void processMessageToOrder(ServiceBusReceivedMessageContext messageContext) {
        ServiceBusReceivedMessage message = messageContext.getMessage();
        Order order = this.convertMessageToOrder(message);
        try {
            this.orderService.createOrReplaceOrder(order);
            this.receiverClient.complete(message.getLockToken());
        } catch (OrderServiceFailureException orderServiceFailureException) {
            this.receiverClient.deadLetter(message.getLockToken());
        } catch (NetworkFailureException networkFailureException) {
            this.receiverClient.abandon(message.getLockToken());
        } catch (Exception otherException) {
            this.receiverClient.defer(message.getLockToken());
        }
    }

    /**
     * Convert a batch of messages to orders and call the order service to process them.
     * Handle exceptions thrown from the order service.
     * The underlying order service will save the order batch in a single transaction, so the message batch
     * will also be settled in a transaction.
     *
     * @param messageContexts The message contexts, each of which includes a received message.
     */
    public void processMessageToOrderInBatch(IterableStream<ServiceBusReceivedMessageContext> messageContexts) {
        List<ServiceBusReceivedMessage> messageList = messageContexts.stream().map(ServiceBusReceivedMessageContext::getMessage).collect(Collectors.toList());
        Stream<Order> orders = messageList.stream().map(this::convertMessageToOrder);
        ServiceBusTransactionContext txContext = this.receiverClient.createTransaction();
        try {
            this.orderService.batchCreateOrReplaceOrder(orders);
            messageList.forEach(message -> this.receiverClient.complete(message.getLockToken()));
        } catch (OrderServiceFailureException orderServiceFailureException) {
            messageList.forEach(message -> this.receiverClient.deadLetter(message.getLockToken()));
        } catch (NetworkFailureException networkFailureException) {
            messageList.forEach(message -> this.receiverClient.abandon(message.getLockToken()));
        } catch (Exception otherException) {
            messageList.forEach(message -> this.receiverClient.defer(message.getLockToken()));
        } finally {
            this.receiverClient.commitTransaction(txContext);
        }
    }

    private Order convertMessageToOrder(ServiceBusReceivedMessage message) {
        Order order = new Order();
        order.setId((String) message.getProperties().get("ORDER_ID"));
        order.setContent(new String(message.getBody()));
        return order;
    }
}
