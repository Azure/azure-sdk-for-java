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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "javax.net.ssl.*",
    "io.netty.handler.ssl.*", "io.netty.buffer.*", "io.netty.channel.*"})
public class DefaultAzureCredentialTest {

    private static final String TENANT_ID = "contoso.com";
    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testUseEnvironmentCredential() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            // setup
            String secret = "secret";
            String token1 = "token1";
            TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("AZURE_CLIENT_ID", CLIENT_ID);
            configuration.put("AZURE_CLIENT_SECRET", secret);
            configuration.put("AZURE_TENANT_ID", TENANT_ID);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateWithConfidentialClientCache(any())).thenReturn(Mono.empty());
            when(identityClient.authenticateWithConfidentialClient(request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withArguments(eq(TENANT_ID), eq(CLIENT_ID), eq(secret), isNull(), isNull(), isNull(), eq(false), any()).thenReturn(identityClient);

            IntelliJCredential intelliJCredential = PowerMockito.mock(IntelliJCredential.class);
            when(intelliJCredential.getToken(request1))
                .thenReturn(Mono.empty());
            PowerMockito.whenNew(IntelliJCredential.class).withAnyArguments()
                .thenReturn(intelliJCredential);

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

        IntelliJCredential intelliJCredential = PowerMockito.mock(IntelliJCredential.class);
        when(intelliJCredential.getToken(request))
            .thenReturn(Mono.empty());
        PowerMockito.whenNew(IntelliJCredential.class).withAnyArguments()
            .thenReturn(intelliJCredential);

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
        IntelliJCredential intelliJCredential = PowerMockito.mock(IntelliJCredential.class);
        when(intelliJCredential.getToken(request))
            .thenReturn(Mono.empty());
        PowerMockito.whenNew(IntelliJCredential.class).withAnyArguments()
            .thenReturn(intelliJCredential);

        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateWithAzureCli(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(Mono.empty());
        when(identityClient.authenticateWithSharedTokenCache(request, null)).thenReturn(Mono.empty());
        when(identityClient.authenticateWithIntelliJ(request)).thenReturn(Mono.empty());
        when(identityClient.authenticateWithVsCodeCredential(any(), any())).thenReturn(Mono.empty());
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
        when(identityClient.authenticateToIMDSEndpoint(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from managed identity")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments()
            .thenReturn(identityClient);

        SharedTokenCacheCredential sharedTokenCacheCredential = PowerMockito.mock(SharedTokenCacheCredential.class);
        when(sharedTokenCacheCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from shared token cache")));
        PowerMockito.whenNew(SharedTokenCacheCredential.class).withAnyArguments()
            .thenReturn(sharedTokenCacheCredential);

        AzureCliCredential azureCliCredential = PowerMockito.mock(AzureCliCredential.class);
        when(azureCliCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Azure CLI credential")));
        PowerMockito.whenNew(AzureCliCredential.class).withAnyArguments()
            .thenReturn(azureCliCredential);

        AzurePowerShellCredential azurePowerShellCredential = PowerMockito.mock(AzurePowerShellCredential.class);
        when(azurePowerShellCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException(
                "Cannot get token from Azure PowerShell credential")));
        PowerMockito.whenNew(AzurePowerShellCredential.class).withAnyArguments()
            .thenReturn(azurePowerShellCredential);

        IntelliJCredential intelliJCredential = PowerMockito.mock(IntelliJCredential.class);
        when(intelliJCredential.getToken(request))
            .thenReturn(Mono.error(
                new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        PowerMockito.whenNew(IntelliJCredential.class).withAnyArguments()
            .thenReturn(intelliJCredential);

        VisualStudioCodeCredential vscodeCredential = PowerMockito.mock(VisualStudioCodeCredential.class);
        when(vscodeCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from VS Code credential")));
        PowerMockito.whenNew(VisualStudioCodeCredential.class).withAnyArguments()
            .thenReturn(vscodeCredential);

        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage()
                .startsWith("EnvironmentCredential authentication unavailable. "))
            .verify();
    }

    @Test
    public void testCredentialUnavailable() throws Exception {
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");

        ManagedIdentityCredential managedIdentityCredential = PowerMockito.mock(ManagedIdentityCredential.class);
        when(managedIdentityCredential.getToken(request))
            .thenReturn(Mono.error(
                new CredentialUnavailableException("Cannot get token from Managed Identity credential")));
        PowerMockito.whenNew(ManagedIdentityCredential.class).withAnyArguments()
            .thenReturn(managedIdentityCredential);

        IntelliJCredential intelliJCredential = PowerMockito.mock(IntelliJCredential.class);
        when(intelliJCredential.getToken(request))
            .thenReturn(Mono.error(
                new CredentialUnavailableException("Cannot get token from IntelliJ Credential")));
        PowerMockito.whenNew(IntelliJCredential.class).withAnyArguments()
            .thenReturn(intelliJCredential);
        VisualStudioCodeCredential vscodeCredential = PowerMockito.mock(VisualStudioCodeCredential.class);
        when(vscodeCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from VS Code credential")));
        PowerMockito.whenNew(VisualStudioCodeCredential.class).withAnyArguments()
            .thenReturn(vscodeCredential);

        AzurePowerShellCredential powerShellCredential = PowerMockito.mock(AzurePowerShellCredential.class);
        when(powerShellCredential.getToken(request))
            .thenReturn(Mono.error(new CredentialUnavailableException("Cannot get token from Powershell credential")));
        PowerMockito.whenNew(AzurePowerShellCredential.class).withAnyArguments()
            .thenReturn(powerShellCredential);

        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof CredentialUnavailableException && t.getMessage()
                .startsWith("EnvironmentCredential authentication unavailable. "))
            .verify();
    }
}
