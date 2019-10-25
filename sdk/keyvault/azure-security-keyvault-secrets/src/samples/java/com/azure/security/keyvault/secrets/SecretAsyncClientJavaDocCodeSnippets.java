// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.security.keyvault.secrets.implementation.KeyVaultCredentialPolicy;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import reactor.util.context.Context;

import java.time.OffsetDateTime;

/**
 * This class contains code samples for generating javadocs through doclets for {@link SecretClient}
 */
public final class SecretAsyncClientJavaDocCodeSnippets {

    private String key1 = "key1";
    private String key2 = "key2";
    private String value1 = "val1";
    private String value2 = "val2";

    /**
     * Generates code sample for creating a {@link SecretAsyncClient}
     * @return An instance of {@link SecretAsyncClient}
     */
    public SecretAsyncClient createAsyncClientWithHttpclient() {
        // BEGIN: com.azure.security.keyvault.secrets.async.secretclient.withhttpclient.instantiation
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .vaultUrl("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.secrets.async.secretclient.withhttpclient.instantiation
        return secretAsyncClient;
    }

    /**
     * Implementation for async SecretAsyncClient
     * @return sync SecretAsyncClient
     */
    private SecretAsyncClient getAsyncSecretClient() {

        // BEGIN: com.azure.security.keyvault.secrets.async.secretclient.construct
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .vaultUrl("https://myvault.vault.azure.net/")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildAsyncClient();
        // END: com.azure.security.keyvault.secrets.async.secretclient.construct
        return secretAsyncClient;
    }

    /**
     * Generates code sample for creating a {@link SecretAsyncClient}
     * @return An instance of {@link SecretAsyncClient}
     */
    public SecretAsyncClient createAsyncClientWithPipeline() {
        // BEGIN: com.azure.security.keyvault.secrets.async.secretclient.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new KeyVaultCredentialPolicy(new DefaultAzureCredentialBuilder().build()), new RetryPolicy())
            .build();
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .pipeline(pipeline)
            .vaultUrl("https://myvault.azure.net/")
            .buildAsyncClient();
        // END: com.azure.security.keyvault.secrets.async.secretclient.pipeline.instantiation
        return secretAsyncClient;
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getSecret(String, String)}
     */
    public void getSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecret#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        secretAsyncClient.getSecret("secretName", secretVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s %n",
                    secretWithVersion.getName(), secretWithVersion.getValue()));
        // END: com.azure.keyvault.secrets.secretclient.getSecret#string-string

        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecret#string
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s %n",
                    secretWithVersion.getName(), secretWithVersion.getValue()));
        // END: com.azure.keyvault.secrets.secretclient.getSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getSecretWithResponse(String, String)}
     */
    public void getSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();

        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        secretAsyncClient.getSecretWithResponse("secretName", secretVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s %n",
                    secretWithVersion.getValue().getName(), secretWithVersion.getValue().getValue()));
        // END: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#setSecret(KeyVaultSecret)}
     */
    public void setSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecret#secret
        KeyVaultSecret newSecret = new KeyVaultSecret("secretName", "secretValue").
            setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        secretAsyncClient.setSecret(newSecret)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
            System.out.printf("Secret is created with name %s and value %s %n",
                secretResponse.getName(), secretResponse.getValue()));
        // END: com.azure.keyvault.secrets.secretclient.setSecret#secret

        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecret#string-string
        secretAsyncClient.setSecret("secretName", "secretValue")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n",
                    secretResponse.getName(), secretResponse.getValue()));
        // END: com.azure.keyvault.secrets.secretclient.setSecret#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#setSecretWithResponse(KeyVaultSecret)}
     */
    public void setSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret
        KeyVaultSecret newSecret = new KeyVaultSecret("secretName", "secretValue").
            setProperties(new SecretProperties().setExpiresOn(OffsetDateTime.now().plusDays(60)));
        secretAsyncClient.setSecretWithResponse(newSecret)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s %n",
                    secretResponse.getValue().getName(), secretResponse.getValue().getValue()));
        // END: com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#updateSecretProperties(SecretProperties)}
     */
    public void updateSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.updateSecretProperties#secretProperties
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponseValue -> {
                SecretProperties secretProperties = secretResponseValue.getProperties();
                //Update the not before time of the secret.
                secretProperties.setNotBefore(OffsetDateTime.now().plusDays(50));
                secretAsyncClient.updateSecretProperties(secretProperties)
                    .subscribe(secretResponse ->
                        System.out.printf("Secret's updated not before time %s %n",
                            secretResponse.getNotBefore().toString()));
            });
        // END: com.azure.keyvault.secrets.secretclient.updateSecretProperties#secretProperties
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#updateSecretPropertiesWithResponse(SecretProperties)}
     */
    public void updateSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.updateSecretPropertiesWithResponse#secretProperties
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponseValue -> {
                SecretProperties secretProperties = secretResponseValue.getProperties();
                //Update the not before time of the secret.
                secretProperties.setNotBefore(OffsetDateTime.now().plusDays(50));
                secretAsyncClient.updateSecretPropertiesWithResponse(secretProperties)
                    .subscribe(secretResponse ->
                        System.out.printf("Secret's updated not before time %s %n",
                            secretResponse.getValue().getNotBefore().toString()));
            });
        // END: com.azure.keyvault.secrets.secretclient.updateSecretPropertiesWithResponse#secretProperties
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#beginDeleteSecret(String)}
     */
    public void deleteSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.deleteSecret#string
        secretAsyncClient.beginDeleteSecret("secretName")
            .getObserver()
            .subscribe(pollResponse -> {
                System.out.println("Delete Status: " + pollResponse.getStatus().toString());
                System.out.println("Deleted Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Deleted Secret Value: " + pollResponse.getValue().getValue());
            });
        // END: com.azure.keyvault.secrets.secretclient.deleteSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getDeletedSecret(String)}
     */
    public void getDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getDeletedSecret#string
        secretAsyncClient.getDeletedSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s %n", deletedSecretResponse.getRecoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.getDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getDeletedSecretWithResponse(String)}
     */
    public void getDeletedSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getDeletedSecretWithResponse#string
        secretAsyncClient.getDeletedSecretWithResponse("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s %n",
                    deletedSecretResponse.getValue().getRecoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.getDeletedSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#purgeDeletedSecret(String)}
     */
    public void purgeDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string
        secretAsyncClient.purgeDeletedSecret("deletedSecretName")
            .doOnSuccess(purgeResponse ->
                System.out.println("Successfully Purged deleted Secret"));
        // END: com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#purgeDeletedSecretWithResponse(String)}
     */
    public void purgeDeletedSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.purgeDeletedSecretWithResponse#string
        secretAsyncClient.purgeDeletedSecretWithResponse("deletedSecretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d %n", purgeResponse.getStatusCode()));
        // END: com.azure.keyvault.secrets.secretclient.purgeDeletedSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#beginRecoverDeletedSecret(String)}
     */
    public void recoverDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string
        secretAsyncClient.beginRecoverDeletedSecret("deletedSecretName")
            .getObserver()
            .subscribe(pollResponse -> {
                System.out.println("Recovery Status: " + pollResponse.getStatus().toString());
                System.out.println("Recovered Secret Name: " + pollResponse.getValue().getName());
                System.out.println("Recovered Secret Value: " + pollResponse.getValue().getValue());
            });
        // END: com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#backupSecret(String)}
     */
    public void backupSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.backupSecret#string
        secretAsyncClient.backupSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBackupResponse ->
                System.out.printf("Secret's Backup Byte array's length %s %n", secretBackupResponse.length));
        // END: com.azure.keyvault.secrets.secretclient.backupSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#backupSecretWithResponse(String)}
     */
    public void backupSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.backupSecretWithResponse#string
        secretAsyncClient.backupSecretWithResponse("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBackupResponse ->
                System.out.printf("Secret's Backup Byte array's length %s %n", secretBackupResponse.getValue().length));
        // END: com.azure.keyvault.secrets.secretclient.backupSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#restoreSecretBackup(byte[])}
     */
    public void restoreSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.restoreSecret#byte
        byte[] secretBackupByteArray = {};
        secretAsyncClient.restoreSecretBackup(secretBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse -> System.out.printf("Restored Secret with name %s and value %s %n",
                secretResponse.getName(), secretResponse.getValue()));
        // END: com.azure.keyvault.secrets.secretclient.restoreSecret#byte
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#restoreSecretBackupWithResponse(byte[])}
     */
    public void restoreSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte
        byte[] secretBackupByteArray = {};
        secretAsyncClient.restoreSecretBackupWithResponse(secretBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse -> System.out.printf("Restored Secret with name %s and value %s %n",
                secretResponse.getValue().getName(), secretResponse.getValue().getValue()));
        // END: com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#listPropertiesOfSecrets()}
     */
    public void listSecretsCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.listSecrets
        secretAsyncClient.listPropertiesOfSecrets()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretProperties -> secretAsyncClient
                .getSecret(secretProperties.getName(), secretProperties.getVersion())
                .subscribe(secretResponse -> System.out.printf("Received secret with name %s and type %s",
                    secretResponse.getName(), secretResponse.getValue())));
        // END: com.azure.keyvault.secrets.secretclient.listSecrets
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#listDeletedSecrets()}
     */
    public void listDeletedSecretsCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.listDeletedSecrets
        secretAsyncClient.listDeletedSecrets()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->  System.out.printf("Deleted Secret's Recovery Id %s %n",
                deletedSecretResponse.getRecoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.listDeletedSecrets
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#listPropertiesOfSecretVersions(String)}
     */
    public void listSecretVersionsCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.listSecretVersions#string
        secretAsyncClient.listPropertiesOfSecretVersions("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretProperties -> secretAsyncClient
                .getSecret(secretProperties.getName(), secretProperties.getVersion())
                .subscribe(secretResponse -> System.out.printf("Received secret with name %s and type %s",
                    secretResponse.getName(), secretResponse.getValue())));
        // END: com.azure.keyvault.secrets.secretclient.listSecretVersions#string
    }
}
