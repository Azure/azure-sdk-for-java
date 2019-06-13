// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import com.azure.core.amqp.Retry;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.handler.ReceiveLinkHandler;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.UnicastProcessor;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Handles receiving events from Event Hubs service and translating them to proton-j messages.
 */
public class ReactorReceiver extends EndpointStateNotifierBase implements AmqpReceiveLink {
    // Initial value is true because we could not have created this receiver without authorising against the CBS node first.
    private final AtomicBoolean hasAuthorized = new AtomicBoolean(true);

    private final String entityPath;
    private final Receiver receiver;
    private final ReceiveLinkHandler handler;
    private final ActiveClientTokenManager tokenManager;
    private final Retry retry;
    private final Disposable.Composite subscriptions;
    private final DirectProcessor<Message> messagesProcessor = DirectProcessor.create();
    private FluxSink<Message> messageSink = messagesProcessor.sink();

    ReactorReceiver(String entityPath, Receiver receiver, ReceiveLinkHandler handler, ActiveClientTokenManager tokenManager, Retry retry) {
        super(new ServiceLogger(ReactorSender.class));
        this.entityPath = entityPath;
        this.receiver = receiver;
        this.handler = handler;
        this.tokenManager = tokenManager;
        this.retry = retry;

        this.subscriptions = Disposables.composite(
            handler.getDeliveredMessages().subscribe(this::decodeDelivery),

            handler.getEndpointStates().subscribe(
                this::notifyEndpointState,
                error -> logger.asError().log("Error encountered getting endpointState", error),
                () -> {
                    logger.asVerbose().log("getEndpointStates completed.");
                    notifyEndpointState(EndpointState.CLOSED);
                }),

            handler.getErrors().subscribe(this::notifyError),

            tokenManager.getAuthorizationResults().subscribe(
                response -> {
                    logger.asVerbose().log("Token refreshed: {}", response);
                    hasAuthorized.set(true);
                }, error -> {
                    logger.asInfo().log("clientId[{}], path[{}], linkName[{}] - tokenRenewalFailure[{}]",
                        handler.getConnectionId(), this.entityPath, getLinkName(), error.getMessage());
                    hasAuthorized.set(false);
                }, () -> hasAuthorized.set(false))
        );
    }

    @Override
    public Flux<Message> receive() {
        return messagesProcessor;
    }

    @Override
    public void addCredits(int credits) {
        receiver.flow(credits);
    }

    @Override
    public int credits() {
        return receiver.getRemoteCredit();
    }

    @Override
    public String getLinkName() {
        return receiver.getName();
    }

    @Override
    public String getEntityPath() {
        return entityPath;
    }

    @Override
    public void close() {
        subscriptions.dispose();
        tokenManager.close();
        messagesProcessor.dispose();
        super.close();
    }

    private void decodeDelivery(Delivery delivery) {
        final int messageSize = delivery.pending();
        final byte[] buffer = new byte[messageSize];
        final int read = receiver.recv(buffer, 0, messageSize);

        final Message message = Proton.message();
        message.decode(buffer, 0, read);

        delivery.settle();

        messageSink.next(message);
    }
}
