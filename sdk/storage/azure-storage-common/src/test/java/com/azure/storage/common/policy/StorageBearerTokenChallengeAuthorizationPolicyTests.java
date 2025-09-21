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
import com.azure.core.util.AuthenticateChallenge;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy.findBearer;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private StorageBearerTokenChallengeAuthorizationPolicy policy;
    private static final String DEFAULT_SCOPE = "https://storage.azure.com/.default";

    @BeforeEach
    public void setup() {
        policy = new StorageBearerTokenChallengeAuthorizationPolicy(new MockTokenCredential(), DEFAULT_SCOPE);
    }

    @Test
    public void testExtractTenantIdFromUri() {
        String uri = "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize";
        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";

        String actualTenantId = policy.extractTenantIdFromUri(uri);

        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    public void testExtractTenantIdFromUriInvalidUri() {
        String invalidUri = "https://login.microsoftonline.com/";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(invalidUri));

        String expectedMessage = "Invalid authorization URI: tenantId not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testExtractTenantIdFromUriMalformedUri() {
        String malformedUri = "ht!tp://invalid-uri";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(malformedUri));

        String expectedMessage = "Invalid authorization URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testFindBearerWithNullHeader() {
        AuthenticateChallenge challenge = findBearer(null);
        assertNull(challenge);
    }

    @Test
    public void testFindBearerWithEmptyHeader() {
        AuthenticateChallenge challenge = findBearer("");
        assertNull(challenge);
    }

    @Test
    public void testFindBearerWithNonBearerHeader() {
        String header = "Basic realm=\"test\"";
        AuthenticateChallenge challenge = findBearer(header);
        assertNull(challenge);
    }

    @Test
    public void testFindBearerWithBearerHeader() {
        String header
            = "Bearer resource_id=\"https://storage.azure.com\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        AuthenticateChallenge challenge = findBearer(header);

        assertNotNull(challenge);
        assertEquals("Bearer", challenge.getScheme());
        assertEquals("https://storage.azure.com", challenge.getParameters().get("resource_id"));
        assertEquals("https://login.microsoftonline.com/tenant/oauth2/authorize",
            challenge.getParameters().get("authorization_uri"));
    }

    @Test
    public void testFindBearerWithMultipleChallenges() {
        String header = "Basic realm=\"test\", Bearer resource_id=\"https://storage.azure.com\", Digest nonce=\"123\"";
        AuthenticateChallenge challenge = findBearer(header);

        assertNotNull(challenge);
        assertEquals("Bearer", challenge.getScheme());
        assertEquals("https://storage.azure.com", challenge.getParameters().get("resource_id"));
    }

    @Test
    public void testProcessBearerChallengeWithNullHeader() {
        TokenRequestContext result = policy.processBearerChallenge(null);
        assertNull(result);
    }

    @Test
    public void testProcessBearerChallengeWithNonBearerHeader() {
        String header = "Basic realm=\"test\"";
        TokenRequestContext result = policy.processBearerChallenge(header);
        assertNull(result);
    }

    @Test
    public void testProcessBearerChallengeWithValidHeader() {
        String header
            = "Bearer resource_id=\"https://storage.azure.com\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"";
        TokenRequestContext result = policy.processBearerChallenge(header);

        assertNotNull(result);
        assertEquals(1, result.getScopes().size());
        assertEquals("https://storage.azure.com/.default", result.getScopes().get(0));
        assertEquals("tenant", result.getTenantId());
        assertTrue(result.isCaeEnabled());
    }

    @ParameterizedTest
    @MethodSource("testDetermineScopesToUseParameters")
    public void testDetermineScopesToUse(String resource, String[] expected) {
        String[] result = policy.determineScopesToUse(resource);
        assertArrayEquals(expected, result);
    }

    private static Stream<Arguments> testDetermineScopesToUseParameters() {
        return Stream.of(
            // Null or empty resource should return initial scopes
            Arguments.of(null, new String[] { "https://storage.azure.com/.default" }),
            Arguments.of("", new String[] { "https://storage.azure.com/.default" }),

            // Resource already ending with /.default
            Arguments.of("https://storage.azure.com/.default", new String[] { "https://storage.azure.com/.default" }),

            // Resource not ending with /.default
            Arguments.of("https://storage.azure.com", new String[] { "https://storage.azure.com/.default" }));
    }

    @Test
    public void testCreateTokenRequestContextWithResourceAndAuthUrl() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("resource_id", "https://storage.azure.com");
        attributes.put("authorization_uri", "https://login.microsoftonline.com/tenant/oauth2/authorize");

        TokenRequestContext result = policy.createTokenRequestContext(attributes);

        assertNotNull(result);
        assertEquals(1, result.getScopes().size());
        assertEquals("https://storage.azure.com/.default", result.getScopes().get(0));
        assertEquals("tenant", result.getTenantId());
        assertTrue(result.isCaeEnabled());
    }

    @Test
    public void testCreateTokenRequestContextWithResourceOnly() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("resource_id", "https://storage.azure.com");

        TokenRequestContext result = policy.createTokenRequestContext(attributes);

        assertNotNull(result);
        assertEquals(1, result.getScopes().size());
        assertEquals("https://storage.azure.com/.default", result.getScopes().get(0));
        assertNull(result.getTenantId());
        assertTrue(result.isCaeEnabled());
    }

    @Test
    public void testCreateTokenRequestContextWithAuthUrlOnly() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("authorization_uri", "https://login.microsoftonline.com/tenant/oauth2/authorize");

        TokenRequestContext result = policy.createTokenRequestContext(attributes);

        assertNotNull(result);
        assertEquals(1, result.getScopes().size());
        assertEquals("https://storage.azure.com/.default", result.getScopes().get(0));
        assertEquals("tenant", result.getTenantId());
        assertTrue(result.isCaeEnabled());
    }

    @Test
    public void testAuthorizeRequestOnChallengeWithValidChallenge() {
        // Use a recording credential so we can assert scopes, tenantId, and CAE.
        RecordingTokenCredential recordingCredential = new RecordingTokenCredential();
        StorageBearerTokenChallengeAuthorizationPolicy policyUnderTest
            = new StorageBearerTokenChallengeAuthorizationPolicy(recordingCredential, DEFAULT_SCOPE);

        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://storage.azure.com");
        // Use a different resource to verify scopes are updated from the challenge.
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.WWW_AUTHENTICATE,
            "Bearer resource_id=\"https://other.core.windows.net\", authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\"");
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
        assertEquals("https://other.core.windows.net/.default", captured.getScopes().get(0));
        assertEquals("tenant", captured.getTenantId());
        assertTrue(captured.isCaeEnabled());
    }

    private static HttpPipelineCallContext createMockCallContext(HttpRequest request) {
        AtomicReference<HttpPipelineCallContext> callContextReference = new AtomicReference<>();

        HttpPipeline callContextCreator = new HttpPipelineBuilder().policies((callContext, next) -> {
            callContextReference.set(callContext);

            return next.process();
        }).httpClient(ignored -> Mono.empty()).build();

        callContextCreator.sendSync(request, Context.NONE);

        return callContextReference.get();
    }

    // New helper credential to capture the TokenRequestContext used during challenge handling.
    static final class RecordingTokenCredential implements TokenCredential {
        private final AtomicReference<TokenRequestContext> lastContext = new AtomicReference<>();

        @Override
        public Mono<AccessToken> getToken(TokenRequestContext requestContext) {
            lastContext.set(requestContext);
            return Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1)));
        }

        TokenRequestContext getLastContext() {
            return lastContext.get();
        }
    }
}
