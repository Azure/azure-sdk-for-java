// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import com.azure.spring.cloud.context.core.util.Tuple;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.api.EventHubRxOperation;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;
import rx.Observable;
import rx.subscriptions.Subscriptions;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of {@link EventHubRxOperation}.
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 *
 * @deprecated {@link rx} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link EventHubTemplate}. From version 4.0.0, the reactor API support will be moved to
 * com.azure.spring.eventhubs.core.EventHubTemplate.
 */
@Deprecated
public class EventHubRxTemplate extends AbstractEventHubTemplate implements EventHubRxOperation {

    private final ConcurrentHashMap<Tuple<String, String>, Observable<Message<?>>> subjectByNameAndGroup =
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
        Tuple<String, String> nameAndConsumerGroup = Tuple.of(destination, consumerGroup);

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
