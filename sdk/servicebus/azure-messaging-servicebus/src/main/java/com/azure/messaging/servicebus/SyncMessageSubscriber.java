package com.azure.messaging.servicebus;

import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

public class SyncMessageSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {

    private Subscription subscription;
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    protected void hookOnNext(ServiceBusReceivedMessageContext value) {
        System.out.println(Thread.currentThread().getName() + " : " + value);
        // Request for one more event
        subscription.request(1);
    }

    @Override
    protected void hookOnComplete() {
        System.out.println("Complete");
    }

    @Override
    protected void hookOnError(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    protected void hookOnCancel() {
        System.out.println("Cancelled");
    }

}
