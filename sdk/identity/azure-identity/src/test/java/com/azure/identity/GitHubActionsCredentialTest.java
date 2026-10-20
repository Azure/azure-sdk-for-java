// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.AddHeadersPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubActionsCredentialTest {
    static final String CLIENT_ID = "00000000-0000-0000-0000-000000000002";
    static final String TENANT_ID = "contoso.onmicrosoft.com";
    static final String SCOPE = "https://management.azure.com/.default";
    static final String ASSERTION = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjQxMDI0NDQ4MDB9.signature";
    static final Duration TIMEOUT = Duration.ofSeconds(20);

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void exchangesAssertionAndUsesAccessTokenCache(boolean async) {
        FakeHttpClient http = new FakeHttpClient();
        GitHubActionsCredential credential = credentialBuilder(http).build();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);
        AccessToken token = getToken(credential, request, async);
        assertNotNull(token);
        assertEquals("access-token-1", token.getToken());
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()));
        assertEquals(CLIENT_ID, http.tokenForms.get(0).get("client_id"));
        assertEquals(ASSERTION, http.tokenForms.get(0).get("client_assertion"));
        assertEquals("client_credentials", http.tokenForms.get(0).get("grant_type"));
        assertEquals("urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
            http.tokenForms.get(0).get("client_assertion_type"));
        assertTrue(http.tokenForms.get(0).get("scope").contains("https://management.azure.com/"));
        assertTrue(http.lastTokenPath.contains(TENANT_ID));
        int assertionRequests = http.oidcRequests.get();
        assertEquals(token.getToken(), getToken(credential, request, async).getToken());
        assertEquals(assertionRequests, http.oidcRequests.get());
        assertEquals(1, http.tokenForms.size());
        assertEquals(http.oidcRequests.get(), http.closedResponses.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void refreshesAssertionWhenAccessTokenIsNearExpiry(boolean async) {
        FakeHttpClient http = new FakeHttpClient();
        http.expiresIn = 60;
        GitHubActionsCredential credential = credentialBuilder(http).build();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);
        getToken(credential, request, async);
        int assertionRequests = http.oidcRequests.get();
        assertEquals("access-token-2", getToken(credential, request, async).getToken());
        assertTrue(http.oidcRequests.get() > assertionRequests);
        assertEquals(2, http.tokenForms.size());
    }

    @ParameterizedTest
    @ValueSource(ints = { 204, 400, 401 })
    void rejectsHttpFailuresWithoutDisclosingResponse(int status) {
        FakeHttpClient http = new FakeHttpClient();
        http.oidcStatus = status;
        http.oidcBody = "{\"value\":\"response-secret-sentinel\"}";
        ClientAuthenticationException error = assertThrows(ClientAuthenticationException.class,
            () -> credentialBuilder(http).build().getTokenSync(new TokenRequestContext().addScopes(SCOPE)));
        assertTrue(error.getMessage().contains(Integer.toString(status)));
        assertFalse(error.getMessage().contains("response-secret-sentinel"));
        assertNull(error.getCause());
        assertEquals(0, http.tokenForms.size());
        assertEquals(http.oidcRequests.get(), http.closedResponses.get());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "null",
            "[]",
            "{}",
            "{\"value\":null}",
            "{\"value\":42}",
            "{\"value\":true}",
            "{\"value\":{}}",
            "{\"value\":[]}",
            "{\"value\":\"\"}",
            "response-secret-sentinel",
            "{\"value\":\"response-secret-sentinel\"",
            "{\"value\":\"response-secret-sentinel\"} trailing" })
    void rejectsInvalidResponsesWithoutDisclosingAssertions(String body) {
        FakeHttpClient http = new FakeHttpClient();
        http.oidcBody = body;
        ClientAuthenticationException error = assertThrows(ClientAuthenticationException.class,
            () -> credentialBuilder(http).build().getTokenSync(new TokenRequestContext().addScopes(SCOPE)));
        assertFalse(error.getMessage().contains("response-secret-sentinel"));
        StringWriter diagnostics = new StringWriter();
        error.printStackTrace(new PrintWriter(diagnostics));
        assertFalse(diagnostics.toString().contains("response-secret-sentinel"));
        assertEquals(0, http.tokenForms.size());
        assertEquals(http.oidcRequests.get(), http.closedResponses.get());
    }

    @Test
    void asyncHttpFailureRetainsAuthenticationException() {
        FakeHttpClient http = new FakeHttpClient();
        http.oidcStatus = 401;
        StepVerifier.create(credentialBuilder(http).build().getToken(new TokenRequestContext().addScopes(SCOPE)))
            .expectError(ClientAuthenticationException.class)
            .verify(TIMEOUT);
        assertEquals(0, http.tokenForms.size());
    }

    @Test
    void asyncRequestIsLazyAndDoesNotPerformHttpOnAnEventLoop() {
        FakeHttpClient http = new FakeHttpClient();
        GitHubActionsCredential credential = credentialBuilder(http).build();
        Mono<AccessToken> token = credential.getToken(new TokenRequestContext().addScopes(SCOPE));
        assertEquals(0, http.oidcRequests.get());
        StepVerifier.create(token.subscribeOn(Schedulers.parallel()))
            .assertNext(value -> assertEquals("access-token-1", value.getToken()))
            .expectComplete()
            .verify(TIMEOUT);
        assertFalse(http.nonBlockingOidcThread.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void propagatesTransportAndEntraFailures(boolean async) {
        FakeHttpClient http = new FakeHttpClient();
        http.transportFailure = new IllegalStateException("GitHub transport unavailable");
        RuntimeException transportError = assertThrows(RuntimeException.class,
            () -> getToken(credentialBuilder(http).build(), new TokenRequestContext().addScopes(SCOPE), async));
        Throwable cause = transportError;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        assertSame(http.transportFailure, cause);
        assertEquals(0, http.tokenForms.size());

        http.transportFailure = null;
        http.tokenStatus = 400;
        RuntimeException tokenError = assertThrows(RuntimeException.class,
            () -> getToken(credentialBuilder(http).build(), new TokenRequestContext().addScopes(SCOPE), async));
        assertTrue(tokenError.getMessage().contains("bad-federation"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void forwardsTenantAndCaeOptions(boolean async) {
        FakeHttpClient http = new FakeHttpClient();
        GitHubActionsCredential credential
            = credentialBuilder(http).additionallyAllowedTenants("other.contoso.com").build();
        TokenRequestContext request
            = new TokenRequestContext().addScopes(SCOPE).setTenantId("other.contoso.com").setCaeEnabled(true);
        getToken(credential, request, async);
        assertTrue(http.lastTokenPath.contains("other.contoso.com"));
        String claims = http.tokenForms.get(0).get("claims");
        assertNotNull(claims);
        assertTrue(claims.contains("CP1"), claims);
    }

    @Test
    void forwardsClaimsOnAsyncTokenRequests() {
        FakeHttpClient http = new FakeHttpClient();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE)
            .setCaeEnabled(true)
            .setClaims("{\"access_token\":{\"auth_time\":{\"essential\":true}}}");
        getToken(credentialBuilder(http).build(), request, true);
        assertTrue(http.tokenForms.get(0).get("claims").contains("auth_time"));
    }

    @Test
    void usesTheSuppliedPipelineForBothRequests() {
        FakeHttpClient http = new FakeHttpClient();
        HttpHeaderName header = HttpHeaderName.fromString("x-test-credential");
        GitHubActionsCredential credential = credentialBuilder(http).pipeline(new HttpPipelineBuilder().httpClient(http)
            .policies(new AddHeadersPolicy(new HttpHeaders().set(header, "custom-pipeline")))
            .build()).build();
        credential.getTokenSync(new TokenRequestContext().addScopes(SCOPE));
        assertEquals("custom-pipeline", http.oidcRequest.getHeaders().getValue(header));
        assertEquals("custom-pipeline", http.tokenRequest.getHeaders().getValue(header));
    }

    static GitHubActionsCredentialBuilder credentialBuilder(FakeHttpClient http) {
        return new GitHubActionsCredentialBuilder().configuration(configuration(settings()))
            .httpClient(http)
            .disableInstanceDiscovery();
    }

    static Map<String, String> settings() {
        Map<String, String> settings = new HashMap<>();
        settings.put("AZURE_CLIENT_ID", CLIENT_ID);
        settings.put("AZURE_TENANT_ID", TENANT_ID);
        settings.put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://github-oidc.test/token?api-version=1");
        settings.put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "runner-token-sentinel");
        return settings;
    }

    static Configuration configuration(Map<String, String> settings) {
        TestConfigurationSource source = new TestConfigurationSource();
        settings.forEach(source::put);
        return TestUtils.createTestConfiguration(source);
    }

    private static AccessToken getToken(GitHubActionsCredential credential, TokenRequestContext request,
        boolean async) {
        return async ? credential.getToken(request).block(TIMEOUT) : credential.getTokenSync(request);
    }

    static Map<String, String> parseForm(String body) {
        Map<String, String> result = new HashMap<>();
        for (String parameter : body.split("&")) {
            String[] parts = parameter.split("=", 2);
            try {
                result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()),
                    parts.length == 1 ? "" : URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()));
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e);
            }
        }
        return result;
    }

    static class FakeHttpClient implements HttpClient {
        final AtomicInteger oidcRequests = new AtomicInteger();
        final AtomicInteger closedResponses = new AtomicInteger();
        final AtomicBoolean nonBlockingOidcThread = new AtomicBoolean();
        final List<Map<String, String>> tokenForms = Collections.synchronizedList(new ArrayList<>());
        volatile HttpRequest oidcRequest;
        volatile HttpRequest tokenRequest;
        volatile String lastTokenPath;
        int oidcStatus = 200;
        int tokenStatus = 200;
        int expiresIn = 3600;
        String oidcBody = "{\"ignored\":{\"nested\":[1,2]},\"value\":\"" + ASSERTION + "\"}";
        RuntimeException transportFailure;

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            return Mono.fromSupplier(() -> sendSync(request, Context.NONE));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            if ("github-oidc.test".equals(request.getUrl().getHost())) {
                if (transportFailure != null) {
                    throw transportFailure;
                }
                oidcRequest = request;
                oidcRequests.incrementAndGet();
                if (Schedulers.isInNonBlockingThread()) {
                    nonBlockingOidcThread.set(true);
                }
                assertEquals(HttpMethod.GET, request.getHttpMethod());
                assertEquals("Bearer runner-token-sentinel",
                    request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
                AtomicBoolean closed = new AtomicBoolean();
                return new MockHttpResponse(request, oidcStatus, jsonHeaders(),
                    oidcBody.getBytes(StandardCharsets.UTF_8)) {
                    @Override
                    public void close() {
                        if (closed.compareAndSet(false, true)) {
                            closedResponses.incrementAndGet();
                        }
                        super.close();
                    }
                };
            }
            String authority = "https://" + request.getUrl().getAuthority();
            String tenant = request.getUrl().getPath().split("/")[1];
            if (request.getUrl().getPath().contains(".well-known/openid-configuration")) {
                String endpoint = authority + "/" + tenant + "/oauth2/v2.0/";
                return response(request, 200,
                    "{\"token_endpoint\":\"" + endpoint + "token\"," + "\"issuer\":\"" + authority + "/" + tenant
                        + "/v2.0\"," + "\"authorization_endpoint\":\"" + endpoint + "authorize\"}");
            }
            assertTrue(request.getUrl().getPath().endsWith("/token"), "Unexpected request: " + request.getUrl());
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            tokenRequest = request;
            lastTokenPath = request.getUrl().getPath();
            Map<String, String> form = parseForm(request.getBodyAsBinaryData().toString());
            tokenForms.add(form);
            if (tokenStatus != 200) {
                return response(request, tokenStatus,
                    "{\"error\":\"invalid_grant\",\"error_description\":\"bad-federation\"}");
            }
            return response(request, 200,
                "{\"token_type\":\"Bearer\",\"scope\":\"" + form.get("scope") + "\",\"expires_in\":" + expiresIn
                    + ",\"ext_expires_in\":" + expiresIn + ",\"access_token\":\"access-token-" + tokenForms.size()
                    + "\"}");
        }

        private static HttpHeaders jsonHeaders() {
            return new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        }

        private static HttpResponse response(HttpRequest request, int status, String body) {
            return new MockHttpResponse(request, status, jsonHeaders(), body.getBytes(StandardCharsets.UTF_8));
        }
    }
}
