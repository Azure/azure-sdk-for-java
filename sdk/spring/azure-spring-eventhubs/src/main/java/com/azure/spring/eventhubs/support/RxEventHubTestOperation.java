// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.support;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.core.util.Tuple;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.eventhubs.core.EventHubClientFactory;
import com.azure.spring.eventhubs.core.EventHubRxOperation;
import com.azure.spring.eventhubs.core.EventHubProcessor;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Rx implementation for {@link EventHubTestOperation}.
 */
public class RxEventHubTestOperation extends EventHubTestOperation implements EventHubRxOperation {
    private final ConcurrentHashMap<Tuple<String, String>, Observable<Message<?>>> subjectByNameAndGroup =
        new ConcurrentHashMap<>();

    public RxEventHubTestOperation(EventHubClientFactory clientFactory, Supplier<EventContext> eventContextSupplier) {
        super(clientFactory, eventContextSupplier);
    }

    private static <T> Observable<T> toObservable(Mono<T> mono) {
        return Observable.create(subscriber -> mono.toFuture().whenComplete((result, error) -> {
            if (error != null) {
                subscriber.onError(error);
            } else {
                subscriber.onNext(result);
                subscriber.onCompleted();
            }
        }));
    }

    @Override
    public <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        return toObservable(sendAsync(destination, message, partitionSupplier));
    }

    @Override
    public Observable<Message<?>> subscribe(String destination, String consumerGroup, Class<?> messagePayloadType) {
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

        subjectByNameAndGroup.computeIfAbsent(nameAndConsumerGroup, k -> Observable.<Message<?>>create(subscriber -> {
            final EventHubProcessor eventHubProcessor = createEventProcessor(subscriber::onNext, messagePayloadType);
            this.createEventProcessorClient(destination, consumerGroup, eventHubProcessor);
            this.startEventProcessorClient(destination, consumerGroup);
            subscriber.add(Subscriptions.create(() -> this.stopEventProcessorClient(destination, consumerGroup)));
        }).share());

        return subjectByNameAndGroup.get(nameAndConsumerGroup);
    }
}

