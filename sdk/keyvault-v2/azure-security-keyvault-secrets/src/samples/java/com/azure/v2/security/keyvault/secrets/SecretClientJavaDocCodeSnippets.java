// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.secrets;

import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.secrets.models.DeletedSecret;
import com.azure.v2.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.v2.security.keyvault.secrets.models.SecretProperties;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;

import java.time.OffsetDateTime;

/**
 * This class contains code samples for generating javadocs through doclets for {@link SecretClient}.
 */
public final class SecretClientJavaDocCodeSnippets {
    /**
     * Generates code sample for creating a {@link SecretClient}.
     *
     * @return An instance of {@link SecretClient}.
     */
    public SecretClient createSecretClient() {
        // BEGIN: com.azure.v2.security.keyvault.secrets.SecretClient.instantiation
        SecretClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("<your-key-vault-url>")
            .buildClient();
        // END: com.azure.v2.security.keyvault.secrets.SecretClient.instantiation

        return secretClient;
    }

     /**
     * Generates code sample for creating a {@link SecretClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link SecretClient}.
     */
    public SecretClient createSecretClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.secrets.SecretClient.instantiation.withHttpClient
        SecretClient secretClient = new SecretClientBuilder()
            .endpoint("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions()
                .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.secrets.SecretClient.instantiation.withHttpClient

        return secretClient;
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecret(String, String)}.
     */
    public void getSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getSecret
        for (SecretProperties secretProperties : secretClient.listPropertiesOfSecrets()) {
            KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

            System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
        }
        // END: com.azure.v2.security.keyvault.SecretClient.getSecret

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getSecret#String
        KeyVaultSecret secret = secretClient.getSecret("secretName");

        System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.getSecret#String

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getSecret#String-String
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultSecret keyVaultSecret = secretClient.getSecret("secretName", secretVersion);

        System.out.printf("Retrieved secret with name '%s' and value '%s'%n", keyVaultSecret.getName(),
            keyVaultSecret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.getSecret#String-String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecretWithResponse(String, String, RequestContext)}.
     */
    public void getSecretWithResponse() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#String-String-RequestContext
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<KeyVaultSecret> response =
            secretClient.getSecretWithResponse("secretName", secretVersion, requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        KeyVaultSecret keyVaultSecret = response.getValue();

        System.out.printf("The response contained the secret with name '%s' and value '%s'%n",
            keyVaultSecret.getName(), keyVaultSecret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.getSecretWithResponse#String-String-RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecret(KeyVaultSecret)}.
     */
    public void setSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.setSecret#String-String
        KeyVaultSecret secret = secretClient.setSecret("secretName", "secretValue");

        System.out.printf("Set secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.setSecret#String-String

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.setSecret#KeyVaultSecret
        KeyVaultSecret secretToSet = new KeyVaultSecret("secretName", "secretValue")
            .setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        KeyVaultSecret returnedSecret = secretClient.setSecret(secretToSet);

        System.out.printf("Set secret with name '%s' and value '%s'%n", returnedSecret.getName(),
            returnedSecret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.setSecret#KeyVaultSecret
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecretWithResponse(KeyVaultSecret, RequestContext)}.
     */
    public void setSecretWithResponse() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestContext
        KeyVaultSecret secretToSet = new KeyVaultSecret("secretName", "secretValue")
            .setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<KeyVaultSecret> response = secretClient.setSecretWithResponse(secretToSet, requestContext);
    
        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        KeyVaultSecret secret = response.getValue();

        System.out.printf("The response contained the set secret with name '%s' and value '%s'%n", secret.getName(),
            secret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.setSecretWithResponse#secret-RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretProperties(SecretProperties)}.
     */
    public void updateSecretProperties() {
        SecretClient secretClient = createSecretClient();
        // BEGIN: com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties
        SecretProperties secretProperties = secretClient.getSecret("secretName")
            .getProperties()
            .setExpiresOn(OffsetDateTime.now().plusDays(60));

        // Update secret with the new properties.
        SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secretProperties);

        // Retrieve updated secret.
        KeyVaultSecret updatedSecret = secretClient.getSecret(updatedSecretProperties.getName());

        System.out.printf("Updated secret with name '%s' and value '%s' to expire at: %s%n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpiresOn());
        // END: com.azure.v2.security.keyvault.SecretClient.updateSecretProperties#secretProperties
    }

    /**
     * Method to insert code snippets for
     * {@link SecretClient#updateSecretPropertiesWithResponse(SecretProperties, RequestContext)}.
     */
    public void updateSecretPropertiesWithResponse() {
        SecretClient secretClient = createSecretClient();
        // BEGIN: com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestContext
        SecretProperties secretProperties = secretClient.getSecret("secretName")
            .getProperties()
            .setExpiresOn(OffsetDateTime.now().plusDays(60));
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        // Update secret with the new properties.
        Response<SecretProperties> response =
            secretClient.updateSecretPropertiesWithResponse(secretProperties, requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        SecretProperties updatedSecretProperties = response.getValue();

        // Retrieve updated secret.
        KeyVaultSecret updatedSecret = secretClient.getSecret(updatedSecretProperties.getName());

        System.out.printf(
            "The response contained the updated secret with name '%s' and value '%s' set to expire at: %s%n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpiresOn());
        // END: com.azure.v2.security.keyvault.SecretClient.updateSecretPropertiesWithResponse#secretProperties-RequestContext
    }

    /**
     * Method to insert code snippets for {/@link SecretClient#beginDeleteSecret(String)}.
     */
    public void deleteSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.deleteSecret#String
        Poller<DeletedSecret, Void> deleteSecretPoller = secretClient.beginDeleteSecret("secretName");

        // Deleted Secret is accessible as soon as polling begins.
        PollResponse<DeletedSecret> deleteSecretPollResponse = deleteSecretPoller.poll();

        // Deletion date only works for a soft-delete enabled key vault.
        System.out.printf("Deleted secret's recovery id: '%s'. Deleted date: '%s'.",
            deleteSecretPollResponse.getValue().getRecoveryId(), deleteSecretPollResponse.getValue().getDeletedOn());

        // Secret is being deleted on server.
        deleteSecretPoller.waitForCompletion();
        // END: com.azure.v2.security.keyvault.SecretClient.deleteSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getDeletedSecret(String)}.
     */
    public void getDeletedSecret() {
        SecretClient secretClient = createSecretClient();
        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#String
        DeletedSecret deletedSecret = secretClient.getDeletedSecret("secretName");

        System.out.printf("Retrieved deleted secret with recovery id: %s%n", deletedSecret.getRecoveryId());
        // END: com.azure.v2.security.keyvault.SecretClient.getDeletedSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getDeletedSecretWithResponse(String, RequestContext)}.
     */
    public void getDeletedSecretWithResponse() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<DeletedSecret> response = secretClient.getDeletedSecretWithResponse("secretName", requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        DeletedSecret deletedSecret = response.getValue();

        System.out.printf("The response contained the deleted secret with name '%s' recovery id '%s'%n",
            deletedSecret.getName(), deletedSecret.getRecoveryId());
        // END: com.azure.v2.security.keyvault.SecretClient.getDeletedSecretWithResponse#String-RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#purgeDeletedSecret(String)}.
     */
    public void purgeDeletedSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String
        secretClient.purgeDeletedSecret("secretName");
        // END: com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#purgeDeletedSecretWithResponse(String, RequestContext)}.
     */
    public void purgeDeletedSecretWithResponse() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<Void> response = secretClient.purgeDeletedSecretWithResponse("secretName", requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());
        // END: com.azure.v2.security.keyvault.SecretClient.purgeDeletedSecretWithResponse#String-RequestContext
    }

    /**
     * Method to insert code snippets for {/@link SecretClient#beginRecoverDeletedSecret(String)}.
     */
    public void beginRecoverDeletedSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.beginRecoverDeletedSecret#String
        Poller<KeyVaultSecret, Void> recoverSecretPoller = secretClient.beginRecoverDeletedSecret("deletedSecretName");

        // A secret to be recovered can be accessed as soon as polling is in progress.
        PollResponse<KeyVaultSecret> recoveredSecretPollResponse = recoverSecretPoller.poll();

        System.out.printf("Recovered deleted secret with name '%s' and id '%s'%n",
            recoveredSecretPollResponse.getValue().getName(), recoveredSecretPollResponse.getValue().getId());

        // Wait for the secret to be recovered on the server.
        recoverSecretPoller.waitForCompletion();
        // END: com.azure.v2.security.keyvault.SecretClient.beginRecoverDeletedSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#backupSecret(String)}.
     */
    public void backupSecret() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.backupSecret#String
        byte[] secretBackup = secretClient.backupSecret("secretName");

        System.out.printf("The length of the resulting backup byte array is: %s%n", secretBackup.length);
        // END: com.azure.v2.security.keyvault.SecretClient.backupSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#backupSecretWithResponse(String, RequestContext)}.
     */
    public void backupSecretWithResponse() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<byte[]> response = secretClient.backupSecretWithResponse("secretName", requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        byte[] secretBackup = response.getValue();

        System.out.printf("The response contained a backup byte array with length: %s%n", secretBackup.length);
        // END: com.azure.v2.security.keyvault.SecretClient.backupSecretWithResponse#String-RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#restoreSecretBackup(byte[])}.
     */
    public void restoreSecretBackup() {
        SecretClient secretClient = createSecretClient();
        byte[] secretBackupByteArray = {};

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte
        KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup(secretBackupByteArray);

        System.out.printf("Restored secret with name '%s' and value '%s'%n", restoredSecret.getName(),
            restoredSecret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.restoreSecret#byte
    }

    /**
     * Method to insert code snippets for {@link SecretClient#restoreSecretBackupWithResponse(byte[], RequestContext)}.
     */
    public void restoreSecretBackupWithResponse() {
        SecretClient secretClient = createSecretClient();
        byte[] secretBackupByteArray = {};

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        Response<KeyVaultSecret> response =
            secretClient.restoreSecretBackupWithResponse(secretBackupByteArray, requestContext);

        System.out.printf("Received response with status code %d and headers: %s%n", response.getStatusCode(),
            response.getHeaders());

        KeyVaultSecret restoredSecret = response.getValue();

        System.out.printf("The response contained the restored secret with name '%s' and value '%s'%n",
            restoredSecret.getName(), restoredSecret.getValue());
        // END: com.azure.v2.security.keyvault.SecretClient.restoreSecretWithResponse#byte-RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listPropertiesOfSecrets()}.
     */
    public void listPropertiesOfSecrets() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets
        secretClient.listPropertiesOfSecrets().forEach(secretProperties -> {
            KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

            System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage
        secretClient.listPropertiesOfSecrets()
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(secretProperties -> {
                    KeyVaultSecret secret =
                        secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

                    System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
                });
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listPropertiesOfSecrets(requestContext).forEach(secretProperties -> {
            KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

            System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(), secret.getValue());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets#RequestContext

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listPropertiesOfSecrets(reqContext)
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(secretProperties -> {
                    KeyVaultSecret secret =
                        secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

                    System.out.printf("Retrieved secret with name '%s' and value '%s'%n", secret.getName(),
                        secret.getValue());
                });
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecrets.iterableByPage#RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listDeletedSecrets()}
     */
    public void listDeletedSecrets() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets
        secretClient.listDeletedSecrets().forEach(deletedSecret -> {
            System.out.printf("Retrieved deleted secret with recovery id: %s", deletedSecret.getRecoveryId());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage
        secretClient.listDeletedSecrets()
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(deletedSecret ->
                    System.out.printf("Retrieved deleted secret with recovery id: %s%n",
                        deletedSecret.getRecoveryId()));
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listDeletedSecrets(requestContext).forEach(deletedSecret -> {
            System.out.printf("Retrieved deleted secret with recovery id: %s%n", deletedSecret.getRecoveryId());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets#RequestContext

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listDeletedSecrets(reqContext)
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(deletedSecret ->
                    System.out.printf("Retrieved deleted secret with recovery id: %s%n",
                        deletedSecret.getRecoveryId()));
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listDeletedSecrets.iterableByPage#RequestContext
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listPropertiesOfSecretVersions(String)}.
     */
    public void listPropertiesOfSecretVersions() {
        SecretClient secretClient = createSecretClient();

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String
        secretClient.listPropertiesOfSecretVersions("secretName").forEach(secretProperties -> {
            KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

            System.out.printf("Retrieved secret version with name '%s' and value '%s'%n", secret.getName(),
                secret.getValue());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String
        secretClient.listPropertiesOfSecretVersions("secretName")
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(secretProperties -> {
                    KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(),
                        secretProperties.getVersion());

                    System.out.printf("Retrieved secret version with name '%s' and value '%s'%n", secret.getName(),
                        secret.getValue());
                });
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listPropertiesOfSecretVersions("secretName", requestContext).forEach(secretProperties -> {

            KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(), secretProperties.getVersion());

            System.out.printf("Retrieved secret version with name '%s' and value '%s'%n", secret.getName(),
                secret.getValue());
        });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions#String-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String-RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("someKey", "someValue")
            .build();

        secretClient.listPropertiesOfSecretVersions("secretName", reqContext)
            .iterableByPage()
            .forEach(pagedResponse -> {
                System.out.printf("Received response with status code %d and headers: %s%n",
                    pagedResponse.getStatusCode(), pagedResponse.getHeaders());

                pagedResponse.getValue().forEach(secretProperties -> {
                    KeyVaultSecret secret = secretClient.getSecret(secretProperties.getName(),
                        secretProperties.getVersion());

                    System.out.printf("Retrieved secret version with name '%s' and value '%s'%n", secret.getName(),
                        secret.getValue());
                });
            });
        // END: com.azure.v2.security.keyvault.SecretClient.listPropertiesOfSecretVersions.iterableByPage#String-RequestContext
    }
}
