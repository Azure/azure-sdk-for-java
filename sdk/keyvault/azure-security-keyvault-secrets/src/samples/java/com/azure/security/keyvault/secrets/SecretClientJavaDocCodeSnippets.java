// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.time.OffsetDateTime;

/**
 * This class contains code samples for generating javadocs through doclets for {@link SecretClient}
 */
public final class SecretClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Implementation for sync SecretClient
     * @return sync SecretClient
     */
    private SecretClient getSecretClient() {
        // BEGIN: com.azure.security.keyvault.secretclient.sync.construct
        SecretClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .vaultUrl("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.security.keyvault.secretclient.sync.construct
        return secretClient;
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecret(String, String)}
     */
    public void getSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#secretProperties
        for (SecretProperties secret : secretClient.listPropertiesOfSecrets()) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Secret is returned with name %s and value %s%n", secretWithValue.getName(),
                secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.getSecret#secretProperties

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultSecret secretWithVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s%n",
            secretWithVersion.getName(), secretWithVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecret#string-string

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string
        KeyVaultSecret secretWithoutVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s%n",
            secretWithoutVersion.getName(), secretWithoutVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecretWithResponse(String, String, Context)}
     */
    public void getSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();

        // BEGIN: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        KeyVaultSecret secretWithVersion = secretClient.getSecretWithResponse("secretName", secretVersion,
            new Context(key2, value2)).getValue();
        System.out.printf("Secret is returned with name %s and value %s%n",
            secretWithVersion.getName(), secretWithVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecret(KeyVaultSecret)}
     */
    public void setSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#secret
        KeyVaultSecret newSecret = new KeyVaultSecret("secretName", "secretValue")
            .setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        KeyVaultSecret returnedSecret = secretClient.setSecret(newSecret);
        System.out.printf("Secret is created with name %s and value %s%n", returnedSecret.getName(),
            returnedSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecret#secret

        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#string-string
        KeyVaultSecret secret = secretClient.setSecret("secretName", "secretValue");
        System.out.printf("Secret is created with name %s and value %s%n", secret.getName(), secret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecret#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecretWithResponse(KeyVaultSecret, Context)}
     */
    public void setSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
        KeyVaultSecret newSecret = new KeyVaultSecret("secretName", "secretValue")
                 .setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        KeyVaultSecret secret = secretClient.setSecretWithResponse(newSecret, new Context(key1, value1)).getValue();
        System.out.printf("Secret is created with name %s and value %s%n", secret.getName(), secret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretProperties(SecretProperties)}
     */
    public void updateSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties
        SecretProperties secretProperties = secretClient.getSecret("secretName").getProperties();
        secretProperties.setExpiresOn(OffsetDateTime.now().plusDays(60));
        SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secretProperties);
        KeyVaultSecret updatedSecret = secretClient.getSecret(updatedSecretProperties.getName());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s%n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpiresOn());
        // END: com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretPropertiesWithResponse(SecretProperties, Context)}
     */
    public void updateSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context
        SecretProperties secretProperties = secretClient.getSecret("secretName").getProperties();
        secretProperties.setExpiresOn(OffsetDateTime.now().plusDays(60));
        SecretProperties updatedSecretBase = secretClient.updateSecretPropertiesWithResponse(secretProperties,
            new Context(key2, value2)).getValue();
        KeyVaultSecret updatedSecret = secretClient.getSecret(updatedSecretBase.getName());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s%n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpiresOn());
        // END: com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#beginDeleteSecret(String)}.
     */
    public void deleteSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.deleteSecret#String
        SyncPoller<DeletedSecret, Void> deleteSecretPoller = secretClient.beginDeleteSecret("secretName");

        // Deleted Secret is accessible as soon as polling begins.
        PollResponse<DeletedSecret> deleteSecretPollResponse = deleteSecretPoller.poll();

        // Deletion date only works for a SoftDelete-enabled Key Vault.
        System.out.println("Deleted Date  %s" + deleteSecretPollResponse.getValue()
            .getDeletedOn().toString());
        System.out.printf("Deleted Secret's Recovery Id %s", deleteSecretPollResponse.getValue()
            .getRecoveryId());

        // Secret is being deleted on server.
        deleteSecretPoller.waitForCompletion();
        // END: com.azure.security.keyvault.secretclient.deleteSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#beginDeleteSecret(String)}
     */
    public void getDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getDeletedSecret#string
        DeletedSecret deletedSecret = secretClient.getDeletedSecret("secretName");
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.getRecoveryId());
        // END: com.azure.security.keyvault.secretclient.getDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getDeletedSecretWithResponse(String, Context)}
     */
    public void getDeletedSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context
        DeletedSecret deletedSecret = secretClient.getDeletedSecretWithResponse("secretName",
            new Context(key2, value2)).getValue();
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.getRecoveryId());
        // END: com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#purgeDeletedSecret(String)}
     */
    public void purgeDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();

        // BEGIN: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string
        secretClient.purgeDeletedSecret("secretName");
        // END: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#purgeDeletedSecretWithResponse(String, Context)}
     */
    public void purgeDeletedSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.purgeDeletedSecretWithResponse#string-Context
        Response<Void> purgeResponse = secretClient.purgeDeletedSecretWithResponse("secretName",
            new Context(key1, value1));
        System.out.printf("Purge Status Code: %d", purgeResponse.getStatusCode());
        // END: com.azure.security.keyvault.secretclient.purgeDeletedSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#beginRecoverDeletedSecret(String)}.
     */
    public void recoverDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.recoverDeletedSecret#String
        SyncPoller<KeyVaultSecret, Void> recoverSecretPoller =
            secretClient.beginRecoverDeletedSecret("deletedSecretName");

        // Deleted Secret can be accessed as soon as polling is in progress.
        PollResponse<KeyVaultSecret> recoveredSecretPollResponse = recoverSecretPoller.poll();
        System.out.println("Recovered Key Name %s" + recoveredSecretPollResponse.getValue().getName());
        System.out.printf("Recovered Key's Id %s", recoveredSecretPollResponse.getValue().getId());

        // Key is being recovered on server.
        recoverSecretPoller.waitForCompletion();
        // END: com.azure.security.keyvault.secretclient.recoverDeletedSecret#String
    }

    /**
     * Method to insert code snippets for {@link SecretClient#backupSecret(String)}
     */
    public void backupSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.backupSecret#string
        byte[] secretBackup = secretClient.backupSecret("secretName");
        System.out.printf("Secret's Backup Byte array's length %s", secretBackup.length);
        // END: com.azure.security.keyvault.secretclient.backupSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#backupSecretWithResponse(String, Context)}
     */
    public void backupSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.backupSecretWithResponse#string-Context
        byte[] secretBackup = secretClient.backupSecretWithResponse("secretName",
            new Context(key1, value1)).getValue();
        System.out.printf("Secret's Backup Byte array's length %s", secretBackup.length);
        // END: com.azure.security.keyvault.secretclient.backupSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#restoreSecretBackup(byte[])}
     */
    public void restoreSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.restoreSecret#byte
        // Pass the secret backup byte array of the secret to be restored.
        byte[] secretBackupByteArray = {};
        KeyVaultSecret restoredSecret = secretClient.restoreSecretBackup(secretBackupByteArray);
        System.out
            .printf("Restored Secret with name %s and value %s", restoredSecret.getName(), restoredSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.restoreSecret#byte
    }

    /**
     * Method to insert code snippets for {@link SecretClient#restoreSecretBackupWithResponse(byte[], Context)}
     */
    public void restoreSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context
        // Pass the secret backup byte array of the secret to be restored.
        byte[] secretBackupByteArray = {};
        KeyVaultSecret restoredSecret = secretClient.restoreSecretBackupWithResponse(secretBackupByteArray,
            new Context(key2, value2)).getValue();
        System.out
            .printf("Restored Secret with name %s and value %s", restoredSecret.getName(), restoredSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listPropertiesOfSecrets()}
     */
    public void listSecretsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets
        for (SecretProperties secret : secretClient.listPropertiesOfSecrets()) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets

        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets#Context
        for (SecretProperties secret : secretClient.listPropertiesOfSecrets(new Context(key1, value2))) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets#Context

        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets.iterableByPage
        secretClient.listPropertiesOfSecrets().iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                KeyVaultSecret secretWithValue = secretClient.getSecret(value.getName(), value.getVersion());
                System.out.printf("Received secret with name %s and value %s",
                    secretWithValue.getName(), secretWithValue.getValue());
            });
        });
        // END: com.azure.security.keyvault.secretclient.listSecrets.iterableByPage
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listDeletedSecrets()}
     */
    public void listDeletedSecretsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listDeletedSecrets
        for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets()) {
            System.out.printf("Deleted secret's recovery Id %s", deletedSecret.getRecoveryId());
        }
        // END: com.azure.security.keyvault.secretclient.listDeletedSecrets

        // BEGIN: com.azure.security.keyvault.secretclient.listDeletedSecrets#Context
        for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets(new Context(key1, value2))) {
            System.out.printf("Deleted secret's recovery Id %s", deletedSecret.getRecoveryId());
        }
        // END: com.azure.security.keyvault.secretclient.listDeletedSecrets#Context

        // BEGIN: com.azure.security.keyvault.secretclient.listDeletedSecrets.iterableByPage
        secretClient.listDeletedSecrets().iterableByPage().forEach(resp -> {
            System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                System.out.printf("Deleted secret's recovery Id %s", value.getRecoveryId());
            });
        });
        // END: com.azure.security.keyvault.secretclient.listDeletedSecrets.iterableByPage
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listPropertiesOfSecretVersions(String)}
     */
    public void listSecretVersionsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string
        for (SecretProperties secret : secretClient.listPropertiesOfSecretVersions("secretName")) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string

        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context
        for (SecretProperties secret : secretClient
            .listPropertiesOfSecretVersions("secretName", new Context(key1, value2))) {
            KeyVaultSecret secretWithValue = secretClient.getSecret(secret.getName(), secret.getVersion());
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context

        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage
        secretClient.listPropertiesOfSecretVersions("secretName", new Context(key1, value2))
                    .iterableByPage().forEach(resp -> {
                        System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                            resp.getRequest().getUrl(), resp.getStatusCode());
                        resp.getItems().forEach(value -> {
                            KeyVaultSecret secretWithValue = secretClient.getSecret(value.getName(), value.getVersion());
                            System.out.printf("Received secret's version with name %s and value %s",
                                secretWithValue.getName(), secretWithValue.getValue());
                        });
                    });
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage
    }
}
