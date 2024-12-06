// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@LiveOnly
public class AzurePipelinesCredentialTest extends TestProxyTestBase {

    static String clientId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_CLIENT_ID");
    static String tenantId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_TENANT_ID");

    static String serviceConnectionId = Configuration.getGlobalConfiguration().get("AZURE_SERVICE_CONNECTION_ID");
    static String systemAccessToken = Configuration.getGlobalConfiguration().get("SYSTEM_ACCESSTOKEN");

    private static AzurePipelinesCredential getCredential() {
        return new AzurePipelinesCredentialBuilder().clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .systemAccessToken(systemAccessToken)
            .build();
    }

    private static AzurePipelinesCredential getInvalidCredential() {
        return new AzurePipelinesCredentialBuilder().clientId(clientId)
            .tenantId(tenantId)
            .serviceConnectionId(serviceConnectionId)
            .systemAccessToken("InvalidToken")
            .build();
    }

    @Test
    public void testGetTokenFromPipeline() {
        // Arrange
        AzurePipelinesCredential credential = getCredential();
        // Act & Assert
        StepVerifier
            .create(credential.getToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .assertNext(accessToken -> {
                assertNotNull(accessToken.getToken());
                assertNotNull(accessToken.getExpiresAt());
            })
            .verifyComplete();
    }

    @Test
    public void testGetTokenFromPipelineSync() {
        // Arrange
        AzurePipelinesCredential credential = getCredential();

        // Act & Assert
        assertNotNull(credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default")));
    }

    @Test
    public void noRedirectOnFailure() {
        AzurePipelinesCredential credential = getInvalidCredential();
        StepVerifier
            .create(credential.getToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .expectErrorMatches(
                throwable -> ((ClientAuthenticationException) throwable).getResponse().getStatusCode() == 401)
            .verify();
    }

    @Test
    public void validateHeadersInException() {
        AzurePipelinesCredential credential = getInvalidCredential();
        StepVerifier
            .create(credential.getToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .expectErrorMatches(throwable -> throwable.getMessage().contains("x-vss-e2eid")
                && throwable.getMessage().contains("x-msedge-ref"))
            .verify();
    }
}
