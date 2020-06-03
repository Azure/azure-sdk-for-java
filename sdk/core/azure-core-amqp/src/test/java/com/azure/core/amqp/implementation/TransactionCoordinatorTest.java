// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.azure.core.amqp.AmqpTransaction;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Unit tests for {@link TransactionCoordinator}
 */
public class TransactionCoordinatorTest {

    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private AmqpSendLink sendLink;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCompleteTransaction() {
        Accepted outcome = Accepted.getInstance();

        AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), anyInt(), isNull());

        StepVerifier.create(transactionCoordinator.completeTransaction(transaction, true))
            .verifyComplete();

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), anyInt(), isNull());
    }

    @Test
    public void testCreateTransaction() {
        final Duration shortTimeout = Duration.ofSeconds(5);
        final byte[] transactionId = "1".getBytes();
        Declared transactionState = new Declared();
        transactionState.setTxnId(Binary.create(ByteBuffer.wrap(transactionId)));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(transactionState)).when(sendLink).send(any(byte[].class), anyInt(), anyInt(), isNull());

        AmqpTransaction actual = transactionCoordinator.createTransaction().block(shortTimeout);

        Assertions.assertNotNull(actual);
        Assertions.assertArrayEquals(transactionId, actual.getTransactionId().array());
        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), anyInt(), isNull());
    }
}
