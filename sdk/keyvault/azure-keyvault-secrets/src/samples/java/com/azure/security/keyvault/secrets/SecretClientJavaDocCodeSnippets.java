// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
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
     * Method to insert code snippets for {@link SecretClient#getSecret(SecretProperties)}
     */
    public void getSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#secretProperties
        for (SecretProperties secret : secretClient.listSecrets()) {
            Secret secretWithValue = secretClient.getSecret(secret);
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.getName(),
                secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.getSecret#secretProperties

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        Secret secretWithVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s %n",
            secretWithVersion.getName(), secretWithVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecret#string-string

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string
        Secret secretWithoutVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s %n",
            secretWithoutVersion.getName(), secretWithoutVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecretWithResponse(String, String, Context)}
     */
    public void getSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getSecretWithResponse#secretProperties
        for (SecretProperties secret : secretClient.listSecrets()) {
            Secret secretWithValue = secretClient.getSecretWithResponse(secret, new Context(key2, value2)).getValue();
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.getName(),
                secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.getSecretWithResponse#secretProperties

        // BEGIN: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        Secret secretWithVersion = secretClient.getSecretWithResponse("secretName", secretVersion,
            new Context(key2, value2)).getValue();
        System.out.printf("Secret is returned with name %s and value %s %n",
            secretWithVersion.getName(), secretWithVersion.getValue());
        // END: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecret(Secret)}
     */
    public void setSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#secret
        Secret newSecret = new Secret("secretName", "secretValue")
            .setProperties(new SecretProperties().setExpires(OffsetDateTime.now().plusDays(60)));
        Secret returnedSecret = secretClient.setSecret(newSecret);
        System.out.printf("Secret is created with name %s and value %s %n", returnedSecret.getName(),
            returnedSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecret#secret

        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#string-string
        Secret secret = secretClient.setSecret("secretName", "secretValue");
        System.out.printf("Secret is created with name %s and value %s %n", secret.getName(), secret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecret#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecretWithResponse(Secret, Context)}
     */
    public void setSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
        Secret newSecret = new Secret("secretName", "secretValue")
                 .setProperties(new SecretProperties().setExpires(OffsetDateTime.now().plusDays(60)));
        Secret secret = secretClient.setSecretWithResponse(newSecret, new Context(key1, value1)).getValue();
        System.out.printf("Secret is created with name %s and value %s %n", secret.getName(), secret.getValue());
        // END: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretProperties(SecretProperties)}
     */
    public void updateSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties
        SecretProperties secretProperties = secretClient.getSecret("secretName").getProperties();
        secretProperties.setExpires(OffsetDateTime.now().plusDays(60));
        SecretProperties updatedSecretProperties = secretClient.updateSecretProperties(secretProperties);
        Secret updatedSecret = secretClient.getSecret(updatedSecretProperties.getName());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s %n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpires());
        // END: com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretPropertiesWithResponse(SecretProperties, Context)}
     */
    public void updateSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context
        SecretProperties secretProperties = secretClient.getSecret("secretName").getProperties();
        secretProperties.setExpires(OffsetDateTime.now().plusDays(60));
        SecretProperties updatedSecretBase = secretClient.updateSecretPropertiesWithResponse(secretProperties,
            new Context(key2, value2)).getValue();
        Secret updatedSecret = secretClient.getSecret(updatedSecretBase.getName());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s %n",
            updatedSecret.getName(), updatedSecret.getValue(), updatedSecret.getProperties().getExpires());
        // END: com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecret(String)}
     */
    public void deleteSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.deleteSecret#string
        DeletedSecret deletedSecret = secretClient.deleteSecret("secretName");
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.getRecoveryId());
        // END: com.azure.security.keyvault.secretclient.deleteSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecretWithResponse(String, Context)}
     */
    public void deleteSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.deleteSecretWithResponse#string-Context
        DeletedSecret deletedSecret = secretClient.deleteSecretWithResponse("secretName",
            new Context(key2, value2)).getValue();
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.getRecoveryId());
        // END: com.azure.security.keyvault.secretclient.deleteSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecret(String)}
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
     * Method to insert code snippets for {@link SecretClient#recoverDeletedSecret(String)}
     */
    public void recoverDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.recoverDeletedSecret#string
        Secret recoveredSecret = secretClient.recoverDeletedSecret("secretName");
        System.out.printf("Recovered Secret with name %s", recoveredSecret.getName());
        // END: com.azure.security.keyvault.secretclient.recoverDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#recoverDeletedSecretWithResponse(String, Context)}
     */
    public void recoverDeletedSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.recoverDeletedSecretWithResponse#string-Context
        Secret recoveredSecret = secretClient.recoverDeletedSecretWithResponse("secretName",
            new Context(key1, value1)).getValue();
        System.out.printf("Recovered Secret with name %s", recoveredSecret.getName());
        // END: com.azure.security.keyvault.secretclient.recoverDeletedSecretWithResponse#string-Context
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
     * Method to insert code snippets for {@link SecretClient#restoreSecret(byte[])}
     */
    public void restoreSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.restoreSecret#byte
        byte[] secretBackupByteArray = {};
        Secret restoredSecret = secretClient.restoreSecret(secretBackupByteArray);
        System.out
            .printf("Restored Secret with name %s and value %s", restoredSecret.getName(), restoredSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.restoreSecret#byte
    }

    /**
     * Method to insert code snippets for {@link SecretClient#restoreSecretWithResponse(byte[], Context)}
     */
    public void restoreSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context
        byte[] secretBackupByteArray = {};
        Secret restoredSecret = secretClient.restoreSecretWithResponse(secretBackupByteArray,
            new Context(key2, value2)).getValue();
        System.out
            .printf("Restored Secret with name %s and value %s", restoredSecret.getName(), restoredSecret.getValue());
        // END: com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listSecrets()}
     */
    public void listSecretsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets
        for (SecretProperties secret : secretClient.listSecrets()) {
            Secret secretWithValue = secretClient.getSecret(secret);
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets

        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets#Context
        for (SecretProperties secret : secretClient.listSecrets(new Context(key1, value2))) {
            Secret secretWithValue = secretClient.getSecret(secret);
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets#Context

        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets.iterableByPage
        secretClient.listSecrets().iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getItems().forEach(value -> {
                Secret secretWithValue = secretClient.getSecret(value);
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
     * Method to insert code snippets for {@link SecretClient#listSecretVersions(String)}
     */
    public void listSecretVersionsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string
        for (SecretProperties secret : secretClient.listSecretVersions("secretName")) {
            Secret secretWithValue = secretClient.getSecret(secret);
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string

        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context
        for (SecretProperties secret : secretClient.listSecretVersions("secretName", new Context(key1, value2))) {
            Secret secretWithValue = secretClient.getSecret(secret);
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.getName(), secretWithValue.getValue());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context

        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage
        secretClient.listSecretVersions("secretName", new Context(key1, value2))
                    .iterableByPage().forEach(resp -> {
                        System.out.printf("Got response headers . Url: %s, Status code: %d %n",
                            resp.getRequest().getUrl(), resp.getStatusCode());
                        resp.getItems().forEach(value -> {
                            Secret secretWithValue = secretClient.getSecret(value);
                            System.out.printf("Received secret's version with name %s and value %s",
                                secretWithValue.getName(), secretWithValue.getValue());
                        });
                    });
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage
    }

    /**
     * Implementation for sync SecretClient
     *
     * @return sync SecretClient
     */
    private SecretClient getSyncSecretClientCodeSnippets() {

        // BEGIN: com.azure.security.keyvault.secretclient.sync.construct
        SecretClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();
        // END: com.azure.security.keyvault.secretclient.sync.construct
        return secretClient;
    }

    /**
     * Implementation not provided for this method
     *
     * @return {@code null}
     */
    private SecretClient getSecretClient() {
        return null;
    }
}
