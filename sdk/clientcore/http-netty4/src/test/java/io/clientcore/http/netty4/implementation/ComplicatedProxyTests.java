// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.http.netty4.NettyHttpClientTests;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.junit.jupiter.api.RepeatedTest;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.http.netty4.NettyHttpClientTests.uri;
import static io.clientcore.http.netty4.implementation.Netty4Utility.PROGRESS_AND_TIMEOUT_HANDLER_NAME;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;
import static io.clientcore.http.netty4.implementation.Netty4Utility.setOrSuppressError;
import static io.clientcore.http.netty4.implementation.NettyHttpClientLocalTestServer.PROXY_TO_ADDRESS;
import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests a complicated issue being seen in macOS pipelines where
 * {@link NettyHttpClientTests#failedProxyAuthenticationReturnsCorrectError()} fails with
 * {@code StacklessClosedChannelException}. This attempts to replicate the problem without using the full
 * {@code NettyHttpClient}.
 */
public class ComplicatedProxyTests {
    private static final ClientLogger LOGGER = new ClientLogger(ComplicatedProxyTests.class);

    @RepeatedTest(100)
    public void complicatedProxyIssue() {
        try (MockProxyServer mockProxyServer = new MockProxyServer("1", "1")) {
            ChannelInitializationProxyHandler channelInitializationProxyHandler = new ChannelInitializationProxyHandler(
                new ProxyOptions(ProxyOptions.Type.HTTP, mockProxyServer.socketAddress()).setCredentials("2", "2"));
            AtomicReference<List<AuthenticateChallenge>> proxyChallenges = new AtomicReference<>();

            Bootstrap bootstrap = new Bootstrap().channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup(new DefaultThreadFactory("complicated-proxy-issue")))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10_000);
            // Disable auto-read as we want to control when and how data is read from the channel.
            bootstrap.option(ChannelOption.AUTO_READ, false);

            HttpRequest request = new HttpRequest().setUri(uri(PROXY_TO_ADDRESS));

            URI uri = request.getUri();
            String host = uri.getHost();
            int port = uri.getPort() == -1 ? ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80) : uri.getPort();

            // Configure an immutable ChannelInitializer in the builder with everything that can be added on a non-per
            // request basis.
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    ch.pipeline().addFirst(channelInitializationProxyHandler.createProxy(proxyChallenges));

                    // Finally add the HttpClientCodec last as it will need to handle processing request and response
                    // writes and reads for not only the actual request but any proxy or SSL handling.
                    ch.pipeline().addLast(createCodec());
                }
            });

            AtomicReference<Response<BinaryData>> responseReference = new AtomicReference<>();
            AtomicReference<Throwable> errorReference = new AtomicReference<>();
            CountDownLatch latch = new CountDownLatch(1);

            try {
                bootstrap.connect(host, port).addListener((ChannelFutureListener) connectListener -> {
                    if (!connectListener.isSuccess()) {
                        LOGGER.atError().setThrowable(connectListener.cause()).log("Failed to send request");
                        errorReference.set(connectListener.cause());
                        connectListener.channel().close();
                        latch.countDown();
                        return;
                    }

                    Channel channel = connectListener.channel();
                    channel.closeFuture().addListener(closeListener -> {
                        if (!closeListener.isSuccess()) {
                            setOrSuppressError(errorReference, closeListener.cause());
                        }
                    });

                    channel.pipeline()
                        .addLast(PROGRESS_AND_TIMEOUT_HANDLER_NAME,
                            new Netty4ProgressAndTimeoutHandler(null, 60_000, 60_000, 60_000));

                    Netty4ResponseHandler responseHandler
                        = new Netty4ResponseHandler(request, responseReference, errorReference, latch);
                    channel.pipeline().addLast(responseHandler);

                    Throwable earlyError = errorReference.get();
                    if (earlyError != null) {
                        // If an error occurred between the connect and the request being sent, don't proceed with sending
                        // the request.
                        latch.countDown();
                        return;
                    }

                    sendRequest(request, channel, errorReference).addListener((ChannelFutureListener) sendListener -> {
                        if (!sendListener.isSuccess()) {
                            setOrSuppressError(errorReference, sendListener.cause());
                            sendListener.channel().close();
                            latch.countDown();
                        } else {
                            sendListener.channel().read();
                        }
                    });
                });

                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw LOGGER.logThrowableAsError(CoreException.from("Request interrupted", e));
            }

            Response<BinaryData> response = responseReference.get();
            if (response == null) {
                assertInstanceOf(HttpProxyHandler.HttpProxyConnectException.class, errorReference.get());
            } else {
                fail("Request should've failed as the proxy required authentication that wasn't possible.");
            }
        }
    }

    private ChannelFuture sendRequest(HttpRequest request, Channel channel, AtomicReference<Throwable> errorReference)
        throws InterruptedException {
        String uri = request.getUri().toString();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        wrappedHttpHeaders.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri,
            Unpooled.EMPTY_BUFFER, wrappedHttpHeaders, trailersFactory().newHeaders()));
    }
}
