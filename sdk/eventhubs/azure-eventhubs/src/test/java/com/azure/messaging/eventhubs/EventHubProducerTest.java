// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.messaging.eventhubs.implementation.AmqpSendLink;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class EventHubProducerTest {
    @Mock
    private AmqpSendLink sendLink;

    @Captor
    ArgumentCaptor<Message> singleMessageCaptor;

    @Captor
    ArgumentCaptor<List<Message>> messagesCaptor;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(sendLink.getLinkSize()).thenReturn(Mono.just(EventHubProducer.MAX_MESSAGE_LENGTH_BYTES));
    }

    @After
    public void teardown() {
        Mockito.framework().clearInlineMocks();
        sendLink = null;
        singleMessageCaptor = null;
        messagesCaptor = null;
    }

    /**
     * Verifies that sending multiple events will result in calling producer.send(List<Message>).
     */
    @Test
    public void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final byte[] contents = CONTENTS.getBytes(UTF_8);
        final Flux<EventData> testData = Flux.range(0, count).flatMap(number -> {
            final EventData data = new EventData(contents);
            return Flux.just(data);
        });

        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final SendOptions options = new SendOptions();
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
            .retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(sendLink), producerOptions);

        // Act
        StepVerifier.create(producer.send(testData, options))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<Message> messagesSent = messagesCaptor.getValue();
        Assert.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assert.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending a single event data will result in calling producer.send(Message).
     */
    @Test
    public void sendSingleMessage() {
        // Arrange
        final EventData testData = new EventData(CONTENTS.getBytes(UTF_8));

        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());

        final SendOptions options = new SendOptions();
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
            .retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(sendLink), producerOptions);

        // Act
        StepVerifier.create(producer.send(testData, options))
            .verifyComplete();

        // Assert
        verify(sendLink, times(1)).send(any(Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assert.assertEquals(Section.SectionType.Data, message.getBody().getType());
    }

    /**
     * Verifies that a partitioned producer cannot also send events with a partition key.
     */
    @Test
    public void partitionProducerCannotSendWithPartitionKey() {
        // Arrange
        final Flux<EventData> testData = Flux.just(
            new EventData(CONTENTS.getBytes(UTF_8)),
            new EventData(CONTENTS.getBytes(UTF_8)));

        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        final SendOptions options = new SendOptions().partitionKey("Some partition key");
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions()
            .retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)))
            .partitionId("my-partition-id");

        final EventHubProducer producer = new EventHubProducer(Mono.just(sendLink), producerOptions);

        // Act & Assert
        try {
            producer.send(testData, options).block(Duration.ofSeconds(10));
            Assert.fail("Should have thrown an exception.");
        } catch (IllegalArgumentException e) {
            // This is what we expect.
        }

        verifyZeroInteractions(sendLink);
    }

    /**
     * Verifies that it fails if we try to send multiple messages that cannot fit in a single message batch.
     */
    @Test
    public void sendTooManyMessages() {
        // Arrange
        int maxLinkSize = 1024;
        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // We believe 20 events is enough for that EventDataBatch to be greater than max size.
        final Flux<EventData> testData = Flux.range(0, 20).flatMap(number -> {
            final EventData data = new EventData(CONTENTS.getBytes(UTF_8));
            return Flux.just(data);
        });

        final SendOptions options = new SendOptions();
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.send(testData, options))
            .verifyErrorMatches(error -> error instanceof AmqpException
                && ((AmqpException) error).getErrorCondition() == ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED);

        verify(link, times(0)).send(any(Message.class));
    }

    /**
     * Verifies that the producer can create an {@link EventDataBatch} with the size given by the underlying AMQP send
     * link.
     */
    @Test
    public void createsEventDataBatch() {
        // Arrange
        int maxLinkSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxLinkSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();

        verify(link, times(2)).getLinkSize();
    }

    /**
     * Verifies we can create an EventDataBatch with partition key and link size.
     */
    @Test
    public void createsEventDataBatchWithPartitionKey() {
        // Arrange
        int maxLinkSize = 1024;

        // No idea what the overhead for adding partition key is. But we know this will be smaller than the max size.
        int eventPayload = maxLinkSize - 100;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[eventPayload]);
        final BatchOptions options = new BatchOptions().partitionKey("some-key");
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assert.assertEquals(options.partitionKey(), batch.getPartitionKey());
                Assert.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();
    }

    /**
     * Verifies we cannot create an EventDataBatch if the BatchOptions size is larger than the link.
     */
    @Test
    public void createEventDataBatchWhenMaxSizeIsTooBig() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // This event is 1024 bytes when serialized.
        final BatchOptions options = new BatchOptions().maximumSizeInBytes(batchSize);
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .expectError(IllegalArgumentException.class)
            .verify();
    }

    /**
     * Verifies that the producer can create an {@link EventDataBatch} with a given {@link
     * BatchOptions#maximumSizeInBytes()}.
     */
    @Test
    public void createsEventDataBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = batchSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);

        final BatchOptions options = new BatchOptions().maximumSizeInBytes(batchSize);
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();
    }

    @Test
    public void batchOptionsIsCloned() {
        // Arrange
        int maxLinkSize = 1024;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        final String originalKey = "some-key";
        final BatchOptions options = new BatchOptions().partitionKey(originalKey);
        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch(options))
            .assertNext(batch -> {
                options.partitionKey("something-else");
                Assert.assertEquals(originalKey, batch.getPartitionKey());
            })
            .verifyComplete();
    }

    @Test
    public void sendsAnEventDataBatch() {
        // Arrange
        int maxLinkSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 24;
        int maxEventPayload = maxLinkSize - eventOverhead;

        final AmqpSendLink link = mock(AmqpSendLink.class);
        when(link.getLinkSize()).thenReturn(Mono.just(maxLinkSize));

        // This event is 1024 bytes when serialized.
        final EventData event = new EventData(new byte[maxEventPayload]);

        // This event will be 1025 bytes when serialized.
        final EventData tooLargeEvent = new EventData(new byte[maxEventPayload + 1]);

        final EventHubProducerOptions producerOptions = new EventHubProducerOptions().retry(new RetryOptions().tryTimeout(Duration.ofSeconds(30)));
        final EventHubProducer producer = new EventHubProducer(Mono.just(link), producerOptions);

        // Act & Assert
        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertTrue(batch.tryAdd(event));
            })
            .verifyComplete();

        StepVerifier.create(producer.createBatch())
            .assertNext(batch -> {
                Assert.assertNull(batch.getPartitionKey());
                Assert.assertFalse(batch.tryAdd(tooLargeEvent));
            })
            .verifyComplete();

        verify(link, times(2)).getLinkSize();
    }

    private static final String CONTENTS = "SSLorem ipsum dolor sit amet, consectetur adipiscing elit. Donec vehicula posuere lobortis. Aliquam finibus volutpat dolor, faucibus pellentesque ipsum bibendum vitae. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Ut sit amet urna hendrerit, dapibus justo a, sodales justo. Mauris finibus augue id pulvinar congue. Nam maximus luctus ipsum, at commodo ligula euismod ac. Phasellus vitae lacus sit amet diam porta placerat. \n"
        + "Ut sodales efficitur sapien ut posuere. Morbi sed tellus est. Proin eu erat purus. Proin massa nunc, condimentum id iaculis dignissim, consectetur et odio. Cras suscipit sem eu libero aliquam tincidunt. Nullam ut arcu suscipit, eleifend velit in, cursus libero. Ut eleifend facilisis odio sit amet feugiat. Phasellus at nunc sit amet elit sagittis commodo ac in nisi. Fusce vitae aliquam quam. Integer vel nibh euismod, tempus elit vitae, pharetra est. Duis vulputate enim a elementum dignissim. Morbi dictum enim id elit scelerisque, in elementum nulla pharetra. \n"
        + "Aenean aliquet aliquet condimentum. Proin dapibus dui id libero tempus feugiat. Sed commodo ligula a lectus mattis, vitae tincidunt velit auctor. Fusce quis semper dui. Phasellus eu efficitur sem. Ut non sem sit amet enim condimentum venenatis id dictum massa. Nullam sagittis lacus a neque sodales, et ultrices arcu mattis. Aliquam erat volutpat. \n"
        + "Aenean fringilla quam elit, id mattis purus vestibulum nec. Praesent porta eros in dapibus molestie. Vestibulum orci libero, tincidunt et turpis eget, condimentum lobortis enim. Fusce suscipit ante et mauris consequat cursus nec laoreet lorem. Maecenas in sollicitudin diam, non tincidunt purus. Nunc mauris purus, laoreet eget interdum vitae, placerat a sapien. In mi risus, blandit eu facilisis nec, molestie suscipit leo. Pellentesque molestie urna vitae dui faucibus bibendum. \n"
        + "Donec quis ipsum ultricies, imperdiet ex vel, scelerisque eros. Ut at urna arcu. Vestibulum rutrum odio dolor, vitae cursus nunc pulvinar vel. Donec accumsan sapien in malesuada tempor. Maecenas in condimentum eros. Sed vestibulum facilisis massa a iaculis. Etiam et nibh felis. Donec maximus, sem quis vestibulum gravida, turpis risus congue dolor, pharetra tincidunt lectus nisi at velit.";
}
