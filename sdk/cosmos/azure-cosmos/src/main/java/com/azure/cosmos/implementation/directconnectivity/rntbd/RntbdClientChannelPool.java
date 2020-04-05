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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link ChannelPool} implementation that enforces a maximum number of concurrent direct TCP Cosmos connections
 */
@JsonSerialize(using = RntbdClientChannelPool.JsonSerializer.class)
public final class RntbdClientChannelPool implements ChannelPool {

    private static final TimeoutException ACQUISITION_TIMEOUT = ThrowableUtil.unknownStackTrace(
        new TimeoutException("Acquisition took longer than the configured maximum time"),
        RntbdClientChannelPool.class, "<init>");

    private static final ClosedChannelException CHANNEL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new ClosedChannelException(), RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("RntbdClientChannelPool was closed"),
        RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_RELEASE = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("RntbdClientChannelPool was closed"),
        RntbdClientChannelPool.class, "release");

    private static final AttributeKey<RntbdClientChannelPool> POOL_KEY = AttributeKey.newInstance(
        RntbdClientChannelPool.class.getName());

    private static final IllegalStateException TOO_MANY_PENDING_ACQUISITIONS = ThrowableUtil.unknownStackTrace(
        new StacklessIllegalStateException("Too many outstanding acquire operations"),
        RntbdClientChannelPool.class, "acquire");

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelPool.class);
    private static final EventExecutor closer = new DefaultEventExecutor();

    private final long acquisitionTimeoutInNanos;
    private final PooledByteBufAllocatorMetric allocatorMetric;
    private final Bootstrap bootstrap;
    private final EventExecutor executor;
    private final ChannelHealthChecker healthChecker;
    private final ScheduledFuture<?> idleStateDetectionScheduledFuture;
    private final int maxChannels;
    private final int maxPendingAcquisitions;
    private final int maxRequestsPerChannel;
    private final ChannelPoolHandler poolHandler;
    private final boolean releaseHealthCheck;

    // No need to worry about synchronization as everything that modified the queue or counts is done by this.executor

    private final Queue<AcquireTask> pendingAcquisitionQueue = new ArrayDeque<>();
    private final Runnable acquisitionTimeoutTask;

    // Because state from these fields can be requested on any thread...

    private final ConcurrentHashMap<Channel, Channel> acquiredChannels = new ConcurrentHashMap<>();
    private final Deque<Channel> availableChannels = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean closed = new AtomicBoolean();
    private final AtomicBoolean connecting = new AtomicBoolean();

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

        // TODO (DANOBLE) Add RntbdEndpoint.Config settings for acquisition timeout action
        //  Alternatively: drop acquisition timeout and acquisition timeout action
        //  Decision should be based on performance, reliability, and usability considerations

        final AcquisitionTimeoutAction acquisitionTimeoutAction = AcquisitionTimeoutAction.NEW;

        if (acquisitionTimeoutInNanos <= 0) {

            this.acquisitionTimeoutTask = null;

        } else {

            switch (acquisitionTimeoutAction) {
                case FAIL:
                    this.acquisitionTimeoutTask = new AcquireTimeoutTask(this) {
                        /**
                         * Fails a request to acquire a {@link Channel channel}.
                         *
                         * @param task a {@link AcquireTask channel acquisition task} that has timed out.
                         */
                        @Override
                        public void onTimeout(AcquireTask task) {
                            task.promise.setFailure(ACQUISITION_TIMEOUT);
                        }
                    };
                    break;
                case NEW:
                    this.acquisitionTimeoutTask = new AcquireTimeoutTask(this) {
                        /**
                         * Reissues a request to acquire a {@link Channel channel}.
                         *
                         * @param task a {@link AcquireTask channel acquisition task} that has timed out.
                         */
                        @Override
                        public void onTimeout(AcquireTask task) {
                            task.acquired(true);
                            RntbdClientChannelPool.this.acquire(task.promise);
                        }
                    };
                    break;
                default:
                    throw new Error();
            }
        }

        final long idleEndpointTimeoutInNanos = config.idleEndpointTimeoutInNanos();

        this.idleStateDetectionScheduledFuture = this.executor.scheduleAtFixedRate(
            () -> {
                final long elapsedTimeInNanos = System.nanoTime() - endpoint.lastRequestNanoTime();

                if (idleEndpointTimeoutInNanos - elapsedTimeInNanos <= 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} closing endpoint due to inactivity (elapsedTime: {} > idleEndpointTimeout: {})",
                            endpoint,
                            Duration.ofNanos(elapsedTimeInNanos),
                            Duration.ofNanos(idleEndpointTimeoutInNanos));
                    }
                    endpoint.close();
                }
            }, idleEndpointTimeoutInNanos, idleEndpointTimeoutInNanos, TimeUnit.NANOSECONDS);
    }

    // region Accessors

    /**
     * Gets the current channel count.
     *
     * @return the current channel count.
     */
    public int channels() {
        return this.acquiredChannels.size() + this.availableChannels.size();
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
     * Healthy channels with fewer than {@code maxRequestsPerChannel} requests pending are considered to be
     * serviceable channels. Unhealthy channels are regularly removed from the pool and--depending on load--may be
     * replaced with new channels later.
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
        return this.pendingAcquisitionQueue.size();
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
     * @return a {@link Promise promise} to be notified when the operation completes. If the operation fails,
     * {@code channel} will be closed automatically.
     *
     * <p><strong>
     * It is important to {@link #release} every {@link Channel channel} acquired from the pool, even when the
     * {@link Channel channel} is closed explicitly.</strong>
     */
    @Override
    public Future<Channel> acquire() {
        return this.acquire(this.bootstrap.config().group().next().<Channel>newPromise());
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
     * It is important to {@link #release} every {@link Channel channel} acquired from the pool, even when the
     * {@link Channel channel} is closed explicitly.</strong>
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
                this.close0();
            } else {
                this.executor.submit(this::close0).awaitUninterruptibly(); // block until complete
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
            this.closeChannelAndFail(channel, cause, anotherPromise);
        }

        anotherPromise.addListener((FutureListener<Void>) future -> {

            assertInEventLoop();

            if (this.isClosed()) {
                // Since the pool is closed, we have no choice but to close the channel
                promise.setFailure(POOL_CLOSED_ON_RELEASE);
                channel.close();
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
     * <li>process items in the {@link #pendingAcquisitionQueue} on each call to {@link #acquire} or {@link #release}
     * , and</li>
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

        assertInEventLoop();

        if (this.isClosed()) {
            promise.setFailure(POOL_CLOSED_ON_ACQUIRE);
            return;
        }

            try {
                final Channel candidate = this.pollChannel();

                if (candidate != null) {

                    // Fulfill this request with our candidate, assuming it's healthy
                    // If our candidate is unhealthy, notifyChannelHealthCheck will call us again

                    final Promise<Channel> anotherPromise = newChannelPromise(promise);
                    final EventLoop loop = candidate.eventLoop();

                    if (loop.inEventLoop()) {
                        this.doChannelHealthCheck(candidate, anotherPromise);
                    } else {
                        loop.execute(() -> this.doChannelHealthCheck(candidate, anotherPromise));
                    }

                    return;
                }

                final int channelsAcquired = this.acquiredChannels.size();
                final int channelCount = this.availableChannels.size() + channelsAcquired;

                if (channelsAcquired == 0 && channelCount < this.maxChannels) {

                    if (this.connecting.compareAndSet(false, true)) {

                        // Fulfill this request with a new channel, assuming we can connect one
                        // If our connection attempt fails, notifyChannelConnect will call us again

                        final Promise<Channel> anotherPromise = newChannelPromise(promise);
                        final ChannelFuture connected = this.bootstrap.clone().attr(POOL_KEY, this).connect();

                        if (connected.isDone()) {
                            this.notifyChannelConnect(connected, anotherPromise);
                        } else {
                            connected.addListener(ignored -> this.notifyChannelConnect(connected, anotherPromise));
                        }

                        return;
                    }
                }

                // Complete this request the next time a channel is released to the pool

                checkState(channelCount == this.maxChannels || channelsAcquired > 0 || this.connecting.get());

                // TODO (DANOBLE) Remove or log the question answers in this block: Are all channels fully loaded?

                if (channelCount == this.maxChannels && channelsAcquired <= 0) {

                    // All channels are swamped?

                    int pendingRequestCountTotal = 0;

                    for (Channel channel : this.availableChannels) {
                        final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);
                        final int pendingRequestCount = manager.pendingRequestCount();
                        pendingRequestCountTotal += pendingRequestCount;
                    }

                    assert channelCount == this.availableChannels.size();
                    double load = (double) pendingRequestCountTotal / channelCount;
                    assert load > 0.50D;  // because checkstyle complains if we do nothing with the load value
                }

                this.addTaskToPendingAcquisitionQueue(promise);

            } catch (Throwable cause) {
                promise.tryFailure(cause);
            }
    }

    /**
     * Add a task to the pending acquisition queue to fulfill the request for a {@link Channel channel} later.
     * <p>
     * Tasks in the pending acquisition queue are run whenever a channel is released. This ensures that pending
     * requests for channels are fulfilled as soon as possible.
     *
     * @param promise a {@link Promise promise} that will be completed when a {@link Channel channel} is acquired or an
     * error is encountered.
     *
     * @see #runTasksInPendingAcquisitionQueue
     */
    private void addTaskToPendingAcquisitionQueue(Promise<Channel> promise) {

        if (this.pendingAcquisitionQueue.size() >= this.maxPendingAcquisitions) {

            promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);

        } else {

            final AcquireTask acquireTask = new AcquireTask(this, promise);

            if (this.pendingAcquisitionQueue.offer(acquireTask)) {
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

        checkState(this.pendingAcquisitionQueue.size() > 0);
    }

    private void assertInEventLoop() {
        checkState(this.executor.inEventLoop());
    }

    private void close0() {

        assertInEventLoop();
        this.idleStateDetectionScheduledFuture.cancel(true);

        for (;;) {
            final AcquireTask task = this.pendingAcquisitionQueue.poll();
            if (task == null) {
                break;
            }
            final ScheduledFuture<?> timeoutFuture = task.timeoutFuture;
            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }
            task.promise.setFailure(new ClosedChannelException());
        }

        // Ensure we dispatch this on another Thread as close0 will be called from the EventExecutor and we need to
        // ensure we will not block in an EventExecutor

        closer.submit(() -> {

            this.availableChannels.addAll(this.acquiredChannels.values());
            this.acquiredChannels.clear();
            for (;;) {
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
                closer.shutdownGracefully().awaitUninterruptibly();
            });
    }

    private void closeChannel(final Channel channel) {
        channel.attr(POOL_KEY).set(null);
        channel.close();
    }

    private void closeChannelAndFail(final Channel channel, final Throwable cause, final Promise<?> promise) {
        closeChannel(channel);
        promise.tryFailure(cause);
    }

    private void doChannelHealthCheck(final Channel channel, final Promise<Channel> promise) {

        checkState(channel.eventLoop().inEventLoop());
        final Future<Boolean> isHealthy = this.healthChecker.isHealthy(channel);

        if (isHealthy.isDone()) {
            this.notifyChannelHealthCheck(isHealthy, channel, promise);
        } else {
            isHealthy.addListener((FutureListener<Boolean>) future -> this.notifyChannelHealthCheck(future, channel, promise));
        }
    }

    private void doChannelHealthCheckOnRelease(final Channel channel, final Promise<Void> promise) throws Exception {

        checkState(channel.eventLoop().inEventLoop());
        final Future<Boolean> future = this.healthChecker.isHealthy(channel);

        if (future.isDone()) {
            this.releaseAndOfferChannelIfHealthy(channel, promise, future);
        } else {
            future.addListener(ignored -> this.releaseAndOfferChannelIfHealthy(channel, promise, future));
        }
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

    /**
     * {@code true} if the given {@link Channel channel} is serviceable; {@code false} otherwise.
     * <p>
     * A serviceable channel is one that is open and has less than {@link #maxRequestsPerChannel} requests in its
     * pipeline.
     *
     * @param channel the channel to check.
     *
     * @return {@code true} if the given {@link Channel channel} is serviceable; {@code false} otherwise.
     */
    private boolean isChannelServiceable(final Channel channel) {
        final RntbdRequestManager requestManager = channel.pipeline().get(RntbdRequestManager.class);
        return requestManager.isServiceable(this.maxRequestsPerChannel);
    }

    private void notifyChannelConnect(final ChannelFuture future, final Promise<Channel> promise) throws Exception {

        this.connecting.set(false);  // we're now ready to set up a new channel, if we're below maxChannels

        if (future.isSuccess()) {
            final Channel channel = future.channel();
            this.poolHandler.channelAcquired(channel);
            this.acquiredChannels.put(channel, channel);

            if (!promise.trySuccess(channel)) {
                // Promise was completed in the meantime (like cancelled), just release the channel again
                this.release(channel);
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
            if (future.getNow()) {
                try {
                    channel.attr(POOL_KEY).set(this);
                    this.poolHandler.channelAcquired(channel);
                    this.acquiredChannels.put(channel, channel);
                    promise.setSuccess(channel);
                } catch (Throwable cause) {
                    this.closeChannelAndFail(channel, cause, promise);
                }
            } else {
                this.closeChannel(channel);
                this.acquireChannel(promise);
            }
        } else {
            closeChannel(channel);
            this.acquireChannel(promise);
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
        checkState(this.availableChannels.size() < this.maxChannels);
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

        for (Channel next = this.availableChannels.pollLast(); next != first; next = this.availableChannels.pollLast()) {
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
     * @param promise a promise to fulfill when the operation completes. If the operation fails, {@code channel} will
     * be closed.
     *
     * @throws Exception if {@code channel} cannot be released.
     */
    private void releaseAndOfferChannel(final Channel channel, final Promise<Void> promise) throws Exception {
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
    }

    /**
     * Adds a {@link Channel channel} back to the {@link RntbdClientChannelPool pool} only if the channel is healthy.
     *
     * @param channel the  {@link Channel channel} to put back to the {@link RntbdClientChannelPool pool}.
     * @param promise offer operation promise.
     * @param future contains a value of {@code true}, if (@code channel} is healthy; {@code false} otherwise.
     *
     * @throws Exception if the {@code channel} cannot be released.
     */
    private void releaseAndOfferChannelIfHealthy(
        final Channel channel,
        final Promise<Void> promise,
        final Future<Boolean> future) throws Exception {

        if (future.getNow()) { //channel turns out to be healthy, offering and releasing it.
            this.releaseAndOfferChannel(channel, promise);
        } else { //channel not healthy, just releasing it.
            this.poolHandler.channelReleased(channel);
            promise.setSuccess(null);
        }
    }

    /**
     * Releases a {@link Channel channel} back to the {@link RntbdClientChannelPool pool}.
     *
     * @param channel a {@link Channel channel} to be released to the current {@link RntbdClientChannelPool pool}.
     * @param promise a promise that completes when {@code channel} is released.
     * <p>
     * If {@code channel} was not acquired from the current {@link RntbdClientChannelPool pool}, it is closed and
     * {@code promise} completes with an {@link IllegalStateException}.
     */
    private void releaseChannel(final Channel channel, final Promise<Void> promise) {

        checkState(channel.eventLoop().inEventLoop());

        final boolean isNotAcquired = this.acquiredChannels.remove(channel) == null;
        final ChannelPool pool = channel.attr(POOL_KEY).getAndSet(null);

        if (isNotAcquired || pool != this) {
            final IllegalStateException error = new IllegalStateException(lenientFormat(
                "[%s] cannot be released because it was not acquired by this pool: %s",
                RntbdObjectMapper.toJson(channel),
                channel,
                this));
            this.closeChannelAndFail(channel, error, promise);
        } else {
            try {
                if (this.releaseHealthCheck) {
                    this.doChannelHealthCheckOnRelease(channel, promise);
                } else {
                    this.releaseAndOfferChannel(channel, promise);
                }
            } catch (Throwable cause) {
                this.closeChannelAndFail(channel, cause, promise);
            }
        }
    }

    /**
     * Runs tasks in the pending acquisition queue until it's empty.
     * <p>
     * Tasks that run without being fulfilled will be added back to the {@link #pendingAcquisitionQueue} by a call to
     * {@link #acquire}.
     */
    private void runTasksInPendingAcquisitionQueue() {

        for (int i = 0; i < this.maxChannels; i++) {

            final AcquireTask task = this.pendingAcquisitionQueue.poll();

            if (task == null) {
                break;
            }

            final ScheduledFuture<?> timeoutFuture = task.timeoutFuture;

            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            if (!task.acquired()) {
                task.acquired(true);
                this.acquire(task.promise);
            }
        }
    }

    private void throwIfClosed() {
        checkState(!this.isClosed(), "%s is closed", this);
    }

    // endregion

    // region Types

    private enum AcquisitionTimeoutAction {

        /**
         * Create a new connection when the timeout is detected.
         */
        NEW,

        /**
         * Fail the {@link Future} of the acquire call with a {@link TimeoutException}.
         */
        FAIL
    }

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

            final int channelsAcquired = this.pool.channelsAcquired();
            final int channelsAvailable = this.pool.channelsAvailable();
            final int channelCount = channelsAcquired + channelsAvailable;

            checkState(0  <= channelCount && channelCount <= this.pool.maxChannels,
                "expected channelCount in range [0, %s], not %s",
                this.pool.maxChannels,
                channelCount);

            this.acquired = true;
            return this;
        }

        @Override
        public final void operationComplete(Future<Channel> future) {

            checkState(this.pool.executor.inEventLoop());

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
                // A Direct TCP channel is ready to receive requests when it:
                // * is active and
                // * has an RntbdContext
                // We send a health check request on a channel without an RntbdContext to force:
                // 1. SSL negotiation
                // 2. RntbdContextRequest -> RntbdContext
                // 3. RntbdHealthCheckRequest -> receive acknowledgement

                final Channel channel = future.getNow();

                channel.eventLoop().execute(() -> {

                    if (!channel.isActive()) {
                        this.fail(CHANNEL_CLOSED_ON_ACQUIRE);
                        return;
                    }

                    final ChannelPipeline pipeline = channel.pipeline();
                    checkState(pipeline != null);

                    final RntbdRequestManager requestManager = pipeline.get(RntbdRequestManager.class);
                    checkState(requestManager != null);

                    if (requestManager.hasRequestedRntbdContext()) {

                        this.originalPromise.setSuccess(channel);

                    } else {

                        channel.writeAndFlush(RntbdHealthCheckRequest.MESSAGE).addListener(completed -> {

                            if (completed.isSuccess()) {

                                reportIssueUnless(logger, this.acquired && requestManager.hasRntbdContext(),
                                    channel,"acquired: {}, rntbdContext: {}", this.acquired,
                                    requestManager.rntbdContext());

                                this.originalPromise.setSuccess(channel);

                            } else {

                                logger.warn("Channel({}) health check request failed due to:", channel, completed.cause());
                                this.fail(completed.cause());
                            }
                        });
                    }
                });

            } else {
                logger.warn("channel acquisition failed due to:", future.cause());
                this.fail(future.cause());
            }
        }

        private void fail(Throwable cause) {
            this.pool.runTasksInPendingAcquisitionQueue();
            this.originalPromise.setFailure(cause);
        }
    }

    private static final class AcquireTask extends AcquireListener {

        // AcquireTask extends AcquireListener to reduce object creations and so GC pressure

        final long expireNanoTime;
        final Promise<Channel> promise;
        ScheduledFuture<?> timeoutFuture;

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

        @Override
        public final void run() {

            checkState(this.pool.executor.inEventLoop());
            final long nanoTime = System.nanoTime();

            for (;;) {
                AcquireTask task = this.pool.pendingAcquisitionQueue.peek();
                // Compare nanoTime as described in the System.nanoTime documentation
                // See:
                // * https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime()
                // * https://github.com/netty/netty/issues/3705
                if (task == null || nanoTime - task.expireNanoTime < 0) {
                    break;
                }
                this.pool.pendingAcquisitionQueue.remove();
                this.onTimeout(task);
            }
        }
    }

    static final class JsonSerializer extends StdSerializer<RntbdClientChannelPool> {

        private static final long serialVersionUID = -8688539496437151693L;

        JsonSerializer() {
            super(RntbdClientChannelPool.class);
        }

        @Override
        public void serialize(final RntbdClientChannelPool value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

            RntbdClientChannelHealthChecker healthChecker = (RntbdClientChannelHealthChecker) value.healthChecker;

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
            generator.writeNumberField("usedDirectMemory", value.usedDirectMemory());
            generator.writeNumberField("usedHeapMemory", value.usedHeapMemory());
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
