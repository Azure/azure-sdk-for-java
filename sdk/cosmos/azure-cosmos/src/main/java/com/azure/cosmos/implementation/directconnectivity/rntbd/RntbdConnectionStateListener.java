// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Instant;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {
    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);

    private final IAddressResolver addressResolver;
    private final RntbdEndpoint endpoint;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final IAddressResolver addressResolver, final RntbdEndpoint endpoint) {
        this.addressResolver = checkNotNull(addressResolver, "expected non-null addressResolver");
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
    }

    // endregion

    // region Methods

    public void onException(final RxDocumentServiceRequest request, Throwable exception) {
        checkNotNull(request, "expect non-null request");
        checkNotNull(exception, "expect non-null exception");

        if (exception instanceof GoneException) {
            final Throwable cause = exception.getCause();

            if (cause != null) {

                // GoneException was produced by the client, not the server
                //
                // This could occur for example:
                //
                // * an operation fails due to an IOException which indicates a connection reset by the server,
                // * a channel closes unexpectedly because the server stopped taking requests, or
                // * an error was detected by the transport client (e.g., IllegalStateException)
                // * a request timed out in pending acquisition queue
                // * a request failed fast in admission control layer due to high load
                // * channel connect timed out
                //
                // Currently, only ClosedChannelException will raise onConnectionEvent since it is more sure of a signal the server is going down.

                if (cause instanceof IOException) {

                    if (cause instanceof ClosedChannelException) {
                        this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, request, exception);
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Will not raise the connection state change event for error {}", cause);
                        }
                    }
                }
            }
        }
    }

    // endregion

    // region Privates

    private void onConnectionEvent(final RntbdConnectionEvent event, final RxDocumentServiceRequest request, final Throwable exception) {

        checkNotNull(request, "expected non-null request");
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

                this.addressResolver.updateAddresses(request, this.endpoint.serverKey());
            } else {
                logger.warn("Endpoint closed while onConnectionEvent: {}", this.endpoint);
            }
        }
    }
    // endregion
}
