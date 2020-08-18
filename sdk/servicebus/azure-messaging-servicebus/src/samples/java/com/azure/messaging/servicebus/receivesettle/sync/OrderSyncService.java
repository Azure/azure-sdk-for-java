package com.azure.messaging.servicebus.receivesettle.sync;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.receivesettle.NetworkFailureException;
import com.azure.messaging.servicebus.receivesettle.Order;
import com.azure.messaging.servicebus.receivesettle.OrderServiceFailureException;

import java.util.UUID;
import java.util.Random;
import java.util.stream.Stream;

/**
 * An order service that save orders and possibly throw exception for each operation.
 */
public class OrderSyncService {
    private final Random rand = new Random();
    private final ClientLogger logger = new ClientLogger(OrderSyncService.class);

    /**
     * Simulate an order creation or update
     * Exception might be thrown before or after the order is saved (logged)
     */
    public void createOrReplaceOrder(Order order) throws OrderServiceFailureException, NetworkFailureException {
        this.throwRandomError();
        if(order.getId() == null) {
            Order newOrder = new Order();
            newOrder.setId(UUID.randomUUID().toString());
            newOrder.setContent(order.getContent());
            order = newOrder;
        }
        // simulate saving the order into repository.
        logger.info(String.format("Order saved {\"id\": %s, \"content\": %s}", order.getId(), order.getContent()));
        this.throwRandomError();
    }

    /**
     * Simulate to create and/or update a batch of orders in a single transaction.
     * Exception might be thrown before or after the orders are saved (logged)
     */
    public void batchCreateOrReplaceOrder(Stream<Order> orders)  throws OrderServiceFailureException, NetworkFailureException {
        this.throwRandomError();
        orders.forEach(order -> {
            if(order.getId() == null) {
                Order newOrder = new Order();
                newOrder.setId(UUID.randomUUID().toString());
                newOrder.setContent(order.getContent());
                order = newOrder;
            }
            // simulate saving the order into repository.
            logger.info(String.format("Order in batch saved {\"id\": %s, \"content\": %s}", order.getId(), order.getContent()));
        });
        this.throwRandomError();
    }

    /**
     * Simulate the reality to throw a network exception or service exception.
     */
    private void throwRandomError() throws OrderServiceFailureException, NetworkFailureException {
        int nextInt = rand.nextInt(200);
        if(nextInt == 1) {
            logger.info("A simulated OrderServiceFailureException is thrown");
            throw new OrderServiceFailureException("A simulated service failure happens");
        } else if (nextInt == 2) {
            logger.info("A simulated NetworkFailureException is thrown");
            throw new NetworkFailureException("A simulated network failure happens");
        }
    }
}
