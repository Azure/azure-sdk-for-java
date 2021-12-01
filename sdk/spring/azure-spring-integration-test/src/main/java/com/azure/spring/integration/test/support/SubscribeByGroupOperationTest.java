// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @param <O> The type that extends SubscribeByGroupOperation.
 */
public abstract class SubscribeByGroupOperationTest<O extends SubscribeByGroupOperation> {

    /**
     * Subscribe by group operation.
     */
    protected O subscribeByGroupOperation;

    /**
     * Consumer group.
     */
    protected String consumerGroup = "consumer-group";

    /**
     * Another consumer group.
     */
    protected String anotherConsumerGroup = "consumer-group2";

    /**
     * The destination.
     */
    protected String destination = "event-hub";

    /**
     * Test subscribe and unsubscribe.
     */
    @Test
    public void testSubscribeAndUnsubscribe() {
        boolean succeed = this.subscribeByGroupOperation.subscribe(destination, consumerGroup, this::handleMessage);

        assertTrue(succeed);

        verifySubscriberCreatorCalled();
        verifySubscriberRegistered(1);

        boolean unsubscribed = this.subscribeByGroupOperation.unsubscribe(destination, consumerGroup);

        assertTrue(unsubscribed);
        verifySubscriberUnregistered(1);
    }

    /**
     * Test subscribe twice.
     */
    @Test
    public void testSubscribeTwice() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumerGroup, this::handleMessage);

        assertTrue(onceSucceed);
        verifySubscriberRegistered(1);

        boolean twiceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumerGroup,
                                                                        this::handleMessage);

        assertFalse(twiceSucceed);

        verifySubscriberCreatorCalled();
        verifySubscriberRegistered(1);
        verifySubscriberUnregistered(0);
    }

    /**
     * Test subscribe with another group.
     */
    @Test
    public void testSubscribeWithAnotherGroup() {
        boolean onceSucceed = this.subscribeByGroupOperation.subscribe(destination, consumerGroup, this::handleMessage);

        assertTrue(onceSucceed);
        verifySubscriberRegistered(1);

        boolean twiceSucceed = this.subscribeByGroupOperation.subscribe(destination, anotherConsumerGroup,
                                                                        this::handleMessage);

        assertTrue(twiceSucceed);

        verifySubscriberCreatorCalled();
        verifySubscriberUnregistered(0);
    }

    /**
     * Test unsubscribe not subscribed.
     */
    @Test
    public void testUnsubscribeNotSubscribed() {
        boolean unsubscribed = this.subscribeByGroupOperation.unsubscribe(destination, consumerGroup);

        assertFalse(unsubscribed);

        verifySubscriberCreatorNotCalled();
        verifySubscriberUnregistered(0);
    }

    private void handleMessage(Message<?> message) {
    }

    /**
     * Verify subscribe creator called.
     */
    protected abstract void verifySubscriberCreatorCalled();

    /**
     * Verify subscriber creator not called
     */
    protected abstract void verifySubscriberCreatorNotCalled();

    /**
     *
     * @param times The times.
     */
    protected abstract void verifySubscriberRegistered(int times);

    /**
     *
     * @param times The times.
     */
    protected abstract void verifySubscriberUnregistered(int times);

    /**
     *
     * @return The subscribeByGroupOperation.
     */
    public O getSubscribeByGroupOperation() {
        return subscribeByGroupOperation;
    }

    /**
     *
     * @param subscribeByGroupOperation The subscribeByGroupOperation.
     */
    public void setSubscribeByGroupOperation(O subscribeByGroupOperation) {
        this.subscribeByGroupOperation = subscribeByGroupOperation;
    }
}
