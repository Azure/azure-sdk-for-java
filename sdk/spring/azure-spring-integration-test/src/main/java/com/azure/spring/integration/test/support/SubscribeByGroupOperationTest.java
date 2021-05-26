// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SubscribeByGroupOperationTest<O extends SubscribeByGroupOperation> {

    protected O subscribeByGroupOperation;
    protected String consumerGroup = "consumer-group";
    protected String anotherConsumerGroup = "consumer-group2";
    protected String destination = "event-hub";

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

    @Test
    public void testUnsubscribeNotSubscribed() {
        boolean unsubscribed = this.subscribeByGroupOperation.unsubscribe(destination, consumerGroup);

        assertFalse(unsubscribed);

        verifySubscriberCreatorNotCalled();
        verifySubscriberUnregistered(0);
    }

    private void handleMessage(Message<?> message) {
    }

    protected abstract void verifySubscriberCreatorCalled();

    protected abstract void verifySubscriberCreatorNotCalled();

    protected abstract void verifySubscriberRegistered(int times);

    protected abstract void verifySubscriberUnregistered(int times);

    public O getSubscribeByGroupOperation() {
        return subscribeByGroupOperation;
    }

    public void setSubscribeByGroupOperation(O subscribeByGroupOperation) {
        this.subscribeByGroupOperation = subscribeByGroupOperation;
    }
}
