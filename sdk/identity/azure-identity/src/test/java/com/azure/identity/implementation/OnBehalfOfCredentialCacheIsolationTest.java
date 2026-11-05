// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.OnBehalfOfCredential;
import com.azure.identity.OnBehalfOfCredentialBuilder;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OnBehalfOfCredentialCacheIsolationTest {
    private static final String TENANT_ID = "00000000-0000-0000-0000-000000000001";
    private static final String CLIENT_ID = "00000000-0000-0000-0000-000000000002";
    private static final String AUTHORITY_HOST = "https://login.microsoftonline.test";
    private static final String SCOPE = "https://resource.test/.default";
    private static final String USER_A = "00000000-0000-0000-0000-00000000000a";
    private static final String USER_B = "00000000-0000-0000-0000-00000000000b";
    private static final String USER_C = "00000000-0000-0000-0000-00000000000c";
    private static final String ASSERTION_A = createJwt(USER_A);
    private static final String ASSERTION_B = createJwt(USER_B);
    private static final String ASSERTION_C = createJwt(USER_C);
    private static final String TOKEN_A = createJwt(USER_A);
    private static final String TOKEN_B = createJwt(USER_B);

    @ParameterizedTest(name = "sync cache isolation with CAE enabled: {0}")
    @ValueSource(booleans = { false, true })
    public void syncCacheLookupRemainsBoundToAssertion(boolean caeEnabled) {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE).setCaeEnabled(caeEnabled);

        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(request), USER_A);
        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(request), USER_A);
        assertEquals(1, httpClient.getTokenRequestCount());

        assertTokenForUser(createCredential(ASSERTION_B, cache, httpClient).getTokenSync(request), USER_B);
        assertEquals(2, httpClient.getTokenRequestCount());

        assertTokenForUser(createCredential(ASSERTION_B, cache, httpClient).getTokenSync(request), USER_B);
        assertEquals(2, httpClient.getTokenRequestCount());

        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(request), USER_A);
        assertEquals(3, httpClient.getTokenRequestCount());
    }

    @ParameterizedTest(name = "async cache isolation with CAE enabled: {0}")
    @ValueSource(booleans = { false, true })
    public void asyncCacheLookupRemainsBoundToAssertion(boolean caeEnabled) {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE).setCaeEnabled(caeEnabled);

        verifyTokenForUser(createCredential(ASSERTION_A, cache, httpClient), request, USER_A);
        verifyTokenForUser(createCredential(ASSERTION_A, cache, httpClient), request, USER_A);
        assertEquals(1, httpClient.getTokenRequestCount());

        verifyTokenForUser(createCredential(ASSERTION_B, cache, httpClient), request, USER_B);
        assertEquals(2, httpClient.getTokenRequestCount());

        verifyTokenForUser(createCredential(ASSERTION_B, cache, httpClient), request, USER_B);
        assertEquals(2, httpClient.getTokenRequestCount());

        verifyTokenForUser(createCredential(ASSERTION_A, cache, httpClient), request, USER_A);
        assertEquals(3, httpClient.getTokenRequestCount());
    }

    @Test
    public void syncTokenEndpointFailureDoesNotReturnAnotherAssertionsToken() {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);

        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(request), USER_A);
        httpClient.failAssertion(ASSERTION_B, 503);

        assertThrows(ClientAuthenticationException.class,
            () -> createCredential(ASSERTION_B, cache, httpClient).getTokenSync(request));
        assertTrue(httpClient.getTokenRequestCount() > 1);
    }

    @Test
    public void asyncInvalidAssertionDoesNotReturnCachedToken() {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);

        verifyTokenForUser(createCredential(ASSERTION_A, cache, httpClient), request, USER_A);
        StepVerifier.create(createCredential(ASSERTION_C, cache, httpClient).getToken(request))
            .expectError()
            .verify(Duration.ofSeconds(10));
        assertEquals(2, httpClient.getTokenRequestCount());
    }

    @Test
    public void cacheEntriesRemainIsolatedByScope() {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext firstRequest = new TokenRequestContext().addScopes(SCOPE);
        TokenRequestContext secondRequest
            = new TokenRequestContext().addScopes("https://resource.test/alternate/.default");

        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(firstRequest), USER_A);
        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(secondRequest), USER_A);
        assertTokenForUser(createCredential(ASSERTION_A, cache, httpClient).getTokenSync(firstRequest), USER_A);
        assertEquals(2, httpClient.getTokenRequestCount());
    }

    @Test
    public void concurrentRequestsNeverCrossAssertions() {
        SharedTokenCacheAccessAspect cache = new SharedTokenCacheAccessAspect();
        OboTokenHttpClient httpClient = new OboTokenHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);

        StepVerifier.create(Flux.range(0, 20).flatMap(index -> {
            boolean requestUserA = index % 2 == 0;
            String assertion = requestUserA ? ASSERTION_A : ASSERTION_B;
            String expectedUser = requestUserA ? USER_A : USER_B;
            return createCredential(assertion, cache, httpClient).getToken(request)
                .doOnNext(token -> assertTokenForUser(token, expectedUser));
        }, 20)).expectNextCount(20).expectComplete().verify(Duration.ofSeconds(10));
        assertTrue(httpClient.getTokenRequestCount() >= 2);
    }

    private static OnBehalfOfCredential createCredential(String assertion, ITokenCacheAccessAspect cache,
        HttpClient httpClient) {
        OnBehalfOfCredentialBuilder builder = createCredentialBuilder(assertion, httpClient);
        CredentialBuilderBaseHelper.getClientOptions(builder).setTokenCacheAccessAspect(cache);
        return builder.build();
    }

    private static OnBehalfOfCredentialBuilder createCredentialBuilder(String assertion, HttpClient httpClient) {
        return new OnBehalfOfCredentialBuilder().tenantId(TENANT_ID)
            .clientId(CLIENT_ID)
            .clientSecret("secret")
            .userAssertion(assertion)
            .authorityHost(AUTHORITY_HOST)
            .disableInstanceDiscovery()
            .maxRetry(0)
            .httpClient(httpClient);
    }

    private static void verifyTokenForUser(OnBehalfOfCredential credential, TokenRequestContext request,
        String expectedUser) {
        StepVerifier.create(credential.getToken(request))
            .assertNext(token -> assertTokenForUser(token, expectedUser))
            .expectComplete()
            .verify(Duration.ofSeconds(10));
    }

    private static void assertTokenForUser(AccessToken token, String expectedUser) {
        assertEquals(expectedUser, getObjectId(token.getToken()));
    }

    private static String createJwt(String objectId) {
        String header = encodeBase64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String payload = encodeBase64Url(
            String.format("{\"aud\":\"resource\",\"iss\":\"%s/%s/v2.0\",\"oid\":\"%s\",\"sub\":\"%s\",\"tid\":\"%s\"}",
                AUTHORITY_HOST, TENANT_ID, objectId, objectId, TENANT_ID));
        return header + "." + payload + ".signature";
    }

    private static String encodeBase64Url(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String getObjectId(String jwt) {
        String payload = new String(Base64.getUrlDecoder().decode(jwt.split("\\.")[1]), StandardCharsets.UTF_8);
        String prefix = "\"oid\":\"";
        int start = payload.indexOf(prefix) + prefix.length();
        return payload.substring(start, payload.indexOf('"', start));
    }

    private static final class SharedTokenCacheAccessAspect implements ITokenCacheAccessAspect {
        private final ReentrantLock lock = new ReentrantLock();
        private String serializedCache;

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext context) {
            lock.lock();
            if (serializedCache != null) {
                context.tokenCache().deserialize(serializedCache);
            }
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext context) {
            try {
                if (context.hasCacheChanged()) {
                    serializedCache = context.tokenCache().serialize();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    private static final class OboTokenHttpClient implements HttpClient {
        private static final HttpHeaderName CONTENT_TYPE = HttpHeaderName.fromString("Content-Type");
        private final AtomicInteger tokenRequestCount = new AtomicInteger();
        private final Map<String, Integer> assertionFailures = new ConcurrentHashMap<>();

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            if (request.getUrl().getPath().contains(".well-known/openid-configuration")) {
                return jsonResponse(request, 200,
                    String.format(
                        "{\"token_endpoint\":\"%s/%s/oauth2/v2.0/token\","
                            + "\"issuer\":\"%s/%s/v2.0\",\"authorization_endpoint\":\"%s/%s/oauth2/v2.0/authorize\"}",
                        AUTHORITY_HOST, TENANT_ID, AUTHORITY_HOST, TENANT_ID, AUTHORITY_HOST, TENANT_ID));
            }

            Map<String, String> form = parseForm(request.getBodyAsBinaryData().toString());
            assertEquals("urn:ietf:params:oauth:grant-type:jwt-bearer", form.get("grant_type"));
            assertEquals("on_behalf_of", form.get("requested_token_use"));
            assertTrue(form.get("scope").contains("https://resource.test/"));
            tokenRequestCount.incrementAndGet();

            String assertion = form.get("assertion");
            Integer failureStatus = assertionFailures.get(assertion);
            if (failureStatus != null) {
                return jsonResponse(request, failureStatus,
                    "{\"error\":\"server_error\",\"error_description\":\"Token endpoint unavailable\"}");
            }

            String token;
            if (ASSERTION_A.equals(assertion)) {
                token = TOKEN_A;
            } else if (ASSERTION_B.equals(assertion)) {
                token = TOKEN_B;
            } else {
                return jsonResponse(request, 400,
                    "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid assertion\"}");
            }
            return jsonResponse(request, 200,
                String.format("{\"token_type\":\"Bearer\",\"scope\":\"%s\",\"expires_in\":3600,"
                    + "\"ext_expires_in\":3600,\"access_token\":\"%s\"}", form.get("scope"), token));
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromSupplier(() -> sendSync(request, Context.NONE));
        }

        int getTokenRequestCount() {
            return tokenRequestCount.get();
        }

        void failAssertion(String assertion, int statusCode) {
            assertionFailures.put(assertion, statusCode);
        }

        private static MockHttpResponse jsonResponse(HttpRequest request, int statusCode, String body) {
            HttpHeaders headers = new HttpHeaders().set(CONTENT_TYPE, "application/json");
            return new MockHttpResponse(request, statusCode, headers, body.getBytes(StandardCharsets.UTF_8));
        }

        private static Map<String, String> parseForm(String body) {
            Map<String, String> form = new HashMap<>();
            for (String pair : body.split("&")) {
                String[] parts = pair.split("=", 2);
                form.put(decode(parts[0]), parts.length == 1 ? "" : decode(parts[1]));
            }
            return form;
        }

        private static String decode(String value) {
            try {
                return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
