// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.test.support;

import com.azure.spring.integration.core.api.SubscribeOperation;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class SubscribeOperationTest<O extends SubscribeOperation> {

    protected O subscribeOperation;
    protected String destination = "event-hub";

    @Test
    public void testSubscribeAndUnsubscribe() {
        boolean succeed = this.subscribeOperation.subscribe(destination, this::handleMessage);

        assertTrue(succeed);

        verifySubscriberCreatorCalled();
        verifySubscriberRegistered(1);

        boolean unsubscribed = this.subscribeOperation.unsubscribe(destination);

        assertTrue(unsubscribed);
        verifySubscriberUnregistered(1);
    }

    @Test
    public void testSubscribeTwice() {
        boolean onceSucceed = this.subscribeOperation.subscribe(destination, this::handleMessage);

        assertTrue(onceSucceed);
        verifySubscriberRegistered(1);

        boolean twiceSucceed = this.subscribeOperation.subscribe(destination, this::handleMessage);

        assertFalse(twiceSucceed);

        verifySubscriberCreatorCalled();
        verifySubscriberRegistered(1);
        verifySubscriberUnregistered(0);
    }

    @Test
    public void testUnsubscribeNotSubscribed() {
        boolean unsubscribed = this.subscribeOperation.unsubscribe(destination);

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

    public O getSubscribeOperation() {
        return subscribeOperation;
    }

    public void setSubscribeOperation(O subscribeOperation) {
        this.subscribeOperation = subscribeOperation;
    }

}
