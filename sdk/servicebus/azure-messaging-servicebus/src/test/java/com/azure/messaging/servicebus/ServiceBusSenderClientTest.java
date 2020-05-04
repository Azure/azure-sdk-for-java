// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.CreateBatchOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusSenderClientTest {
    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";

    @Mock
    private ServiceBusSenderAsyncClient asyncSender;

    @Captor
    private ArgumentCaptor<ServiceBusMessage> singleMessageCaptor;

    @Captor
    private ArgumentCaptor<List<ServiceBusMessage>> messageListCaptor;

    @Captor
    private ArgumentCaptor<Instant> scheduleMessageCaptor;

    @Captor
    private ArgumentCaptor<Long> cancelScheduleMessageCaptor;

    private ServiceBusSenderClient sender;

    private static final Duration RETRY_TIMEOUT = Duration.ofSeconds(10);
    private static final String TEST_CONTENTS = "My message for service bus queue!";

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(30));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);
        when(asyncSender.getEntityPath()).thenReturn(ENTITY_NAME);
        when(asyncSender.getFullyQualifiedNamespace()).thenReturn(NAMESPACE);
        sender = new ServiceBusSenderClient(asyncSender, RETRY_TIMEOUT);
    }

    @AfterEach
    void teardown() {
        sender.close();
        singleMessageCaptor = null;
        messageListCaptor = null;
        Mockito.framework().clearInlineMocks();
    }

    @Test
    void verifyProperties() {
        Assertions.assertEquals(ENTITY_NAME, sender.getEntityPath());
        Assertions.assertEquals(NAMESPACE, sender.getFullyQualifiedNamespace());
    }

    /**
     * Verifies that an exception is thrown when we create a batch with null options.
     */
    @Test
    void createBatchNull() {
        Assertions.assertThrows(NullPointerException.class, () -> sender.createBatch(null));
    }

    /**
     * Verifies that the default batch is the same size as the message link.
     */
    @Test
    void createBatchDefault() {
        // Arrange
        ServiceBusMessageBatch batch =  new ServiceBusMessageBatch(MAX_MESSAGE_LENGTH_BYTES, null, null,
            null);
        when(asyncSender.createBatch()).thenReturn(Mono.just(batch));

        //Act
        ServiceBusMessageBatch batchMessage = sender.createBatch();

        //Assert
        Assertions.assertEquals(MAX_MESSAGE_LENGTH_BYTES, batchMessage.getMaxSizeInBytes());
        Assertions.assertEquals(0, batchMessage.getCount());
        verify(asyncSender).createBatch();
    }

    /**
     * Verifies we cannot create a batch if the options size is larger than the link.
     */
    @Test
    void createBatchWhenSizeTooBigThanOnSendLink() {
        // Arrange
        int maxLinkSize = 1024;
        int batchSize = maxLinkSize + 10;

        // This event is 1024 bytes when serialized.
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);
        when(asyncSender.createBatch(options)).thenThrow(new IllegalArgumentException("too large size"));

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> sender.createBatch(options));
        verify(asyncSender, times(1)).createBatch(options);
    }

    /**
     * Verifies that the producer can create a batch with a given {@link CreateBatchOptions#getMaximumSizeInBytes()}.
     */
    @Test
    void createsMessageBatchWithSize() {
        // Arrange
        int batchSize = 1024;

        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(batchSize, null, null,
            null);
        when(asyncSender.createBatch(options)).thenReturn(Mono.just(batch));

        // Act
        ServiceBusMessageBatch messageBatch = sender.createBatch(options);

        //Assert
        Assertions.assertEquals(batch, messageBatch);
    }

    /**
     * Verifies that sending an array of message will result in calling sender.send(Message...).
     */
    @Test
    void sendMessageList() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final List<ServiceBusMessage> messages = TestUtils.getServiceBusMessages(count, UUID.randomUUID().toString(),
            contents);

        when(asyncSender.send(messages)).thenReturn(Mono.empty());

        // Act
        sender.send(messages);

        // Assert
        verify(asyncSender, times(1)).send(messages);
        verify(asyncSender).send(messageListCaptor.capture());

        final List<ServiceBusMessage> sentMessages = messageListCaptor.getValue();
        Assertions.assertEquals(count, sentMessages.size());
        sentMessages.forEach(serviceBusMessage -> Assertions.assertArrayEquals(contents, serviceBusMessage.getBody()));
    }

    /**
     * Verifies that sending a single message will result in calling sender.send(Message).
     */
    @Test
    void sendSingleMessage() {
        // Arrange
        final ServiceBusMessage testData =
            new ServiceBusMessage(TEST_CONTENTS.getBytes(UTF_8));

        when(asyncSender.send(testData)).thenReturn(Mono.empty());

        // Act
        sender.send(testData);

        // Assert
        verify(asyncSender, times(1)).send(testData);
        verify(asyncSender).send(singleMessageCaptor.capture());

        final ServiceBusMessage message = singleMessageCaptor.getValue();
        Assertions.assertArrayEquals(testData.getBody(), message.getBody());
    }

    /**
     * Verifies that scheduling a message will result in calling asyncSender.scheduleMessage().
     */
    @Test
    void scheduleMessage() {
        // Arrange
        final ServiceBusMessage testData =
            new ServiceBusMessage(TEST_CONTENTS.getBytes(UTF_8));
        final Instant scheduledEnqueueTime = Instant.now();
        final long sequenceNumber = 1;

        when(asyncSender.scheduleMessage(testData, scheduledEnqueueTime)).thenReturn(Mono.just(sequenceNumber));

        // Act
        sender.scheduleMessage(testData, scheduledEnqueueTime);

        // Assert
        verify(asyncSender, times(1)).scheduleMessage(testData, scheduledEnqueueTime);
        verify(asyncSender).scheduleMessage(singleMessageCaptor.capture(), scheduleMessageCaptor.capture());

        final ServiceBusMessage message = singleMessageCaptor.getValue();
        Assertions.assertArrayEquals(testData.getBody(), message.getBody());

        final Instant scheduledEnqueueTimeActual = scheduleMessageCaptor.getValue();
        Assertions.assertEquals(scheduledEnqueueTime, scheduledEnqueueTimeActual);
    }

    /**
     * Verifies that cancel a scheduled message will result in calling asyncSender.cancelScheduledMessage().
     */
    @Test
    void cancelScheduleMessage() {
        // Arrange
        final long sequenceNumber = 1;

        when(asyncSender.cancelScheduledMessage(sequenceNumber)).thenReturn(Mono.empty());

        // Act
        sender.cancelScheduledMessage(sequenceNumber);

        // Assert
        verify(asyncSender, times(1)).cancelScheduledMessage(sequenceNumber);
        verify(asyncSender).cancelScheduledMessage(cancelScheduleMessageCaptor.capture());

        final long sequenceNumberActual = cancelScheduleMessageCaptor.getValue();
        Assertions.assertEquals(sequenceNumber, sequenceNumberActual);
    }
}
