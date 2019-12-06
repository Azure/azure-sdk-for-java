// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdEndpoint.Config;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater;

@JsonSerialize(using = RntbdClientChannelHealthChecker.JsonSerializer.class)
public final class RntbdClientChannelHealthChecker implements ChannelHealthChecker {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelHealthChecker.class);

    // A channel will be declared healthy if a read succeeded recently as defined by this value.
    private static final long recentReadWindow = 1_000_000_000L;

    // A channel should not be declared unhealthy if a write succeeded recently. As such gaps between
    // Timestamps.lastChannelWrite and Timestamps.lastChannelRead lower than this value are ignored.
    // Guidance: The grace period should be large enough to accommodate the round trip time of the slowest server
    // request. Assuming 1s of network RTT, a 2 MB request, a 2 MB response, a connection that can sustain 1 MB/s
    // both ways, and a 5-second deadline at the server, 10 seconds should be enough.
    private static final long readHangGracePeriod = 10L * 1_000_000_000L;

    // A channel will not be declared unhealthy if a write was attempted recently. As such gaps between
    // Timestamps.lastChannelWriteAttempt and Timestamps.lastChannelWrite lower than this value are ignored.
    // Guidance: The grace period should be large enough to accommodate slow writes. For example, a value of 2s requires
    // that the client can sustain data rates of at least 1 MB/s when writing 2 MB documents.
    private static final long writeHangGracePeriod = 2L * 1_000_000_000L;

    // A channel is considered idle if:
    // idleConnectionTimeout > 0L && System.nanoTime() - Timestamps.lastChannelRead() >= idleConnectionTimeout
    private final long idleConnectionTimeout;

    // A channel will be declared unhealthy if the gap between Timestamps.lastChannelWrite and Timestamps.lastChannelRead
    // grows beyond this value.
    // Constraint: readDelayLimit > readHangGracePeriod
    private final long readDelayLimit;

    // A channel will be declared unhealthy if the gap between Timestamps.lastChannelWriteAttempt and Timestamps.lastChannelWrite
    // grows beyond this value.
    // Constraint: writeDelayLimit > writeHangGracePeriod
    private final long writeDelayLimit;

    // endregion

    // region Constructors

    public RntbdClientChannelHealthChecker(final Config config) {

        checkNotNull(config, "config: null");

        this.idleConnectionTimeout = config.idleConnectionTimeoutInNanos();

        this.readDelayLimit = config.receiveHangDetectionTimeInNanos();
        checkArgument(this.readDelayLimit > readHangGracePeriod, "config.receiveHangDetectionTime: %s", this.readDelayLimit);

        this.writeDelayLimit = config.sendHangDetectionTimeInNanos();
        checkArgument(this.writeDelayLimit > writeHangGracePeriod, "config.sendHangDetectionTime: %s", this.writeDelayLimit);
    }

    // endregion

    // region Methods

    public long idleConnectionTimeout() {
        return this.idleConnectionTimeout;
    }

    public long readDelayLimit() {
        return this.readDelayLimit;
    }

    public long writeDelayLimit() {
        return this.writeDelayLimit;
    }

    public Future<Boolean> isHealthy(final Channel channel) {

        checkNotNull(channel, "expected non-null channel");

        final RntbdRequestManager requestManager = channel.pipeline().get(RntbdRequestManager.class);
        final Promise<Boolean> promise = channel.eventLoop().newPromise();

        if (requestManager == null) {
            reportIssueUnless(logger, !channel.isActive(), channel, "active with no request manager");
            return promise.setSuccess(Boolean.FALSE);
        }

        final Timestamps timestamps = requestManager.snapshotTimestamps();
        final long currentTime = System.nanoTime();

        if (currentTime - timestamps.lastChannelRead() < recentReadWindow) {
            return promise.setSuccess(Boolean.TRUE);  // because we recently received data
        }

        // Black hole detection, part 1:
        // Treat the channel as unhealthy if the gap between the last attempted write and the last successful write
        // grew beyond acceptable limits, unless a write was attempted recently. This is a sign of a hung write.

        final long writeDelay = timestamps.lastChannelWriteAttempt() - timestamps.lastChannelWrite();

        if (writeDelay > this.writeDelayLimit && currentTime - timestamps.lastChannelWriteAttempt() > writeHangGracePeriod) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            logger.warn("{} health check failed due to hung write: {lastChannelWriteAttempt: {}, lastChannelWrite: {}, "
                + "writeDelay: {}, writeDelayLimit: {}, rntbdContext: {}, pendingRequestCount: {}}", channel,
                timestamps.lastChannelWriteAttempt(), timestamps.lastChannelWrite(), writeDelay,
                this.writeDelayLimit, rntbdContext, pendingRequestCount);

            return promise.setSuccess(Boolean.FALSE);
        }

        // Black hole detection, part 2:
        // Treat the connection as unhealthy if the gap between the last successful write and the last successful read
        // grew beyond acceptable limits, unless a write succeeded recently. This is a sign of a hung read.

        final long readDelay = timestamps.lastChannelWrite() - timestamps.lastChannelRead();

        if (readDelay > this.readDelayLimit && currentTime - timestamps.lastChannelWrite() > readHangGracePeriod) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            logger.warn("{} health check failed due to hung read: {lastChannelWrite: {}, lastChannelRead: {}, "
                + "readDelay: {}, readDelayLimit: {}, rntbdContext: {}, pendingRequestCount: {}}", channel,
                timestamps.lastChannelWrite(), timestamps.lastChannelRead(), readDelay,
                this.readDelayLimit, rntbdContext, pendingRequestCount);

            return promise.setSuccess(Boolean.FALSE);
        }

        if (this.idleConnectionTimeout > 0L) {
            if (currentTime - timestamps.lastChannelRead() > this.idleConnectionTimeout) {
                return promise.setSuccess(Boolean.FALSE);
            }
        }

        channel.writeAndFlush(RntbdHealthCheckRequest.MESSAGE).addListener(completed -> {
            if (completed.isSuccess()) {
                promise.setSuccess(Boolean.TRUE);
            } else {
                logger.warn("{} health check request failed due to:", channel, completed.cause());
                promise.setSuccess(Boolean.FALSE);
            }
        });

        return promise;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Types

    static final class JsonSerializer extends StdSerializer<RntbdClientChannelHealthChecker> {

        JsonSerializer() {
            super(RntbdClientChannelHealthChecker.class);
        }

        @Override
        public void serialize(RntbdClientChannelHealthChecker value, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeNumberField("idleConnectionTimeout", value.idleConnectionTimeout());
            generator.writeNumberField("readDelayLimit", value.readDelayLimit());
            generator.writeNumberField("writeDelayLimit", value.writeDelayLimit());
            generator.writeEndObject();
        }
    }

    @JsonSerialize(using = Timestamps.JsonSerializer.class)
    static final class Timestamps {

        private static final AtomicLongFieldUpdater<Timestamps> lastPingUpdater =
            newUpdater(Timestamps.class, "lastPing");

        private static final AtomicLongFieldUpdater<Timestamps> lastReadUpdater =
            newUpdater(Timestamps.class, "lastRead");

        private static final AtomicLongFieldUpdater<Timestamps> lastWriteUpdater =
            newUpdater(Timestamps.class, "lastWrite");

        private static final AtomicLongFieldUpdater<Timestamps> lastWriteAttemptUpdater =
            newUpdater(Timestamps.class, "lastWriteAttempt");

        private volatile long lastPing;
        private volatile long lastRead;
        private volatile long lastWrite;
        private volatile long lastWriteAttempt;

        public Timestamps() {
        }

        @SuppressWarnings("CopyConstructorMissesField")
        public Timestamps(Timestamps other) {
            checkNotNull(other, "other: null");
            this.lastPing = lastPingUpdater.get(other);
            this.lastRead = lastReadUpdater.get(other);
            this.lastWrite = lastWriteUpdater.get(other);
            this.lastWriteAttempt = lastWriteAttemptUpdater.get(other);
        }

        public void channelPingCompleted() {
            lastPingUpdater.set(this, System.nanoTime());
        }

        public void channelReadCompleted() {
            lastReadUpdater.set(this, System.nanoTime());
        }

        public void channelWriteAttempted() {
            lastWriteUpdater.set(this, System.nanoTime());
        }

        public void channelWriteCompleted() {
            lastWriteAttemptUpdater.set(this, System.nanoTime());
        }

        public long lastChannelPing() {
            return lastPingUpdater.get(this);
        }

        public long lastChannelRead() {
            return lastReadUpdater.get(this);
        }

        public long lastChannelWrite() {
            return lastWriteUpdater.get(this);
        }

        public long lastChannelWriteAttempt() {
            return lastWriteAttemptUpdater.get(this);
        }

        @Override
        public String toString() {
            return "RntbdClientChannelHealthChecker.Timestamps(" + RntbdObjectMapper.toJson(this) + ')';
        }

        static final class JsonSerializer extends StdSerializer<Timestamps> {

            JsonSerializer() {
                super(Timestamps.class);
            }

            @Override
            public void serialize(Timestamps value, JsonGenerator generator, SerializerProvider provider) throws IOException {
                generator.writeStartObject();
                generator.writeNumberField("lastChannelPing", value.lastChannelPing());
                generator.writeNumberField("lastChannelRead", value.lastChannelRead());
                generator.writeNumberField("lastChannelWrite", value.lastChannelWrite());
                generator.writeNumberField("lastChannelWriteAttempt", value.lastChannelWriteAttempt());
                generator.writeEndObject();
            }
        }
    }

    // endregion
}
