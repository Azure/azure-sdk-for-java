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

package com.azure.data.cosmos.directconnectivity;

import com.google.common.collect.ImmutableMap;
import com.azure.data.cosmos.internal.UserAgentContainer;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdClientChannelInitializer;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdRequestArgs;
import com.azure.data.cosmos.directconnectivity.rntbd.RntbdRequestManager;
import com.azure.data.cosmos.internal.Configs;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslContext;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Single;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.data.cosmos.internal.HttpConstants.HttpHeaders;

final public class RntbdTransportClient extends TransportClient implements AutoCloseable {

    // region Fields

    final private static String className = RntbdTransportClient.class.getName();
    final private static AtomicLong counter = new AtomicLong(0L);
    final private static Logger logger = LoggerFactory.getLogger(className);

    final private AtomicBoolean closed = new AtomicBoolean(false);
    final private EndpointFactory endpointFactory;
    final private String name;

    // endregion

    // region Constructors

    RntbdTransportClient(EndpointFactory endpointFactory) {
        this.name = className + '-' + counter.incrementAndGet();
        this.endpointFactory = endpointFactory;
    }

    RntbdTransportClient(Options options, SslContext sslContext, UserAgentContainer userAgent) {
        this(new EndpointFactory(options, sslContext, userAgent));
    }

    RntbdTransportClient(Configs configs, int requestTimeoutInSeconds, UserAgentContainer userAgent) {
        this(new Options(Duration.ofSeconds((long)requestTimeoutInSeconds)), configs.getSslContext(), userAgent);
    }

    // endregion

    // region Methods

    @Override
    public void close() {

        if (this.closed.compareAndSet(false, true)) {

            this.endpointFactory.close().addListener(future -> {

                if (future.isSuccess()) {

                    // TODO: DANOBLE: Deal with fact that all channels are closed, but each of their sockets are open
                    //  Links:
                    //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/367028
                    //  Notes:
                    //  Observation: Closing/shutting down a channel does not cause its underlying socket to be closed
                    //  Option: Pool SocketChannel instances and manage the SocketChannel used by each NioSocketChannel
                    //  Option: Inherit from NioSocketChannel to ensure the behavior we'd like (close means close)
                    //  Option: Recommend that customers increase their system's file descriptor count (e.g., on macOS)

                    logger.info("{} closed", this);
                    return;
                }

                logger.error("{} close failed: {}", this, future.cause());
            });

        } else {
            logger.debug("{} already closed", this);
        }
    }

    @Override
    public Single<StoreResponse> invokeStoreAsync(
        URI physicalAddress, ResourceOperation unused, RxDocumentServiceRequest request
    ) {
        Objects.requireNonNull(physicalAddress, "physicalAddress");
        Objects.requireNonNull(request, "request");
        this.throwIfClosed();

        final RntbdRequestArgs requestArgs = new RntbdRequestArgs(request, physicalAddress.getPath());
        final Endpoint endpoint = this.endpointFactory.getEndpoint(physicalAddress);

        final CompletableFuture<StoreResponse> responseFuture = endpoint.write(requestArgs);

        return Single.fromEmitter(emitter -> responseFuture.whenComplete((response, error) -> {

            requestArgs.traceOperation(logger, null, "emitSingle", response, error);

            if (error == null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} [physicalAddress: {}, activityId: {}] Request succeeded with response status: {}",
                        endpoint, physicalAddress, request.getActivityId(), response.getStatus()
                    );
                }
                emitter.onSuccess(response);

            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("{} [physicalAddress: {}, activityId: {}] Request failed: {}",
                        endpoint, physicalAddress, request.getActivityId(), error.getMessage()
                    );
                }
                emitter.onError(error);
            }

            requestArgs.traceOperation(logger, null, "completeEmitSingle");
        }));
    }

    @Override
    public String toString() {
        return '[' + name + ", endpointCount: " + this.endpointFactory.endpoints.mappingCount() + ']';
    }

    private void throwIfClosed() {
        if (this.closed.get()) {
            throw new IllegalStateException(String.format("%s is closed", this));
        }
    }

    // endregion

    // region Types

    interface Endpoint {

        Future<?> close();

        CompletableFuture<StoreResponse> write(RntbdRequestArgs requestArgs);
    }

    private static class DefaultEndpoint implements Endpoint {

        final private ChannelFuture channelFuture;
        final private RntbdRequestManager requestManager;

        DefaultEndpoint(EndpointFactory factory, URI physicalAddress) {

            final RntbdClientChannelInitializer clientChannelInitializer = factory.createClientChannelInitializer();
            this.requestManager = clientChannelInitializer.getRequestManager();
            final int connectionTimeout = factory.getConnectionTimeout();

            final Bootstrap bootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(factory.eventLoopGroup)
                .handler(clientChannelInitializer)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true);

            this.channelFuture = bootstrap.connect(physicalAddress.getHost(), physicalAddress.getPort());
        }

        public Future<?> close() {
            return this.channelFuture.channel().close();
        }

        @Override
        public String toString() {
            return this.channelFuture.channel().toString();
        }

        public CompletableFuture<StoreResponse> write(RntbdRequestArgs requestArgs) {

            Objects.requireNonNull(requestArgs, "requestArgs");

            final CompletableFuture<StoreResponse> responseFuture = this.requestManager.createStoreResponseFuture(requestArgs);

            this.channelFuture.addListener((ChannelFuture future) -> {

                if (future.isSuccess()) {
                    requestArgs.traceOperation(logger, null, "doWrite");
                    logger.debug("{} connected", future.channel());
                    doWrite(future.channel(), requestArgs);
                    return;
                }

                UUID activityId = requestArgs.getActivityId();

                if (future.isCancelled()) {

                    this.requestManager.cancelStoreResponseFuture(activityId);

                    logger.debug("{}{} request cancelled: ", future.channel(), requestArgs, future.cause());

                } else {

                    final Channel channel = future.channel();
                    Throwable cause = future.cause();

                    logger.error("{}{} request failed: ", channel, requestArgs, cause);

                    GoneException goneException = new GoneException(
                        String.format("failed to establish connection to %s: %s",
                            channel.remoteAddress(), cause.getMessage()
                        ),
                        cause instanceof Exception ? (Exception)cause : new IOException(cause.getMessage(), cause),
                        ImmutableMap.of(HttpHeaders.ACTIVITY_ID, activityId.toString()),
                        requestArgs.getReplicaPath()
                    );

                    logger.debug("{}{} {} mapped to GoneException: ",
                        channel, requestArgs, cause.getClass(), goneException
                    );

                    this.requestManager.completeStoreResponseFutureExceptionally(activityId, goneException);
                }

            });

            return responseFuture;
        }

        private static void doWrite(Channel channel, RntbdRequestArgs requestArgs) {

            channel.write(requestArgs).addListener((ChannelFuture future) -> {

                requestArgs.traceOperation(logger, null, "writeComplete", future.channel());

                if (future.isSuccess()) {

                    logger.debug("{} request sent: {}", future.channel(), requestArgs);

                } else if (future.isCancelled()) {

                    logger.debug("{}{} request cancelled: {}",
                        future.channel(), requestArgs, future.cause().getMessage()
                    );

                } else {
                    Throwable cause = future.cause();
                    logger.error("{}{} request failed due to {}: {}",
                        future.channel(), requestArgs, cause.getClass(), cause.getMessage()
                    );
                }
            });
        }
    }

    static class EndpointFactory {

        final private ConcurrentHashMap<String, Endpoint> endpoints = new ConcurrentHashMap<>();
        final private NioEventLoopGroup eventLoopGroup;
        final private Options options;
        final private SslContext sslContext;
        final private UserAgentContainer userAgent;

        EndpointFactory(Options options, SslContext sslContext, UserAgentContainer userAgent) {

            Objects.requireNonNull(options, "options");
            Objects.requireNonNull(sslContext, "sslContext");
            Objects.requireNonNull(userAgent, "userAgent");

            final DefaultThreadFactory threadFactory = new DefaultThreadFactory("CosmosEventLoop", true);
            final int threadCount = Runtime.getRuntime().availableProcessors();

            this.eventLoopGroup = new NioEventLoopGroup(threadCount, threadFactory);
            this.options = options;
            this.sslContext = sslContext;
            this.userAgent = userAgent;
        }

        int getConnectionTimeout() {
            return (int)this.options.getOpenTimeout().toMillis();
        }

        Options getOptions() {
            return this.options;
        }

        UserAgentContainer getUserAgent() {
            return this.userAgent;
        }

        Future<?> close() {
            return this.eventLoopGroup.shutdownGracefully();
        }

        RntbdClientChannelInitializer createClientChannelInitializer() {

            final LogLevel logLevel;

            if (RntbdTransportClient.logger.isTraceEnabled()) {
                logLevel = LogLevel.TRACE;
            } else if (RntbdTransportClient.logger.isDebugEnabled()) {
                logLevel = LogLevel.DEBUG;
            } else {
                logLevel = null;
            }

            return new RntbdClientChannelInitializer(this.userAgent, this.sslContext, logLevel, this.options);
        }

        Endpoint createEndpoint(URI physicalAddress) {
            return new DefaultEndpoint(this, physicalAddress);
        }

        void deleteEndpoint(URI physicalAddress) {

            // TODO: DANOBLE: Utilize this method of tearing down unhealthy endpoints
            //  Links:
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331552
            //  https://msdata.visualstudio.com/CosmosDB/_workitems/edit/331593

            final String authority = physicalAddress.getAuthority();
            final Endpoint endpoint = this.endpoints.remove(authority);

            if (endpoint == null) {
                throw new IllegalArgumentException(String.format("physicalAddress: %s", physicalAddress));
            }

            endpoint.close().addListener(future -> {

                if (future.isSuccess()) {
                    logger.info("{} closed channel of communication with {}", endpoint, authority);
                    return;
                }

                logger.error("{} failed to close channel of communication with {}: {}", endpoint, authority, future.cause());
            });
        }

        Endpoint getEndpoint(URI physicalAddress) {
            return this.endpoints.computeIfAbsent(
                physicalAddress.getAuthority(), authority -> this.createEndpoint(physicalAddress)
            );
        }
    }

    final public static class Options {

        // region Fields

        private String certificateHostNameOverride;
        private int maxChannels;
        private int maxRequestsPerChannel;
        private Duration openTimeout = Duration.ZERO;
        private int partitionCount;
        private Duration receiveHangDetectionTime;
        private Duration requestTimeout;
        private Duration sendHangDetectionTime;
        private Duration timerPoolResolution = Duration.ZERO;
        private UserAgentContainer userAgent = null;

        // endregion

        // region Constructors

        public Options(int requestTimeoutInSeconds) {
            this(Duration.ofSeconds((long)requestTimeoutInSeconds));
        }

        public Options(Duration requestTimeout) {

            Objects.requireNonNull(requestTimeout);

            if (requestTimeout.compareTo(Duration.ZERO) <= 0) {
                throw new IllegalArgumentException("requestTimeout");
            }

            this.maxChannels = 0xFFFF;
            this.maxRequestsPerChannel = 30;
            this.partitionCount = 1;
            this.receiveHangDetectionTime = Duration.ofSeconds(65L);
            this.requestTimeout = requestTimeout;
            this.sendHangDetectionTime = Duration.ofSeconds(10L);
        }

        // endregion

        // region Property accessors

        public String getCertificateHostNameOverride() {
            return certificateHostNameOverride;
        }

        public void setCertificateHostNameOverride(String value) {
            this.certificateHostNameOverride = value;
        }

        public int getMaxChannels() {
            return this.maxChannels;
        }

        public void setMaxChannels(int value) {
            this.maxChannels = value;
        }

        public int getMaxRequestsPerChannel() {
            return this.maxRequestsPerChannel;
        }

        public void setMaxRequestsPerChannel(int maxRequestsPerChannel) {
            this.maxRequestsPerChannel = maxRequestsPerChannel;
        }

        public Duration getOpenTimeout() {
            return this.openTimeout.isNegative() || this.openTimeout.isZero() ? this.requestTimeout : this.openTimeout;
        }

        public void setOpenTimeout(Duration value) {
            this.openTimeout = value;
        }

        public int getPartitionCount() {
            return this.partitionCount;
        }

        public void setPartitionCount(int value) {
            this.partitionCount = value;
        }

        public Duration getReceiveHangDetectionTime() {
            return this.receiveHangDetectionTime;
        }

        public void setReceiveHangDetectionTime(Duration value) {
            this.receiveHangDetectionTime = value;
        }

        public Duration getRequestTimeout() {
            return this.requestTimeout;
        }

        public Duration getSendHangDetectionTime() {
            return this.sendHangDetectionTime;
        }

        public void setSendHangDetectionTime(Duration value) {
            this.sendHangDetectionTime = value;
        }

        public Duration getTimerPoolResolution() {
            return calculateTimerPoolResolutionSeconds(this.timerPoolResolution, this.requestTimeout, this.openTimeout);
        }

        public void setTimerPoolResolution(Duration value) {
            this.timerPoolResolution = value;
        }

        public UserAgentContainer getUserAgent() {

            if (this.userAgent != null) {
                return this.userAgent;
            }

            this.userAgent = new UserAgentContainer();
            return this.userAgent;
        }

        public void setUserAgent(UserAgentContainer value) {
            this.userAgent = value;
        }

        // endregion

        // region Methods

        private static Duration calculateTimerPoolResolutionSeconds(

            Duration timerPoolResolution,
            Duration requestTimeout,
            Duration openTimeout) {

            Objects.requireNonNull(timerPoolResolution, "timerPoolResolution");
            Objects.requireNonNull(requestTimeout, "requestTimeout");
            Objects.requireNonNull(openTimeout, "openTimeout");

            if (timerPoolResolution.compareTo(Duration.ZERO) <= 0 && requestTimeout.compareTo(Duration.ZERO) <= 0 &&
                openTimeout.compareTo(Duration.ZERO) <= 0) {

                throw new IllegalStateException("RntbdTransportClient.Options");
            }

            if (timerPoolResolution.compareTo(Duration.ZERO) > 0 && timerPoolResolution.compareTo(openTimeout) < 0 &&
                timerPoolResolution.compareTo(requestTimeout) < 0) {

                return timerPoolResolution;
            }

            if (openTimeout.compareTo(Duration.ZERO) > 0 && requestTimeout.compareTo(Duration.ZERO) > 0) {
                return openTimeout.compareTo(requestTimeout) < 0 ? openTimeout : requestTimeout;
            }

            return openTimeout.compareTo(Duration.ZERO) > 0 ? openTimeout : requestTimeout;
        }

        // endregion
    }

    // endregion
}
