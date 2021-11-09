// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.spring.eventhubs.core.EventHubsTemplate;
import com.azure.spring.eventhubs.core.producer.EventHubProducerFactory;
import com.azure.spring.eventhubs.support.converter.EventHubMessageConverter;
import com.azure.spring.messaging.PartitionSupplier;
import com.azure.spring.service.eventhubs.processor.RecordEventProcessingListener;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A test implementation of {@link EventHubsTemplate}. This is used for testing.
 */
public class EventHubsTestTemplate extends EventHubsTemplate {

    private final EventHubMessageConverter messageConverter = new EventHubMessageConverter();
    private final RecordEventProcessingListener listener;

    public EventHubsTestTemplate(EventHubProducerFactory producerFactory,
                                 RecordEventProcessingListener listener) {
        super(producerFactory);
        this.listener = listener;
    }

    @Override
    public <U> Mono<Void> sendAsync(String eventHubName,
                                    @NonNull Message<U> message,
                                    PartitionSupplier partitionSupplier) {
        EventData azureMessage = messageConverter.fromMessage(message, EventData.class);

        EventContext eventContext = mock(EventContext.class);
        when(eventContext.getEventData()).thenReturn(azureMessage);
        when(eventContext.updateCheckpointAsync()).thenReturn(Mono.empty());

        listener.onEvent(eventContext);

        return Mono.empty();
    }


}
