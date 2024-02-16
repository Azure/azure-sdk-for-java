// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpManagementNode;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.exception.SessionErrorContext;
import com.azure.core.amqp.models.AmqpAnnotatedMessage;
import com.azure.core.amqp.models.DeliveryOutcome;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addKeyValueIfNotNull;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_STATE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_CONDITION_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ERROR_DESCRIPTION_KEY;

/**
 * AMQP node responsible for performing management and metadata operations on an Azure AMQP message broker.
 */
public class ManagementChannel implements AmqpManagementNode {
    private final TokenManager tokenManager;
    private final AmqpChannelProcessor<RequestResponseChannel> createChannel;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;

    /**
     * Creates a new instance of ManagementChannel.
     *
     * @param createChannel Creates a new AMQP channel.
     * @param fullyQualifiedNamespace Fully qualified namespace for the message broker.
     * @param entityPath The entity path for the message broker.
     * @param tokenManager Manages tokens for authorization.
     */
    public ManagementChannel(AmqpChannelProcessor<RequestResponseChannel> createChannel, String fullyQualifiedNamespace,
        String entityPath, TokenManager tokenManager) {
        this.createChannel = Objects.requireNonNull(createChannel, "'createChannel' cannot be null.");
        this.fullyQualifiedNamespace
            = Objects.requireNonNull(fullyQualifiedNamespace, "'fullyQualifiedNamespace' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");

        Map<String, Object> globalLoggingContext = new HashMap<>();
        globalLoggingContext.put(ENTITY_PATH_KEY, entityPath);
        this.logger = new ClientLogger(ManagementChannel.class, globalLoggingContext);

        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
    }

    @Override
    public Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message) {
        return isAuthorized().then(createChannel.flatMap(channel -> {
            final Message protonJMessage = MessageUtils.toProtonJMessage(message);

            return channel.sendWithAck(protonJMessage)
                .handle((Message responseMessage, SynchronousSink<AmqpAnnotatedMessage> sink) -> handleResponse(
                    responseMessage, sink, channel.getErrorContext()))
                .switchIfEmpty(errorIfEmpty(channel, null));
        }));
    }

    @Override
    public Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message, DeliveryOutcome deliveryOutcome) {
        return isAuthorized().then(createChannel.flatMap(channel -> {
            final Message protonJMessage = MessageUtils.toProtonJMessage(message);
            final DeliveryState protonJDeliveryState = MessageUtils.toProtonJDeliveryState(deliveryOutcome);

            return channel.sendWithAck(protonJMessage, protonJDeliveryState)
                .handle((Message responseMessage, SynchronousSink<AmqpAnnotatedMessage> sink) -> handleResponse(
                    responseMessage, sink, channel.getErrorContext()))
                .switchIfEmpty(errorIfEmpty(channel, deliveryOutcome.getDeliveryState()));
        }));
    }

    @Override
    public Mono<Void> closeAsync() {
        return createChannel.flatMap(channel -> channel.closeAsync()).cache();
    }

    private void handleResponse(Message response, SynchronousSink<AmqpAnnotatedMessage> sink,
        AmqpErrorContext errorContext) {

        if (RequestResponseUtils.isSuccessful(response)) {
            sink.next(MessageUtils.toAmqpAnnotatedMessage(response));
            return;
        }

        final AmqpResponseCode statusCode = RequestResponseUtils.getStatusCode(response);
        if (statusCode == AmqpResponseCode.NO_CONTENT) {
            sink.next(MessageUtils.toAmqpAnnotatedMessage(response));
            return;
        }

        final String errorCondition = RequestResponseUtils.getErrorCondition(response);
        if (statusCode == AmqpResponseCode.NOT_FOUND) {
            final AmqpErrorCondition amqpErrorCondition = AmqpErrorCondition.fromString(errorCondition);

            if (amqpErrorCondition == AmqpErrorCondition.MESSAGE_NOT_FOUND) {
                logger.info("There was no matching message found.");
                sink.next(MessageUtils.toAmqpAnnotatedMessage(response));
            } else if (amqpErrorCondition == AmqpErrorCondition.SESSION_NOT_FOUND) {
                logger.info("There was no matching session found.");
                sink.next(MessageUtils.toAmqpAnnotatedMessage(response));
            }

            return;
        }

        final String statusDescription = RequestResponseUtils.getStatusDescription(response);

        LoggingEventBuilder log = logger.atWarning().addKeyValue("status", statusCode);

        addKeyValueIfNotNull(log, ERROR_DESCRIPTION_KEY, statusDescription);
        addKeyValueIfNotNull(log, ERROR_CONDITION_KEY, errorCondition);
        log.log("Operation not successful.");

        final Throwable throwable = ExceptionUtil.toException(errorCondition, statusDescription, errorContext);
        sink.error(throwable);
    }

    private <T> Mono<T> errorIfEmpty(RequestResponseChannel channel,
        com.azure.core.amqp.models.DeliveryState deliveryState) {
        return Mono.error(() -> {
            String error
                = String.format("entityPath[%s] deliveryState[%s] No response received from management channel.",
                    entityPath, deliveryState);
            AmqpException exception = new AmqpException(true, error, channel.getErrorContext());
            return logger.atError().addKeyValue(DELIVERY_STATE_KEY, deliveryState).log(exception);
        });
    }

    private Mono<Void> isAuthorized() {
        return tokenManager.getAuthorizationResults()
            .next()
            .switchIfEmpty(Mono.error(() -> new AmqpException(false,
                "Did not get response from tokenManager: " + entityPath, getErrorContext())))
            .handle((response, sink) -> {
                if (RequestResponseUtils.isSuccessful(response)) {
                    sink.complete();
                } else {
                    final String message = String.format(
                        "User does not have authorization to perform operation " + "on entity [%s]. Response: [%s]",
                        entityPath, response);
                    sink.error(
                        ExceptionUtil.amqpResponseCodeToException(response.getValue(), message, getErrorContext()));
                }
            });
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }
}
