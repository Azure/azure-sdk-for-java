// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.azure.core.amqp.AmqpTransaction;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.qpid.proton.amqp.Binary;
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
    AmqpSendLink sendLink;

    @BeforeEach
    public void setup() throws IOException {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCompleteTransaction() {
        final String transactionId = "1";
        Declared transactionState = new Declared();
        transactionState.setTxnId(Binary.create(ByteBuffer.wrap(transactionId.getBytes())));

        AmqpTransaction transaction = new AmqpTransaction(ByteBuffer.wrap("1".getBytes()));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(transactionState)).when(sendLink).send(any(byte[].class), anyInt(), anyInt());

        StepVerifier.create(transactionCoordinator.completeTransaction(transaction, true))
            .verifyComplete();

        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testCreateTransaction() {
        final String transactionId = "1";
        Declared transactionState = new Declared();
        transactionState.setTxnId(Binary.create(ByteBuffer.wrap(transactionId.getBytes())));

        TransactionCoordinator transactionCoordinator = new TransactionCoordinator(sendLink, messageSerializer);

        doReturn(Mono.just(transactionState)).when(sendLink).send(any(byte[].class), anyInt(), anyInt());

        AtomicReference<AmqpTransaction> createdTransaction = new AtomicReference<>();

        StepVerifier.create(transactionCoordinator.createTransaction()
            .map(txn -> {
                createdTransaction.set(txn);
                return txn;
            }))
            .verifyComplete();

        Assertions.assertNotNull(createdTransaction.get(), "Should have got transaction id.");
        Assertions.assertTrue(new String(createdTransaction.get().getTransactionId().array()).equals(transactionId),
            "Transaction id is not equal.");
        verify(sendLink, times(1)).send(any(byte[].class), anyInt(), anyInt());
    }
}
