package com.azure.identity;

import com.azure.core.credentials.AccessToken;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.ProxyOptions.Type;
import org.junit.Assert;
import org.junit.Ignore;

import java.net.InetSocketAddress;

public class IdentityClientIntegrationTests {
    private static final String AZURE_TENANT_ID = "AZURE_TENANT_ID";
    private static final String AZURE_CLIENT_ID = "AZURE_CLIENT_ID";
    private static final String AZURE_CLI_CLIENT_ID = "AZURE_CLI_CLIENT_ID";
    private static final String AZURE_CLIENT_SECRET = "AZURE_CLIENT_SECRET";
    private static final String AZURE_CLIENT_CERTIFICATE = "AZURE_CLIENT_CERTIFICATE";
    private static final String[] scopes = new String[] { "https://management.azure.com/.default" };

    @Ignore("Integration test")
    public void clientSecretCanGetToken() {
        IdentityClient client = new IdentityClient(new IdentityClientOptions().proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        AccessToken token = client.authenticateWithClientSecret(System.getenv(AZURE_TENANT_ID), System.getenv(AZURE_CLIENT_ID), System.getenv(AZURE_CLIENT_SECRET), scopes).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithCurrentlyLoggedInAccount(new String[] { "https://vault.azure.net/.default" }).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration test")
    public void deviceCodeCanGetToken() {
        IdentityClient client = new IdentityClient(new IdentityClientOptions().proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        AccessToken token = client.authenticateWithDeviceCode(System.getenv(AZURE_CLI_CLIENT_ID), scopes, deviceCode -> {
            System.out.println(deviceCode.message());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithCurrentlyLoggedInAccount(new String[] { "https://vault.azure.net/.default" }).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
    }

    @Ignore("Integration test")
    public void browserCanGetToken() {
        IdentityClient client = new IdentityClient(new IdentityClientOptions().proxyOptions(new ProxyOptions(Type.HTTP, new InetSocketAddress("localhost", 8888))));
        AccessToken token = client.authenticateWithBrowserInteraction(System.getenv(AZURE_CLI_CLIENT_ID), scopes, 8765).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
        token = client.authenticateWithCurrentlyLoggedInAccount(new String[] { "https://vault.azure.net/.default" }).block();
        Assert.assertNotNull(token);
        Assert.assertNotNull(token.token());
        Assert.assertNotNull(token.expiresOn());
        Assert.assertFalse(token.isExpired());
    }
}
