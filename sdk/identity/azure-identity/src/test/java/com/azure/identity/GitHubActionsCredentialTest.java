// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentitySyncClient;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class GitHubActionsCredentialTest {
    private static final ClientLogger LOGGER = new ClientLogger(GitHubActionsCredentialTest.class);

    @Test
    public void testDeriveAudience() {
        assertEquals("api://AzureADTokenExchange",
            GitHubActionsCredential.deriveAudience("https://login.microsoftonline.com"));
        assertEquals("api://AzureADTokenExchange",
            GitHubActionsCredential.deriveAudience("https://login.microsoftonline.com/"));
        assertEquals("api://AzureADTokenExchangeUSGov",
            GitHubActionsCredential.deriveAudience("https://login.microsoftonline.us"));
        assertEquals("api://AzureADTokenExchangeUSGov",
            GitHubActionsCredential.deriveAudience("https://login.microsoftonline.us/"));
        assertEquals("api://AzureADTokenExchangeChina",
            GitHubActionsCredential.deriveAudience("https://login.chinacloudapi.cn"));
        assertEquals("api://AzureADTokenExchangeChina",
            GitHubActionsCredential.deriveAudience("https://login.chinacloudapi.cn/"));
        assertEquals("api://AzureADTokenExchangeFrance",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.fr"));
        assertEquals("api://AzureADTokenExchangeFrance",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.fr/"));
        assertEquals("api://AzureADTokenExchangeGermany",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.de"));
        assertEquals("api://AzureADTokenExchangeGermany",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.de/"));
        assertEquals("api://AzureADTokenExchangeGovSG",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.sg"));
        assertEquals("api://AzureADTokenExchangeGovSG",
            GitHubActionsCredential.deriveAudience("https://login.sovcloud-identity.sg/"));
    }

    @Test
    public void testDeriveAudienceThrowsForUnknownHost() {
        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> GitHubActionsCredential.deriveAudience("https://custom.authority.example.com"));
        assertTrue(
            e.getMessage().contains("The authority host \"https://custom.authority.example.com\" is not supported."));
    }

    @Test
    public void testDeriveAudienceThrowsForInvalidUrl() {
        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> GitHubActionsCredential.deriveAudience("not-a-url"));
        assertTrue(e.getMessage().contains("The authority host \"not-a-url\" is not supported."));
    }

    @Test
    public void testDeriveAudienceThrowsForNullOrEmpty() {
        CredentialUnavailableException e1
            = assertThrows(CredentialUnavailableException.class, () -> GitHubActionsCredential.deriveAudience(null));
        assertTrue(e1.getMessage().contains("is not supported"));

        CredentialUnavailableException e2
            = assertThrows(CredentialUnavailableException.class, () -> GitHubActionsCredential.deriveAudience(""));
        assertTrue(e2.getMessage().contains("is not supported"));
    }

    @Test
    public void testHandleOidcResponseValid() {
        String json = "{\"value\":\"test-jwt-token\"}";
        MockHttpResponse response = new MockHttpResponse(null, 200, json.getBytes(StandardCharsets.UTF_8));
        String result = GitHubActionsCredential.handleOidcResponse(response, LOGGER);
        assertEquals("test-jwt-token", result);
    }

    @Test
    public void testHandleOidcResponseNullOrEmptyBody() {
        MockHttpResponse response = new MockHttpResponse(null, 400, (byte[]) null);
        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> GitHubActionsCredential.handleOidcResponse(response, LOGGER));
        assertTrue(e.getMessage().contains("Received null token from OIDC request. Status code: 400"));
    }

    @Test
    public void testHandleOidcResponseNon200() {
        String json = "{\"value\":\"test-jwt-token\"}";
        MockHttpResponse response = new MockHttpResponse(null, 500, json.getBytes(StandardCharsets.UTF_8));
        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> GitHubActionsCredential.handleOidcResponse(response, LOGGER));
        assertTrue(e.getMessage().contains("OIDC request returned status code 500"));
    }

    @Test
    public void testHandleOidcResponseMissingValue() {
        String json = "{\"error\":\"Bad Request\"}";
        MockHttpResponse response = new MockHttpResponse(null, 200, json.getBytes(StandardCharsets.UTF_8));
        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> GitHubActionsCredential.handleOidcResponse(response, LOGGER));
        assertTrue(e.getMessage().contains("\"value\" field not detected in the response"));
    }

    @Test
    public void testHandleOidcResponseInvalidJsonDoesNotLeakBody() {
        String sensitiveAssertion = "test-sensitive-assertion";
        MockHttpResponse response
            = new MockHttpResponse(null, 200, sensitiveAssertion.getBytes(StandardCharsets.UTF_8));
        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> GitHubActionsCredential.handleOidcResponse(response, LOGGER));
        assertTrue(e.getMessage().contains("Failed to parse OIDC response"));
        assertFalse(e.getMessage().contains(sensitiveAssertion));
    }

    @Test
    public void testHandleOidcResponseStatusInNullBody() {
        MockHttpResponse response = new MockHttpResponse(null, 401, (byte[]) null);
        ClientAuthenticationException e = assertThrows(ClientAuthenticationException.class,
            () -> GitHubActionsCredential.handleOidcResponse(response, LOGGER));
        assertTrue(e.getMessage().contains("Status code: 401"));
    }

    @Test
    public void testTokenAcquisitionAsync() {
        String tokenStr = "test-access-token";
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL",
                    "https://token.actions.githubusercontent.com/request?api-version=1.0")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-request-token"));

        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
                when(identityClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(TestUtils.getMockAccessToken(tokenStr, expiresAt));
            })) {

            GitHubActionsCredential credential
                = new GitHubActionsCredentialBuilder().configuration(configuration).build();

            StepVerifier.create(credential.getToken(request)).assertNext(token -> {
                assertEquals(tokenStr, token.getToken());
                assertEquals(expiresAt.getSecond(), token.getExpiresAt().getSecond());
            }).verifyComplete();
            assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testTokenAcquisitionSync() {
        String tokenStr = "test-access-token";
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-request-token"));

        try (MockedConstruction<IdentitySyncClient> identitySyncClientMock
            = mockConstruction(IdentitySyncClient.class, (identitySyncClient, context) -> {
                when(identitySyncClient.authenticateWithConfidentialClientCache(any())).thenReturn(null);
                when(identitySyncClient.authenticateWithConfidentialClient(any(TokenRequestContext.class)))
                    .thenReturn(new AccessToken(tokenStr, expiresAt));
            })) {

            GitHubActionsCredential credential
                = new GitHubActionsCredentialBuilder().configuration(configuration).build();

            AccessToken token = credential.getTokenSync(request);
            assertNotNull(token);
            assertEquals(tokenStr, token.getToken());
            assertEquals(expiresAt.getSecond(), token.getExpiresAt().getSecond());
            assertNotNull(identitySyncClientMock);
        }
    }
}
