// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.BadRequestException;
import com.azure.cosmos.implementation.ConflictException;
import com.azure.cosmos.implementation.CosmosError;
import com.azure.cosmos.implementation.ForbiddenException;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.InternalServerErrorException;
import com.azure.cosmos.implementation.InvalidPartitionException;
import com.azure.cosmos.implementation.LockedException;
import com.azure.cosmos.implementation.MethodNotAllowedException;
import com.azure.cosmos.implementation.NotFoundException;
import com.azure.cosmos.implementation.PartitionIsMigratingException;
import com.azure.cosmos.implementation.PartitionKeyRangeGoneException;
import com.azure.cosmos.implementation.PartitionKeyRangeIsSplittingException;
import com.azure.cosmos.implementation.PreconditionFailedException;
import com.azure.cosmos.implementation.RequestEntityTooLargeException;
import com.azure.cosmos.implementation.RequestRateTooLargeException;
import com.azure.cosmos.implementation.RequestTimeoutException;
import com.azure.cosmos.implementation.RetryWithException;
import com.azure.cosmos.implementation.ServiceUnavailableException;
import com.azure.cosmos.implementation.UnauthorizedException;
import com.azure.cosmos.implementation.directconnectivity.StoreResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CoalescingBufferQueue;
import io.netty.channel.EventLoop;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCounted;
import io.netty.util.Timeout;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.ThrowableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.HttpConstants.StatusCodes;
import static com.azure.cosmos.implementation.HttpConstants.SubStatusCodes;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdClientChannelHealthChecker.Timestamps;
import static com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava27.Strings.lenientFormat;

public final class RntbdRequestManager implements ChannelHandler, ChannelInboundHandler, ChannelOutboundHandler {

    // region Fields

    private static final ClosedChannelException ON_CHANNEL_UNREGISTERED =
        ThrowableUtil.unknownStackTrace(new ClosedChannelException(), RntbdRequestManager.class, "channelUnregistered");

    private static final ClosedChannelException ON_CLOSE =
        ThrowableUtil.unknownStackTrace(new ClosedChannelException(), RntbdRequestManager.class, "close");

    private static final ClosedChannelException ON_DEREGISTER =
        ThrowableUtil.unknownStackTrace(new ClosedChannelException(), RntbdRequestManager.class, "deregister");

    private static final EventExecutor requestExpirationExecutor = new DefaultEventExecutor(new RntbdThreadFactory(
        "request-expirator",
        true,
        Thread.NORM_PRIORITY));

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestManager.class);

    private final CompletableFuture<RntbdContext> contextFuture = new CompletableFuture<>();
    private final CompletableFuture<RntbdContextRequest> contextRequestFuture = new CompletableFuture<>();
    private final ChannelHealthChecker healthChecker;
    private final int pendingRequestLimit;
    private final ConcurrentHashMap<Long, RntbdRequestRecord> pendingRequests;
    private final Timestamps timestamps = new Timestamps();

    private boolean closingExceptionally = false;
    private CoalescingBufferQueue pendingWrites;

    // endregion

    public RntbdRequestManager(final ChannelHealthChecker healthChecker, final int pendingRequestLimit) {

        checkArgument(pendingRequestLimit > 0, "pendingRequestLimit: %s", pendingRequestLimit);
        checkNotNull(healthChecker, "healthChecker");

        this.pendingRequests = new ConcurrentHashMap<>(pendingRequestLimit);
        this.pendingRequestLimit = pendingRequestLimit;
        this.healthChecker = healthChecker;
    }

    // region ChannelHandler methods

    /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and it's ready to handle events.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void handlerAdded(final ChannelHandlerContext context) {
        this.traceOperation(context, "handlerAdded");
    }

    /**
     * Gets called after the {@link ChannelHandler} was removed from the actual context and it doesn't handle events
     * anymore.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void handlerRemoved(final ChannelHandlerContext context) {
        this.traceOperation(context, "handlerRemoved");
    }

    // endregion

    // region ChannelInboundHandler methods

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelActive(final ChannelHandlerContext context) {
        this.traceOperation(context, "channelActive");
        context.fireChannelActive();
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was registered and has reached the end of its lifetime
     * <p>
     * This method will only be called after the channel is closed.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelInactive(final ChannelHandlerContext context) {
        this.traceOperation(context, "channelInactive");
        context.fireChannelInactive();
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} has read a message from its peer.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs.
     * @param message The message read.
     */
    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {

        this.traceOperation(context, "channelRead");

        try {
            if (message.getClass() == RntbdResponse.class) {

                try {
                    this.messageReceived(context, (RntbdResponse) message);
                } catch (CorruptedFrameException error) {
                    this.exceptionCaught(context, error);
                } catch (Throwable throwable) {
                    reportIssue(context, "{} ", message, throwable);
                    this.exceptionCaught(context, throwable);
                }

            } else {

                final IllegalStateException error = new IllegalStateException(
                    lenientFormat("expected message of %s, not %s: %s",
                        RntbdResponse.class,
                        message.getClass(),
                        message));

                reportIssue(context, "", error);
                this.exceptionCaught(context, error);
            }
        } finally {
            if (message instanceof ReferenceCounted) {
                boolean released = ((ReferenceCounted) message).release();
                reportIssueUnless(released, context, "failed to release message: {}", message);
            }
        }
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} has fully consumed the most-recent message read.
     * <p>
     * If {@link ChannelOption#AUTO_READ} is off, no further attempt to read inbound data from the current
     * {@link Channel} will be made until {@link ChannelHandlerContext#read} is called. This leaves time
     * for outbound messages to be written.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext context) {
        this.traceOperation(context, "channelReadComplete");
        this.timestamps.channelReadCompleted();
        context.fireChannelReadComplete();
    }

    /**
     * Constructs a {@link CoalescingBufferQueue} for buffering encoded requests until we have an {@link RntbdRequest}
     * <p>
     * This method then calls {@link ChannelHandlerContext#fireChannelRegistered()} to forward to the next
     * {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param context the {@link ChannelHandlerContext} for which the bind operation is made
     */
    @Override
    public void channelRegistered(final ChannelHandlerContext context) {

        this.traceOperation(context, "channelRegistered");

        reportIssueUnless(this.pendingWrites == null, context, "pendingWrites: {}", pendingWrites);
        this.pendingWrites = new CoalescingBufferQueue(context.channel());

        context.fireChannelRegistered();
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered from its {@link EventLoop}
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelUnregistered(final ChannelHandlerContext context) {

        this.traceOperation(context, "channelUnregistered");

        if (!this.closingExceptionally) {
            this.completeAllPendingRequestsExceptionally(context, ON_CHANNEL_UNREGISTERED);
        } else {
            logger.debug("{} channelUnregistered exceptionally", context);
        }

        context.fireChannelUnregistered();
    }

    /**
     * Gets called once the writable state of a {@link Channel} changed. You can check the state with
     * {@link Channel#isWritable()}.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelWritabilityChanged(final ChannelHandlerContext context) {
        this.traceOperation(context, "channelWritabilityChanged");
        context.fireChannelWritabilityChanged();
    }

    /**
     * Processes {@link ChannelHandlerContext#fireExceptionCaught(Throwable)} in the {@link ChannelPipeline}
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     * @param cause   Exception caught
     */
    @Override
    @SuppressWarnings("deprecation")
    public void exceptionCaught(final ChannelHandlerContext context, final Throwable cause) {

        // TODO: DANOBLE: replace RntbdRequestManager.exceptionCaught with read/write listeners
        //  Notes:
        //    ChannelInboundHandler.exceptionCaught is deprecated and--today, prior to deprecation--only catches read--
        //    i.e., inbound--exceptions.
        //    Replacements:
        //    * read listener: unclear as there is no obvious replacement
        //    * write listener: implemented by RntbdTransportClient.DefaultEndpoint.doWrite
        //  Links:
        //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/373213

        this.traceOperation(context, "exceptionCaught", cause);

        if (!this.closingExceptionally) {
            this.completeAllPendingRequestsExceptionally(context, cause);
            logger.debug("{} closing due to:", context, cause);
            context.flush().close();
        }
    }

    /**
     * Processes inbound events triggered by channel handlers in the {@link RntbdClientChannelHandler} pipeline
     * <p>
     * All but inbound request management events are ignored.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     * @param event   An object representing a user event
     */
    @Override
    public void userEventTriggered(final ChannelHandlerContext context, final Object event) {

        this.traceOperation(context, "userEventTriggered", event);

        try {

            if (event instanceof IdleStateEvent) {
                // NOTE: if the connection is killed this may not receive any event
                this.healthChecker.isHealthy(context.channel()).addListener((Future<Boolean> future) -> {

                    final Throwable cause;

                    if (future.isSuccess()) {
                        if (future.get()) {
                            return;
                        }
                        cause = UnhealthyChannelException.INSTANCE;
                    } else {
                        cause = future.cause();
                    }

                    this.exceptionCaught(context, cause);
                });

                return;
            }
            if (event instanceof RntbdContext) {
                this.contextFuture.complete((RntbdContext) event);
                this.removeContextNegotiatorAndFlushPendingWrites(context);
                return;
            }
            if (event instanceof RntbdContextException) {
                this.contextFuture.completeExceptionally((RntbdContextException) event);
                context.pipeline().flush().close();
                return;
            }
            context.fireUserEventTriggered(event);

        } catch (Throwable error) {
            reportIssue(context, "{}: ", event, error);
            this.exceptionCaught(context, error);
        }
    }

    // endregion

    // region ChannelOutboundHandler methods

    /**
     * Called once a bind operation is made.
     *
     * @param context      the {@link ChannelHandlerContext} for which the bind operation is made
     * @param localAddress the {@link SocketAddress} to which it should bound
     * @param promise      the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void bind(final ChannelHandlerContext context, final SocketAddress localAddress, final ChannelPromise promise) {
        this.traceOperation(context, "bind", localAddress);
        context.bind(localAddress, promise);
    }

    /**
     * Called once a close operation is made.
     *
     * @param context the {@link ChannelHandlerContext} for which the close operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void close(final ChannelHandlerContext context, final ChannelPromise promise) {

        this.traceOperation(context, "close");

        if (!this.closingExceptionally) {
            this.completeAllPendingRequestsExceptionally(context, ON_CLOSE);
        } else {
            logger.debug("{} closed exceptionally", context);
        }

        final SslHandler sslHandler = context.pipeline().get(SslHandler.class);

        if (sslHandler != null) {

            try {
                // Netty 4.1.36.Final: SslHandler.closeOutbound must be called before closing the pipeline
                // This ensures that all SSL engine and ByteBuf resources are released
                // This is something that does not occur in the call to ChannelPipeline.close that follows
                sslHandler.closeOutbound();
            } catch (Exception exception) {

                // Netty will throw the following exception here if the outbound SSL connection has been closed already
                // javax.net.ssl.SSLException: SSLEngine closed already
                // Reducing the noise level here because multiple concurrent closes can happen due to race conditions
                // and there is no harm in this case
                if (exception instanceof SSLException) {
                    logger.debug(
                        "SslException when attempting to close the outbound SSL connection: ",
                        exception);
                } else {
                    logger.warn(
                        "Exception when attempting to close the outbound SSL connection: ",
                        exception);

                    throw exception;
                }
            }
        }

        context.close(promise);
    }

    /**
     * Called once a connect operation is made.
     *
     * @param context       the {@link ChannelHandlerContext} for which the connect operation is made
     * @param remoteAddress the {@link SocketAddress} to which it should connect
     * @param localAddress  the {@link SocketAddress} which is used as source on connect
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void connect(
        final ChannelHandlerContext context, final SocketAddress remoteAddress, final SocketAddress localAddress,
        final ChannelPromise promise
    ) {
        this.traceOperation(context, "connect", remoteAddress, localAddress);
        context.connect(remoteAddress, localAddress, promise);
    }

    /**
     * Called once a deregister operation is made from the current registered {@link EventLoop}.
     *
     * @param context the {@link ChannelHandlerContext} for which the deregister operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void deregister(final ChannelHandlerContext context, final ChannelPromise promise) {

        this.traceOperation(context, "deregister");

        if (!this.closingExceptionally) {
            this.completeAllPendingRequestsExceptionally(context, ON_DEREGISTER);
        } else {
            logger.debug("{} deregistered exceptionally", context);
        }

        context.deregister(promise);
    }

    /**
     * Called once a disconnect operation is made.
     *
     * @param context the {@link ChannelHandlerContext} for which the disconnect operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void disconnect(final ChannelHandlerContext context, final ChannelPromise promise) {
        this.traceOperation(context, "disconnect");
        context.disconnect(promise);
    }

    /**
     * Called once a flush operation is made
     * <p>
     * The flush operation will try to flush out all previous written messages that are pending.
     *
     * @param context the {@link ChannelHandlerContext} for which the flush operation is made
     */
    @Override
    public void flush(final ChannelHandlerContext context) {
        this.traceOperation(context, "flush");
        context.flush();
    }

    /**
     * Intercepts {@link ChannelHandlerContext#read}
     *
     * @param context the {@link ChannelHandlerContext} for which the read operation is made
     */
    @Override
    public void read(final ChannelHandlerContext context) {
        this.traceOperation(context, "read");
        context.read();
    }

    /**
     * Called once a write operation is made
     * <p>
     * The write operation will send messages through the {@link ChannelPipeline} which are then ready to be flushed
     * to the actual {@link Channel}. This will occur when {@link Channel#flush} is called.
     *
     * @param context the {@link ChannelHandlerContext} for which the write operation is made
     * @param message the message to write
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void write(final ChannelHandlerContext context, final Object message, final ChannelPromise promise) {

        this.traceOperation(context, "write", message);

        if (message instanceof RntbdRequestRecord) {

            final RntbdRequestRecord record = (RntbdRequestRecord) message;
            this.timestamps.channelWriteAttempted();
            record.setSendingRequestHasStarted();

            context.write(this.addPendingRequestRecord(context, record), promise).addListener(completed -> {
                record.stage(RntbdRequestRecord.Stage.SENT);
                if (completed.isSuccess()) {
                    this.timestamps.channelWriteCompleted();
                }
            });

            return;
        }

        if (message == RntbdHealthCheckRequest.MESSAGE) {

            context.write(RntbdHealthCheckRequest.MESSAGE, promise).addListener(completed -> {
                if (completed.isSuccess()) {
                    this.timestamps.channelPingCompleted();
                }
            });

            return;
        }

        final IllegalStateException error = new IllegalStateException(lenientFormat("message of %s: %s",
                message.getClass(),
                message));

        reportIssue(context, "", error);
        this.exceptionCaught(context, error);
    }

    // endregion

    // region Package private methods

    int pendingRequestCount() {
        return this.pendingRequests.size();
    }

    Optional<RntbdContext> rntbdContext() {
        return Optional.of(this.contextFuture.getNow(null));
    }

    CompletableFuture<RntbdContextRequest> rntbdContextRequestFuture() {
        return this.contextRequestFuture;
    }

    boolean hasRequestedRntbdContext() {
        return this.contextRequestFuture.getNow(null) != null;
    }

    boolean hasRntbdContext() {
        return this.contextFuture.getNow(null) != null;
    }

    boolean isServiceable(final int demand) {
        reportIssueUnless(this.hasRequestedRntbdContext(), this, "Direct TCP context request was not issued");
        final int limit = this.hasRntbdContext() ? this.pendingRequestLimit : Math.min(this.pendingRequestLimit, demand);
        return this.pendingRequests.size() < limit;
    }

    void pendWrite(final ByteBuf out, final ChannelPromise promise) {
        this.pendingWrites.add(out, promise);
    }

    Timestamps snapshotTimestamps() {
        return new Timestamps(this.timestamps);
    }

    // endregion

    // region Private methods

    private RntbdRequestRecord addPendingRequestRecord(final ChannelHandlerContext context, final RntbdRequestRecord record) {

        return this.pendingRequests.compute(record.transportRequestId(), (id, current) -> {

            reportIssueUnless(current == null, context, "id: {}, current: {}, request: {}", record);
            record.pendingRequestQueueSize(pendingRequests.size());

            final Timeout pendingRequestTimeout = record.newTimeout(timeout -> {

                // We don't wish to complete on the timeout thread, but rather on a thread doled out by our executor
                requestExpirationExecutor.execute(record::expire);
            });

            record.whenComplete((response, error) -> {
                this.pendingRequests.remove(id);
                pendingRequestTimeout.cancel();
            });

            return record;

        });
    }

    private void completeAllPendingRequestsExceptionally(
        final ChannelHandlerContext context, final Throwable throwable
    ) {

        reportIssueUnless(!this.closingExceptionally, context, "", throwable);
        this.closingExceptionally = true;

        if (this.pendingWrites != null && !this.pendingWrites.isEmpty()) {
            // an expensive call that fires at least one exceptionCaught event
            this.pendingWrites.releaseAndFailAll(context, throwable);
        }

        if (this.pendingRequests.isEmpty()) {
            return;
        }

        if (!this.contextRequestFuture.isDone()) {
            this.contextRequestFuture.completeExceptionally(throwable);
        }

        if (!this.contextFuture.isDone()) {
            this.contextFuture.completeExceptionally(throwable);
        }

        final int count = this.pendingRequests.size();
        Exception contextRequestException = null;
        String phrase = null;

        if (this.contextRequestFuture.isCompletedExceptionally()) {

            try {
                this.contextRequestFuture.get();
            } catch (final CancellationException error) {
                phrase = "RNTBD context request write cancelled";
                contextRequestException = error;
            } catch (final Exception error) {
                phrase = "RNTBD context request write failed";
                contextRequestException = error;
            } catch (final Throwable error) {
                phrase = "RNTBD context request write failed";
                contextRequestException = new ChannelException(error);
            }

        } else if (this.contextFuture.isCompletedExceptionally()) {

            try {
                this.contextFuture.get();
            } catch (final CancellationException error) {
                phrase = "RNTBD context request read cancelled";
                contextRequestException = error;
            } catch (final Exception error) {
                phrase = "RNTBD context request read failed";
                contextRequestException = error;
            } catch (final Throwable error) {
                phrase = "RNTBD context request read failed";
                contextRequestException = new ChannelException(error);
            }

        } else {

            phrase = "closed exceptionally";
        }

        final String message = lenientFormat("%s %s with %s pending requests", context, phrase, count);
        final Exception cause;

        if (throwable instanceof ClosedChannelException) {

            cause = contextRequestException == null
                ? (ClosedChannelException) throwable
                : contextRequestException;

        } else {

            cause = throwable instanceof Exception
                ? (Exception) throwable
                : new ChannelException(throwable);
        }

        for (RntbdRequestRecord record : this.pendingRequests.values()) {

            final Map<String, String> requestHeaders = record.args().serviceRequest().getHeaders();
            final String requestUri = record.args().physicalAddress().toString();

            final GoneException error = new GoneException(message, cause, null, requestUri);
            BridgeInternal.setRequestHeaders(error, requestHeaders);

            record.completeExceptionally(error);
        }
    }

    /**
     * This method is called for each incoming message of type {@link RntbdResponse} to complete a request.
     *
     * @param context  {@link ChannelHandlerContext} to which this {@link RntbdRequestManager request manager} belongs.
     * @param response the {@link RntbdResponse message} received.
     */
    private void messageReceived(final ChannelHandlerContext context, final RntbdResponse response) {

        final Long transportRequestId = response.getTransportRequestId();

        if (transportRequestId == null) {
            reportIssue(context, "response ignored because its transportRequestId is missing: {}", response);
            return;
        }

        final RntbdRequestRecord requestRecord = this.pendingRequests.get(transportRequestId);

        if (requestRecord == null) {
            logger.debug("response {} ignored because its requestRecord is missing: {}", transportRequestId, response);
            return;
        }

        requestRecord.responseLength(response.getMessageLength());
        requestRecord.stage(RntbdRequestRecord.Stage.RECEIVED);

        final HttpResponseStatus status = response.getStatus();
        final UUID activityId = response.getActivityId();
        final int statusCode = status.code();

        if ((HttpResponseStatus.OK.code() <= statusCode && statusCode < HttpResponseStatus.MULTIPLE_CHOICES.code()) ||
            statusCode == HttpResponseStatus.NOT_MODIFIED.code()) {

            final StoreResponse storeResponse = response.toStoreResponse(this.contextFuture.getNow(null));
            requestRecord.complete(storeResponse);

        } else {

            // Map response to a CosmosException

            final CosmosException cause;

            // ..Fetch required header values

            final long lsn = response.getHeader(RntbdResponseHeader.LSN);
            final String partitionKeyRangeId = response.getHeader(RntbdResponseHeader.PartitionKeyRangeId);

            // ..Create Error instance

            final CosmosError error = response.hasPayload()
                ? new CosmosError(RntbdObjectMapper.readTree(response))
                : new CosmosError(Integer.toString(statusCode), status.reasonPhrase(), status.codeClass().name());

            // ..Map RNTBD response headers to HTTP response headers

            final Map<String, String> responseHeaders = response.getHeaders().asMap(
                this.rntbdContext().orElseThrow(IllegalStateException::new), activityId
            );

            // ..Create CosmosException based on status and sub-status codes

            final String resourceAddress = requestRecord.args().physicalAddress() != null ?
                requestRecord.args().physicalAddress().toString() : null;

            switch (status.code()) {

                case StatusCodes.BADREQUEST:
                    cause = new BadRequestException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.CONFLICT:
                    cause = new ConflictException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.FORBIDDEN:
                    cause = new ForbiddenException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.GONE:

                    final int subStatusCode = Math.toIntExact(response.getHeader(RntbdResponseHeader.SubStatus));

                    switch (subStatusCode) {
                        case SubStatusCodes.COMPLETING_SPLIT:
                            cause = new PartitionKeyRangeIsSplittingException(error, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.COMPLETING_PARTITION_MIGRATION:
                            cause = new PartitionIsMigratingException(error, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.NAME_CACHE_IS_STALE:
                            cause = new InvalidPartitionException(error, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.PARTITION_KEY_RANGE_GONE:
                            cause = new PartitionKeyRangeGoneException(error, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        default:
                            GoneException goneExceptionFromService =
                                new GoneException(error, lsn, partitionKeyRangeId, responseHeaders);
                            goneExceptionFromService.setIsBasedOn410ResponseFromService();
                            cause = goneExceptionFromService;
                            break;
                    }
                    break;

                case StatusCodes.INTERNAL_SERVER_ERROR:
                    cause = new InternalServerErrorException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.LOCKED:
                    cause = new LockedException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.METHOD_NOT_ALLOWED:
                    cause = new MethodNotAllowedException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.NOTFOUND:
                    cause = new NotFoundException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.PRECONDITION_FAILED:
                    cause = new PreconditionFailedException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.REQUEST_ENTITY_TOO_LARGE:
                    cause = new RequestEntityTooLargeException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.REQUEST_TIMEOUT:
                    Exception inner = new RequestTimeoutException(error, lsn, partitionKeyRangeId, responseHeaders);
                    cause = new GoneException(resourceAddress, error, lsn, partitionKeyRangeId, responseHeaders, inner);
                    break;

                case StatusCodes.RETRY_WITH:
                    cause = new RetryWithException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.SERVICE_UNAVAILABLE:
                    cause = new ServiceUnavailableException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.TOO_MANY_REQUESTS:
                    cause = new RequestRateTooLargeException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.UNAUTHORIZED:
                    cause = new UnauthorizedException(error, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                default:
                    cause = BridgeInternal.createCosmosException(resourceAddress, status.code(), error, responseHeaders);
                    break;
            }
            BridgeInternal.setResourceAddress(cause, resourceAddress);

            requestRecord.completeExceptionally(cause);
        }
    }

    private void removeContextNegotiatorAndFlushPendingWrites(final ChannelHandlerContext context) {

        final RntbdContextNegotiator negotiator = context.pipeline().get(RntbdContextNegotiator.class);
        negotiator.removeInboundHandler();
        negotiator.removeOutboundHandler();

        if (!this.pendingWrites.isEmpty()) {
            this.pendingWrites.writeAndRemoveAll(context);
            context.flush();
        }
    }

    private static void reportIssue(final Object subject, final String format, final Object... args) {
        RntbdReporter.reportIssue(logger, subject, format, args);
    }

    private static void reportIssueUnless(
        final boolean predicate, final Object subject, final String format, final Object... args
    ) {
        RntbdReporter.reportIssueUnless(logger, predicate, subject, format, args);
    }

    private void traceOperation(final ChannelHandlerContext context, final String operationName, final Object... args) {
        logger.debug("{}\n{}\n{}", operationName, context, args);
    }

    // endregion

    // region Types

    private static final class UnhealthyChannelException extends ChannelException {

        static final UnhealthyChannelException INSTANCE = new UnhealthyChannelException();

        private UnhealthyChannelException() {
            super("health check failed");
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    // endregion
}
