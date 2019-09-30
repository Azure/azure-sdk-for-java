// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.credential;

import com.azure.core.credentials.TokenRequest;
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

import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(fullyQualifiedNames = "com.azure.identity.*")
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
public class ManagedIdentityCredentialTest {

    private final String tenantId = "contoso.com";
    private final String clientId = UUID.randomUUID().toString();

    @Test
    public void testAppServiceMSICredentialConfigurations() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            configuration
                .put(Configuration.PROPERTY_MSI_ENDPOINT, "http://foo")
                .put(Configuration.PROPERTY_MSI_SECRET, "bar");
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();
            Assert.assertEquals("http://foo", credential.getMsiEndpoint());
            Assert.assertEquals("bar", credential.getMsiSecret());
        } finally {
            configuration.remove(Configuration.PROPERTY_MSI_ENDPOINT);
            configuration.remove(Configuration.PROPERTY_MSI_SECRET);
        }
    }

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
            TokenRequest request1 = new TokenRequest().addScopes("https://management.azure.com");
            OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);
            configuration.put("MSI_ENDPOINT", endpoint);
            configuration.put("MSI_SECRET", secret);

            // mock
            IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
            when(identityClient.authenticateToManagedIdentityEndpoint(endpoint, secret, request1)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
            PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

            // test
            ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(clientId).build();
            StepVerifier.create(credential.getToken(request1))
                .expectNextMatches(token -> token1.equals(token.getToken())
                    && expiresOn.getSecond() == token.getExpiresOn().getSecond())
                .verifyComplete();
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
        TokenRequest request = new TokenRequest().addScopes("https://management.azure.com");
        OffsetDateTime expiresOn = OffsetDateTime.now(ZoneOffset.UTC).plusHours(1);

        // mock
        IdentityClient identityClient = PowerMockito.mock(IdentityClient.class);
        when(identityClient.authenticateToIMDSEndpoint(request)).thenReturn(TestUtils.getMockAccessToken(token1, expiresOn));
        PowerMockito.whenNew(IdentityClient.class).withAnyArguments().thenReturn(identityClient);

        // test
        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().clientId(clientId).build();
        StepVerifier.create(credential.getToken(request))
            .expectNextMatches(token -> token1.equals(token.getToken())
                && expiresOn.getSecond() == token.getExpiresOn().getSecond())
            .verifyComplete();
    }
}
