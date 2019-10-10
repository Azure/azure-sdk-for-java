// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credentials.TokenRequest;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import org.junit.Assert;
import org.junit.Ignore;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.net.URI;

public class IdentityClientIntegrationTests {
    private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String AZURE_CLI_CLIENT_ID = "AZURE_CLI_CLIENT_ID";
    private static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    private static final String AZURE_CLIENT_CERTIFICATE = "AZURE_CLIENT_CERTIFICATE";
    private final TokenRequest request = new TokenRequest().addScopes("https://management.azure.com/.default");

    @Ignore("Integration test")
    public void clientSecretCanGetToken() {
        IdentityClient client = new IdentityClient(System.getenv(AZURE_TENANT_ID), System.getenv(AZURE_CLIENT_ID), new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        StepVerifier.create(client.authenticateWithClientSecret(System.getenv(AZURE_CLIENT_SECRET), request))
            .expectNextMatches(token -> token.getToken() != null
                && token.getExpiresOn() != null
                && !token.isExpired())
            .verifyComplete();
        StepVerifier.create(client.authenticateWithClientSecret(System.getenv(AZURE_CLIENT_SECRET), new TokenRequest().addScopes("https://vault.azure.net/.default")))
            .expectNextMatches(token -> token.getToken() != null
                && token.getExpiresOn() != null
                && !token.isExpired())
            .verifyComplete();
    }

    @Ignore("Integration tests")
    public void deviceCodeCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
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
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequest().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration tests")
    public void browserCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        MsalToken token = client.authenticateWithBrowserInteraction(request, 8765).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequest().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration tests")
    public void usernamePasswordCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        MsalToken token = client.authenticateWithUsernamePassword(request, System.getenv("username"), System.getenv("password")).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequest().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration tests")
    public void authCodeCanGetToken() throws Exception {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), new IdentityClientOptions());
        MsalToken token = client.authenticateWithAuthorizationCode(request, System.getenv("AZURE_AUTH_CODE"), new URI("http://localhost:8000")).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithUserRefreshToken(new TokenRequest().addScopes("https://vault.azure.net/.default"), token).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.getToken());
        Assert.assertNotNull(token.getExpiresOn());
        Assert.assertFalse(token.isExpired());
    }
}
