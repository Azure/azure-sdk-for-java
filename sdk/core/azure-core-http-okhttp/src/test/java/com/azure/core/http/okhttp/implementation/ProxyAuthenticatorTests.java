// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.util.AuthorizationChallengeHandler;
import okhttp3.Address;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Dns;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.azure.core.util.AuthorizationChallengeHandler.PROXY_AUTHORIZATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ProxyAuthenticator}.
 */
public class ProxyAuthenticatorTests {
    private static final String PREEMPTIVE_AUTHENTICATE = "Preemptive Authenticate";
    private static final Request DEFAULT_REQUEST = new Request.Builder().get().url("http://localhost:8888/get").build();
    private static final Address DEFAULT_ADDRESS = new Address("localhost", 80, Dns.SYSTEM, SocketFactory.getDefault(),
        null, null, null, Authenticator.NONE, null, Collections.singletonList(Protocol.HTTP_1_1),
        Collections.singletonList(ConnectionSpec.MODERN_TLS), ProxySelector.getDefault());

    private static final String DIGEST_CHALLENGE = "Digest realm=\"test realm\", qop=\"auth\", algorithm=SHA-256, "
        + "nonce=\"7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v\", "
        + "opaque=\"FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS\"";

    private static final Headers DIGEST_CHALLENGE_HEADERS = new Headers.Builder()
        .add("Proxy-Authenticate: " + DIGEST_CHALLENGE)
        .build();

    private static final Predicate<String> BASIC_PREDICATE = "Basic MTox"::equals;
    private static final Predicate<String> DIGEST_PREDICATE = (authHeader) -> authHeader.startsWith("Digest");

    private static final String ORIGINAL_NONCE = "7ypf/xlj9XXwfDPEoM4URrv/xwf94BcCAzFZH4GiTo0v";
    private static final String UPDATED_NONCE = "FQhe/qaU925kfnzjCev0ciny7QMkPqMAFRtzCUYo5tdS";

    @AfterEach
    public void cleanupInlineMocks() {
        Mockito.framework().clearInlineMock(this);
    }

    /**
     * Tests that when a preemptive challenge is sent by the OkHttp client before a {@code Proxy-Authenticate} challenge
     * has been captured a {@code Proxy-Authorization} header isn't added to the {@code CONNECT} request.
     */
    @Test
    public void preemptiveChallengesAreIgnored() {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse(PREEMPTIVE_AUTHENTICATE, new Headers.Builder().build());
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertNull(authenticateRequest.header(PROXY_AUTHORIZATION));
    }

    /**
     * Tests that when {@link ProxyAuthenticator} has received a {@code Proxy-Authenticate} challenge a
     * {@code Proxy-Authorization} header will be applied to the {@code CONNECT} requests.
     */
    @ParameterizedTest
    @MethodSource("authorizationIsAppliedSupplier")
    public void authorizationIsApplied(Headers challengeHeaders, Predicate<String> expectedPredicate) {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", challengeHeaders);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(expectedPredicate.test(authenticateRequest.header(PROXY_AUTHORIZATION)));
    }

    public static Stream<Arguments> authorizationIsAppliedSupplier() {
        return Stream.of(
            // ChallengeHolder only containing Basic challenge.
            Arguments.of(new Headers.Builder()
                .add("Proxy-Authenticate: Basic")
                .build(), BASIC_PREDICATE),

            // ChallengeHolder only containing Digest challenge.
            Arguments.of(new Headers.Builder()
                .add("Proxy-Authenticate: " + DIGEST_CHALLENGE)
                .build(), DIGEST_PREDICATE),

            // ChallengeHolder containing both Basic and Digest challenge.
            Arguments.of(new Headers.Builder()
                .add("Proxy-Authenticate: Basic")
                .add("Proxy-Authenticate: " + DIGEST_CHALLENGE)
                .build(), DIGEST_PREDICATE)
        );
    }

    /**
     * Tests that when the {@link AuthorizationChallengeHandler} has already handled a {@code Proxy-Authenticate}
     * challenge it is capable of pipelining subsequent requests.
     */
    @ParameterizedTest
    @MethodSource("authorizationCanBePipelinedSupplier")
    public void authorizationCanBePipelined(Headers challengeHeaders, Predicate<String> expectedPredicate) {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", challengeHeaders);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(expectedPredicate.test(authenticateRequest.header(PROXY_AUTHORIZATION)));

        /*
         * Now that a request challenge has been handled we should apply a preemptive Proxy-Authorization header to
         * subsequent requests.
         */
        response = mockResponse(PREEMPTIVE_AUTHENTICATE, new Headers.Builder().build());
        authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(expectedPredicate.test(authenticateRequest.header(PROXY_AUTHORIZATION)));
    }

    public static Stream<Arguments> authorizationCanBePipelinedSupplier() {
        return Stream.of(
            // Pipelined Basic authorization.
            Arguments.of(new Headers.Builder()
                .add("Proxy-Authenticate: Basic")
                .build(), BASIC_PREDICATE),

            // Pipelined Digest authorization.
            Arguments.of(new Headers.Builder()
                .add("Proxy-Authenticate: " + DIGEST_CHALLENGE)
                .build(), DIGEST_PREDICATE)
        );
    }

    /**
     * Tests that receiving a successful response without {@code Proxy-Authenticate-Info} doesn't validate the sent
     * {@code Proxy-Authorization} header or update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    public void nullOrEmptyProxyAuthenticateInfoIsIgnored() throws IOException {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", DIGEST_CHALLENGE_HEADERS);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(DIGEST_PREDICATE.test(authenticateRequest.header(PROXY_AUTHORIZATION)));

        Interceptor interceptor = proxyAuthenticator.getProxyAuthenticationInfoInterceptor();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(mockResponse("This is a test", new Headers.Builder().build()));

        interceptor.intercept(chain);

        authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);

        String nonce = AuthorizationChallengeHandler
            .parseAuthenticationOrAuthorizationHeader(authenticateRequest.header(PROXY_AUTHORIZATION))
            .get("nonce");
        assertEquals(ORIGINAL_NONCE, nonce);
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} validates the {@code
     * Proxy-Authorization} header sent to the server but doesn't update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    public void proxyAuthenticateInfoValidatesProxyAuthorization() throws IOException {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", DIGEST_CHALLENGE_HEADERS);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(DIGEST_PREDICATE.test(authenticateRequest.header(PROXY_AUTHORIZATION)));

        String cnonce = AuthorizationChallengeHandler
            .parseAuthenticationOrAuthorizationHeader(authenticateRequest.header(PROXY_AUTHORIZATION))
            .get("cnonce");

        Interceptor interceptor = proxyAuthenticator.getProxyAuthenticationInfoInterceptor();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(mockResponse("This is a test",
            new Headers.Builder()
                .add("Proxy-Authentication-Info: nc=00000001, cnonce=\"" + cnonce + "\"")
                .build()));
        when(chain.request()).thenReturn(authenticateRequest);

        interceptor.intercept(chain);

        authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);

        String nonce = AuthorizationChallengeHandler
            .parseAuthenticationOrAuthorizationHeader(authenticateRequest.header(PROXY_AUTHORIZATION))
            .get("nonce");
        assertEquals(ORIGINAL_NONCE, nonce);
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} that doesn't match the values
     * sent in the {@code Proxy-Authorization} header will throw a {@link IllegalStateException}.
     */
    @Test
    public void proxyAuthenticateInfoFailsValidation() throws IOException {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", DIGEST_CHALLENGE_HEADERS);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(DIGEST_PREDICATE.test(authenticateRequest.header(PROXY_AUTHORIZATION)));

        Interceptor interceptor = proxyAuthenticator.getProxyAuthenticationInfoInterceptor();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(mockResponse("This is a test",
            new Headers.Builder()
                .add("Proxy-Authentication-Info: nc=00000001, cnonce=\"incorrectCnonce\"")
                .build()));
        when(chain.request()).thenReturn(authenticateRequest);

        assertThrows(IllegalStateException.class, () -> interceptor.intercept(chain));
    }

    /**
     * Tests that receiving a successful response with a {@code Proxy-Authenticate-Info} that contains a {@code
     * nextnonce} value will update the {@link AuthorizationChallengeHandler}.
     */
    @Test
    public void proxyAuthenticateInfoUpdatesNonce() throws IOException {
        ProxyAuthenticator proxyAuthenticator = new ProxyAuthenticator("1", "1");

        Response response = mockResponse("This is a test", DIGEST_CHALLENGE_HEADERS);
        Route route = new Route(DEFAULT_ADDRESS, new Proxy(Proxy.Type.HTTP, new InetSocketAddress("localhost", 8888)),
            new InetSocketAddress("localhost", 80));

        Request authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);
        assertTrue(DIGEST_PREDICATE.test(authenticateRequest.header(PROXY_AUTHORIZATION)));

        Interceptor interceptor = proxyAuthenticator.getProxyAuthenticationInfoInterceptor();

        Interceptor.Chain chain = mock(Interceptor.Chain.class);
        when(chain.proceed(any())).thenReturn(mockResponse("This is a test",
            new Headers.Builder()
                .add("Proxy-Authentication-Info: nextnonce=\"" + UPDATED_NONCE + "\"")
                .build()));
        when(chain.request()).thenReturn(authenticateRequest);

        interceptor.intercept(chain);

        authenticateRequest = proxyAuthenticator.authenticate(route, response);
        assertNotNull(authenticateRequest);

        String nonce = AuthorizationChallengeHandler
            .parseAuthenticationOrAuthorizationHeader(authenticateRequest.header(PROXY_AUTHORIZATION))
            .get("nonce");
        assertEquals(UPDATED_NONCE, nonce);
    }

    private static Response mockResponse(String message, Headers headers) {
        return new Response.Builder().request(DEFAULT_REQUEST).protocol(Protocol.HTTP_1_1).message(message)
            .code(407).headers(headers).sentRequestAtMillis(0).receivedResponseAtMillis(1)
            .build();
    }
}
