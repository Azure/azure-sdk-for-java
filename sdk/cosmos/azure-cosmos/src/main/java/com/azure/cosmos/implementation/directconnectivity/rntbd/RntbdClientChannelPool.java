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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;
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
 *
 * RntbdClientChannelPool: Actors
 * 	- acquire (RntbdServiceEndpoint): acquire a channel to use
 * 	- release (RntbdServiceEndpoint): channel usage is complete and returning it back to pool
 * 	- Channel.closeChannel() Future: Event handling notifying the channel termination to refresh bookkeeping
 * 	- acquisitionTimeoutTimer: channel acquisition time-out handler
 * 	- monitoring (through RntbdServiceEndpoint): get monitoring metrics
 *
 * 	Behaviors/Expectations:
 * 	    - Bounds:
 * 	        - max requests in-flight per channelPool: MAX_CHANNELS_PER_ENDPOINT * MAX_REQUESTS_ENDPOINT (NOT A GUARANTEE)
 * 	        - AvailableChannels.size() + AcquiredChannels.size() + (connections in connecting state, i.e., connecting.get()) <= MAX_CHANNELS_PER_ENDPOINT
 * 	        - PendingAcquisition queue default-size: Max(10_000, MAX_CHANNELS_PER_ENDPOINT * MAX_REQUESTS_ENDPOINT)
 * 	        - ChannelPool executor included event-loop task: MAX_CHANNELS_PER_ENDPOINT * MAX_REQUESTS_ENDPOINT + newInFlightAcquisitions (not yet in pendingAcquisitionQueue)
 * 	            - newInFlightAcquisitions: is expected to very very short. Hard-bound to ADMINSSON_CONTROL (upstream in RntbdServiceEndpoint)
 * 	    - NewChannel vs ReUseChannel:
 * 	        - NewChannels are serially created (reasonable current state, possible future change, upstream please DON'T TAKE any dependency)
 * 	        - Will re-use an existing channel when possible (with MAX_REQUESTS_ENDPOINT attempt not GUARANTEED)
 * 	        - Channel usage fairness: fairness is attempted but not guaranteed
 * 	            - When loadFactor is > 90%, fairness is attempted by selecting Channel with less concurrency
 * 	            - Otherwise no guarantees on fairness per channel with-in bounds of MAX_REQUESTS_ENDPOINT. I.e. some channel might have high request concurrency compared to others
 * 	    - Channel serving guarantees:
 * 	        - Ordered delivery is not guaranteed (by-design)
 * 	        - Fairness is attempted but not a guarantee
 * 	        - [UNRELATED TO CHANNEL-POOL] [CURRENT DESIGN]: RntbdServiceEndpoint.write releases Channel before its usage -> acquisition order and channel user order might differ.
 * 	    - AcquisitionTimeout: if not can't be served in an expected time, fails gracefully
 * 	    - Metrics: are approximations and might be in-consistent(by-design) as well
 * 	    - EventLoop
 * 	        - ChannelPool executor might be shared across ChannelPools or Channel
 *
 * 	Design Notes:
 * 	    - channelPool.eventLoop{@Link executor}: (executes on a single & same thread, serially)
 * 	        - Each channelPool gets an EventLoop (selection is round-robin)
 * 	        - Schedule only when it can be served immediately
 * 	        - Updates and reads that depend on "strong consistency" - like whether to create a new connection or not.
 * 	            - Updates to below data structures should be done only when inside eventLoop
 * 	            - {@Link acquiredChannels}
 * 	            - {@Link availableChannels}
 * 	    - AcquisitionTimeout handling:
 * 	        - A global single threaded scheduler
 * 	        - [***] Each channel independently schedules acquisitionTimeout handlers
 * 	        - touches {@Link pendingAcquisitions} might result in impacting the fairness
 * 	    - RntbdServiceEndpoint.write:
 * 	        - Promise<Channel> might AcquisitionTimeout
 * 	        - RntbdServiceEndpoint.writeWhenConnected
 * 	            - releaseToPool immediately -> unblocks next acquisition if-any
 * 	            - **Uses Channel even after release**, in channelEventLoop [Not a functional issue but to be noted]
 * 	                - Possible that acquisition order might differ the ChannelWrite order
 * 	    - MAX_REQUESTS_ENDPOINT: Truth managed by RntbdRequestManager in Channel.Pipeline
 * 	        - RequestManager only known when the Channel process them.
 * 	        - In-flight scheduled ones are unknown -> its a SOFT BOUND
 *
 */
@JsonSerialize(using = RntbdClientChannelPool.JsonSerializer.class)
public final class RntbdClientChannelPool implements ChannelPool {

    // TODO: moderakh setup proper retry in higher stack for the exceptions here
    private static final TimeoutException ACQUISITION_TIMEOUT = ThrowableUtil.unknownStackTrace(
        new TimeoutException("acquisition took longer than the configured maximum time"),
        RntbdClientChannelPool.class, "<init>");

    private static final ClosedChannelException CHANNEL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new ClosedChannelException(), RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new ChannelAcquisitionException("service endpoint was closed while acquiring a channel"),
        RntbdClientChannelPool.class, "acquire");

    private static final IllegalStateException POOL_CLOSED_ON_RELEASE = ThrowableUtil.unknownStackTrace(
        new ChannelAcquisitionException("service endpoint was closed while releasing a channel"),
        RntbdClientChannelPool.class, "release");

    private static final AttributeKey<RntbdClientChannelPool> POOL_KEY = AttributeKey.newInstance(
        RntbdClientChannelPool.class.getName());

    private static final IllegalStateException TOO_MANY_PENDING_ACQUISITIONS = ThrowableUtil.unknownStackTrace(
        new ChannelAcquisitionException("too many outstanding acquire operations"),
        RntbdClientChannelPool.class, "acquire");

    private static final EventExecutor closer = new DefaultEventExecutor(new RntbdThreadFactory(
        "channel-pool-closer",
        true,
        Thread.NORM_PRIORITY));

    private static final EventExecutor pendingAcquisitionExpirationExecutor = new DefaultEventExecutor(new RntbdThreadFactory(
        "pending-acquisition-expirator",
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
    private final RntbdServiceEndpoint endpoint;
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

    private final Queue<AcquireListener> pendingAcquisitions = new PriorityBlockingQueue<>(
        100,
        Comparator.comparingLong((task) -> task.originalPromise.getExpiryTimeInNanos()));

    private final ScheduledFuture<?> pendingAcquisitionExpirationFuture;

    /**
     * Initializes a newly created {@link RntbdClientChannelPool} instance.
     *
     * @param bootstrap the {@link Bootstrap} that is used for connections.
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

        this.endpoint = endpoint;
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
        //  The metric is redundant in the scope of this class and should be pulled up to RntbdServiceEndpoint or
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
             * @param task a {@link AcquireListener channel acquisition task} that has timed out.
             */
            @Override
            public void onTimeout(AcquireListener task) {
                task.originalPromise.setFailure(ACQUISITION_TIMEOUT);
            }
        };

        newTimeout(endpoint, config.idleEndpointTimeoutInNanos(), config.requestTimerResolutionInNanos());

        if (this.acquisitionTimeoutTask != null) {
            this.pendingAcquisitionExpirationFuture =
                pendingAcquisitionExpirationExecutor.scheduleAtFixedRate(
                    this.acquisitionTimeoutTask,
                    this.acquisitionTimeoutInNanos,
                    this.acquisitionTimeoutInNanos,
                    TimeUnit.NANOSECONDS);
        } else {
            this.pendingAcquisitionExpirationFuture = null;
        }

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
     * @param approximationAcceptable if approximation is acceptable.
     * @return the current channel count.
     */
    public int channels(boolean approximationAcceptable) {
        if (!approximationAcceptable) {
            ensureInEventLoop();
        }

        return this.acquiredChannels.size() + this.availableChannels.size() + (this.connecting.get() ? 1 : 0);
    }

    /**
     * Gets the current acquired channel count.
     *
     * @return the current acquired channel count.
     */
    public int channelsAcquiredMetrics() {
        return this.acquiredChannels.size();
    }

    /**
     * Gets the current available channel count.
     *
     * NOTE: this only provides approximation for metrics
     *
     * @return the current available channel count.
     */
    public int channelsAvailableMetrics() {
        return this.availableChannels.size();
    }

    /**
     * Gets the number of connections which are getting established.
     *
     * @return the number of connections which are getting established.
     */
    public int attemptingToConnectMetrics() {
        return this.connecting.get() ? 1 : 0;
    }

    /**
     * Gets the current tasks in the executor pool
     *
     * NOTE: this only provides approximation for metrics
     *
     * @return the current tasks in the executor pool
     */
    public int executorTaskQueueMetrics() {
        return RntbdUtils.tryGetExecutorTaskQueueSize(this.executor);
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
        return this.acquire(new ChannelPromiseWithExpiryTime(
            this.bootstrap.config().group().next().newPromise(),
            System.nanoTime() + this.acquisitionTimeoutInNanos));
    }

    public Future<Channel> acquire(RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        return this.acquire(new ChannelPromiseWithExpiryTime(
            this.bootstrap.config().group().next().newPromise(),
            System.nanoTime() + this.acquisitionTimeoutInNanos,
            channelAcquisitionTimeline));
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

        final ChannelPromiseWithExpiryTime promiseWithExpiryTime = promise instanceof ChannelPromiseWithExpiryTime ?
            (ChannelPromiseWithExpiryTime) promise :
                new ChannelPromiseWithExpiryTime(promise, System.nanoTime() + acquisitionTimeoutInNanos);

        try {
            if (this.executor.inEventLoop()) {
                this.acquireChannel(promiseWithExpiryTime);
            } else {
                if (pendingAcquisitions.size() > 1000) {
                    addTaskToPendingAcquisitionQueue(promiseWithExpiryTime);
                } else {
                    this.executor.execute(() -> this.acquireChannel(promiseWithExpiryTime));
                }
            }
        } catch (Throwable cause) {
            promiseWithExpiryTime.setFailure(cause);
        }

        return promiseWithExpiryTime;
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
        this.pendingAcquisitionExpirationFuture.cancel(false);
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
     * <li>there are no acquired channels pending release ({@link #channelsAcquiredMetrics} == 0).</li>
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
     * @see #getChannelState(Channel) (Channel)
     * @see AcquireTimeoutTask
     */
    private void acquireChannel(final ChannelPromiseWithExpiryTime promise) {

        this.ensureInEventLoop();

        reportIssueUnless(logger, promise != null, this, "Channel promise should not be null");
        RntbdChannelAcquisitionTimeline channelAcquisitionTimeline = promise.getChannelAcquisitionTimeline();
        RntbdChannelAcquisitionTimeline.startNewEvent(
            channelAcquisitionTimeline,
            RntbdChannelAcquisitionEventType.ATTEMPT_TO_ACQUIRE_CHANNEL);

        if (this.isClosed()) {
            promise.setFailure(POOL_CLOSED_ON_ACQUIRE);
            return;
        }

        try {
            Channel candidate = this.pollChannel(channelAcquisitionTimeline);

            if (candidate != null) {

                // Fulfill this request with our candidate, assuming it's healthy
                // If our candidate is unhealthy, notifyChannelHealthCheck will call us again

                doAcquireChannel(promise, candidate);
                return;
            }

            // make sure to retrieve the actual channel count to avoid establishing more
            // TCP connections than allowed.
            final int channelCount = this.channels(false);

            if (channelCount < this.maxChannels) {

                if (this.connecting.compareAndSet(false, true)) {

                    // Fulfill this request with a new channel, assuming we can connect one
                    // If our connection attempt fails, notifyChannelConnect will call us again

                    final Promise<Channel> anotherPromise = this.newChannelPromiseForToBeEstablishedChannel(promise);

                    RntbdChannelAcquisitionTimeline.startNewEvent(
                        channelAcquisitionTimeline,
                        RntbdChannelAcquisitionEventType.ATTEMPTED_TO_CREATE_NEW_CHANNEL);

                    final ChannelFuture future = this.bootstrap.clone().attr(POOL_KEY, this).connect();

                    if (future.isDone()) {
                        this.safeNotifyChannelConnect(future, anotherPromise);
                    } else {
                        future.addListener(ignored -> this.safeNotifyChannelConnect(future, anotherPromise));
                    }

                    return;
                }

            } else if (this.computeLoadFactor() > 0.90D) {

                // All channels are swamped and we'll pick the one with the lowest pending request count

                long pendingRequestCountMin = Long.MAX_VALUE;

                for (Channel channel : this.availableChannels) {

                    final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);

                    if (manager == null) {
                        logger.debug("Channel({} --> {}) closed", channel, this.remoteAddress());
                    } else {
                        final long pendingRequestCount = manager.pendingRequestCount();

                        // we accept the risk of reusing the channel even if more than maxPendingRequests are
                        // queued - by picking the channel with the least number of outstanding requests we load
                        // balance reasonably
                        if (pendingRequestCount < pendingRequestCountMin) {
                            RntbdChannelState channelState = this.getChannelState(channel);
                            RntbdChannelAcquisitionTimeline.addDetailsToLastEvent(channelAcquisitionTimeline, channelState);

                            if (channelState == RntbdChannelState.OK) {
                                pendingRequestCountMin = pendingRequestCount;
                                candidate = channel;
                            }
                        }
                    }
                }

                if (candidate != null && this.availableChannels.remove(candidate)) {
                    this.doAcquireChannel(promise, candidate);
                    return;
                }
            } else {
                for (Channel channel : this.availableChannels) {

                    // we pick the first available channel to avoid the additional cost of load balancing
                    // as long as the load is lower than the load factor threshold above.
                    RntbdChannelState channelState = this.getChannelState(channel);
                    RntbdChannelAcquisitionTimeline.addDetailsToLastEvent(channelAcquisitionTimeline, channelState);

                    if (channelState == RntbdChannelState.OK) {
                        if (this.availableChannels.remove(channel)) {
                            this.doAcquireChannel(promise, channel);
                            return;
                        }
                    }
                }
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
    private void addTaskToPendingAcquisitionQueue(ChannelPromiseWithExpiryTime promise) {
        if (logger.isDebugEnabled()) {
            logger.debug("{}, {}, {}, {}, {}, {}",
                Instant.now(),
                this.remoteAddress(),
                this.channels(true),
                this.channelsAcquiredMetrics(),
                this.channelsAvailableMetrics(),
                this.requestQueueLength());
        }

        if (this.pendingAcquisitions.size() >= this.maxPendingAcquisitions) {
            promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);
        } else {
            final AcquireListener acquireTask = new AcquireListener(this, promise);

            if (!this.pendingAcquisitions.offer(acquireTask)) {
                promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);
            } else {
                RntbdChannelAcquisitionTimeline.startNewEvent(
                    promise.getChannelAcquisitionTimeline(),
                    RntbdChannelAcquisitionEventType.ADDED_TO_PENDING_QUEUE);
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
        this.availableChannels.remove(channel);
        channel.attr(POOL_KEY).set(null);
        channel.close();
    }

    private void closeChannelAndFail(final Channel channel, final Throwable cause, final Promise<?> promise) {
        this.ensureInEventLoop();
        this.closeChannel(channel);
        promise.tryFailure(cause);
    }

    private double computeLoadFactor() {
        // TODO: moderakh improve logic and use in acquire?
        ensureInEventLoop();

        long pendingRequestCountMin = Long.MAX_VALUE;
        long pendingRequestCountTotal = 0L;
        long channelCount = 0;

        for (Channel channel : this.availableChannels) {
            final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);

            if (manager == null) {
                logger.debug("Channel({}) connection lost", channel);
                continue;
            }

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

    private void doAcquireChannel(final ChannelPromiseWithExpiryTime promise, final Channel candidate) {
        this.ensureInEventLoop();
        acquiredChannels.put(candidate, candidate);

        final ChannelPromiseWithExpiryTime anotherPromise =
            this.newChannelPromiseForAvailableChannel(promise, candidate);

        final EventLoop loop = candidate.eventLoop();

        if (loop.inEventLoop()) {
            this.doChannelHealthCheck(candidate, anotherPromise);
        } else {
            loop.execute(() -> this.doChannelHealthCheck(candidate, anotherPromise));
        }
    }

    private void doChannelHealthCheck(final Channel channel, final ChannelPromiseWithExpiryTime promise) {

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
            final AcquireListener task = this.pendingAcquisitions.poll();
            if (task == null) {
                break;
            }
            task.originalPromise.setFailure(new ClosedChannelException());
        }

        // NOTE: we must dispatch this request on another thread--the closer thread--as this.doClose is called on
        // this.executor and we need to ensure we will not block it.

        this.executor.submit(() -> {

            // TODO: moderakh how can we ensure no one else is creating connections right now ???
            // validate race condition
            ensureInEventLoop();

            this.availableChannels.addAll(this.acquiredChannels.values());
            this.acquiredChannels.clear();

            List<Channel> channelList = new ArrayList<>();

            for (; ; ) {
                // will remove from available channels
                final Channel channel = this.pollChannel(null);
                if (channel == null) {
                    break;
                }

                channelList.add(channel);
            }

            assert this.acquiredChannels.isEmpty() && this.availableChannels.isEmpty();

            closer.submit(() -> {
                    for (Channel channel : channelList) {
                        channel.close().awaitUninterruptibly(); // block and ignore errors reported back from channel
                        // .close
                    }
                }
            );

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
     * Creates a new {@link Channel channel} {@link Promise promise} that completes on a dedicated
     * {@link EventExecutor executor} to avoid spamming the {@link RntbdClientChannelPool pool}'s
     * {@link EventExecutor executor}.
     *
     * @return a newly created {@link Promise promise} that completes on a dedicated
     * {@link EventExecutor executor} to avoid spamming the {@link RntbdClientChannelPool pool}'s
     * {@link EventExecutor executor}.
     */
    private ChannelPromiseWithExpiryTime newChannelPromiseForAvailableChannel(
        final ChannelPromiseWithExpiryTime promise,
        final Channel candidate) {

        return this.createNewChannelPromise(promise, candidate.eventLoop());
    }

    /**
     * Creates a new {@link Channel channel} {@link Promise promise} that completes on a dedicated
     * {@link EventExecutor executor} to avoid spamming the {@link RntbdClientChannelPool pool}'s
     * {@link EventExecutor executor}.
     *
     * @return a newly created {@link Promise promise} that completes on a dedicated
     * {@link EventExecutor executor} to avoid spamming the {@link RntbdClientChannelPool pool}'s
     * {@link EventExecutor executor}.
     */
    private ChannelPromiseWithExpiryTime newChannelPromiseForToBeEstablishedChannel(
        final ChannelPromiseWithExpiryTime promise) {

        return this.createNewChannelPromise(promise, this.executor);
    }

    private ChannelPromiseWithExpiryTime createNewChannelPromise(
        final ChannelPromiseWithExpiryTime promise,
        final EventExecutor eventLoop) {

        checkNotNull(promise, "expected non-null promise");

        final AcquireListener listener = new AcquireListener(this, promise);
        final Promise<Channel> anotherPromise = eventLoop.newPromise();

        listener.acquired();
        anotherPromise.addListener(listener);

        return new ChannelPromiseWithExpiryTime(anotherPromise, promise.getExpiryTimeInNanos(), promise.getChannelAcquisitionTimeline());
    }

    private void newTimeout(
        final RntbdServiceEndpoint endpoint,
        final long idleEndpointTimeoutInNanos,
        final long requestTimerResolutionInNanos) {

        this.acquisitionAndIdleEndpointDetectionTimeout.set(acquisitionAndIdleEndpointDetectionTimer.newTimeout(
            (Timeout timeout) -> {
                if (idleEndpointTimeoutInNanos == 0) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Idle endpoint check is disabled");
                    }
                } else {
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

    private void safeNotifyChannelConnect(final ChannelFuture future, final Promise<Channel> promise) {
        if (this.executor.inEventLoop()) {
            notifyChannelConnect(future, promise);
        } else {
            this.executor.submit(() ->  notifyChannelConnect(future, promise));
        }
    }

    private void safeCloseChannel(final Channel channel) {
        if (this.executor.inEventLoop()) {
            this.closeChannel(channel);
        } else {
            this.executor.submit(() -> this.closeChannel(channel));
        }
    }

    private void notifyChannelConnect(final ChannelFuture future, final Promise<Channel> promise) {
        ensureInEventLoop();

        reportIssueUnless(logger, this.connecting.get(), this, "connecting: false");

        try {
            if (future.isSuccess()) {
                final Channel channel = future.channel();

                channel.closeFuture().addListener((ChannelFuture f) -> {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Channel to endpoint {} is closed. " +
                                "isInAvailableChannels={}, " +
                                "isInAcquiredChannels={}, " +
                                "isOnChannelEventLoop={}, " +
                                "isActive={}, " +
                                "isOpen={}, " +
                                "isRegistered={}, " +
                                "isWritable={}, " +
                                "threadName={}",
                            channel.remoteAddress(),
                            availableChannels.contains(channel),
                            acquiredChannels.contains(channel),
                            channel.eventLoop().inEventLoop(),
                            channel.isActive(),
                            channel.isOpen(),
                            channel.isRegistered(),
                            channel.isWritable(),
                            Thread.currentThread().getName()
                        );
                    }

                    this.safeCloseChannel(channel);
                });

                try {
                    this.poolHandler.channelAcquired(channel);
                } catch (Throwable error) {
                    this.closeChannelAndFail(channel, error, promise);
                    return;
                }

                if (promise.trySuccess(channel)) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("established a channel local {}, remote {}", channel.localAddress(), channel.remoteAddress());
                    }

                    this.acquiredChannels.compute(channel, (ignored, acquiredChannel) -> {
                        reportIssueUnless(logger, acquiredChannel == null, this,
                            "Channel({}) to be acquired has already been acquired",
                            channel);
                        reportIssueUnless(logger, !this.availableChannels.remove(channel), this,
                            "Channel({}) to be acquired is still in the list of available channels",
                            channel);

                        return channel;
                    });
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("notifyChannelConnect promise.trySuccess(channel)=false");
                    }

                    // Promise was completed in the meantime (like cancelled), just close the channel
                    this.closeChannel(channel);
                }

            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("notifyChannelConnect future was not successful");
                }
                promise.tryFailure(future.cause());
            }
        } finally {
            if (promise instanceof ChannelPromiseWithExpiryTime) {
                RntbdChannelAcquisitionTimeline.startNewEvent(
                    ((ChannelPromiseWithExpiryTime) promise).getChannelAcquisitionTimeline(),
                    RntbdChannelAcquisitionEventType.ATTEMPTED_TO_CREATE_NEW_CHANNEL_COMPLETE
                );
            }
            this.connecting.set(false);
        }
    }

    private void notifyChannelHealthCheck(
        final Future<Boolean> future,
        final Channel channel,
        final ChannelPromiseWithExpiryTime promise) {
        checkState(channel.eventLoop().inEventLoop());

        if (future.isSuccess()) {
            final boolean isHealthy = future.getNow();
            if (isHealthy) {
                try {
                    channel.attr(POOL_KEY).set(this);
                    this.poolHandler.channelAcquired(channel);
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
     * Return {@link RntbdChannelState}.
     * <p>
     * A serviceable channel is one that is open, has an {@link RntbdContext RNTBD context}, and has fewer than {@link
     * #maxRequestsPerChannel} requests in its pipeline. An inactive channel will not have a {@link RntbdRequestManager
     * request manager}. Hence, this method first checks that the channel's request manager is non-null.
     *
     * @param channel the channel to check.
     *
     * @return {@link RntbdChannelState}.
     */
    private RntbdChannelState getChannelState(Channel channel) {
        checkNotNull(channel, "Channel cannot be null");

        final RntbdRequestManager manager = channel.pipeline().get(RntbdRequestManager.class);
        if (manager == null) {
            return RntbdChannelState.NULL_REQUEST_MANAGER;
        }
        if (!channel.isOpen()) {
            return RntbdChannelState.CLOSED;
        }

        return manager.getChannelState(this.maxPendingAcquisitions);
    }

    /**
     * Poll a {@link Channel} out of internal storage to reuse it
     * <p>
     * Maintainers: Implementations of this method must be thread-safe and this type ensures thread safety by calling
     * this method serially on a single-threaded EventExecutor. As a result this method need not (and should not) be
     * synchronized.
     *
     *
     * @param channelAcquisitionTimeline the {@link RntbdChannelAcquisitionTimeline}.
     * @return a value of {@code null}, if no {@link Channel} is ready to be reused
     *
     * @see #acquire(Promise)
     */
    private Channel pollChannel(RntbdChannelAcquisitionTimeline channelAcquisitionTimeline) {
        ensureInEventLoop();

        RntbdChannelAcquisitionEvent event =
            RntbdChannelAcquisitionTimeline.startNewEvent(
                channelAcquisitionTimeline,
                RntbdChannelAcquisitionEventType.ATTEMPT_TO_POLL_CHANNEL);

        final Channel first = this.availableChannels.pollFirst();

        if (first == null) {
            return null;  // because there are no available channels
        }

        if (this.isClosed()) {
            return first;  // because this.close -> this.close0 -> this.pollChannel
        }

        // Only return channels as servicable here if less than maxPendingRequests
        // are queued on them
        RntbdChannelState channelState = this.getChannelState(first);
        RntbdChannelAcquisitionEvent.addDetails(event, channelState);

        if (channelState == RntbdChannelState.OK) {
            return first;
        }

        this.availableChannels.offer(first);  // because we need a non-null sentinel to stop the search for a channel

        for (Channel next = this.availableChannels.pollFirst(); next != first; next = this.availableChannels.pollFirst()) {
            assert next != null : "impossible";

            if (next.isActive()) {

                // Only return channels as serviceable here if less than maxPendingRequests
                // are queued on them
                RntbdChannelState state = this.getChannelState(next);
                RntbdChannelAcquisitionEvent.addDetails(event, channelState);

                if (state == RntbdChannelState.OK) {
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

            // NOTE: The check below is just defense in-depth. We would only ever
            // try to remove a channel from acquiredChannels unsuccessfully if releaseChannel
            // is called concurrently on the same channel instance.
            //
            // We grab the channel from acquiredChannels optimistically - so
            // could end-up retrieving the same channel multiple times
            // before switching to event loop thread and removing it here
            // so we need to make sure that we only move the channel
            // back to availableChannels once
            if (this.acquiredChannels.remove(channel) == null) {
                logger.warn(
                    "Unexpected race condition - releaseChannel called twice for the same channel [{} -> {}]",
                    channel.id(),
                    this.remoteAddress());
                promise.setSuccess(null);

                return;
            }

            if (this.offerChannel(channel)) {
                this.poolHandler.channelReleased(channel);
                promise.setSuccess(null);
            } else {
                final IllegalStateException error = new ChannelAcquisitionException(lenientFormat(
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

        // NOTE: this potentially will cause unfair-ness with respect to task scheduling because
        // task from head of the pendingAcquisitions queue
        // can be taken out and be added to the end of the queue if no channel can be acquired.
        do {

            // translate a pending acquisition item to a task
            final AcquireListener task = this.pendingAcquisitions.poll();

            if (task == null) {
                break;
            }

            task.acquired();
            this.acquire(task.originalPromise);
        } while (--channelsAvailable > 0);
    }

    private void throwIfClosed() {
        checkState(!this.isClosed(), "%s is closed", this);
    }

    // endregion

    // region Types

    private static class AcquireListener implements FutureListener<Channel> {

        private final ChannelPromiseWithExpiryTime originalPromise;
        private final RntbdClientChannelPool pool;
        private boolean acquired;

        AcquireListener(RntbdClientChannelPool pool, ChannelPromiseWithExpiryTime originalPromise) {
            this.originalPromise = originalPromise;
            this.pool = pool;
        }

        public final boolean isAcquired() {
            return this.acquired;
        }

        public final AcquireListener acquired() {

            if (this.acquired) {
                return this;
            }

            this.acquired = true;
            return this;
        }

        private void doOperationComplete(Channel channel) {
            checkState(channel.eventLoop().inEventLoop());

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
                        reportIssueUnless(
                            logger,
                            this.acquired && requestManager.hasRntbdContext(),
                            this,
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

                if (channel.eventLoop().inEventLoop()) {
                    doOperationComplete(channel);
                } else {
                    channel.eventLoop().execute(() -> doOperationComplete(channel));
                }

            } else {
                logger.warn("channel acquisition failed due to ", future.cause());
                this.fail(future.cause());
            }
        }

        public long getAcquisitionTimeoutInNanos() {
            return this.originalPromise.getExpiryTimeInNanos();
        }

        private void fail(Throwable cause) {
            this.originalPromise.setFailure(cause);

            if (this.pool.executor.inEventLoop()) {
                this.pool.runTasksInPendingAcquisitionQueue();
            } else {
                this.pool.executor.submit(this.pool::runTasksInPendingAcquisitionQueue);
            }
        }
    }

    private static abstract class AcquireTimeoutTask implements Runnable {

        private final RntbdClientChannelPool pool;

        public AcquireTimeoutTask(RntbdClientChannelPool pool) {
            this.pool = pool;
        }

        public abstract void onTimeout(AcquireListener task);

        /**
         * Runs the {@link #onTimeout} method on each expired task in {@link
         * RntbdClientChannelPool#pendingAcquisitions}.
         */
        @Override
        public final void run() {
            if (logger.isDebugEnabled()) {
                logger.debug("Starting the AcquireTimeoutTask to clean for endpoint [{}].", this.pool.remoteAddress());
            }
            long currentNanoTime = System.nanoTime();

            while (true) {
                AcquireListener removedTask = this.pool.pendingAcquisitions.poll();
                if (removedTask == null) {
                    // queue is empty
                    break;
                }

                long expiryTime = removedTask.getAcquisitionTimeoutInNanos();

                // Compare nanoTime as described in the System.nanoTime documentation
                // See:
                // * https://docs.oracle.com/javase/7/docs/api/java/lang/System.html#nanoTime()
                // * https://github.com/netty/netty/issues/3705
                if (expiryTime - currentNanoTime <= 0) {
                    this.onTimeout(removedTask);
                } else {
                    if (!this.pool.pendingAcquisitions.offer(removedTask)) {
                        logger.error("Unexpected failure when returning the removed task"
                                + " to pending acquisition queue. current size [{}]",
                            this.pool.pendingAcquisitions.size());
                    }
                    break;
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
            generator.writeNumberField("channelsAcquired", value.channelsAcquiredMetrics());
            generator.writeNumberField("channelsAvailable", value.channelsAvailableMetrics());
            generator.writeNumberField("requestQueueLength", value.requestQueueLength());
            generator.writeEndObject();
            generator.writeEndObject();
        }
    }

    private static class ChannelAcquisitionException extends IllegalStateException {

        private static final long serialVersionUID = -6011782222645074949L;

        public ChannelAcquisitionException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }

    // endregion

}
