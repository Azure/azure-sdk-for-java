// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.azure.v2.identity.implementation.client.ManagedIdentityClient;
import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import com.azure.v2.identity.util.TestConfigurationSource;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public class ManagedIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String OBJECT_ID = UUID.randomUUID().toString();
    private static final String RESOURCE_ID = "/subscriptions/" + UUID.randomUUID()
        + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";

    @Test
    public void testMiAuthFlow() {
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
        try (MockedConstruction<ManagedIdentityClient> managedIdentityMock
            = mockConstruction(ManagedIdentityClient.class, (miClient, context) -> {
                when(miClient.authenticate(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
            // test
            ManagedIdentityCredential credential
                = new ManagedIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
            AccessToken accessToken = credential.getToken(request1);
            Assertions.assertTrue(token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond());
            Assertions.assertNotNull(managedIdentityMock);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "clientId", "resourceId", "objectId" })
    public void testArcUserAssigned(String identityType) {
        // setup
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource());

        String clientId = "clientId".equals(identityType) ? CLIENT_ID : null;
        String resourceId = "resourceId".equals(identityType) ? RESOURCE_ID : null;
        String objectId = "objectId".equals(identityType) ? OBJECT_ID : null;

        // mock
        try (MockedStatic<ManagedIdentityApplication> applicationMock = mockStatic(ManagedIdentityApplication.class);
            MockedConstruction<ManagedIdentityClient> managedIdentityMock
                = mockConstruction(ManagedIdentityClient.class, (miClient, context) -> {
                    ManagedIdentityClientOptions options = (ManagedIdentityClientOptions) context.arguments().get(0);
                    Assertions.assertEquals(clientId, options.getClientId());
                    Assertions.assertEquals(resourceId, options.getResourceId());
                    Assertions.assertEquals(objectId, options.getObjectId());
                    when(miClient.authenticate(request)).thenReturn(TestUtils.getMockAccessToken(token, expiresAt));
                })) {
            applicationMock.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.AZURE_ARC);

            ManagedIdentityCredentialBuilder builder
                = new ManagedIdentityCredentialBuilder().configuration(configuration);
            if (clientId != null) {
                builder.clientId(clientId);
            } else if (resourceId != null) {
                builder.resourceId(resourceId);
            } else {
                builder.objectId(objectId);
            }

            // test
            AccessToken accessToken = builder.build().getToken(request);
            Assertions.assertEquals(token, accessToken.getToken());
            Assertions.assertEquals(expiresAt.getSecond(), accessToken.getExpiresAt().getSecond());
            Assertions.assertEquals(1, managedIdentityMock.constructed().size());
        }
    }

    @Test
    public void testCloudShellUserAssigned() {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource());

        try (MockedStatic<ManagedIdentityApplication> applicationMock = mockStatic(ManagedIdentityApplication.class)) {
            applicationMock.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.CLOUD_SHELL);

            // test
            ManagedIdentityCredential credential
                = new ManagedIdentityCredentialBuilder().configuration(configuration).objectId(OBJECT_ID).build();
            CredentialUnavailableException exception
                = Assertions.assertThrows(CredentialUnavailableException.class, () -> credential.getToken(request));
            Assertions.assertTrue(
                exception.getMessage().contains("User-assigned managed identity is not supported in CLOUD_SHELL"));
        }
    }
}
