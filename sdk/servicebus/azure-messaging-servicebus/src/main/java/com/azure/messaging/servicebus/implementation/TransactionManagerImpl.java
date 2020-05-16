package com.azure.messaging.servicebus.implementation;

import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.RequestResponseChannel;
import com.azure.core.amqp.implementation.RequestResponseUtils;
import com.azure.core.amqp.implementation.TokenManager;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.Transaction;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.ManagementConstants.OPERATION_RENEW_SESSION_LOCK;

public class TransactionManagerImpl implements TransactionManager {

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
        this.logger = new ClientLogger(String.format("%s<%s>", ManagementChannel.class, entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Transaction> createTransaction() {

        return isAuthorized(OPERATION_RENEW_SESSION_LOCK).then(sendLink.flatMap(sendLink -> {
            final Message message = createManagementMessage(OPERATION_RENEW_SESSION_LOCK, associatedLinkName);

            final Map<String, Object> body = new HashMap<>();
            //body.put(ManagementConstants.SESSION_ID, sessionId);

            message.setBody(new AmqpValue(body));

            return sendWithVerify(sendLink, message);
        })).map(response -> {
            final Object value = ((AmqpValue) response.getBody()).getValue();

            Transaction transaction = null;//new Transaction(ByteBuffer.wrap("".getBytes()));
            return transaction;
        });
    }

    private Mono<Message> sendWithVerify(AmqpSendLink sendLink, Message message) {
        return sendLink.send(message)
            .handle(Message response, SynchronousSink<Message> sink) -> {
                Message m =  null;
                sink.next(response);
                //return ;
            })
            /*.handle((Message response, SynchronousSink<Message> sink) -> {
                if (RequestResponseUtils.isSuccessful(response)) {
                    sink.next(response);
                    return;
                }

                final AmqpResponseCode statusCode = RequestResponseUtils.getStatusCode(response);
                final String statusDescription = RequestResponseUtils.getStatusDescription(response);
                final String errorCondition = RequestResponseUtils.getErrorCondition(response);
                final Throwable throwable = MessageUtils.toException(
                    new ErrorCondition(Symbol.getSymbol(errorCondition), statusDescription), sendLink.getErrorContext());

                logger.warning("status[{}] description[{}] condition[{}] Operation not successful.",
                    statusCode, statusDescription, errorCondition);

                sink.error(throwable);
            })*/
            .switchIfEmpty(Mono.error(new AmqpException(true, "No response received from management channel.",
                sendLink.getErrorContext())));
    }

    /**
     * Creates an AMQP message with the required application properties.
     *
     * @param operation Management operation to perform (ie. peek, update-disposition, etc.)
     * @param associatedLinkName Name of the open receive link that first received the message.
     *
     * @return An AMQP message with the required headers.
     */
    private Message createManagementMessage(String operation, String associatedLinkName) {
        final Duration serverTimeout = MessageUtils.adjustServerTimeout(operationTimeout);
        final Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(ManagementConstants.MANAGEMENT_OPERATION_KEY, operation);
        applicationProperties.put(ManagementConstants.SERVER_TIMEOUT, serverTimeout.toMillis());

        if (!CoreUtils.isNullOrEmpty(associatedLinkName)) {
            applicationProperties.put(ManagementConstants.ASSOCIATED_LINK_NAME_KEY, associatedLinkName);
        }

        final Message message = Proton.message();
        message.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return message;
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
    @Override
    public void close() throws Exception {

    }
}
