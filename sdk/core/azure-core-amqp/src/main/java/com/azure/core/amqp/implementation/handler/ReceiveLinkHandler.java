// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation.handler;

import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpUtil;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveLinkHandler extends LinkHandler {
    private final String receiverName;
    private AtomicBoolean isFirstResponse = new AtomicBoolean(true);
    private final Flux<Delivery> deliveries;
    private FluxSink<Delivery> deliverySink;
    private String sessionId;
    private Instant sessionLockedUntilUtc;

    public ReceiveLinkHandler(String connectionId, String hostname, String receiverName, String entityPath) {
        super(connectionId, hostname, entityPath, new ClientLogger(ReceiveLinkHandler.class));
        this.deliveries = Flux.create(sink -> {
            deliverySink = sink;
        });
        this.receiverName = receiverName;
    }

    public Flux<Delivery> getDeliveredMessages() {
        return deliveries;
    }

    @Override
    public void close() {
        deliverySink.complete();
        super.close();
    }

    @Override
    public void onLinkLocalOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Receiver) {
            logger.info("onLinkLocalOpen connectionId[{}], linkName[{}], localSource[{}]",
                getConnectionId(), link.getName(), link.getSource());
        }
    }

    @Override
    public void onLinkRemoteOpen(Event event) {
        final Link link = event.getLink();
        if (link instanceof Receiver) {
            if (link.getRemoteSource() != null) {
                Object remoteSourceFilterObj = ((Source) link.getRemoteSource()).getFilter();
                Map<Symbol, Object> remoteSourceFilter = null;
                if (remoteSourceFilterObj != null) {
                    @SuppressWarnings("unchecked")
                    Map<Symbol, Object> responseBody = (Map<Symbol, Object>) ((Source) link.getRemoteSource())
                        .getFilter();
                    remoteSourceFilter = responseBody;
                }
                if (remoteSourceFilter != null && remoteSourceFilter.containsKey(AmqpConstants.SESSION_FILTER)) {
                    this.sessionId = (String) remoteSourceFilter.get(AmqpConstants.SESSION_FILTER);
                    setSessionId(this.sessionId);

                    if (link.getRemoteProperties() != null && link.getRemoteProperties()
                        .containsKey(AmqpConstants.LOCKED_UNTIL_UTC)) {
                        this.sessionLockedUntilUtc = AmqpUtil.convertDotNetTicksToInstant((long) link
                            .getRemoteProperties().get(AmqpConstants.LOCKED_UNTIL_UTC));
                        setSessionLockedUntilUtc(sessionLockedUntilUtc);
                        Instant now = Instant.now();
                        logger.info("Accepted a session with id '{} and SessionLockedUntilUtc {} Now {} . "
                                + "How long session is locked {} ", this.sessionId, this.sessionLockedUntilUtc,
                            now, (this.sessionLockedUntilUtc.getEpochSecond() - now.getEpochSecond()));
                    } else {
                        logger.info("Accepted a session with id '{}', which didn't set '{}' property on the "
                            + "receive link.", this.sessionId, AmqpConstants.LOCKED_UNTIL_UTC);
                        this.sessionLockedUntilUtc = Instant.ofEpochMilli(0);
                    }

                    logger.info("Accepted session with id '{}', lockedUntilUtc '{}''.", this.sessionId,
                        this.sessionLockedUntilUtc);
                }

                logger.info("onLinkRemoteOpen connectionId[{}], linkName[{}], remoteSource[{}]",
                    getConnectionId(), link.getName(), link.getRemoteSource());

                if (isFirstResponse.getAndSet(false)) {
                    onNext(EndpointState.ACTIVE);
                }
            } else {
                logger.info("onLinkRemoteOpen connectionId[{}], linkName[{}], action[waitingForError]",
                    getConnectionId(), link.getName());
            }
        }
    }

    @Override
    public void onDelivery(Event event) {
        if (isFirstResponse.getAndSet(false)) {
            onNext(EndpointState.ACTIVE);
        }

        final Delivery delivery = event.getDelivery();
        final Receiver link = (Receiver) delivery.getLink();

        // If a message spans across deliveries (for ex: 200k message will be 4 frames (deliveries) 64k 64k 64k 8k),
        // all until "last-1" deliveries will be partial
        // reactor will raise onDelivery event for all of these - we only need the last one
        if (!delivery.isPartial()) {
            // One of our customers hit an issue - where duplicate 'Delivery' events are raised to Reactor in
            // proton-j layer
            // While processing the duplicate event - reactor hits an IllegalStateException in proton-j layer
            // before we fix proton-j - this work around ensures that we ignore the duplicate Delivery event
            if (delivery.isSettled()) {
                if (link != null) {
                    logger.verbose("onDelivery connectionId[{}], linkName[{}], updatedLinkCredit[{}], remoteCredit[{}],"
                            + " remoteCondition[{}], delivery.isSettled[{}]",
                        getConnectionId(), link.getName(), link.getCredit(), link.getRemoteCredit(),
                        link.getRemoteCondition(), delivery.isSettled());
                } else {
                    logger.warning("connectionId[{}], delivery.isSettled[{}]", getConnectionId(), delivery.isSettled());
                }
            } else {
                deliverySink.next(delivery);
            }
        }

        if (link != null) {
            logger.verbose("onDelivery connectionId[{}], linkName[{}], updatedLinkCredit[{}], remoteCredit[{}],"
                    + " remoteCondition[{}], delivery.isPartial[{}]",
                getConnectionId(), link.getName(), link.getCredit(), link.getRemoteCredit(), link.getRemoteCondition(),
                delivery.isPartial());
        }
    }
}
