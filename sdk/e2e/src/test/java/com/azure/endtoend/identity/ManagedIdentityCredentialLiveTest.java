// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.Assert;
import org.junit.Test;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class ManagedIdentityCredentialLiveTest {
    private static final String AZURE_VAULT_URL = "AZURE_VAULT_URL";
    private static final String VAULT_SECRET_NAME = "secret";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();
    private static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";
    private static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";

    @Test
    public void testMSIEndpointWithSystemAssigned() throws Exception {
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT));
        org.junit.Assume.assumeTrue(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID) == null);
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        IdentityClient client = new IdentityClientBuilder().build();
        StepVerifier.create(client.authenticateToManagedIdentityEndpoint(
            CONFIGURATION.get(PROPERTY_IDENTITY_ENDPOINT),
            CONFIGURATION.get(PROPERTY_IDENTITY_HEADER),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET),
            new TokenRequestContext().addScopes("https://management.azure.com/.default")))
            .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
            .verifyComplete();
    }

    @Test
    public void testMSIEndpointWithSystemAssignedAccessKeyVault() throws Exception {
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT));
        org.junit.Assume.assumeTrue(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID) == null);
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        SecretClient client = new SecretClientBuilder()
            .credential(credential)
            .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
            .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        Assert.assertNotNull(secret);
        Assert.assertEquals(VAULT_SECRET_NAME, secret.getName());
        Assert.assertNotNull(secret.getValue());
    }

    @Test
    public void testMSIEndpointWithUserAssigned() throws Exception {
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        IdentityClient client = new IdentityClientBuilder()
            .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
            .build();
        StepVerifier.create(client.authenticateToManagedIdentityEndpoint(
            CONFIGURATION.get(PROPERTY_IDENTITY_ENDPOINT),
            CONFIGURATION.get(PROPERTY_IDENTITY_HEADER),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET),
            new TokenRequestContext().addScopes("https://management.azure.com/.default")))
            .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
            .verifyComplete();
    }

    @Test
    public void testMSIEndpointWithUserAssignedAccessKeyVault() throws Exception {
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
            .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
            .build();

        SecretClient client = new SecretClientBuilder()
            .credential(credential)
            .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
            .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        Assert.assertNotNull(secret);
        Assert.assertEquals(VAULT_SECRET_NAME, secret.getName());
        Assert.assertNotNull(secret.getValue());
    }

    @Test
    public void testIMDSEndpointWithSystemAssigned() throws Exception {
        org.junit.Assume.assumeTrue(checkIMDSAvailable());
        org.junit.Assume.assumeTrue(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID) == null);
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        IdentityClient client = new IdentityClientBuilder().build();
        StepVerifier.create(client.authenticateToIMDSEndpoint(
            new TokenRequestContext().addScopes("https://management.azure.com/.default")))
            .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
            .verifyComplete();
    }

    @Test
    public void testIMDSEndpointWithSystemAssignedAccessKeyVault() throws Exception {
        org.junit.Assume.assumeTrue(checkIMDSAvailable());
        org.junit.Assume.assumeTrue(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID) == null);
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        SecretClient client = new SecretClientBuilder()
            .credential(credential)
            .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
            .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        Assert.assertNotNull(secret);
        Assert.assertEquals(VAULT_SECRET_NAME, secret.getName());
        Assert.assertNotNull(secret.getValue());
    }

    @Test
    public void testIMDSEndpointWithUserAssigned() throws Exception {
        org.junit.Assume.assumeTrue(checkIMDSAvailable());
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        IdentityClient client = new IdentityClientBuilder()
            .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
            .build();
        StepVerifier.create(client.authenticateToIMDSEndpoint(
            new TokenRequestContext().addScopes("https://management.azure.com/.default")))
            .expectNextMatches(accessToken -> accessToken != null && accessToken.getToken() != null)
            .verifyComplete();
    }

    @Test
    public void testIMDSEndpointWithUserAssignedAccessKeyVault() throws Exception {
        org.junit.Assume.assumeTrue(checkIMDSAvailable());
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID));
        org.junit.Assume.assumeNotNull(CONFIGURATION.get(AZURE_VAULT_URL));

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
            .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
            .build();

        SecretClient client = new SecretClientBuilder()
            .credential(credential)
            .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
            .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        Assert.assertNotNull(secret);
        Assert.assertEquals(VAULT_SECRET_NAME, secret.getName());
        Assert.assertNotNull(secret.getValue());
    }

    private boolean checkIMDSAvailable() {
        StringBuilder payload = new StringBuilder();

        try {
            payload.append("api-version=");
            payload.append(URLEncoder.encode("2018-02-01", "UTF-8"));
        } catch (IOException exception) {
            return false;
        }

        HttpURLConnection connection = null;

        try {
            URL url = new URL(String.format("http://169.254.169.254/metadata/identity/oauth2/token?%s",
                payload.toString()));

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(500);
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
