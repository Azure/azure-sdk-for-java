// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ManagedIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String OBJECT_ID = UUID.randomUUID().toString();

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId("foo").build();
        assertEquals("foo", credential.getClientId());
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testInvalidJsonResponse(boolean isChained) {
        HttpClient client = TestUtils.getMockHttpClient(
            getMockResponse(400,
                "{\"error\":\"invalid_request\",\"error_description\":\"Required metadata header not specified\"}"),
            getMockResponse(200, "invalid json"));

        String endpoint = "http://localhost";
        String secret = "secret";

        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("MSI_ENDPOINT", endpoint) // This must stay to signal we are in an app service context
                .put("MSI_SECRET", secret)
                .put("IDENTITY_ENDPOINT", endpoint)
                .put("IDENTITY_HEADER", secret));

        IdentityClientOptions options
            = new IdentityClientOptions().setChained(isChained).setHttpClient(client).setConfiguration(configuration);
        ManagedIdentityCredential cred = new ManagedIdentityCredential("clientId", null, null, options);
        StepVerifier.create(cred.getToken(new TokenRequestContext().addScopes("https://management.azure.com")))
            .expectErrorMatches(t -> {
                if (isChained) {
                    return t instanceof CredentialUnavailableException;
                } else {
                    return t instanceof ClientAuthenticationException;
                }
            })
            .verify();

    }

    @Test
    public void testMSIEndpoint() {
        // setup
        String endpoint = "http://localhost";
        String secret = "secret";
        String token1 = "token1";
        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("MSI_ENDPOINT", endpoint) // This must stay to signal we are in an app service context
                .put("MSI_SECRET", secret)
                .put("IDENTITY_ENDPOINT", endpoint)
                .put("IDENTITY_HEADER", secret));

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithManagedIdentityMsalClient(request1))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            // test
            ManagedIdentityCredential credential
                = new ManagedIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testIMDS() {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        try (MockedConstruction<IdentityClient> identityClientMock
            = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithManagedIdentityMsalClient(request))
                    .thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));

            })) {
            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresOn.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
            Assertions.assertNotNull(identityClientMock);
        }
    }

    @Test
    public void testArcUserAssigned() {
        // setup
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put("IDENTITY_ENDPOINT", endpoint).put("IMDS_ENDPOINT", endpoint));

        // test
        ManagedIdentityCredential credential
            = new ManagedIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof ClientAuthenticationException)
            .verify();
    }

    @Test
    public void testCloudshellUserAssigned() {
        // setup
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("MSI_ENDPOINT", endpoint));

        // test
        ManagedIdentityCredential credential
            = new ManagedIdentityCredentialBuilder().configuration(configuration).objectId(OBJECT_ID).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof ClientAuthenticationException)
            .verify();
    }

    @Test
    public void testInvalidIdCombination() {
        // setup
        String resourceId = "/subscriptions/" + UUID.randomUUID()
            + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";
        String objectId = "2323-sd2323s-32323-32334-34343";

        // test
        assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).resourceId(resourceId).build());

        assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID)
                .resourceId(resourceId)
                .objectId(objectId)
                .build());

        assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).objectId(objectId).build());

        assertThrows(IllegalStateException.class,
            () -> new ManagedIdentityCredentialBuilder().resourceId(resourceId).objectId(objectId).build());
    }

    @Test
    public void testAksExchangeTokenCredentialCreated() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "tenantId")
                .put("AZURE_CLIENT_ID", "clientId")
                .put("AZURE_FEDERATED_TOKEN_FILE", "tokenFile"));

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertInstanceOf(AksExchangeTokenCredential.class, cred.managedIdentityServiceCredential);
    }

    private MockHttpResponse getMockResponse(int code, String body) {
        return new MockHttpResponse(null, code, body.getBytes(StandardCharsets.UTF_8));
    }
}
