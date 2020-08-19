// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.receivesettle.async;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.receivesettle.NetworkFailureException;
import com.azure.messaging.servicebus.receivesettle.Order;
import com.azure.messaging.servicebus.receivesettle.OrderServiceFailureException;
import com.azure.messaging.servicebus.receivesettle.sync.OrderSyncService;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * An order service that save orders and possibly throw exception for each operation.
 */
public class OrderAsyncService {
    private final Random rand = new Random();
    private final ClientLogger logger = new ClientLogger(OrderSyncService.class);

    /**
     * Simulate an order creation or update
     * Exception might be thrown before or after the order is saved (logged)
     * @param order The order to create or replace.
     * @return Mono
     */
    public Mono<Void> createOrReplaceOrder(Order order) {
        Order workOrder;
        if (order.getId() == null) {
            workOrder = new Order();
            workOrder.setId(UUID.randomUUID().toString());
            workOrder.setContent(order.getContent());
        } else {
            workOrder = order;
        }
        return this.throwRandomError()
            .then(
                Mono.fromRunnable(() -> logger.info(String.format("Order saved {\"id\": %s, \"content\": %s}", workOrder.getId(), workOrder.getContent()))))
            .then(this.throwRandomError());
    }

    /**
     * Simulate to create and/or update a batch of orders in a single transaction.
     * Exception might be thrown before or after the orders are saved (logged).
     * @param orders The orders to create or replace.
     * @return Mono
     */
    public Mono<Void> batchCreateOrReplaceOrder(Stream<Order> orders) {
        return this.throwRandomError().then(
            Mono.fromRunnable(() -> orders.forEach(order -> {
                String orderId = order.getId();
                if (orderId == null) {
                    Order newOrder = new Order();
                    orderId = UUID.randomUUID().toString();
                    newOrder.setId(orderId);
                    newOrder.setContent(order.getContent());
                }
                logger.info(String.format("Order in batch saved {\"id\": %s, \"content\": %s}", orderId, order.getContent()));
            })).then(this.throwRandomError())
        );
    }

    /**
     * Simulate the reality to throw a network exception or service exception.
     * @return Mono
     */
    private Mono<Void> throwRandomError() {
        int nextInt = rand.nextInt(100);
        if (nextInt == 1) {
            logger.info("A simulated OrderServiceFailureException is thrown");
            return Mono.error(new OrderServiceFailureException("A simulated service failure happens"));
        } else if (nextInt == 2) {
            logger.info("A simulated NetworkFailureException is thrown");
            return Mono.error(new NetworkFailureException("A simulated network failure happens"));
        }
        return Mono.empty();
    }
}
