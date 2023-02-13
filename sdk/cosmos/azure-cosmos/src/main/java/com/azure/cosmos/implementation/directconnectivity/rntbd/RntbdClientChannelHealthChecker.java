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
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdHealthCheckResults;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater;

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
    private final long networkRequestTimeoutInNanos;
    @JsonProperty
    private final boolean timeoutDetectionEnabled;
    @JsonProperty
    private final long timeoutTimeLimitInNanos;
    @JsonProperty
    private final int timeoutHighFrequencyThreshold;
    @JsonProperty
    private final long timeoutHighFrequencyTimeLimitInNanos;
    @JsonProperty
    private final int timeoutOnWriteThreshold;
    @JsonProperty
    private final long timeoutOnWriteTimeLimitInNanos;


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
        this.networkRequestTimeoutInNanos = config.tcpNetworkRequestTimeoutInNanos();
        this.timeoutDetectionEnabled = config.timeoutDetectionEnabled();
        this.timeoutTimeLimitInNanos = config.timeoutDetectionTimeLimitInNanos();
        this.timeoutHighFrequencyThreshold = config.timeoutDetectionHighFrequencyThreshold();
        this.timeoutHighFrequencyTimeLimitInNanos = config.timeoutDetectionHighFrequencyTimeLimitInNanos();
        this.timeoutOnWriteThreshold = config.timeoutDetectionOnWriteThreshold();
        this.timeoutOnWriteTimeLimitInNanos = config.timeoutDetectionOnWriteTimeLimitInNanos();
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

        final Promise<Boolean> promise = channel.eventLoop().newPromise();

        this.isHealthyWithFailureReason(channel)
            .addListener((Future<String> future) -> {
                if (future.isSuccess()) {
                    if (RntbdHealthCheckResults.SuccessValue.equals(future.get())) {
                        promise.setSuccess(Boolean.TRUE);
                    } else {
                        promise.setSuccess(Boolean.FALSE);
                    }
                } else {
                    promise.setFailure(future.cause());
                }
            });

        return promise;
    }

    /**
     * Determines whether a specified channel is healthy.
     *
     * @param channel A channel whose health is to be checked.
     * @return A future with a result reason {@link RntbdHealthCheckResults} if the channel is healthy, otherwise return the failed reason.
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
        final Instant currentTime = Instant.now();

        if (Duration.between(timestamps.lastChannelReadTime(), currentTime).toNanos() < recentReadWindowInNanos) {
            // because we recently received data
            return promise.setSuccess(RntbdHealthCheckResults.SuccessValue);
        }

        String writeIsHangMessage = this.isWriteHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(writeIsHangMessage)) {
            return promise.setSuccess(writeIsHangMessage);
        }

        String readIsHangMessage = this.isReadHang(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(readIsHangMessage)) {
            return promise.setSuccess(readIsHangMessage);
        }

        String transitTimeoutValidationMessage = this.transitTimeoutValidation(timestamps, currentTime, requestManager, channel);
        if (StringUtils.isNotEmpty(transitTimeoutValidationMessage)) {
            return promise.setSuccess(transitTimeoutValidationMessage);
        }

        String idleConnectionValidationMessage = this.idleConnectionValidation(timestamps, currentTime, channel);
        if(StringUtils.isNotEmpty(idleConnectionValidationMessage)) {
            return promise.setSuccess(idleConnectionValidationMessage);
        }

        channel.writeAndFlush(RntbdHealthCheckRequest.MESSAGE).addListener(completed -> {
            if (completed.isSuccess()) {
                promise.setSuccess(RntbdHealthCheckResults.SuccessValue);
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

    private String isWriteHang(Timestamps timestamps, Instant currentTime, RntbdRequestManager requestManager, Channel channel) {
        // Treat the channel as unhealthy if the gap between the last attempted to write and the last successful write
        // grew beyond acceptable limits, unless a write was attempted recently. This is a sign of a non-responding write.

        final long writeDelayInNanos =
                Duration.between(timestamps.lastChannelWriteTime(), timestamps.lastChannelWriteAttemptTime()).toNanos();

        final long writeHangDurationInNanos =
                Duration.between(timestamps.lastChannelWriteAttemptTime(), currentTime).toNanos();

        String writeHangMessage = StringUtils.EMPTY;

        if (writeDelayInNanos > this.writeDelayLimitInNanos && writeHangDurationInNanos > writeHangGracePeriodInNanos) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            writeHangMessage = MessageFormat.format(
                    "{0} health check failed due to non-responding write: [lastChannelWriteAttemptTime: {1}, " +
                            "lastChannelWriteTime: {2}, writeDelayInNanos: {3}, writeDelayLimitInNanos: {4}, " +
                            "rntbdContext: {5}, pendingRequestCount: {6}]",
                    channel,
                    timestamps.lastChannelWriteAttemptTime(),
                    timestamps.lastChannelWriteTime(),
                    writeDelayInNanos,
                    this.writeDelayLimitInNanos,
                    rntbdContext,
                    pendingRequestCount);

            logger.warn(writeHangMessage);
        }

        return writeHangMessage;
    }

    private String isReadHang(Timestamps timestamps, Instant currentTime, RntbdRequestManager requestManager, Channel channel) {
        // Treat the connection as unhealthy if the gap between the last successful write and the last successful read
        // grew beyond acceptable limits, unless a write succeeded recently or transitTimeout is below threshold.
        // This is a sign of a non-responding read.

        final long readDelay = Duration.between(timestamps.lastChannelReadTime(), timestamps.lastChannelWriteTime()).toNanos();
        final long readHangDuration = Duration.between(timestamps.lastChannelWriteTime(), currentTime).toNanos();

        String readHangMessage = StringUtils.EMPTY;

        if (readDelay > this.readDelayLimitInNanos && readHangDuration > readHangGracePeriodInNanos) {

            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();
            final int pendingRequestCount = requestManager.pendingRequestCount();

            readHangMessage = MessageFormat.format(
                    "{0} health check failed due to non-responding read: [lastChannelWrite: {1}, lastChannelRead: {2}, "
                            + "readDelay: {3}, readDelayLimit: {4}, rntbdContext: {5}, pendingRequestCount: {6}]",
                    channel,
                    timestamps.lastChannelWriteTime(),
                    timestamps.lastChannelReadTime(),
                    readDelay,
                    this.readDelayLimitInNanos,
                    rntbdContext,
                    pendingRequestCount);


            logger.warn(readHangMessage);
        }

        return readHangMessage;
    }

    private String transitTimeoutValidation(Timestamps timestamps, Instant currentTime, RntbdRequestManager requestManager, Channel channel) {
        String transitTimeoutValidationMessage = StringUtils.EMPTY;

        if (this.timeoutDetectionEnabled && timestamps.tansitTimeoutCount() > 0) {
            final Optional<RntbdContext> rntbdContext = requestManager.rntbdContext();

            // The channel will be closed if all requests are failed due to transit timeout within the time limit.
            // This helps to close channel faster for sparse workload.
            long readDelay = Duration.between(timestamps.lastChannelReadTime(), currentTime).toNanos();
            if (readDelay >= this.timeoutTimeLimitInNanos) {
                transitTimeoutValidationMessage = MessageFormat.format(
                    "{0} health check failed due to transit timeout detection time limit: [rntbdContext: {1},"
                        + "lastChannelRead: {2}, timeoutTimeLimitInNanos: {3}]",
                    channel,
                    rntbdContext,
                    timestamps.lastReadTime,
                    this.timeoutTimeLimitInNanos);

                logger.warn(transitTimeoutValidationMessage);
                return transitTimeoutValidationMessage;
            }

            // Consecutive connection timeout happens
            // Max(TransitTimeoutDetectionThreshold * networkRequestTimeout, transitTimeoutHighFrequencyGracePeriodInNanos)
            if (timestamps.tansitTimeoutCount() >= this.timeoutHighFrequencyThreshold
                && readDelay >= this.timeoutHighFrequencyTimeLimitInNanos) {
                transitTimeoutValidationMessage = MessageFormat.format(
                    "{0} health check failed due to transit timeout high frequency threshold hit: [rntbdContext: {1},"
                        + "lastChannelRead: {2}, transitTimeoutCount: {3}, timeoutHighFrequencyThreshold: {4}, timeoutHighFrequencyTimeLimitInNanos: {5}]",
                    channel,
                    rntbdContext,
                    timestamps.lastReadTime,
                    timestamps.transitTimeoutCount,
                    this.timeoutHighFrequencyThreshold,
                    this.timeoutHighFrequencyTimeLimitInNanos);

                logger.warn(transitTimeoutValidationMessage);
                return transitTimeoutValidationMessage;
            }

            // timeout happens on write operations
            // For write operation, SDK can only send request to primary replica, so the detection here will be more aggressive
            if (timestamps.tansitTimeoutWriteCount() >= this.timeoutOnWriteThreshold
                && readDelay >= this.timeoutOnWriteTimeLimitInNanos) {
                transitTimeoutValidationMessage = MessageFormat.format(
                    "{0} health check failed due to transit timeout on write threshold hit: [rntbdContext: {1},"
                        + "lastChannelRead: {2}, transitTimeoutWriteCount: {3}, timeoutOnWriteThreshold: {4}, timeoutOnWriteTimeLimitInNanos: {5}]",
                    channel,
                    rntbdContext,
                    timestamps.lastReadTime,
                    timestamps.transitTimeoutWriteCount,
                    this.timeoutOnWriteThreshold,
                    this.timeoutOnWriteTimeLimitInNanos);

                logger.warn(transitTimeoutValidationMessage);
                return transitTimeoutValidationMessage;
            }

            //  case 4: TO Be implemented - Can we defer the health status based on the RntbdServiceEndpointStats?
        }

        return transitTimeoutValidationMessage;
    }

    private String idleConnectionValidation(Timestamps timestamps, Instant currentTime, Channel channel) {
        String errorMessage = StringUtils.EMPTY;

        if (this.idleConnectionTimeoutInNanos > 0L) {
            if (Duration.between(currentTime, timestamps.lastChannelReadTime()).toNanos() > this.idleConnectionTimeoutInNanos) {
                errorMessage = MessageFormat.format(
                        "{0} health check failed due to idle connection timeout: [lastChannelWrite: {1}, lastChannelRead: {2}, "
                                + "idleConnectionTimeout: {3}, currentTime: {4}]",
                        channel,
                        timestamps.lastChannelWriteTime(),
                        timestamps.lastChannelReadTime(),
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

        private static final AtomicReferenceFieldUpdater<Timestamps, Instant> lastPingUpdater =
            newUpdater(Timestamps.class, Instant.class, "lastPingTime");

        private static final AtomicReferenceFieldUpdater<Timestamps, Instant>lastReadUpdater =
            newUpdater(Timestamps.class, Instant.class, "lastReadTime");

        private static final AtomicReferenceFieldUpdater<Timestamps, Instant> lastWriteUpdater =
            newUpdater(Timestamps.class, Instant.class, "lastWriteTime");

        private static final AtomicReferenceFieldUpdater<Timestamps, Instant> lastWriteAttemptUpdater =
            newUpdater(Timestamps.class, Instant.class, "lastWriteAttemptTime");

        private static final AtomicIntegerFieldUpdater<Timestamps> transitTimeoutCountUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Timestamps.class, "transitTimeoutCount");

        private static final AtomicIntegerFieldUpdater<Timestamps> transitTimeoutWriteCountUpdater =
            AtomicIntegerFieldUpdater.newUpdater(Timestamps.class, "transitTimeoutWriteCount");

        private static final AtomicReferenceFieldUpdater<Timestamps, Instant> transitTimeoutStartingTimeUpdater =
            newUpdater(Timestamps.class, Instant.class, "transitTimeoutStartingTime");

        private volatile Instant lastPingTime;
        private volatile Instant lastReadTime;
        private volatile Instant lastWriteTime;
        private volatile Instant lastWriteAttemptTime;
        private volatile int transitTimeoutCount;
        private volatile int transitTimeoutWriteCount;
        private volatile Instant transitTimeoutStartingTime;

        public Timestamps() {
            lastPingUpdater.set(this, Instant.now());
            lastReadUpdater.set(this, Instant.now());
            lastWriteUpdater.set(this, Instant.now());
            lastWriteAttemptUpdater.set(this, Instant.now());
        }

        @SuppressWarnings("CopyConstructorMissesField")
        public Timestamps(Timestamps other) {
            checkNotNull(other, "other: null");
            this.lastPingTime = lastPingUpdater.get(other);
            this.lastReadTime = lastReadUpdater.get(other);
            this.lastWriteTime = lastWriteUpdater.get(other);
            this.lastWriteAttemptTime = lastWriteAttemptUpdater.get(other);
            this.transitTimeoutCount = transitTimeoutCountUpdater.get(other);
            this.transitTimeoutWriteCount = transitTimeoutWriteCountUpdater.get(other);
            this.transitTimeoutStartingTime = transitTimeoutStartingTimeUpdater.get(other);
        }

        public void channelPingCompleted() {
            lastPingUpdater.set(this, Instant.now());
        }

        public void channelReadCompleted() {
            lastReadUpdater.set(this, Instant.now());
        }

        public void channelWriteAttempted() {
            lastWriteAttemptUpdater.set(this, Instant.now());
        }

        public void channelWriteCompleted() {
            lastWriteUpdater.set(this, Instant.now());
        }

        public void transitTimeout(boolean isReadOnly, Instant requestCreatedTime) {
            if (transitTimeoutCountUpdater.incrementAndGet(this) == 1) {
                transitTimeoutStartingTimeUpdater.set(this, requestCreatedTime);
            }
            if (!isReadOnly) {
                transitTimeoutWriteCountUpdater.incrementAndGet(this);
            }
        }

        public void resetTransitTimeout() {
            transitTimeoutCountUpdater.set(this, 0);
            transitTimeoutWriteCountUpdater.set(this, 0);
            transitTimeoutStartingTimeUpdater.set(this, null);
        }

        @JsonProperty
        public Instant lastChannelPingTime() {
            return lastPingUpdater.get(this);
        }

        @JsonProperty
        public Instant lastChannelReadTime() {
            return lastReadUpdater.get(this);
        }

        @JsonProperty
        public Instant lastChannelWriteTime() {
            return lastWriteUpdater.get(this);
        }

        @JsonProperty
        public Instant lastChannelWriteAttemptTime() {
            return lastWriteAttemptUpdater.get(this);
        }

        @JsonProperty
        public int tansitTimeoutCount() {
            return transitTimeoutCountUpdater.get(this);
        }

        @JsonProperty
        public int tansitTimeoutWriteCount() {
            return transitTimeoutWriteCountUpdater.get(this);
        }

        @JsonProperty
        public Instant transitTimeoutStartingTime() {
            return transitTimeoutStartingTimeUpdater.get(this);
        }

        @Override
        public String toString() {
            return RntbdObjectMapper.toString(this);
        }
    }

    // endregion
}
