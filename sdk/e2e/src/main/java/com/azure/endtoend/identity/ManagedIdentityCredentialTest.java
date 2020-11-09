// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.endtoend.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Managed Identity Credential test.
 */
public class ManagedIdentityCredentialTest {
    private static final String VAULT_SECRET_NAME = "secret";
    private static final String AZURE_MANAGED_IDENTITY_TEST_MODE = "AZURE_MANAGED_IDENTITY_TEST_MODE";
    private static final String AZURE_VAULT_URL = "AZURE_VAULT_URL";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();
    private final ClientLogger logger = new ClientLogger(ManagedIdentityCredentialTest.class);

    /**
     * Runs the Web jobs identity tests
     * @throws IllegalStateException if AZURE_ARC_TEST_MODE is not set to "user" or "system"
     */
    void run() throws IllegalStateException {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(AZURE_MANAGED_IDENTITY_TEST_MODE))) {
            throw logger.logExceptionAsError(
                new IllegalStateException("Managed Identity Test mode is not set. Set environemnt "
                                                   + "variable AZURE_MANAGED_IDENTITY_TEST_MODE to user or system"));
        }

        String mode = CONFIGURATION.get(AZURE_MANAGED_IDENTITY_TEST_MODE).toLowerCase(Locale.ENGLISH);
        ManagedIdentityCredentialTest identityTest = new ManagedIdentityCredentialTest();
        switch (mode) {
            case "user":
                identityTest.testWithUserAssigned();
                identityTest.testWithUserAssignedAccessKeyVault();
                break;
            case "system":
                identityTest.testWithSystemAssigned();
                identityTest.testWithSystemAssignedAccessKeyVault();
                break;
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Invalid Test mode is configured AZURE_MANAGED_IDENTITY_TEST_MODE"
                                                  + ". Possible values are user or system."));
        }

    }

    private void testWithSystemAssigned() {

        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .build();

        AccessToken accessToken = managedIdentityCredential.getToken(new TokenRequestContext()
                                               .addScopes("https://management.azure.com/.default")).block();

        if (accessToken == null) {
            System.out.println("Error: Access token is null.");
            return;
        }

        System.out.printf("Received token with length %d and expiry at %s %n "
                              + "testWithSystemAssigned - Succeeded %n",
            accessToken.getToken().length(), accessToken.getExpiresAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private void testWithSystemAssignedAccessKeyVault() {
        assertConfigPresence(AZURE_VAULT_URL,
            "testWithUserAssignedKeyVault - Vault URL is not configured in the environment.");

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        SecretClient client = new SecretClientBuilder()
                                  .credential(credential)
                                  .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
                                  .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        System.out.printf("testWithSystemAssignedAccessKeyVault - "
                              + "Retrieved Secret with name %s and value %s %n",
            secret.getName(), secret.getValue());
        if (secret != null) {
            System.out.println("SUCCESS: Secret retrieved - testWithSystemAssignedAccessKeyVault succeeded");
        } else {
            System.out.println("ERROR: Could not retrieve secret - testWithSystemAssignedAccessKeyVault failed");
        }
    }

    private void testWithUserAssigned() {

        ManagedIdentityCredential managedIdentityCredential = new ManagedIdentityCredentialBuilder()
            .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
            .build();

        AccessToken accessToken = managedIdentityCredential.getToken(new TokenRequestContext()
                                                     .addScopes("https://management.azure.com/.default")).block();

        if (accessToken == null) {
            System.out.println("Error: Access token is null.");
            return;
        }

        System.out.printf("Received token with length %d and expiry at %s %n "
                              + "testWithUserAssigned - succeeded %n",
            accessToken.getToken().length(), accessToken.getExpiresAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private void testWithUserAssignedAccessKeyVault() {

        assertConfigPresence(Configuration.PROPERTY_AZURE_CLIENT_ID,
            "testWithUserAssignedKeyVault - Client is not configured in the environment.");
        assertConfigPresence(AZURE_VAULT_URL,
            "testWithUserAssignedKeyVault - Vault URL is not configured in the environment.");

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
                                                   .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                                                   .build();
        SecretClient client = new SecretClientBuilder()
                                  .credential(credential)
                                  .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
                                  .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        System.out.printf("testWithSystemAssignedAccessKeyVault - "
                              + "Retrieved Secret with name %s and value %s %n",
            secret.getName(), secret.getValue());

        if (secret != null) {
            System.out.println("SUCCESS: Secret retrieved - testWithUserAssignedAccessKeyVault succeeded");
        } else {
            System.out.println("ERROR: Could not retrieve secret - "
                                   + "testWithUserAssignedAccessKeyVault failed");
        }
    }

    private void assertConfigPresence(String identitfer, String errorMessage) {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(identitfer))) {
            throw logger.logExceptionAsError(new IllegalStateException(errorMessage));
        }
    }
}
