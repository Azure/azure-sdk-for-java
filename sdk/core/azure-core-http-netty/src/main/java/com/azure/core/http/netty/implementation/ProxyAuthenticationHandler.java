// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
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
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHENTICATE;
import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHENTICATION_INFO;
import static com.azure.core.http.AuthorizationChallengeHandler.PROXY_AUTHORIZATION;

/**
 * This class handles authorizing requests being sent through a proxy which require authentication.
 */
public final class ProxyAuthenticationHandler extends ProxyHandler {
    /*
     * AttributeKey used to pass the HTTP request through the channel's attributes.
     */
    public static final AttributeKey<String> REQUEST_METHOD_KEY = AttributeKey.newInstance("RequestMethod");

    /*
     * AttributeKey used to pass the HTTP request URI through the channel's attributes.
     */
    public static final AttributeKey<String> REQUEST_URI_KEY = AttributeKey.newInstance("RequestUri");

    /*
     * AttributeKey used to pass the HTTP request entity body supplier through the channel's attributes.
     */
    public static final AttributeKey<Supplier<byte[]>> REQUEST_ENTITY_BODY_KEY = AttributeKey
        .newInstance("RequestEntityBody");

    /*
     * AttributeKey used to pass the proxy authentication challenge between
     */
    public static final AttributeKey<ChallengeHolder> AUTHENTICATION_CHALLENGE_KEY = AttributeKey
        .newInstance("AuthenticationChallenge");

    private static final AttributeKey<String> PROXY_AUTHORIZATION_KEY = AttributeKey.newInstance("ProxyAuthorization");

    private static final String CNONCE = "cnonce";
    private static final String NC = "nc";

    private final ClientLogger logger = new ClientLogger(ProxyAuthenticationHandler.class);

    private static final String AUTH_BASIC = "basic";
    private static final String AUTH_DIGEST = "digest";

    private final AuthorizationChallengeHandler challengeHandler;
    private final AtomicReference<ChallengeHolder> proxyChallengeHolderReference;
    private final HttpClientCodec codec;
    private final AtomicReference<String> authScheme;

    private HttpResponseStatus status;
    private HttpHeaders inboundHeaders;

    public ProxyAuthenticationHandler(InetSocketAddress proxyAddress, AuthorizationChallengeHandler challengeHandler,
        AtomicReference<ChallengeHolder> proxyChallengeHolderReference) {
        super(proxyAddress);

        this.challengeHandler = challengeHandler;
        this.proxyChallengeHolderReference = proxyChallengeHolderReference;

        this.codec = new HttpClientCodec();
        this.authScheme = new AtomicReference<>();
    }

    @Override
    public String protocol() {
        return "http";
    }

    @Override
    public String authScheme() {
        String scheme = authScheme.get();

        return (scheme == null) ? "none" : scheme;
    }

    @Override
    protected void addCodec(ChannelHandlerContext channelHandlerContext) {
        channelHandlerContext.pipeline().addBefore(channelHandlerContext.name(), null, this.codec);
    }

    @Override
    protected void removeEncoder(ChannelHandlerContext channelHandlerContext) {
        this.codec.removeOutboundHandler();
    }

    @Override
    protected void removeDecoder(ChannelHandlerContext channelHandlerContext) {
        this.codec.removeInboundHandler();
    }

    @Override
    protected Object newInitialMessage(ChannelHandlerContext ctx) {
        // This needs to handle no authorization proxying.
        InetSocketAddress raddr = this.destinationAddress();
        String hostString = HttpUtil.formatHostnameForHttp(raddr);
        int port = raddr.getPort();
        String url = hostString + ":" + port;
        String hostHeader = (port != 80 && port != 443) ? url : hostString;
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url,
            Unpooled.EMPTY_BUFFER, false);

        request.headers().set(HttpHeaderNames.HOST, hostHeader);

        // Proxy is expected to have authentication requirements, attempt to authenticate.
        if (challengeHandler != null) {
            String authorizationHeader = createAuthorizationHeader(ctx.channel());

            if (!CoreUtils.isNullOrEmpty(authorizationHeader)) {
                request.headers().set(PROXY_AUTHORIZATION, authorizationHeader);
                ctx.channel().attr(PROXY_AUTHORIZATION_KEY).set(authorizationHeader);
            }
        }

        return request;
    }

    private String createAuthorizationHeader(Channel channel) {
        String method = channel.attr(REQUEST_METHOD_KEY).get();
        String uri = channel.attr(REQUEST_URI_KEY).get();
        Supplier<byte[]> entityBodySupplier = channel.attr(REQUEST_ENTITY_BODY_KEY).get();

        /*
         * Attempt to pipeline the request.
         *
         * Pipelining is only possible if another request has been authenticated using the
         * AuthenticationChallengeHandler, it attempts to leverage state from the previous authentication request.
         * This may fail and result in the server requesting authentication by returning a proxy authentication
         * challenge.
         */
        String authorizationHeader = challengeHandler.attemptToPipelineAuthorization(method, uri, entityBodySupplier);

        if (!CoreUtils.isNullOrEmpty(authorizationHeader)) {
            return authorizationHeader;
        }

        ChallengeHolder proxyChallengeHolder = proxyChallengeHolderReference.get();

        /*
         * Check if a proxy authentication challenge has been passed to the channel. This occurs when a proxy
         * authentication challenge response is seen by the connection observer and passed back to a future channel
         * created from the same client.
         */
        if (proxyChallengeHolder != null) {
            // Attempt to apply digest challenges, these are preferred over basic authorization.
            List<Map<String, String>> digestChallenges = proxyChallengeHolder.getDigestChallenges();
            if (!CoreUtils.isNullOrEmpty(digestChallenges)) {
                authorizationHeader = challengeHandler.handleDigest(method, uri, digestChallenges, entityBodySupplier);
            }

            // If digest challenges exist or all failed attempt to use basic authorization.
            if (CoreUtils.isNullOrEmpty(authorizationHeader) && proxyChallengeHolder.hasBasicChallenge()) {
                authorizationHeader = challengeHandler.handleBasic();
            }
        }

        return authorizationHeader;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext ctx, Object o) throws Exception{
        if (o instanceof HttpResponse) {
            if (status != null) {
                throw new HttpProxyHandler.HttpProxyConnectException(exceptionMessage("too many responses"), null);
            }

            HttpResponse response = (HttpResponse) o;
            this.status = response.status();
            this.inboundHeaders = response.headers();

            if (response.status().code() == 407) {
                proxyChallengeHolderReference.set(extractChallengesFromHeaders(response.headers()));
            } else if (response.status().code() == 200) {
                consumeProxyAuthenticationInfoHeader(response.headers().get(PROXY_AUTHENTICATION_INFO),
                    ctx.channel().attr(PROXY_AUTHORIZATION_KEY).get());
            }
        }

        boolean finished = o instanceof LastHttpContent;
        if (finished) {
            if (status == null) {
                throw new HttpProxyHandler.HttpProxyConnectException(exceptionMessage("missing response"),
                    inboundHeaders);
            }
            if (status.code() != 200) {
                throw new HttpProxyHandler.HttpProxyConnectException(exceptionMessage("status: " + status),
                    inboundHeaders);
            }
        }

        return finished;
    }

    private static ChallengeHolder extractChallengesFromHeaders(HttpHeaders headers) {
        boolean hasBasicChallenge = false;
        List<Map<String, String>> digestChallenges = new ArrayList<>();

        for (String proxyAuthenticationHeader : headers.getAll(PROXY_AUTHENTICATE)) {
            String[] typeValuePair = proxyAuthenticationHeader.split(" ", 2);

            String challengeType = typeValuePair[0].trim();
            if (challengeType.equalsIgnoreCase(AUTH_BASIC)) {
                hasBasicChallenge = true;
            } else if (challengeType.equalsIgnoreCase(AUTH_DIGEST)) {
                Map<String, String> digestChallenge = new HashMap<>();
                for (String challengePiece : typeValuePair[1].split(",")) {
                    String[] kvp = challengePiece.split("=", 2);
                    if (kvp.length != 2) {
                        continue;
                    }

                    digestChallenge.put(kvp[0].trim(), kvp[1].trim().replace("\"", ""));
                }

                digestChallenges.add(digestChallenge);
            }
        }

        return new ChallengeHolder(hasBasicChallenge, digestChallenges);
    }

    private void consumeProxyAuthenticationInfoHeader(String infoHeader, String authorizationHeader) {
        // Server didn't return a 'Proxy-Authentication-Info' header, nothing to consume.
        if (CoreUtils.isNullOrEmpty(infoHeader)) {
            return;
        }

        Map<String, String> authenticationInfoPieces = AuthorizationChallengeHandler
            .parseChallengeHeader(infoHeader);
        Map<String, String> authorizationPieces = AuthorizationChallengeHandler
            .parseChallengeHeader(authorizationHeader);

        /*
         * If the authentication info response contains a cnonce or nc value it MUST match the value sent in the
         * authorization header. This is the server performing validation to the client that it received the
         * information.
         */
        assert !authenticationInfoPieces.containsKey(CNONCE) ||
            authenticationInfoPieces.get(CNONCE).equals(authorizationPieces.get(CNONCE));
        assert !authenticationInfoPieces.containsKey(NC)
            || authenticationInfoPieces.get(NC).equals(authorizationPieces.get(NC));

        challengeHandler.consumeAuthenticationInfoHeader(authenticationInfoPieces);
    }
}
