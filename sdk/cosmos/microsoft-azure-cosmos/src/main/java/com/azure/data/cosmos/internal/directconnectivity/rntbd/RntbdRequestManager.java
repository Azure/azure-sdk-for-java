// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.azure.data.cosmos.BadRequestException;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.ConflictException;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosError;
import com.azure.data.cosmos.ForbiddenException;
import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.InternalServerErrorException;
import com.azure.data.cosmos.InvalidPartitionException;
import com.azure.data.cosmos.LockedException;
import com.azure.data.cosmos.MethodNotAllowedException;
import com.azure.data.cosmos.NotFoundException;
import com.azure.data.cosmos.PartitionIsMigratingException;
import com.azure.data.cosmos.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.PartitionKeyRangeIsSplittingException;
import com.azure.data.cosmos.PreconditionFailedException;
import com.azure.data.cosmos.RequestEntityTooLargeException;
import com.azure.data.cosmos.RequestRateTooLargeException;
import com.azure.data.cosmos.RequestTimeoutException;
import com.azure.data.cosmos.RetryWithException;
import com.azure.data.cosmos.ServiceUnavailableException;
import com.azure.data.cosmos.UnauthorizedException;
import com.azure.data.cosmos.internal.directconnectivity.StoreResponse;
import com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import com.google.common.base.Strings;
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
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.Timeout;
import io.netty.util.concurrent.EventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdReporter.reportIssue;
import static com.azure.data.cosmos.internal.directconnectivity.rntbd.RntbdReporter.reportIssueUnless;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public final class RntbdRequestManager implements ChannelHandler, ChannelInboundHandler, ChannelOutboundHandler {

    // region Fields

    private static final Logger logger = LoggerFactory.getLogger(RntbdRequestManager.class);

    private final CompletableFuture<RntbdContext> contextFuture = new CompletableFuture<>();
    private final CompletableFuture<RntbdContextRequest> contextRequestFuture = new CompletableFuture<>();
    private final ConcurrentHashMap<Long, RntbdRequestRecord> pendingRequests;
    private final int pendingRequestLimit;

    private boolean closingExceptionally = false;
    private CoalescingBufferQueue pendingWrites;

    // endregion

    public RntbdRequestManager(int capacity) {
        checkArgument(capacity > 0, "capacity: %s", capacity);
        this.pendingRequests = new ConcurrentHashMap<>(capacity);
        this.pendingRequestLimit = capacity;
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
     * Completes all pending requests exceptionally when a channel reaches the end of its lifetime
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

    @Override
    public void channelRead(final ChannelHandlerContext context, final Object message) {

        this.traceOperation(context, "channelRead");

        if (message instanceof RntbdResponse) {

            try {
                this.messageReceived(context, (RntbdResponse)message);
            } catch (Throwable throwable) {
                reportIssue(logger, context, "{} ", message, throwable);
                this.exceptionCaught(context, throwable);
            } finally {
                ReferenceCountUtil.release(message);
            }

        } else {

            final IllegalStateException error = new IllegalStateException(
                Strings.lenientFormat("expected message of %s, not %s: %s",
                    RntbdResponse.class, message.getClass(), message
                )
            );

            reportIssue(logger, context, "", error);
            this.exceptionCaught(context, error);
        }
    }

    /**
     * Invoked when the last message read by the current read operation has been consumed
     * <p>
     * If {@link ChannelOption#AUTO_READ} is off, no further attempt to read an inbound data from the current
     * {@link Channel} will be made until {@link ChannelHandlerContext#read} is called. This leaves time
     * for outbound messages to be written.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelReadComplete(final ChannelHandlerContext context) {
        this.traceOperation(context, "channelReadComplete");
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

        checkState(this.pendingWrites == null, "pendingWrites: %s", this.pendingWrites);
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

        checkState(this.pendingWrites != null, "pendingWrites: null");
        this.completeAllPendingRequestsExceptionally(context, ClosedWithPendingRequestsException.INSTANCE);
        this.pendingWrites = null;

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

            reportIssueUnless(cause != ClosedWithPendingRequestsException.INSTANCE, logger, context,
                "expected an exception other than ", ClosedWithPendingRequestsException.INSTANCE);

            this.completeAllPendingRequestsExceptionally(context, cause);
            context.pipeline().flush().close();
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
            if (event instanceof RntbdContext) {
                this.contextFuture.complete((RntbdContext)event);
                this.removeContextNegotiatorAndFlushPendingWrites(context);
                return;
            }
            if (event instanceof RntbdContextException) {
                this.contextFuture.completeExceptionally((RntbdContextException)event);
                context.pipeline().flush().close();
                return;
            }
            context.fireUserEventTriggered(event);

        } catch (Throwable error) {
            reportIssue(logger, context, "{}: ", event, error);
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

        this.completeAllPendingRequestsExceptionally(context, ClosedWithPendingRequestsException.INSTANCE);
        final SslHandler sslHandler = context.pipeline().get(SslHandler.class);

        if (sslHandler != null) {
            // Netty 4.1.36.Final: SslHandler.closeOutbound must be called before closing the pipeline
            // This ensures that all SSL engine and ByteBuf resources are released
            // This is something that does not occur in the call to ChannelPipeline.close that follows
            sslHandler.closeOutbound();
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
     * @param context the {@link ChannelHandlerContext} for which the close operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     */
    @Override
    public void deregister(final ChannelHandlerContext context, final ChannelPromise promise) {
        this.traceOperation(context, "deregister");
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

        // TODO: DANOBLE: Ensure that all write errors are reported with a root cause of type EncoderException

        this.traceOperation(context, "write", message);

        if (message instanceof RntbdRequestRecord) {

            context.write(this.addPendingRequestRecord(context, (RntbdRequestRecord)message), promise);

        } else {

            final IllegalStateException error = new IllegalStateException(
                Strings.lenientFormat("expected message of %s, not %s: %s",
                    RntbdRequestRecord.class, message.getClass(), message
                )
            );

            reportIssue(logger, context, "", error);
            this.exceptionCaught(context, error);
        }
    }

    // endregion

    // region Private and package private methods

    CompletableFuture<RntbdContextRequest> getRntbdContextRequestFuture() {
        return this.contextRequestFuture;
    }

    boolean hasRntbdContext() {
        return this.contextFuture.getNow(null) != null;
    }

    boolean isServiceable(final int demand) {
        final int limit = this.hasRntbdContext() ? this.pendingRequestLimit : Math.min(this.pendingRequestLimit, demand);
        return this.pendingRequests.size() < limit;
    }

    void pendWrite(final ByteBuf out, final ChannelPromise promise) {
        this.pendingWrites.add(out, promise);
    }

    private RntbdRequestArgs addPendingRequestRecord(final ChannelHandlerContext context, final RntbdRequestRecord record) {

        return this.pendingRequests.compute(record.getTransportRequestId(), (id, current) -> {

            reportIssueUnless(current == null, logger, context, "id: {}, current: {}, request: {}", id, current, record);

            final Timeout pendingRequestTimeout = record.newTimeout(timeout -> {

                // We don't wish to complete on the timeout thread, but rather on a thread doled out by our executor

                EventExecutor executor = context.executor();

                if (executor.inEventLoop()) {
                    record.expire();
                } else {
                    executor.next().execute(record::expire);
                }
            });

            record.whenComplete((response, error) -> {
                this.pendingRequests.remove(id);
                pendingRequestTimeout.cancel();
            });

            return record;

        }).getArgs();
    }

    private Optional<RntbdContext> getRntbdContext() {
        return Optional.of(this.contextFuture.getNow(null));
    }

    private void completeAllPendingRequestsExceptionally(final ChannelHandlerContext context, final Throwable throwable) {

        if (this.closingExceptionally) {

            reportIssueUnless(throwable == ClosedWithPendingRequestsException.INSTANCE, logger, context,
                "throwable: ", throwable);

            reportIssueUnless(this.pendingRequests.isEmpty() && this.pendingWrites.isEmpty(), logger, context,
                "pendingRequests: {}, pendingWrites: {}", this.pendingRequests.isEmpty(),
                this.pendingWrites.isEmpty());

            return;
        }

        this.closingExceptionally = true;

        if (!this.pendingWrites.isEmpty()) {
            this.pendingWrites.releaseAndFailAll(context, ClosedWithPendingRequestsException.INSTANCE);
        }

        if (!this.pendingRequests.isEmpty()) {

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

            final String message = Strings.lenientFormat("%s %s with %s pending requests", context, phrase, count);
            final Exception cause;

            if (throwable == ClosedWithPendingRequestsException.INSTANCE) {

                cause = contextRequestException == null
                    ? ClosedWithPendingRequestsException.INSTANCE
                    : contextRequestException;

            } else {

                cause = throwable instanceof Exception
                    ? (Exception)throwable
                    : new ChannelException(throwable);
            }

            for (RntbdRequestRecord record : this.pendingRequests.values()) {

                final Map<String, String> requestHeaders = record.getArgs().getServiceRequest().getHeaders();
                final String requestUri = record.getArgs().getPhysicalAddress().toString();

                final GoneException error = new GoneException(message, cause, (Map<String, String>)null, requestUri);
                BridgeInternal.setRequestHeaders(error, requestHeaders);

                record.completeExceptionally(error);
            }
        }
    }

    /**
     * This method is called for each incoming message of type {@link StoreResponse} to complete a request
     *
     * @param context  {@link ChannelHandlerContext} encode to which this {@link RntbdRequestManager} belongs
     * @param response the message encode handle
     */
    private void messageReceived(final ChannelHandlerContext context, final RntbdResponse response) {

        final Long transportRequestId = response.getTransportRequestId();

        if (transportRequestId == null) {
            reportIssue(logger, context, "{} ignored because there is no transport request identifier, response");
            return;
        }

        final RntbdRequestRecord pendingRequest = this.pendingRequests.get(transportRequestId);

        if (pendingRequest == null) {
            reportIssue(logger, context, "{} ignored because there is no matching pending request", response);
            return;
        }

        final HttpResponseStatus status = response.getStatus();
        final UUID activityId = response.getActivityId();

        if (HttpResponseStatus.OK.code() <= status.code() && status.code() < HttpResponseStatus.MULTIPLE_CHOICES.code()) {

            final StoreResponse storeResponse = response.toStoreResponse(this.contextFuture.getNow(null));
            pendingRequest.complete(storeResponse);

        } else {

            // Map response to a CosmosClientException

            final CosmosClientException cause;

            // ..Fetch required header values

            final long lsn = response.getHeader(RntbdResponseHeader.LSN);
            final String partitionKeyRangeId = response.getHeader(RntbdResponseHeader.PartitionKeyRangeId);

            // ..Create Error instance

            final CosmosError cosmosError = response.hasPayload() ?
                BridgeInternal.createCosmosError(RntbdObjectMapper.readTree(response)) :
                new CosmosError(Integer.toString(status.code()), status.reasonPhrase(), status.codeClass().name());

            // ..Map RNTBD response headers to HTTP response headers

            final Map<String, String> responseHeaders = response.getHeaders().asMap(
                this.getRntbdContext().orElseThrow(IllegalStateException::new), activityId
            );

            // ..Create CosmosClientException based on status and sub-status codes

            switch (status.code()) {

                case StatusCodes.BADREQUEST:
                    cause = new BadRequestException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.CONFLICT:
                    cause = new ConflictException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.FORBIDDEN:
                    cause = new ForbiddenException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.GONE:

                    final int subStatusCode = Math.toIntExact(response.getHeader(RntbdResponseHeader.SubStatus));

                    switch (subStatusCode) {
                        case SubStatusCodes.COMPLETING_SPLIT:
                            cause = new PartitionKeyRangeIsSplittingException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.COMPLETING_PARTITION_MIGRATION:
                            cause = new PartitionIsMigratingException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.NAME_CACHE_IS_STALE:
                            cause = new InvalidPartitionException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        case SubStatusCodes.PARTITION_KEY_RANGE_GONE:
                            cause = new PartitionKeyRangeGoneException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                        default:
                            cause = new GoneException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                            break;
                    }
                    break;

                case StatusCodes.INTERNAL_SERVER_ERROR:
                    cause = new InternalServerErrorException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.LOCKED:
                    cause = new LockedException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.METHOD_NOT_ALLOWED:
                    cause = new MethodNotAllowedException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.NOTFOUND:
                    cause = new NotFoundException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.PRECONDITION_FAILED:
                    cause = new PreconditionFailedException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.REQUEST_ENTITY_TOO_LARGE:
                    cause = new RequestEntityTooLargeException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.REQUEST_TIMEOUT:
                    cause = new RequestTimeoutException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.RETRY_WITH:
                    cause = new RetryWithException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.SERVICE_UNAVAILABLE:
                    cause = new ServiceUnavailableException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.TOO_MANY_REQUESTS:
                    cause = new RequestRateTooLargeException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                case StatusCodes.UNAUTHORIZED:
                    cause = new UnauthorizedException(cosmosError, lsn, partitionKeyRangeId, responseHeaders);
                    break;

                default:
                    cause = BridgeInternal.createCosmosClientException(status.code(), cosmosError, responseHeaders);
                    break;
            }

            pendingRequest.completeExceptionally(cause);
        }
    }

    private void removeContextNegotiatorAndFlushPendingWrites(final ChannelHandlerContext context) {

        final RntbdContextNegotiator negotiator = context.pipeline().get(RntbdContextNegotiator.class);
        negotiator.removeInboundHandler();
        negotiator.removeOutboundHandler();

        if (!this.pendingWrites.isEmpty()) {
            this.pendingWrites.writeAndRemoveAll(context);
        }
    }

    private void traceOperation(final ChannelHandlerContext context, final String operationName, final Object... args) {
        logger.trace("{}\n{}\n{}", operationName, context, args);
    }

    // endregion

    // region Types

    private static class ClosedWithPendingRequestsException extends RuntimeException {

        static ClosedWithPendingRequestsException INSTANCE = new ClosedWithPendingRequestsException();

        // TODO: DANOBLE: Consider revising strategy for closing an RntbdTransportClient with pending requests
        //  One possibility:
        //  A channel associated with an RntbdTransportClient will not be closed immediately, if there are any pending
        //  requests on it. Instead it will be scheduled to close after the request timeout interval (default: 60s) has
        //  elapsed.
        //  Algorithm:
        //  When the RntbdTransportClient is closed, it closes each of its RntbdServiceEndpoint instances. In turn each
        //  RntbdServiceEndpoint closes its RntbdClientChannelPool. The RntbdClientChannelPool.close method should
        //  schedule closure of any channel with pending requests for later; when the request timeout interval has
        //  elapsed or--ideally--when all pending requests have completed.
        //  Links:
        //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/388987

        private ClosedWithPendingRequestsException() {
            super(null, null, /* enableSuppression */ false, /* writableStackTrace */ false);
        }
    }

    // endregion
}
