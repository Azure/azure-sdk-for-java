// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.AuthorizationChallengeHandler;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class ProxyAuthenticationHandler extends ProxyHandler {
    /*
     * AttributeKey used to pass the HTTP request through the channel's attributes.
     */
    public static final AttributeKey<String> REQUEST_METHOD_KEY = AttributeKey.newInstance("RequestMethod");

    /*
     * AttributeKey used to pass the HTTP request entity body supplier through the channel's attributes.
     */
    public static final AttributeKey<Supplier<byte[]>> REQUEST_ENTITY_BODY_KEY = AttributeKey
        .newInstance("RequestEntityBody");

    /*
     * AttributeKey used to pass the proxy authentication challenge between
     */
    private static final AttributeKey<ChallengeHolder> AUTHENTICATION_CHALLENGE = AttributeKey
        .newInstance("AuthenticationChallenge");

    private static final String CNONCE = "cnonce";
    private static final String NC = "nc";

    private final ClientLogger logger = new ClientLogger(ProxyAuthenticationHandler.class);

    private static final String AUTH_BASIC = "basic";
    private static final String AUTH_DIGEST = "digest";

    private final AuthorizationChallengeHandler challengeHandler;
    private final HttpClientCodec codec;
    private final AtomicReference<String> authScheme;

    private volatile ChallengeHolder challengeHolder;

    public ProxyAuthenticationHandler(InetSocketAddress proxyAddress, AuthorizationChallengeHandler challengeHandler) {
        super(proxyAddress);

        this.challengeHandler = challengeHandler;
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
    protected Object newInitialMessage(ChannelHandlerContext channelHandlerContext) {
        // This needs to handle no authorization proxying.
        InetSocketAddress raddr = this.destinationAddress();
        String hostString = HttpUtil.formatHostnameForHttp(raddr);
        int port = raddr.getPort();
        String url = hostString + ":" + port;
        String hostHeader = (port != 80 && port != 443) ? url : hostString;
        FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.CONNECT, url,
            Unpooled.EMPTY_BUFFER, false);

        request.headers().set(HttpHeaderNames.HOST, hostHeader);

        // Proxy requires authorization, construct the Proxy-Authorization header.
        if (challengeHandler != null) {
            String proxyAuthorizationHeader = challengeHandler.attemptToPipelineAuthorization(
                channelHandlerContext.channel().attr(REQUEST_METHOD_KEY).get(), url,
                channelHandlerContext.channel().attr(REQUEST_ENTITY_BODY_KEY).get());

            if (!CoreUtils.isNullOrEmpty(proxyAuthorizationHeader)) {
                request.headers().set(HttpHeaderNames.PROXY_AUTHORIZATION, proxyAuthorizationHeader);
            }
        }

        return request;
    }

    private String createAuthorizationHeader(String httpMethod, String uri, Supplier<byte[]> entitySupplier) {
        String header = challengeHandler.attemptToPipelineAuthorization(httpMethod, uri, entitySupplier);

        // If the authorization was able to be pipelined return the generated header.
        if (!CoreUtils.isNullOrEmpty(header)) {
            return header;
        }

        List<Map<String, String>> digestChallenges = challengeHolder.getDigestChallenges();
        boolean allowBasicChallenge = challengeHolder.hasBasicChallenge();

        // Prefer digest challenges over basic challenges.
        if (!CoreUtils.isNullOrEmpty(digestChallenges)) {
            header = challengeHandler.handleDigest(httpMethod, uri, digestChallenges, entitySupplier);
        }

        // If the was no digest challenges or all failed attempt to use basic authorization.
        if (CoreUtils.isNullOrEmpty(header) && allowBasicChallenge) {
            header = challengeHandler.handleBasic();
        }

        return header;
    }

    @Override
    protected boolean handleResponse(ChannelHandlerContext channelHandlerContext, Object o) {
        if (o instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) o;
            if (response.status().code() == 407) {
                channelHandlerContext.channel().attr(AUTHENTICATION_CHALLENGE)
                    .set(extractChallengesFromHeaders(response.headers()));
                this.challengeHolder = extractChallengesFromHeaders(response.headers());
                return false;
            } else if (response.status().code() == 200) {
                //consumeProxyAuthenticationInfoHeader(response.headers());

                return true;
            }
        }

        return false;
    }

    private static ChallengeHolder extractChallengesFromHeaders(HttpHeaders headers) {
        boolean hasBasicChallenge = false;
        List<Map<String, String>> digestChallenges = new ArrayList<>();

        for (String proxyAuthenticationHeader : headers.getAll(HttpHeaderNames.PROXY_AUTHENTICATE)) {
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

    private void consumeProxyAuthenticationInfoHeader(String proxyAuthenticationInfoHeader,
        String proxyAuthorizationHeader) {
        // Server didn't return a 'Proxy-Authentication-Info' header, nothing to consume.
        if (CoreUtils.isNullOrEmpty(proxyAuthenticationInfoHeader)) {
            return;
        }

        Map<String, String> authenticationInfoPieces = AuthorizationChallengeHandler
            .parseChallengeHeader(proxyAuthenticationInfoHeader);
        Map<String, String> authorizationPieces = AuthorizationChallengeHandler
            .parseChallengeHeader(proxyAuthorizationHeader);

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

    private static class ChallengeHolder {
        private final boolean hasBasicChallenge;
        private final List<Map<String, String>> digestChallenges;

        private ChallengeHolder(boolean hasBasicChallenge, List<Map<String, String>> digestChallenges) {
            this.hasBasicChallenge = hasBasicChallenge;
            this.digestChallenges = digestChallenges;
        }

        private boolean hasBasicChallenge() {
            return hasBasicChallenge;
        }

        private List<Map<String, String>> getDigestChallenges() {
            return digestChallenges;
        }
    }
}
