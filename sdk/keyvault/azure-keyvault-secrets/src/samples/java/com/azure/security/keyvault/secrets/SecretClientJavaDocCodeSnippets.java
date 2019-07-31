// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

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
     * Method to insert code snippets for {@link SecretClient#getSecret(SecretBase)}
     */
    public void getSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#secretBase
        for (SecretBase secret : secretClient.listSecrets()) {
            Secret secretWithValue  = secretClient.getSecret(secret);
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.name(),
                    secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.getSecret#secretBase

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        Secret secretWithVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s \n",
            secretWithVersion.name(), secretWithVersion.value());
        // END: com.azure.security.keyvault.secretclient.getSecret#string-string

        // BEGIN: com.azure.security.keyvault.secretclient.getSecret#string
        Secret secretWithoutVersion = secretClient.getSecret("secretName", secretVersion);
        System.out.printf("Secret is returned with name %s and value %s \n",
            secretWithoutVersion.name(), secretWithoutVersion.value());
        // END: com.azure.security.keyvault.secretclient.getSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getSecretWithResponse(String, String, Context)}
     */
    public void getSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getSecretWithResponse#secretBase
        for (SecretBase secret : secretClient.listSecrets()) {
            Secret secretWithValue  = secretClient.getSecretWithResponse(secret, new Context(key2, value2)).value();
            System.out.printf("Secret is returned with name %s and value %s %n", secretWithValue.name(),
                    secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.getSecretWithResponse#secretBase

        // BEGIN: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        Secret secretWithVersion = secretClient.getSecretWithResponse("secretName", secretVersion,
            new Context(key2, value2)).value();
        System.out.printf("Secret is returned with name %s and value %s \n",
            secretWithVersion.name(), secretWithVersion.value());
        // END: com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecret(Secret)}
     */
    public void setSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#secret
        Secret newSecret = new Secret("secretName", "secretValue").expires(OffsetDateTime.now().plusDays(60));
        Secret returnedSecret = secretClient.setSecret(newSecret);
        System.out.printf("Secret is created with name %s and value %s \n", returnedSecret.name(), returnedSecret.value());
        // END: com.azure.security.keyvault.secretclient.setSecret#secret

        // BEGIN: com.azure.security.keyvault.secretclient.setSecret#string-string
        Secret secret = secretClient.setSecret("secretName", "secretValue");
        System.out.printf("Secret is created with name %s and value %s \n", secret.name(), secret.value());
        // END: com.azure.security.keyvault.secretclient.setSecret#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#setSecretWithResponse(Secret, Context)}
     */
    public void setSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
        Secret newSecret = new Secret("secretName", "secretValue").expires(OffsetDateTime.now().plusDays(60));
        Secret secret = secretClient.setSecretWithResponse(newSecret, new Context(key1, value1)).value();
        System.out.printf("Secret is created with name %s and value %s \n", secret.name(), secret.value());
        // END: com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecret(SecretBase)}
     */
    public void updateSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecret#secretBase
        Secret secret = secretClient.getSecret("secretName");
        secret.expires(OffsetDateTime.now().plusDays(60));
        SecretBase updatedSecretBase = secretClient.updateSecret(secret);
        Secret updatedSecret = secretClient.getSecret(updatedSecretBase.name());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s \n",
            updatedSecret.name(), updatedSecret.value(), updatedSecret.expires());
        // END: com.azure.security.keyvault.secretclient.updateSecret#secretBase
    }

    /**
     * Method to insert code snippets for {@link SecretClient#updateSecretWithResponse(SecretBase, Context)}
     */
    public void updateSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.updateSecretWithResponse#secretBase-Context
        Secret secret = secretClient.getSecret("secretName");
        secret.expires(OffsetDateTime.now().plusDays(60));
        SecretBase updatedSecretBase = secretClient.updateSecretWithResponse(secret, new Context(key2, value2)).value();
        Secret updatedSecret = secretClient.getSecret(updatedSecretBase.name());
        System.out.printf("Updated Secret is returned with name %s, value %s and expires %s \n",
            updatedSecret.name(), updatedSecret.value(), updatedSecret.expires());
        // END: com.azure.security.keyvault.secretclient.updateSecretWithResponse#secretBase-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecret(String)}
     */
    public void deleteSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.deleteSecret#string
        DeletedSecret deletedSecret = secretClient.deleteSecret("secretName");
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.recoveryId());
        // END: com.azure.security.keyvault.secretclient.deleteSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecretWithResponse(String, Context)}
     */
    public void deleteSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.deleteSecretWithResponse#string-Context
        DeletedSecret deletedSecret = secretClient.deleteSecretWithResponse("secretName",
            new Context(key2, value2)).value();
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.recoveryId());
        // END: com.azure.security.keyvault.secretclient.deleteSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#deleteSecret(String)}
     */
    public void getDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getDeletedSecret#string
        DeletedSecret deletedSecret = secretClient.getDeletedSecret("secretName");
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.recoveryId());
        // END: com.azure.security.keyvault.secretclient.getDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#getDeletedSecretWithResponse(String, Context)}
     */
    public void getDeletedSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context
        DeletedSecret deletedSecret = secretClient.getDeletedSecretWithResponse("secretName",
            new Context(key2, value2)).value();
        System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.recoveryId());
        // END: com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#purgeDeletedSecret(String)}
     */
    public void purgeDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string
        VoidResponse purgeResponse = secretClient.purgeDeletedSecret("secretName");
        System.out.printf("Purge Status Code: %d", purgeResponse.statusCode());
        // END: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string

        // BEGIN: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string-Context
        VoidResponse purgedResponse = secretClient.purgeDeletedSecret("secretName", new Context(key2, value2));
        System.out.printf("Purge Status Code: %d", purgedResponse.statusCode());
        // END: com.azure.security.keyvault.secretclient.purgeDeletedSecret#string-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#recoverDeletedSecret(String)}
     */
    public void recoverDeletedSecretCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.recoverDeletedSecret#string
        Secret recoveredSecret = secretClient.recoverDeletedSecret("secretName");
        System.out.printf("Recovered Secret with name %s", recoveredSecret.name());
        // END: com.azure.security.keyvault.secretclient.recoverDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretClient#recoverDeletedSecretWithResponse(String, Context)}
     */
    public void recoverDeletedSecretWithResponseCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.recoverDeletedSecretWithResponse#string-Context
        Secret recoveredSecret = secretClient.recoverDeletedSecretWithResponse("secretName",
            new Context(key1, value1)).value();
        System.out.printf("Recovered Secret with name %s", recoveredSecret.name());
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
            new Context(key1, value1)).value();
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
        System.out.printf("Restored Secret with name %s and value %s", restoredSecret.name(), restoredSecret.value());
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
            new Context(key2, value2)).value();
        System.out.printf("Restored Secret with name %s and value %s", restoredSecret.name(), restoredSecret.value());
        // END: com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listSecrets()}
     */
    public void listSecretsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets
        for (SecretBase secret : secretClient.listSecrets()) {
            Secret secretWithValue  = secretClient.getSecret(secret);
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.name(), secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets

        // BEGIN: com.azure.security.keyvault.secretclient.listSecrets#Context
        for (SecretBase secret : secretClient.listSecrets(new Context(key1, value2))) {
            Secret secretWithValue  = secretClient.getSecret(secret);
            System.out.printf("Received secret with name %s and value %s",
                secretWithValue.name(), secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.listSecrets#Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listDeletedSecrets()}
     */
    public void listDeletedSecretsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listDeletedSecrets
        for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets()) {
            System.out.printf("Deleted secret's recovery Id %s", deletedSecret.recoveryId());
        }
        // END: com.azure.security.keyvault.secretclient.listDeletedSecrets

        // BEGIN: com.azure.security.keyvault.secretclient.listDeletedSecrets#Context
        for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets(new Context(key1, value2))) {
            System.out.printf("Deleted secret's recovery Id %s", deletedSecret.recoveryId());
        }
        // END: com.azure.security.keyvault.secretclient.listDeletedSecrets#Context
    }

    /**
     * Method to insert code snippets for {@link SecretClient#listSecretVersions(String)}
     */
    public void listSecretVersionsCodeSnippets() {
        SecretClient secretClient = getSecretClient();
        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string
        for (SecretBase secret : secretClient.listSecretVersions("secretName")) {
            Secret secretWithValue  = secretClient.getSecret(secret);
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.name(), secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string

        // BEGIN: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context
        for (SecretBase secret : secretClient.listSecretVersions("secretName", new Context(key1, value2))) {
            Secret secretWithValue  = secretClient.getSecret(secret);
            System.out.printf("Received secret's version with name %s and value %s",
                secretWithValue.name(), secretWithValue.value());
        }
        // END: com.azure.security.keyvault.secretclient.listSecretVersions#string-Context
    }

    /**
     * Implementation for sync SecretClient
     * @return sync SecretClient
     */
    private SecretClient getSyncSecretClientCodeSnippets() {

        // BEGIN: com.azure.security.keyvault.secretclient.sync.construct
        SecretClient secretClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .buildClient();
        // END: com.azure.security.keyvault.secretclient.sync.construct
        return secretClient;
    }

    /**
     * Implementation not provided for this method
     * @return {@code null}
     */
    private SecretClient getSecretClient() {
        return null;
    }
}
