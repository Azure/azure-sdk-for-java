// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpShutdownSignal;
import com.azure.core.amqp.implementation.AmqpReceiveLink;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.message.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.Disposable;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.mockito.Mockito.when;

public class ReceiveMessageAndExtendLockSample {

    private static final String PAYLOAD = "hello";
    private static final byte[] PAYLOAD_BYTES = PAYLOAD.getBytes(UTF_8);
    private static final int PREFETCH = 1;

    private final ClientLogger logger = new ClientLogger(MessageReceiverAsyncClient.class);
    private final String messageTrackingUUID = UUID.randomUUID().toString();
    private final DirectProcessor<Message> messageProcessor = DirectProcessor.create();
    private final DirectProcessor<Throwable> errorProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpEndpointState> endpointProcessor = DirectProcessor.create();
    private final DirectProcessor<AmqpShutdownSignal> shutdownProcessor = DirectProcessor.create();

    @Mock
    private AmqpReceiveLink amqpReceiveLink;

    @Captor
    private ArgumentCaptor<Supplier<Integer>> creditSupplier;

    private Mono<AmqpReceiveLink> receiveLinkMono;
    private List<Message> messages = new ArrayList<>();

    private ServiceBusReceiverAsyncClient consumer;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        receiveLinkMono = Mono.fromCallable(() -> amqpReceiveLink);

        when(amqpReceiveLink.receive()).thenReturn(messageProcessor);
        //when(amqpReceiveLink.getErrors()).thenReturn(errorProcessor);
        //when(amqpReceiveLink.getConnectionStates()).thenReturn(endpointProcessor);
        //when(amqpReceiveLink.getShutdownSignals()).thenReturn(shutdownProcessor);

        String connectionString = System.getenv("AZURE_SERVICEBUS_CONNECTION_STRING")
            + ";EntityPath=hemant-test1";

        // Instantiate a client that will be used to call the service.

        consumer = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .scheduler(Schedulers.elastic())
            .buildAsyncReceiverClient();
    }

    @AfterEach
    public void teardown() {
        messages.clear();
        Mockito.framework().clearInlineMocks();
        consumer.close();
    }

    @Test
    public void receiveAndExtendLockMessage() {

        // Arrange
        final int numberOfEvents = 1;

        // Act & Assert
        StepVerifier.create(
            consumer.receive()
                .take(numberOfEvents))
            .assertNext(receivedMessage -> {
                AtomicReference<Instant> timeToRefresh = new AtomicReference<>(receivedMessage.getLockedUntil());
                log(" Got message time to refresh in " + receivedMessage.getLockedUntil());
                Disposable renewDisposable = consumer.renewMessageLock(receivedMessage)
                    .repeat(() -> true)
                    .delayElements(Duration.ofSeconds(1))
                    .subscribe(instant -> {
                        log(" New time instant:" + instant);
                        timeToRefresh.set(instant);
                    });

                // processing the messaging
                int count = 0;
                while (count < 15) {
                    ++count;
                    log(count + ". processing message ");
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ignored) {

                    }
                }
                log("processing done");
                renewDisposable.dispose();
            }).verifyComplete();
    }

    private void log(String message) {
        System.out.println(message);
    }
}
