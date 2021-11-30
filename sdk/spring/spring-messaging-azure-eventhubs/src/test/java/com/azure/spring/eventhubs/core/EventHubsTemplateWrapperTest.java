// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.spring.eventhubs.core.producer.EventHubsProducerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class EventHubsTemplateWrapperTest {

    private EventHubProducerAsyncClient mockProducerClient;
    protected String destination = "event-hub";
    protected Mono<Void> mono = Mono.empty();
    private EventHubsTemplateWrapper eventHubsTemplateWrapper;

    @BeforeEach
    public void setUp() {

        EventHubsProducerFactory producerFactory = mock(EventHubsProducerFactory.class);
        this.mockProducerClient = mock(EventHubProducerAsyncClient.class);
        CreateBatchOptions options =
                new CreateBatchOptions().setMaximumSizeInBytes(1024).setPartitionKey("test");

        when(producerFactory.createProducer(this.destination)).thenReturn(this.mockProducerClient);

        EventHubsTemplate eventHubsTemplate = spy(new EventHubsTemplate(producerFactory));
        when(eventHubsTemplate.buildCreateBatchOptions(any())).thenReturn(options);
        when(this.mockProducerClient.send(any(EventDataBatch.class))).thenReturn(this.mono);
        this.eventHubsTemplateWrapper = new EventHubsTemplateWrapper(eventHubsTemplate);
    }


    @Test
    void testSendAsyncForMessagesWithMoreThanOneBatch() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
                .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true, true, false, true,
                false);
        when(eventDataBatch.getCount()).thenReturn(2, 2, 1);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
                messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        this.eventHubsTemplateWrapper.sendAsync(this.destination, messages, null).block();
        verify(this.mockProducerClient, times(3)).send(any(EventDataBatch.class));
    }

    /**
     * test the normal case
     */
    @Test
    void testSendAsyncForMessagesOneBatchAndSendCompletely() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
                .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true, true, true, true, true);
        when(eventDataBatch.getCount()).thenReturn(5);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
                messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        this.eventHubsTemplateWrapper.sendAsync(this.destination, messages, null).block();
        verify(this.mockProducerClient, times(1)).send(any(EventDataBatch.class));
    }


}
