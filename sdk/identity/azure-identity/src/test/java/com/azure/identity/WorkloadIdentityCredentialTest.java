// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.MockedConstruction;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

public class WorkloadIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testWorkloadIdentityFlow() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            // setup
            String endpoint = "https://localhost";
            String token1 = "token1";
            TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint); // This must stay to signal we are in an app service context
            configuration.put(Configuration.PROPERTY_AZURE_CLIENT_ID, "dummy-clientId");
            configuration.put(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE, "dummy-file");
            configuration.put(Configuration.PROPERTY_AZURE_TENANT_ID, "dummy-tenant");

            // mock
            try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithExchangeToken(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
                // test
                WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
                StepVerifier.create(credential.getToken(request1))
                    .expectNextMatches(token -> token1.equals(token.getToken())
                        && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                    .verifyComplete();
                Assert.assertNotNull(identityClientMock);
            }
        } finally {
            // clean up
            configuration.remove(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
            configuration.remove(Configuration.PROPERTY_AZURE_CLIENT_ID);
            configuration.remove(ManagedIdentityCredential.AZURE_FEDERATED_TOKEN_FILE);
            configuration.remove(Configuration.PROPERTY_AZURE_TENANT_ID);
        }
    }

    @Test
    public void testWorkloadIdentityFlowFailure() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            // setup
            String endpoint = "https://localhost";
            String token1 = "token1";
            TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put(Configuration.PROPERTY_AZURE_AUTHORITY_HOST, endpoint); // This must stay to signal we are in an app service context
            configuration.put(Configuration.PROPERTY_AZURE_CLIENT_ID, "dummy-clientId");

            // mock
            try (MockedConstruction<IdentityClient> identityClientMock = mockConstruction(IdentityClient.class, (identityClient, context) -> {
                when(identityClient.authenticateWithExchangeToken(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            })) {
                // test
                WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().configuration(configuration).clientId(CLIENT_ID).build();
                StepVerifier.create(credential.getToken(request1)).expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage().startsWith("WorkloadIdentityCredential authentication unavailable. ")).verify();
                Assert.assertNotNull(identityClientMock);
            }
        } finally {
            // clean up
            configuration.remove(Configuration.PROPERTY_AZURE_AUTHORITY_HOST);
            configuration.remove(Configuration.PROPERTY_AZURE_CLIENT_ID);
        }
    }
}

