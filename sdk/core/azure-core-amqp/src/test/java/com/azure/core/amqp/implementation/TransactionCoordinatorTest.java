// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransaction;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Rejected;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link TransactionCoordinator}
 */
public class TransactionCoordinatorTest {

    @Mock
    private MessageSerializer messageSerializer;
    @Mock
    private AmqpSendLink sendLink;
    private AutoCloseable mocksCloseable;

    @BeforeEach
    public void setup() {
        mocksCloseable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void teardown() throws Exception {
        // Tear down any inline mocks to avoid memory leaks.
        // https://github.com/mockito/mockito/wiki/What's-new-in-Mockito-2#mockito-2250
        Mockito.framework().clearInlineMock(this);

        if (mocksCloseable != null) {
            mocksCloseable.close();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testCompleteTransactionRejected(boolean isCommit) {
        final Rejected outcome = new Rejected();

        final AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.discharge(transaction, isCommit))
            .verifyError(IllegalArgumentException.class);

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testCompleteTransaction(boolean isCommit) {
        final Accepted outcome = Accepted.getInstance();

        final AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.discharge(transaction, isCommit))
            .verifyComplete();

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    @Test
    public void testCreateTransactionRejected() {
        Rejected outcome = new Rejected();

        final TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(outcome)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.declare())
            .verifyError(IllegalArgumentException.class);

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }

    @Test
    public void testCreateTransaction() {
        final byte[] transactionId = "1".getBytes();
        Declared transactionState = new Declared();
        transactionState.setTxnId(Binary.create(ByteBuffer.wrap(transactionId)));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(transactionState)).when(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());

        StepVerifier.create(transactionCoordinator.declare())
            .assertNext(actual -> {
                Assertions.assertNotNull(actual);
                Assertions.assertArrayEquals(transactionId, actual.getTransactionId().array());
            })
            .verifyComplete();

        verify(sendLink).send(any(byte[].class), anyInt(), eq(DeliveryImpl.DEFAULT_MESSAGE_FORMAT), isNull());
    }
}
