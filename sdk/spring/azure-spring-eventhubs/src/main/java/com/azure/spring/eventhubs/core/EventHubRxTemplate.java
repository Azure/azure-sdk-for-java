// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.spring.messaging.PartitionSupplier;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link EventHubRxOperation}.
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public class EventHubRxTemplate extends AbstractEventHubTemplate implements EventHubRxOperation {

    private final ConcurrentHashMap<Tuple2<String, String>, Observable<Message<?>>> subjectByNameAndGroup =
        new ConcurrentHashMap<>();

    public EventHubRxTemplate(EventHubClientFactory clientFactory) {
        super(clientFactory);
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
        Tuple2<String, String> nameAndConsumerGroup = Tuples.of(destination, consumerGroup);

        subjectByNameAndGroup.computeIfAbsent(nameAndConsumerGroup, k -> Observable.<Message<?>>create(subscriber -> {
            final EventHubProcessor eventHubProcessor = new EventHubProcessor(subscriber::onNext, messagePayloadType,
                getCheckpointConfig(), getMessageConverter());
            this.createEventProcessorClient(destination, consumerGroup, eventHubProcessor);
            this.startEventProcessorClient(destination, consumerGroup);
            subscriber.add(Subscriptions.create(() -> this.stopEventProcessorClient(destination, consumerGroup)));
        }).share());

        return subjectByNameAndGroup.get(nameAndConsumerGroup);
    }

}
