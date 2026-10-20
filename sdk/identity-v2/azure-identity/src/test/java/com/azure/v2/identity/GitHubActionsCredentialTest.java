// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.exceptions.CredentialAuthenticationException;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.configuration.ConfigurationSource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.UnsupportedEncodingException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubActionsCredentialTest {
    static final String CLIENT_ID = "00000000-0000-0000-0000-000000000002";
    static final String TENANT_ID = "contoso.onmicrosoft.com";
    static final String SCOPE = "https://management.azure.com/.default";
    static final String ASSERTION = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjQxMDI0NDQ4MDB9.signature";

    @Test
    void exchangesAssertionAndUsesAccessTokenCache() {
        FakeHttpClient http = new FakeHttpClient();
        GitHubActionsCredential credential = credentialBuilder(http).build();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);
        AccessToken token = credential.getToken(request);
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
        assertEquals(token.getToken(), credential.getToken(request).getToken());
        assertEquals(assertionRequests, http.oidcRequests.get());
        assertEquals(1, http.tokenForms.size());
        assertEquals(http.oidcRequests.get(), http.closedResponses.get());
    }

    @Test
    void refreshesAssertionWhenAccessTokenIsNearExpiry() {
        FakeHttpClient http = new FakeHttpClient();
        http.expiresIn = 60;
        GitHubActionsCredential credential = credentialBuilder(http).build();
        TokenRequestContext request = new TokenRequestContext().addScopes(SCOPE);
        credential.getToken(request);
        int assertionRequests = http.oidcRequests.get();
        assertEquals("access-token-2", credential.getToken(request).getToken());
        assertTrue(http.oidcRequests.get() > assertionRequests);
        assertEquals(2, http.tokenForms.size());
    }

    @ParameterizedTest
    @ValueSource(ints = { 204, 400, 401 })
    void rejectsHttpFailuresWithoutDisclosingResponse(int status) {
        FakeHttpClient http = new FakeHttpClient();
        http.oidcStatus = status;
        http.oidcBody = "{\"value\":\"response-secret-sentinel\"}";
        CredentialAuthenticationException error = assertThrows(CredentialAuthenticationException.class,
            () -> credentialBuilder(http).build().getToken(new TokenRequestContext().addScopes(SCOPE)));
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
        CredentialAuthenticationException error = assertThrows(CredentialAuthenticationException.class,
            () -> credentialBuilder(http).build().getToken(new TokenRequestContext().addScopes(SCOPE)));
        assertFalse(error.getMessage().contains("response-secret-sentinel"));
        StringWriter diagnostics = new StringWriter();
        error.printStackTrace(new PrintWriter(diagnostics));
        assertFalse(diagnostics.toString().contains("response-secret-sentinel"));
        assertEquals(0, http.tokenForms.size());
        assertEquals(http.oidcRequests.get(), http.closedResponses.get());
    }

    @Test
    void propagatesTransportAndEntraFailures() {
        FakeHttpClient http = new FakeHttpClient();
        http.transportFailure = new IllegalStateException("GitHub transport unavailable");
        RuntimeException transportError = assertThrows(RuntimeException.class,
            () -> credentialBuilder(http).build().getToken(new TokenRequestContext().addScopes(SCOPE)));
        assertTrue(transportError.getMessage().contains("GitHub transport unavailable"));
        assertEquals(0, http.tokenForms.size());

        http.transportFailure = null;
        http.tokenStatus = 400;
        RuntimeException tokenError = assertThrows(RuntimeException.class,
            () -> credentialBuilder(http).build().getToken(new TokenRequestContext().addScopes(SCOPE)));
        assertTrue(tokenError.getMessage().contains("bad-federation"));
    }

    @Test
    void forwardsTenantAndCaeOptions() {
        FakeHttpClient http = new FakeHttpClient();
        GitHubActionsCredential credential
            = credentialBuilder(http).additionallyAllowedTenants("other.contoso.com").build();
        TokenRequestContext request
            = new TokenRequestContext().addScopes(SCOPE).setTenantId("other.contoso.com").setCaeEnabled(true);
        credential.getToken(request);
        assertTrue(http.lastTokenPath.contains("other.contoso.com"));
        String claims = http.tokenForms.get(0).get("claims");
        assertNotNull(claims);
        assertTrue(claims.contains("CP1"), claims);
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
        return Configuration.from(new ConfigurationSource() {
            @Override
            public String getProperty(String name) {
                return settings.get(name);
            }

            @Override
            public boolean isMutable() {
                return false;
            }
        });
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
        public Response<BinaryData> send(HttpRequest request) {
            if ("github-oidc.test".equals(request.getUri().getHost())) {
                if (transportFailure != null) {
                    throw transportFailure;
                }
                oidcRequest = request;
                oidcRequests.incrementAndGet();
                assertEquals(HttpMethod.GET, request.getHttpMethod());
                assertEquals("Bearer runner-token-sentinel",
                    request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
                AtomicBoolean closed = new AtomicBoolean();
                return new Response<BinaryData>(request, oidcStatus, jsonHeaders(), BinaryData.fromString(oidcBody)) {
                    @Override
                    public void close() {
                        if (closed.compareAndSet(false, true)) {
                            closedResponses.incrementAndGet();
                        }
                        super.close();
                    }
                };
            }
            String authority = "https://" + request.getUri().getAuthority();
            String tenant = request.getUri().getPath().split("/")[1];
            if (request.getUri().getPath().contains(".well-known/openid-configuration")) {
                String endpoint = authority + "/" + tenant + "/oauth2/v2.0/";
                return response(request, 200,
                    "{\"token_endpoint\":\"" + endpoint + "token\"," + "\"issuer\":\"" + authority + "/" + tenant
                        + "/v2.0\"," + "\"authorization_endpoint\":\"" + endpoint + "authorize\"}");
            }
            assertTrue(request.getUri().getPath().endsWith("/token"), "Unexpected request: " + request.getUri());
            assertEquals(HttpMethod.POST, request.getHttpMethod());
            tokenRequest = request;
            lastTokenPath = request.getUri().getPath();
            Map<String, String> form = parseForm(request.getBody().toString());
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

        private static Response<BinaryData> response(HttpRequest request, int status, String body) {
            return new Response<>(request, status, jsonHeaders(), BinaryData.fromString(body));
        }
    }
}
