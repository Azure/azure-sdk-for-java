// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core;

import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class EventHubsTemplateTests {

    private EventHubProducerAsyncClient mockProducerClient;
    protected String destination = "event-hub";
    protected Mono<Void> empty = Mono.empty();
    private EventHubsTemplate eventHubsTemplate;

    @BeforeEach
    public void setUp() {

        EventHubsProducerFactory producerFactory = mock(EventHubsProducerFactory.class);
        this.mockProducerClient = mock(EventHubProducerAsyncClient.class);
        when(producerFactory.createProducer(this.destination)).thenReturn(this.mockProducerClient);

        //spy EventHusTemplate just to mock partial method
        this.eventHubsTemplate = spy(new EventHubsTemplate(producerFactory));
        when(this.mockProducerClient.send(any(EventDataBatch.class))).thenReturn(this.empty);
    }


    /**
     * test the three batches case
     */
    @Test
    void testSendAsyncForMessagesWithThreeBatch() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true, true, false, true, true,
            false, true);
        when(eventDataBatch.getCount()).thenReturn(2, 2, 1);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
            messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        Mono<Void> mono = this.eventHubsTemplate.sendAsync(this.destination, messages, null);
        StepVerifier.create(mono)
                    .verifyComplete();
        verify(this.mockProducerClient, times(3)).send(any(EventDataBatch.class));
    }

    /**
     * test the normal one batch case
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

        Mono<Void> mono = this.eventHubsTemplate.sendAsync(this.destination, messages, null).doOnSuccess(t -> {
            System.out.println("do on success:" + t);
        });

        StepVerifier.create(mono)
                    .verifyComplete();
        verify(this.mockProducerClient, times(1)).send(any(EventDataBatch.class));
    }


    /**
     * test the normal one batch case with one exception at the first
     */
    @Test
    void testSendAsyncForMessagesOneBatchAndSendCompletelyWithException() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenThrow(new AmqpException(false,
            AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
            String.format(Locale.US, "Size of the payload exceeded maximum message size: %s kb",
                1024 * 1024 / 1024),
            null)).thenReturn(true, true, true, true);
        when(eventDataBatch.getCount()).thenReturn(5);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
            messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        Mono<Void> mono = this.eventHubsTemplate.sendAsync(this.destination, messages, null).doOnError(ex -> {
            System.out.println("do on Error");
            ex.printStackTrace();
        }).doOnSuccess(t -> {
            System.out.println("do on success:" + t);
        });
        StepVerifier.create(mono)
                    .verifyComplete();
        verify(this.mockProducerClient, times(1)).send(any(EventDataBatch.class));
    }

    /**
     * test the normal two batch case with one exception in the middle
     */
    @Test
    void testSendAsyncForMessagesTwoBatchAndSendCompletelyWithException() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true, false, true).
                                                         thenThrow(new AmqpException(false,
                                                             AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED,
                                                             String.format(Locale.US, "Size of the payload exceeded "
                                                                     + "maximum message size: %s kb",
                                                                 1024 * 1024 / 1024),
                                                             null)).thenReturn(true, true);
        when(eventDataBatch.getCount()).thenReturn(5);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
            messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        Mono<Void> mono = this.eventHubsTemplate.sendAsync(this.destination, messages, null).doOnError(ex -> {
            System.out.println("do on Error");
            ex.printStackTrace();
        }).doOnSuccess(t -> {
            System.out.println("do on success:" + t);
        });
        StepVerifier.create(mono)
                    .verifyComplete();
        verify(this.mockProducerClient, times(2)).send(any(EventDataBatch.class));
    }

    /**
     * test the case that the second event is too large for one new batch
     */
    @Test
    void testSendAsyncForMessagesWithTheSecondEventTooLargeForOneNewBatch() {
        EventDataBatch eventDataBatch = mock(EventDataBatch.class);

        when(this.mockProducerClient.createBatch(any(CreateBatchOptions.class)))
            .thenReturn(Mono.just(eventDataBatch));
        when(eventDataBatch.tryAdd(any(EventData.class))).thenReturn(true, false, false).thenReturn(true, true, true);
        when(eventDataBatch.getCount()).thenReturn(5);
        List<String> messagesList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            messagesList.add("abcde");
        }
        List<Message<String>> messages =
            messagesList.stream().map((Function<String, GenericMessage<String>>) GenericMessage::new).collect(Collectors.toList());

        Mono<Void> mono = this.eventHubsTemplate.sendAsync(this.destination, messages, null).doOnError(ex -> {
            System.out.println("do on Error" + ex.getMessage());
            ex.printStackTrace();
        }).doOnSuccess(t -> {
            System.out.println("do on success:" + t);
        });
        StepVerifier.create(mono)
                    .verifyComplete();
        verify(this.mockProducerClient, times(2)).send(any(EventDataBatch.class));
    }
}
