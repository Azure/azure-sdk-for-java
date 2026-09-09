// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenRequestContext;
import com.azure.v2.identity.implementation.client.ManagedIdentityClient;
import com.azure.v2.identity.implementation.models.ManagedIdentityClientOptions;
import com.azure.v2.identity.util.TestConfigurationSource;
import com.azure.v2.identity.util.TestUtils;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultAzureCredentialTest {
    private static final String CLIENT_ID = UUID.randomUUID().toString();
    private static final String RESOURCE_ID = "/subscriptions/" + UUID.randomUUID()
        + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";

    @ParameterizedTest
    @ValueSource(strings = { "clientId", "resourceId" })
    public void testUseArcUserAssignedManagedIdentityCredential(String identityType) {
        // setup
        String token = "token";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource());

        String clientId = "clientId".equals(identityType) ? CLIENT_ID : null;
        String resourceId = "resourceId".equals(identityType) ? RESOURCE_ID : null;

        // mock
        try (MockedStatic<ManagedIdentityApplication> applicationMock = mockStatic(ManagedIdentityApplication.class);
            MockedConstruction<ManagedIdentityClient> managedIdentityMock
                = mockConstruction(ManagedIdentityClient.class, (miClient, context) -> {
                    ManagedIdentityClientOptions options = (ManagedIdentityClientOptions) context.arguments().get(0);
                    Assertions.assertEquals(clientId, options.getClientId());
                    Assertions.assertEquals(resourceId, options.getResourceId());
                    when(miClient.authenticate(request)).thenReturn(TestUtils.getMockAccessToken(token, expiresAt));
                })) {
            applicationMock.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.AZURE_ARC);

            DefaultAzureCredentialBuilder builder = new DefaultAzureCredentialBuilder().configuration(configuration);
            if (clientId != null) {
                builder.managedIdentityClientId(clientId);
            } else {
                builder.managedIdentityResourceId(resourceId);
            }
            DefaultAzureCredential credential = builder.build();

            // test
            AccessToken firstToken = credential.getToken(request);
            AccessToken cachedToken = credential.getToken(request);
            Assertions.assertEquals(token, firstToken.getToken());
            Assertions.assertEquals(token, cachedToken.getToken());
            Assertions.assertEquals(expiresAt.getSecond(), firstToken.getExpiresAt().getSecond());
            Assertions.assertEquals(1, managedIdentityMock.constructed().size());
            verify(managedIdentityMock.constructed().get(0), times(2)).authenticate(request);
        }
    }
}
