// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {
    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);

    private final RntbdEndpoint endpoint;
    private final RntbdConnectionStateListenerMetrics metrics;
    private final Set<Uri> addressUris;
    private final RntbdOpenConnectionsHandler rntbdOpenConnectionsHandler;
    private final AtomicBoolean openConnectionsInProgress = new AtomicBoolean(false);
    private static final Integer maxOpenConnectionRetryAttempts = 5;
    private final AtomicInteger openConnectionRetryAttempts = new AtomicInteger(0);
    private final Integer proactiveConnectionsCount = 4;
    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final RntbdEndpoint endpoint, final RntbdOpenConnectionsHandler openConnectionsHandler) {
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
        this.rntbdOpenConnectionsHandler = checkNotNull(openConnectionsHandler, "expected non-null openConnectionsHandler");
        this.metrics = new RntbdConnectionStateListenerMetrics();
        this.addressUris = ConcurrentHashMap.newKeySet();
    }

    // endregion

    // region Methods

    public void onBeforeSendRequest(Uri addressUri) {
        checkNotNull(addressUri, "Argument 'addressUri' should not be null");
        this.addressUris.add(addressUri);
    }

    public void onException(Throwable exception) {
        checkNotNull(exception, "expect non-null exception");

        this.metrics.record();

        // * An operation could fail due to an IOException which indicates a connection reset by the server,
        // * or a channel closes unexpectedly because the server stopped taking requests
        // * or the channel has been shutdown gracefully
        if (exception instanceof IOException) {
            if (exception instanceof ClosedChannelException) {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, exception));
            } else {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_FAILURE, exception));
            }
        } else if (exception instanceof RntbdRequestManager.UnhealthyChannelException) {
            // A channel is closed due to Rntbd health check
            this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_FAILURE, exception));
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Will not raise the connection state change event for error", exception);
            }
        }

        openConnectionIfNeeded();
    }

    public RntbdConnectionStateListenerMetrics getMetrics() {
        return this.metrics;
    }

    private void openConnectionIfNeeded() {

        if (this.endpoint.isClosed()) {
            return;
        }

        if (this.endpoint.channelsMetrics() < proactiveConnectionsCount) {
            return;
        }

        if (this.openConnectionsInProgress.compareAndSet(false, true) &&
                this.openConnectionRetryAttempts.get() < maxOpenConnectionRetryAttempts
        ) {
            Mono.defer(() -> this.rntbdOpenConnectionsHandler
                            .openConnection(this.addressUris.stream().findFirst().get(), RntbdOpenConnectionsHandler.DEFENSIVE_CONNECTIONS_MODE))
                    // TODO: Should be wired to CosmosContainerProactiveInitConfig
                    .repeat(proactiveConnectionsCount - this.endpoint.channelsMetrics() - 1)
                    .doOnTerminate(() -> {
                        this.openConnectionsInProgress.set(false);
                        this.openConnectionRetryAttempts.incrementAndGet();
                    })
                    .subscribeOn(CosmosSchedulers.OPEN_CONNECTIONS_BOUNDED_ELASTIC)
                    .subscribe();
        }
    }
    // endregion

    // region Privates

    private int onConnectionEvent(final RntbdConnectionEvent event, final Throwable exception) {

        checkNotNull(exception, "expected non-null exception");

        if (event == RntbdConnectionEvent.READ_EOF || event == RntbdConnectionEvent.READ_FAILURE) {
            if (logger.isDebugEnabled()) {
                logger.debug("onConnectionEvent({\"event\":{},\"time\":{},\"endpoint\":{},\"cause\":{})",
                    event,
                    RntbdObjectMapper.toJson(Instant.now()),
                    RntbdObjectMapper.toJson(this.endpoint),
                    RntbdObjectMapper.toJson(exception));
            }

            // When idleEndpointTimeout reached, SDK will close all existing channels,
            // which will translate into ClosedChannelException which does not mean server is in unhealthy status.
            // But it makes sense to make the server as unhealthy as it is safer to validate the server health again for future requests
            for (Uri addressUri : this.addressUris) {
                addressUri.setUnhealthy();
            }

            return addressUris.size();
        }

        return 0;
    }
    // endregion
}
