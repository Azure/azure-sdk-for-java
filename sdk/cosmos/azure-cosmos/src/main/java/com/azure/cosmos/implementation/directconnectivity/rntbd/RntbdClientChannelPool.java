// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdEndpoint.Config;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.util.AttributeKey;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.time.Instant;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

/**
 * A {@link ChannelPool} implementation that enforces a maximum number of concurrent direct TCP Cosmos connections.
 */
@JsonSerialize(using = RntbdClientChannelPool.JsonSerializer.class)
public final class RntbdClientChannelPool implements ChannelPool {

    private static final TimeoutException ACQUISITION_TIMEOUT = ThrowableUtil.unknownStackTrace(
        new TimeoutException("acquisition took longer than the configured maximum time"),
        RntbdClientChannelPool.class, "<init>");

    private static final ClosedChannelException CHANNEL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new ClosedChannelException(), RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("service endpoint was closed"),
        RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_RELEASE = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("service endpoint was closed"),
        RntbdClientChannelPool.class, "release");

    private static final AttributeKey<RntbdClientChannelPool> POOL_KEY = AttributeKey.newInstance(
        RntbdClientChannelPool.class.getName());

    private static final IllegalStateException TOO_MANY_PENDING_ACQUISITIONS = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("too many outstanding acquire operations"),
        RntbdClientChannelPool.class, "acquire");

    private static final EventExecutor closer = new DefaultEventExecutor(new RntbdThreadFactory(
        "channel-pool-closer",
        true,
        Thread.NORM_PRIORITY));

    private static final HashedWheelTimer acquisitionAndIdleEndpointDetectionTimer =
        new HashedWheelTimer(new RntbdThreadFactory(
            "channel-acquisition-timer",
            true,
            Thread.NORM_PRIORITY));

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelPool.class);

    private final long acquisitionTimeoutInNanos;
    private final Runnable acquisitionTimeoutTask;
    private final PooledByteBufAllocatorMetric allocatorMetric;
    private final Bootstrap bootstrap;
    private final EventExecutor executor;
    private final ChannelHealthChecker healthChecker;
    // private final ScheduledFuture<?> idleStateDetectionScheduledFuture;
    private final int maxChannels;
    private final int maxPendingAcquisitions;
    private final int maxRequestsPerChannel;
    private final ChannelPoolHandler poolHandler;
    private final boolean releaseHealthCheck;

    // Because state from these fields can be requested on any thread...

    private final AtomicReference<Timeout> acquisitionAndIdleEndpointDetectionTimeout = new AtomicReference<>();
    private final ConcurrentHashMap<Channel, Channel> acquiredChannels = new ConcurrentHashMap<>();
    private final Deque<Channel> availableChannels = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean connecting = new AtomicBoolean();
    private final Queue<AcquireTask> pendingAcquisitions = new ConcurrentLinkedQueue<>();

    /**
     * Initializes a newly created {@link RntbdClientChannelPool} instance.
     *
     * @param bootstrap the {@link Bootstrap} that is used for connections.
     * @param config the {@link Config} that is used for the channel pool instance created.
     */
    RntbdClientChannelPool(final RntbdServiceEndpoint endpoint, final Bootstrap bootstrap, final Config config) {
        this(endpoint, bootstrap, config, new RntbdClientChannelHealthChecker(config));
    }

    private RntbdClientChannelPool(
        final RntbdServiceEndpoint endpoint,
        final Bootstrap bootstrap,
        final Config config,
        final RntbdClientChannelHealthChecker healthChecker) {

        checkNotNull(endpoint, "expected non-null endpoint");
        checkNotNull(bootstrap, "expected non-null bootstrap");
        checkNotNull(config, "expected non-null config");
        checkNotNull(healthChecker, "expected non-null healthChecker");

        this.poolHandler = new RntbdClientChannelHandler(config, healthChecker);
        this.executor = bootstrap.config().group().next();
        this.healthChecker = healthChecker;

        this.bootstrap = bootstrap.clone().handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(final Channel channel) throws Exception {
            checkState(channel.eventLoop().inEventLoop());
            RntbdClientChannelPool.this.poolHandler.channelCreated(channel);
            }
        });

        // TODO (DANOBLE) Consider moving or removing this.allocatorMetric
        //  This metric is redundant in the scope of this class and should be pulled up to RntbdServiceEndpoint or
        //  entirely removed.

        this.acquisitionTimeoutInNanos = config.connectionAcquisitionTimeoutInNanos();
        this.allocatorMetric = config.allocator().metric();
        this.maxChannels = config.maxChannelsPerEndpoint();
        this.maxRequestsPerChannel = config.maxRequestsPerChannel();

        this.maxPendingAcquisitions = Integer.MAX_VALUE;
        this.releaseHealthCheck = true;

        this.acquisitionTimeoutTask = acquisitionTimeoutInNanos <= 0 ? null : new AcquireTimeoutTask(this) {
            /**
             * Fails a request due to a channel acquisition timeout.
             *
             * @param task a {@link AcquireTask channel acquisition task} that has timed out.
             */
            @Override
            public void onTimeout(AcquireTask task) {
                task.promise.setFailure(ACQUISITION_TIMEOUT);
            }
        };

        newTimeout(endpoint, config.idleEndpointTimeoutInNanos(), config.requestTimerResolutionInNanos());

//        this.idleStateDetectionScheduledFuture = this.executor.scheduleAtFixedRate(
//            () -> {
//                final long elapsedTimeInNanos = System.nanoTime() - endpoint.lastRequestNanoTime();
//
//                if (idleEndpointTimeoutInNanos - elapsedTimeInNanos <= 0) {
//                    if (logger.isDebugEnabled()) {
//                        logger.debug(
//                            "{} closing endpoint due to inactivity (elapsedTime: {} > idleEndpointTimeout: {})",
//                            endpoint,
//                            Duration.ofNanos(elapsedTimeInNanos),
//                            Duration.ofNanos(idleEndpointTimeoutInNanos));
//                    }
//                    endpoint.close();
//                    return;
//                }
//
//                this.runTasksInPendingAcquisitionQueue();
//
//            }, requestTimerResolutionInNanos, requestTimerResolutionInNanos, TimeUnit.NANOSECONDS);
    }

    // region Accessors

    /**
     * Gets the current channel count.
     * <p>
     * The value returned is consistent, if called from the {@link RntbdClientChannelPool pool}'s thread
     * {@link #executor}. It is an approximation that may be inconsistent depending on the pattern of {@link #acquire}
     * and {@link #release} operations, if called from any other thread.
     *
     * @return the current channel count.
     */
    public int channels() {
        return this.acquiredChannels.size() + this.availableChannels.size() + (this.connecting.get() ? 1 : 0);
    }

    /**
     * Gets the current acquired channel count.
     *
     * @return the current acquired channel count.
     */
    public int channelsAcquired() {
        return this.acquiredChannels.size();
    }

    /**
     * Gets the current available channel count.
     *
     * @return the current available channel count.
     */
    public int channelsAvailable() {
        return this.availableChannels.size();
    }

    /**
     * {@code true} if this {@link RntbdClientChannelPool pool} is closed or {@code false} otherwise.
     *
     * @return {@code true} if this {@link RntbdClientChannelPool pool} is closed or {@code false} otherwise.
     */
    public boolean isClosed() {
        return this.closed.get();
    }

    /**
     * Gets the maximum number of channels that will be allocated to this {@link RntbdClientChannelPool pool}.
     * <p>
     * No more than {@code maxChannels} channels will ever be pooled.
     *
     * @return the maximum number of channels that will be allocated to this {@link RntbdClientChannelPool pool}.
     */
    public int maxChannels() {
        return this.maxChannels;
    }

    /**
     * Gets the maximum number of requests that will be left pending on any {@link Channel channel} in the {@link
     * RntbdClientChannelPool pool}.
     * <p>
     * Healthy channels with fewer than {@code maxRequestsPerChannel} requests pending are considered to be serviceable
     * channels. Unhealthy channels are regularly removed from the pool and--depending on load--may be replaced with new
     * channels later.
     *
     * @return the maximum number of channels that will be allocated to this {@link RntbdClientChannelPool pool}.
     */
    public int maxRequestsPerChannel() {
        return this.maxRequestsPerChannel;
    }

    /**
     * Gets the {@link SocketAddress address} of the replica served by this {@link RntbdClientChannelPool pool}.
     *
     * @return the {@link SocketAddress address} of the replica served by this {@link RntbdClientChannelPool pool}.
     */
    public SocketAddress remoteAddress() {
        return this.bootstrap.config().remoteAddress();
    }

    /**
     * Gets the number of pending (asynchronous) channel acquisitions.
     * <p>
     * Pending acquisitions map to requests that have not yet been dispatched to a channel. Such requests are said to be
     * queued. Hence the count of the the number of pending channel acquisitions is called the {@code
     * requestQueueLength}.
     *
     * @return the number of pending channel acquisitions.
     */
    public int requestQueueLength() {
        return this.pendingAcquisitions.size();
    }

    public long usedDirectMemory() {
        return this.allocatorMetric.usedDirectMemory();
    }

    public long usedHeapMemory() {
        return this.allocatorMetric.usedHeapMemory();
    }

    // endregion

    // region Methods

    /**
     * Acquire a {@link Channel channel} from the current {@link RntbdClientChannelPool pool}.
     * <p>
     * TODO (DANOBLE) Javadoc for {@link #acquire}.
     *
     * @return a {@link Promise promise} to be notified when the operation completes. If the operation fails, {@code
     * channel} will be closed automatically.
     *
     * <p><strong>
     * It is important to {@link #release} every {@link Channel channel} acquired from the pool, even when the {@link
     * Channel channel} is closed explicitly.</strong>
     */
    @Override
    public Future<Channel> acquire() {
        return this.acquire(this.bootstrap.config().group().next().newPromise());
    }

    /**
     * Acquire a {@link Channel channel} from the current {@link RntbdClientChannelPool pool}.
     * <p>
     * TODO (DANOBLE) Javadoc for {@link #acquire}.
     *
     * @param promise a {@link Promise promise} to be notified when the operation completes.
     *
     * @return a reference to {@code promise}. If the operation fails, {@code channel} will be closed automatically.
     *
     * <p><strong>
     * It is important to {@link #release} every {@link Channel channel} acquired from the pool, even when the {@link
     * Channel channel} is closed explicitly.</strong>
     *
     * @throws IllegalStateException if this {@link RntbdClientChannelPool} is closed.
     */
    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {

        this.throwIfClosed();

        try {
            if (this.executor.inEventLoop()) {
                this.acquireChannel(promise);
            } else {
                this.executor.execute(() -> this.acquireChannel(promise)); // fire and forget
            }
        } catch (Throwable cause) {
            promise.setFailure(cause);
        }

        return promise;
    }

    @Override
    public void close() {
        if (this.closed.compareAndSet(false, true)) {
            if (this.executor.inEventLoop()) {
                this.doClose();
            } else {
                this.executor.submit(this::doClose).awaitUninterruptibly(); // block until complete
            }
        }
    }

    /**
     * Releases a {@link Channel channel} back to the current {@link RntbdClientChannelPool pool}.
     * <p>
     * TODO (DANOBLE) Javadoc for {@link RntbdClientChannelPool#release}.
     *
     * @param channel a {@link Channel channel} to release back to the  {@link RntbdClientChannelPool channel pool}.
     *
     * @return asynchronous result of the operation. If the operation fails, {@code channel} will be closed
     * automatically.
     */
    @Override
    public Future<Void> release(final Channel channel) {
        return this.release(channel, channel.eventLoop().newPromise());
    }

    /**
     * Releases a {@link Channel channel} back to the current {@link RntbdClientChannelPool pool}.
     * <p>
     * TODO (DANOBLE) Javadoc for {@link RntbdClientChannelPool#release}.
     *
     * @param channel a {@link Channel channel} to release back to the  {@link RntbdClientChannelPool channel pool}.
     * @param promise a {@link Promise promise} to be notified once the release is successful; failed otherwise.
     *
     * @return a reference to {@code promise}. If the operation fails, {@code channel} will be closed automatically.
     */
    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {

        // We do not call this.throwIfClosed because a channel may be released back to the pool during close

        checkNotNull(channel, "expected non-null channel");
        checkNotNull(promise, "expected non-null promise");

        Promise<Void> anotherPromise = this.executor.newPromise(); // ensures we finish in our executor's event loop

        try {
            final EventLoop loop = channel.eventLoop();
            if (loop.inEventLoop()) {
                this.releaseChannel(channel, anotherPromise);
            } else {
                loop.execute(() -> this.releaseChannel(channel, anotherPromise));
            }
        } catch (Throwable cause) {
            if (this.executor.inEventLoop()) {
                this.closeChannelAndFail(channel, cause, anotherPromise);
            } else {
                this.executor.submit(() -> this.closeChannelAndFail(channel, cause, anotherPromise));
            }
        }

        anotherPromise.addListener((FutureListener<Void>) future -> {

            this.ensureInEventLoop();

            if (this.isClosed()) {
                // We have no choice but to close the channel
                promise.setFailure(POOL_CLOSED_ON_RELEASE);
                this.closeChannel(channel);
                return;
            }

            if (future.isSuccess()) {
                this.runTasksInPendingAcquisitionQueue();
                promise.setSuccess(null);
            } else {
                // TODO (DANOBLE) Is this check for an IllegalArgumentException required?
                //  Explain here, if so; otherwise remove the check.
                final Throwable cause = future.cause();
                if (!(cause instanceof IllegalArgumentException)) {
                    this.runTasksInPendingAcquisitionQueue();
                }
                promise.setFailure(cause);
            }
        });

        return promise;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    // endregion

    // region Privates

    /**
     * Acquires a serviceable channel from the {@link RntbdClientChannelPool pool}.
     * <p>
     * This method acquires the first channel that's both available and serviceable in LIFO order. A new channel is
     * created and added to the pool if and only if:
     * <ul>
     * <li>fewer than {@link #maxChannels} channels have been created ({@link #channels} < {@link #maxChannels()}))
     * and</li>
     * <li>there are no acquired channels pending release ({@link #channelsAcquired} == 0).</li>
     * </ul>
     * Under load it is possible that:
     * <ul>
     * <li>no available channels are serviceable ({@link RntbdRequestManager#pendingRequestCount()} ==
     * {@link #maxChannels()})</li>
     * <li>there are acquired channels pending release, and</li>
     * <li>{@link #maxChannels} channels have been created.</li>
     * </ul>
     * Under these circumstances a request to acquire a channel will be satisfied by the
     * {@link #acquisitionTimeoutTask} which will:
     * <ul>
     * <li>process items in the {@link #pendingAcquisitions} on each call to {@link #acquire} or {@link #release},
     * and</li>
     * <li>each {@link #acquisitionTimeoutInNanos} nanoseconds
     * </ul>
     * until a channel is acquired.
     *
     * @param promise the promise of a {@link Channel channel}.
     *
     * @see #isChannelServiceable(Channel)
     * @see AcquireTimeoutTask
     */
    private void acquireChannel(final Promise<Channel> promise) {

        this.ensureInEventLoop();

        if (this.isClosed()) {
            promise.setFailure(POOL_CLOSED_ON_ACQUIRE);
            return;
        }

        try {
            Channel candidate = this.pollChannel();

            if (candidate != null) {

                // Fulfill this request with our candidate, assuming it's healthy
                // If our candidate is unhealthy, notifyChannelHealthCheck will call us again

                doAcquireChannel(promise, candidate);
                return;
            }

            final int channelCount = this.channels();

            if (channelCount < this.maxChannels) {

                if (this.connecting.compareAndSet(false, true)) {

                    // Fulfill this request with a new channel, assuming we can connect one
                    // If our connection attempt fails, notifyChannelConnect will call us again

                    final Promise<Channel> anotherPromise = this.newChannelPromise(promise);
                    final ChannelFuture future = this.bootstrap.clone().attr(POOL_KEY, this).connect();

                    if (future.isDone()) {
                        this.notifyChannelConnect(future, anotherPromise);
                    } else {
                        future.addListener(ignored -> this.notifyChannelConnect(future, anotherPromise));
                    }

                    return;
                }

            } else if (this.computeLoadFactor() > 0.90D) {

                // All channels are swamped and we'll pick the one with the lowest pending request count

                long pendingRequestCountMin = Long.MAX_VALUE;

                for (Channel channel : this.availableChannels) {

                    final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);
                    final long pendingRequestCount = manager.pendingRequestCount();

                    if (pendingRequestCount < pendingRequestCountMin) {
                        pendingRequestCountMin = pendingRequestCount;
                        candidate = channel;
                    }
                }

                assert candidate != null;

                this.availableChannels.remove(candidate);
                doAcquireChannel(promise, candidate);

                return;
            }

            this.addTaskToPendingAcquisitionQueue(promise);

        } catch (Throwable cause) {
            promise.tryFailure(cause);
        }
    }

    /**
     * Add a task to the pending acquisition queue to fulfill the request for a {@link Channel channel} later.
     * <p>
     * Tasks in the pending acquisition queue are run whenever a channel is released. This ensures that pending requests
     * for channels are fulfilled as soon as possible.
     *
     * @param promise a {@link Promise promise} that will be completed when a {@link Channel channel} is acquired or an
     * error is encountered.
     *
     * @see #runTasksInPendingAcquisitionQueue
     */
    private void addTaskToPendingAcquisitionQueue(Promise<Channel> promise) {

        this.ensureInEventLoop();

        if (logger.isDebugEnabled()) {
            logger.debug("{}, {}, {}, {}, {}, {}",
                Instant.now(),
                this.remoteAddress(),
                this.channels(),
                this.channelsAcquired(),
                this.channelsAvailable(),
                this.requestQueueLength());
        }

        if (this.pendingAcquisitions.size() >= this.maxPendingAcquisitions) {

            promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);

        } else {

            final AcquireTask acquireTask = new AcquireTask(this, promise);

            if (this.pendingAcquisitions.offer(acquireTask)) {
                if (this.acquisitionTimeoutTask != null) {
                    acquireTask.timeoutFuture = this.executor.schedule(
                        this.acquisitionTimeoutTask,
                        this.acquisitionTimeoutInNanos,
                        TimeUnit.NANOSECONDS);
                }
            } else {
                promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);
            }
        }
    }

    /**
     * Closes a {@link Channel channel} and removes it from the {@link RntbdClientChannelPool pool}.
     *
     * @param channel the {@link Channel channel} to close and remove from the {@link RntbdClientChannelPool pool}.
     */
    private void closeChannel(final Channel channel) {

        this.ensureInEventLoop();

        this.acquiredChannels.remove(channel);
        channel.attr(POOL_KEY).set(null);
        channel.close();
    }

    private void closeChannelAndFail(final Channel channel, final Throwable cause, final Promise<?> promise) {
        this.ensureInEventLoop();
        this.closeChannel(channel);
        promise.tryFailure(cause);
    }

    private double computeLoadFactor() {

        long pendingRequestCountMin = Long.MAX_VALUE;
        long pendingRequestCountTotal = 0L;
        long channelCount = 0;

        for (Channel channel : this.availableChannels) {

            final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);
            final long pendingRequestCount = manager.pendingRequestCount();

            if (pendingRequestCount < pendingRequestCountMin) {
                pendingRequestCountMin = pendingRequestCount;
            }

            pendingRequestCountTotal += pendingRequestCount;
            channelCount++;
        }

        for (Channel channel : this.acquiredChannels.values()) {

            final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);

            if (manager != null) {

                final long pendingRequestCount = manager.pendingRequestCount();

                if (pendingRequestCount < pendingRequestCountMin) {
                    pendingRequestCountMin = pendingRequestCount;
                }

                pendingRequestCountTotal += pendingRequestCount;
            }

            channelCount++;
        }

        return channelCount > 0 ? (double) pendingRequestCountTotal / (channelCount * this.maxRequestsPerChannel) : 1D;
    }

    private void doAcquireChannel(final Promise<Channel> promise, final Channel candidate) {

        final Promise<Channel> anotherPromise = this.newChannelPromise(promise);
        final EventLoop loop = candidate.eventLoop();

        if (loop.inEventLoop()) {
            this.doChannelHealthCheck(candidate, anotherPromise);
        } else {
            loop.execute(() -> this.doChannelHealthCheck(candidate, anotherPromise));
        }
    }

    private void doChannelHealthCheck(final Channel channel, final Promise<Channel> promise) {

        checkState(channel.eventLoop().inEventLoop());
        final Future<Boolean> isHealthy = this.healthChecker.isHealthy(channel);

        if (isHealthy.isDone()) {
            this.notifyChannelHealthCheck(isHealthy, channel, promise);
        } else {
            isHealthy.addListener((FutureListener<Boolean>) future -> this.notifyChannelHealthCheck(
                future,
                channel,
                promise));
        }
    }

    private void doChannelHealthCheckOnRelease(final Channel channel, final Promise<Void> promise) {

        try {
            checkState(channel.eventLoop().inEventLoop());
            final Future<Boolean> future = this.healthChecker.isHealthy(channel);

            if (future.isDone()) {
                this.releaseAndOfferChannelIfHealthy(channel, promise, future);
            } else {
                future.addListener(ignored -> this.releaseAndOfferChannelIfHealthy(channel, promise, future));
            }

        } catch (Throwable error) {
            if (this.executor.inEventLoop()) {
                this.closeChannelAndFail(channel, error, promise);
            } else {
                this.executor.submit(() -> this.closeChannelAndFail(channel, error, promise));
            }
        }
    }

    private void doClose() {

        this.ensureInEventLoop();

        this.acquisitionAndIdleEndpointDetectionTimeout.getAndUpdate(timeout -> {
            timeout.cancel();
            return null;
        });

        // TODO (DANOBLE) this.idleStateDetectionScheduledFuture.cancel(true);

        if (logger.isDebugEnabled()) {
            logger.debug("{} closing with {} pending channel acquisitions", this, this.requestQueueLength());
        }

        for (; ; ) {
            final AcquireTask task = this.pendingAcquisitions.poll();
            if (task == null) {
                break;
            }
            final ScheduledFuture<?> timeoutFuture = task.timeoutFuture;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
            task.promise.setFailure(new ClosedChannelException());
        }

        // NOTE: we must dispatch this request on another thread--the closer thread--as this.doClose is called on
        // this.executor and we need to ensure we will not block it.

        closer.submit(() -> {

            this.availableChannels.addAll(this.acquiredChannels.values());
            this.acquiredChannels.clear();

            for (; ; ) {
                final Channel channel = this.pollChannel();
                if (channel == null) {
                    break;
                }
                channel.close().awaitUninterruptibly(); // block and ignore errors reported back from channel.close
            }

            assert this.acquiredChannels.isEmpty() && this.availableChannels.isEmpty();

        }).addListener(closed -> {
            if (!closed.isSuccess()) {
                logger.error("[{}] close failed due to ", this, closed.cause());
            } else {
                logger.debug("[{}] closed", this);
            }
        });
    }

    private void ensureInEventLoop() {
        reportIssueUnless(logger, this.executor.inEventLoop(), this,
            "expected to be in event loop {}, not thread {}",
            this.executor,
            Thread.currentThread());
    }

    /**
     * {@code true} if the given {@link Channel channel} is serviceable; {@code false} otherwise.
     * <p>
     * A serviceable channel is one that is open, has an {@link RntbdContext RNTBD context}, and has fewer than {@link
     * #maxRequestsPerChannel} requests in its pipeline. An inactive channel will not have a {@link RntbdRequestManager
     * request manager}. Hence, this method first checks that the channel's request manager is non-null.
     *
     * @param channel the channel to check.
     *
     * @return {@code true} if the given {@link Channel channel} is serviceable; {@code false} otherwise.
     */
    private boolean isChannelServiceable(final Channel channel) {
        final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);
        return manager != null && manager.isServiceable(this.maxRequestsPerChannel) && channel.isOpen();
    }

    /**
     * Creates a new {@link Channel channel} {@link Promise promise} that completes on this {@link
     * RntbdClientChannelPool pool}'s {@link EventExecutor executor}.
     *
     * @return a newly created {@link Promise promise} that completes on this {@link RntbdClientChannelPool pool}'s
     * {@link EventExecutor executor}.
     */
    private Promise<Channel> newChannelPromise(final Promise<Channel> promise) {

        checkNotNull(promise, "expected non-null promise");

        final AcquireListener listener = new AcquireListener(this, promise);
        final Promise<Channel> anotherPromise = this.executor.newPromise();

        listener.acquired(true);
        anotherPromise.addListener(listener);

        return anotherPromise;
    }

    private void newTimeout(
        final RntbdServiceEndpoint endpoint,
        final long idleEndpointTimeoutInNanos,
        final long requestTimerResolutionInNanos) {

        this.acquisitionAndIdleEndpointDetectionTimeout.set(acquisitionAndIdleEndpointDetectionTimer.newTimeout(
            (Timeout timeout) -> {
                final long elapsedTimeInNanos = System.nanoTime() - endpoint.lastRequestNanoTime();

                if (idleEndpointTimeoutInNanos - elapsedTimeInNanos <= 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "{} closing endpoint due to inactivity (elapsedTime: {} > idleEndpointTimeout: {})",
                            endpoint,
                            Duration.ofNanos(elapsedTimeInNanos),
                            Duration.ofNanos(idleEndpointTimeoutInNanos));
                    }
                    endpoint.close();
                    return;
                }

                if (this.requestQueueLength() <= 0) {
                    this.newTimeout(endpoint, idleEndpointTimeoutInNanos, requestTimerResolutionInNanos);
                    return;
                }

                this.executor.submit(this::runTasksInPendingAcquisitionQueue).addListener(future -> {
                    reportIssueUnless(logger, future.isSuccess(), this, "failed due to ", future.cause());
                    this.newTimeout(endpoint, idleEndpointTimeoutInNanos, requestTimerResolutionInNanos);
                });

            }, requestTimerResolutionInNanos, TimeUnit.NANOSECONDS));
    }

    private void notifyChannelConnect(final ChannelFuture future, final Promise<Channel> promise) {

        reportIssueUnless(logger, this.connecting.get(), this, "connecting: false");

        if (future.isSuccess()) {

            final Channel channel = future.channel();

            try {
                this.poolHandler.channelAcquired(channel);
            } catch (Throwable error) {
                this.closeChannelAndFail(channel, error, promise);
                return;
            }

            if (promise.trySuccess(channel)) {
                this.acquiredChannels.compute(channel, (k, v) -> {
                    reportIssueUnless(logger, v == null, this, "expected null channel, not {}", v);
                    this.connecting.set(false);
                    return channel;
                });
            } else {
                // Promise was completed in the meantime (like cancelled), just close the channel
                this.closeChannel(channel);
                this.connecting.set(false);
            }

        } else {
            promise.tryFailure(future.cause());
        }
    }

    private void notifyChannelHealthCheck(
        final Future<Boolean> future,
        final Channel channel,
        final Promise<Channel> promise) {

        checkState(channel.eventLoop().inEventLoop());

        if (future.isSuccess()) {
            final boolean isHealthy = future.getNow();
            if (isHealthy) {
                try {
                    channel.attr(POOL_KEY).set(this);
                    this.poolHandler.channelAcquired(channel);
                    this.acquiredChannels.put(channel, channel);
                    promise.setSuccess(channel);
                } catch (Throwable cause) {
                    if (this.executor.inEventLoop()) {
                        this.closeChannelAndFail(channel, cause, promise);
                    } else {
                        this.executor.submit(() -> this.closeChannelAndFail(channel, cause, promise));
                    }
                }
                return;
            }
        }

        if (this.executor.inEventLoop()) {
            this.closeChannel(channel);
            this.acquireChannel(promise);
        } else {
            this.executor.submit(() -> {
                this.closeChannel(channel);
                this.acquireChannel(promise);
            });
        }
    }

    /**
     * Offer a {@link Channel} back to the available channel pool.
     * <p>
     * Maintainers: Implementations of this method must be thread-safe and this type ensures thread safety by calling
     * this method serially on a single-threaded EventExecutor. As a result this method need not (and should not) be
     * synchronized.
     *
     * @param channel the {@link Channel} to return to internal storage.
     *
     * @return {@code true}, if the {@link Channel} could be added to internal storage; otherwise {@code false}.
     */
    private boolean offerChannel(final Channel channel) {
        this.ensureInEventLoop();
        return this.availableChannels.offer(channel);
    }

    /**
     * Poll a {@link Channel} out of internal storage to reuse it
     * <p>
     * Maintainers: Implementations of this method must be thread-safe and this type ensures thread safety by calling
     * this method serially on a single-threaded EventExecutor. As a result this method need not (and should not) be
     * synchronized.
     *
     * @return a value of {@code null}, if no {@link Channel} is ready to be reused
     *
     * @see #acquire(Promise)
     */
    private Channel pollChannel() {

        final Channel first = this.availableChannels.pollLast();

        if (first == null) {
            return null;  // because there are no available channels
        }

        if (this.isClosed()) {
            return first;  // because this.close -> this.close0 -> this.pollChannel
        }

        if (this.isChannelServiceable(first)) {
            return first;
        }

        this.availableChannels.offer(first);  // because we need a non-null sentinel to stop the search for a channel

        for (Channel next = this.availableChannels.pollLast(); next != first; next =
            this.availableChannels.pollLast()) {

            assert next != null : "impossible";

            if (next.isActive()) {
                if (this.isChannelServiceable(next)) {
                    return next;
                }
                this.availableChannels.offer(next);
            }
        }

        this.availableChannels.offer(first);  // we choose not to check any channel more than once in a single call
        return null;
    }

    /**
     * Releases a {@link Channel channel} and offers it back to the {@link RntbdClientChannelPool pool}.
     *
     * @param channel the channel to put back to the pool.
     * @param promise a promise to fulfill when the operation completes. If the operation fails, {@code channel} will be
     * closed.
     */
    private void releaseAndOfferChannel(final Channel channel, final Promise<Void> promise) {
        this.ensureInEventLoop();
        try {
            if (this.offerChannel(channel)) {
                this.poolHandler.channelReleased(channel);
                promise.setSuccess(null);
            } else {
                final IllegalStateException error = new StacklessIllegalStateException(lenientFormat(
                    "cannot offer channel back to pool because the pool is at capacity (%s)\n  %s\n  %s",
                    this.maxChannels,
                    this,
                    channel));
                this.closeChannelAndFail(channel, error, promise);
            }
        } catch (Throwable error) {
            this.closeChannelAndFail(channel, error, promise);
        }
    }

    /**
     * Adds a {@link Channel channel} back to the {@link RntbdClientChannelPool pool} only if the channel is healthy.
     *
     * @param channel the  {@link Channel channel} to put back to the {@link RntbdClientChannelPool pool}.
     * @param promise offer operation promise.
     * @param future contains a value of {@code true}, if (@code channel} is healthy; {@code false} otherwise.
     */
    private void releaseAndOfferChannelIfHealthy(
        final Channel channel,
        final Promise<Void> promise,
        final Future<Boolean> future) {

        final boolean isHealthy = future.getNow();

        if (isHealthy) {
            // Channel is healthy so...
            if (this.executor.inEventLoop()) {
                this.releaseAndOfferChannel(channel, promise);
            } else {
                this.executor.submit(() -> this.releaseAndOfferChannel(channel, promise));
            }
        } else {
            // Channel is unhealthy so just close and release it
            try {
                this.poolHandler.channelReleased(channel);
            } catch (Throwable error) {
                logger.debug("[{}] pool handler failed due to ", this, error);
            } finally {
                if (this.executor.inEventLoop()) {
                    this.closeChannel(channel);
                } else {
                    this.executor.submit(() -> this.closeChannel(channel));
                }
                promise.setSuccess(null);
            }
        }
    }

    /**
     * Releases a {@link Channel channel} back to the {@link RntbdClientChannelPool pool}.
     *
     * @param channel a {@link Channel channel} to be released to the current {@link RntbdClientChannelPool pool}.
     * @param promise a promise that completes when {@code channel} is released.
     * <p>
     * If {@code channel} was not acquired from the current {@link RntbdClientChannelPool pool}, it is closed and {@code
     * promise} completes with an {@link IllegalStateException}.
     */
    private void releaseChannel(final Channel channel, final Promise<Void> promise) {

        checkState(channel.eventLoop().inEventLoop());

        final ChannelPool pool = channel.attr(POOL_KEY).getAndSet(null);
        final boolean acquired = this.acquiredChannels.get(channel) != null;

        if (acquired && pool == this) {
            try {
                if (this.releaseHealthCheck) {
                    this.doChannelHealthCheckOnRelease(channel, promise);
                } else {
                    if (this.executor.inEventLoop()) {
                        this.releaseAndOfferChannel(channel, promise);
                    } else {
                        this.executor.submit(() -> this.releaseAndOfferChannel(channel, promise));
                    }
                }
            } catch (Throwable cause) {
                if (this.executor.inEventLoop()) {
                    this.closeChannelAndFail(channel, cause, promise);
                } else {
                    this.executor.submit(() -> this.closeChannelAndFail(channel, cause, promise));
                }
            }
        } else {
            final IllegalStateException error = new IllegalStateException(lenientFormat(
                "%s cannot be released because it was not acquired by this pool: %s",
                RntbdObjectMapper.toJson(channel),
                this));
            if (this.executor.inEventLoop()) {
                this.closeChannelAndFail(channel, error, promise);
            } else {
                this.executor.submit(() -> this.closeChannelAndFail(channel, error, promise));
            }
        }
    }

    /**
     * Runs tasks in the pending acquisition queue until it's empty.
     * <p>
     * Tasks that run without being fulfilled will be added back to the {@link #pendingAcquisitions} by a call to
     * {@link #acquire}.
     */
    private void runTasksInPendingAcquisitionQueue() {

        this.ensureInEventLoop();
        int channelsAvailable = this.availableChannels.size();

        while (--channelsAvailable >= 0) {

            final AcquireTask task = this.pendingAcquisitions.poll();

            if (task == null) {
                break;
            }

            final ScheduledFuture<?> timeoutFuture = task.timeoutFuture;

            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            task.acquired(true);
            this.acquire(task.promise);
        }
    }

    private void throwIfClosed() {
        checkState(!this.isClosed(), "%s is closed", this);
    }

    // endregion

    // region Types

    private static class AcquireListener implements FutureListener<Channel> {

        private final Promise<Channel> originalPromise;
        private final RntbdClientChannelPool pool;
        private boolean acquired;

        AcquireListener(RntbdClientChannelPool pool, Promise<Channel> originalPromise) {
            this.originalPromise = originalPromise;
            this.pool = pool;
        }

        public final boolean acquired() {
            return this.acquired;
        }

        public final AcquireListener acquired(boolean value) {

            if (this.acquired) {
                return this;
            }

            this.acquired = true;
            return this;
        }

        /**
         * Ensures that a channel in the {@link RntbdClientChannelPool pool} is ready to receive requests.
         * <p>
         * A Direct TCP channel is ready to receive requests when it is active and has an {@link RntbdContext}.
         * This method sends a health check request on a channel without an {@link RntbdContext} to force:
         * <ol>
         * <li>SSL negotiation</li>
         * <li>RntbdContextRequest -> RntbdContext</li>
         * <li>RntbdHealthCheckRequest -> receive acknowledgement</li>
         * </ol>
         *
         * @param future a channel {@link Future future}.
         * <p>
         * {@link #originalPromise} is completed asynchronously when this method determines that the channel is ready to
         * receive requests or an error is encountered.
         */
        @Override
        public final void operationComplete(Future<Channel> future) {

            this.pool.ensureInEventLoop();

            if (this.pool.isClosed()) {
                if (future.isSuccess()) {
                    // Since the pool is closed, we have no choice but to close the channel
                    future.getNow().close();
                }
                this.originalPromise.setFailure(POOL_CLOSED_ON_ACQUIRE);
                return;
            }

            if (future.isSuccess()) {

                // Ensure that the channel is active and ready to receive requests

                final Channel channel = future.getNow();

                channel.eventLoop().execute(() -> {

                    if (!channel.isActive()) {
                        this.fail(CHANNEL_CLOSED_ON_ACQUIRE);
                        return;
                    }

                    final ChannelPipeline pipeline = channel.pipeline();
                    checkState(pipeline != null, "expected non-null channel pipeline");

                    final RntbdRequestManager requestManager = pipeline.get(RntbdRequestManager.class);
                    checkState(requestManager != null, "expected non-null request manager");

                    if (requestManager.hasRequestedRntbdContext()) {
                        this.originalPromise.setSuccess(channel);
                    } else {
                        channel.writeAndFlush(RntbdHealthCheckRequest.MESSAGE).addListener(completed -> {
                            if (completed.isSuccess()) {
                                reportIssueUnless(logger, this.acquired && requestManager.hasRntbdContext(), this,
                                    "acquired: {}, rntbdContext: {}",
                                    this.acquired,
                                    requestManager.rntbdContext());
                                this.originalPromise.setSuccess(channel);
                            } else {
                                final Throwable cause = completed.cause();
                                logger.warn("Channel({}) health check request failed due to:", channel, cause);
                                this.fail(cause);
                            }
                        });
                    }
                });

            } else {
                logger.warn("channel acquisition failed due to ", future.cause());
                this.fail(future.cause());
            }
        }

        private void fail(Throwable cause) {
            if (this.pool.executor.inEventLoop()) {
                this.pool.runTasksInPendingAcquisitionQueue();
            } else {
                this.pool.executor.submit(this.pool::runTasksInPendingAcquisitionQueue);
            }
            this.originalPromise.setFailure(cause);
        }
    }

    private static final class AcquireTask extends AcquireListener {

        // AcquireTask extends AcquireListener to reduce object creations and so GC pressure

        private final long expireNanoTime;
        private final Promise<Channel> promise;
        private ScheduledFuture<?> timeoutFuture;

        AcquireTask(RntbdClientChannelPool pool, Promise<Channel> promise) {
            // We need to create a new promise to ensure the AcquireListener runs in the correct event loop
            super(pool, promise);
            this.promise = pool.executor.<Channel>newPromise().addListener(this);
            this.expireNanoTime = System.nanoTime() + pool.acquisitionTimeoutInNanos;
        }
    }

    private static abstract class AcquireTimeoutTask implements Runnable {

        private final RntbdClientChannelPool pool;

        public AcquireTimeoutTask(RntbdClientChannelPool pool) {
            this.pool = pool;
        }

        public abstract void onTimeout(AcquireTask task);

        /**
         * Runs the {@link #onTimeout} method on each expired task in {@link #pool}'s {@link
         * RntbdClientChannelPool#pendingAcquisitions}.
         */
        @Override
        public final void run() {

            this.pool.ensureInEventLoop();
            final long nanoTime = System.nanoTime();

            for (AcquireTask task : this.pool.pendingAcquisitions) {
                // Compare nanoTime as described in the System.nanoTime documentation
                // See:
                // * https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime()
                // * https://github.com/netty/netty/issues/3705
                if (nanoTime - task.expireNanoTime < 0) {
                    break;
                }
                this.pool.pendingAcquisitions.remove();
                try {
                    this.onTimeout(task);
                } catch (Throwable error) {
                    logger.error("{} channel acquisition timeout task failed due to ", this.pool, error);
                }
            }
        }
    }

    static final class JsonSerializer extends StdSerializer<RntbdClientChannelPool> {

        private static final long serialVersionUID = -8688539496437151693L;

        JsonSerializer() {
            super(RntbdClientChannelPool.class);
        }

        @Override
        public void serialize(
            final RntbdClientChannelPool value,
            final JsonGenerator generator,
            final SerializerProvider provider) throws IOException {

            final RntbdClientChannelHealthChecker healthChecker = (RntbdClientChannelHealthChecker) value.healthChecker;

            generator.writeStartObject();
            generator.writeStringField("remoteAddress", value.remoteAddress().toString());
            generator.writeBooleanField("isClosed", value.isClosed());
            generator.writeObjectFieldStart("configuration");
            generator.writeNumberField("maxChannels", value.maxChannels());
            generator.writeNumberField("maxRequestsPerChannel", value.maxRequestsPerChannel());
            generator.writeNumberField("idleConnectionTimeout", healthChecker.idleConnectionTimeoutInNanos());
            generator.writeNumberField("readDelayLimit", healthChecker.readDelayLimitInNanos());
            generator.writeNumberField("writeDelayLimit", healthChecker.writeDelayLimitInNanos());
            generator.writeEndObject();
            generator.writeObjectFieldStart("state");
            generator.writeNumberField("channelsAcquired", value.channelsAcquired());
            generator.writeNumberField("channelsAvailable", value.channelsAvailable());
            generator.writeNumberField("requestQueueLength", value.requestQueueLength());
            generator.writeEndObject();
            generator.writeEndObject();
        }
    }

    private static class StacklessIllegalStateException extends IllegalStateException {

        private static final long serialVersionUID = -6011782222645074949L;

        public StacklessIllegalStateException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    // endregion
}
