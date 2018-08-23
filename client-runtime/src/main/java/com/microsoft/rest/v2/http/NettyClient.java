/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import com.microsoft.rest.v2.util.FlowableUtil;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.reactivex.Flowable;
import io.reactivex.FlowableSubscriber;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.fuseable.SimplePlainQueue;
import io.reactivex.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.internal.subscriptions.SubscriptionHelper;
import io.reactivex.internal.util.BackpressureHelper;
import io.reactivex.plugins.RxJavaPlugins;

import static com.microsoft.rest.v2.util.FlowableUtil.ensureLength;

/**
 * An HttpClient that is implemented using Netty.
 */
public final class NettyClient extends HttpClient {
    private final NettyAdapter adapter;
    private final HttpClientConfiguration configuration;

    /**
     * Creates NettyClient.
     *
     * @param configuration
     *            the HTTP client configuration.
     * @param adapter
     *            the adapter to Netty
     */
    private NettyClient(HttpClientConfiguration configuration, NettyAdapter adapter) {
        this.adapter = adapter;
        this.configuration = configuration != null ? configuration : new HttpClientConfiguration(null);
    }

    @Override
    public Single<HttpResponse> sendRequestAsync(final HttpRequest request) {
        return adapter.sendRequestInternalAsync(request, configuration);
    }

    private static final class NettyAdapter {
        private static final String EPOLL_GROUP_CLASS_NAME = "io.netty.channel.epoll.EpollEventLoopGroup";
        private static final String EPOLL_SOCKET_CLASS_NAME = "io.netty.channel.epoll.EpollSocketChannel";

        private static final String KQUEUE_GROUP_CLASS_NAME = "io.netty.channel.kqueue.KQueueEventLoopGroup";
        private static final String KQUEUE_SOCKET_CLASS_NAME = "io.netty.channel.kqueue.KQueueSocketChannel";

        private final MultithreadEventLoopGroup eventLoopGroup;
        private final SharedChannelPool channelPool;

        public Future<?> shutdownGracefully() {
            channelPool.close();
            return eventLoopGroup.shutdownGracefully();
        }

        private static final class TransportConfig {
            final MultithreadEventLoopGroup eventLoopGroup;
            final Class<? extends SocketChannel> channelClass;

            private TransportConfig(MultithreadEventLoopGroup eventLoopGroup,
                    Class<? extends SocketChannel> channelClass) {
                this.eventLoopGroup = eventLoopGroup;
                this.channelClass = channelClass;
            }
        }

        private static MultithreadEventLoopGroup loadEventLoopGroup(String className, int size)
                throws ReflectiveOperationException {
            Class<?> cls = Class.forName(className);
            ThreadFactory factory = new DefaultThreadFactory(cls, true);
            MultithreadEventLoopGroup result = (MultithreadEventLoopGroup) cls
                    .getConstructor(Integer.TYPE, ThreadFactory.class).newInstance(size, factory);
            return result;
        }

        @SuppressWarnings("unchecked")
        private static TransportConfig loadTransport(int groupSize) {
            TransportConfig result = null;
            try {
                final String osName = System.getProperty("os.name");
                if (osName.contains("Linux")) {
                    result = new TransportConfig(loadEventLoopGroup(EPOLL_GROUP_CLASS_NAME, groupSize),
                            (Class<? extends SocketChannel>) Class.forName(EPOLL_SOCKET_CLASS_NAME));
                } else if (osName.contains("Mac")) {
                    result = new TransportConfig(loadEventLoopGroup(KQUEUE_GROUP_CLASS_NAME, groupSize),
                            (Class<? extends SocketChannel>) Class.forName(KQUEUE_SOCKET_CLASS_NAME));
                }
            } catch (Exception e) {
                String message = e.getMessage();
                if (message == null) {
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        message = cause.getMessage();
                    }
                }
                LoggerFactory.getLogger(NettyAdapter.class)
                        .debug("Exception when obtaining native EventLoopGroup and SocketChannel: " + message);
            }

            if (result == null) {
                result = new TransportConfig(
                        new NioEventLoopGroup(groupSize, new DefaultThreadFactory(NioEventLoopGroup.class, true)),
                        NioSocketChannel.class);
            }

            return result;
        }

        private static SharedChannelPool createChannelPool(Bootstrap bootstrap, TransportConfig config,
                int poolSize) {
            bootstrap.group(config.eventLoopGroup);
            bootstrap.channel(config.channelClass);
            bootstrap.option(ChannelOption.AUTO_READ, false);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.MINUTES.toMillis(3L));
            return new SharedChannelPool(bootstrap, new AbstractChannelPoolHandler() {
                @Override
                public synchronized void channelCreated(Channel ch) throws Exception {
                    // Why is it necessary to have "synchronized" to prevent NRE in pipeline().get(Class<T>)?
                    // Is channelCreated not run on the eventLoop assigned to the channel?
                    ch.pipeline().addLast("HttpClientCodec", new HttpClientCodec());
                    ch.pipeline().addLast("HttpClientInboundHandler", new HttpClientInboundHandler());
                }
            }, poolSize);
        }

        private NettyAdapter() {
            TransportConfig config = loadTransport(0);
            this.eventLoopGroup = config.eventLoopGroup;
            this.channelPool = createChannelPool(new Bootstrap(), config, eventLoopGroup.executorCount() * 16);
        }

        private NettyAdapter(Bootstrap baseBootstrap, int eventLoopGroupSize, int channelPoolSize) {
            TransportConfig config = loadTransport(eventLoopGroupSize);
            this.eventLoopGroup = config.eventLoopGroup;
            this.channelPool = createChannelPool(baseBootstrap, config, channelPoolSize);
        }

        private Single<HttpResponse> sendRequestInternalAsync(final HttpRequest request, final HttpClientConfiguration configuration) {
            addHeaders(request);

            // Creates cold observable from an emitter
            return Single.create((SingleEmitter<HttpResponse> responseEmitter) -> {
                AcquisitionListener listener = new AcquisitionListener(channelPool, request, responseEmitter);
                responseEmitter.setDisposable(listener);
                channelPool.acquire(request.url().toURI(), configuration.proxy()).addListener(listener);
            });
        }
    }

    private static void addHeaders(final HttpRequest request) {
        request.withHeader(io.netty.handler.codec.http.HttpHeaderNames.HOST.toString(), request.url().getHost())
               .withHeader(io.netty.handler.codec.http.HttpHeaderNames.CONNECTION.toString(),
                        io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE.toString());
    }

    private static final class AcquisitionListener
            implements GenericFutureListener<Future<? super Channel>>, Disposable {

        private final SharedChannelPool channelPool;
        private final HttpRequest request;
        private final SingleEmitter<HttpResponse> responseEmitter;

        // state is tracked to ensure that any races between write, read,
        // disposal, cancel, and request are properly handled via a serialized state machine.
        private final AtomicInteger state = new AtomicInteger(ACQUIRING_NOT_DISPOSED);

        private static final int MAX_SEND_BUF_SIZE = 1024 * 64;

        private static final int ACQUIRING_NOT_DISPOSED = 0;
        private static final int ACQUIRING_DISPOSED = 1;
        private static final int ACQUIRED_CONTENT_NOT_SUBSCRIBED = 2;
        private static final int ACQUIRED_CONTENT_SUBSCRIBED = 3;
        private static final int ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED = 4;
        private static final int ACQUIRED_DISPOSED_CONTENT_NOT_SUBSCRIBED = 5;
        private static final int CHANNEL_RELEASED = 6;

        // synchronized by `state`
        private Channel channel;

        //synchronized by `state`
        private ResponseContentFlowable content;

        private volatile boolean finishedWritingRequestBody;
        private volatile RequestSubscriber requestSubscriber;

        AcquisitionListener(
                SharedChannelPool channelPool,
                HttpRequest request,
                SingleEmitter<HttpResponse> responseEmitter) {
            this.channelPool = channelPool;
            this.request = request;
            this.responseEmitter = responseEmitter;
        }

        @Override
        public void operationComplete(Future<? super Channel> cf) {
            if (!cf.isSuccess()) {
                emitError(cf.cause());
                return;
            }
            channel = (Channel) cf.getNow();
            while (true) {
                int s = state.get();
                if (s == ACQUIRING_DISPOSED) {
                    if (transition(ACQUIRING_DISPOSED, CHANNEL_RELEASED)) {
                        channelPool.closeAndRelease(channel);
                        return;
                    }
                } else if (s == ACQUIRING_NOT_DISPOSED) {
                    if (transition(ACQUIRING_NOT_DISPOSED, ACQUIRED_CONTENT_NOT_SUBSCRIBED)) {
                        break;
                    }
                } else {
                    return;
                }
            }

            final HttpClientInboundHandler inboundHandler =
                    channel.pipeline().get(HttpClientInboundHandler.class);
            inboundHandler.setFields(responseEmitter, this);
            //TODO do we need a memory barrier here to ensure vis of responseEmitter in other threads?

            try {

                final DefaultHttpRequest raw = createDefaultHttpRequest(request);

                writeRequest(raw);

                if (request.body() == null) {
                    writeBodyEnd();
                } else {
                    requestSubscriber = new RequestSubscriber(inboundHandler);
                    String contentLengthHeader = request.headers().value("content-length");
                    try {
                        long contentLength = Long.parseLong(contentLengthHeader);
                        request.body()
                                .flatMap(bb -> bb.remaining() > MAX_SEND_BUF_SIZE ? FlowableUtil.split(bb, MAX_SEND_BUF_SIZE) : Flowable.just(bb))
                                .compose(ensureLength(contentLength))
                                .subscribe(requestSubscriber);
                    } catch (NumberFormatException e) {
                        String message = String.format(
                                "Content-Length was expected to be a valid long but was \"%s\"", contentLengthHeader);
                        throw new IllegalArgumentException(message, e);
                    }
                }
            } catch (Exception e) {
                emitError(e);
            }
        }
        
        private final class RequestSubscriber implements FlowableSubscriber<ByteBuffer>, GenericFutureListener<Future<Void>> {
            Subscription subscription;

            // we need a done flag because an onNext emission can throw and emit an Error
            // event though the onNext cancels the subscription that is best endeavours for the
            // upstream so we need to be defensive about terminal events that follow
            private boolean done;

            private final HttpClientInboundHandler inboundHandler;

            /**
             * Ensures that requests are only made of upstream once the last write has completed
             * and the channel can be written to synchronously (when isWritable is false writes
             * are buffered).
             */
            private final AtomicInteger writing = new AtomicInteger();

            //states for `writing`
            private static final int WRITE_COMPLETED_WRITABLE = 0;
            private static final int WRITING_WRITABLE = 1;
            private static final int WRITE_COMPLETED_NOT_WRITABLE = 2;
            private static final int WRITING_NOT_WRITABLE = 3;

            RequestSubscriber(HttpClientInboundHandler inboundHandler) {
                this.inboundHandler = inboundHandler;
            }

            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                inboundHandler.requestContentSubscription = subscription;
                subscription.request(1);
            }

            @Override
            public void onNext(ByteBuffer buf) {
                if (done) {
                    return;
                }

                try {
                    writing();
                    // Always dispatching writes on the event loop prevents data corruption on macOS.
                    // Since channel.write always dispatches to the event loop itself if needed internally,
                    // it seems fine to do it here.
                    channel.eventLoop().execute(() -> {
                        try {
                            channel
                                    .write(Unpooled.wrappedBuffer(buf))
                                    .addListener(this);
                        } catch (Exception e) {
                            subscription.cancel();
                            onError(e);
                        }
                        writeComplete();
                        if (writing.get() == WRITE_COMPLETED_NOT_WRITABLE) {
                            channel.flush();
                        }
                    });
                } catch (Exception e) {
                    subscription.cancel();
                    onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                // TODO should we wrap the throwable so that the client
                // knows that the error occurred from the request body?
                if (done) {
                    RxJavaPlugins.onError(t);
                    return;
                }
                done = true;
                emitError(t);
            }

            @Override
            public void onComplete() {
                if (done) {
                    return;
                }
                done = true;
                try {
                    writeBodyEnd();
                } catch (Exception e) {
                    emitError(e);
                }
            }
            
            @Override
            public void operationComplete(Future<Void> future) throws Exception {
                if (!future.isSuccess()) {
                    subscription.cancel();
                    emitError(future.cause());
                }
            }

            private void writing() {
                while (true) {
                    int s = writing.get();
                    if (s == WRITE_COMPLETED_NOT_WRITABLE) {
                        if (writing.compareAndSet(s, WRITING_NOT_WRITABLE)) {
                            break;
                        }
                    } else if (s == WRITE_COMPLETED_WRITABLE) {
                        if (writing.compareAndSet(s, WRITING_WRITABLE)) {
                            break;
                        }
                    } else {
                        throw new RuntimeException("unexpected!");
                    }
                }
            }

            private void writeComplete() {
                while (true) {
                    int s = writing.get();
                    if (s == WRITING_NOT_WRITABLE) {
                        if (writing.compareAndSet(s, WRITE_COMPLETED_NOT_WRITABLE)) {
                            break;
                        }
                    } else if (s == WRITING_WRITABLE) {
                        if (writing.compareAndSet(s, WRITE_COMPLETED_WRITABLE)) {
                            subscription.request(1);
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }

            void channelWritable(boolean writable) {
                while (true) {
                    int s = writing.get();
                    if (writable) {
                        if (s == WRITE_COMPLETED_NOT_WRITABLE) {
                            if (writing.compareAndSet(s, WRITE_COMPLETED_WRITABLE)) {
                                subscription.request(1);
                                break;
                            }
                        } else if (s == WRITING_NOT_WRITABLE) {
                            if (writing.compareAndSet(s, WRITING_WRITABLE)) {
                                break;
                            }
                        } else {
                            break;
                        }
                    } else {
                        if (s == WRITE_COMPLETED_WRITABLE) {
                            if (writing.compareAndSet(s, WRITE_COMPLETED_NOT_WRITABLE)) {
                                break;
                            }
                        } else if (s == WRITING_WRITABLE) {
                            if (writing.compareAndSet(s, WRITING_NOT_WRITABLE)) {
                                break;
                            }
                        } else {
                            break;
                        }
                    }
                }
            }

        }

        private void writeRequest(final DefaultHttpRequest raw) {
            channel.eventLoop().execute(() ->
                channel //
                        .write(raw) //
                        .addListener((Future<Void> future) -> {
                            if (!future.isSuccess()) {
                                emitError(future.cause());
                            }
                        })
            );
        }

        private void writeBodyEnd() {
            channel.eventLoop().execute(() -> channel //
                    .writeAndFlush(DefaultLastHttpContent.EMPTY_LAST_CONTENT) //
                    .addListener((Future<Void> future) -> {
                        if (future.isSuccess()) {
                            finishedWritingRequestBody = true;
                            // reads the response status code and headers and may also read some of the
                            // response body which will be buffered in ResponseContentFlowable
                            channel.read();
                        } else {
                            emitError(future.cause());
                        }
                    }));
        }

        private boolean transition(int from, int to) {
            return state.compareAndSet(from, to);
        }

        void emitError(Throwable throwable) {
            while (true) {
                int s = state.get();
                if (s == ACQUIRING_NOT_DISPOSED) {
                    if (transition(ACQUIRING_NOT_DISPOSED, ACQUIRING_DISPOSED)) {
                        responseEmitter.onError(throwable);
                        break;
                    }
                } else if (s == ACQUIRED_CONTENT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_SUBSCRIBED, ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED)) {
                        content.onError(throwable);
                        break;
                    }
                } else if (s == ACQUIRED_CONTENT_NOT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_NOT_SUBSCRIBED, CHANNEL_RELEASED)) {
                        closeAndReleaseChannel();
                        responseEmitter.onError(throwable);
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        /**
         * Returns false if and only if content subscription should be immediately
         * cancelled.
         *
         * @param content the content that was subscribed to
         * @return false if and only if content subscription should be immediately
         *     cancelled
         */
        boolean contentSubscribed(ResponseContentFlowable content) {
            while (true) {
                int s = state.get();
                if (s == ACQUIRED_CONTENT_NOT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_NOT_SUBSCRIBED, ACQUIRED_CONTENT_SUBSCRIBED)) {
                        this.content = content;
                        return true;
                    }
                } else if (s == ACQUIRED_DISPOSED_CONTENT_NOT_SUBSCRIBED) {
                    if (transition(ACQUIRED_DISPOSED_CONTENT_NOT_SUBSCRIBED, ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED)) {
                        this.content = content;
                        return true;
                    }
                } else {
                    return false;
                }
            }
        }

        private void releaseChannel(boolean cancelled) {
            if (!cancelled && finishedWritingRequestBody) {
                channelPool.release(channel);
            } else {
                closeAndReleaseChannel();
            }
        }

        /**
         * Is called when content flowable terminates or is cancelled.
         *
         **/
        void contentDone(boolean cancelled) {
            while (true) {
                int s = state.get();
                if (s == ACQUIRED_CONTENT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_SUBSCRIBED, CHANNEL_RELEASED)) {
                        releaseChannel(cancelled);
                        return;
                    }
                } else if (s == ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED) {
                    if (transition(ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED, CHANNEL_RELEASED)) {
                        releaseChannel(cancelled);
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        @Override
        public void dispose() {
            while (true) {
                int s = state.get();
                if (s == ACQUIRING_NOT_DISPOSED) {
                    if (transition(ACQUIRING_NOT_DISPOSED, ACQUIRING_DISPOSED)) {
                        // haven't got the channel to be able to release it yet
                        // but check in operationComplete will release it
                        return;
                    }
                } else if (s == ACQUIRING_DISPOSED) {
                    if (transition(ACQUIRING_DISPOSED, CHANNEL_RELEASED)) {
                        closeAndReleaseChannel();
                        return;
                    }
                } else if (s == ACQUIRED_CONTENT_NOT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_NOT_SUBSCRIBED, ACQUIRED_DISPOSED_CONTENT_NOT_SUBSCRIBED)) {
                        return;
                    }
                } else if (s == ACQUIRED_CONTENT_SUBSCRIBED) {
                    if (transition(ACQUIRED_CONTENT_SUBSCRIBED, ACQUIRED_DISPOSED_CONTENT_SUBSCRIBED)) {
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        private void closeAndReleaseChannel() {
            if (channel != null) {
                channelPool.closeAndRelease(channel);
            }
        }

        @Override
        public boolean isDisposed() {
            return state.get() == CHANNEL_RELEASED;
        }

        public void channelWritable(boolean writable) {
            if (requestSubscriber != null) {
                requestSubscriber.channelWritable(writable);
            }
        }

    }

    private static DefaultHttpRequest createDefaultHttpRequest(HttpRequest request) {
        final DefaultHttpRequest raw = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
                HttpMethod.valueOf(request.httpMethod().toString()), request.url().toString());

        for (HttpHeader header : request.headers()) {
            raw.headers().add(header.name(), header.value());
        }
        return raw;
    }


    /**
     * Emits HTTP response content from Netty.
     */
    private static final class ResponseContentFlowable extends Flowable<ByteBuf> implements Subscription {

        // single producer, single consumer queue
        private final SimplePlainQueue<HttpContent> queue = new SpscLinkedArrayQueue<>(16);
        private final Subscription channelSubscription;
        private final AtomicBoolean chunkRequested = new AtomicBoolean(true);
        private final AtomicLong requested = new AtomicLong();

        // work-in-progress counter
        private final AtomicInteger wip = new AtomicInteger(1); // set to 1 to disable drain till we are ready

        // ensures one subscriber only
        private final AtomicBoolean once = new AtomicBoolean();

        private Subscriber<? super ByteBuf> subscriber;

        // can be non-volatile because event methods onReceivedContent,
        // chunkComplete, onError are serialized and is only written and
        // read in those event methods
        private boolean done;

        private volatile boolean cancelled = false;

        // must be volatile otherwise parts of Throwable might not be visible to drain
        // loop (or suffer from word tearing)
        private volatile Throwable err;
        private final AcquisitionListener acquisitionListener;

        ResponseContentFlowable(AcquisitionListener acquisitionListener, Subscription channelSubscription) {
            this.acquisitionListener = acquisitionListener;
            this.channelSubscription = channelSubscription;
        }

        @Override
        protected void subscribeActual(Subscriber<? super ByteBuf> s) {
            if (once.compareAndSet(false, true)) {
                subscriber = s;
                s.onSubscribe(this);

                acquisitionListener.contentSubscribed(this);

                // now that subscriber has been set enable the drain loop
                wip.lazySet(0);

                // we call drain because requests could have happened asynchronously before
                // wip was set to 0 (which enables the drain loop)
                drain();
            } else {
                s.onSubscribe(SubscriptionHelper.CANCELLED);
                s.onError(new IllegalStateException("Multiple subscriptions not allowed for response content"));
            }
        }

        @Override
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(requested, n);
                drain();
            }
        }

        @Override
        public void cancel() {
            cancelled = true;
            channelSubscription.cancel();
            drain();
        }

        //
        // EVENTS - serialized
        //

        void onReceivedContent(HttpContent data) {
            if (done) {
                RxJavaPlugins.onError(new IllegalStateException("data arrived after LastHttpContent"));
                return;
            }
            if (data instanceof LastHttpContent) {
                done = true;
            }
            if (cancelled) {
                data.release();
            } else {
                queue.offer(data);
                drain();
            }
        }

        void chunkCompleted() {
            if (done) {
                return;
            }
            chunkRequested.set(false);
            drain();
        }

        void onError(Throwable cause) {
            if (done) {
                RxJavaPlugins.onError(cause);
            }
            done = true;
            err = cause;
            drain();
        }

        void channelInactive() {
            if (!done) {
                done = true;
                err = new IOException("channel inactive");
                drain();
            }
        }

        //
        // PRIVATE METHODS
        //

        private void requestChunkOfByteBufsFromUpstream() {
            channelSubscription.request(1);
        }

        private void drain() {
            // Below is a non-blocking technique to ensure serialization (in-order
            // processing) of the block inside the if statement and also to ensure
            // no race conditions exist where items on the queue would be missed.
            //
            // wip = `work in progress` and follows a naming convention in RxJava
            //
            // `missed` is a clever little trick to ensure that we only do as many
            // loops as actually required. If `drain` is called say 10 times while
            // the `drain` loop is active then we notice that there are possibly
            // extra items on the queue that arrived just after we found none left
            // (and before the method exits). We don't need to loop around ten times
            // but only once because all items will be picked up from the queue in
            // one additional polling loop.
            if (wip.getAndIncrement() == 0) {
                // need to check cancelled even if there are no requests
                if (cancelled) {
                    releaseQueue();
                    acquisitionListener.contentDone(true);
                    return;
                }
                int missed = 1;
                while (true) {
                    long r = requested.get();
                    long e = 0;
                    while (e != r) {
                        // Note that an error can shortcut the emission of content that is currently on
                        // the queue. This is probably desirable generally because it prevents work that being done
                        // downstream that might be thrown away anyway due to the error.
                        Throwable error = err;
                        if (error != null) {
                            releaseQueue();
                            channelSubscription.cancel();
                            subscriber.onError(error);
                            acquisitionListener.contentDone(true);
                            return;
                        }
                        HttpContent o = queue.poll();
                        if (o != null) {
                            e++;
                            if (emitContent(o)) {
                                return;
                            }
                        } else {
                            // queue is empty so lets see if we need to request another chunk
                            // note that we can only request one chunk at a time because the
                            // method channel.read() ignores calls if a read is pending
                            if (chunkRequested.compareAndSet(false, true)) {
                                requestChunkOfByteBufsFromUpstream();
                            }
                            break;
                        }
                        if (cancelled) {
                            releaseQueue();
                            acquisitionListener.contentDone(true);
                            return;
                        }
                    }
                    if (e > 0) {
                        // it's tempting to use the result of this method to avoid
                        // another volatile read of requested but to avoid race conditions
                        // it's essential that requested is read again AFTER wip is changed.
                        BackpressureHelper.produced(requested, e);
                    }
                    missed = wip.addAndGet(-missed);
                    if (missed == 0) {
                        return;
                    }
                }
            }
        }

        // should only be called from the drain loop
        // returns true if complete
        private boolean emitContent(HttpContent data) {
            subscriber.onNext(data.content());
            if (data instanceof LastHttpContent) {
                // release queue defensively (event serialization and the done flag
                // should mean there are no more items on the queue)
                releaseQueue();
                subscriber.onComplete();
                acquisitionListener.contentDone(false);
                return true;
            } else {
                return false;
            }
        }

        // Should only be called from the drain loop. We want to poll
        // the whole queue and release the contents one by one so we
        // need to honor the single consumer aspect of the Spsc queue
        // to ensure proper visibility of the queued items.
        private void releaseQueue() {
            HttpContent c;
            while ((c = queue.poll()) != null) {
                c.release();
            }
        }
    }

    private static final class ChannelSubscription implements Subscription {

        private final AtomicReference<Channel> channel;
        private final AcquisitionListener acquisitionListener;

        ChannelSubscription(AtomicReference<Channel> channel, AcquisitionListener acquisitionListener) {
            this.channel = channel;
            this.acquisitionListener = acquisitionListener;
        }

        @Override
        public void request(long n) {
            Preconditions.checkArgument(n == 1, "requests must be one at a time!");
            Channel c = channel.get();
            if (c != null) {
                c.read();
            }
        }

        @Override
        public void cancel() {
            acquisitionListener.contentDone(true);
        }
    }

    private static final class HttpClientInboundHandler extends ChannelInboundHandlerAdapter {
        private SingleEmitter<HttpResponse> responseEmitter;

        // TODO does this need to be volatile
        private volatile ResponseContentFlowable contentEmitter;
        private AcquisitionListener acquisitionListener;
        //TODO may not need to be volatile, depends on eventLoop involvement
        private volatile Subscription requestContentSubscription;

        private AtomicReference<Channel> channel = new AtomicReference<Channel>();

        HttpClientInboundHandler() {
        }

        void setFields(SingleEmitter<HttpResponse> responseEmitter, AcquisitionListener acquisitionListener) {
            // this will be called before request has been initiated
            this.responseEmitter = responseEmitter;
            this.acquisitionListener = acquisitionListener;
            this.contentEmitter = new ResponseContentFlowable(
                    acquisitionListener, new ChannelSubscription(channel, acquisitionListener));
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            // TODO can ctx.channel() return a different object at some point in the lifecycle?
            channel.set(ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            acquisitionListener.emitError(cause);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            if (contentEmitter != null) {
                // It doesn't seem like this should be possible since we set this volatile field
                // before we begin writing request content, but it can happen under high load
                contentEmitter.chunkCompleted();
            }
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            acquisitionListener.channelWritable(ctx.channel().isWritable());
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof io.netty.handler.codec.http.HttpResponse) {
                io.netty.handler.codec.http.HttpResponse response = (io.netty.handler.codec.http.HttpResponse) msg;

                if (response.decoderResult().isFailure()) {
                    exceptionCaught(ctx, response.decoderResult().cause());
                    return;
                }

                responseEmitter.onSuccess(new NettyResponse(response, contentEmitter));
            }
            else if (msg instanceof HttpContent) {
                HttpContent content = (HttpContent) msg;

                contentEmitter.onReceivedContent(content);
                if (msg instanceof LastHttpContent) {
                    acquisitionListener.contentDone(false);
                }
            } else {
                exceptionCaught(ctx, new IllegalStateException("Unexpected message type: " + msg.getClass().getName()));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (contentEmitter != null) {
                contentEmitter.channelInactive();
            }
            super.channelInactive(ctx);
        }

    }

    /**
     * The factory for creating a NettyClient.
     */
    public static class Factory implements HttpClientFactory {
        private final NettyAdapter adapter;

        /**
         * Create a Netty client factory with default settings.
         */
        public Factory() {
            this.adapter = new NettyAdapter();
        }

        /**
         * Create a Netty client factory, specifying the event loop group size and the
         * channel pool size.
         *
         * @param baseBootstrap
         *            a channel Bootstrap to use as a basis for channel creation
         * @param eventLoopGroupSize
         *            the number of event loop executors
         * @param channelPoolSize
         *            the number of pooled channels (connections)
         */
        public Factory(Bootstrap baseBootstrap, int eventLoopGroupSize, int channelPoolSize) {
            this.adapter = new NettyAdapter(baseBootstrap.clone(), eventLoopGroupSize, channelPoolSize);
        }

        @Override
        public HttpClient create(final HttpClientConfiguration configuration) {
            return new NettyClient(configuration, adapter);
        }

        @Override
        public void close() {
            adapter.shutdownGracefully().awaitUninterruptibly();
        }
    }
}
