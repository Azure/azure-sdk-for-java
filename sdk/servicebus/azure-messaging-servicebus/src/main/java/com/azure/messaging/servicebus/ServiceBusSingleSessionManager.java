// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.CreditFlowMode;
import com.azure.core.amqp.implementation.MessageFlux;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.handler.DeliveryNotOnLinkException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.servicebus.implementation.DispositionStatus;
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.MessageFlux.NULL_RETRY_POLICY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MESSAGE_ID_LOGGING_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;

final class ServiceBusSingleSessionManager implements IServiceBusSessionManager {
    private final ClientLogger logger;
    private final String identifier;
    private final MessageSerializer serializer;
    private final Duration operationTimeout;
    private final ServiceBusSessionReactorReceiver sessionReceiver;
    private final Flux<Message> messageFlux;
    private final ServiceBusReceiverInstrumentation instrumentation;

    ServiceBusSingleSessionManager(ClientLogger logger, String identifier,
        ServiceBusSessionReactorReceiver sessionReceiver, int prefetch,
        MessageSerializer serializer, AmqpRetryOptions retryOptions, ServiceBusReceiverInstrumentation instrumentation) {
        this.logger = Objects.requireNonNull(logger, "logger cannot be null.");
        this.identifier = identifier;
        this.sessionReceiver = Objects.requireNonNull(sessionReceiver, "sessionReceiver cannot be null.");
        this.serializer = Objects.requireNonNull(serializer, "serializer cannot be null.");
        Objects.requireNonNull(retryOptions, "retryOptions cannot be null.");
        this.operationTimeout = retryOptions.getTryTimeout();
        final Flux<ServiceBusSessionReactorReceiver> messageFluxUpstream = new SessionReceiverStream(sessionReceiver).flux();
        this.instrumentation = Objects.requireNonNull(instrumentation, "instrumentation cannot be null");
        MessageFlux messageFluxLocal = new MessageFlux(messageFluxUpstream, prefetch, CreditFlowMode.RequestDriven,
            NULL_RETRY_POLICY);
        this.messageFlux = TracingFluxOperator.create(messageFluxLocal, instrumentation);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getLinkName(String sessionId) {
        return sessionReceiver.getSessionId().equals(sessionId) ? sessionReceiver.getLinkName() : null;
    }

    @Override
    public Flux<ServiceBusMessageContext> receive() {
        return receiveMessages()
            .map(m -> new ServiceBusMessageContext(m))
            .onErrorResume(e -> Mono.just(new ServiceBusMessageContext(sessionReceiver.getSessionId(), e)));
    }

    @Override
    public Mono<Boolean> updateDisposition(String lockToken, String sessionId, DispositionStatus dispositionStatus,
        Map<String, Object> propertiesToModify, String deadLetterReason, String deadLetterDescription,
        ServiceBusTransactionContext transactionContext) {
        final DeliveryState deliveryState = MessageUtils.getDeliveryState(dispositionStatus, deadLetterReason,
            deadLetterDescription, propertiesToModify, transactionContext);
        if (sessionReceiver.getSessionId().equals(sessionId)) {
            return sessionReceiver.updateDisposition(lockToken, deliveryState).thenReturn(true);
            // Once the side-by-side support for V1 is no longer needed, as part of deleting V1 ServiceBusSessionManager,
            // Update this method to return Mono<Void> and remove the thenReturn(true).
        } else {
            return Mono.error(DeliveryNotOnLinkException.noMatchingDelivery(lockToken, deliveryState));
        }
    }

    @Override
    public void close() {
        sessionReceiver.closeAsync().block(operationTimeout);
    }

    // Once the side-by-side support for V1 is no longer needed, as part of deleting V1 ServiceBusSessionManager and
    // temporary contract IServiceBusSessionManager, this method will be renamed to 'receive()'
    // replacing the above receive::Flux<ServiceBusMessageContext>.
    Flux<ServiceBusReceivedMessage> receiveMessages() {
        return messageFlux
            .map(qpidMessage -> {
                final ServiceBusReceivedMessage m = serializer.deserialize(qpidMessage, ServiceBusReceivedMessage.class);
                logger.atVerbose()
                    .addKeyValue(SESSION_ID_KEY, sessionReceiver.getSessionId())
                    .addKeyValue(MESSAGE_ID_LOGGING_KEY, m.getMessageId())
                    .log("Received message.");
                return m;
            })
            .doOnError(e -> {
                withLinkInfo(logger.atWarning()).log("Error occurred. Ending session.", e);
            });
    }

    private LoggingEventBuilder withLinkInfo(LoggingEventBuilder builder) {
        return builder.addKeyValue(SESSION_ID_KEY, sessionReceiver.getSessionId())
            .addKeyValue(ENTITY_PATH_KEY, sessionReceiver.getEntityPath())
            .addKeyValue(LINK_NAME_KEY, sessionReceiver.getLinkName());
    }

    private static final class SessionReceiverStream
        extends AtomicBoolean
        implements Consumer<FluxSink<ServiceBusSessionReactorReceiver>> {
        private final ServiceBusSessionReactorReceiver sessionReceiver;

        SessionReceiverStream(ServiceBusSessionReactorReceiver sessionReceiver) {
            super(false);
            this.sessionReceiver = sessionReceiver;
        }

        /**
         * Flux that emits the {@link ServiceBusSessionReactorReceiver} only once. This Flux will be used as the upstream
         * for the {@link MessageFlux}. The MessageFlux streams messages from the receiver that the Flux emits.
         * The Flux will not complete as it will cause he MessageFlux to terminate, the MessageFlux terminates when
         * the receiver terminates.
         * <p>
         * The Flux expects only one subscription, so if the application subscribed to the MessageFlx more than once
         * then an {@link UnsupportedOperationException} will be emitted, see {@link SessionReceiverStream#accept(FluxSink)}.
         * </p>
         * @return the {@link ServiceBusSessionReactorReceiver} flux.
         */
        public Flux<ServiceBusSessionReactorReceiver> flux() {
            final Consumer<FluxSink<ServiceBusSessionReactorReceiver>> emitter = this;
            return Flux.create(emitter);
        }

        @Override
        public void accept(FluxSink<ServiceBusSessionReactorReceiver> sink) {
            sink.onRequest(r -> {
                if (r != 1) {
                    sink.error(new UnsupportedOperationException("Expects one request for sessionReceiver but was " + r));
                    return;
                }
                final boolean emittedOnce = getAndSet(true);
                if (emittedOnce) {
                    sink.error(new UnsupportedOperationException("Cannot subscribe or request for sessionReceiver more than once."));
                    return;
                }
                sink.next(sessionReceiver);
            });
        }
    }
}
