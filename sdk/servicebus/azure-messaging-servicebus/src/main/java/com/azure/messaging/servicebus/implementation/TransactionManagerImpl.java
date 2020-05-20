package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusTransactionContext;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.transaction.Declared;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Objects;

/**
 * Implements {@link TransactionManager} which provide utility for transaction API.
 */
public class TransactionManagerImpl implements TransactionManager {
    static final String OPERATION_CREATE_TRANSACTION = AmqpConstants.VENDOR + ":create-transaction";

    private final MessageSerializer messageSerializer;
    private final TokenManager tokenManager;
    private final Duration operationTimeout;
    private final Mono<AmqpSendLink> sendLink;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;

    TransactionManagerImpl(Mono<AmqpSendLink> sendLink, String fullyQualifiedNamespace, String entityPath,
                           TokenManager tokenManager, MessageSerializer messageSerializer, Duration operationTimeout) {
        this.sendLink = Objects.requireNonNull(sendLink, "'sendLink' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.logger = new ClientLogger(String.format("%s<%s>", TransactionManagerImpl.class, entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer,
            "'messageSerializer' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ByteBuffer> createTransaction() {

        return isAuthorized(OPERATION_CREATE_TRANSACTION).then(sendLink.flatMap(sendLink ->
            sendLink.createTransaction())).map(state -> {
                Binary txnId = null;
                if (state instanceof Declared) {
                    Declared declared = (Declared) state;
                    txnId = declared.getTxnId();
                    logger.verbose("Created new TX started: {}", txnId);
                } else {
                    logger.error("Not supported response: state {}", state);
                }

            return txnId.asByteBuffer();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Void> completeTransaction(ServiceBusTransactionContext transactionContext, boolean isCommit) {
        return isAuthorized(OPERATION_CREATE_TRANSACTION).then(sendLink.flatMap(sendLink ->
            sendLink.completeTransaction(transactionContext.getTransactionId(), isCommit))).then();
    }

    private Mono<Void> isAuthorized(String operation) {
        return tokenManager.getAuthorizationResults()
            .next()
            .handle((response, sink) -> {
                if (response != AmqpResponseCode.ACCEPTED && response != AmqpResponseCode.OK) {
                    sink.error(new AmqpException(false, String.format(
                        "User does not have authorization to perform operation [%s] on entity [%s]. Response: [%s]",
                        operation, entityPath, response), getErrorContext()));
                } else {
                    sink.complete();
                }
            });
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }
}
