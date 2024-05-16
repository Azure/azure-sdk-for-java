// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.AmqpMetricsProvider;
import com.azure.core.util.logging.LoggingEventBuilder;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Extendable;
import org.apache.qpid.proton.engine.Handler;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Sender;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.ClientConstants.DELIVERY_STATE_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.EMIT_RESULT_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.NOT_APPLICABLE;

/**
 * Handler that receives events from its corresponding {@link Sender}. Handlers must be associated to a {@link Sender}
 * to receive its events.
 *
 * @see BaseHandler#setHandler(Extendable, Handler)
 * @see Sender
 */
public class SendLinkHandler extends LinkHandler {
    private final String linkName;
    private final String entityPath;
    /**
     * Indicates whether or not the link has ever been remotely active (ie. the service has acknowledged that we have
     * opened a send link to the given entityPath.)
     */
    private final AtomicBoolean isRemoteActive = new AtomicBoolean();
    private final AtomicBoolean isTerminated = new AtomicBoolean();
    private final Sinks.Many<Integer> creditProcessor = Sinks.many().unicast().onBackpressureBuffer();
    private final Sinks.Many<Delivery> deliveryProcessor = Sinks.many().multicast().onBackpressureBuffer();

    /**
     * Creates a new instance of SendLinkHandler.
     *
     * @param connectionId The identifier of the connection this link belongs to.
     * @param hostname The hostname for the connection.
     * @param linkName The name of the link.
     * @param entityPath The entity path this link is connected to.
     * @param metricsProvider The AMQP metrics provider.
     */
    public SendLinkHandler(String connectionId, String hostname, String linkName, String entityPath,
        AmqpMetricsProvider metricsProvider) {
        super(connectionId, hostname, entityPath, metricsProvider);
        this.linkName = Objects.requireNonNull(linkName, "'linkName' cannot be null.");
        this.entityPath = entityPath;
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
     * Gets the link credits.
     *
     * @return The link credits.
     */
    public Flux<Integer> getLinkCredits() {
        return creditProcessor.asFlux();
    }

    /**
     * Gets the delivered messages.
     *
     * @return The delivered messages.
     */
    public Flux<Delivery> getDeliveredMessages() {
        return deliveryProcessor.asFlux();
    }

    /**
     * Closes the handler by completing the completing the delivery and link credit fluxes and publishes {@link
     * EndpointState#CLOSED}. {@link #getEndpointStates()} is completely closed when {@link #onLinkRemoteClose(Event)},
     * {@link #onLinkRemoteDetach(Event)}, or {@link #onLinkFinal(Event)} is called.
     */
    @Override
    public void close() {
        if (isTerminated.getAndSet(true)) {
            return;
        }

        creditProcessor.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
        deliveryProcessor.emitComplete((signalType, emitResult) -> {
            addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult).addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("Unable to emit complete on deliverySink.");
            return false;
        });

        onNext(EndpointState.CLOSED);
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Sender) {
            logger.atVerbose()
                .addKeyValue(LINK_NAME_KEY, link.getName())
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .addKeyValue("localTarget", link.getTarget())
                .log("onLinkLocalOpen");
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (!(link instanceof Sender)) {
            return;
        }

        LoggingEventBuilder logBuilder
            = logger.atInfo().addKeyValue(LINK_NAME_KEY, link.getName()).addKeyValue(ENTITY_PATH_KEY, entityPath);

        if (link.getRemoteTarget() != null) {
            logBuilder.addKeyValue("remoteTarget", link.getRemoteTarget());

            if (!isRemoteActive.getAndSet(true)) {
                onNext(EndpointState.ACTIVE);
            }
        } else {
            logBuilder.addKeyValue("remoteTarget", NOT_APPLICABLE).addKeyValue("action", "waitingForError");
        }
        logBuilder.log("onLinkRemoteOpen");
    }

    @Override
    public void onLinkFlow(Event event) {
        if (!isRemoteActive.getAndSet(true)) {
            onNext(EndpointState.ACTIVE);
        }

        final Sender sender = event.getSender();
        final int credits = sender.getRemoteCredit();
        creditProcessor.emitNext(credits, (signalType, emitResult) -> {
            logger.atVerbose()
                .addKeyValue(LINK_NAME_KEY, linkName)
                .addKeyValue(EMIT_RESULT_KEY, emitResult)
                .addKeyValue("credits", credits)
                .log("Unable to emit credits.");
            return false;
        });

        logger.atVerbose()
            .addKeyValue(LINK_NAME_KEY, linkName)
            .addKeyValue("unsettled", sender.getUnsettled())
            .addKeyValue("credits", credits)
            .log("onLinkFlow.");
    }

    @Override
    public void onLinkLocalClose(Event event) {
        super.onLinkLocalClose(event);

        // Someone called sender.close() to set the local link state to close. Since the link was never remotely
        // active, we complete getEndpointStates() ourselves.
        if (!isRemoteActive.get()) {
            logger.atInfo()
                .addKeyValue(LINK_NAME_KEY, getLinkName())
                .addKeyValue(ENTITY_PATH_KEY, entityPath)
                .log("Sender link was never active. Closing endpoint states.");

            super.close();
        }
    }

    @Override
    public void onDelivery(Event event) {
        Delivery delivery = event.getDelivery();

        while (delivery != null) {
            final Sender sender = (Sender) delivery.getLink();
            final String deliveryTag = new String(delivery.getTag(), StandardCharsets.UTF_8);

            logger.atVerbose()
                .addKeyValue(LINK_NAME_KEY, getLinkName())
                .addKeyValue("unsettled", sender.getUnsettled())
                .addKeyValue("credit", sender.getRemoteCredit())
                .addKeyValue(DELIVERY_STATE_KEY, delivery.getRemoteState())
                .addKeyValue("delivery.isBuffered", delivery.isBuffered())
                .addKeyValue("delivery.id", deliveryTag)
                .log("onDelivery");

            deliveryProcessor.emitNext(delivery, (signalType, emitResult) -> {
                logger.atWarning()
                    .addKeyValue(LINK_NAME_KEY, getLinkName())
                    .addKeyValue(EMIT_RESULT_KEY, emitResult)
                    .addKeyValue("delivery.id", deliveryTag)
                    .log("Unable to emit delivery.");

                return emitResult == Sinks.EmitResult.FAIL_OVERFLOW;
            });

            delivery.settle();
            delivery = sender.current();
        }
    }
}
