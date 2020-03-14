// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class ServiceBusReceiverAsyncClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String NAMESPACE = "my-namespace-foo";
    private static final String ENTITY_PATH = "queue-name";
    private static final MessagingEntityType ENTITY_TYPE = MessagingEntityType.QUEUE;

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final TestPublisher<Message> messageProcessor = TestPublisher.createCold();

    private ServiceBusReceiverAsyncClient consumer;
    private TestPublisher<ServiceBusAmqpConnection> connectionPublisher = TestPublisher.createCold();
    private ReceiveMessageOptions receiveOptions;

    @Mock
    private AmqpReceiveLink amqpReceiveLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private TracerProvider tracerProvider;
    @Mock
    private ServiceBusManagementNode managementNode;
    @Mock
    private ServiceBusReceivedMessage receivedMessage;
    @Mock
    private ServiceBusReceivedMessage receivedMessage2;

    @BeforeAll
    static void beforeAll() {
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(10));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.initMocks(this);

        // Forcing us to publish the messages we receive on the AMQP link on single. Similar to how it is done
        // in ReactorExecutor.
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.flux().publishOn(Schedulers.single()));
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(connection.createReceiveLink(anyString(), anyString(), any(ReceiveMode.class), anyBoolean(), any(),
            any(MessagingEntityType.class))).thenReturn(Mono.just(amqpReceiveLink));

        ServiceBusConnectionProcessor processor = new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
            ENTITY_PATH, connectionOptions.getRetry());
        //ServiceBusConnectionProcessor connectionProcessor = connectionPublisher.next(connection).flux().subscribeWith(processor);
        ServiceBusConnectionProcessor connectionProcessor = Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection)).subscribeWith(processor);

        receiveOptions = new ReceiveMessageOptions().setPrefetchCount(PREFETCH);

        consumer = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_PATH, MessagingEntityType.QUEUE,
            false, receiveOptions, connectionProcessor, tracerProvider, messageSerializer, Schedulers.elastic());
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
        consumer.close();
    }

    /**
     * Verifies that when user calls peek more than one time, It returns different object.
     */
    @SuppressWarnings("unchecked")
    @Test
    void peekTwoMessages() {
        // Arrange
        when(managementNode.peek()).thenReturn(Mono.just(receivedMessage), Mono.just(receivedMessage2));

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(receivedMessage)
            .verifyComplete();

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(receivedMessage2)
            .verifyComplete();
    }

    /**
     * Verifies that this peek one messages from a sequence Number.
     */
    @Test
    void peekWithSequenceOneMessage() {
        // Arrange
        final int fromSequenceNumber = 10;
        final ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);

        when(managementNode.peek(fromSequenceNumber)).thenReturn(Mono.just(receivedMessage));

        // Act & Assert
        StepVerifier.create(consumer.peek(fromSequenceNumber))
            .expectNext(receivedMessage)
            .verifyComplete();
    }

    /**
     * Verifies that this receives a number of messages. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    //@Test
    void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 1;
        final List<Message> messages = getMessages(10);

        when(messageSerializer.deserialize(any(Message.class), eq(ServiceBusReceivedMessage.class)))
            .thenReturn(mock(ServiceBusReceivedMessage.class));

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> messages.forEach(m -> messageProcessor.next(m)))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink).addCredits(PREFETCH);
    }

    /**
     * Verifies that we can receive messages from the processor.
     */
    //@Test
    void receivesAndAutoCompletes() {
        // Arrange
        receiveOptions.setAutoComplete(true);

        final Message message = mock(Message.class);
        final Message message2 = mock(Message.class);

        final UUID lockToken1 = UUID.randomUUID();
        final UUID lockToken2 = UUID.randomUUID();

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getLockToken()).thenReturn(lockToken1);
        when(receivedMessage2.getLockToken()).thenReturn(lockToken2);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.complete(lockToken1)).thenReturn(Mono.empty());
        when(managementNode.complete(lockToken2)).thenReturn(Mono.empty());

        // Act and Assert
        StepVerifier.create(consumer.receive().take(2))
            .then(() -> messageProcessor.next(message, message2))
            .expectNext(receivedMessage, receivedMessage2)
            .verifyComplete();

        verify(managementNode).complete(lockToken1);
        verify(managementNode).complete(lockToken2);
    }

    //@Test
    void receivesAndAutoCompleteWithoutLockToken() {
        // Arrange
        receiveOptions.setAutoComplete(true);

        final Message message = mock(Message.class);
        final Message message2 = mock(Message.class);

        when(messageSerializer.deserialize(message, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage);
        when(messageSerializer.deserialize(message2, ServiceBusReceivedMessage.class)).thenReturn(receivedMessage2);

        when(receivedMessage.getLockToken()).thenReturn(null);
        when(receivedMessage2.getLockToken()).thenReturn(null);

        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE))
            .thenReturn(Mono.just(managementNode));

        when(managementNode.complete(any(UUID.class))).thenReturn(Mono.empty());

        // Act and Assert
        StepVerifier.create(consumer.receive().take(2))
            .then(() -> messageProcessor.next(message, message2))
            .expectError(IllegalArgumentException.class)
            .verify();

        verifyZeroInteractions(managementNode);
    }

    /**
     * Verifies that we error if we try to complete a message without a lock token.
     */
    //@Test
    void completeNullLockToken() {
        // Arrange
        when(connection.getManagementNode(ENTITY_PATH, ENTITY_TYPE)).thenReturn(Mono.just(managementNode));
        when(managementNode.complete(any(UUID.class))).thenReturn(Mono.empty());

        Assertions.assertThrows(IllegalArgumentException.class, () -> consumer.complete(receivedMessage));

        verify(managementNode, times(0)).complete(any());
    }

    /**
     * Verifies that we error if we try to complete a null message.
     */
    //@Test
    void completeNullMessage() {
        Assertions.assertThrows(NullPointerException.class, () -> consumer.complete(receivedMessage));
    }


    /**
     * Verifies that this renew the message lock for the message.
     */
    @Test
    void renewMessageLock() {
        // Arrange
        final int numberOfEvents = 1;
        Instant renewTime = mock(Instant.class);

        when(managementNode.renewMessageLock(receivedMessage))
            .thenReturn(Mono.just(renewTime));

        // Act & Assert
        StepVerifier.create(consumer.renewMessageLock(receivedMessage))
            .expectNext(renewTime)
            .verifyComplete();

    }

    private List<Message> getMessages(int numberOfEvents) {
        final Map<String, String> map = Collections.singletonMap("SAMPLE_HEADER", "foo");

        return IntStream.range(0, numberOfEvents)
            .mapToObj(index -> getMessage(PAYLOAD_BYTES, messageTrackingUUID, map))
            .collect(Collectors.toUnmodifiableList());
    }
}
