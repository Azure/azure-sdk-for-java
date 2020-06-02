// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpTransaction;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.transaction.Declare;
import org.apache.qpid.proton.amqp.transaction.Declared;
import org.apache.qpid.proton.amqp.transaction.Discharge;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static com.azure.core.amqp.implementation.ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;

/**
 * Encapsulates transaction functions.
 */
public class TransactionCoordinator {

    private final ClientLogger logger = new ClientLogger(TransactionCoordinator.class);

    private final AmqpSendLink sendLink;
    private final MessageSerializer messageSerializer;

    TransactionCoordinator(AmqpSendLink sendLink, MessageSerializer messageSerializer) {
        this.sendLink = sendLink;
        this.messageSerializer = messageSerializer;
    }

    /**
     * Completes the transaction. All the work in this transaction will either rollback or committed as one unit of
     * work.
     *
     * @param transaction that needs to be completed.
     * @param isCommit true for commit and false to rollback this transaction.
     *
     * @return a completable {@link Mono} which represent {@link DeliveryState}.
     */
    Mono<Void> completeTransaction(AmqpTransaction transaction, boolean isCommit) {
        return Mono.fromCallable(() -> {
            final Message message = Proton.message();
            Discharge discharge = new Discharge();
            discharge.setFail(!isCommit);
            discharge.setTxnId(new Binary(transaction.getTransactionId().array()));
            message.setBody(new AmqpValue(discharge));

            final int payloadSize = messageSerializer.getSize(message);
            final int allocationSize = payloadSize + MAX_AMQP_HEADER_SIZE_BYTES;

            final byte[] bytes = new byte[allocationSize];

            int encodedSize = message.encode(bytes, 0, allocationSize);
            Tuple2<byte[], Integer> tuple = Tuples.of(bytes, encodedSize);
            return tuple;
        })
            .flatMap(tuple2 -> sendLink.send(tuple2.getT1(), tuple2.getT2(), DeliveryImpl.DEFAULT_MESSAGE_FORMAT))
            .handle((state, sink) -> {
                if (!(state instanceof Accepted)) {
                    logger.error("Transaction [{}] could not be completed, Service Bus status [{}].",
                        transaction, state.toString());
                }
                sink.complete();
            });
    }

    /**
     * Creates the transaction in message broker.
     *
     * @return a completable {@link Mono} which represent {@link DeliveryState}.
     */
    Mono<AmqpTransaction> createTransaction() {
        return Mono.fromCallable(() -> {
            Message message = Proton.message();
            Declare declare = new Declare();
            message.setBody(new AmqpValue(declare));
            final int payloadSize = messageSerializer.getSize(message);
            final int allocationSize = payloadSize + MAX_AMQP_HEADER_SIZE_BYTES;

            final byte[] bytes = new byte[allocationSize];

            int encodedSize = message.encode(bytes, 0, allocationSize);
            Tuple2<byte[], Integer> tuple = Tuples.of(bytes, encodedSize);
            return tuple;
        })
            .flatMap(tuple2 -> sendLink.send(tuple2.getT1(), tuple2.getT2(), DeliveryImpl.DEFAULT_MESSAGE_FORMAT))
            .handle((state, sink) -> {
                if (state instanceof Declared) {
                    Binary txnId;
                    Declared declared = (Declared) state;
                    txnId = declared.getTxnId();
                    logger.verbose("Created new TX started: {}", txnId);
                    sink.next(new AmqpTransaction(txnId.asByteBuffer()));
                } else {
                    logger.error("Failed to create transaction, message broker status [{}].", state.toString());
                    sink.complete();
                }
            });
    }
}
