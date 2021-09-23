// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.util.AuthorizationChallengeHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.proxy.ProxyConnectException;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.core.util.AuthorizationChallengeHandler.PROXY_AUTHENTICATE;
import static com.azure.core.util.AuthorizationChallengeHandler.PROXY_AUTHENTICATION_INFO;
import static com.azure.core.util.AuthorizationChallengeHandler.PROXY_AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link HttpProxyHandler}.
 */
public class HttpProxyHandlerTests {
    private static final String BASIC = "basic";
    private static final String DIGEST = "digest";

    private static final String DIGEST_CHALLENGE = "Digest realm=\"test realm\", qop=\"auth\", algorithm=SHA-256, "
        + "nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\", "
        + "opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\"";
    private static final Map<String, String> PARSED_DIGEST_CHALLENGE = parseDigestChallenge(DIGEST_CHALLENGE);
    private static final String ORIGINAL_NONCE = "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v";
    private static final String UPDATED_NONCE = "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS";

    @AfterEach
    public void cleanupInlineMocks() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests that constructing {@link HttpProxyHandler} throws a {@link NullPointerException} when passed a
     * {@code null} proxy.
     */
    @Test
    public void nullProxyThrows() {
        assertThrows(NullPointerException.class, () -> new HttpProxyHandler(null, null, null));
    }

    /**
     * Tests that {@link HttpProxyHandler#protocol()} returns {@code "http"}.
     */
    @Test
    public void protocolIsHttp() {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        assertEquals("http", proxyAuthenticationHandler.protocol());
    }

    /**
     * Tests that {@link HttpProxyHandler#authScheme()} is {@code "none"} before applying any authorizations.
     */
    @Test
    public void authSchemeIsNoneBeforeHandlingChallenge() {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        assertEquals("none", proxyAuthenticationHandler.authScheme());
    }

    @ParameterizedTest
    @MethodSource("authSchemeIsDeterminedByAuthorizationTypeSupplier")
    @SuppressWarnings("unchecked")
    public void authSchemeIsDeterminedByAuthorizationType(ChallengeHolder challengeHolder, String expectedAuthScheme)
        throws Exception {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), new AuthorizationChallengeHandler("1", "1"),
            new AtomicReference<>(challengeHolder));

        Attribute<String> attribute = mock(Attribute.class);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.connect(any(), any(), any())).thenReturn(null);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.connect(ctx, new InetSocketAddress("localhost", 80), null, null);
        proxyAuthenticationHandler.newInitialMessage(ctx);
        assertEquals(expectedAuthScheme, proxyAuthenticationHandler.authScheme());
    }

    public static Stream<Arguments> authSchemeIsDeterminedByAuthorizationTypeSupplier() {
        return Stream.of(
            // ChallengeHolder only containing Basic challenge.
            Arguments.of(new ChallengeHolder(true, null), BASIC),

            // ChallengeHolder only containing Digest challenge.
            Arguments.of(new ChallengeHolder(false, Collections.singletonList(PARSED_DIGEST_CHALLENGE)), DIGEST),

            // ChallengeHolder containing both Basic and Digest challenge.
            Arguments.of(new ChallengeHolder(true, Collections.singletonList(PARSED_DIGEST_CHALLENGE)), DIGEST)
        );
    }

    /**
     * Tests that a {@code Proxy-Authorization} isn't applied to a {@code CONNECT} request when {@link
     * HttpProxyHandler} is constructed with a {@code null} {@link AuthorizationChallengeHandler}.
     *
     * <p>
     * A {@code null} {@link AuthorizationChallengeHandler} indicates that the proxy will be connected to anonymously.
     */
    @Test
    public void connectMessageDoesNotHaveAuthorizationWhenUsingAnonymousProxy() throws Exception {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.connect(any(), any(), any())).thenReturn(null);

        proxyAuthenticationHandler.connect(ctx, new InetSocketAddress("localhost", 80), null, null);
        FullHttpRequest request = (FullHttpRequest) proxyAuthenticationHandler.newInitialMessage(null);
        assertFalse(request.headers().contains(AuthorizationChallengeHandler.PROXY_AUTHORIZATION));
    }

    /**
     * Tests that a {@code Proxy-Authorization} isn't applied to a {@code CONNECT} request when a {@code
     * Proxy-Authenticate} challenge hasn't been returned from a server.
     */
    @Test
    public void connectMessageDoesNotHaveAuthorizationBeforeHandlingChallenge() throws Exception {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), new AuthorizationChallengeHandler("1", "1"),
            new AtomicReference<>());

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.connect(any(), any(), any())).thenReturn(null);

        proxyAuthenticationHandler.connect(ctx, new InetSocketAddress("localhost", 80), null, null);
        FullHttpRequest request = (FullHttpRequest) proxyAuthenticationHandler.newInitialMessage(null);
        assertFalse(request.headers().contains(AuthorizationChallengeHandler.PROXY_AUTHORIZATION));
    }

    /**
     * Tests that response handling doesn't consume non {@link HttpResponse} or {@link LastHttpContent} objects.
     */
    @Test
    public void nonHttpResponseIsIgnoredInResponseHandler() throws ProxyConnectException {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        assertFalse(proxyAuthenticationHandler.handleResponse(mock(ChannelHandlerContext.class), "random"));
    }

    /**
     * Tests that receiving multiple {@link HttpResponse HttpResponses} for a single {@code CONNECT} request it throws a
     * {@link RuntimeException}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void multipleHttpResponsesThrowsException() throws ProxyConnectException {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        Attribute<String> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(null);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.OK);
        when(response.headers()).thenReturn(EmptyHttpHeaders.INSTANCE);

        assertFalse(proxyAuthenticationHandler.handleResponse(ctx, response));
        assertThrows(RuntimeException.class, () -> proxyAuthenticationHandler.handleResponse(ctx, response));
    }

    /**
     * Tests that handling a {@link LastHttpContent} response object before receiving a {@link HttpResponse} will throw
     * a {@link ProxyConnectException}.
     */
    @Test
    public void lastHttpContentWithoutResponseThrows() {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        assertThrows(ProxyConnectException.class, () -> proxyAuthenticationHandler
            .handleResponse(mock(ChannelHandlerContext.class), LastHttpContent.EMPTY_LAST_CONTENT));
    }

    /**
     * Tests that handling a {@link LastHttpContent} response object after receiving a non 200 {@link HttpResponse} will
     * throw a {@link ProxyConnectException}.
     */
    @Test
    public void lastHttpContentWithoutValidStatusThrows() throws ProxyConnectException {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), null, null);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.CONFLICT);

        assertFalse(proxyAuthenticationHandler.handleResponse(ctx, response));
        assertThrows(ProxyConnectException.class, () -> proxyAuthenticationHandler
            .handleResponse(ctx, LastHttpContent.EMPTY_LAST_CONTENT));
    }

    /**
     * Tests that receiving a {@link HttpResponse} contains a {@code Proxy-Authenticate} challenge, or challenges, will
     * be captured correctly.
     */
    @ParameterizedTest
    @MethodSource("challengeIsCapturedSupplier")
    public void challengeIsCaptured(List<String> proxyAuthenticateChallenges, ChallengeHolder expected)
        throws ProxyConnectException {
        AtomicReference<ChallengeHolder> proxyChallengeHolder = new AtomicReference<>();
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), new AuthorizationChallengeHandler("1", "1"),
            proxyChallengeHolder);

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.getAll(PROXY_AUTHENTICATE)).thenReturn(proxyAuthenticateChallenges);

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.PROXY_AUTHENTICATION_REQUIRED);
        when(response.headers()).thenReturn(headers);

        assertFalse(proxyAuthenticationHandler.handleResponse(mock(ChannelHandlerContext.class), response));

        ChallengeHolder capturedChallenges = proxyChallengeHolder.get();
        assertNotNull(capturedChallenges);
        assertEquals(expected.hasBasicChallenge(), capturedChallenges.hasBasicChallenge());
        assertIterableEquals(expected.getDigestChallenges(), capturedChallenges.getDigestChallenges());
    }

    public static Stream<Arguments> challengeIsCapturedSupplier() {
        String basicChallenge = "Basic realm\"test realm\"";
        String anotherDigestChallenge = "Digest realm=\"test realm\", qop=\"auth\", algorithm=MD5, nonce=\""
            + "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\", opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\"";

        Map<String, String> anotherParsedDigestChallenge = parseDigestChallenge(anotherDigestChallenge);

        ChallengeHolder basicOnlyHolder = new ChallengeHolder(true, Collections.emptyList());
        ChallengeHolder digestOnlyHolder = new ChallengeHolder(false,
            Collections.singletonList(PARSED_DIGEST_CHALLENGE));
        ChallengeHolder basicAndDigestHolder = new ChallengeHolder(true,
            Collections.singletonList(PARSED_DIGEST_CHALLENGE));
        ChallengeHolder multipleDigestHolder = new ChallengeHolder(false,
            Stream.of(PARSED_DIGEST_CHALLENGE, anotherParsedDigestChallenge).collect(Collectors.toList()));
        ChallengeHolder basicAndMultipleDigestHolder = new ChallengeHolder(true,
            Stream.of(PARSED_DIGEST_CHALLENGE, anotherParsedDigestChallenge).collect(Collectors.toList()));

        return Stream.of(
            // Basic only Proxy-Authenticate challenge.
            Arguments.of(Collections.singletonList(basicChallenge), basicOnlyHolder),

            // Digest only Proxy-Authenticate challenge.
            Arguments.of(Collections.singletonList(DIGEST_CHALLENGE), digestOnlyHolder),

            // Basic and Digest Proxy-Authenticate challenges.
            Arguments.of(Stream.of(basicChallenge, DIGEST_CHALLENGE).collect(Collectors.toList()),
                basicAndDigestHolder),

            // Multiple Digest Proxy-Authenticate challenges.
            Arguments.of(Stream.of(DIGEST_CHALLENGE, anotherDigestChallenge).collect(Collectors.toList()),
                multipleDigestHolder),

            // Basic and multiple Digest Proxy-Authenticate challenges.
            Arguments.of(Stream.of(basicChallenge, DIGEST_CHALLENGE, anotherDigestChallenge)
                .collect(Collectors.toList()), basicAndMultipleDigestHolder)
        );
    }

    /**
     * Tests that when {@link HttpProxyHandler} has a {@link ChallengeHolder} a {@code Proxy-Authorization}
     * header will be applied to the {@code CONNECT} requests, this is determined by the challenges in the holder.
     */
    @ParameterizedTest
    @MethodSource("authorizationIsAppliedSupplier")
    @SuppressWarnings("unchecked")
    public void authorizationIsApplied(ChallengeHolder challengeHolder, Predicate<String> expectedPredicate)
        throws Exception {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), new AuthorizationChallengeHandler("1", "1"),
            new AtomicReference<>(challengeHolder));

        Attribute<String> attribute = mock(Attribute.class);
        ArgumentCaptor<String> setCapture = ArgumentCaptor.forClass(String.class);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.connect(ctx, new InetSocketAddress("localhost", 80), null, null);
        FullHttpRequest fullHttpRequest = (FullHttpRequest) proxyAuthenticationHandler.newInitialMessage(ctx);
        assertNotNull(fullHttpRequest);
        assertTrue(expectedPredicate.test(fullHttpRequest.headers().get(PROXY_AUTHORIZATION)));
        verify(attribute).set(setCapture.capture());
        assertTrue(expectedPredicate.test(setCapture.getValue()));
    }

    public static Stream<Arguments> authorizationIsAppliedSupplier() {
        Predicate<String> basicPredicate = "Basic MTox"::equals;
        Predicate<String> digestPredicate = (authHeader) -> authHeader.startsWith("Digest");

        return Stream.of(
            // ChallengeHolder only containing Basic challenge.
            Arguments.of(new ChallengeHolder(true, null), basicPredicate),

            // ChallengeHolder only containing Digest challenge.
            Arguments.of(new ChallengeHolder(false, Collections.singletonList(PARSED_DIGEST_CHALLENGE)),
                digestPredicate),

            // ChallengeHolder containing both Basic and Digest challenge.
            Arguments.of(new ChallengeHolder(true, Collections.singletonList(PARSED_DIGEST_CHALLENGE)), digestPredicate)
        );
    }

    /**
     * Tests that when the {@link AuthorizationChallengeHandler} has already handled a {@code Proxy-Authenticate}
     * challenge it is capable of pipelining subsequent requests.
     */
    @ParameterizedTest
    @MethodSource("authorizationCanBePipelinedSupplier")
    @SuppressWarnings("unchecked")
    public void authorizationCanBePipelined(AuthorizationChallengeHandler challengeHandler,
        Predicate<String> expectedPredicate) throws Exception {
        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), challengeHandler, new AtomicReference<>());

        Attribute<String> attribute = mock(Attribute.class);
        ArgumentCaptor<String> setCapture = ArgumentCaptor.forClass(String.class);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.connect(ctx, new InetSocketAddress("localhost", 80), null, null);
        FullHttpRequest fullHttpRequest = (FullHttpRequest) proxyAuthenticationHandler.newInitialMessage(ctx);
        assertNotNull(fullHttpRequest);
        assertTrue(expectedPredicate.test(fullHttpRequest.headers().get(PROXY_AUTHORIZATION)));
        verify(attribute).set(setCapture.capture());
        assertTrue(expectedPredicate.test(setCapture.getValue()));
    }

    public static Stream<Arguments> authorizationCanBePipelinedSupplier() {
        AuthorizationChallengeHandler basicChallengeHandler = new AuthorizationChallengeHandler("1", "1");
        basicChallengeHandler.handleBasic();
        Predicate<String> basicPredicate = "Basic MTox"::equals;

        AuthorizationChallengeHandler digestChallengeHandler = new AuthorizationChallengeHandler("1", "1");
        digestChallengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);
        Predicate<String> digestPredicate = (authHeader) -> authHeader.startsWith("Digest");

        return Stream.of(
            // Pipelined Basic authorization.
            Arguments.of(basicChallengeHandler, basicPredicate),

            // Pipelined Digest authorization.
            Arguments.of(digestChallengeHandler, digestPredicate)
        );
    }

    /**
     * Tests that receiving a successful response without {@code Proxy-Authenticate-Info} doesn't validate the sent
     * {@code Proxy-Authorization} header or update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void nullOrEmptyProxyAuthenticateInfoIsIgnored() throws ProxyConnectException {
        AuthorizationChallengeHandler challengeHandler = new AuthorizationChallengeHandler("1", "1");
        challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);

        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), challengeHandler, new AtomicReference<>());

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.OK);
        when(response.headers()).thenReturn(EmptyHttpHeaders.INSTANCE);

        Attribute<String> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(null);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.handleResponse(ctx, response);
        String challengeResponse = challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);
        String nonce = AuthorizationChallengeHandler.parseAuthenticationOrAuthorizationHeader(challengeResponse)
            .get("nonce");

        assertEquals(ORIGINAL_NONCE, nonce);
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} validates the {@code
     * Proxy-Authorization} header sent to the server but doesn't update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void proxyAuthenticateInfoValidatesProxyAuthorization() throws ProxyConnectException {
        AuthorizationChallengeHandler challengeHandler = new AuthorizationChallengeHandler("1", "1");
        String authorizationHeader = challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);
        String cnonce = AuthorizationChallengeHandler.parseAuthenticationOrAuthorizationHeader(authorizationHeader)
            .get("cnonce");

        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), challengeHandler, new AtomicReference<>());

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.get(PROXY_AUTHENTICATION_INFO)).thenReturn("nc=00000001, cnonce=\"" + cnonce + "\"");

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.OK);
        when(response.headers()).thenReturn(headers);

        Attribute<String> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(authorizationHeader);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.handleResponse(ctx, response);
        String challengeResponse = challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);
        String nonce = AuthorizationChallengeHandler.parseAuthenticationOrAuthorizationHeader(challengeResponse)
            .get("nonce");

        assertEquals(ORIGINAL_NONCE, nonce);
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} that doesn't match the values
     * sent in the {@code Proxy-Authorization} header will throw a {@link IllegalStateException}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void proxyAuthenticateInfoFailsValidation() {
        AuthorizationChallengeHandler challengeHandler = new AuthorizationChallengeHandler("1", "1");
        String authorizationHeader = challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/",
            Collections.singletonList(PARSED_DIGEST_CHALLENGE), () -> new byte[0]);

        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), challengeHandler, new AtomicReference<>());

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.get(PROXY_AUTHENTICATION_INFO)).thenReturn("nc=00000001, cnonce=\"incorrectCnonce\"");

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.OK);
        when(response.headers()).thenReturn(headers);

        Attribute<String> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(authorizationHeader);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        assertThrows(IllegalStateException.class, () -> proxyAuthenticationHandler.handleResponse(ctx, response));
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} that contains a {@code
     * nextnonce} value will update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void proxyAuthenticateInfoUpdatesNonce() throws ProxyConnectException {
        Map<String, String> challengeCopy = new HashMap<>(PARSED_DIGEST_CHALLENGE);
        AuthorizationChallengeHandler challengeHandler = new AuthorizationChallengeHandler("1", "1");
        challengeHandler.handleDigest(HttpMethod.CONNECT.name(), "/", Collections.singletonList(challengeCopy),
            () -> new byte[0]);

        HttpProxyHandler proxyAuthenticationHandler = new HttpProxyHandler(
            new InetSocketAddress("localhost", 8888), challengeHandler, new AtomicReference<>());

        HttpHeaders headers = mock(HttpHeaders.class);
        when(headers.get(PROXY_AUTHENTICATION_INFO)).thenReturn("nextnonce=\"" + UPDATED_NONCE + "\"");

        HttpResponse response = mock(HttpResponse.class);
        when(response.status()).thenReturn(HttpResponseStatus.OK);
        when(response.headers()).thenReturn(headers);

        Attribute<String> attribute = mock(Attribute.class);
        when(attribute.get()).thenReturn(null);

        Channel channel = mock(Channel.class);
        when(channel.attr(any(AttributeKey.class))).thenReturn(attribute);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.channel()).thenReturn(channel);

        proxyAuthenticationHandler.handleResponse(ctx, response);
        String challengeResponse = challengeHandler.attemptToPipelineAuthorization(HttpMethod.CONNECT.name(), "/",
            () -> new byte[0]);
        String nonce = AuthorizationChallengeHandler.parseAuthenticationOrAuthorizationHeader(challengeResponse)
            .get("nonce");

        assertEquals(UPDATED_NONCE, nonce);
    }

    private static Map<String, String> parseDigestChallenge(String digestChallenge) {
        String challenge = digestChallenge.split(" ", 2)[1];

        Map<String, String> parsedChallenge = new HashMap<>();
        for (String challengePiece : challenge.split(",")) {
            String[] kvp = challengePiece.split("=", 2);

            // Skip challenge information that has no value.
            if (kvp.length != 2) {
                continue;
            }

            parsedChallenge.put(kvp[0].trim(), kvp[1].trim().replace("\"", ""));
        }

        return parsedChallenge;
    }
}
