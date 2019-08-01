// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.AccessToken;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.configuration.Configuration;
import com.azure.core.util.configuration.ConfigurationManager;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class DefaultAzureCredentialTest {

    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testUseEnvironmentCredential() throws Exception {
        Configuration configuration = ConfigurationManager.getConfiguration();

        try {
            // setup
            String secret = "secret";
            String token1 = "token1";
            String[] scopes1 = new String[]{"https://management.azure.com"};
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("AZURE_CLIENT_ID", clientId);
            configuration.put("AZURE_CLIENT_SECRET", secret);
            configuration.put("AZURE_TENANT_ID", tenantId);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateWithClientSecret(secret, scopes1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            AccessToken token = credential.getToken(scopes1).block();
            Assert.assertEquals(token1, token.token());
            Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
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
        String[] scopes = new String[] { "https://management.azure.com" };
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(scopes)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
        AccessToken token = credential.getToken(scopes).block();
        Assert.assertEquals(token1, token.token());
        Assert.assertEquals(expiresOn.getSecond(), token.expiresOn().getSecond());
    }

    @Test
    public void testNoCredentialWorks() throws Exception {
        // setup
        String[] scopes = new String[] { "https://management.azure.com" };

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(scopes)).thenReturn(Mono.error(new RuntimeException("Hidden error message")));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        try {
            DefaultAzureCredential credential = new DefaultAzureCredentialBuilder().build();
            credential.getToken(scopes).block();
            fail();
        } catch (ClientAuthenticationException e) {
            Assert.assertTrue(e.getMessage().contains("No credential can provide a token"));
        }
    }
}
