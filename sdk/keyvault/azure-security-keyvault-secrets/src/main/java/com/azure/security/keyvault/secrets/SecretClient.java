// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.exception.HttpRequestException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.polling.Poller;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;

import java.util.List;

/**
 * The SecretClient provides synchronous methods to manage {@link KeyVaultSecret secrets} in the Azure Key Vault. The client
 * supports creating, retrieving, updating, deleting, purging, backing up, restoring and listing the {@link KeyVaultSecret
 * secrets}. The client also supports listing {@link DeletedSecret deleted secrets} for a soft-delete enabled Azure Key
 * Vault.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.secretclient.sync.construct}
 *
 * @see SecretClientBuilder
 * @see PagedIterable
 */
@ServiceClient(builder = SecretClientBuilder.class, serviceInterfaces = SecretService.class)
public final class SecretClient {
    private final SecretAsyncClient client;

    /**
     * Get the vault endpoint
     * @return the vault endpoint
     */
    public String getVaultEndpoint() {
        return client.getVaultEndpoint();
    }

    /**
     * Creates a SecretClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link SecretAsyncClient} that the client routes its request through.
     */
    SecretClient(SecretAsyncClient client) {
        this.client = client;
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, a new version of the
     * secret is created in the key vault. This operation requires the {@code secrets/set} permission.
     *
     * <p>The {@link KeyVaultSecret} is required. The {@link SecretProperties#getExpiresOn() expires}, {@link SecretProperties#getContentType() contentType}
     * and
     * {@link SecretProperties#getNotBefore() notBefore} values in {@code secret} are optional. The {@link SecretProperties#isEnabled() enabled}
     * field is set to true by key vault, if not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.security.keyvault.secretclient.setSecret#secret}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     secret.name and secret.value must be non null.
     * @return The {@link KeyVaultSecret created secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceModifiedException if {@code secret} is malformed.
     * @throws HttpRequestException if {@link KeyVaultSecret#getName() name} or {@link KeyVaultSecret#getValue() value} is empty string.
     */
    public KeyVaultSecret setSecret(KeyVaultSecret secret) {
        return setSecretWithResponse(secret, Context.NONE).getValue();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault
     * creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.setSecret#string-string}
     *
     * @param name The name of the secret. It is required and cannot be null.
     * @param value The value of the secret. It is required and cannot be null.
     * @return The {@link KeyVaultSecret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} is specified.
     * @throws HttpRequestException if {@code name} or {@code value} is empty string.
     */
    public KeyVaultSecret setSecret(String name, String value) {
        return setSecretWithResponse(new KeyVaultSecret(name, value), Context.NONE).getValue();
    }

    /**
     * The set operation adds a secret to the Azure Key Vault. If the named secret already exists, Azure Key Vault
     * creates a new version of that secret.
     * This operation requires the {@code secrets/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new secret in the key vault. Prints out the details of the newly created secret returned in the
     * response.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.setSecretWithResponse#secret-Context}
     *
     * @param secret The Secret object containing information about the secret and its properties. The properties
     *     secret.name and secret.value must be non null.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret created secret}.
     * @throws ResourceModifiedException if invalid {@code name} or {@code value} is specified.
     * @throws HttpRequestException if {@code name} or {@code value} is empty string.
     */
    public Response<KeyVaultSecret> setSecretWithResponse(KeyVaultSecret secret, Context context) {
        return client.setSecretWithResponse(secret, context).block();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret
     * stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecret#string-string}
     *
     * @param name The name of the secret, cannot be null.
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is
     *     equivalent to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @return The requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the
     *     key vault.
     * @throws HttpRequestException if {@code name} or {@code version} is empty string.
     */
    public KeyVaultSecret getSecret(String name, String version) {
        return getSecretWithResponse(name, version, Context.NONE).getValue();
    }

    /**
     * Get the secret which represents {@link SecretProperties secretProperties} from the key vault. The get operation is applicable
     * to any secret stored in Azure Key Vault. This operation requires the {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretClient#listPropertiesOfSecrets()} and {@link SecretClient#listPropertiesOfSecretVersions(String)}
     * return the {@link List} containing {@link SecretProperties secret properties} as output excluding the include the value of
     * the secret.
     * This operation can then be used to get the full secret with its value from {@code secretProperties}.</p>
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecretWithResponse#secretProperties}
     *
     * @param secretProperties The {@link SecretProperties secret properties} holding attributes of the secret being requested.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretProperties#getName()  name} or {@link SecretProperties#getVersion() version} is empty
     *     string.
     */
    public Response<KeyVaultSecret> getSecretWithResponse(SecretProperties secretProperties, Context context) {
        return client.getSecretWithResponse(secretProperties, context).block();
    }

    /**
     * Get the secret which represents {@link SecretProperties secretProperties} from the key vault. The get operation is applicable
     * to any secret stored in Azure Key Vault. This operation requires the {@code secrets/get} permission.
     *
     * <p>The list operations {@link SecretClient#listPropertiesOfSecrets()} and {@link SecretClient#listPropertiesOfSecretVersions(String)}
     * return the {@link List} containing {@link SecretProperties secret properties} as output excluding the include the value of
     * the secret.
     * This operation can then be used to get the full secret with its value from {@code secretProperties}.</p>
     * <p><strong>Code Samples</strong></p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecret#secretProperties}
     *
     * @param secretProperties The {@link SecretProperties secret properties} holding attributes of the secret being requested.
     * @return The requested {@link KeyVaultSecret secret}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretProperties#getName()  name} or {@link SecretProperties#getVersion() version} is
     *     empty string.
     */
    public KeyVaultSecret getSecret(SecretProperties secretProperties) {
        return getSecretWithResponse(secretProperties, Context.NONE).getValue();
    }

    /**
     * Get the latest version of the specified secret from the key vault. The get operation is applicable to any secret
     * stored in Azure Key Vault.
     * This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the secret in the key vault. Prints out the details of the returned secret.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecret#string}
     *
     * @param name The name of the secret.
     * @return The requested {@link KeyVaultSecret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException if {@code name} is empty string.
     */
    public KeyVaultSecret getSecret(String name) {
        return getSecretWithResponse(name, "", Context.NONE).getValue();
    }

    /**
     * Get the specified secret with specified version from the key vault. The get operation is
     * applicable to any secret stored in Azure Key Vault. This operation requires the {@code secrets/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the secret in the key vault. Subscribes to the call asynchronously and prints out
     * the returned secret details when a response is received.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.getSecretWithResponse#string-string-Context}
     *
     * @param name The name of the secret, cannot be null
     * @param version The version of the secret to retrieve. If this is an empty String or null, this call is equivalent
     *     to calling {@link #getSecret(String)}, with the latest version being retrieved.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested {@link KeyVaultSecret}.
     * @throws ResourceNotFoundException when a secret with {@code name} and {@code version} doesn't exist in the key
     *     vault.
     * @throws HttpRequestException if {@code name}  name} or {@code version} is empty string.
     */
    public Response<KeyVaultSecret> getSecretWithResponse(String name, String version, Context context) {
        return client.getSecretWithResponse(name, version, context).block();
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key
     * vault. The update operation changes specified attributes of an existing stored secret and attributes that are not
     * specified in the request are left unchanged. The value of a secret itself cannot be changed. This operation
     * requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields {@link SecretProperties#getName() name} and {@link SecretProperties#getVersion()
     * version} cannot be null.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the secret, changes its expiry time and the updates the secret in the key vault.
     * </p>
     * {@codesnippet com.azure.security.keyvault.secretclient.updateSecretPropertiesWithResponse#secretProperties-Context}
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretProperties#getName() name} or {@link SecretProperties#getVersion() version} is
     *     empty string.
     */
    public Response<SecretProperties> updateSecretPropertiesWithResponse(SecretProperties secretProperties, Context context) {
        return client.updateSecretPropertiesWithResponse(secretProperties, context).block();
    }

    /**
     * Updates the attributes associated with the specified secret, but not the value of the specified secret in the key
     * vault. The update operation changes specified attributes of an existing stored secret and attributes that are not
     * specified in the request are left unchanged. The value of a secret itself cannot be changed. This operation
     * requires the {@code secrets/set} permission.
     *
     * <p>The {@code secret} is required and its fields {@link SecretProperties#getName() name} and {@link SecretProperties#getVersion()
     * version} cannot be null.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the secret, changes its expiry time and the updates the secret in the key
     * vault.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.updateSecretProperties#secretProperties}
     *
     * @param secretProperties The {@link SecretProperties secret properties} object with updated properties.
     * @return The {@link SecretProperties updated secret}.
     * @throws NullPointerException if {@code secret} is {@code null}.
     * @throws ResourceNotFoundException when a secret with {@link SecretProperties#getName() name} and {@link
     *     SecretProperties#getVersion() version} doesn't exist in the key vault.
     * @throws HttpRequestException if {@link SecretProperties#getName() name} or {@link SecretProperties#getVersion() version} is
     *     empty string.
     */
    public SecretProperties updateSecretProperties(SecretProperties secretProperties) {
        return updateSecretPropertiesWithResponse(secretProperties, Context.NONE).getValue();
    }

    /**
     * Deletes a secret from the key vault. If soft-delete is enabled on the key vault then the secret is placed in the
     * deleted state and requires to be purged for permanent deletion else the secret is permanently deleted. The delete
     * operation applies to any secret stored in Azure Key Vault but it cannot be applied to an individual version of a
     * secret. This operation requires the {@code secrets/delete} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the secret from the keyvault. Prints out the recovery id of the deleted secret returned in the
     * response.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.deleteSecret#string}
     *
     * @param name The name of the secret to be deleted.
     * @return A {@link Poller} to poll on and retrieve the {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public Poller<DeletedSecret, Void> beginDeleteSecret(String name) {
        return client.beginDeleteSecret(name);
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted secret from the key vault enabled for soft-delete. Prints out the details of the deleted
     * secret returned in the response.</p>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.security.keyvault.secretclient.getDeletedSecret#string}
     *
     * @param name The name of the deleted secret.
     * @return The {@link DeletedSecret deleted secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public DeletedSecret getDeletedSecret(String name) {
        return getDeletedSecretWithResponse(name, Context.NONE).getValue();
    }

    /**
     * The get deleted secret operation returns the secrets that have been deleted for a vault enabled for soft-delete.
     * This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the deleted secret from the key vault enabled for soft-delete. Prints out the details of the deleted
     * secret returned in the response.</p>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.security.keyvault.secretclient.getDeletedSecretWithResponse#string-Context}
     *
     * @param name The name of the deleted secret.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedSecret deleted
     * secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public Response<DeletedSecret> getDeletedSecretWithResponse(String name, Context context) {
        return client.getDeletedSecretWithResponse(name, context).block();
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the
     * {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Prints out the status code from the
     * server response.</p>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.security.keyvault.secretclient.purgeDeletedSecret#string}
     *
     * @param name The name of the secret.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public void purgeDeletedSecret(String name) {
        purgeDeletedSecretWithResponse(name, Context.NONE);
    }

    /**
     * The purge deleted secret operation removes the secret permanently, without the possibility of recovery.
     * This operation can only be enabled on a soft-delete enabled vault. This operation requires the
     * {@code secrets/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the deleted secret from the key vault enabled for soft-delete. Prints out the status code from the
     * server response.</p>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.security.keyvault.secretclient.purgeDeletedSecretWithResponse#string-Context}
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A response containing status code and HTTP headers.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public Response<Void> purgeDeletedSecretWithResponse(String name, Context context) {
        return client.purgeDeletedSecretWithResponse(name, context).block();
    }

    /**
     * Recovers the deleted secret in the key vault to its latest version and can only be performed on a soft-delete
     * enabled vault.
     * This operation requires the {@code secrets/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the deleted secret from the key vault enabled for soft-delete. Prints out the details of the
     * recovered secret returned in the response.</p>
     * //Assuming secret is deleted on a soft-delete enabled key vault.
     * {@codesnippet com.azure.security.keyvault.secretclient.recoverDeletedSecret#string}
     *
     * @param name The name of the deleted secret to be recovered.
     * @return A {@link Poller} to poll on and retrieve the {@link KeyVaultSecret recovered secret}.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public Poller<KeyVaultSecret, Void> beginRecoverDeletedSecret(String name) {
        return client.beginRecoverDeletedSecret(name);
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be
     * downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.backupSecret#string}
     *
     * @param name The name of the secret.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public byte[] backupSecret(String name) {
        return backupSecretWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Requests a backup of the specified secret be downloaded to the client. All versions of the secret will be
     * downloaded.
     * This operation requires the {@code secrets/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the secret from the key vault and prints out the length of the secret's backup byte array returned in
     * the response</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.backupSecretWithResponse#string-Context}
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up secret blob.
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public Response<byte[]> backupSecretWithResponse(String name, Context context) {
        return client.backupSecretWithResponse(name, context).block();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     * //Pass the secret backup byte array of the secret to be restored.
     * {@codesnippet com.azure.security.keyvault.secretclient.restoreSecret#byte}
     *
     * @param backup The backup blob associated with the secret.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    public KeyVaultSecret restoreSecretBackup(byte[] backup) {
        return restoreSecretBackupWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up secret, and all its versions, to a vault.
     * This operation requires the {@code secrets/restore} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the secret in the key vault from its backup byte array. Prints out the details of the restored secret
     * returned in the response.</p>
     * //Pass the secret backup byte array of the secret to be restored.
     * {@codesnippet com.azure.security.keyvault.secretclient.restoreSecretWithResponse#byte-Context}
     *
     * @param backup The backup blob associated with the secret.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultSecret restored secret}.
     * @throws ResourceModifiedException when {@code backup} blob is malformed.
     */
    public Response<KeyVaultSecret> restoreSecretBackupWithResponse(byte[] backup, Context context) {
        return client.restoreSecretBackupWithResponse(backup, context).block();
    }

    /**
     * List the secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual
     * secret response in the list is represented by {@link SecretProperties} as only the secret identifier and its
     * attributes are provided in the response. The secret values and individual secret versions are not listed in the
     * response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full secrets with values from this information. Loop over the {@link SecretProperties secret}
     * and call {@link SecretClient#getSecret(SecretProperties baseSecret)} . This will return the {@link KeyVaultSecret secret} with
     * value included of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecrets}
     *
     * <p><strong>Code Samples to iterate over secrets by page</strong></p>
     * <p>It is possible to get full secrets with values from this information. Iterate over all the {@link SecretProperties
     * secret} by page and call {@link SecretClient#getSecret(SecretProperties baseSecret)} . This will return the
     * {@link KeyVaultSecret secret} with value included of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecrets.iterableByPage}
     *
     * @return {@link PagedIterable} of {@link SecretProperties} of all the secrets in the vault. The {@link SecretProperties}
     *     contains all the information about the secret, except its value.
     */
    public PagedIterable<SecretProperties> listPropertiesOfSecrets() {
        return listPropertiesOfSecrets(Context.NONE);
    }

    /**
     * List the secrets in the key vault. The list Secrets operation is applicable to the entire vault. The individual
     * secret response in the list is represented by {@link SecretProperties} as only the secret identifier and its
     * attributes are provided in the response. The secret values and individual secret versions are not listed in the
     * response. This operation requires the {@code secrets/list} permission.
     *
     * <p><strong>Code Samples to iterate over secrets by page</strong></p>
     * <p>It is possible to get full secrets with values from this information. Loop over the {@link SecretProperties secret}
     * and call {@link SecretClient#getSecret(SecretProperties baseSecret)} . This will return the {@link KeyVaultSecret secret} with
     * value included of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecrets#Context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.*
     * @return {@link PagedIterable} of {@link SecretProperties} of all the secrets in the vault. The {@link SecretProperties}
     *     contains all the information about the secret, except its value.
     */
    public PagedIterable<SecretProperties> listPropertiesOfSecrets(Context context) {
        return new PagedIterable<>(client.listPropertiesOfSecrets(context));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault. The get deleted secrets operation returns the
     * secrets that have been deleted for a vault enabled for soft-delete. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted secrets in the key vault and for each deleted secret prints out its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listDeletedSecrets#Context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public PagedIterable<DeletedSecret> listDeletedSecrets(Context context) {
        return new PagedIterable<>(client.listDeletedSecrets(context));
    }

    /**
     * Lists {@link DeletedSecret deleted secrets} of the key vault. The get deleted secrets operation returns the
     * secrets that have been deleted for a vault enabled for soft-delete. This operation requires the
     * {@code secrets/list} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the deleted secrets in the key vault and for each deleted secret prints out its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listDeletedSecrets}
     *
     * <p><strong>Code Samples to iterate over secrets by page</strong></p>
     * <p>Iterate over Lists the deleted secrets by page in the key vault and for each deleted secret prints out its
     * recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listDeletedSecrets.iterableByPage}
     *
     * @return {@link PagedIterable} of all of the {@link DeletedSecret deleted secrets} in the vault.
     */
    public PagedIterable<DeletedSecret> listDeletedSecrets() {
        return listDeletedSecrets(Context.NONE);
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link
     * SecretProperties} as only the secret identifier and its attributes are provided in the response. The secret values
     * are not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values for each version from this information. Loop over the {@link
     * SecretProperties secret} and call {@link SecretClient#getSecret(SecretProperties)}. This will return the
     * {@link KeyVaultSecret secrets} with values included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecretVersions#string}
     *
     * @param name The name of the secret.
     * @return {@link PagedIterable} of {@link SecretProperties} of all the versions of the specified secret in the vault.
     *     List is empty if secret with {@code name} does not exist in key vault
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name) {
        return listPropertiesOfSecretVersions(name, Context.NONE);
    }

    /**
     * List all versions of the specified secret. The individual secret response in the list is represented by {@link
     * SecretProperties} as only the secret identifier and its attributes are provided in the response. The secret values
     * are not provided in the response. This operation requires the {@code secrets/list} permission.
     *
     * <p>It is possible to get full Secrets with values for each version from this information. Loop over the {@link
     * SecretProperties secret} and call {@link SecretClient#getSecret(SecretProperties)} . This will return the
     * {@link KeyVaultSecret secrets} with values included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecretVersions#string-Context}
     *
     *
     * <p><strong>Code Samples to iterate over secret versions by page</strong></p>
     * <p>It is possible to get full Secrets with values for each version from this information. Iterate over all the
     * {@link SecretProperties secret} by each page and call {@link SecretClient#getSecret(SecretProperties)} . This will return the
     * {@link KeyVaultSecret secrets} with values included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.secretclient.listSecretVersions#string-Context-iterableByPage}
     *
     * @param name The name of the secret.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return {@link PagedIterable} of {@link SecretProperties} of all the versions of the specified secret in the vault.
     *     List is empty if secret with {@code name} does not exist in key vault
     * @throws ResourceNotFoundException when a secret with {@code name} doesn't exist in the key vault.
     * @throws HttpRequestException when a secret with {@code name} is empty string.
     */
    public PagedIterable<SecretProperties> listPropertiesOfSecretVersions(String name, Context context) {
        return new PagedIterable<>(client.listPropertiesOfSecretVersions(name, context));
    }
}
