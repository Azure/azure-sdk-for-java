// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.util.configuration.BaseConfigurations;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
public class ManagedIdentityCredentialTest {

    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testAppServiceMSICredentialConfigurations() {
        ConfigurationManager.getConfiguration()
            .put(BaseConfigurations.MSI_ENDPOINT, "http://foo")
            .put(BaseConfigurations.MSI_SECRET, "bar");
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();
        Assert.assertEquals("http://foo", credential.msiEndpoint());
        Assert.assertEquals("bar", credential.msiSecret());
    }

    @Test
    public void testVirtualMachineMSICredentialConfigurations() {
        ConfigurationManager.getConfiguration().remove(BaseConfigurations.MSI_ENDPOINT);
        ConfigurationManager.getConfiguration().remove(BaseConfigurations.MSI_SECRET);
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId("foo").build();
        Assert.assertEquals("foo", credential.clientId());
    }

    @Test
    public void testMSIEndpoint() throws Exception {
        Configuration configuration = ConfigurationManager.getConfiguration();

        try {
            // setup
            String endpoint = "http://localhost";
            String secret = "secret";
            String token1 = "token1";
            String[] scopes1 = new String[]{"https://management.azure.com"};
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("MSI_ENDPOINT", endpoint);
            configuration.put("MSI_SECRET", secret);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateToManagedIdentityEndpoint(endpoint, secret, scopes1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(clientId).build();
            AccessToken token = credential.getToken(scopes1).block();
            Assert.assertEquals(token1, token.token());
            Assert.assertEquals(expiresOn, token.expiresOn());
        } finally {
            // clean up
            configuration.remove("MSI_ENDPOINT");
            configuration.remove("MSI_SECRET");
        }
    }

    @Test
    public void testIMDS() throws Exception {
        // setup
        String token1 = "token1";
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(scopes)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(clientId).build();
        AccessToken token = credential.getToken(scopes).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn, token.expiresOn());
    }
}
