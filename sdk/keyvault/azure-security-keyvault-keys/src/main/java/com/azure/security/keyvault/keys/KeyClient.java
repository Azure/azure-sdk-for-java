// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.security.keyvault.keys.models.KeyRotationPolicyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.RandomBytes;
import com.azure.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.security.keyvault.keys.models.ReleaseKeyResult;

/**
 * The {@link KeyClient} provides synchronous methods to manage {@link KeyVaultKey keys} in the Azure Key Vault. The
 * client supports creating, retrieving, updating, deleting, purging, backing up, restoring, listing, releasing and
 * rotating the {@link KeyVaultKey keys}. The client also supports listing {@link DeletedKey deleted keys} for a
 * soft-delete enabled Azure Key Vault.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.KeyClient.instantiation}
 *
 * @see KeyClientBuilder
 * @see PagedIterable
 */
@ServiceClient(builder = KeyClientBuilder.class, serviceInterfaces = KeyService.class)
public final class KeyClient {
    private final KeyAsyncClient client;

    /**
     * Creates a {@link KeyClient} that uses a {@link KeyAsyncClient} to service requests.
     *
     * @param client The {@link KeyAsyncClient} that the client routes its request through.
     */
    KeyClient(KeyAsyncClient client) {
        this.client = client;
    }

    /**
     * Get the vault endpoint url to which service requests are sent to.
     *
     * @return The vault endpoint url.
     */
    public String getVaultUrl() {
        return client.getVaultUrl();
    }

    /**
     * Creates a {@link CryptographyClient} for the latest version of a given key.
     *
     * @param keyName The name of the key.
     *
     * @return An instance of {@link CryptographyClient} associated with the latest version of a key with the
     * provided name.
     *
     * @throws IllegalArgumentException If {@code keyName} is {@code null} or empty.
     */
    public CryptographyClient getCryptographyClient(String keyName) {
        return client.getCryptographyClientBuilder(keyName, null).buildClient();
    }

    /**
     * Creates a {@link CryptographyClient} for a given key version.
     *
     * @param keyName The name of the key.
     * @param keyVersion The key version.
     *
     * @return An instance of {@link CryptographyClient} associated with a key with the provided name and version.
     * If {@code keyVersion} is {@code null} or empty, the client will use the latest version of the key.
     *
     * @throws IllegalArgumentException If {@code keyName} is {@code null} or empty.
     */
    public CryptographyClient getCryptographyClient(String keyName, String keyVersion) {
        return client.getCryptographyClientBuilder(keyName, keyVersion).buildClient();
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link KeyType keyType} indicates the type of {@link KeyVaultKey key} to create. Possible values include:
     * {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM},
     * {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key}. Prints out the details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createKey#String-KeyType}
     *
     * @param name The name of the {@link KeyVaultKey key} being created.
     * @param keyType The type of {@link KeyVaultKey key} to create. For valid values, see {@link KeyType KeyType}.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws ResourceModifiedException If {@code name} or {@code keyType} are {@code null}.
     * @throws HttpResponseException If {@code name} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(String name, KeyType keyType) {
        return createKeyWithResponse(new CreateKeyOptions(name, keyType), Context.NONE).getValue();
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} parameter is required. The {@link CreateKeyOptions#getExpiresOn() expires} and
     * {@link CreateKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateKeyOptions#isEnabled()} enabled} field is set to {@code true} by Azure Key Vault, if not specified.
     * </p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA},
     * {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} which activates in one day and expires in one year. Prints out the
     * details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions}
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the
     * {@link KeyVaultKey key} being created.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     * @throws HttpResponseException If {@code name} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(CreateKeyOptions createKeyOptions) {
        return createKeyWithResponse(createKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new {@link KeyVaultKey key} and stores it in the key vault. The create key operation can be used to
     * create any {@link KeyType keyType} in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name
     * already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateKeyOptions} parameter is required. The {@link CreateKeyOptions#getExpiresOn() expires} and
     * {@link CreateKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not specified.
     * </p>
     *
     * <p>The {@link CreateKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#EC EC}, {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA},
     * {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} which activates in one day and expires in one year. Prints out the
     * details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-Context}
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the
     * {@link KeyVaultKey key} being created.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions, Context context) {
        return client.createKeyWithResponse(createKeyOptions, context).block();
    }

    /**
     * Creates a new {@link KeyVaultKey RSA key} and stores it in the key vault. The create RSA key operation can be
     * used to create any RSA key type in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name already
     * exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} parameter is required. The {@link CreateRsaKeyOptions#getKeySize() keySize}
     * can be optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateRsaKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} with size 2048 which activates in one day and expires in one year.
     * Prints out the details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions}
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the
     * {@link KeyVaultKey RSA key} being created.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateRsaKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createRsaKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createRsaKey(CreateRsaKeyOptions createRsaKeyOptions) {
        return createRsaKeyWithResponse(createRsaKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new {@link KeyVaultKey RSA key} and stores it in the key vault. The create RSA key operation can be
     * used to create any RSA key type in Azure Key Vault. If a {@link KeyVaultKey key} with the provided name already
     * exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires the
     * {@code keys/create} permission.
     *
     * <p>The {@link CreateRsaKeyOptions} parameter is required. The {@link CreateRsaKeyOptions#getKeySize() keySize}
     * can be optionally specified. The {@link CreateRsaKeyOptions#getExpiresOn() expires} and
     * {@link CreateRsaKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateRsaKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateRsaKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey key} to create.
     * Possible values include: {@link KeyType#RSA RSA} and {@link KeyType#RSA_HSM RSA-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey RSA key} with size 2048 which activates in one day and expires in one year.
     * Prints out the details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-Context}
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the
     * {@link KeyVaultKey RSA key} being created.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateRsaKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createRsaKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions, Context context) {
        return client.createRsaKeyWithResponse(createRsaKeyOptions, context).block();
    }

    /**
     * Creates a new {@link KeyVaultKey EC key} and stores it in the key vault. The create EC key operation can be
     * used to create any EC {@link KeyType key type} in Azure Key Vault. If a {@link KeyVaultKey key} with the
     * provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve}
     * can be optionally specified. If not specified, the default value {@link KeyCurveName#P_256 P-256} is used by
     * Azure Key Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not specified.
     * </p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key} with a {@link KeyCurveName#P_384 P-384} web key curve. The key
     * activates in one day and expires in one year. Prints out the details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions}
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the
     * {@link KeyVaultKey EC key} being created.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateEcKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createEcKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createEcKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createEcKey(CreateEcKeyOptions createEcKeyOptions) {
        return createEcKeyWithResponse(createEcKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates a new {@link KeyVaultKey EC key} and stores it in the key vault. The create EC key operation can be
     * used to create any EC {@link KeyType key type} in Azure Key Vault. If a {@link KeyVaultKey key} with the
     * provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. It requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateEcKeyOptions} parameter is required. The {@link CreateEcKeyOptions#getCurveName() key curve}
     * can be optionally specified. If not specified, the default value {@link KeyCurveName#P_256 P-256} is used by
     * Azure Key Vault. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.
     * </p>
     *
     * <p>The {@link CreateEcKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#EC EC} and {@link KeyType#EC_HSM EC-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey EC key} with a {@link KeyCurveName#P_384 P-384} web key curve. The key
     * activates in one day and expires in one year. Prints out the details of the {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-Context}
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the
     * {@link KeyVaultKey EC key} being created.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateEcKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createEcKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createEcKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions, Context context) {
        return client.createEcKeyWithResponse(createEcKeyOptions, context).block();
    }

    /**
     * Creates and stores a new {@link KeyVaultKey symmetric key} in the key vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the key. This operation requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey symmetric key}. The {@link KeyVaultKey key} activates in one day and expires
     * in one year. Prints out the details of the newly {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions}
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * {@link KeyVaultKey symmetric key} being created.
     *
     * @return The {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateOctKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createOctKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createOctKey(CreateOctKeyOptions createOctKeyOptions) {
        return createOctKeyWithResponse(createOctKeyOptions, Context.NONE).getValue();
    }

    /**
     * Creates and stores a new {@link KeyVaultKey symmetric key} in the key vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the key. This operation requires
     * the {@code keys/create} permission.
     *
     * <p>The {@link CreateOctKeyOptions} parameter is required. The {@link CreateOctKeyOptions#getExpiresOn() expires}
     * and {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by Azure Key Vault, if not
     * specified.</p>
     *
     * <p>The {@link CreateOctKeyOptions#getKeyType() keyType} indicates the type of {@link KeyVaultKey} key to create.
     * Possible values include: {@link KeyType#OCT OCT} and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a new {@link KeyVaultKey symmetric key}. The {@link KeyVaultKey key} activates in one day and expires
     * in one year. Prints out the details of the newly {@link KeyVaultKey created key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.async.KeyClient.createOctKey#CreateOctKeyOptions-Context}
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * {@link KeyVaultKey symmetric key} being created.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey created key}.
     *
     * @throws HttpResponseException If {@link CreateOctKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     * @throws ResourceModifiedException If {@code createOctKeyOptions} is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions, Context context) {
        return client.createOctKeyWithResponse(createOctKeyOptions, context).block();
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into the key vault. Prints out the details of the
     * {@link KeyVaultKey imported key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey}
     *
     * @param name The name for the {@link KeyVaultKey imported key}.
     * @param keyMaterial The {@link JsonWebKey} being imported.
     *
     * @return The {@link KeyVaultKey imported key}.
     *
     * @throws HttpResponseException If {@code name} is an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(String name, JsonWebKey keyMaterial) {
        return importKeyWithResponse(new ImportKeyOptions(name, keyMaterial), Context.NONE).getValue();
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p>{@link ImportKeyOptions} is required and its fields {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} cannot be {@code null}. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. If not specified, no values are set for the fields. The
     * {@link ImportKeyOptions#isEnabled() enabled} field is set to {@code true} and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by Azure Key Vault, if they are
     * not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into the key vault. Prints out the details of the
     * {@link KeyVaultKey imported key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions}
     *
     * @param importKeyOptions The {@link ImportKeyOptions options object} containing information about the
     * {@link JsonWebKey} being imported.
     *
     * @return The {@link KeyVaultKey imported key}.
     *
     * @throws HttpResponseException If {@link ImportKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code importKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(ImportKeyOptions importKeyOptions) {
        return importKeyWithResponse(importKeyOptions, Context.NONE).getValue();
    }

    /**
     * Imports an externally created {@link JsonWebKey key} and stores it in the key vault. The import key operation
     * may be used to import any {@link KeyType key type} into Azure Key Vault. If a {@link KeyVaultKey key} with
     * the provided name already exists, Azure Key Vault creates a new version of the {@link KeyVaultKey key}. This
     * operation requires the {@code keys/import} permission.
     *
     * <p>{@link ImportKeyOptions} is required and its fields {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} cannot be {@code null}. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. If not specified, no values are set for the fields. The
     * {@link ImportKeyOptions#isEnabled() enabled} field is set to {@code true} and the
     * {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by Azure Key Vault, if they are
     * not specified.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Imports a new {@link KeyVaultKey key} into the key vault. Prints out the details of the
     * {@link KeyVaultKey imported key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-Context}
     *
     * @param importKeyOptions The {@link ImportKeyOptions options object} containing information about the
     * {@link JsonWebKey} being imported.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey imported key}.
     *
     * @throws HttpResponseException If {@link ImportKeyOptions#getName()} is an empty string.
     * @throws NullPointerException If {@code keyImportOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> importKeyWithResponse(ImportKeyOptions importKeyOptions, Context context) {
        return client.importKeyWithResponse(importKeyOptions, context).block();
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Prints out the details of the
     * {@link KeyVaultKey retrieved key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getKey#String-String}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     * @param version The version of the {@link KeyVaultKey key}  to retrieve. If this is an empty string or
     * {@code null}, this call is equivalent to calling {@link KeyClient#getKey(String)}, with the latest version
     * being retrieved.
     *
     * @return The requested {@link KeyVaultKey key}. The content of the {@link KeyVaultKey key}  is {@code null} if
     * both {@code name} and {@code version} are {@code null} or empty.
     *
     * @throws HttpResponseException If a valid {@code name} and a non-null/empty {@code version} is specified.
     * @throws ResourceNotFoundException When a {@link KeyVaultKey key} with the provided {@code name} doesn't exist in
     * the key vault or an empty/{@code null} {@code name} and a non-null/empty {@code version} is provided.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name, String version) {
        return getKeyWithResponse(name, version, Context.NONE).getValue();
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Prints out the details of the
     * {@link KeyVaultKey retrieved key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-Context}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     * @param version The version of the {@link KeyVaultKey key}  to retrieve. If this is an empty string or
     * {@code null}, this call is equivalent to calling {@link KeyClient#getKey(String)}, with the latest version
     * being retrieved.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the requested
     * {@link KeyVaultKey key}. The content of the {@link KeyVaultKey key} is {@code null} if both {@code name} and
     * {@code version} are {@code null} or empty.
     *
     * @throws HttpResponseException If a valid {@code name} and a non-null/empty {@code version} is specified.
     * @throws ResourceNotFoundException When a {@link KeyVaultKey key} with the provided {@code name} doesn't exist in
     * the key vault or an empty/{@code null} {@code name} and a non-null/empty {@code version} is provided.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, Context context) {
        return client.getKeyWithResponse(name, version, context).block();
    }

    /**
     * Gets the public part of the specified {@link KeyVaultKey key} and key version. The get key operation is
     * applicable to all {@link KeyType key types} and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a specific version of the {@link KeyVaultKey key} in the key vault. Prints out the details of the
     * {@link KeyVaultKey retrieved key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key}, cannot be {@code null}.
     *
     * @return The requested {@link KeyVaultKey key}. The content of the key is {@code null} if {@code name} is
     * {@code null} or empty.
     *
     * @throws HttpResponseException If a non null/empty and an invalid {@code name} is specified.
     * @throws ResourceNotFoundException When a key with non null/empty {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name) {
        return getKeyWithResponse(name, "", Context.NONE).getValue();
    }

    /**
     * Updates the {@link KeyProperties attributes} and {@link KeyOperation key operations} associated with the
     * specified {@link KeyVaultKey key}, but not the cryptographic key material of the specified
     * {@link KeyVaultKey key} in the key vault. The update operation changes specified
     * {@link KeyProperties attributes} of an existing stored {@link KeyVaultKey key} and
     * {@link KeyProperties attributes} that are not specified in the request are left unchanged. The cryptographic
     * key material of a {@link KeyVaultKey key} itself cannot be changed. This operation requires the
     * {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the {@link KeyVaultKey key}, changes its expiry time and
     * {@link KeyOperation key operations} and the updates the {@link KeyVaultKey key} in the key vault.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param keyOperations The updated {@link KeyOperation key operations} to associate with the key.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey updated key}.
     *
     * @throws HttpResponseException If {@link KeyProperties#getName() name} or
     * {@link KeyProperties#getVersion() version} is an empty string.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException When a key with {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey updateKeyProperties(KeyProperties keyProperties, KeyOperation... keyOperations) {
        return updateKeyPropertiesWithResponse(keyProperties, Context.NONE, keyOperations).getValue();
    }

    /**
     * Updates the {@link KeyProperties attributes} and {@link KeyOperation key operations} associated with the
     * specified {@link KeyVaultKey key}, but not the cryptographic key material of the specified
     * {@link KeyVaultKey key} in the key vault. The update operation changes specified
     * {@link KeyProperties attributes} of an existing stored {@link KeyVaultKey key} and
     * {@link KeyProperties attributes} that are not specified in the request are left unchanged. The cryptographic
     * key material of a {@link KeyVaultKey key} itself cannot be changed. This operation requires the
     * {@code keys/set} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the latest version of the {@link KeyVaultKey key}, changes its expiry time and
     * {@link KeyOperation key operations} and the updates the {@link KeyVaultKey key} in the key vault.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-Context-KeyOperation}
     *
     * @param keyProperties The {@link KeyProperties key properties} object with updated properties.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     * @param keyOperations The updated {@link KeyOperation key operations} to associate with the key.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey updated key}.
     *
     * @throws HttpResponseException If {@link KeyProperties#getName() name} or
     * {@link KeyProperties#getVersion() version} is an empty string.
     * @throws NullPointerException If {@code key} is {@code null}.
     * @throws ResourceNotFoundException When a key with {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> updateKeyPropertiesWithResponse(KeyProperties keyProperties, Context context,
                                                                 KeyOperation... keyOperations) {
        return client.updateKeyPropertiesWithResponse(keyProperties, context, keyOperations).block();
    }

    /**
     * Deletes a {@link KeyVaultKey key} of any type from the key vault. If soft-delete is enabled on the key vault then
     * the {@link KeyVaultKey key} is placed in the deleted state and requires to be purged for permanent deletion
     * else the {@link KeyVaultKey key} is permanently deleted. The delete operation applies to any
     * {@link KeyVaultKey key} stored in Azure Key Vault but it cannot be applied to an individual version
     * of a {@link KeyVaultKey key}. This operation removes the cryptographic material associated with the
     * {@link KeyVaultKey key}, which means the {@link KeyVaultKey key} is not usable for {@code Sign/Verify},
     * {@code Wrap/Unwrap} or {@code Encrypt/Decrypt} operations. This operation requires the {@code keys/delete}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes the {@link KeyVaultKey key} from the key vault. Prints out the recovery id of the
     * {@link KeyVaultKey deleted key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.deleteKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key} to be deleted.
     *
     * @return A {@link SyncPoller} to poll on and retrieve {@link DeletedKey deleted key}
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<DeletedKey, Void> beginDeleteKey(String name) {
        return client.beginDeleteKey(name).getSyncPoller();
    }

    /**
     * Gets the public part of a {@link KeyVaultKey deleted key}. The get deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Prints out the details
     * of the {@link KeyVaultKey deleted key}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getDeletedKey#String}
     *
     * @param name The name of the deleted {@link KeyVaultKey key}.
     *
     * @return The {@link DeletedKey deleted key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedKey getDeletedKey(String name) {
        return getDeletedKeyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Gets the public part of a {@link KeyVaultKey deleted key}. The get deleted Key operation is applicable for
     * soft-delete enabled vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete. Prints out the details
     * of the {@link KeyVaultKey deleted key} returned in the {@link Response HTTPresponse}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-Context}
     *
     * @param name The name of the deleted {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link DeletedKey deleted key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedKey> getDeletedKeyWithResponse(String name, Context context) {
        return client.getDeletedKeyWithResponse(name, context).block();
    }

    /**
     * Permanently deletes the specified {@link KeyVaultKey key} without the possibility of recovery. The purge
     * deleted key operation is applicable for soft-delete enabled vaults. This operation requires the
     * {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.purgeDeletedKey#String}
     *
     * @param name The name of the {@link KeyVaultKey deleted key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedKey(String name) {
        purgeDeletedKeyWithResponse(name, Context.NONE);
    }

    /**
     * Permanently deletes the specified {@link KeyVaultKey key} without the possibility of recovery. The purge
     * deleted key operation is applicable for soft-delete enabled vaults. This operation requires the
     * {@code keys/purge} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Purges the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-Context}
     *
     * @param name The name of the {@link KeyVaultKey deleted key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} containing status code and HTTP headers.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedKeyWithResponse(String name, Context context) {
        return client.purgeDeletedKeyWithResponse(name, context).block();
    }

    /**
     * Recovers the {@link KeyVaultKey deleted key} in the key vault to its latest version and can only be performed
     * on a soft-delete enabled vault. An attempt to recover an {@link KeyVaultKey non-deleted key} will return an
     * error. Consider this the inverse of the delete operation on soft-delete enabled vaults. This operation
     * requires the {@code keys/recover} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Recovers the {@link KeyVaultKey deleted key} from the key vault enabled for soft-delete.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.recoverDeletedKey#String}
     *
     * @param name The name of the {@link KeyVaultKey deleted key} to be recovered.
     *
     * @return A {@link SyncPoller} to poll on and retrieve {@link KeyVaultKey recovered key}.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
        return client.beginRecoverDeletedKey(name).getSyncPoller();
    }

    /**
     * Requests a backup of the specified {@link KeyVaultKey key} be downloaded to the client. The key backup
     * operation exports a {@link KeyVaultKey key} from Azure Key Vault in a protected form. Note that this operation
     * does not return key material in a form that can be used outside the Azure Key Vault system, the returned key
     * material is either protected to a Azure Key Vault HSM or to Azure Key Vault itself. The intent of this
     * operation is to allow a client to generate a {@link KeyVaultKey key} in one Azure Key Vault instance, backup the
     * {@link KeyVaultKey key}, and then restore it into another Azure Key Vault instance. The backup operation may
     * be used to export, in protected form, any {@link KeyType key type} from Azure Key Vault. Individual versions
     * of a {@link KeyVaultKey key} cannot be backed up. {@code Backup/Restore} can be performed within geographical
     * boundaries only; meaning that a backup from one geographical area cannot be restored to another geographical
     * area. For example, a backup from the US geographical area cannot be restored in an EU geographical area. This
     * operation requires the {@code key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the {@link KeyVaultKey key} from the key vault.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.backupKey#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return The backed up key blob.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupKey(String name) {
        return backupKeyWithResponse(name, Context.NONE).getValue();
    }

    /**
     * Requests a backup of the specified {@link KeyVaultKey key} be downloaded to the client. The key backup
     * operation exports a {@link KeyVaultKey key} from Azure Key Vault in a protected form. Note that this operation
     * does not return key material in a form that can be used outside the Azure Key Vault system, the returned key
     * material is either protected to a Azure Key Vault HSM or to Azure Key Vault itself. The intent of this
     * operation is to allow a client to generate a {@link KeyVaultKey key} in one Azure Key Vault instance, backup the
     * {@link KeyVaultKey key}, and then restore it into another Azure Key Vault instance. The backup operation may
     * be used to export, in protected form, any {@link KeyType key type} from Azure Key Vault. Individual versions
     * of a {@link KeyVaultKey key} cannot be backed up. {@code Backup/Restore} can be performed within geographical
     * boundaries only; meaning that a backup from one geographical area cannot be restored to another geographical
     * area. For example, a backup from the US geographical area cannot be restored in an EU geographical area. This
     * operation requires the {@code key/backup} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Backs up the {@link KeyVaultKey key} from the key vault and prints out the length of the key's backup byte
     * array returned in the {@link Response HTTPresponse}.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-Context}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the backed up key blob.
     *
     * @throws HttpResponseException When a key with {@code name} is an empty string.
     * @throws ResourceNotFoundException When a key with {@code name} doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupKeyWithResponse(String name, Context context) {
        return client.backupKeyWithResponse(name, context).block();
    }

    /**
     * Restores a backed up {@link KeyVaultKey key} to a vault. Imports a previously backed up {@link KeyVaultKey key}
     * into Azure Key Vault, restoring the {@link KeyVaultKey key}, its key identifier, attributes and access control
     * policies. The restore operation may be used to import a previously backed up {@link KeyVaultKey key}. Individual
     * versions of a {@link KeyVaultKey key} cannot be restored. The {@link KeyVaultKey key} is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target key vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the {@link KeyVaultKey key} is restored to a different vault. Restore will restore all versions
     * and preserve version identifiers. The restore operation is subject to security constraints: The target key
     * vault must be owned by the same Microsoft Azure Subscription as the source key vault. The user must have
     * the {@code restore} permission in the target key vault. This operation requires the {@code keys/restore}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the {@link KeyVaultKey key} in the key vault from its backup.</p>
     * // Pass the key backup byte array to the restore operation.
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.restoreKeyBackup#byte}
     *
     * @param backup The backup blob associated with the {@link KeyVaultKey key}.
     *
     * @return The {@link KeyVaultKey restored key}.
     *
     * @throws ResourceModifiedException When the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey restoreKeyBackup(byte[] backup) {
        return restoreKeyBackupWithResponse(backup, Context.NONE).getValue();
    }

    /**
     * Restores a backed up {@link KeyVaultKey key} to a vault. Imports a previously backed up {@link KeyVaultKey key}
     * into Azure Key Vault, restoring the {@link KeyVaultKey key}, its key identifier, attributes and access control
     * policies. The restore operation may be used to import a previously backed up {@link KeyVaultKey key}. Individual
     * versions of a {@link KeyVaultKey key} cannot be restored. The {@link KeyVaultKey key} is restored in its entirety
     * with the same key name as it had when it was backed up. If the key name is not available in the target key vault,
     * the restore operation will be rejected. While the key name is retained during restore, the final key identifier
     * will change if the {@link KeyVaultKey key} is restored to a different vault. Restore will restore all versions
     * and preserve version identifiers. The restore operation is subject to security constraints: The target key
     * vault must be owned by the same Microsoft Azure Subscription as the source key vault. The user must have
     * the {@code restore} permission in the target key vault. This operation requires the {@code keys/restore}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Restores the {@link KeyVaultKey key} in the key vault from its backup. Prints out the details of the
     * {@link KeyVaultKey restored key} returned in the {@link Response HTTPresponse}.</p>
     * // Pass the key backup byte array to the restore operation.
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-Context}
     *
     * @param backup The backup blob associated with the {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the {@link KeyVaultKey restored key}.
     *
     * @throws ResourceModifiedException When the {@code backup} blob is malformed.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, Context context) {
        return client.restoreKeyBackupWithResponse(backup, context).block();
    }

    /**
     * List {@link KeyVaultKey keys} in the key vault. Retrieves a list of the {@link KeyVaultKey keys} in the key
     * vault as {@link JsonWebKey} structures that contain the public part of a stored {@link KeyVaultKey key}. The list
     * operation is applicable to all {@link KeyType key types} and the individual {@link KeyVaultKey key} response
     * in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material from this information. Loop over the
     * {@link KeyProperties} and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey key} with key material included as of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material from this information. Iterate over all
     * the {@link KeyProperties} by page and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey key} with key material included as of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage}
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the {@link KeyVaultKey keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys() {
        return listPropertiesOfKeys(Context.NONE);
    }

    /**
     * List {@link KeyVaultKey keys} in the key vault. Retrieves a list of the {@link KeyVaultKey keys} in the key
     * vault as {@link JsonWebKey} structures that contain the public part of a stored {@link KeyVaultKey key}. The list
     * operation is applicable to all {@link KeyType key types} and the individual {@link KeyVaultKey key} response
     * in the list is represented by {@link KeyProperties} as only the key identifier, attributes and tags are
     * provided in the response. The key material and individual key versions are not listed in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material from this information. Loop over the
     * {@link KeyProperties} and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey key} with key material included as of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys#Context}
     *
     * <p><strong>Code Samples to iterate keys by page</strong></p>
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material from this information. Iterate over all
     * the {@link KeyProperties} by page and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey key} with key material included as of its latest version.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage}
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the {@link KeyVaultKey keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys(Context context) {
        return new PagedIterable<>(client.listPropertiesOfKeys(context));
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The {@link DeletedKey deleted keys} are retrieved as
     * {@link JsonWebKey} structures that contain the public part of a {@link DeletedKey deleted key}. The get deleted
     * keys operation is applicable for vaults enabled for soft-delete. This operation requires the {@code keys/list}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the {@link DeletedKey deleted keys} in the key vault and for each {@link DeletedKey deleted key} prints
     * out its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listDeletedKeys}
     *
     * <p><strong>Code Samples to iterate over deleted keys by page</strong></p>
     * <p>Iterates over the {@link DeletedKey deleted keys} by page in the key vault and for each deleted key prints out
     * its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage}
     *
     * @return {@link PagedIterable} of all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys() {
        return listDeletedKeys(Context.NONE);
    }

    /**
     * Lists {@link DeletedKey deleted keys} of the key vault. The {@link DeletedKey deleted keys} are retrieved as
     * {@link JsonWebKey} structures that contain the public part of a {@link DeletedKey deleted key}. The get deleted
     * keys operation is applicable for vaults enabled for soft-delete. This operation requires the {@code keys/list}
     * permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Lists the {@link DeletedKey deleted keys} in the key vault and for each {@link DeletedKey deleted key} prints
     * out its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listDeletedKeys#Context}
     *
     * <p><strong>Code Samples to iterate over deleted keys by page</strong></p>
     * <p>Iterates over the {@link DeletedKey deleted keys} by page in the key vault and for each deleted key prints out
     * its recovery id.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage}
     *
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return {@link PagedIterable} of all of the {@link DeletedKey deleted keys} in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys(Context context) {
        return new PagedIterable<>(client.listDeletedKeys(context));
    }

    /**
     * List all versions of the specified {@link KeyVaultKey keys}. The individual key response in the flux is
     * represented by {@link KeyProperties} as only the key identifier, attributes and tags are provided in the
     * response. The key material values are not provided in the response. This operation requires the
     * {@code keys/list} permission.
     *
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material for each version from this information.
     * Loop over the {@link KeyProperties key} and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material for each version from this information.
     * Iterate over all the {@link KeyProperties key} by page and call {@link KeyClient#getKey(String, String)}. This
     * will return the {@link KeyVaultKey keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified key in the vault.
     * The list is empty if a {@link KeyVaultKey key} with the provided {@code name} does not exist in the key vault.
     *
     * @throws ResourceNotFoundException When a given key {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name) {
        return listPropertiesOfKeyVersions(name, Context.NONE);
    }

    /**
     * List all versions of the specified {@link KeyVaultKey keys}. The individual key response in the flux is
     * represented by {@link KeyProperties} as only the key identifier, attributes and tags are provided in the
     * response. The key material values are not provided in the response. This operation requires the
     * {@code keys/list} permission.
     *
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material for each version from this information.
     * Loop over the {@link KeyProperties key} and call {@link KeyClient#getKey(String, String)}. This will return the
     * {@link KeyVaultKey keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-Context}
     *
     * <p><strong>Code Samples to iterate over key versions by page</strong></p>
     * <p>It is possible to get {@link KeyVaultKey full keys} with key material for each version from this information.
     * Iterate over all the {@link KeyProperties key} by page and call {@link KeyClient#getKey(String, String)}. This
     * will return the {@link KeyVaultKey keys} with key material included of the specified versions.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return {@link PagedIterable} of {@link KeyProperties key} of all the versions of the specified
     * {@link KeyVaultKey key} in the vault. The list is empty if a {@link KeyVaultKey key} with the provided
     * {@code name} does not exist in the key vault.
     *
     * @throws ResourceNotFoundException When a given key {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name, Context context) {
        return new PagedIterable<>(client.listPropertiesOfKeyVersions(name, context));
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the retrieved bytes in
     * base64Url format.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getRandomBytes#int}
     *
     * @param count The requested number of random bytes.
     *
     * @return The requested number of bytes containing random values from a managed HSM.
     */
    public RandomBytes getRandomBytes(int count) {
        return client.getRandomBytes(count).block();
    }

    /**
     * Get the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a number of bytes containing random values from a Managed HSM. Prints out the
     * {@link Response HTTP Response} details and the retrieved bytes in base64Url format.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-Context}
     *
     * @param count The requested number of random bytes.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return The {@link Response HTTP response} for this operation and the requested number of bytes containing
     * random values from a managed HSM.
     */
    public Response<RandomBytes> getRandomBytesWithResponse(int count, Context context) {
        return client.getRandomBytesWithResponse(count, context).block();
    }

    /**
     * Releases the latest version of a {@link KeyVaultKey key}.
     *
     * <p>The {@link KeyVaultKey key} must be exportable. This operation requires the {@code keys/release} permission.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Prints out the signed object that contains the release key.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String}
     *
     * @param name The name of the {@link KeyVaultKey key} to release.
     * @param target The attestation assertion for the target of the {@link KeyVaultKey key} release.
     *
     * @return The key release result containing the {@link KeyVaultKey released key}.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String target) {
        return client.releaseKey(name, target).block();
    }

    /**
     * Releases a specific version of a {@link KeyVaultKey key}.
     *
     * <p>The {@link KeyVaultKey key} must be exportable. This operation requires the {@code keys/release} permission.
     * </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Prints out the signed object that contains the release key.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.releaseKey#String-String-String}
     *
     * @param name The name of the {@link KeyVaultKey key} to release.
     * @param version The version of the key to release. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyAsyncClient#releaseKey(String, String)}, with the latest key version being released.
     * @param target The attestation assertion for the target of the {@link KeyVaultKey key} release.
     *
     * @return The key release result containing the {@link KeyVaultKey released key}.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String version, String target) {
        return client.releaseKey(name, version, target).block();
    }

    /**
     * Releases a {@link KeyVaultKey key}.
     *
     * <p>The key must be exportable. This operation requires the {@code keys/release} permission.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Releases a {@link KeyVaultKey key}. Prints out the
     * {@link Response HTTP Response} details and the signed object that contains the release key.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-Context}
     *
     * @param name The name of the {@link KeyVaultKey key} to release.
     * @param version The version of the {@link KeyVaultKey key} to release. If this is empty or {@code null}, this call
     * is equivalent to calling {@link KeyAsyncClient#releaseKey(String, String)}, with the latest key version being
     * released.
     * @param target The attestation assertion for the target of the key release.
     * @param options Additional {@link ReleaseKeyOptions options} for releasing a {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return The {@link Response HTTP response} for this operation and the {@link ReleaseKeyResult} containing the
     * {@link KeyVaultKey released key}.
     *
     * @throws IllegalArgumentException If {@code name} or {@code target} are {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version, String target,
                                                             ReleaseKeyOptions options, Context context) {
        return client.releaseKeyWithResponse(name, version, target, options, context).block();
    }

    /**
     * Rotates a {@link KeyVaultKey key}. The rotate key operation will do so based on
     * {@link KeyRotationPolicy key's rotation policy}. This operation requires the {@code keys/rotate} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Rotates a {@link KeyVaultKey key}. Prints out {@link KeyVaultKey rotated key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String}
     *
     * @param name The name of {@link KeyVaultKey key} to be rotated. The system will generate a new version in the
     * specified {@link KeyVaultKey key}.
     *
     * @return The new version of the rotated {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey rotateKey(String name) {
        return client.rotateKey(name).block();
    }

    /**
     * Rotates a {@link KeyVaultKey key}. The rotate key operation will do so based on
     * {@link KeyRotationPolicy key's rotation policy}. This operation requires the {@code keys/rotate} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Rotates a {@link KeyVaultKey key}. Prints out the {@link Response HTTP Response} and
     * {@link KeyVaultKey rotated key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-Context}
     *
     * @param name The name of {@link KeyVaultKey key} to be rotated. The system will generate a new version in the
     * specified {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return The {@link Response HTTP response} for this operation containing the new version of the rotated
     * {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> rotateKeyWithResponse(String name, Context context) {
        return client.rotateKeyWithResponse(name, context).block();
    }

    /**
     * Gets the {@link KeyRotationPolicy} for the {@link KeyVaultKey key} with the provided name. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Prints out the
     * {@link KeyRotationPolicy rotation policy key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     *
     * @return The {@link KeyRotationPolicy} for the {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyRotationPolicy getKeyRotationPolicy(String name) {
        return client.getKeyRotationPolicy(name).block();
    }

    /**
     * Gets the {@link KeyRotationPolicy} for the {@link KeyVaultKey key} with the provided name. This operation
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Retrieves the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Prints out the
     * {@link Response HTTP Response} and {@link KeyRotationPolicy rotation policy key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-Context}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A  {@link Response HTTP response} for this operation containing the {@link KeyRotationPolicy} for the
     * {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> getKeyRotationPolicyWithResponse(String name, Context context) {
        return client.getKeyRotationPolicyWithResponse(name, context).block();
    }

    /**
     * Updates the {@link KeyRotationPolicy} of the {@link KeyVaultKey key} with the provided name. This operation
     * requires the {@code keys/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Prints out the
     * {@link KeyRotationPolicy rotation policy key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicyProperties}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param keyRotationPolicyProperties The {@link KeyRotationPolicy} for the ke{@link KeyVaultKey key}y.
     *
     * @return The {@link KeyRotationPolicy} for the {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyRotationPolicy updateKeyRotationPolicy(String name, KeyRotationPolicyProperties keyRotationPolicyProperties) {
        return client.updateKeyRotationPolicy(name, keyRotationPolicyProperties).block();
    }

    /**
     * Updates the {@link KeyRotationPolicy} of the key with the provided name. This operation requires the
     * {@code keys/update} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Updates the {@link KeyRotationPolicy rotation policy} of a given {@link KeyVaultKey key}. Prints out the
     * {@link Response HTTP Response} and {@link KeyRotationPolicy rotation policy key} details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicyProperties-Context}
     *
     * @param name The name of the {@link KeyVaultKey key}.
     * @param keyRotationPolicyProperties The {@link KeyRotationPolicyProperties} for the key.
     * @param context Additional {@link Context} that is passed through the {@link HttpPipeline} during the service
     * call.
     *
     * @return A {@link Response HTTP response} for this operation containing the {@link KeyRotationPolicy} for the
     * {@link KeyVaultKey key}.
     *
     * @throws IllegalArgumentException If {@code name} is {@code null} or empty.
     * @throws ResourceNotFoundException If the {@link KeyVaultKey key} for the provided {@code name} does not exist.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> updateKeyRotationPolicyWithResponse(String name, KeyRotationPolicyProperties keyRotationPolicyProperties, Context context) {
        return client.updateKeyRotationPolicyWithResponse(name, keyRotationPolicyProperties, context).block();
    }
}
