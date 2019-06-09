/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.directconnectivity.rntbd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.Error;
import com.azure.data.cosmos.internal.InternalServerErrorException;
import com.azure.data.cosmos.directconnectivity.ConflictException;
import com.azure.data.cosmos.directconnectivity.ForbiddenException;
import com.azure.data.cosmos.directconnectivity.GoneException;
import com.azure.data.cosmos.directconnectivity.LockedException;
import com.azure.data.cosmos.directconnectivity.MethodNotAllowedException;
import com.azure.data.cosmos.directconnectivity.PartitionKeyRangeGoneException;
import com.azure.data.cosmos.directconnectivity.PreconditionFailedException;
import com.azure.data.cosmos.directconnectivity.RequestEntityTooLargeException;
import com.azure.data.cosmos.directconnectivity.RequestRateTooLargeException;
import com.azure.data.cosmos.directconnectivity.RequestTimeoutException;
import com.azure.data.cosmos.directconnectivity.RetryWithException;
import com.azure.data.cosmos.directconnectivity.ServiceUnavailableException;
import com.azure.data.cosmos.directconnectivity.StoreResponse;
import com.azure.data.cosmos.directconnectivity.UnauthorizedException;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdConstants.RntbdResponseHeader;
import com.azure.data.cosmos.internal.BadRequestException;
import com.azure.data.cosmos.internal.InvalidPartitionException;
import com.azure.data.cosmos.internal.NotFoundException;
import com.azure.data.cosmos.internal.PartitionIsMigratingException;
import com.azure.data.cosmos.internal.PartitionKeyRangeIsSplittingException;
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
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.data.cosmos.internal.HttpConstants.StatusCodes;
import static com.azure.data.cosmos.internal.HttpConstants.SubStatusCodes;

final public class RntbdRequestManager implements ChannelInboundHandler, ChannelOutboundHandler, ChannelHandler {

    // region Fields

    final private static Logger logger = LoggerFactory.getLogger(RntbdRequestManager.class);
    final private CompletableFuture<RntbdContext> contextFuture = new CompletableFuture<>();
    final private CompletableFuture<RntbdContextRequest> contextRequestFuture = new CompletableFuture<>();
    final private ConcurrentHashMap<UUID, PendingRequest> pendingRequests = new ConcurrentHashMap<>();

    private volatile ChannelHandlerContext context;
    private volatile PendingRequest currentRequest;
    private volatile CoalescingBufferQueue pendingWrites;

    // endregion

    // region Request management methods

    /**
     * Cancels the {@link CompletableFuture} for the request message identified by @{code activityId}
     *
     * @param activityId identifies an RNTBD request message
     */
    public void cancelStoreResponseFuture(UUID activityId) {
        Objects.requireNonNull(activityId, "activityId");
        this.removePendingRequest(activityId).getResponseFuture().cancel(true);
    }

    /**
     * Fails a {@link CompletableFuture} for the request message identified by {@code activityId}
     *
     * @param activityId identifies an RNTBD request message
     * @param cause      specifies the cause of the failure
     */
    public void completeStoreResponseFutureExceptionally(UUID activityId, Throwable cause) {
        Objects.requireNonNull(activityId, "activityId");
        Objects.requireNonNull(cause, "cause");
        this.removePendingRequest(activityId).getResponseFuture().completeExceptionally(cause);
    }

    /**
     * Creates a {@link CompletableFuture} of a {@link StoreResponse} for the message identified by {@code activityId}
     *
     * @param requestArgs identifies a request message
     * @return a {@link CompletableFuture} of a {@link StoreResponse}
     */
    public CompletableFuture<StoreResponse> createStoreResponseFuture(RntbdRequestArgs requestArgs) {

        Objects.requireNonNull(requestArgs, "requestArgs");

        this.currentRequest = this.pendingRequests.compute(requestArgs.getActivityId(), (activityId, pendingRequest) -> {

            if (pendingRequest == null) {
                pendingRequest = new PendingRequest(requestArgs);
                logger.trace("{} created new pending request", pendingRequest);
            } else {
                logger.trace("{} renewed existing pending request", pendingRequest);
            }

            return pendingRequest;

        });

        this.traceOperation(logger, this.context, "createStoreResponseFuture");
        return this.currentRequest.getResponseFuture();
    }

    void traceOperation(Logger logger, ChannelHandlerContext context, String operationName, Object... args) {

        if (logger.isTraceEnabled()) {
            
            final long birthTime;
            final BigDecimal lifetime;

            if (this.currentRequest == null) {
                birthTime = System.nanoTime();
                lifetime = BigDecimal.ZERO;
            } else {
                birthTime = this.currentRequest.getBirthTime();
                lifetime = BigDecimal.valueOf(this.currentRequest.getLifetime().toNanos(), 6);
            }

            logger.info("{},{},\"{}({})\",\"{}\",\"{}\"", birthTime, lifetime, operationName, Stream.of(args).map(arg ->
                    arg == null ? "null" : arg.toString()).collect(Collectors.joining(",")
                ), this.currentRequest, context
            );
        }
    }

    // endregion

    // region ChannelInboundHandler methods

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} is now active
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, this.context, "channelActive");
        context.fireChannelActive();
    }

    /**
     * Completes all pending requests exceptionally when a channel reaches the end of its lifetime
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {

        this.traceOperation(logger, this.context, "channelInactive");
        Channel channel = context.channel();

        try {

            this.contextRequestFuture.getNow(null);
            this.contextFuture.getNow(null);

            logger.debug("{} INACTIVE: RNTBD negotiation request status:\nrequest: {}\nresponse: {}",
                channel, this.contextRequestFuture, this.contextFuture
            );

        } catch (CancellationException error) {
            logger.debug("{} INACTIVE: RNTBD negotiation request cancelled:", channel, error);

        } catch (Exception error) {
            logger.error("{} INACTIVE: RNTBD negotiation request failed:", channel, error);
        }

        if (!this.pendingWrites.isEmpty()) {
            this.pendingWrites.releaseAndFailAll(context, new ChannelException("Closed with pending writes"));
        }

        if (!this.pendingRequests.isEmpty()) {

            String reason = String.format("%s Closed with pending requests", channel);
            ChannelException cause = new ChannelException(reason);

            for (PendingRequest pendingRequest : this.pendingRequests.values()) {
                pendingRequest.getResponseFuture().completeExceptionally(cause);
            }

            this.pendingRequests.clear();
        }

        context.fireChannelInactive();
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {

        this.traceOperation(logger, context, "channelRead");

        if (message instanceof RntbdResponse) {
            try {
                this.messageReceived(context, (RntbdResponse)message);
            } finally {
                ReferenceCountUtil.release(message);
            }
            this.traceOperation(logger, context, "messageReceived");
            return;
        }

        String reason = String.format("Expected message of type %s, not %s", RntbdResponse.class, message.getClass());
        throw new IllegalStateException(reason);
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
    public void channelReadComplete(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "channelReadComplete");
        context.fireChannelReadComplete();
    }

    /**
     * Constructs a {@link CoalescingBufferQueue} for buffering encoded requests until we have an {@link RntbdRequest}
     *
     * This method then calls {@link ChannelHandlerContext#fireChannelRegistered()} to forward to the next
     * {@link ChannelInboundHandler} in the {@link ChannelPipeline}.
     * <p>
     * Sub-classes may override this method to change behavior.
     *
     * @param context the {@link ChannelHandlerContext} for which the bind operation is made
     */
    @Override
    public void channelRegistered(ChannelHandlerContext context) throws Exception {

        this.traceOperation(logger, context, "channelRegistered");

        if (!(this.context == null && this.pendingWrites == null)) {
            throw new IllegalStateException();
        };

        this.pendingWrites = new CoalescingBufferQueue(context.channel());
        this.context = context;
        context.fireChannelRegistered();
    }

    /**
     * The {@link Channel} of the {@link ChannelHandlerContext} was unregistered from its {@link EventLoop}
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelUnregistered(ChannelHandlerContext context) throws Exception {

        this.traceOperation(logger, context, "channelUnregistered");

        if (this.context == null || this.pendingWrites == null || !this.pendingWrites.isEmpty()) {
            throw new IllegalStateException();
        };

        this.pendingWrites = null;
        this.context = null;
        context.fireChannelUnregistered();
    }

    /**
     * Gets called once the writable state of a {@link Channel} changed. You can check the state with
     * {@link Channel#isWritable()}.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "channelWritabilityChanged");
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
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {

        // TODO: DANOBLE: replace RntbdRequestManager.exceptionCaught with read/write listeners
        //  Notes:
        //  ChannelInboundHandler.exceptionCaught is deprecated and--today, prior to deprecation--only catches read--
        //  i.e., inbound--exceptions.
        //  Replacements:
        //  * read listener: unclear as there is no obvious replacement
        //  * write listener: implemented by RntbdTransportClient.DefaultEndpoint.doWrite
        //  Links:
        //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/373213

        logger.error("{} closing exceptionally: {}", context.channel(), cause.getMessage());
        traceOperation(logger, context, "exceptionCaught", cause);
        context.close();
    }

    /**
     * Gets called after the {@link ChannelHandler} was added to the actual context and it's ready to handle events.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void handlerAdded(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "handlerAdded");
    }

    /**
     * Gets called after the {@link ChannelHandler} was removed from the actual context and it doesn't handle events
     * anymore.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "handlerRemoved");
    }

    /**
     * Processes inbound events triggered by channel handlers in the {@link RntbdClientChannelInitializer} pipeline
     * <p>
     * ALL but inbound request management events are ignored.
     *
     * @param context {@link ChannelHandlerContext} to which this {@link RntbdRequestManager} belongs
     * @param event   An object representing a user event
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        this.traceOperation(logger, context, "userEventTriggered", event);
        if (event instanceof RntbdContext) {
            this.completeRntbdContextFuture(context, (RntbdContext)event);
            return;
        }
        context.fireUserEventTriggered(event);
    }

    // endregion

    // region ChannelOutboundHandler methods

    /**
     * Called once a bind operation is made.
     *
     * @param context      the {@link ChannelHandlerContext} for which the bind operation is made
     * @param localAddress the {@link SocketAddress} to which it should bound
     * @param promise      the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void bind(ChannelHandlerContext context, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.traceOperation(logger, context, "bind");
        context.bind(localAddress, promise);
    }

    /**
     * Called once a connect operation is made.
     *
     * @param context       the {@link ChannelHandlerContext} for which the connect operation is made
     * @param remoteAddress the {@link SocketAddress} to which it should connect
     * @param localAddress  the {@link SocketAddress} which is used as source on connect
     * @param promise       the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void connect(ChannelHandlerContext context, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.traceOperation(logger, context, "connect");
        context.connect(remoteAddress, localAddress, promise);
    }

    /**
     * Called once a disconnect operation is made.
     *
     * @param context     the {@link ChannelHandlerContext} for which the disconnect operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void disconnect(ChannelHandlerContext context, ChannelPromise promise) throws Exception {
        this.traceOperation(logger, context, "disconnect");
        context.disconnect(promise);
    }

    /**
     * Called once a close operation is made.
     *
     * @param context the {@link ChannelHandlerContext} for which the close operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void close(ChannelHandlerContext context, ChannelPromise promise) throws Exception {
        this.traceOperation(logger, context, "close");
        context.close(promise);
    }

    /**
     * Called once a deregister operation is made from the current registered {@link EventLoop}.
     *
     * @param context the {@link ChannelHandlerContext} for which the close operation is made
     * @param promise the {@link ChannelPromise} to notify once the operation completes
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void deregister(ChannelHandlerContext context, ChannelPromise promise) throws Exception {
        this.traceOperation(logger, context, "deregister");
        context.deregister(promise);
    }

    /**
     * Called once a flush operation is made
     * <p>
     * The flush operation will try to flush out all previous written messages that are pending.
     *
     * @param context the {@link ChannelHandlerContext} for which the flush operation is made
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void flush(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "flush");
        context.flush();
    }

    /**
     * Intercepts {@link ChannelHandlerContext#read}
     *
     * @param context the {@link ChannelHandlerContext} for which the read operation is made
     */
    @Override
    public void read(ChannelHandlerContext context) throws Exception {
        this.traceOperation(logger, context, "read");
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
     * @throws Exception thrown if an error occurs
     */
    @Override
    public void write(ChannelHandlerContext context, Object message, ChannelPromise promise) throws Exception {

        this.traceOperation(logger, context, "write", message);

        if (message instanceof RntbdRequestArgs) {
            this.currentRequest = this.getPendingRequest((RntbdRequestArgs)message);
            context.write(message, promise);
            return;
        }

        String reason = String.format("Expected message of type %s, not %s", RntbdRequestArgs.class, message.getClass());
        throw new IllegalStateException(reason);
    }

    // endregion

    // region Private and package private methods

    CompletableFuture<RntbdContextRequest> getRntbdContextRequestFuture() {
        return this.contextRequestFuture;
    }

    boolean hasRntbdContext() {
        return this.contextFuture.getNow(null) != null;
    }

    void pendWrite(ByteBuf out, ChannelPromise promise) {

        Objects.requireNonNull(out, "out");

        if (this.pendingWrites == null) {
            throw new IllegalStateException("pendingWrites: null");
        }

        this.pendingWrites.add(out, promise);
    }

    private PendingRequest checkPendingRequest(UUID activityId, PendingRequest pendingRequest) {

        if (pendingRequest == null) {
            throw new IllegalStateException(String.format("Pending request not found: %s", activityId));
        }

        if (pendingRequest.getResponseFuture().isDone()) {
            throw new IllegalStateException(String.format("Request is not pending: %s", activityId));
        }

        return pendingRequest;
    }

    private void completeRntbdContextFuture(ChannelHandlerContext context, RntbdContext value) {

        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(value, "value");

        if (this.contextFuture.isDone()) {
            throw new IllegalStateException(String.format("rntbdContextFuture: %s", this.contextFuture));
        }

        contextFuture.complete(value);

        RntbdContextNegotiator negotiator = context.channel().pipeline().get(RntbdContextNegotiator.class);
        negotiator.removeInboundHandler();
        negotiator.removeOutboundHandler();

        if (!pendingWrites.isEmpty()) {
            this.pendingWrites.writeAndRemoveAll(context);
        }
    }

    private PendingRequest getPendingRequest(RntbdRequestArgs args) {
        UUID activityId = args.getActivityId();
        return checkPendingRequest(activityId, this.pendingRequests.get(activityId));
    }

    private Optional<RntbdContext> getRntbdContext() {
        return Optional.of(this.contextFuture.getNow(null));
    }

    /**
     * This method is called for each incoming message of type {@link StoreResponse} to complete a request
     *
     * @param context  {@link ChannelHandlerContext} encode to which this {@link RntbdRequestManager} belongs
     * @param response the message encode handle
     */
    private void messageReceived(ChannelHandlerContext context, RntbdResponse response) {

        final UUID activityId = response.getActivityId();
        final PendingRequest pendingRequest = this.pendingRequests.remove(activityId);

        if (pendingRequest == null) {
            logger.warn("[activityId: {}] no request pending", activityId);
            return;
        }

        final CompletableFuture<StoreResponse> future = pendingRequest.getResponseFuture();
        final HttpResponseStatus status = response.getStatus();

        if (HttpResponseStatus.OK.code() <= status.code() && status.code() < HttpResponseStatus.MULTIPLE_CHOICES.code()) {

            final StoreResponse storeResponse = response.toStoreResponse(this.contextFuture.getNow(null));
            future.complete(storeResponse);

        } else {

            // Map response to a CosmosClientException

            final CosmosClientException cause;

            // ..Fetch required header values

            final long lsn = response.getHeader(RntbdResponseHeader.LSN);
            final String partitionKeyRangeId = response.getHeader(RntbdResponseHeader.PartitionKeyRangeId);

            // ..CREATE Error instance

            final ObjectMapper mapper = new ObjectMapper();
            final Error error;

            if (response.hasPayload()) {

                try (Reader reader = response.getResponseStreamReader()) {

                    error = BridgeInternal.createError((ObjectNode)mapper.readTree(reader));

                } catch (IOException e) {

                    String message = String.format("%s: %s", e.getClass(), e.getMessage());
                    logger.error("{} %s", context.channel(), message);
                    throw new CorruptedFrameException(message);
                }

            } else {
                error = new Error(Integer.toString(status.code()), status.reasonPhrase(), status.codeClass().name());
            }

            // ..Map RNTBD response headers to HTTP response headers

            final Map<String, String> responseHeaders = response.getHeaders().asMap(
                this.getRntbdContext().orElseThrow(IllegalStateException::new), activityId
            );

            // ..CREATE CosmosClientException based on status and sub-status codes

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

                    int subStatusCode = Math.toIntExact(response.getHeader(RntbdResponseHeader.SubStatus));

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
                            cause = new GoneException(error, lsn, partitionKeyRangeId, responseHeaders);
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
                    cause = new RequestTimeoutException(error, lsn, partitionKeyRangeId, responseHeaders);
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
                    cause = new CosmosClientException(status.code(), error, responseHeaders);
                    break;
            }

            logger.trace("{}[activityId: {}, statusCode: {}, subStatusCode: {}] {}",
                context.channel(), cause.message(), cause.statusCode(), cause.subStatusCode(),
                cause.getMessage()
            );

            future.completeExceptionally(cause);
        }
    }

    private PendingRequest removePendingRequest(UUID activityId) {
        PendingRequest pendingRequest = this.pendingRequests.remove(activityId);
        return checkPendingRequest(activityId, pendingRequest);
    }

    // endregion

    // region Types

    private static class PendingRequest {

        private final RntbdRequestArgs args;
        private final CompletableFuture<StoreResponse> responseFuture = new CompletableFuture<>();

        PendingRequest(RntbdRequestArgs args) {
            this.args = args;
        }

        RntbdRequestArgs getArgs() {
            return this.args;
        }

        long getBirthTime() {
            return this.args.getBirthTime();
        }

        Duration getLifetime() {
            return this.args.getLifetime();
        }

        CompletableFuture<StoreResponse> getResponseFuture() {
            return this.responseFuture;
        }

        @Override
        public String toString() {
            return this.args.toString();
        }
    }

    // endregion
}
