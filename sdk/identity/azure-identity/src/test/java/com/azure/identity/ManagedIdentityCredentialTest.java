// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class ManagedIdentityCredentialTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId("foo").build();
        Assert.assertEquals("foo", credential.getClientId());
    }

    @Test
    public void testMSIEndpoint() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            // setup
            String endpoint = "http://localhost";
            String secret = "secret";
            String token1 = "token1";
            TokenRequestContext request1 = new TokenRequestContext().addScopes("https://management.azure.com");
            OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("MSI_ENDPOINT", endpoint); // This must stay to signal we are in an app service context
            configuration.put("MSI_SECRET", secret);
            configuration.put("IDENTITY_ENDPOINT", endpoint);
            configuration.put("IDENTITY_HEADER", secret);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateToManagedIdentityEndpoint(endpoint, secret, endpoint, secret, request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresAt));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresAt.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
        } finally {
            // clean up
            configuration.remove("MSI_ENDPOINT");
            configuration.remove("MSI_SECRET");
            configuration.remove("IDENTITY_ENDPOINT");
            configuration.remove("IDENTITY_HEADER");
        }
    }

    @Test
    public void testIMDS() throws Exception {
        // setup
        String token1 = "token1";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).build();
        StepVerifier.create(credential.getToken(request))
                .expectNextMatches(token -> token1.equals(token.getToken())
                        && expiresOn.getSecond() == token.getExpiresAt().getSecond())
                .verifyComplete();
    }

    @Test
    public void testArcUserAssigned() throws Exception {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        // setup
        String token1 = "token1";
        String endpoint = "http://localhost";
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com");
        configuration.put("IDENTITY_ENDPOINT", endpoint);
        configuration.put("IMDS_ENDPOINT", endpoint);


        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
            .configuration(configuration).clientId(CLIENT_ID).build();
        StepVerifier.create(credential.getToken(request))
            .expectErrorMatches(t -> t instanceof ClientAuthenticationException)
            .verify();
    }

    @Test (expected = IllegalStateException.class)
    public void testInvalidIdCombination()  {
        // setup
        String resourceId = "/subscriptions/" + UUID.randomUUID() + "/resourcegroups/aresourcegroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/ident";

        // test
        new ManagedIdentityCredentialBuilder().clientId(CLIENT_ID).resourceId(resourceId).build();
    }

    @Test
    public void testArcIdentityCredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("IDENTITY_ENDPOINT", "http://localhost");
        configuration.put("IMDS_ENDPOINT", "http://localhost");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(ArcIdentityCredential.class));
    }

    @Test
    public void testServiceFabricMsiCredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("IDENTITY_ENDPOINT", "http://localhost");
        configuration.put("IDENTITY_SERVER_THUMBPRINT", "thumbprint");
        configuration.put("IDENTITY_HEADER", "header");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(ServiceFabricMsiCredential.class));
    }

    @Test
    public void testAppServiceMsi2019CredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("IDENTITY_ENDPOINT", "http://localhost");
        configuration.put("IDENTITY_HEADER", "header");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testAppServiceMsi2017CredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("MSI_ENDPOINT", "http://localhost");
        configuration.put("MSI_SECRET", "secret");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testCloudShellCredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("MSI_ENDPOINT", "http://localhost");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AppServiceMsiCredential.class));
    }

    @Test
    public void testAksExchangeTokenCredentialCreated() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        configuration.put("AZURE_TENANT_ID", "tenantId");
        configuration.put("AZURE_CLIENT_ID", "clientId");
        configuration.put("AZURE_FEDERATED_TOKEN_FILE", "tokenFile");

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential, instanceOf(AksExchangeTokenCredential.class));
    }

    @Test
    public void testIMDSPodIdentityV1Credential() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        ManagedIdentityCredential cred = new ManagedIdentityCredentialBuilder().configuration(configuration).build();
        assertThat("Received class " + cred.managedIdentityServiceCredential.getClass().toString(),
            cred.managedIdentityServiceCredential,  instanceOf(VirtualMachineMsiCredential.class));
    }
}

