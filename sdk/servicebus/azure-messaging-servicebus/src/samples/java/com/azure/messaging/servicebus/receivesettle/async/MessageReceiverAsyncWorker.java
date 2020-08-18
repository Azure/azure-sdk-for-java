package com.azure.messaging.servicebus.receivesettle.async;

import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.receivesettle.NetworkFailureException;
import com.azure.messaging.servicebus.receivesettle.Order;
import com.azure.messaging.servicebus.receivesettle.OrderServiceFailureException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MessageReceiverAsyncWorker {
    private final ServiceBusReceiverAsyncClient receiverClient;
    private final OrderAsyncService orderService;

    public MessageReceiverAsyncWorker(ServiceBusReceiverAsyncClient receiverClient, OrderAsyncService orderService) {
        this.receiverClient = receiverClient;
        this.orderService = orderService;
    }

    public Mono<Void> processMessageOneByOne(ServiceBusReceivedMessageContext messageContext) {
        ServiceBusReceivedMessage message = messageContext.getMessage();
        Order order = convertMessageToOrder(message);
        return orderService.createOrReplaceOrder(order)
            .then(receiverClient.complete(message.getLockToken()))
            .onErrorResume(error -> {
                if (error instanceof OrderServiceFailureException) {
                    return receiverClient.deadLetter(message.getLockToken());
                } else if (error instanceof NetworkFailureException) {
                    return receiverClient.abandon(message.getLockToken());
                } else {
                    return receiverClient.defer(message.getLockToken());
                }
            });
    }

    public Mono<Void> processMessageInBatch(IterableStream<ServiceBusReceivedMessageContext> messageContexts) {
        List<ServiceBusReceivedMessage> messageList = messageContexts.stream().map(ServiceBusReceivedMessageContext::getMessage).collect(Collectors.toList());
        Stream<Order> orders = messageList.stream().map(this::convertMessageToOrder);
        return receiverClient.createTransaction().flatMap(txContext -> orderService.batchCreateOrReplaceOrder(orders)
            .then(Flux.fromIterable(messageList).flatMap(message -> receiverClient.complete(message.getLockToken())).then())
            .onErrorResume(error -> {
            if (error instanceof OrderServiceFailureException) {
                return Flux.fromIterable(messageList).flatMap(message -> receiverClient.deadLetter(message.getLockToken())).then();
            } else if (error instanceof NetworkFailureException) {
                return Flux.fromIterable(messageList).flatMap(message -> receiverClient.abandon(message.getLockToken())).then();
            } else {
                return Flux.fromIterable(messageList).flatMap(message -> receiverClient.defer(message.getLockToken())).then();
            }
        }).then(receiverClient.commitTransaction(txContext)));
    }

    public Order convertMessageToOrder(ServiceBusReceivedMessage message) {
        Order order = new Order();
        order.setId((String) message.getProperties().get("ORDER_ID"));
        order.setContent(new String(message.getBody()));
        return order;
    }
}
