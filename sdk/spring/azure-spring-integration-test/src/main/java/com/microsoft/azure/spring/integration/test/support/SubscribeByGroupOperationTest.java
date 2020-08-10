// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.test.support;

import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.junit.Test;
import org.springframework.messaging.Message;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public abstract class SubscribeByGroupOperationTest<O extends SubscribeByGroupOperation> {
    protected O subscribeByGroupOperation;
    protected String consumerGroup = "consumer-group";
    protected String anotherConsumerGroup = "consumer-group2";
    protected String destination = "event-hub";

    public O getSubscribeByGroupOperation() {
        return subscribeByGroupOperation;
    }

    public void setSubscribeByGroupOperation(O subscribeByGroupOperation) {
        this.subscribeByGroupOperation = subscribeByGroupOperation;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public String getAnotherConsumerGroup() {
        return anotherConsumerGroup;
    }

    public void setAnotherConsumerGroup(String anotherConsumerGroup) {
        this.anotherConsumerGroup = anotherConsumerGroup;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

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

        boolean twiceSucceed =
            this.subscribeByGroupOperation.subscribe(destination, consumerGroup, this::handleMessage);

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

        boolean twiceSucceed =
            this.subscribeByGroupOperation.subscribe(destination, anotherConsumerGroup, this::handleMessage);

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
}
