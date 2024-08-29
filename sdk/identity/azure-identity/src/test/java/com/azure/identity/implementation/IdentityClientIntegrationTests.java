// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
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

    @Disabled("Integration tests")
    public void clientSecretCanGetToken() {
        IdentityClient client = new IdentityClient(System.getenv(AZURE_TENANT_ID), System.getenv(AZURE_CLIENT_ID), System.getenv(AZURE_CLIENT_SECRET), null, null, null,null, null, null, null, null, false, null, new IdentityClientOptions());
        StepVerifier.create(client.authenticateWithConfidentialClient(request))
            .expectNextMatches(token -> token.getToken() != null
                && token.getExpiresAt() != null
                && !token.isExpired())
            .verifyComplete();
        StepVerifier.create(client.authenticateWithConfidentialClient(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .expectNextMatches(token -> token.getToken() != null
                && token.getExpiresAt() != null
                && !token.isExpired())
            .verifyComplete();
    }

    @Disabled("Integration tests")
    public void deviceCodeCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, null,null, null, null, null, null, false, null, new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        MsalToken token = client.authenticateWithDeviceCode(request, deviceCode -> {
            System.out.println(deviceCode.getMessage());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
        token = client.authenticateWithPublicClientCache(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token.getAccount()).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
    }

    @Disabled("Integration tests")
    public void browserCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, null,null, null, null, null, null, false, null, new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        MsalToken token = client.authenticateWithBrowserInteraction(request, 8765, null, null).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
        token = client.authenticateWithPublicClientCache(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token.getAccount()).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
    }

    @Disabled("Integration tests")
    public void usernamePasswordCanGetToken() {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, null,null, null, null, null, null, false, null, new IdentityClientOptions().setProxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        MsalToken token = client.authenticateWithUsernamePassword(request, System.getenv("username"), System.getenv("password")).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
        token = client.authenticateWithPublicClientCache(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token.getAccount()).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
    }

    @Disabled("Integration tests")
    public void authCodeCanGetToken() throws Exception {
        IdentityClient client = new IdentityClient("common", System.getenv(AZURE_CLIENT_ID), null, null, null, null,null, null, null, null, null,  false, null, new IdentityClientOptions());
        MsalToken token = client.authenticateWithAuthorizationCode(request, System.getenv("AZURE_AUTH_CODE"), new URI("http://localhost:8000")).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
        token = client.authenticateWithPublicClientCache(new TokenRequestContext().addScopes("https://vault.azure.net/.default"), token.getAccount()).block();
        Assertions.assertNotNull(token);
        Assertions.assertNotNull(token.getToken());
        Assertions.assertNotNull(token.getExpiresAt());
        Assertions.assertFalse(token.isExpired());
    }
}
