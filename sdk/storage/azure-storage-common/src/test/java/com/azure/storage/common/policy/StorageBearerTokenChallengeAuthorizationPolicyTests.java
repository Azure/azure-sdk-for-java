// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static com.azure.storage.common.policy.StorageBearerTokenChallengeAuthorizationPolicy.BEARER_TOKEN_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private String[] scopes;
    private TokenCredential mockCredential;

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
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader, BEARER_TOKEN_PREFIX);

        assertNotNull(challenges);
        assertEquals("https://login.microsoftonline.com/tenantid/oauth2/authorize",
            challenges.get("authorization_uri"));
        assertEquals("https://storage.azure.com", challenges.get("resource_id"));
    }

    @Test
    public void testGetScopeFromChallenges() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        Map<String, String> challenges = new HashMap<>();
        challenges.put("resource_id", "https://storage.azure.com");

        String scope = policy.getScopeFromChallenges(challenges);

        assertEquals("https://storage.azure.com", scope);
    }

    @Test
    public void testGetAuthorizationFromChallenges() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        Map<String, String> challenges = new HashMap<>();
        challenges.put("authorization_uri", "https://login.microsoftonline.com/tenantId/oauth2/authorize");

        String authorization = policy.getAuthorizationFromChallenges(challenges);

        assertEquals("https://login.microsoftonline.com/tenantId/oauth2/authorize", authorization);
    }

    @Test
    public void usesTokenProvidedByCredentials() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        Map<String, String> challenges = policy.extractChallengeAttributes(null, BEARER_TOKEN_PREFIX);

        String scope = policy.getScopeFromChallenges(challenges);
        String authorization = policy.getAuthorizationFromChallenges(challenges);

        assertNull(scope);
        assertNull(authorization);
    }

    @Test
    public void doesNotSendUnauthorizedRequestWhenEnableTenantDiscoveryIsFalse() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, scopes);

        Map<String, String> challenges = policy.extractChallengeAttributes(null, BEARER_TOKEN_PREFIX);

        String scope = policy.getScopeFromChallenges(challenges);
        String authorization = policy.getAuthorizationFromChallenges(challenges);

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
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader, BEARER_TOKEN_PREFIX);

        String scope = policy.getScopeFromChallenges(challenges);
        String authorization = policy.getAuthorizationFromChallenges(challenges);

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
        Map<String, String> challenges = policy.extractChallengeAttributes(authHeader, BEARER_TOKEN_PREFIX);

        String scope = policy.getScopeFromChallenges(challenges);
        String authorization = policy.getAuthorizationFromChallenges(challenges);

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

        Map<String, String> challenges1 = policy.extractChallengeAttributes(authHeader1, BEARER_TOKEN_PREFIX);
        Map<String, String> challenges2 = policy.extractChallengeAttributes(authHeader2, BEARER_TOKEN_PREFIX);

        String scope1 = policy.getScopeFromChallenges(challenges1);
        String authorization1 = policy.getAuthorizationFromChallenges(challenges1);
        String scope2 = policy.getScopeFromChallenges(challenges2);
        String authorization2 = policy.getAuthorizationFromChallenges(challenges2);

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

        Exception exception = assertThrows(RuntimeException.class, () -> {
            policy.extractTenantIdFromUri(invalidUri);
        });

        String expectedMessage = "Invalid authorization URI: tenantId not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testExtractTenantIdFromUriMalformedUri() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "https://storage.azure.com/.default");

        String malformedUri = "ht!tp://invalid-uri";

        Exception exception = assertThrows(RuntimeException.class, () -> {
            policy.extractTenantIdFromUri(malformedUri);
        });

        String expectedMessage = "Invalid authorization URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

}
