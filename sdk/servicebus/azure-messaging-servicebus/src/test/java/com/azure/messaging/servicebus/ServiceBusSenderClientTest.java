// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
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
import java.util.Collections;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusSenderClientTest {
    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";

    @Mock
    private ErrorContextProvider errorContextProvider;

    @Mock
    private ServiceBusSenderAsyncClient asyncSender;

    @Captor
    private ArgumentCaptor<ServiceBusMessage> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<ServiceBusMessageBatch> messageBatchCaptor;

    private MessageSerializer serializer = new ServiceBusMessageSerializer();
    private TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());
    private final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(10));

    private ServiceBusSenderClient sender;

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

        sender = new ServiceBusSenderClient(asyncSender, retryOptions.getTryTimeout());

    }

    @AfterEach
    void teardown() {
        sender.close();
        singleMessageCaptor = null;
        messageBatchCaptor = null;
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
        verify(asyncSender, times(1)).createBatch();

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
        try {
            sender.createBatch(options);
            Assertions.fail("Should not have created batch because batchSize is bigger than the size on SenderLink.");
        } catch (Exception ex) {
            Assertions.assertTrue(ex instanceof IllegalArgumentException);
        }

        verify(asyncSender, times(1)).createBatch(options);
    }

    /**
     * Verifies that the producer can create a batch with a given {@link CreateBatchOptions#getMaximumSizeInBytes()}.
     */
    @Test
    void createsMessageBatchWithSize() {
        // Arrange
        int maxLinkSize = 10000;
        int batchSize = 1024;

        // Overhead when serializing an event, to figure out what the maximum size we can use for an event payload.
        int eventOverhead = 46;
        int maxEventPayload = batchSize - eventOverhead;

        // This is 1024 bytes when serialized.
        final ServiceBusMessage message = new ServiceBusMessage(new byte[maxEventPayload]);

        final ServiceBusMessage tooLargeMessage = new ServiceBusMessage(new byte[maxEventPayload + 1]);
        final CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(batchSize);
        ServiceBusMessageBatch batch =  new ServiceBusMessageBatch(batchSize, null, tracerProvider,
            messageSerializer);
        when(asyncSender.createBatch(options)).thenReturn(Mono.just(batch));

        // Act & Assert
        ServiceBusMessageBatch messageBatch = sender.createBatch(options);

        Assertions.assertEquals(batchSize, messageBatch.getMaxSizeInBytes());
        Assertions.assertTrue(messageBatch.tryAdd(message));
        Assertions.assertFalse(messageBatch.tryAdd(tooLargeMessage));

    }

    /**
     * Verifies that sending multiple message will result in calling sender.send(MessageBatch).
     */
    @Test
    void sendMultipleMessages() {
        // Arrange
        final int count = 4;
        final byte[] contents = TEST_CONTENTS.getBytes(UTF_8);
        final ServiceBusMessageBatch batch = new ServiceBusMessageBatch(256 * 1024,
            errorContextProvider, tracerProvider, serializer);

        IntStream.range(0, count).forEach(index -> {
            final ServiceBusMessage message = new ServiceBusMessage(contents);
            Assertions.assertTrue(batch.tryAdd(message));
        });
        when(asyncSender.send(batch)).thenReturn(Mono.empty());

        // Act
        sender.send(batch);

        // Assert
        verify(asyncSender).send(messageBatchCaptor.capture());

        final ServiceBusMessageBatch messagesSent = messageBatchCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.getCount());

        messagesSent.getMessages().forEach(message -> Assertions.assertArrayEquals(contents, message.getBody()));
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
}
