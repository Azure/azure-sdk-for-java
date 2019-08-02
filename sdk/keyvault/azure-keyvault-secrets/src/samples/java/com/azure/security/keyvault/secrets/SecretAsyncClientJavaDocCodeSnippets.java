// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.test.models.RecordedData;
import com.azure.core.test.policy.RecordNetworkCallPolicy;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;
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
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        SecretAsyncClient keyClient = new SecretClientBuilder()
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .addPolicy(new RecordNetworkCallPolicy(networkData))
            .httpClient(HttpClient.createDefault())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.secrets.async.secretclient.withhttpclient.instantiation
        return keyClient;
    }

    /**
     * Implementation for async SecretAsyncClient
     * @return sync SecretAsyncClient
     */
    private SecretAsyncClient getAsyncSecretClient() {

        // BEGIN: com.azure.security.keyvault.secrets.async.secretclient.construct
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .credential(new DefaultAzureCredentialBuilder().build())
            .endpoint("https://myvault.vault.azure.net/")
            .httpLogDetailLevel(HttpLogDetailLevel.BODY_AND_HEADERS)
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
        RecordedData networkData = new RecordedData();
        HttpPipeline pipeline = new HttpPipelineBuilder().policies(new RecordNetworkCallPolicy(networkData)).build();
        SecretAsyncClient secretAsyncClient = new SecretClientBuilder()
            .pipeline(pipeline)
            .endpoint("https://myvault.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.secrets.async.secretclient.pipeline.instantiation
        return secretAsyncClient;
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getSecret(SecretBase)}
     */
    public void getSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecret#secretBase
        secretAsyncClient.listSecrets()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBase -> secretAsyncClient.getSecret(secretBase)
                .subscribe(secretResponse ->
                    System.out.printf("Secret is returned with name %s and value %s %n", secretResponse.name(),
                        secretResponse.value())));
        // END: com.azure.keyvault.secrets.secretclient.getSecret#secretBase

        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecret#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        secretAsyncClient.getSecret("secretName", secretVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s \n",
                    secretWithVersion.name(), secretWithVersion.value()));
        // END: com.azure.keyvault.secrets.secretclient.getSecret#string-string

        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecret#string
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s \n",
                    secretWithVersion.name(), secretWithVersion.value()));
        // END: com.azure.keyvault.secrets.secretclient.getSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#getSecretWithResponse(SecretBase)}
     */
    public void getSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#secretBase
        secretAsyncClient.listSecrets()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBase -> secretAsyncClient.getSecretWithResponse(secretBase)
                .subscribe(secretResponse ->
                    System.out.printf("Secret is returned with name %s and value %s %n", secretResponse.value().name(),
                        secretResponse.value().value())));
        // END: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#secretBase

        // BEGIN: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string
        String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
        secretAsyncClient.getSecretWithResponse("secretName", secretVersion)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretWithVersion ->
                System.out.printf("Secret is returned with name %s and value %s \n",
                    secretWithVersion.value().name(), secretWithVersion.value().value()));
        // END: com.azure.keyvault.secrets.secretclient.getSecretWithResponse#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#setSecret(Secret)}
     */
    public void setSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecret#secret
        Secret newSecret = new Secret("secretName", "secretValue").
            expires(OffsetDateTime.now().plusDays(60));
        secretAsyncClient.setSecret(newSecret)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
            System.out.printf("Secret is created with name %s and value %s \n",
                secretResponse.name(), secretResponse.value()));
        // END: com.azure.keyvault.secrets.secretclient.setSecret#secret

        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecret#string-string
        secretAsyncClient.setSecret("secretName", "secretValue")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n",
                    secretResponse.name(), secretResponse.value()));
        // END: com.azure.keyvault.secrets.secretclient.setSecret#string-string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#setSecretWithResponse(Secret)}
     */
    public void setSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret
        Secret newSecret = new Secret("secretName", "secretValue").
            expires(OffsetDateTime.now().plusDays(60));
        secretAsyncClient.setSecretWithResponse(newSecret)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse ->
                System.out.printf("Secret is created with name %s and value %s \n",
                    secretResponse.value().name(), secretResponse.value().value()));
        // END: com.azure.keyvault.secrets.secretclient.setSecretWithResponse#secret
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#updateSecret(SecretBase)}
     */
    public void updateSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.updateSecret#secretBase
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponseValue -> {
                Secret secret = secretResponseValue;
                //Update the not before time of the secret.
                secret.notBefore(OffsetDateTime.now().plusDays(50));
                secretAsyncClient.updateSecret(secret)
                    .subscribe(secretResponse ->
                        System.out.printf("Secret's updated not before time %s \n",
                            secretResponse.notBefore().toString()));
            });
        // END: com.azure.keyvault.secrets.secretclient.updateSecret#secretBase
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#updateSecretWithResponse(SecretBase)}
     */
    public void updateSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.updateSecretWithResponse#secretBase
        secretAsyncClient.getSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponseValue -> {
                Secret secret = secretResponseValue;
                //Update the not before time of the secret.
                secret.notBefore(OffsetDateTime.now().plusDays(50));
                secretAsyncClient.updateSecretWithResponse(secret)
                    .subscribe(secretResponse ->
                        System.out.printf("Secret's updated not before time %s \n",
                            secretResponse.value().notBefore().toString()));
            });
        // END: com.azure.keyvault.secrets.secretclient.updateSecretWithResponse#secretBase
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#deleteSecret(String)}
     */
    public void deleteSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.deleteSecret#string
        secretAsyncClient.deleteSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.deleteSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#deleteSecretWithResponse(String)}
     */
    public void deleteSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.deleteSecretWithResponse#string
        secretAsyncClient.deleteSecretWithResponse("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.deleteSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#deleteSecret(String)}
     */
    public void getDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.getDeletedSecret#string
        secretAsyncClient.getDeletedSecret("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(deletedSecretResponse ->
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.recoveryId()));
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
                System.out.printf("Deleted Secret's Recovery Id %s \n", deletedSecretResponse.value().recoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.getDeletedSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#purgeDeletedSecret(String)}
     */
    public void purgeDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string
        secretAsyncClient.purgeDeletedSecret("deletedSecretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(purgeResponse ->
                System.out.printf("Purge Status response %d \n", purgeResponse.statusCode()));
        // END: com.azure.keyvault.secrets.secretclient.purgeDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#recoverDeletedSecret(String)}
     */
    public void recoverDeletedSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string
        secretAsyncClient.recoverDeletedSecret("deletedSecretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Secret with name %s \n", recoveredSecretResponse.name()));
        // END: com.azure.keyvault.secrets.secretclient.recoverDeletedSecret#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#recoverDeletedSecretWithResponse(String)}
     */
    public void recoverDeletedSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.recoverDeletedSecretWithResponse#string
        secretAsyncClient.recoverDeletedSecretWithResponse("deletedSecretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(recoveredSecretResponse ->
                System.out.printf("Recovered Secret with name %s \n", recoveredSecretResponse.value().name()));
        // END: com.azure.keyvault.secrets.secretclient.recoverDeletedSecretWithResponse#string
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
                System.out.printf("Secret's Backup Byte array's length %s \n", secretBackupResponse.length));
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
                System.out.printf("Secret's Backup Byte array's length %s \n", secretBackupResponse.value().length));
        // END: com.azure.keyvault.secrets.secretclient.backupSecretWithResponse#string
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#restoreSecret(byte[])}
     */
    public void restoreSecretCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.restoreSecret#byte
        byte[] secretBackupByteArray = {};
        secretAsyncClient.restoreSecret(secretBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse -> System.out.printf("Restored Secret with name %s and value %s \n",
                secretResponse.name(), secretResponse.value()));
        // END: com.azure.keyvault.secrets.secretclient.restoreSecret#byte
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#restoreSecretWithResponse(byte[])}
     */
    public void restoreSecretWithResponseCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte
        byte[] secretBackupByteArray = {};
        secretAsyncClient.restoreSecretWithResponse(secretBackupByteArray)
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretResponse -> System.out.printf("Restored Secret with name %s and value %s \n",
                secretResponse.value().name(), secretResponse.value().value()));
        // END: com.azure.keyvault.secrets.secretclient.restoreSecretWithResponse#byte
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#listSecrets()}
     */
    public void listSecretsCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.listSecrets
        secretAsyncClient.listSecrets()
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBase -> secretAsyncClient.getSecret(secretBase)
                .subscribe(secretResponse -> System.out.printf("Received secret with name %s and type %s",
                    secretResponse.name(), secretResponse.value())));
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
            .subscribe(deletedSecretResponse ->  System.out.printf("Deleted Secret's Recovery Id %s \n",
                deletedSecretResponse.recoveryId()));
        // END: com.azure.keyvault.secrets.secretclient.listDeletedSecrets
    }

    /**
     * Method to insert code snippets for {@link SecretAsyncClient#listSecretVersions(String)}
     */
    public void listSecretVersionsCodeSnippets() {
        SecretAsyncClient secretAsyncClient = getAsyncSecretClient();
        // BEGIN: com.azure.keyvault.secrets.secretclient.listSecretVersions#string
        secretAsyncClient.listSecretVersions("secretName")
            .subscriberContext(Context.of(key1, value1, key2, value2))
            .subscribe(secretBase -> secretAsyncClient.getSecret(secretBase)
                .subscribe(secretResponse -> System.out.printf("Received secret with name %s and type %s",
                    secretResponse.name(), secretResponse.value())));
        // END: com.azure.keyvault.secrets.secretclient.listSecretVersions#string
    }
}
