// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.azure.core.amqp.AmqpTransaction;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.stream.Stream;

import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @MethodSource("commitParams")
    @ParameterizedTest
    public void testCompleteTransactionRejected(boolean isCommit) {
        final Rejected outcome = new Rejected();

        final AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.completeTransaction(transaction, isCommit))
            .verifyError(IllegalArgumentException.class);

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    @MethodSource("commitParams")
    @ParameterizedTest
    public void testCompleteTransaction(boolean isCommit) {
        final Accepted outcome = Accepted.getInstance();

        final AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.completeTransaction(transaction, isCommit))
            .verifyComplete();

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    @Test
    public void testCreateTransactionRejected() {
        Rejected outcome = new Rejected();

        final TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.createTransaction())
            .verifyError(IllegalArgumentException.class);

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
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
        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    protected static Stream<Arguments> commitParams() {
        return Stream.of(
            Arguments.of(true),
            Arguments.of(false)
        );
    }
}
