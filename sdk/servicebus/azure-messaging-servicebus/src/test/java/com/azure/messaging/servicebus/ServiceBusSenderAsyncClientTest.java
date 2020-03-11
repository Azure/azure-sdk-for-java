// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.amqp.ProxyOptions;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.CbsAuthorizationType;
import com.azure.core.amqp.implementation.ConnectionOptions;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusAmqpConnection;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.Message;
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
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ServiceBusSenderAsyncClientTest {
    private static final String NAMESPACE = "my-namespace";
    private static final String ENTITY_NAME = "my-servicebus-entity";

    @Mock
    private AmqpSendLink sendLink;
    @Mock
    private ServiceBusAmqpConnection connection;
    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private ErrorContextProvider errorContextProvider;

    @Captor
    private ArgumentCaptor<org.apache.qpid.proton.message.Message> singleMessageCaptor;
    @Captor
    private ArgumentCaptor<List<org.apache.qpid.proton.message.Message>> messagesCaptor;

    private MessageSerializer serializer = new ServiceBusMessageSerializer();
    private TracerProvider tracerProvider = new TracerProvider(Collections.emptyList());

    private final ClientLogger logger = new ClientLogger(ServiceBusSenderAsyncClient.class);
    private final MessageSerializer messageSerializer = new ServiceBusMessageSerializer();
    private final AmqpRetryOptions retryOptions = new AmqpRetryOptions()
        .setDelay(Duration.ofMillis(500))
        .setMode(AmqpRetryMode.FIXED)
        .setTryTimeout(Duration.ofSeconds(10));
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final FluxSink<AmqpEndpointState> endpointSink = endpointProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private ServiceBusSenderAsyncClient sender;
    private ServiceBusConnectionProcessor connectionProcessor;
    private ConnectionOptions connectionOptions;

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

        tracerProvider = new TracerProvider(Collections.emptyList());
        connectionOptions = new ConnectionOptions(NAMESPACE, tokenCredential,
            CbsAuthorizationType.SHARED_ACCESS_SIGNATURE, AmqpTransportType.AMQP, retryOptions,
            ProxyOptions.SYSTEM_DEFAULTS, Schedulers.parallel());

        when(connection.getEndpointStates()).thenReturn(endpointProcessor);
        endpointSink.next(AmqpEndpointState.ACTIVE);

        connectionProcessor = Mono.fromCallable(() -> connection).repeat(10).subscribeWith(
            new ServiceBusConnectionProcessor(connectionOptions.getFullyQualifiedNamespace(),
                ENTITY_NAME, connectionOptions.getRetry()));
        sender = new ServiceBusSenderAsyncClient(ENTITY_NAME, connectionProcessor, retryOptions,
            tracerProvider, messageSerializer);

        when(sendLink.getLinkSize()).thenReturn(Mono.just(ServiceBusSenderAsyncClient.MAX_MESSAGE_LENGTH_BYTES));
    }

    @AfterEach
    void teardown() {
        Mockito.framework().clearInlineMocks();
        sendLink = null;
        connection = null;
        singleMessageCaptor = null;
        messagesCaptor = null;
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

        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));
        when(sendLink.send(any(Message.class))).thenReturn(Mono.empty());
        when(sendLink.send(anyList())).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(batch))
            .verifyComplete();

        // Assert
        verify(sendLink).send(messagesCaptor.capture());

        final List<org.apache.qpid.proton.message.Message> messagesSent = messagesCaptor.getValue();
        Assertions.assertEquals(count, messagesSent.size());

        messagesSent.forEach(message -> Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType()));
    }

    /**
     * Verifies that sending a single message will result in calling sender.send(Message).
     */
    @Test
    void sendSingleMessage() {
        // Arrange
        final ServiceBusMessage testData =
            new ServiceBusMessage(TEST_CONTENTS.getBytes(UTF_8));

        // EC is the prefix they use when creating a link that sends to the service round-robin.
        when(connection.createSendLink(eq(ENTITY_NAME), eq(ENTITY_NAME), eq(retryOptions)))
            .thenReturn(Mono.just(sendLink));

        when(sendLink.send(any(org.apache.qpid.proton.message.Message.class))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(sender.send(testData))
            .verifyComplete();

        // Assert
        verify(sendLink, times(1)).send(any(org.apache.qpid.proton.message.Message.class));
        verify(sendLink).send(singleMessageCaptor.capture());

        final Message message = singleMessageCaptor.getValue();
        Assertions.assertEquals(Section.SectionType.Data, message.getBody().getType());
    }
}
