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
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.util.Objects;

/**
 * AMQP node responsible for performing management and metadata operations on an Azure AMQP message broker.
 */
public class ManagementChannel implements AmqpManagementNode {
    private final TokenManager tokenManager;
    private final AmqpChannelProcessor<RequestResponseChannel> createChannel;
    private final String fullyQualifiedNamespace;
    private final ClientLogger logger;
    private final String entityPath;

    public ManagementChannel(AmqpChannelProcessor<RequestResponseChannel> createChannel,
        String fullyQualifiedNamespace, String entityPath, TokenManager tokenManager) {
        this.createChannel = Objects.requireNonNull(createChannel, "'createChannel' cannot be null.");
        this.fullyQualifiedNamespace = Objects.requireNonNull(fullyQualifiedNamespace,
            "'fullyQualifiedNamespace' cannot be null.");
        this.logger = new ClientLogger(String.format("%s<%s>", ManagementChannel.class.getName(), entityPath));
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.tokenManager = Objects.requireNonNull(tokenManager, "'tokenManager' cannot be null.");
    }

    @Override
    public Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message) {
        return isAuthorized().then(createChannel.flatMap(channel -> {
            final Message protonJMessage = MessageUtils.toProtonJMessage(message);

            return channel.sendWithAck(protonJMessage)
                .handle((Message responseMessage, SynchronousSink<AmqpAnnotatedMessage> sink) ->
                    handleResponse(responseMessage, sink, channel.getErrorContext()))
                .switchIfEmpty(Mono.error(new AmqpException(true, String.format(
                    "entityPath[%s] No response received from management channel.", entityPath),
                    channel.getErrorContext())));
        }));
    }

    @Override
    public Mono<AmqpAnnotatedMessage> send(AmqpAnnotatedMessage message, DeliveryOutcome deliveryOutcome) {
        return isAuthorized().then(createChannel.flatMap(channel -> {
            final Message protonJMessage = MessageUtils.toProtonJMessage(message);
            final DeliveryState protonJDeliveryState = MessageUtils.toProtonJDeliveryState(deliveryOutcome);

            return channel.sendWithAck(protonJMessage, protonJDeliveryState)
                .handle((Message responseMessage, SynchronousSink<AmqpAnnotatedMessage> sink) ->
                    handleResponse(responseMessage, sink, channel.getErrorContext()))
                .switchIfEmpty(Mono.error(new AmqpException(true, String.format(
                    "entityPath[%s] outcome[%s] No response received from management channel.", entityPath,
                    deliveryOutcome.getDeliveryState()), channel.getErrorContext())));
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

        logger.warning("status[{}] description[{}] condition[{}] Operation not successful.",
            statusCode, statusDescription, errorCondition);

        final Throwable throwable = ExceptionUtil.toException(errorCondition, statusDescription, errorContext);
        sink.error(throwable);
    }

    private Mono<Void> isAuthorized() {
        return tokenManager.getAuthorizationResults()
            .next()
            .switchIfEmpty(Mono.error(new AmqpException(false, "Did not get response from tokenManager: " + entityPath, getErrorContext())))
            .handle((response, sink) -> {
                if (RequestResponseUtils.isSuccessful(response)) {
                    sink.complete();
                } else {
                    final String message = String.format("User does not have authorization to perform operation "
                        + "on entity [%s]. Response: [%s]", entityPath, response);
                    sink.error(ExceptionUtil.amqpResponseCodeToException(response.getValue(), message,
                        getErrorContext()));
                }
            });
    }

    private AmqpErrorContext getErrorContext() {
        return new SessionErrorContext(fullyQualifiedNamespace, entityPath);
    }
}
