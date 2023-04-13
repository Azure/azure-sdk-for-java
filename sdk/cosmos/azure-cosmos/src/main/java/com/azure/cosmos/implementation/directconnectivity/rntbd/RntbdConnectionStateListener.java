// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.directconnectivity.Uri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {
    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);

    private final RntbdEndpoint endpoint;
    private final RntbdConnectionStateListenerMetrics metrics;
    private final Set<Uri> addressUris;
    private final ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final RntbdEndpoint endpoint, final ProactiveOpenConnectionsProcessor proactiveOpenConnectionsProcessor) {
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
        this.metrics = new RntbdConnectionStateListenerMetrics();
        this.addressUris = ConcurrentHashMap.newKeySet();
        this.proactiveOpenConnectionsProcessor = proactiveOpenConnectionsProcessor;
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

        openConnectionIfNeeded(exception);
    }

    public RntbdConnectionStateListenerMetrics getMetrics() {
        return this.metrics;
    }

    private void openConnectionIfNeeded(Throwable exception) {

        if (this.endpoint.isClosed()) {
            return;
        }

        // do not fail here, just log
        // this attempts to make the open connections flow
        // best effort
        if (this.proactiveOpenConnectionsProcessor == null) {
            logger.warn("proactiveOpenConnectionsProcessor is null");
            return;
        }

        // if an application developer has set the default min pool size to 0
        // (a system config which applies to all endpoints) then it is best
        // to avoid opening the min connections / channels required for the endpoint
        // (to reduce CPU cycles to open connections) in the case of a connection
        // close / reset
        // connections will be opened proactively on this endpoint only in the
        // openConnectionsAndInitCaches flow
        if (Configs.getMinConnectionPoolSizePerEndpoint() > 0) {

            logger.warn("Exception occurred {}, trying to proactively open a connection.", exception.getMessage());

            this.proactiveOpenConnectionsProcessor.submitOpenConnectionTask(
                    "",
                    this.endpoint.serviceEndpoint(),
                    this.addressUris.stream().findFirst().get(),
                    this.endpoint.getMinChannelsRequired());
        }

        this.proactiveOpenConnectionsProcessor
                .getOpenConnectionsPublisher()
                .subscribe();
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
