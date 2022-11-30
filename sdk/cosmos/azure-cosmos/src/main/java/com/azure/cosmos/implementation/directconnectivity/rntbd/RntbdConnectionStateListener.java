// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

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

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final RntbdEndpoint endpoint) {
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
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
        //
        // Currently, only ClosedChannelException will raise onConnectionEvent since it is more sure of a signal the server is going down.

        if (exception instanceof IOException) {

            if (exception instanceof ClosedChannelException) {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, exception));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Will not raise the connection state change event for error", exception);
                }
            }
        }
    }

    public RntbdConnectionStateListenerMetrics getMetrics() {
        return this.metrics;
    }

    // endregion

    // region Privates

    private int onConnectionEvent(final RntbdConnectionEvent event, final Throwable exception) {

        checkNotNull(exception, "expected non-null exception");

        if (event == RntbdConnectionEvent.READ_EOF) {
            if (!this.endpoint.isClosed()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("onConnectionEvent({\"event\":{},\"time\":{},\"endpoint\":{},\"cause\":{})",
                        event,
                        RntbdObjectMapper.toJson(Instant.now()),
                        RntbdObjectMapper.toJson(this.endpoint),
                        RntbdObjectMapper.toJson(exception));
                }

                for (Uri addressUri : this.addressUris) {
                    addressUri.setUnhealthy();
                }

                return addressUris.size();

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Endpoint closed while onConnectionEvent: {}", this.endpoint);
                }
            }
        }

        return 0;
    }
    // endregion
}
