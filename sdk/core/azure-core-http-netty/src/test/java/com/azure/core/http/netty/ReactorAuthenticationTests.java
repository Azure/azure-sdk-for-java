// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.AuthorizationChallengeHandler;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.netty.http.client.HttpClient;
import reactor.test.StepVerifier;

import java.net.SocketAddress;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This class tests using {@link AuthorizationChallengeHandler} with {@link NettyAsyncHttpClient}.
 */
public class ReactorAuthenticationTests {
    private static final int PORT = 443;
    private static final String HOST = "localhost";

    // Valid username/password combination.
    private static final String USERNAME = "1";
    private static final String PASSWORD = "1";

    // Invalid username/password combination.
    private static final String INVALID_USERNAME = "2";
    private static final String INVALID_PASSWORD = "2";

    /*
     * Tests that using digest authentication succeeds or fails according to the passed username and password.
     */
    @ParameterizedTest
    @MethodSource("authorizationParametersSupplier")
    public void connectWithDigest(String username, String password, HttpStatusClass expectedCodeClass) {
//        HttpClient client = configureClient(username, password);
//
//        NettyAsyncHttpClient httpClient = new NettyAsyncHttpClient(HttpClient.create().port(PORT));
//        HttpResponse response = httpClient.send(new HttpRequest(com.azure.core.http.HttpMethod.CONNECT, "https://localhost")).block();

//        StepVerifier.create(client.request(HttpMethod.CONNECT).uri(HOST).response())
//            .assertNext(httpClientResponse ->
//                assertEquals(expectedCodeClass, httpClientResponse.status().codeClass()))
//            .verifyComplete();
    }

    /*
     * Tests that using basic authentication succeeds or fails according to the password username and password.
     */
    @ParameterizedTest
    @MethodSource("authorizationParametersSupplier")
    public void connectWithBasic(String username, String password, HttpStatusClass expectedCodeClass) {
        HttpClient client = configureClient(username, password);

        StepVerifier.create(client.request(HttpMethod.CONNECT).uri(HOST).response())
            .assertNext(httpClientResponse ->
                assertEquals(expectedCodeClass, httpClientResponse.status().codeClass()))
            .verifyComplete();
    }

    /*
     * Tests that anonymous authentication always passes if the server allows it.
     */
    @ParameterizedTest
    @MethodSource("authorizationParametersSupplier")
    public void anonymousConnection(String username, String password, HttpStatusClass ignoredCodeClass) {
        HttpClient client = configureClient(username, password);

        StepVerifier.create(client.request(HttpMethod.CONNECT).uri(HOST).response())
            .assertNext(httpClientResponse ->
                assertEquals(HttpStatusClass.SUCCESS, httpClientResponse.status().codeClass()))
            .verifyComplete();
    }

    private static Stream<Arguments> authorizationParametersSupplier() {
        return Stream.of(
            // Correct credentials should result in a successful connection.
            Arguments.of(USERNAME, PASSWORD, HttpStatusClass.SUCCESS),

            // Incorrect credentials should result in a client error being returned.
            Arguments.of(INVALID_USERNAME, INVALID_PASSWORD, HttpStatusClass.CLIENT_ERROR)
        );
    }

    private static HttpClient configureClient(String username, String password) {
        return HttpClient.create()
            .port(PORT)
            .tcpConfiguration(tcpClient -> tcpClient.bootstrap(bootstrap ->
                bootstrap.handler(new AuthorizationChannelInitializer(username, password))));
    }

    /*
     * Helper class which configures the channel that will be used when sending requests with Reactor Netty.
     */
    private static final class AuthorizationChannelInitializer extends ChannelInitializer<SocketChannel> {
        private final String username;
        private final String password;

        AuthorizationChannelInitializer(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            // Creates a default SSL handshake handler.
            SslHandler sslHandler = SslContextBuilder.forClient().build().newHandler(ch.alloc());

            // Creates a handler which deals with authorization challenges.
            AuthorizationChannelHandler channelHandler = new AuthorizationChannelHandler(username, password);

            // Have the pipeline handle the SSL handshake before dealing with authorization challenges.
            ch.pipeline().addFirst(sslHandler, channelHandler);
        }
    }

    /*
     * Helper class which performs authentication.
     */
    private static final class AuthorizationChannelHandler extends ChannelDuplexHandler {
        private final ChannelFutureListener connectListener = channelFuture -> {
            if (!channelFuture.isSuccess()) {
                failConnection(channelFuture.cause());
            }
        };

        private final AuthorizationChallengeHandler challengeHandler;

        private volatile ChannelHandlerContext ctx;
        private volatile boolean finished;

        AuthorizationChannelHandler(String username, String password) {
            this.challengeHandler = new AuthorizationChallengeHandler(username, password);
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) {
            ctx.connect(remoteAddress, localAddress, promise).addListener(connectListener);
        }

        @Override
        public void read(ChannelHandlerContext ctx) {
            ctx.read();
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            ctx.write(msg, promise);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }

        private void failConnection(Throwable cause) {
            finished = true;
            ctx.fireExceptionCaught(cause);
            ctx.close();
        }
    }
}
