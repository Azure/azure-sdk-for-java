// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.models.OidcTokenResponse;
import com.azure.json.JsonProviders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AzurePipelinesCredentialTest extends TestProxyTestBase {

    static String clientId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_CLIENT_ID");
    static String tenantId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_TENANT_ID");

    static String serviceConnectionId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_ID");

    private AzurePipelinesCredential getCredential() {
        return new AzurePipelinesCredentialBuilder().clientId(clientId).tenantId(tenantId).serviceConnectionId(serviceConnectionId).build();
    }

    @Test
    @LiveOnly
    public void testGetToken() {
        // Arrange

        AzurePipelinesCredential credential = getCredential();

        // Act & Assert
        StepVerifier.create(credential.getToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .assertNext(accessToken -> {
                assertNotNull(accessToken.getToken());
                assertNotNull(accessToken.getExpiresAt());
            })
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void testGetTokenSync() {
        // Arrange
        AzurePipelinesCredential credential = getCredential();

        // Act & Assert
        assertNotNull(credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default")));
    }
}
