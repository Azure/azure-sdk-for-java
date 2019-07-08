// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.VoidResponse;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.Secret;
import com.azure.security.keyvault.secrets.models.SecretBase;

import java.util.List;

/**
 * The SecretClient provides synchronous methods to manage {@link Secret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link Secret secrets}. The client
 * also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secretclient.sync.construct}
 *
 * @see SecretClientBuilder
 */
public final class SecretClient {
    private final SecretAsyncClient client;


    /**
     * Creates a SecretClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link SecretAsyncClient} that the client routes its request through.
     */
    SecretClient(SecretAsyncClient client) {
        this.client = client;
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, a new version of the secret
     * is created in the key vault. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link Secret} is required. The {@link Secret#expires() expires}, {@link Secret#contentType() contentType} and
     * {@link Secret#notBefore() notBefore} values in {@code secret} are optional. The {@link Secret#enabled() enabled} field is
     * set to true by key vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret which expires in 60 days in the key vault. Prints out the details of the
     * newly created secret returned in the response.</p>
     * <pre>
     * Secret secret = new Secret("secretName", "secretValue")
     *   .expires(OffsetDateTime.now.plusDays(60));
     *
     * Secret retSecret = secretClient.setSecret(keySecret).value();
     * System.out.printf("Secret is created with name %s and value %s \n", retSecret.name(), retSecret.value());
     * </pre>
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties secret.name and secret.value must be non null.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpRequestException if {@link Secret#name() name} or {@link Secret#value() value} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Secret created secret}.
     */
    public Response<Secret> setSecret(Secret secret) {
        return client.setSecret(secret).block();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the response.</p>
     * <pre>
     * Secret secret = secretClient.setSecret("secretName", "secretValue").value();
     * System.out.printf("Secret is created with name %s and value %s \n", secret.name(), secret.value());
     * </pre>
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} is specified.
     * @throws HttpRequestException if {@code name} or {@code value} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Secret created secret}.
     */
    public Response<Secret> setSecret(String name, String value) {
        return client.setSecret(name, value).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * <pre>
     * String secretVersion = "6A385B124DEF4096AF1361A85B16C204";
     * Secret secretWithVersion = secretClient.getSecret("secretName", secretVersion).value();
     * System.out.printf("Secret is returned with name %s and value %s \n", secretWithVersion.name(), secretWithVersion.value());
     * </pre>
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Secret secret}.
     */
    public Response<Secret> getSecret(String name, String version) {
        return client.getSecret(name, version).block();
    }

    /**
     * Get the secret which represents {@link SecretBase secretBase} from the key vault. The get operation is applicable to any
     * secret stored in Azure Key Vault. This operation requires the {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretClient#listSecrets()} and {@link SecretClient#listSecretVersions(String)} return
     * the {@link List} containing {@link SecretBase base secret} as output excluding the include the value of the secret.
     * This operation can then be used to get the full secret with its value from {@code secretBase}.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecret#secretBase}
     *
     * @param secretBase The {@link SecretBase base secret} holding attributes of the secret being requested.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name()  name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Secret secret}.
     */
    public Response<Secret> getSecret(SecretBase secretBase) {
        return client.getSecret(secretBase).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * <pre>
     * Secret secret = secretClient.getSecret("secretName").value();
     * System.out.printf("Secret is returned with name %s and value %s \n", secret.name(), secret.value());
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value()} contains the requested {@link Secret}.
     */
    public Response<Secret> getSecret(String name) {
        return getSecret(name, "");
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key vault. The update
     * operation changes specified attributes of an existing stored secret and attributes that are not specified in the request are left unchanged.
     * The value of a secret itself cannot be changed. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields {@link SecretBase#name() name} and {@link SecretBase#version() version} cannot be null.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the secret, changes its expiry time and the updates the secret in the key vault.</p>
     * <pre>
     * Secret secret = secretClient.getSecret("secretName").value();
     * secret.expires(OffsetDateTime.now().plusDays(60));
     * SecretBase updatedSecretBase = secretClient.updateSecret(secret).value();
     * Secret updatedSecret = secretClient.getSecret(updatedSecretBase.name()).value();
     * </pre>
     *
     * @param secret The {@link SecretBase base secret} object with updated properties.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretBase#name() name} and {@link SecretBase#version() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretBase#name() name} or {@link SecretBase#version() version} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link SecretBase updated secret}.
     */
    public Response<SecretBase> updateSecret(SecretBase secret) {
        return client.updateSecret(secret).block();
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the deleted state
     * and requires to be purged for permanent deletion else the secret is permanently deleted. The delete operation applies to any secret stored in Azure Key Vault but
     * it cannot be applied to an individual version of a secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the secret from the keyvault. Prints out the recovery id of the deleted secret returned in the response.</p>
     * <pre>
     * DeletedSecret deletedSecret = secretClient.deleteSecret("secretName").value();
     * System.out.printf("Deleted Secret's Recovery Id %s", deletedSecret.recoveryId()));
     * </pre>
     *
     * @param name The name of the secret to be deleted.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link DeletedSecret deleted secret}.
     */
    public Response<DeletedSecret> deleteSecret(String name) {
        return client.deleteSecret(name).block();
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted secret from the key vault enabled for soft-delete. Prints out the details of the deleted secret
     * returned in the response.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * DeletedSecret deletedSecret = secretClient.getDeletedSecret("secretName").value();
     * System.out.printf("Deleted Secret with recovery Id %s \n", deletedSecret.recoveryId());
     * </pre>
     *
     * @param name The name of the deleted secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link DeletedSecret deleted secret}.
     */
    public Response<DeletedSecret> getDeletedSecret(String name) {
        return client.getDeletedSecret(name).block();
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Prints out the status code from the server response.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * VoidResponse purgeResponse = secretClient.purgeDeletedSecret("deletedSecretName");
     * System.out.printf("Purge Status Code: %d", purgeResponse.statusCode());
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link VoidResponse}.
     */
    public VoidResponse purgeDeletedSecret(String name) {
        return client.purgeDeletedSecret(name).block();
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for soft-delete. Prints out the details of the recovered secret
     * returned in the response.</p>
     * <pre>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * Secret recoveredSecret =  secretClient.recoverDeletedSecret("deletedSecretName").value();
     * System.out.printf("Recovered Secret with name %s", recoveredSecret.name());
     * </pre>
     *
     * @param name The name of the deleted secret to be recovered.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Secret recovered secret}.
     */
    public Response<Secret> recoverDeletedSecret(String name) {
        return client.recoverDeletedSecret(name).block();
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in the response</p>
     * <pre>
     * byte[] secretBackup = secretClient.backupSecret("secretName").value();
     * System.out.printf("Secret's Backup Byte array's length %s", secretBackup.length);
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link Response} whose {@link Response#value() value} contains the backed up secret blob.
     */
    public Response<byte[]> backupSecret(String name) {
        return client.backupSecret(name).block();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup byte array. Prints out the details of the restored secret returned
     * in the response.</p>
     * <pre>
     * //Pass the secret backup byte array of the secret to be restored.
     * Secret restoredSecret = secretClient.restoreSecret(secretBackupByteArray).value();
     * System.out.printf("Restored Secret with name %s and value %s", restoredSecret.name(), restoredSecret.value());
     * </pre>
     *
     * @param backup The backup blob associated with the secret.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     * @return A {@link Response} whose {@link Response#value() value} contains the {@link Secret restored secret}.
     */
    public Response<Secret> restoreSecret(byte[] backup) {
        return client.restoreSecret(backup).block();
    }

    /**
     * List the secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual secret response
     * in the list is represented by {@link SecretBase} as only the base secret identifier and its attributes are
     * provided in the response. The secret values and individual secret versions are not listed in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full secrets with values from this information. Loop over the {@link SecretBase secret} and
     * call {@link SecretClient#getSecret(SecretBase baseSecret)} . This will return the {@link Secret secret} with value included of its latest version.</p>
     * <pre>
     * for (SecretBase secret : secretClient.listSecrets()) {
     *   Secret secretWithValue  = secretClient.getSecret(secret).value();
     *   System.out.printf("Received secret with name %s and value %s", secretWithValue.name(), secretWithValue.value());
     * }
     * </pre>
     *
     * @return A {@link List} containing {@link SecretBase} of all the secrets in the vault. The {@link SecretBase} contains all the information about the secret, except its value.
     */
    public Iterable<SecretBase> listSecrets() {
        return client.listSecrets().toIterable();
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault. The get deleted secrets operation returns the secrets that
     * have been deleted for a vault enabled for soft-delete. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted secrets in the key vault and for each deleted secret prints out its recovery id.</p>
     * <pre>
     * for (DeletedSecret deletedSecret : secretClient.listDeletedSecrets()) {
     *   System.out.printf("Deleted secret's recovery Id %s", deletedSecret.recoveryId());
     * }
     * </pre>
     *
     * @return A {@link List} containing all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public Iterable<DeletedSecret> listDeletedSecrets() {
        return client.listDeletedSecrets().toIterable();
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link SecretBase}
     * as only the base secret identifier and its attributes are provided in the response. The secret values are
     * not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values for each version from this information. Loop over the {@link SecretBase secret} and
     * call {@link SecretClient#getSecret(SecretBase)} . This will return the {@link Secret} secrets with values included of the specified versions.</p>
     * <pre>
     * for (SecretBase secret : secretClient.listSecretVersions("secretName")) {
     *   Secret secretWithValue  = secretClient.getSecret(secret).value();
     *   System.out.printf("Received secret's version with name %s and value %s", secretWithValue.name(), secretWithValue.value());
     * }
     * </pre>
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     * @return A {@link List} containing {@link SecretBase} of all the versions of the specified secret in the vault. List is empty if secret with {@code name} does not exist in key vault
     */
    public Iterable<SecretBase> listSecretVersions(String name) {
        return client.listSecretVersions(name).toIterable();
    }
}
