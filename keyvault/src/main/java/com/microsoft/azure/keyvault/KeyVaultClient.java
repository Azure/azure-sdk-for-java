/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.microsoft.azure.keyvault;

import java.io.Closeable;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.Future;

import com.microsoft.azure.keyvault.models.KeyAttributes;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyOperationResult;
import com.microsoft.azure.keyvault.models.ListKeysResponseMessage;
import com.microsoft.azure.keyvault.models.ListSecretsResponseMessage;
import com.microsoft.azure.keyvault.models.Secret;
import com.microsoft.azure.keyvault.models.SecretAttributes;
import com.microsoft.windowsazure.core.FilterableService;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.credentials.CloudCredentials;

/**
 * A client for the Azure Key Vault service.
 * <p>
 * Applications typically call one of the {@link KeyVaultClientService}
 * <code>create</code> methods to obtain an instance of this class.
 * </p>
 * 
 * @author Fernando Colombo (Microsoft)
 *
 */
public interface KeyVaultClient extends Closeable, FilterableService<KeyVaultClient> {

    /**
     * Gets the API version.
     * 
     * @return The ApiVersion value.
     */
    String getApiVersion();

    /**
     * Gets the URI used as the base for all cloud service requests.
     * 
     * @return The BaseUri value.
     */
    URI getBaseUri();

    /**
     * Gets or sets the credential
     * 
     * @return The Credentials value.
     */
    CloudCredentials getCredentials();

    /**
     * Gets or sets the credential
     * 
     * @param credentialsValue
     *            The Credentials value.
     */
    void setCredentials(final CloudCredentials credentialsValue);

    /**
     * Gets or sets the initial timeout for Long Running Operations.
     * 
     * @return The LongRunningOperationInitialTimeout value.
     */
    int getLongRunningOperationInitialTimeout();

    /**
     * Gets or sets the initial timeout for Long Running Operations.
     * 
     * @param longRunningOperationInitialTimeoutValue
     *            The LongRunningOperationInitialTimeout value.
     */
    void setLongRunningOperationInitialTimeout(final int longRunningOperationInitialTimeoutValue);

    /**
     * Gets or sets the retry timeout for Long Running Operations.
     * 
     * @return The LongRunningOperationRetryTimeout value.
     */
    int getLongRunningOperationRetryTimeout();

    /**
     * Gets or sets the retry timeout for Long Running Operations.
     * 
     * @param longRunningOperationRetryTimeoutValue
     *            The LongRunningOperationRetryTimeout value.
     */
    void setLongRunningOperationRetryTimeout(final int longRunningOperationRetryTimeoutValue);

    /**
     * The underlying {@link ServiceClient}, used mainly for tests.
     */
    ServiceClient<?> getServiceClient();

    /**
     * Performs an ENCRYPT operation using the specified key.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyVersion
     *            The key version. Can be <code>null</code>, on which case the
     *            current key version is used.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param plainText
     *            The data to be encrypted.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> encryptAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] plainText);

    /**
     * Performs an ENCRYPT operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param plainText
     *            The data to be encrypted.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> encryptAsync(String keyIdentifier, String algorithm, byte[] plainText);

    /**
     * Performs a DECRYPT operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param plainText
     *            The data to be encrypted.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> decryptAsync(String keyIdentifier, String algorithm, byte[] cipherText);

    /**
     * Performs a SIGN operation using the specified key.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyVersion
     *            The key version. Can be <code>null</code>, on which case the
     *            current key version is used.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeySignatureAlgorithm}.
     * @param digest
     *            The digest to be signed.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> signAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] digest);

    /**
     * Performs a SIGN operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeySignatureAlgorithm}.
     * @param digest
     *            The digest to be signed.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> signAsync(String keyIdentifier, String algorithm, byte[] digest);

    /**
     * Performs a VERIFY operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeySignatureAlgorithm}.
     * @param digest
     *            The signed digest.
     * @param signature
     *            The signature to be verified.
     * @return <code>true</code> if the signature was verified,
     *         <code>false</code> otherwise.
     */
    Future<Boolean> verifyAsync(String keyIdentifier, String algorithm, byte[] digest, byte[] signature);

    /**
     * Performs a WRAPKEY operation using the specified key.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyVersion
     *            The key version. Can be <code>null</code>, on which case the
     *            current key version is used.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param key
     *            The key to be wrapped
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> wrapKeyAsync(String vault, String keyName, String keyVersion, String algorithm, byte[] key);

    /**
     * Performs a WRAPKEY operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param key
     *            The key to be wrapped
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> wrapKeyAsync(String keyIdentifier, String algorithm, byte[] key);

    /**
     * Performs am UNWRAPKEY operation using the specified key.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param algorithm
     *            The algorithm name. You may use a constant from
     *            {@link JsonWebKeyEncryptionAlgorithms}.
     * @param wrappedKey
     *            The key to be unwrapped.
     * @return An instance of {@link KeyOperationResult}.
     */
    Future<KeyOperationResult> unwrapKeyAsync(String keyIdentifier, String algorithm, byte[] wrappedKey);

    /**
     * Creates a new key and returns its attributes.
     * <p>
     * This method actually creates a new key version for the informed key name.
     * Existing key versions for that name are left untouched and don't cause a
     * conflict exception.
     * </p>
     * <p>
     * The new key will become the current version for the informed key name,
     * and will be used whenever the key version is not explicitly informed on
     * an operation.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyType
     *            The key type identifier. You may use a constant from
     *            {@link JsonWebKeyType}.
     * @param keySize
     *            The key size, which corresponds to the key strength. If
     *            <code>null</code>, a default size is used for the specified
     *            type.
     * @param keyOps
     *            The operations that will be allowed on the new key. You may
     *            use values from {@link JsonWebKeyOperation}.
     * @param keyAttributes
     *            An optional instance of {@link KeyAttributes}. If
     *            <code>null</code>, default values are used.
     * @param tags
     *            An optional map of tags. If <code>null</code>, the key will
     *            have no tags.
     * @return A key bundle containing data about the created key.
     */
    Future<KeyBundle> createKeyAsync(String vault, String keyName, String keyType, Integer keySize, String[] keyOps, KeyAttributes keyAttributes, Map<String, String> tags);

    /**
     * Returns key data.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyVersion
     *            The key version. If <code>null</code>, the current key version
     *            is returned. The returned version can be identified from the
     *            <code>\"kid\"</code> attribute.
     * @return A key bundle containing data about the requested key.
     */
    Future<KeyBundle> getKeyAsync(String vault, String keyName, String keyVersion);

    /**
     * Returns key data.
     * 
     * @param keyIdentifier
     *            The key URI.
     * @return A key bundle containing data about the requested key.
     */
    Future<KeyBundle> getKeyAsync(String keyIdentifier);

    /**
     * Returns a list of keys in the vault. This method returns only one entry
     * for each key in the vault (i.e. if a key has multiple versions, it's
     * returned only once).
     * <p>
     * The service may use paging. In order to obtain all keys, the caller must
     * verify the value of {@link ListKeysResponseMessage#getNextLink()}, and if
     * not <code>null</code>, call {@link #getKeysNextAsync(String)} to return
     * the next page.
     * </p>
     * <p>
     * The caller is not required to fetch all pages (i.e. the server does not
     * hold significant resources). However, the link to next page may expire,
     * so make sure you use it as soon as possible.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param maxresults
     *            The maximum number of keys to return. If <code>null</code>,
     *            all keys are returned. This has no influence over the number
     *            of pages.
     * @return
     */
    Future<ListKeysResponseMessage> getKeysAsync(String vault, Integer maxresults);

    /**
     * Returns the next page of the list of keys in the vault.
     * 
     * @see #getKeyAsync(String)
     */
    Future<ListKeysResponseMessage> getKeysNextAsync(String nextLink);

    /**
     * Returns a list of versions of a vault key.
     * <p>
     * The service may use paging. In order to obtain all versions, the caller
     * must verify the value of {@link ListKeysResponseMessage#getNextLink()},
     * and if not <code>null</code>, call {@link #getKeysNextAsync(String)} to
     * return the next page.
     * </p>
     * <p>
     * The caller is not required to fetch all pages (i.e. the server does not
     * hold significant resources). However, the link to next page may expire,
     * so make sure you use it as soon as possible.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The name of key to get versions from.
     * @param maxresults
     *            The maximum number of keys to return. If <code>null</code>,
     *            all versions are returned. This has no influence over the
     *            number of pages.
     */
    Future<ListKeysResponseMessage> getKeyVersionsAsync(String vault, String keyName, Integer maxresults);

    /**
     * Returns the next page of the list of versions of a vault key.
     * 
     * @see #getKeyVersionsAsync(String, String, Integer)
     */
    Future<ListKeysResponseMessage> getKeyVersionsNextAsync(String nextLink);

    /**
     * Deletes a vault key, including all its versions.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @return The object that represents the current key version, as seen
     *         before the delete operation is performed.
     */
    Future<KeyBundle> deleteKeyAsync(String vault, String keyName);

    /**
     * Updates attributes of the current version of a key.
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The key name.
     * @param keyOps
     *            The operations that will be allowed on the key. You may use
     *            values from {@link JsonWebKeyOperation}. If <code>null</code>,
     *            this attribute is not modified.
     * @param keyAttributes
     *            An optional instance of {@link KeyAttributes}. If
     *            <code>null</code>, no attribute is modified.
     * @param tags
     *            An optional map of tags. If <code>null</code>, this attribute
     *            is not modified.
     * @return A key bundle that reflects the performed modifications.
     */
    Future<KeyBundle> updateKeyAsync(String vault, String keyName, String[] keyOps, KeyAttributes attributes, Map<String, String> tags);

    /**
     * Updates attributes of a version of a key.
     * <p>
     * If key URI contains the version identifier, only that version is
     * modified. Otherwise, only the current version is modified.
     * </p>
     * 
     * @param keyIdentifier
     *            The key URI.
     * @param keyOps
     *            The operations that will be allowed on the key. You may use
     *            values from {@link JsonWebKeyOperation}. If <code>null</code>,
     *            this attribute is not modified.
     * @param keyAttributes
     *            An optional instance of {@link KeyAttributes}. If
     *            <code>null</code>, no attribute is modified.
     * @param tags
     *            An optional map of tags. If <code>null</code>, this attribute
     *            is not modified.
     * @return A key bundle that reflects the performed modifications.
     */
    Future<KeyBundle> updateKeyAsync(String keyIdentifier, String[] keyOps, KeyAttributes attributes, Map<String, String> tags);

    /**
     * Imports an existing key into the service.
     * <p>
     * This method actually creates a new key version for the informed key name.
     * Existing key versions for that name are left untouched and don't cause a
     * conflict exception.
     * </p>
     * <p>
     * The key being imported will become the current version for the informed
     * key name, and will be used whenever the key version is not explicitly
     * informed on an operation.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The name of key to import.
     * @param keyBundle
     *            Key material and attributes. The key identifier must not be
     *            informed.
     * @param importToHardware
     *            <code>true</code> if the key is to be imported to hardware,
     *            <code>false</code> if not. If <code>null</code>, the system
     *            will use the default for key type.
     * @return A key bundle containing data about the imported key.
     */
    Future<KeyBundle> importKeyAsync(String vault, String keyName, KeyBundle keyBundle, Boolean importToHardware);

    /**
     * Creates a backup of a key. The backup can be used to restore the key into
     * the same vault, or into other vaults of same subscription.
     * <p>
     * All versions of the informed key are backed up. The backup value opaquely
     * identifies the key name and version, as well as the current version.
     * </p>
     * <p>
     * <b>IMPORTANT: The service does not store the backup value anywhere. The
     * caller must properly save the backup value in order to restore it
     * afterwards. </b>
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param keyName
     *            The name of key to backup.
     * @return An opaque <code>byte[]</code> containing the key backup. The data
     *         is encrypted to a high-security key and cannot be inspected. It
     *         can only used by a restore operation.
     * @see #restoreKeyAsync(String, byte[])
     */
    Future<byte[]> backupKeyAsync(String vault, String keyName);

    /**
     * Restores a key into the specified vault.
     * <p>
     * All versions of the backed up key are restored. The key name is obtained
     * from the backup value. The current version becomes the one that was
     * current when the backup value was obtained.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param keyBackup
     *            The key backup value, typically obtained through
     *            {@link #backupKeyAsync(String, String)}.
     * @return A key bundle containing data about the restored key. While this
     *         operation restores all key versions, only the current version of
     *         key is returned.
     */
    Future<KeyBundle> restoreKeyAsync(String vault, byte[] keyBackup);

    /**
     * Returns secret data.
     * 
     * @param vault
     *            The vault URI.
     * @param secretName
     *            The secret name.
     * @param secretVersion
     *            The secret version. If <code>null</code>, the current secret
     *            version is returned. The returned version can be identified
     *            from the <code>\"id\"</code> attribute.
     * @return A bundle containing data about the requested secret.
     */
    Future<Secret> getSecretAsync(String vault, String secretName, String secretVersion);

    /**
     * Returns secret data.
     * 
     * @param secretIdentifier
     *            The secret URI.
     * @return A bundle containing data about the requested secret.
     */
    Future<Secret> getSecretAsync(String secretIdentifier);

    /**
     * Sets the value of a secret and returns its attributes.
     * <p>
     * This method actually creates a new secret version for the informed secret
     * name. Existing secret versions for that name are left untouched and don't
     * cause a conflict exception.
     * </p>
     * <p>
     * The informed value will become the current for the informed secret name,
     * and will be used whenever the secret version is not explicitly informed
     * on an operation.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param secretName
     *            The secret name.
     * @param value
     *            The secret value.
     * @param contentType
     *            The value content type. This is not validated. It may be used
     *            by clients, though.
     * @param secretAttributes
     *            An optional instance of {@link SecretAttributes}. If
     *            <code>null</code>, default values are used.
     * @param tags
     *            An optional map of tags. If <code>null</code>, the secret will
     *            have no tags.
     * @return A bundle containing data about the created secret.
     */
    Future<Secret> setSecretAsync(String vault, String secretName, String value, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    /**
     * Updates attributes of the current version of a secret..
     * 
     * @param vault
     *            The vault URI.
     * @param secretName
     *            The secret name.
     * @param contentType
     *            The value content type.
     * @param secretAttributes
     *            An optional instance of {@link SecretAttributes}. If
     *            <code>null</code>, no attribute is modified.
     * @param tags
     *            An optional map of tags. If <code>null</code>, this attribute
     *            is not modified.
     * @return A bundle that reflects the performed modifications.
     */
    Future<Secret> updateSecretAsync(String vault, String secretName, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    /**
     * Updates attributes of a version of a secret.
     * <p>
     * If secret URI contains the version identifier, only that version is
     * modified. Otherwise, only the current version is modified.
     * </p>
     * 
     * @param secretIdentifier
     *            The secret URI.
     * @param contentType
     *            The value content type.
     * @param secretAttributes
     *            An optional instance of {@link SecretAttributes}. If
     *            <code>null</code>, no attribute is modified.
     * @param tags
     *            An optional map of tags. If <code>null</code>, this attribute
     *            is not modified.
     * @return A bundle that reflects the performed modifications.
     */
    Future<Secret> updateSecretAsync(String secretIdentifier, String contentType, SecretAttributes secretAttributes, Map<String, String> tags);

    /**
     * Deletes a vault secret, including all its versions.
     * 
     * @param vault
     *            The vault URI.
     * @param secretName
     *            The secret name.
     * @return The object that represents the current secret version, as seen
     *         before the delete operation is performed.
     */
    Future<Secret> deleteSecretAsync(String vault, String secretName);

    /**
     * Returns a list of secrets in the vault. This method returns only one
     * entry for each secret in the vault (i.e. if a secret has multiple
     * versions, it's returned only once).
     * <p>
     * The service may use paging. In order to obtain all secrets, the caller
     * must verify the value of {@link ListSecretsResponseMessage#getNextLink()}
     * , and if not <code>null</code>, call {@link #getSecretsNextAsync(String)}
     * to return the next page.
     * </p>
     * <p>
     * The caller is not required to fetch all pages (i.e. the server does not
     * hold significant resources). However, the link to next page may expire,
     * so make sure you use it as soon as possible.
     * </p>
     * 
     * @param vault
     *            The vault URI.
     * @param maxresults
     *            The maximum number of secrets to return. If <code>null</code>,
     *            all secrets are returned. This has no influence over the
     *            number of pages.
     * @return
     */
    Future<ListSecretsResponseMessage> getSecretsAsync(String vault, Integer maxresults);

    Future<ListSecretsResponseMessage> getSecretsNextAsync(String nextLink);

    Future<ListSecretsResponseMessage> getSecretVersionsAsync(String vault, String secretName, Integer maxresults);

    Future<ListSecretsResponseMessage> getSecretVersionsNextAsync(String nextLink);

}
