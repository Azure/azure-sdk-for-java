// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint.Config;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.util.concurrent.atomic.AtomicLongFieldUpdater.newUpdater;

public final class RntbdClientChannelHealthChecker implements ChannelHealthChecker {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelHealthChecker.class);

    // A channel will be declared healthy if a read succeeded recently as defined by this value.
    private static final long recentReadWindowInNanos = 1_000_000_000L;

    // A channel should not be declared unhealthy if a write succeeded recently. As such gaps between
    // Timestamps.lastChannelWrite and Timestamps.lastChannelRead lower than this value are ignored.
    // Guidance: The grace period should be large enough to accommodate the round trip time of the slowest server
    // request. Assuming 1s of network RTT, a 2 MB request, a 2 MB response, a connection that can sustain 1 MB/s
    // both ways, and a 5-second deadline at the server, 10 seconds should be enough.
    // Adding an additional 45 seconds grace period because of relatively high number of
    // false negatives here under high CPU load (in Spark for example)
    private static final long readHangGracePeriodInNanos = (45L + 10L) * 1_000_000_000L;

    // A channel will not be declared unhealthy if a write was attempted recently. As such gaps between
    // Timestamps.lastChannelWriteAttempt and Timestamps.lastChannelWrite lower than this value are ignored.
    // Guidance: The grace period should be large enough to accommodate slow writes. For example, a value of 2s
    // requires that the client can sustain data rates of at least 1 MB/s when writing 2 MB documents.
    private static final long writeHangGracePeriodInNanos = 2L * 1_000_000_000L;

    @JsonProperty
    private final long idleConnectionTimeoutInNanos;

    @JsonProperty
    private final long readDelayLimitInNanos;

    @JsonProperty
    private final long writeDelayLimitInNanos;

    @JsonProperty
    private final int transitTimeoutDetectionThreshold;

    // endregion

    // region Constructors

    public RntbdClientChannelHealthChecker(final Config config) {

        checkNotNull(config, "expected non-null config");

        checkArgument(config.receiveHangDetectionTimeInNanos() > readHangGracePeriodInNanos,
            "config.receiveHangDetectionTimeInNanos: %s",
            config.receiveHangDetectionTimeInNanos());

        checkArgument(config.sendHangDetectionTimeInNanos() > writeHangGracePeriodInNanos,
            "config.sendHangDetectionTimeInNanos: %s",
            config.sendHangDetectionTimeInNanos());

        this.idleConnectionTimeoutInNanos = config.idleConnectionTimeoutInNanos();
        this.readDelayLimitInNanos = config.receiveHangDetectionTimeInNanos();
        this.writeDelayLimitInNanos = config.sendHangDetectionTimeInNanos();
        this.transitTimeoutDetectionThreshold = config.transitTimeoutDetectionThreshold();

    }

    // endregion

    // region Methods

    /**
     * Returns the idle connection timeout interval in nanoseconds.
     * <p>
     * A channel is considered idle if {@link #idleConnectionTimeoutInNanos} is greater than zero and the time since
     * the last channel read is greater than {@link #idleConnectionTimeoutInNanos}.
     *
     * @return Idle connection timeout interval in nanoseconds.
     */
    public long idleConnectionTimeoutInNanos() {
        return this.idleConnectionTimeoutInNanos;
    }

    /**
     * Returns the read delay limit in nanoseconds.
     * <p>
     * A channel will be declared unhealthy if the gap between the last channel write and the last channel read grows
     * beyond this value.
     * <p>
     * Constraint: {@link #readDelayLimitInNanos} > {@link #readHangGracePeriodInNanos}
     *
     * @return Read delay limit in nanoseconds.
     */
    public long readDelayLimitInNanos() {
        return this.readDelayLimitInNanos;
    }

    /**
     * Returns the write delay limit in nanoseconds.
     * <p>
     * A channel will be declared unhealthy if the gap between the last channel write attempt and the last channel write
     * grows beyond this value.
     * <p>
     * Constraint: {@link #writeDelayLimitInNanos} > {@link #writeHangGracePeriodInNanos}
     *
     * @return Write delay limit in nanoseconds.
     */
    public long writeDelayLimitInNanos() {
        return this.writeDelayLimitInNanos;
    }

    /**
     * Determines whether a specified channel is healthy.
     *
     * @param channel A channel whose health is to be checked.
     * @return A future with a result of {@code true} if the channel is healthy, or {@code false} otherwise.
     */
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

        if (currentTime - timestamps.lastChannelReadNanoTime() < recentReadWindowInNanos) {
            return promise.setSuccess(Boolean.TRUE);  // because we recently received data
        }

        String writeIsHangMessage = this.isWriteHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(writeIsHangMessage)) {
            return promise.setSuccess(Boolean.FALSE);
        }

        String readIsHangMessage = this.isReadHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(readIsHangMessage)) {
            return promise.setSuccess(Boolean.FALSE);
        }

        String idleConnectionValidationMessage = this.idleConnectionValidation(timestamps, currentTime, channel);
        if(StringUtils.isNotEmpty(idleConnectionValidationMessage)) {
            return promise.setSuccess(Boolean.FALSE);
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

    /**
     * Determines whether a specified channel is healthy.
     *
     * @param channel A channel whose health is to be checked.
     * @return A future with a result of {@code true} if the channel is healthy, or {@code false} otherwise.
     */
    public Future<String> isHealthyWithFailureReason(final Channel channel) {

        checkNotNull(channel, "expected non-null channel");

        final RntbdRequestManager requestManager = channel.pipeline().get(RntbdRequestManager.class);
        final Promise<String> promise = channel.eventLoop().newPromise();

        if (requestManager == null) {
            reportIssueUnless(logger, !channel.isActive(), channel, "active with no request manager");
            return promise.setSuccess("active with no request manager");
        }

        final Timestamps timestamps = requestManager.snapshotTimestamps();
        final long currentTime = System.nanoTime();

        if (currentTime - timestamps.lastChannelReadNanoTime() < recentReadWindowInNanos) {
            // because we recently received data
            return promise.setSuccess(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
        }

        String writeIsHangMessage = this.isWriteHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(writeIsHangMessage)) {
            return promise.setSuccess(writeIsHangMessage);
        }

        String readIsHangMessage = this.isReadHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(readIsHangMessage)) {
            return promise.setSuccess(readIsHangMessage);
        }

        String idleConnectionValidationMessage = this.idleConnectionValidation(timestamps, currentTime, channel);
        if(StringUtils.isNotEmpty(idleConnectionValidationMessage)) {
            return promise.setSuccess(idleConnectionValidationMessage);
        }

        channel.writeAndFlush(RntbdHealthCheckRequest.MESSAGE).addListener(completed -> {
            if (completed.isSuccess()) {
                promise.setSuccess(RntbdConstants.RntbdHealthCheckResults.SuccessValue);
            } else {
                String msg = MessageFormat.format(
                    "{0} health check request failed due to: {1}",
                    channel,
                    completed.cause().toString()
                );

                logger.warn(msg);
                promise.setSuccess(msg);
            }
        });

        return promise;
    }

    private String isWriteHang(Timestamps timestamps, long currentTime, RntbdRequestManager requestManager, Channel channel) {
        // Treat the channel as unhealthy if the gap between the last attempted to write and the last successful write
        // grew beyond acceptable limits, unless a write was attempted recently. This is a sign of a non-responding write.

        final long writeDelayInNanos =
                timestamps.lastChannelWriteAttemptNanoTime() - timestamps.lastChannelWriteNanoTime();

        final long writeHangDurationInNanos =
                currentTime - timestamps.lastChannelWriteAttemptNanoTime();

        String writeHangMessage = StringUtils.EMPTY;

        if (writeDelayInNanos > this.writeDelayLimitInNanos && writeHangDurationInNanos > writeHangGracePeriodInNanos) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            writeHangMessage = MessageFormat.format(
                    "{0} health check failed due to non-responding write: [lastChannelWriteAttemptNanoTime: {1}, " +
                            "lastChannelWriteNanoTime: {2}, writeDelayInNanos: {3}, writeDelayLimitInNanos: {4}, " +
                            "rntbdContext: {5}, pendingRequestCount: {6}]",
                    channel,
                    timestamps.lastChannelWriteAttemptNanoTime(),
                    timestamps.lastChannelWriteNanoTime(),
                    writeDelayInNanos,
                    this.writeDelayLimitInNanos,
                    rntbdContext,
                    pendingRequestCount);

            logger.warn(writeHangMessage);
        }

        return writeHangMessage;
    }

    private String isReadHang(Timestamps timestamps, long currentTime, RntbdRequestManager requestManager, Channel channel) {
        // Treat the connection as unhealthy if the gap between the last successful write and the last successful read
        // grew beyond acceptable limits, unless a write succeeded recently or transitTimeout is below threshold. This is a sign of a non-responding read.

        final long readDelay = timestamps.lastChannelWriteNanoTime() - timestamps.lastChannelReadNanoTime();
        final long readHangDuration = currentTime - timestamps.lastChannelWriteNanoTime();

        String readHangMessage = StringUtils.EMPTY;

        if (readDelay > this.readDelayLimitInNanos &&
                (readHangDuration > readHangGracePeriodInNanos || timestamps.transitTimeoutCount() >= this.transitTimeoutDetectionThreshold)) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            readHangMessage = MessageFormat.format(
                    "{0} health check failed due to non-responding read: [lastChannelWrite: {1}, lastChannelRead: {2}, "
                            + "readDelay: {3}, readDelayLimit: {4}, rntbdContext: {5}, pendingRequestCount: {6}, transitTimeoutCount: {7}]",
                    channel,
                    timestamps.lastChannelWriteNanoTime(),
                    timestamps.lastChannelReadNanoTime(),
                    readDelay,
                    this.readDelayLimitInNanos,
                    rntbdContext,
                    pendingRequestCount,
                    timestamps.transitTimeoutCount());


            logger.warn(readHangMessage);
        }

        return readHangMessage;
    }

    private String idleConnectionValidation(Timestamps timestamps, long currentTime, Channel channel) {
        String errorMessage = StringUtils.EMPTY;

        if (this.idleConnectionTimeoutInNanos > 0L) {
            if (currentTime - timestamps.lastChannelReadNanoTime() > this.idleConnectionTimeoutInNanos) {
                errorMessage = MessageFormat.format(
                        "{0} health check failed due to idle connection timeout: [lastChannelWrite: {1}, lastChannelRead: {2}, "
                                + "idleConnectionTimeout: {3}, currentTime: {4}]",
                        channel,
                        timestamps.lastChannelWriteNanoTime(),
                        timestamps.lastChannelReadNanoTime(),
                        idleConnectionTimeoutInNanos,
                        currentTime);

                logger.warn(errorMessage);
            }
        }

        return errorMessage;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Types

    public static final class Timestamps {

        private static final AtomicLongFieldUpdater<Timestamps> lastPingUpdater =
            newUpdater(Timestamps.class, "lastPingNanoTime");

        private static final AtomicLongFieldUpdater<Timestamps> lastReadUpdater =
            newUpdater(Timestamps.class, "lastReadNanoTime");

        private static final AtomicLongFieldUpdater<Timestamps> lastWriteUpdater =
            newUpdater(Timestamps.class, "lastWriteNanoTime");

        private static final AtomicLongFieldUpdater<Timestamps> lastWriteAttemptUpdater =
            newUpdater(Timestamps.class, "lastWriteAttemptNanoTime");

        private static final AtomicLongFieldUpdater<Timestamps> transitTimeoutCountUpdater =
            newUpdater(Timestamps.class, "transitTimeoutCount");

        private volatile long lastPingNanoTime;
        private volatile long lastReadNanoTime;
        private volatile long lastWriteNanoTime;
        private volatile long lastWriteAttemptNanoTime;
        private volatile long transitTimeoutCount;

        public Timestamps() {
        }

        @SuppressWarnings("CopyConstructorMissesField")
        public Timestamps(Timestamps other) {
            checkNotNull(other, "other: null");
            this.lastPingNanoTime = lastPingUpdater.get(other);
            this.lastReadNanoTime = lastReadUpdater.get(other);
            this.lastWriteNanoTime = lastWriteUpdater.get(other);
            this.lastWriteAttemptNanoTime = lastWriteAttemptUpdater.get(other);
            this.transitTimeoutCount = transitTimeoutCountUpdater.get(other);
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

        public void transitTimeout() {
            transitTimeoutCountUpdater.incrementAndGet(this);
        }
        public void resetTransitTimeout() {
            transitTimeoutCountUpdater.set(this, 0);
        }

        @JsonProperty
        public long lastChannelPingNanoTime() {
            return lastPingUpdater.get(this);
        }

        @JsonProperty
        public long lastChannelReadNanoTime() {
            return lastReadUpdater.get(this);
        }

        @JsonProperty
        public long lastChannelWriteNanoTime() {
            return lastWriteUpdater.get(this);
        }

        @JsonProperty
        public long lastChannelWriteAttemptNanoTime() {
            return lastWriteAttemptUpdater.get(this);
        }

        @JsonProperty
        public long transitTimeoutCount() {
            return transitTimeoutCountUpdater.get(this);
        }

        @Override
        public String toString() {
            return RntbdObjectMapper.toString(this);
        }
    }

    // endregion
}
