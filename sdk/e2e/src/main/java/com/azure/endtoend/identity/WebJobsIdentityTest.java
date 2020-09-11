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
import com.azure.identity.implementation.IdentityClient;
import com.azure.identity.implementation.IdentityClientBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Identity MSI and IMDS endpoints test in Azure Web Jobs.
 */
class WebJobsIdentityTest {
    private static final String VAULT_SECRET_NAME = "secret";
    private static final String AZURE_WEBJOBS_TEST_MODE = "AZURE_WEBJOBS_TEST_MODE";
    private static final String AZURE_VAULT_URL = "AZURE_VAULT_URL";
    private static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration().clone();
    private static final String PROPERTY_IDENTITY_ENDPOINT = "IDENTITY_ENDPOINT";
    private static final String PROPERTY_IDENTITY_HEADER = "IDENTITY_HEADER";
    private final ClientLogger logger = new ClientLogger(WebJobsIdentityTest.class);

    /**
     * Runs the Web jobs identity tests
     * @throws IllegalStateException if AZURE_WEBJOBS_TEST_MODE is not set to "user" or "system"
     */
    void run() throws IllegalStateException {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(AZURE_WEBJOBS_TEST_MODE))) {
            throw logger.logExceptionAsError(new IllegalStateException("Webjobs Test mode is not set. Set environemnt "
                                                + "variable AZURE_WEBJOBS_TEST_MODE to user or system"));
        }

        String mode = CONFIGURATION.get(AZURE_WEBJOBS_TEST_MODE).toLowerCase(Locale.ENGLISH);
        WebJobsIdentityTest identityTest = new WebJobsIdentityTest();
        switch (mode) {
            case "user":
                identityTest.testMSIEndpointWithUserAssigned();
                identityTest.testMSIEndpointWithUserAssignedAccessKeyVault();
                break;
            case "system":
                identityTest.testMSIEndpointWithSystemAssigned();
                identityTest.testMSIEndpointWithSystemAssignedAccessKeyVault();
                break;
            default:
                throw logger.logExceptionAsError(
                    new IllegalStateException("Invalid Test mode is configured AZURE_WEBJOBS_TEST_MODE. "
                                                    + "Possible values are user or system."));
        }

    }

    private void testMSIEndpointWithSystemAssigned() {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT))
                && CoreUtils.isNullOrEmpty(CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET)))  {
            throw logger.logExceptionAsError(
                new IllegalStateException("testMSIEndpointWithUserAssigned - MSIEndpoint and Identity Point not"
                                              + "configured in the environment. At least one should be configured"));
        }
        assertConfigPresence(Configuration.PROPERTY_MSI_SECRET,
            "testMSIEndpointWithSystemAssigned - MSISecret not configured in the environment.");
        IdentityClient client = new IdentityClientBuilder().build();
        AccessToken accessToken = client.authenticateToManagedIdentityEndpoint(
            CONFIGURATION.get(PROPERTY_IDENTITY_ENDPOINT),
            CONFIGURATION.get(PROPERTY_IDENTITY_HEADER),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET),
            new TokenRequestContext().addScopes("https://management.azure.com/.default"))
                                      .flatMap(token -> {
                                          if (token == null || token.getToken() == null) {
                                              return Mono.error(logger.logExceptionAsError(new IllegalStateException(
                                                      "Access Token not returned from System Assigned Identity")));
                                          } else {
                                              return Mono.just(token);
                                          }
                                      }).block();

        if (accessToken == null) {
            System.out.println("Error: Access token is null.");
            return;
        }

        System.out.printf("Received token with length %d and expiry at %s %n "
                              + "testMSIEndpointWithSystemAssigned - Succeeded %n",
            accessToken.getToken().length(), accessToken.getExpiresAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private void testMSIEndpointWithSystemAssignedAccessKeyVault() {
        assertConfigPresence(Configuration.PROPERTY_MSI_ENDPOINT,
            "testMSIEndpointWithSystemAssignedKeyVault - MSIEndpoint not configured in the environment.");
        assertConfigNotPresent(Configuration.PROPERTY_AZURE_CLIENT_ID,
            "testMSIEndpointWithSystemAssignedKeyVault - Client id"
                + " should not be configured in the environment.");
        assertConfigPresence(AZURE_VAULT_URL,
            "testMSIEndpointWithSystemAssigned - Vault URL is not configured in the environment.");

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder().build();

        SecretClient client = new SecretClientBuilder()
                                  .credential(credential)
                                  .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
                                  .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        System.out.printf("testMSIEndpointWithSystemAssignedAccessKeyVault - "
                              + "Retrieved Secret with name %s and value %s %n",
            secret.getName(), secret.getValue());
        assertExpectedValue(VAULT_SECRET_NAME, secret.getName(),
            "SUCCESS: Secret matched - testMSIEndpointWithSystemAssignedAccessKeyVault succeeded",
            "Error: Secret name didn't match expected name - "
                + "testMSIEndpointWithSystemAssignedAccessKeyVault failed");
    }

    private void testMSIEndpointWithUserAssigned() {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT))
            && CoreUtils.isNullOrEmpty(CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET)))  {
            throw logger.logExceptionAsError(
                new IllegalStateException("testMSIEndpointWithUserAssigned - MSIEndpoint and Identity Point not"
                    + "configured in the environment. Atleast one should be configuured"));
        }
        assertConfigPresence(Configuration.PROPERTY_AZURE_CLIENT_ID,
            "testMSIEndpointWithUserAssigned - Client is not configured in the environment.");
        assertConfigPresence(AZURE_VAULT_URL,
            "testMSIEndpointWithUserAssigned - Vault URL is not configured in the environment.");

        IdentityClient client = new IdentityClientBuilder()
                                    .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                                    .build();

        AccessToken accessToken = client.authenticateToManagedIdentityEndpoint(
            CONFIGURATION.get(PROPERTY_IDENTITY_ENDPOINT),
            CONFIGURATION.get(PROPERTY_IDENTITY_HEADER),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT),
            CONFIGURATION.get(Configuration.PROPERTY_MSI_SECRET),
            new TokenRequestContext().addScopes("https://management.azure.com/.default"))
                                      .flatMap(token -> {
                                          if (token == null || token.getToken() == null) {
                                              return Mono.error(logger.logExceptionAsError(new IllegalStateException(
                                                      "Access Token not returned from System Assigned Identity")));
                                          } else {
                                              return Mono.just(token);
                                          }
                                      }).block();

        if (accessToken == null) {
            System.out.println("Error: Access token is null.");
            return;
        }

        System.out.printf("Received token with length %d and expiry at %s %n "
                              + "testMSIEndpointWithUserAssigned - succeeded %n",
            accessToken.getToken().length(), accessToken.getExpiresAt().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private void testMSIEndpointWithUserAssignedAccessKeyVault() {

        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(Configuration.PROPERTY_MSI_ENDPOINT) )
                                        && CoreUtils.isNullOrEmpty(CONFIGURATION.get("IDENTITY_ENDPOINT"))) {

        }

        assertConfigPresence(Configuration.PROPERTY_MSI_ENDPOINT,
            "testMSIEndpointWithUserAssignedKeyVault - MSIEndpoint not configured in the environment.");
        assertConfigPresence(Configuration.PROPERTY_AZURE_CLIENT_ID,
            "testMSIEndpointWithUserAssignedKeyVault - Client is not configured in the environment.");
        assertConfigPresence(AZURE_VAULT_URL,
            "testMSIEndpointWithUserAssignedKeyVault - Vault URL is not configured in the environment.");

        ManagedIdentityCredential credential = new ManagedIdentityCredentialBuilder()
                                                   .clientId(CONFIGURATION.get(Configuration.PROPERTY_AZURE_CLIENT_ID))
                                                   .build();
        SecretClient client = new SecretClientBuilder()
                                  .credential(credential)
                                  .vaultUrl(CONFIGURATION.get(AZURE_VAULT_URL))
                                  .buildClient();

        KeyVaultSecret secret = client.getSecret(VAULT_SECRET_NAME);
        System.out.printf("testMSIEndpointWithSystemAssignedAccessKeyVault - "
                              + "Retrieved Secret with name %s and value %s %n",
            secret.getName(), secret.getValue());
        assertExpectedValue(VAULT_SECRET_NAME, secret.getName(),
            "SUCCESS: Secret matched - testMSIEndpointWithUserAssignedAccessKeyVault - succeeded",
            "Error: Secret name didn't match expected name - testMSIEndpointWithUserAssignedAccessKeyVault - failed");
    }

    private void assertExpectedValue(String expected, String actual, String success, String faiure) {
        if (expected.equals(actual)) {
            System.out.println(success);
            return;
        }
        System.out.println(faiure);
    }


    private void assertConfigPresence(String identitfer, String errorMessage) {
        if (CoreUtils.isNullOrEmpty(CONFIGURATION.get(identitfer))) {
            throw logger.logExceptionAsError(new IllegalStateException(errorMessage));
        }
    }

    private void assertConfigNotPresent(String identitfer, String errorMessage) {
        if (!CoreUtils.isNullOrEmpty(CONFIGURATION.get(identitfer))) {
            throw logger.logExceptionAsError(new IllegalStateException(errorMessage));
        }
    }
}

