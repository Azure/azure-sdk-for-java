// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.ProxyOptions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.URI;

public class IdentityClientIntegrationTests {
    private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String AZURE_CLI_CLIENT_ID = "AZURE_CLI_CLIENT_ID";
    private static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    private static final String AZURE_CLIENT_CERTIFICATE = "AZURE_CLIENT_CERTIFICATE";
    private final TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

    @Test
    public void clientSecretCanGetToken() {
        IdentityClient client = new IdentityClient(System.getenv(AZURE_TENANT_ID), System.getenv(AZURE_CLIENT_ID), System.getenv(AZURE_CLIENT_SECRET), null, null, new IdentityClientOptions().setProxyOptions(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("10.127.70.25", 8888))));
        StepVerifier.create(client.authenticateWithClientSecret(request))
                .expectNextMatches(token -> token.getToken() != null
                        && token.getExpiresAt() != null
                        && !token.isExpired())
                .verifyComplete();
        StepVerifier.create(client.authenticateWithClientSecret(request))
                .expectNextMatches(token -> token.getToken() != null
                        && token.getExpiresAt() != null
                        && !token.isExpired())
                .verifyComplete();
        StepVerifier.create(client.authenticateWithClientSecret(request))
                .expectNextMatches(token -> token.getToken() != null
                        && token.getExpiresAt() != null
                        && !token.isExpired())
                .verifyComplete();
        StepVerifier.create(client.authenticateWithClientSecret(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .expectNextMatches(token -> token.getToken() != null
                && token.getExpiresAt() != null
                && !token.isExpired())
            .verifyComplete();
    }

    @Ignore("Integration test")
    public void deviceCodeCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, new IdentityClientOptions());
        MsalToken token = client.authenticateWithDeviceCode(request, deviceCode -> {
            System.out.println(deviceCode.getMessage());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration test")
    public void browserCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), System.getenv(AZURE_CLIENT_SECRET), null, null, new IdentityClientOptions());
        MsalToken token = client.authenticateWithBrowserInteraction(request, 8765).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration test")
    public void usernamePasswordCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, new IdentityClientOptions());
        MsalToken token = client.authenticateWithUsernamePassword(request, System.getenv("username"), System.getenv("password")).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration tests")
    public void authCodeCanGetToken() throws Exception {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), System.getenv(AZURE_CLIENT_SECRET), null, null, new IdentityClientOptions());
        MsalToken token = client.authenticateWithAuthorizationCode(request, System.getenv("AZURE_AUTH_CODE"), new URI("http://localhost:8000")).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresAt());
        Assert.assertFalse(token.isExpired());
    }
}
