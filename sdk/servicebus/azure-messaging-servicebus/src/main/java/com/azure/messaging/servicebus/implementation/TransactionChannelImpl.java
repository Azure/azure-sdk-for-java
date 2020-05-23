package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpCoordinatorLink;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.transaction.Declared;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Implements {@link TransactionChannel} which provide utility for transaction API.
 */
public class TransactionChannelImpl implements TransactionChannel {

    private final Mono<AmqpCoordinatorLink> sendLink;
    private final String fullyQualifiedNamespace;
    private final String linkName;
    private final ClientLogger logger =  new ClientLogger(TransactionChannelImpl.class);

    TransactionChannelImpl(Mono<AmqpCoordinatorLink> sendLink, String fullyQualifiedNamespace, String linkName) {
        this.sendLink = Objects.requireNonNull(sendLink, "'sendLink' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");

    }

    @Override
    public Mono<ByteBuffer> transactionSelect() {

        return  sendLink.flatMap(sendLink ->
            sendLink.createTransaction()).map(state -> {
            Binary txnId = null;
            if (state instanceof Declared) {
                Declared declared = (Declared) state;
                txnId = declared.getTxnId();
                logger.verbose("Created new TX started: {}", txnId);
            } else {
                logger.error("Error in creating transaction, Not supported response: state {}", state);
            }

            return txnId.asByteBuffer();
        });

    }

    @Override
    public Mono<Void> transactionCommit(ServiceBusTransactionContext transactionContext) {
        return sendLink.flatMap(sendLink ->
            sendLink.completeTransaction(transactionContext.getTransactionId(), true)).then();
    }

    @Override
    public Mono<Void> transactionRollback(ServiceBusTransactionContext transactionContext) {
        return sendLink.flatMap(sendLink ->
            sendLink.completeTransaction(transactionContext.getTransactionId(), false)).then();
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, linkName);
    }
}
