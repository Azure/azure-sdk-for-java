// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.ProxyOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.utils.AuthUtils;
import io.clientcore.core.utils.AuthenticateChallenge;
import io.clientcore.core.utils.CoreUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.handler.proxy.ProxyHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static io.clientcore.core.utils.AuthUtils.parseAuthenticationOrAuthorizationHeader;
import static io.clientcore.http.netty4.implementation.Netty4Utility.createCodec;

/**
 * This class handles authorizing requests being sent through a proxy that requires authentication.
 */
public final class Netty4HttpProxyHandler extends ProxyHandler {
    private static final String PROXY_AUTHENTICATION_INFO = "Proxy-Authentication-Info";
    private static final HttpHeaderName PROXY_AUTHENTICATION_INFO_NAME
        = HttpHeaderName.fromString(PROXY_AUTHENTICATION_INFO);

    private static final String NONE = "none";
    private static final String HTTP = "http";

    private static final String CNONCE = "cnonce";
    private static final String NC = "nc";

    // CoreHttpProxyHandler will be created for each network request that is using proxy, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(Netty4HttpProxyHandler.class);

    private final ProxyOptions proxyOptions;
    private final AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder;
    private final HttpClientCodecWrapper wrapper;

    private String authScheme = NONE;
    private String lastUsedAuthorizationHeader;

    private HttpResponseStatus status;
    private HttpHeaders innerHeaders;

    /**
     * Creates an instance of HttpProxyHandler.
     *
     * @param proxyOptions The configured {@link ProxyOptions} that will dictate the behavior of proxying.
     * @param proxyChallengeHolder An {@link AtomicReference} containing parsed {@code Proxy-Authenticate} challenges.
     */
    public Netty4HttpProxyHandler(ProxyOptions proxyOptions,
        AtomicReference<List<AuthenticateChallenge>> proxyChallengeHolder) {
        super(proxyOptions.getAddress());

        this.proxyOptions = proxyOptions;
        this.proxyChallengeHolder = proxyChallengeHolder;
        this.wrapper = new HttpClientCodecWrapper();
    }

    // Cannot share instances of CoreHttpProxyHandler across ChannelPipelines.
    @Override
    public boolean isSharable() {
        return false;
    }

    @Override
    public String protocol() {
        return HTTP;
    }

    @Override
    public String authScheme() {
        return authScheme;
    }

    @Override
    protected void addCodec(ChannelHandlerContext ctx) {
        // TODO (alzimmer): Need to support HTTP/2 proxying. Check (issue 12088) if Netty itself even supports this.
        ctx.pipeline().addBefore(ctx.name(), Netty4HandlerNames.PROXY_CODEC, this.wrapper);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext ctx) {
        this.wrapper.codec.removeOutboundHandler();
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext ctx) {
        this.wrapper.codec.removeInboundHandler();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) throws ProxyConnectException {
        // This needs to handle no authorization proxying.
        InetSocketAddress destinationAddress = this.destinationAddress();
        String hostString = HttpUtil.formatHostnameForHttp(destinationAddress);
        int port = destinationAddress.getPort();
        String url = hostString + ":" + port;
        String hostHeader = (port == 80 || port == 443) ? url : hostString;
        FullHttpRequest request
            = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url, Unpooled.EMPTY_BUFFER, false);

        request.headers().set(HttpHeaderNames.HOST, hostHeader);

        // If proxy authorization was already attempted with this handler attempt to use the previous authorization
        // again. This should be safe as this handler will always attempt to proxy against the same host, meaning there
        // is one of two outcomes here:
        // 1. The previous authorization is still valid and will result in another successful proxy connection.
        // 2. The previous authorization has gone stale and will result in the slower CONNECT attempt followed by
        //    processing the response for authorization.
        //
        // If proxy authorization wasn't already attempt but challenges have already been received, or in other terms
        // a CONNECT was attempted but failed on authorization requirements, create and cache the current authorization
        // header.
        //
        // If a CONNECT hasn't been attempted, where the current authorization header and challenges will be null, just
        // send a CONNECT request. If the proxy doesn't require authorization it should succeed. If the proxy does
        // require authorization it should send back a 407 with Proxy-Authenticate headers that will be parsed and used
        // to create an authorization header.
        List<AuthenticateChallenge> authenticateChallenges = proxyChallengeHolder.get();
        if (!CoreUtils.isNullOrEmpty(authenticateChallenges)) {
            Map.Entry<String, AuthenticateChallenge> handledChallenge = proxyOptions.getChallengeHandler()
                .handleChallenge("CONNECT", URI.create(hostHeader), authenticateChallenges);
            lastUsedAuthorizationHeader = handledChallenge.getKey();
            authScheme = handledChallenge.getValue().getScheme();

            request.headers().set(HttpHeaderNames.PROXY_AUTHORIZATION, lastUsedAuthorizationHeader);
        }

        if (ctx.channel().isActive()) {
            return request;
        } else {
            throw LOGGER.throwableAtError()
                .log(exceptionMessage("Channel became inactive before 'newInitialMessage' was sent"),
                    m -> new HttpProxyHandler.HttpProxyConnectException(m, null));
        }
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object o) throws ProxyConnectException {
        if (o instanceof HttpResponse) {
            if (status != null) {
                throw LOGGER.throwableAtWarning().log("Received too many responses for a request", CoreException::from);
            }

            HttpResponse response = (HttpResponse) o;
            status = response.status();
            innerHeaders = response.headers();

            if (response.status().code() == 407) {
                /*
                 * Attempt to CONNECT to the proxy resulted in a request for authentication, parse the
                 * Proxy-Authenticate headers returned for challenges and update the ChallengeHolder reference. This
                 * will allow subsequent requests to CONNECT to this proxy to use the challenges returned to create a
                 * Proxy-Authorization header.
                 */
                proxyChallengeHolder.set(extractChallengesFromHeaders(response.headers()));
            } else if (response.status().code() == 200) {
                /*
                 * Attempt to CONNECT to the proxy succeeded, retrieve the Proxy-Authorization header passed in the
                 * Channel's attributes to compare it against a potential Proxy-Authentication-Info response header.
                 * This header is used by the server to validate to the client that it received the correct request.
                 */
                validateProxyAuthenticationInfo(
                    Netty4Utility.get(response.headers(), PROXY_AUTHENTICATION_INFO, PROXY_AUTHENTICATION_INFO_NAME),
                    lastUsedAuthorizationHeader);
            }
        }

        boolean responseComplete = o instanceof LastHttpContent;
        if (responseComplete) {
            if (status == null) {
                throw LOGGER.throwableAtError()
                    .log(exceptionMessage("missing response"),
                        m -> new HttpProxyHandler.HttpProxyConnectException(m, innerHeaders));
            } else if (status.code() != 200) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("status", status.code())
                    .log(exceptionMessage(status.reasonPhrase()),
                        m -> new HttpProxyHandler.HttpProxyConnectException(m, innerHeaders));
            }
        }

        return responseComplete;
    }

    /*
     * Search the response HttpHeaders for Proxy-Authenticate headers.
     */
    private static List<AuthenticateChallenge> extractChallengesFromHeaders(HttpHeaders headers) {
        List<String> proxyAuthenticateValues
            = Netty4Utility.getAll(headers, HttpHeaderNames.PROXY_AUTHENTICATE, HttpHeaderName.PROXY_AUTHENTICATE);

        List<AuthenticateChallenge> challenges = new ArrayList<>();
        for (String proxyAuthenticateValue : proxyAuthenticateValues) {
            challenges.addAll(AuthUtils.parseAuthenticateHeader(proxyAuthenticateValue));
        }

        return challenges;
    }

    /*
     * Validate the Proxy-Authorization header used in authentication against the server's Proxy-Authentication-Info
     * header. This header is an optional return by the server so this validation is guaranteed to happen.
     */
    static void validateProxyAuthenticationInfo(String infoHeader, String authorizationHeader) {
        // Client didn't send 'Proxy-Authorization' or server didn't return a 'Proxy-Authentication-Info' header,
        // nothing to validate.
        if (CoreUtils.isNullOrEmpty(authorizationHeader) || CoreUtils.isNullOrEmpty(infoHeader)) {
            return;
        }

        Map<String, String> authenticationInfoPieces = parseAuthenticationOrAuthorizationHeader(infoHeader);
        Map<String, String> authorizationPieces = parseAuthenticationOrAuthorizationHeader(authorizationHeader);

        /*
         * If the authentication info response contains a cnonce or nc value it MUST match the value sent in the
         * authorization header. This is the server performing validation to the client that it received the
         * information.
         */
        validateProxyAuthenticationInfoValue(CNONCE, authenticationInfoPieces, authorizationPieces);
        validateProxyAuthenticationInfoValue(NC, authenticationInfoPieces, authorizationPieces);
    }

    /*
     * Validates that the value received in the 'Proxy-Authentication-Info' matches the value sent in the
     * 'Proxy-Authorization' header. If the values don't match an 'IllegalStateException' will be thrown with a message
     * outlining that the values didn't match.
     */
    private static void validateProxyAuthenticationInfoValue(String name, Map<String, String> authenticationInfoPieces,
        Map<String, String> authorizationPieces) {
        if (authenticationInfoPieces.containsKey(name)) {
            String sentValue = authorizationPieces.get(name);
            String receivedValue = authenticationInfoPieces.get(name);

            if (!receivedValue.equalsIgnoreCase(sentValue)) {
                throw LOGGER.throwableAtError()
                    .addKeyValue("propertyName", name)
                    .addKeyValue("sent", sentValue)
                    .addKeyValue("received", receivedValue)
                    .log(
                        "Property received in the 'Proxy-Authentication-Info' header doesn't match the value sent in the 'Proxy-Authorization' header",
                        IllegalStateException::new);
            }
        }
    }

    private static final class HttpClientCodecWrapper implements ChannelInboundHandler, ChannelOutboundHandler {
        final HttpClientCodec codec = createCodec();

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            codec.handlerAdded(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            codec.handlerRemoved(ctx);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            codec.exceptionCaught(ctx, cause);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            codec.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            codec.channelUnregistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            codec.channelActive(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            codec.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            codec.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            codec.channelReadComplete(ctx);
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            codec.userEventTriggered(ctx, evt);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            codec.channelWritabilityChanged(ctx);
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
            throws Exception {
            codec.bind(ctx, localAddress, promise);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {
            codec.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.close(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            codec.deregister(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            codec.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            codec.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            codec.flush(ctx);
        }
    }
}
