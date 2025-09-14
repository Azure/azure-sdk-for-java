// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy;

import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StorageBearerTokenChallengeAuthorizationPolicyTests {

    private TokenCredential mockCredential;
    private StorageBearerTokenChallengeAuthorizationPolicy defaultPolicy;
    private static final String RESOURCE_ID = "resource_id";
    private static final String AUTHORIZATION_URI = "authorization_uri";
    private static final String DEFAULT_SCOPE = "https://storage.azure.com/.default";
    private static final String RESPONSE_SCOPE = "https://storage.azure.com";
    private static final String DEFAULT_AUTH_HEADER
        = "Bearer authorization_uri=https://login.microsoftonline.com/tenantId/oauth2/authorize resource_id=https://storage.azure.com";

    @BeforeEach
    public void setup() {
        mockCredential = new MockTokenCredential();
        defaultPolicy = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, DEFAULT_SCOPE);
    }

    @Test
    public void testExtractChallengeAttributes() {
        Map<String, String> challenges = defaultPolicy.extractChallengeAttributes(DEFAULT_AUTH_HEADER);

        assertNotNull(challenges);
        assertEquals("https://login.microsoftonline.com/tenantId/oauth2/authorize",
            challenges.get("authorization_uri"));
        assertEquals(RESPONSE_SCOPE, challenges.get("resource_id"));
    }

    @Test
    public void usesTokenProvidedByCredentials() {
        Map<String, String> challenges = defaultPolicy.extractChallengeAttributes(null);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertNull(scope);
        assertNull(authorization);
    }

    @Test
    public void sendsUnauthorizedRequestWhenEnableTenantDiscoveryIsTrue() {
        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";
        String authHeader = "Bearer authorization_uri=https://login.microsoftonline.com/" + expectedTenantId
            + "/oauth2/authorize resource_id=https://storage.azure.com";
        Map<String, String> challenges = defaultPolicy.extractChallengeAttributes(authHeader);

        String scope = challenges.get(RESOURCE_ID);
        String authorization = challenges.get(AUTHORIZATION_URI);

        assertEquals(RESPONSE_SCOPE, scope);
        assertEquals("https://login.microsoftonline.com/" + expectedTenantId + "/oauth2/authorize", authorization);
    }

    @Test
    public void testMultiTenantAuthentication() {
        String tenantId1 = "tenant1";
        String tenantId2 = "tenant2";

        String authHeader1 = "Bearer authorization_uri=https://login.microsoftonline.com/" + tenantId1
            + "/oauth2/authorize resource_id=" + RESPONSE_SCOPE;
        String authHeader2 = "Bearer authorization_uri=https://login.microsoftonline.com/" + tenantId2
            + "/oauth2/authorize resource_id=" + RESPONSE_SCOPE;

        Map<String, String> challenges1 = defaultPolicy.extractChallengeAttributes(authHeader1);
        Map<String, String> challenges2 = defaultPolicy.extractChallengeAttributes(authHeader2);

        String scope1 = challenges1.get(RESOURCE_ID);
        String authorization1 = challenges1.get(AUTHORIZATION_URI);
        String scope2 = challenges2.get(RESOURCE_ID);
        String authorization2 = challenges2.get(AUTHORIZATION_URI);

        assertEquals(RESPONSE_SCOPE, scope1);
        assertEquals("https://login.microsoftonline.com/" + tenantId1 + "/oauth2/authorize", authorization1);
        assertEquals(RESPONSE_SCOPE, scope2);
        assertEquals("https://login.microsoftonline.com/" + tenantId2 + "/oauth2/authorize", authorization2);
    }

    @Test
    public void testExtractTenantIdFromUri() {
        String uri = "https://login.microsoftonline.com/72f988bf-86f1-41af-91ab-2d7cd011db47/oauth2/authorize";
        String expectedTenantId = "72f988bf-86f1-41af-91ab-2d7cd011db47";

        String actualTenantId = defaultPolicy.extractTenantIdFromUri(uri);

        assertEquals(expectedTenantId, actualTenantId);
    }

    @Test
    public void testExtractTenantIdFromUriInvalidUri() {
        String invalidUri = "https://login.microsoftonline.com/";

        Exception exception
            = assertThrows(RuntimeException.class, () -> defaultPolicy.extractTenantIdFromUri(invalidUri));

        String expectedMessage = "Invalid authorization URI: tenantId not found";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void testExtractTenantIdFromUriMalformedUri() {
        String malformedUri = "ht!tp://invalid-uri";

        Exception exception
            = assertThrows(RuntimeException.class, () -> defaultPolicy.extractTenantIdFromUri(malformedUri));

        String expectedMessage = "Invalid authorization URI";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    public void parsesCommaSeparatedAndQuotedValues() {
        String header
            = "Bearer authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize,  resource_id=\"https://storage.azure.com\" ,";
        Map<String, String> map = defaultPolicy.extractChallengeAttributes(header);

        assertEquals("https://login.microsoftonline.com/tenant/oauth2/authorize", map.get("authorization_uri"));
        assertEquals(RESPONSE_SCOPE, map.get("resource_id"));
        // trailing comma / empty token ignored
        assertEquals(2, map.size());
    }

    @Test
    public void emptyHeaderReturnsEmptyMap() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "scope");
        assertTrue(policy.extractChallengeAttributes(null).isEmpty());
        assertTrue(policy.extractChallengeAttributes("Basic realm=xyz").isEmpty());
    }

    @Test
    public void headerWithExtraWhitespaceParsed() {
        StorageBearerTokenChallengeAuthorizationPolicy policy
            = new StorageBearerTokenChallengeAuthorizationPolicy(mockCredential, "scope");
        String header
            = "Bearer   authorization_uri=https://login.microsoftonline.com/tenant/oauth2/authorize   resource_id=https://storage.azure.com   ";
        Map<String, String> map = policy.extractChallengeAttributes(header);
        assertEquals(2, map.size());
    }

}
