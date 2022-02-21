// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.directconnectivity.IAddressResolver;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class RntbdConnectionStateListener {
    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdConnectionStateListener.class);

    private final IAddressResolver addressResolver;
    private final RntbdEndpoint endpoint;
    private final RntbdConnectionStateListenerMetrics metrics;

    // endregion

    // region Constructors

    public RntbdConnectionStateListener(final IAddressResolver addressResolver, final RntbdEndpoint endpoint) {
        this.addressResolver = checkNotNull(addressResolver, "expected non-null addressResolver");
        this.endpoint = checkNotNull(endpoint, "expected non-null endpoint");
        this.metrics = new RntbdConnectionStateListenerMetrics();
    }

    // endregion

    // region Methods

    public void onException(Throwable exception) {
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
                        this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, exception));
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Will not raise the connection state change event for error {}", cause);
                        }
                    }
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

                return this.addressResolver.updateAddresses(this.endpoint.serverKey());
            } else {
                logger.warn("Endpoint closed while onConnectionEvent: {}", this.endpoint);
            }
        }

        return 0;
    }
    // endregion

    @JsonSerialize(using = RntbdConnectionStateListenerMetricsJsonSerializer.class)
    final class RntbdConnectionStateListenerMetrics {
        private final AtomicLong totalActedOnCount;
        private final AtomicLong totalAddressesUpdatedCount;
        private final AtomicReference<Instant> lastActedOnTimestamp;

        public RntbdConnectionStateListenerMetrics() {
            totalActedOnCount = new AtomicLong(0L);
            totalAddressesUpdatedCount = new AtomicLong(0L);
            this.lastActedOnTimestamp = new AtomicReference<>();
        }

        public void recordAddressUpdated(int addressEntryUpdatedCount) {
            try {
                this.totalActedOnCount.getAndIncrement();
                this.totalAddressesUpdatedCount.accumulateAndGet(addressEntryUpdatedCount, (oldValue, newValue) -> oldValue + newValue);
                this.lastActedOnTimestamp.set(Instant.now());
            } catch (Exception exception) {
                logger.warn("Failed to record connection state listener metrics. ", exception);
            }
        }
    }

    final static class RntbdConnectionStateListenerMetricsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetrics> {

        public RntbdConnectionStateListenerMetricsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetrics metrics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            writer.writeNumberField("totalActedOnCount", metrics.totalActedOnCount.get());
            writer.writeNumberField("totalAddressesUpdatedCount", metrics.totalAddressesUpdatedCount.get());
            if (metrics.lastActedOnTimestamp.get() != null) {
                writer.writeStringField("lastActedOnTimestamp", metrics.lastActedOnTimestamp.toString());
            }

            writer.writeEndObject();
        }
    }
}
