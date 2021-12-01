// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.support;

import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubRxOperation;
import com.azure.spring.integration.eventhub.impl.EventHubProcessor;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Rx implementation for {@link EventHubTestOperation}.
 *
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link EventHubTestOperation}. From version 4.0.0, the reactor API support will be moved to
 * com.azure.spring.eventhubs.support.EventHubTestOperation.
 */
@Deprecated
public class RxEventHubTestOperation extends EventHubTestOperation implements EventHubRxOperation {
    private final ConcurrentHashMap<Tuple<String, String>, Observable<Message<?>>> subjectByNameAndGroup =
        new ConcurrentHashMap<>();

    /**
     *
     * @param clientFactory The client factory.
     * @param eventContextSupplier The event context supplier.
     */
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

    /**
     *
     * @param destination The destination.
     * @param message The message.
     * @param partitionSupplier The partition supplier.
     * @param <T> The type of message.
     * @return The Observable.
     */
    @Override
    public <T> Observable<Void> sendRx(String destination, Message<T> message, PartitionSupplier partitionSupplier) {
        return toObservable(sendAsync(destination, message, partitionSupplier));
    }

    /**
     *
     * @param destination destination
     * @param consumerGroup consumer group
     * @param messagePayloadType message payload type
     * @return The Observable.
     */
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

