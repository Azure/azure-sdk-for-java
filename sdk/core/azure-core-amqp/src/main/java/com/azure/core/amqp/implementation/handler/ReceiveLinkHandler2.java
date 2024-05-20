// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.amqp.implementation.ReactorDispatcher;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Extendable;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;

/**
 * Handler that receives events from its corresponding {@link Receiver}. Handlers must be associated to a
 * {@link Receiver} to receive its events.
 *
 * Note: ReceiveLinkHandler2 will become the ReceiveLinkHandler once the side by side support for v1 and v2 stack is
 * removed.
 *
 * @see BaseHandler#setHandler(Extendable, Handler)
 * @see Receiver
 */
public class ReceiveLinkHandler2 extends LinkHandler {
    private final String linkName;

    /**
     * Indicates whether or not the link has ever been remotely active (ie. the service has acknowledged that we have
     * opened a send link to the given entityPath.)
     */
    private final AtomicBoolean isRemoteActive = new AtomicBoolean();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final String entityPath;
    private final ReceiverUnsettledDeliveries unsettledDeliveries;
    private final ReceiverDeliveryHandler deliveryHandler;

    /**
     * Creates a new instance of ReceiveLinkHandler2.
     *
     * @param connectionId The identifier of the connection this link belongs to.
     * @param hostname The hostname for the connection.
     * @param linkName The name of the link.
     * @param entityPath The entity path this link is connected to.
     * @param settlingMode The {@link DeliverySettleMode} to use.
     * @param dispatcher The reactor dispatcher to handle reactor events.
     * @param retryOptions The retry options to use when sending dispositions.
     * @param includeDeliveryTagInMessage Whether to include the delivery tag in the message.
     * @param metricsProvider The AMQP metrics provider.
     */
    public ReceiveLinkHandler2(String connectionId, String hostname, String linkName, String entityPath,
        DeliverySettleMode settlingMode, ReactorDispatcher dispatcher, AmqpRetryOptions retryOptions,
        boolean includeDeliveryTagInMessage, AmqpMetricsProvider metricsProvider) {
        super(connectionId, hostname, entityPath, metricsProvider);
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.unsettledDeliveries
            = new ReceiverUnsettledDeliveries(hostname, entityPath, linkName, dispatcher, retryOptions, super.logger);
        this.deliveryHandler = new ReceiverDeliveryHandler(entityPath, linkName, settlingMode, unsettledDeliveries,
            includeDeliveryTagInMessage, super.logger);
    }

    /**
     * Gets the name of the link.
     *
     * @return The name of the link.
     */
    public String getLinkName() {
        return linkName;
    }

    /**
     * Gets the messages.
     *
     * @return The messages.
     */
    public Flux<Message> getMessages() {
        return deliveryHandler.getMessages();
    }

    /**
     * Closes the handler by completing the queued deliveries and deliveries then publishes {@link
     * EndpointState#CLOSED}. {@link #getEndpointStates()} is completely closed when {@link #onLinkRemoteClose(Event)},
     * {@link #onLinkRemoteDetach(Event)}, or {@link #onLinkFinal(Event)} is called.
     */
    @Override
    public void close() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        deliveryHandler.close("Could not emit messages.close when closing handler.");
        unsettledDeliveries.close();

        onNext(EndpointState.CLOSED);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Receiver) {
            logger.atVerbose()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, link.getName())
                .addKeyValue("localSource", link.getSource())
                .log("onLinkLocalOpen");
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (!(link instanceof Receiver)) {
            return;
        }

        LoggingEventBuilder logBuilder
            = logger.atInfo().addKeyValue(ENTITY_PATH_KEY, entityPath).addKeyValue(LINK_NAME_KEY, link.getName());

        if (link.getRemoteSource() != null) {
            logBuilder.addKeyValue("remoteSource", link.getRemoteSource());

            if (!isRemoteActive.getAndSet(true)) {
                onNext(EndpointState.ACTIVE);
            }
        } else {
            logBuilder.addKeyValue("action", "waitingForError");
        }

        logBuilder.log("onLinkRemoteOpen");
    }

    @Override
    public void onDelivery(Event event) {
        if (!isRemoteActive.getAndSet(true)) {
            onNext(EndpointState.ACTIVE);
        }
        deliveryHandler.onDelivery(event.getDelivery());
    }

    @Override
    public void onLinkLocalClose(Event event) {
        super.onLinkLocalClose(event);

        // Someone called receiver.close() to set the local link state to close. Since the link was never remotely
        // active, we complete getEndpointStates() ourselves.
        if (!isRemoteActive.get()) {
            logger.atInfo()
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue(LINK_NAME_KEY, linkName)
                .log("Receiver link was never active. Closing endpoint states");

            super.close();
        }
    }

    @Override
    public void onLinkRemoteClose(Event event) {
        deliveryHandler.close("Could not complete 'messages' when remotely closed.");
        unsettledDeliveries.close();

        super.onLinkRemoteClose(event);
    }

    @Override
    public void onLinkFinal(Event event) {
        // Unlikely though, because RemoteClose() or close() would have been called first.
        // In the case that we haven't cleared the pending deliveries.
        close();

        super.onLinkFinal(event);
    }

    /**
     * Request settlement of an unsettled delivery (identified by the unique {@code deliveryTag}) by sending
     * a disposition frame with a state representing the desired-outcome, which the application wishes to
     * occur at the broker.
     * <p>
     * Disposition frame is sent via the same amqp receive-link that delivered the delivery, which was
     * notified to {@link ReceiverDeliveryHandler#onDelivery(Delivery)}}.
     *
     * @param deliveryTag the unique delivery tag identifying the delivery.
     * @param desiredState The state to include in the disposition frame indicating the desired-outcome
     *                     that the application wish to occur at the broker.
     * @return the {@link Mono} upon subscription starts the work by requesting ProtonJ library to send
     * disposition frame to settle the delivery on the broker, and this Mono terminates once the broker
     * acknowledges with disposition frame indicating outcome (a.ka. remote-outcome).
     * The Mono can terminate if the configured timeout elapses or cannot initiate the request to ProtonJ
     * library.
     */
    public Mono<Void> sendDisposition(String deliveryTag, DeliveryState desiredState) {
        return unsettledDeliveries.sendDisposition(deliveryTag, desiredState);
    }

    /**
     * Perform any optional possible graceful cleanup before the closure.
     *
     * @return a {@link Mono} that completes upon the completion of any pre-close work.
     */
    public Mono<Void> preClose() {
        deliveryHandler.preClose();
        return unsettledDeliveries.terminateAndAwaitForDispositionsInProgressToComplete();
    }

    @Override
    public void onError(Throwable e) {
        deliveryHandler.onLinkError();
        super.onError(e);
    }
}
