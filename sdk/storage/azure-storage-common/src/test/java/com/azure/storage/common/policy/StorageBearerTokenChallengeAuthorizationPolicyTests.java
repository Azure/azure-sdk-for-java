// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.*;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private String[] scopes;
    private TokenCredential mockCredential;
    private static final String RESOURCE_ID = "resource_id";
    private static final String AUTHORIZATION_URI = "authorization_uri";

    @BeforeEach
    public void setup() {
        scopes = new String[] { "https://storage.azure.com/.default" };
        mockCredential = new MockTokenCredential();
    }

    @Test
    public void testExtractChallengeAttributes() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String authHeader
            = "Bearer authorization_uri=https://login.microsoftonline.com/tenantId/oauth2/authorize resource_id=https://storage.azure.com";
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader);

        assertNotNull(challenges);
        assertEquals("https://login.microsoftonline.com/tenantId/oauth2/authorize",
            challenges.get("authorization_uri"));
        assertEquals("https://storage.azure.com", challenges.get("resource_id"));
    }

    @Test
    public void usesTokenProvidedByCredentials() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        Map<String, String> challenges = policy.extractChallengeAttributes(null);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertNull(scope);
        assertNull(authorization);
    }

    @Test
    public void doesNotSendUnauthorizedRequestWhenEnableTenantDiscoveryIsFalse() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        Map<String, String> challenges = policy.extractChallengeAttributes(null);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertNull(scope);
        assertNull(authorization);
    }

    @Test
    public void sendsUnauthorizedRequestWhenEnableTenantDiscoveryIsTrue() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";
        String authHeader = "Bearer authorization_uri=https://login.microsoftonline.com/" + expectedTenantId
            + "/oauth2/authorize resource_id=https://storage.azure.com";
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertEquals("https://storage.azure.com", scope);
        assertEquals("https://login.microsoftonline.com/" + expectedTenantId + "/oauth2/authorize", authorization);
    }

    @Test
    public void usesScopeFromBearerChallenge() {
        StorageBearerTokenChallengeAuthorizationPolicy policy = new StorageBearerTokenChallengeAuthorizationPolicy(
            mockCredential, "https://disk.compute.azure.com/.default");

        String serviceChallengeResponseScope = "https://storage.azure.com";
        String authHeader
            = "Bearer authorization_uri=https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize resource_id="
                + serviceChallengeResponseScope;
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertEquals(serviceChallengeResponseScope, scope);
        assertEquals("https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize",
            authorization);
    }

    @Test
    public void testMultiTenantAuthentication() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String tenantId1 = "tenant1";
        String tenantId2 = "tenant2";

        String authHeader1 = "Bearer authorization_uri=https://login.microsoftonline.com/" + tenantId1
            + "/oauth2/authorize resource_id=https://storage.azure.com";
        String authHeader2 = "Bearer authorization_uri=https://login.microsoftonline.com/" + tenantId2
            + "/oauth2/authorize resource_id=https://storage.azure.com";

        Map<String, String> challenges1 = policy.extractChallengeAttributes(authHeader1);
        Map<String, String> challenges2 = policy.extractChallengeAttributes(authHeader2);

        String scope1 = challenges1.get(RESOURCE_ID);
        String authorization1 = challenges1.get(AUTHORIZATION_URI);
        String scope2 = challenges2.get(RESOURCE_ID);
        String authorization2 = challenges2.get(AUTHORIZATION_URI);

        assertEquals("https://storage.azure.com", scope1);
        assertEquals("https://login.microsoftonline.com/" + tenantId1 + "/oauth2/authorize", authorization1);
        assertEquals("https://storage.azure.com", scope2);
        assertEquals("https://login.microsoftonline.com/" + tenantId2 + "/oauth2/authorize", authorization2);
    }

    @Test
    public void testExtractTenantIdFromUri() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String uri = "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize";
        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";

        String actualTenantId = policy.extractTenantIdFromUri(uri);

        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    public void testExtractTenantIdFromUriInvalidUri() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String invalidUri = "https://login.microsoftonline.com/";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(invalidUri));

        String expectedMessage = "Invalid authorization URI: tenantId not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testExtractTenantIdFromUriMalformedUri() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String malformedUri = "ht!tp://invalid-uri";

        Exception exception = assertThrows(RuntimeException.class, () -> policy.extractTenantIdFromUri(malformedUri));

        String expectedMessage = "Invalid authorization URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void parsesCommaSeparatedAndQuotedValues() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String header = "Bearer authorization_uri=\"https://login.microsoftonline.com/tenant/oauth2/authorize\",  resource_id=\"https://storage.azure.com\" ,";
        Map<String,String> map = policy.extractChallengeAttributes(header);

        assertEquals("https://login.microsoftonline.com/tenant/oauth2/authorize", map.get("authorization_uri"));
        assertEquals("https://storage.azure.com", map.get("resource_id"));
        // trailing comma / empty token ignored
        assertEquals(2, map.size());
    }

    @Test
    public void malformedTokensAreIgnored() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "scope");

        String header = "Bearer novalue=  =badpair missingEquals resource_id= https://storage.azure.com=extra";
        Map<String,String> map = policy.extractChallengeAttributes(header);
        // All malformed -> empty map
        assertTrue(map.isEmpty());
    }

    /* //copilot stuff
    @Test
    public void missingResourceKeepsInitialScopes() {
        String initial = "https://storage.azure.com/.default";
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, initial);

        String header = "Bearer authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize";
        HttpResponse resp = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"),
            401, header);

        policy.authorizeRequestOnChallenge(newContext("https://example.com"), resp).block();

        assertArrayEquals(new String[]{initial}, cred.lastScopes.get());
    }

    @Test
    public void resourceAddsDefaultSuffixIfMissing() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://other/.default");

        String header = "Bearer resource_id=https://storage.azure.com authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize";
        HttpResponse resp = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"), 401, header);

        policy.authorizeRequestOnChallenge(newContext("https://example.com"), resp).block();

        assertArrayEquals(new String[]{"https://storage.azure.com/.default"}, cred.lastScopes.get());
    }

    @Test
    public void resourceWithExistingSuffixUnchanged() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://other/.default");

        String header = "Bearer resource_id=https://storage.azure.com/.default authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize";
        HttpResponse resp = new MockHttpResponse(new HttpRequest(HttpMethod.GET, "https://example.com"), 401, header);

        policy.authorizeRequestOnChallenge(newContext("https://example.com"), resp).block();

        assertArrayEquals(new String[]{"https://storage.azure.com/.default"}, cred.lastScopes.get());
    }

    @Test
    public void emptyHeaderReturnsEmptyMap() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "scope");
        assertTrue(policy.extractChallengeAttributes(null).isEmpty());
        assertTrue(policy.extractChallengeAttributes("Basic realm=xyz").isEmpty());
    }

    @Test
    public void concurrentInitialRequestsSingleTokenAcquisition() throws InterruptedException {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        // 200 OK never triggers challenge path; tests base-path caching.
        HttpClient stub = req -> Mono.just(new MockHttpResponse(req, 200));
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(policy).httpClient(stub).build();

        int n = 10;
        CountDownLatch latch = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            int idx = i;
            new Thread(() -> {
                try {
                    pipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com/" + idx)).block();
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        assertEquals(1, cred.calls.get(), "Only one token acquisition expected for concurrent initial requests");
    }

    @Test
    public void headerWithExtraWhitespaceParsed() {
        StorageBearerTokenChallengeAuthorizationPolicy policy =
            new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "scope");
        String header = "Bearer   authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize   resource_id=https://storage.azure.com   ";
        Map<String,String> map = policy.extractChallengeAttributes(header);
        assertEquals(2, map.size());
    }

     */

}
