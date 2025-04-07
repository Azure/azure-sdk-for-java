// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.identity.implementation.client.ManagedIdentityClient;
import com.azure.v2.identity.util.TestConfigurationSource;
import com.azure.v2.identity.util.TestUtils;
import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class ManagedIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

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
}
