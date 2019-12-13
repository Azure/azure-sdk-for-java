// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdEndpoint.Config;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocatorMetric;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.google.common.base.Preconditions.checkState;

/**
 * {@link ChannelPool} implementation that enforces a maximum number of concurrent direct TCP Cosmos connections
 */
@JsonSerialize(using = RntbdClientChannelPool.JsonSerializer.class)
public final class RntbdClientChannelPool extends SimpleChannelPool {

    private static final TimeoutException ACQUISITION_TIMEOUT = ThrowableUtil.unknownStackTrace(
        new TimeoutException("Acquisition took longer than the configured maximum time"),
        RntbdClientChannelPool.class, "<init>");

    private static final ClosedChannelException CHANNEL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new ClosedChannelException(), RntbdClientChannelPool.class, "acquire0(...)");

    private static final IllegalStateException POOL_CLOSED_ON_ACQUIRE = ThrowableUtil.unknownStackTrace(
        new IllegalStateException("RntbdClientChannelPool was closed"),
        RntbdClientChannelPool.class, "acquire0");

    private static final IllegalStateException POOL_CLOSED_ON_RELEASE = ThrowableUtil.unknownStackTrace(
        new IllegalStateException("RntbdClientChannelPool was closed"),
        RntbdClientChannelPool.class, "release");

    private static final IllegalStateException TOO_MANY_PENDING_ACQUISITIONS = ThrowableUtil.unknownStackTrace(
        new IllegalStateException("Too many outstanding acquire operations"),
        RntbdClientChannelPool.class, "acquire0");

    private static final Logger logger = LoggerFactory.getLogger(RntbdClientChannelPool.class);

    private final long acquisitionTimeoutNanos;
    private final PooledByteBufAllocatorMetric allocatorMetric;
    private final EventExecutor executor;
    private final ScheduledFuture<?> idleStateDetectionScheduledFuture;
    private final int maxChannels;
    private final int maxPendingAcquisitions;
    private final int maxRequestsPerChannel;

    // No need to worry about synchronization as everything that modified the queue or counts is done by this.executor

    private final Queue<AcquireTask> pendingAcquisitionQueue = new ArrayDeque<AcquireTask>();
    private final Runnable acquisitionTimeoutTask;

    // Because these values can be requested on any thread...

    private final AtomicInteger acquiredChannelCount = new AtomicInteger();
    private final AtomicInteger availableChannelCount = new AtomicInteger();
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Initializes a newly created {@link RntbdClientChannelPool} object
     *  @param bootstrap the {@link Bootstrap} that is used for connections
     * @param config    the {@link Config} that is used for the channel pool instance created
     */
    RntbdClientChannelPool(final RntbdServiceEndpoint endpoint, final Bootstrap bootstrap, final Config config) {
        this(endpoint, bootstrap, config, new RntbdClientChannelHealthChecker(config));
    }

    private RntbdClientChannelPool(
        final RntbdServiceEndpoint endpoint, final Bootstrap bootstrap, final Config config,
        final RntbdClientChannelHealthChecker healthChecker
    ) {

        super(bootstrap, new RntbdClientChannelHandler(config, healthChecker), healthChecker, true, true);

        this.allocatorMetric = config.allocator().metric();
        this.executor = bootstrap.config().group().next();
        this.maxChannels = config.maxChannelsPerEndpoint();
        this.maxPendingAcquisitions = Integer.MAX_VALUE;
        this.maxRequestsPerChannel = config.maxRequestsPerChannel();

        // TODO: DANOBLE: Add RntbdEndpoint.Config settings for acquisition timeout and acquisition timeout action
        //  Alternatively: drop acquisition timeout and acquisition timeout action
        //  Decision should be based on performance, reliability, and usability considerations

        final AcquisitionTimeoutAction acquisitionTimeoutAction = null;
        final long acquisitionTimeoutNanos = -1L;

        if (acquisitionTimeoutAction == null) {

            this.acquisitionTimeoutNanos = -1L;
            this.acquisitionTimeoutTask = null;

        } else {

            this.acquisitionTimeoutNanos = acquisitionTimeoutNanos;

            switch (acquisitionTimeoutAction) {
                case FAIL:
                    this.acquisitionTimeoutTask = new AcquireTimeoutTask(this) {
                        @Override
                        public void onTimeout(AcquireTask task) {
                            task.promise.setFailure(ACQUISITION_TIMEOUT);
                        }
                    };
                    break;
                case NEW:
                    this.acquisitionTimeoutTask = new AcquireTimeoutTask(this) {
                        @Override
                        public void onTimeout(AcquireTask task) {
                            // Increment the acquire count and get a new Channel by delegating to super.acquire
                            task.acquired();
                            RntbdClientChannelPool.super.acquire(task.promise);
                        }
                    };
                    break;
                default:
                    throw new Error();
            }
        }

        final long idleEndpointTimeout = config.idleEndpointTimeoutInNanos();

        this.idleStateDetectionScheduledFuture = this.executor.scheduleAtFixedRate(
            () -> {

                final long currentTime = System.nanoTime();
                final long lastRequestTime = endpoint.lastRequestTime();
                final long elapsedTime = currentTime - lastRequestTime;

                if (elapsedTime > idleEndpointTimeout) {

                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "{} closing due to inactivity (time elapsed since last request: {} > idleEndpointTimeout: {})",
                            endpoint, Duration.ofNanos(elapsedTime), Duration.ofNanos(idleEndpointTimeout));
                    }

                    endpoint.close();
                }

            }, idleEndpointTimeout, idleEndpointTimeout, TimeUnit.NANOSECONDS);
    }

    // region Accessors

    public int channelsAcquired() {
        return this.acquiredChannelCount.get();
    }

    public int channelsAvailable() {
        return this.availableChannelCount.get();
    }

    public int maxChannels() {
        return this.maxChannels;
    }

    public int maxRequestsPerChannel() {
        return this.maxRequestsPerChannel;
    }

    public SocketAddress remoteAddress() {
        return this.bootstrap().config().remoteAddress();
    }

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

    public boolean isClosed() {
        return this.closed.get();
    }

    @Override
    public Future<Channel> acquire(final Promise<Channel> promise) {

        this.throwIfClosed();

        try {
            if (this.executor.inEventLoop()) {
                this.acquire0(promise);
            } else {
                this.executor.execute(() -> this.acquire0(promise));
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
                this.executor.submit(this::close0).awaitUninterruptibly();
            }
        }
    }

    @Override
    public Future<Void> release(final Channel channel, final Promise<Void> promise) {

        // We do not call this.throwIfClosed because a channel may be released back to the pool during close

        super.release(channel, this.executor.<Void>newPromise().addListener((FutureListener<Void>)future -> {

            checkState(this.executor.inEventLoop());

            if (this.isClosed()) {
                // Since the pool is closed, we have no choice but to close the channel
                promise.setFailure(POOL_CLOSED_ON_RELEASE);
                channel.close();
                return;
            }

            if (future.isSuccess()) {

                this.decrementAndRunTaskQueue();
                promise.setSuccess(null);

            } else {

                final Throwable cause = future.cause();

                if (!(cause instanceof IllegalArgumentException)) {
                    this.decrementAndRunTaskQueue();
                }

                promise.setFailure(cause);
            }
        }));

        return promise;
    }

    @Override
    public String toString() {
        return RntbdObjectMapper.toString(this);
    }

    /**
     * Offer a {@link Channel} back to the internal storage
     * <p>
     * Maintainers: Implementations of this method must be thread-safe.
     *
     * @param channel the {@link Channel} to return to internal storage
     * @return {@code true}, if the {@link Channel} could be added to internal storage; otherwise {@code false}
     */
    @Override
    protected boolean offerChannel(final Channel channel) {
        if (super.offerChannel(channel)) {
            this.availableChannelCount.incrementAndGet();
            return true;
        }
        return false;
    }

    /**
     * Poll a {@link Channel} out of internal storage to reuse it
     * <p>
     * Maintainers: Implementations of this method must be thread-safe and this type ensures thread safety by calling
     * this method serially on a single-threaded EventExecutor. As a result this method need not (and should not) be
     * synchronized.
     *
     * @return a value of {@code null}, if no {@link Channel} is ready to be reused
     * @see #acquire(Promise)
     */
    @Override
    protected Channel pollChannel() {

        final Channel first = super.pollChannel();

        if (first == null) {
            return null;
        }

        if (this.isClosed()) {
            return first;  // because this.close -> this.close0 -> super.close -> this.pollChannel
        }

        if (this.isServiceableOrInactiveChannel(first)) {
            return this.decrementAvailableChannelCountAndAccept(first);
        }

        super.offerChannel(first);  // because we need a non-null sentinel to stop the search for a channel

        for (Channel next = super.pollChannel(); next != first; super.offerChannel(next), next = super.pollChannel()) {
            if (this.isServiceableOrInactiveChannel(next)) {
                return this.decrementAvailableChannelCountAndAccept(next);
            }
        }

        super.offerChannel(first);  // because we choose not to check any channel more than once in a single call
        return null;
    }

    // endregion

    // region Privates

    private void acquire0(final Promise<Channel> promise) {

        checkState(this.executor.inEventLoop());

        if (this.isClosed()) {
            promise.setFailure(POOL_CLOSED_ON_ACQUIRE);
            return;
        }

        if (this.acquiredChannelCount.get() < this.maxChannels) {

            // We need to create a new promise to ensure the AcquireListener runs in the correct event loop

            checkState(this.acquiredChannelCount.get() >= 0);

            final AcquireListener l = new AcquireListener(this, promise);
            l.acquired();

            final Promise<Channel> p = this.executor.newPromise();
            p.addListener(l);

            super.acquire(p); // acquire an existing channel or create and acquire a new channel

        } else {

            if (this.pendingAcquisitionQueue.size() >= this.maxPendingAcquisitions) {

                promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);

            } else {

                // Add a task to the pending acquisition queue and we'll satisfy the request later

                AcquireTask task = new AcquireTask(this, promise);

                if (this.pendingAcquisitionQueue.offer(task)) {

                    if (acquisitionTimeoutTask != null) {
                        task.timeoutFuture = executor.schedule(acquisitionTimeoutTask, acquisitionTimeoutNanos, TimeUnit.NANOSECONDS);
                    }

                } else {
                    promise.setFailure(TOO_MANY_PENDING_ACQUISITIONS);
                }
            }

            checkState(this.pendingAcquisitionQueue.size() > 0);
        }
    }

    private void close0() {

        checkState(this.executor.inEventLoop());

        this.idleStateDetectionScheduledFuture.cancel(false);
        this.acquiredChannelCount.set(0);
        this.availableChannelCount.set(0);

        for (; ; ) {
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

        // Ensure we dispatch this on another Thread as close0 will be called from the EventExecutor and we need
        // to ensure we will not block in an EventExecutor

        GlobalEventExecutor.INSTANCE.execute(RntbdClientChannelPool.super::close);
    }

    private void decrementAndRunTaskQueue() {

        checkState(acquiredChannelCount.decrementAndGet() >= 0);

        // Run the pending acquisition tasks before notifying the original promise so that if the user tries to
        // acquire again from the ChannelFutureListener and the pendingChannelAcquisitionCount is greater than
        // maxPendingAcquisitions we may be able to run some pending tasks first and so allow to add more
        runTaskQueue();
    }

    private Channel decrementAvailableChannelCountAndAccept(final Channel first) {
        this.availableChannelCount.decrementAndGet();
        return first;
    }

    private boolean isServiceableOrInactiveChannel(final Channel channel) {

        if (!channel.isActive()) {
            return true;
        }

        final RntbdRequestManager requestManager = channel.pipeline().get(RntbdRequestManager.class);

        if (requestManager == null) {
            reportIssueUnless(logger, !channel.isActive(), channel, "active with no request manager");
            return true; // inactive
        }

        return requestManager.isServiceable(1 /* this.maxRequestsPerChannel */);
    }

    private void runTaskQueue() {

        while (this.acquiredChannelCount.get() < this.maxChannels) {

            final AcquireTask task = this.pendingAcquisitionQueue.poll();

            if (task == null) {
                break;
            }

            final ScheduledFuture<?> timeoutFuture = task.timeoutFuture;

            if (timeoutFuture != null) {
                timeoutFuture.cancel(false);
            }

            task.acquired();

            super.acquire(task.promise);
        }

        checkState(this.acquiredChannelCount.get() >= 0);  // we should never have negative values
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

        public void acquired() {
            if (this.acquired) {
                return;
            }
            this.pool.acquiredChannelCount.incrementAndGet();
            this.acquired = true;
        }

        @Override
        public void operationComplete(Future<Channel> future) {

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
            if (this.acquired) {
                this.pool.decrementAndRunTaskQueue();
            } else {
                this.pool.runTaskQueue();
            }
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
            this.expireNanoTime = System.nanoTime() + pool.acquisitionTimeoutNanos;
        }
    }

    private static abstract class AcquireTimeoutTask implements Runnable {

        final RntbdClientChannelPool pool;

        public AcquireTimeoutTask(RntbdClientChannelPool pool) {
            this.pool = pool;
        }

        public abstract void onTimeout(AcquireTask task);

        @Override
        public final void run() {

            checkState(this.pool.executor.inEventLoop());
            final long nanoTime = System.nanoTime();

            for (; ; ) {
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

        JsonSerializer() {
            super(RntbdClientChannelPool.class);
        }

        @Override
        public void serialize(final RntbdClientChannelPool value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {

            RntbdClientChannelHealthChecker healthChecker = (RntbdClientChannelHealthChecker)value.healthChecker();

            generator.writeStartObject();
            generator.writeBooleanField("isClosed", value.isClosed());
            generator.writeObjectFieldStart("configuration");
            generator.writeNumberField("maxChannels", value.maxChannels());
            generator.writeNumberField("maxRequestsPerChannel", value.maxRequestsPerChannel());
            generator.writeNumberField("idleConnectionTimeout", healthChecker.idleConnectionTimeout());
            generator.writeNumberField("readDelayLimit", healthChecker.readDelayLimit());
            generator.writeNumberField("writeDelayLimit", healthChecker.writeDelayLimit());
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

    // endregion
}
