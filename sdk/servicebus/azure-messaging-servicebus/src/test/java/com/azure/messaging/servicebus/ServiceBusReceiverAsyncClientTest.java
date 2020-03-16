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
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.models.ReceiveMessageOptions;
import com.azure.messaging.servicebus.models.ReceiveMode;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static com.azure.messaging.servicebus.TestUtils.getMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static reactor.core.publisher.Mono.*;

public class ServiceBusReceiverAsyncClientTest {
    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 5;
    private static final String NAMESPACE = "my-namespace-foo";
    private static final String ENTITY_NAME = "queue-name";

    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();

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
    private ServiceBusReceivedMessage message1;
    @Mock
    private ServiceBusReceivedMessage message2;

    private ServiceBusReceiverAsyncClient consumer;

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
        when(amqpReceiveLink.receive()).thenReturn(messageProcessor.publishOn(Schedulers.single()));
        when(amqpReceiveLink.getEndpointStates()).thenReturn(endpointProcessor);

        when(messageSerializer.deserialize(any(), argThat(ServiceBusReceivedMessage.class::equals)))
            .thenAnswer(invocation -> {
                return mock(ServiceBusReceivedMessage.class);
            });

        ConnectionOptions connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, new AmqpRetryOptions(),
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        when(connection.createReceiveLink(anyString(), anyString(),
            any(ReceiveMode.class))).thenReturn(just(amqpReceiveLink));

        when(connection.getManagementNode(anyString())).thenReturn(just(managementNode));

        ServiceBusConnectionProcessor connectionProcessor = Flux.<ServiceBusAmqpConnection>create(sink -> sink.next(connection))
            .subscribeWith(new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                ENTITY_NAME, connectionOptions.getRetry()));

        ReceiveMessageOptions receiveOptions = new ReceiveMessageOptions().setPrefetchCount(PREFETCH);
        consumer = new ServiceBusReceiverAsyncClient(NAMESPACE, ENTITY_NAME, connectionProcessor, tracerProvider,
            messageSerializer, receiveOptions);
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

        /* Arrange */
        final int numberOfEvents = 1;
        when(managementNode.peek())
            .thenReturn(just(message1), just(message2));

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(message1)
            .verifyComplete();

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNext(message2)
            .verifyComplete();
    }

    /**
     * Verifies that this peek one messages.
     */
    @Test
    void peekOneMessage() {
        // Arrange
        final int numberOfEvents = 1;
        when(managementNode.peek())
            .thenReturn(just(mock(ServiceBusReceivedMessage.class)));

        // Act & Assert
        StepVerifier.create(consumer.peek())
            .expectNextCount(numberOfEvents)
            .verifyComplete();

    }

    /**
     * Verifies that this peek one messages from a sequence Number.
     */
    @Test
    void peekWithSequenceOneMessage() {
        // Arrange
        final int numberOfEvents = 1;
        final int fromSequenceNumber = 10;

        when(managementNode.peek(fromSequenceNumber))
            .thenReturn(just(mock(ServiceBusReceivedMessage.class)));

        // Act & Assert
        StepVerifier.create(consumer.peek(fromSequenceNumber))
            .expectNextCount(numberOfEvents)
            .verifyComplete();
    }

    /**
     * Verifies that this receives a number of messages. Verifies that the initial credits we add are equal to the
     * prefetch value.
     */
    @Test
    void receivesNumberOfEvents() {
        // Arrange
        final int numberOfEvents = 1;

        // Act & Assert
        StepVerifier.create(consumer.receive().take(numberOfEvents))
            .then(() -> sendMessages(messageProcessor.sink(), numberOfEvents))
            .expectNextCount(numberOfEvents)
            .verifyComplete();

        verify(amqpReceiveLink, times(1)).addCredits(PREFETCH);


    }

    private void sendMessages(FluxSink<Message> sink, int numberOfEvents) {
        // When we start receiving, then send those numberOfEvents messages.
        Map<String, String> map = Collections.singletonMap("SAMPLE_HEADER", "foo");

        for (int i = 0; i < numberOfEvents; i++) {
            Message message = getMessage(PAYLOAD_BYTES, messageTrackingUUID, map);
            sink.next(message);
        }
    }
}
