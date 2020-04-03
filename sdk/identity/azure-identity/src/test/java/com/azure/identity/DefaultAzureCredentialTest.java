// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class DefaultAzureCredentialTest {

    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testUseEnvironmentCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            // setup
            String secret = "secret";
            String token1 = "token1";
            TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("AZURE_CLIENT_ID", clientId);
            configuration.put("AZURE_CLIENT_SECRET", secret);
            configuration.put("AZURE_TENANT_ID", tenantId);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateWithClientSecret(secret, request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                    && expiresOn.getSecond() == accessToken.getExpiresAt().getSecond())
                .verifyComplete();
        } finally {
            // clean up
            configuration.remove("AZURE_CLIENT_ID");
            configuration.remove("AZURE_CLIENT_SECRET");
            configuration.remove("AZURE_TENANT_ID");
        }
    }

    @Test
    public void testUseManagedIdentityCredential() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }
    
    @Test
    public void testUseAzureCliCredential() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(Mono.empty());
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectNextMatches(accessToken -> token1.equals(accessToken.getToken())
                && expiresAt.getSecond() == accessToken.getExpiresAt().getSecond())
            .verifyComplete();
    }

    @Test
    public void testNoCredentialWorks() throws Exception {
        // setup
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(Mono.error(new RuntimeException("Cannot get token from managed identity")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        SharedTokenCacheCredential sharedTokenCacheCredential = PowerMockito.mock(SharedTokenCacheCredential.class);
        when(sharedTokenCacheCredential.getToken(request)).thenReturn(Mono.error(new RuntimeException("Cannot get token from shared token cache")));
        PowerMockito.whenNew(SharedTokenCacheCredential.class).withAnyArguments().thenReturn(sharedTokenCacheCredential);

        AzureCliCredential azureCliCredential = PowerMockito.mock(AzureCliCredential.class);
        when(azureCliCredential.getToken(request)).thenReturn(Mono.error(new RuntimeException("Cannot get token from Azure CLI credential")));
        PowerMockito.whenNew(AzureCliCredential.class).withAnyArguments().thenReturn(azureCliCredential);


        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage()
                                      .matches("Tried EnvironmentCredential, ManagedIdentityCredential, "
                                                   + "SharedTokenCacheCredential"
                                                   + "[\\$\\w]+\\$\\d*,\\s+AzureCliCredential[\\$\\w\\s\\.]+"))
            .verify();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExcludeCredentials() throws Exception {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                                                .excludeEnvironmentCredential()
                                                .excludeAzureCliCredential()
                                                .excludeManagedIdentityCredential()
                                                .excludeSharedTokenCacheCredential()
                                                .build();
    }

    @Test
    public void testExclueEnvironmentCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                                                .excludeEnvironmentCredential()
                                                .build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage()
                                      .startsWith("Tried ManagedIdentityCredential, "
                                                   + "SharedTokenCacheCredential, "
                                                   + "AzureCliCredential"))
            .verify();
    }

    @Test
    public void testExclueManagedIdentityCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                                                .excludeManagedIdentityCredential()
                                                .build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage()
                                      .startsWith("Tried EnvironmentCredential, "
                                                   + "SharedTokenCacheCredential, "
                                                   + "AzureCliCredential"))
            .verify();
    }

    @Test
    public void testExcludeSharedTokenCacheCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                                                .excludeEnvironmentCredential()
                                                .excludeSharedTokenCacheCredential()
                                                .build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage()
                                      .startsWith("Tried "
                                                      + "ManagedIdentityCredential, "
                                                      + "AzureCliCredential"))
            .verify();
    }

    @Test
    public void testExcludeAzureCliCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                                                .excludeEnvironmentCredential()
                                                .excludeAzureCliCredential()
                                                .build();
        StepVerifier.create(credential.getToken(request1))
            .expectErrorMatches(t -> t instanceof RuntimeException && t.getMessage()
                                      .startsWith("Tried "
                                                      + "ManagedIdentityCredential, "
                                                      + "SharedTokenCacheCredential but"))
            .verify();
    }
}
