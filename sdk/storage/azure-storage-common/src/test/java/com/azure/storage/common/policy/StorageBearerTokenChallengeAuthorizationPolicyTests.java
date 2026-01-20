// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy.extractChallengeAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private StorageBearerTokenChallengeAuthorizationPolicy policy;
    private static final String DEFAULT_SCOPE = "https://storage.azure.com/.default";
    private static final String RESOURCE_ID_STRING = "resource_id";
    private static final String AUTHORIZATION_URI_STRING = "authorization_uri";

    @BeforeEach
    public void setup() {
        policy = new StorageBearerTokenChallengeAuthorizationPolicy(new MockTokenCredential(), DEFAULT_SCOPE);
    }

    @Test
    public void extractChallengeAttributesExpected() {
        String header
            = "Bearer resource_id=\"https://storage.azure.com\",authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        Map<String, String> challengeAttributes = extractChallengeAttributes(header);
        assertNotNull(challengeAttributes);
        assertExampleResourceId(challengeAttributes.get(RESOURCE_ID_STRING));
        assertExampleAuthURI(challengeAttributes.get(AUTHORIZATION_URI_STRING));
    }

    @Test
    public void extractChallengeAttributesSpaceAfterComma() {
        String header
            = "Bearer resource_id=\"https://storage.azure.com\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        Map<String, String> challengeAttributes = extractChallengeAttributes(header);
        assertNotNull(challengeAttributes);
        assertExampleResourceId(challengeAttributes.get(RESOURCE_ID_STRING));
        assertExampleAuthURI(challengeAttributes.get(AUTHORIZATION_URI_STRING));
    }

    @Test
    public void extractChallengeAttributesWithUnknownExtraParam() {
        String header
            = "Bearer resource_id=\"https://storage.azure.com\", foo=\"bar\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        Map<String, String> challengeAttributes = extractChallengeAttributes(header);
        assertNotNull(challengeAttributes);
        assertEquals("bar", challengeAttributes.get("foo"));
        assertExampleResourceId(challengeAttributes.get(RESOURCE_ID_STRING));
    }

    @Test
    public void extractChallengeAttributesWithLowercaseScheme() {
        String header
            = "bearer resource_id=\"https://storage.azure.com\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        Map<String, String> challengeAttributes = extractChallengeAttributes(header);
        assertNotNull(challengeAttributes);
        assertExampleResourceId(challengeAttributes.get(RESOURCE_ID_STRING));
        assertExampleAuthURI(challengeAttributes.get(AUTHORIZATION_URI_STRING));
    }

    @Test
    public void extractEmptyChallenges() {
        assertTrue(extractChallengeAttributes(null).isEmpty());
        assertTrue(extractChallengeAttributes("Basic realm=\"test\"").isEmpty());
        assertTrue(extractChallengeAttributes("").isEmpty());
    }

    @Test
    public void doesNotAuthorizeRequestOnInvalidChallenge() {
        // Use a recording credential so we can assert scopes, tenantId, and CAE.
        RecordingTokenCredential recordingCredential = new RecordingTokenCredential();
        StorageBearerTokenChallengeAuthorizationPolicy policyUnderTest
            = new StorageBearerTokenChallengeAuthorizationPolicy(recordingCredential, DEFAULT_SCOPE);

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://storage.azure.com");
        HttpResponse response = new MockHttpResponse(request, 401);
        HttpPipelineCallContext context = createMockCallContext(request);

        boolean result = policyUnderTest.authorizeRequestOnChallengeSync(context, response);

        assertFalse(result);
        assertNull(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION));
        assertNull(recordingCredential.getLastContext());
    }

    @Test
    public void authorizeRequestOnValidChallenge() {
        // Use a recording credential so we can assert scopes, tenantId, and CAE.
        RecordingTokenCredential recordingCredential = new RecordingTokenCredential();
        StorageBearerTokenChallengeAuthorizationPolicy policyUnderTest
            = new StorageBearerTokenChallengeAuthorizationPolicy(recordingCredential, DEFAULT_SCOPE);

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://storage.azure.com");
        // Use a different resource to verify scopes are updated from the challenge.
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
            "Bearer resource_id=\"https://storage.core.windows.net\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"");
        HttpResponse response = new MockHttpResponse(request, 401, headers);
        HttpPipelineCallContext context = createMockCallContext(request);

        boolean result = policyUnderTest.authorizeRequestOnChallengeSync(context, response);

        assertTrue(result);
        String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
        assertNotNull(authHeader);
        assertTrue(authHeader.startsWith("Bearer "));

        TokenRequestContext captured = recordingCredential.getLastContext();
        assertNotNull(captured);
        assertEquals(1, captured.getScopes().size());
        assertEquals("https://storage.core.windows.net/.default", captured.getScopes().get(0));
        assertEquals("tenant", captured.getTenantId());
        assertTrue(captured.isCaeEnabled());
    }

    @Test
    public void authorizeRequestOnChallengeWithAuthorizationUriOnly() {
        RecordingTokenCredential recordingCredential = new RecordingTokenCredential();
        StorageBearerTokenChallengeAuthorizationPolicy policyUnderTest
            = new StorageBearerTokenChallengeAuthorizationPolicy(recordingCredential, DEFAULT_SCOPE);

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://storage.azure.com");
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
            "Bearer authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"");
        HttpResponse response = new MockHttpResponse(request, 401, headers);
        HttpPipelineCallContext context = createMockCallContext(request);

        boolean result = policyUnderTest.authorizeRequestOnChallengeSync(context, response);
        assertTrue(result);

        TokenRequestContext captured = recordingCredential.getLastContext();
        assertNotNull(captured);
        assertEquals(1, captured.getScopes().size());
        assertEquals(DEFAULT_SCOPE, captured.getScopes().get(0)); // falls back to initial scopes
        assertEquals("tenant", captured.getTenantId());
        assertTrue(captured.isCaeEnabled());
    }

    @Test
    public void authorizeRequestOnChallengeWithMultipleTenants() {
        RecordingTokenCredential credential = new RecordingTokenCredential();
        StorageBearerTokenChallengeAuthorizationPolicy policyUnderTest
            = new StorageBearerTokenChallengeAuthorizationPolicy(credential, DEFAULT_SCOPE);

        String[] tenants = { "tenantA", "72f988bf-86f1-41af-91ab-2d7cd011db47", "my-tenant" };

        for (String tenant : tenants) {
            HttpRequest request = new HttpRequest(HttpMethod.GET, "https://storage.azure.com");
            HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
                "Bearer resource_id=\"https://storage.azure.com\", authorization_uri=\"https://login.microsoftonline.com/"
                    + tenant + "/oauth2/authorize\"");
            HttpResponse response = new MockHttpResponse(request, 401, headers);
            HttpPipelineCallContext context = createMockCallContext(request);

            boolean handled = policyUnderTest.authorizeRequestOnChallengeSync(context, response);
            assertTrue(handled, "Challenge should be handled for tenant: " + tenant);

            String authHeader = request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION);
            assertNotNull(authHeader);
            assertTrue(authHeader.startsWith("Bearer "));
        }

        List<TokenRequestContext> captured = credential.getContexts();
        assertEquals(tenants.length, captured.size());
        for (int i = 0; i < tenants.length; i++) {
            TokenRequestContext ctx = captured.get(i);
            assertEquals(1, ctx.getScopes().size());
            assertEquals(DEFAULT_SCOPE, ctx.getScopes().get(0));
            assertEquals(tenants[i], ctx.getTenantId());
            assertTrue(ctx.isCaeEnabled());
        }
    }

    @Test
    public void extractTenantIdFromUri() {
        String uri = "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize";
        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";

        String actualTenantId = policy.extractTenantIdFromUri(uri);

        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    public void extractTenantIdFromUriInvalidUri() {
        String invalidUri = "https://login.microsoftonline.com/";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(invalidUri));

        String expectedMessage = "Invalid authorization URI: tenantId not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void extractTenantIdFromUriMalformedUri() {
        String malformedUri = "ht!tp://invalid-uri";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(malformedUri));

        String expectedMessage = "Invalid authorization URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    private static HttpPipelineCallContext createMockCallContext(HttpRequest request) {
        AtomicReference<HttpPipelineCallContext> callContextReference = new AtomicReference<>();

        HttpPipeline callContextCreator = new HttpPipelineBuilder().policies((callContext, next) -> {
            callContextReference.set(callContext);

            return next.process();
        }).httpClient(ignored -> Mono.empty()).build();

        HttpResponse response = callContextCreator.sendSync(request, Context.NONE);
        if (response != null) {
            response.close();
        }
        return callContextReference.get();
    }

    // New helper credential to capture the TokenRequestContext used during challenge handling.
    static final class RecordingTokenCredential implements TokenCredential {
        private final AtomicReference<TokenRequestContext> lastContext = new AtomicReference<>();
        private final List<TokenRequestContext> contexts = new ArrayList<>();

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext requestContext) {
            // Snapshot to avoid mutation side effects if the same instance is reused.
            TokenRequestContext snapshot
                = new TokenRequestContext().addScopes(requestContext.getScopes().toArray(new String[0]))
                    .setTenantId(requestContext.getTenantId())
                    .setCaeEnabled(requestContext.isCaeEnabled());
            lastContext.set(snapshot);
            contexts.add(snapshot);
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        }

        TokenRequestContext getLastContext() {
            return lastContext.get();
        }

        List<TokenRequestContext> getContexts() {
            return contexts;
        }
    }

    private void assertExampleResourceId(String resourceId) {
        assertEquals("https://storage.azure.com", resourceId);
    }

    private void assertExampleAuthURI(String authURI) {
        assertEquals("https://login.microsoftonline.com/tenant/oauth2/authorize", authURI);
    }
}
