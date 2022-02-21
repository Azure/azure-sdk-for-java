// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

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

        this.metrics.increaseTotalCount();

        // * An operation could fail due to an IOException which indicates a connection reset by the server,
        // * or a channel closes unexpectedly because the server stopped taking requests
        //
        // Currently, only ClosedChannelException will raise onConnectionEvent since it is more sure of a signal the server is going down.

        if (exception instanceof IOException) {

            if (exception instanceof ClosedChannelException) {
                this.metrics.recordAddressUpdated(this.onConnectionEvent(RntbdConnectionEvent.READ_EOF, exception));
            } else {
                logger.warn("Will not raise the connection state change event for error", exception);

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
        private final AtomicLong totalCount;
        private final AtomicLong totalApplicableCount;
        private final AtomicLong totalAddressesUpdatedCount;
        private final AtomicReference<Instant> lastApplicableTimestamp;

        public RntbdConnectionStateListenerMetrics() {

            this.totalCount = new AtomicLong(0L);
            this.totalApplicableCount = new AtomicLong(0L);
            this.totalAddressesUpdatedCount = new AtomicLong(0L);
            this.lastApplicableTimestamp = new AtomicReference<>();
        }

        public void recordAddressUpdated(int addressEntryUpdatedCount) {
            try {
                this.lastApplicableTimestamp.set(Instant.now());
                this.totalApplicableCount.getAndIncrement();
                this.totalAddressesUpdatedCount.accumulateAndGet(addressEntryUpdatedCount, (oldValue, newValue) -> oldValue + newValue);
            } catch (Exception exception) {
                logger.warn("Failed to record connection state listener metrics. ", exception);
            }
        }

        private void increaseTotalCount() {
            try{
                this.totalCount.getAndIncrement();
            } catch (Exception exception) {
                logger.warn("Failed to record total count of connection state listener. ", exception);
            }
        }
    }

    final static class RntbdConnectionStateListenerMetricsJsonSerializer extends com.fasterxml.jackson.databind.JsonSerializer<RntbdConnectionStateListenerMetrics> {

        public RntbdConnectionStateListenerMetricsJsonSerializer() {
        }

        @Override
        public void serialize(RntbdConnectionStateListenerMetrics metrics, JsonGenerator writer, SerializerProvider serializers) throws IOException {
            writer.writeStartObject();

            writer.writeNumberField("totalCount", metrics.totalCount.get());
            writer.writeNumberField("totalApplicableCount", metrics.totalApplicableCount.get());
            writer.writeNumberField("totalAddressesUpdatedCount", metrics.totalAddressesUpdatedCount.get());
            if (metrics.lastApplicableTimestamp.get() != null) {
                writer.writeStringField("lastApplicableTimestamp", metrics.lastApplicableTimestamp.toString());
            }

            writer.writeEndObject();
        }
    }
}
