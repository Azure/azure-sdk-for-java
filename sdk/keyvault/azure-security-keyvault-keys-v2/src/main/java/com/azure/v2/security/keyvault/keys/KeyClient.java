// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.core.http.polling.PollingContext;
import com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.v2.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.v2.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeysUtils;
import com.azure.v2.security.keyvault.keys.implementation.models.BackupKeyResult;
import com.azure.v2.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyCreateParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyImportParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyReleaseParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyRestoreParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyUpdateParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils;
import com.azure.v2.security.keyvault.keys.implementation.models.RandomBytes;
import com.azure.v2.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.v2.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.v2.security.keyvault.keys.models.DeletedKey;
import com.azure.v2.security.keyvault.keys.models.ImportKeyOptions;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyCurveName;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import com.azure.v2.security.keyvault.keys.models.KeyProperties;
import com.azure.v2.security.keyvault.keys.models.KeyRotationPolicy;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import com.azure.v2.security.keyvault.keys.models.ReleaseKeyOptions;
import com.azure.v2.security.keyvault.keys.models.ReleaseKeyResult;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.createDeletedKey;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.createKeyAttributes;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.createKeyVaultKey;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.mapJsonWebKey;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.mapKeyReleasePolicy;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.mapKeyRotationPolicy;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.mapKeyRotationPolicyImpl;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * This class provides methods to manage {@link KeyVaultKey keys} in Azure Key Vault or Managed HSM. The client supports
 * creating, retrieving, updating, deleting, purging, backing up, restoring, listing, releasing and rotating the
 * {@link KeyVaultKey keys}. The client also supports listing {@link DeletedKey deleted keys} for a soft-delete enabled
 * key vault or managed HSM.
 *
 * <h2>Getting Started</h2>
 *
 * <p>In order to interact with the Azure Key Vault or Managed HSM service, you will need to create an instance of the
 * {@link KeyClient} class, an Azure Key Vault or Managed HSM endpoint and a credential object.</p>
 *
 * <p>The examples shown in this document use a credential object named {@code DefaultAzureCredential} for
 * authentication, which is appropriate for most scenarios, including local development and production environments.
 * Additionally, we recommend using a
 * <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">managed identity</a>
 * for authentication in production environments. You can find more information on different ways of authenticating and
 * their corresponding credential types in the
 * <a href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity documentation"</a>.</p>
 *
 * <p><strong>Sample: Construct Key Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link KeyClient}, using the {@link KeyClientBuilder}
 * to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.instantiation -->
 *
 * <br>
 * <hr>
 *
 * <h2>Create a Cryptographic Key</h2>
 * The {@link KeyClient} can be used to create a key in the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to create a cryptographic key in the key vault, using the
 * {@link KeyClient#createKey(String, KeyType)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
 *
 * <br>
 * <hr>
 *
 * <h2>Get a Cryptographic Key</h2>
 * The {@link KeyClient} can be used to retrieve a key from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to retrieve a key from the key vault, using the
 * {@link KeyClient#getKey(String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKey#String -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKey#String -->
 *
 * <br>
 * <hr>
 *
 * <h2>Delete a Cryptographic Key</h2>
 * The {@link KeyClient} can be used to delete a key from the key vault.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to delete a key from the key vault, using the
 * {/@link KeyClient#beginDeleteKey(String)} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String -->
 * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String -->
 *
 * @see com.azure.v2.security.keyvault.keys
 * @see KeyClientBuilder
 */
@ServiceClient(builder = KeyClientBuilder.class, serviceInterfaces = KeyClientImpl.KeyClientService.class)
public final class KeyClient {
    private static final ClientLogger LOGGER = new ClientLogger(KeyClient.class);

    private final KeyClientImpl clientImpl;

    /**
     * Creates an instance of {@link KeyClient}.
     *
     * @param clientImpl The implementation client.
     */
    KeyClient(KeyClientImpl clientImpl) {
        this.clientImpl = clientImpl;
    }

    /**
     * Creates a {@link CryptographyClient} for the latest version of a given key.
     *
     * <p>To ensure correct behavior when performing operations such as {@code Decrypt}, {@code Unwrap} and
     * {@code Verify}, it is recommended to use a {@link CryptographyClient} created for the specific key version that
     * was used for the corresponding inverse operation: {@code Encrypt}, {@code Wrap}, or {@code Sign}, respectively.
     * </p>
     *
     * <p>You can provide a key version either via {@link KeyClient#getCryptographyClient(String, String)} or by
     * ensuring it is included in the {@code keyIdentifier} passed to
     * {@link CryptographyClientBuilder#keyIdentifier(String)} before building a client.</p>
     *
     * @param keyName The name of the key.
     * @return An instance of {@link CryptographyClient} associated with the latest version of a key with the
     * provided name.
     *
     * @throws IllegalArgumentException If the provided {@code keyName} is {@code null} or an empty string.
     */
    public CryptographyClient getCryptographyClient(String keyName) {
        return getCryptographyClient(keyName, null);
    }

    /**
     * Creates a {@link CryptographyClient} for a given key version.
     *
     * @param keyName The name of the key.
     * @param keyVersion The key version.
     * @return An instance of {@link CryptographyClient} associated with a key with the provided name and version.
     * If {@code keyVersion} is {@code null} or an empty string, the client will use the latest version of the key.
     *
     * @throws IllegalArgumentException If the provided {@code keyName} is {@code null} or an empty string.
     */
    public CryptographyClient getCryptographyClient(String keyName, String keyVersion) {
        if (isNullOrEmpty(keyName)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'keyName' cannot be null or empty."));
        }

        return KeyVaultKeysUtils.getCryptographyClientBuilder(keyName, keyVersion, clientImpl.getVaultBaseUrl(),
            clientImpl.getHttpPipeline(), clientImpl.getServiceVersion()).buildClient();
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the key is
     * created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC},
     * {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT},
     * and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new key in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createKey#String-KeyType -->
     *
     * @param name The name of the key. It is required and cannot be {@code null} or empty.
     * @param keyType The type of key. For valid values, see {@link KeyType}.
     * @return The newly created key.
     *
     * @throws HttpResponseException If the provided {@code keyType} is {@code null} or invalid.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(String name, KeyType keyType) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'name' cannot be null or empty."));
        }

        return createKey(new CreateKeyOptions(name, keyType));
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the key is
     * created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createKeyOptions} parameter and its {@link CreateKeyOptions#getName() name} value are required. The
     * {@link CreateKeyOptions#getExpiresOn() expires} and {@link CreateKeyOptions#getNotBefore() notBefore} values are
     * optional. The {@link CreateKeyOptions#isEnabled()} enabled} field is set to {@code true} by default if not
     * specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC},
     * {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT},
     * and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new key which activates in one day and expires in one year. Prints out the details of the newly
     * created key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createKey#CreateKeyOptions -->
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the key being
     * created. It is required and cannot be {@code null}.
     * @return The newly created key.
     *
     * @throws HttpResponseException If {@code createKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createKey(CreateKeyOptions createKeyOptions) {
        try {
            Objects.requireNonNull(createKeyOptions, "'createKeyOptions' cannot be null.");

            if (isNullOrEmpty(createKeyOptions.getName())) {
                throw new IllegalArgumentException("'createKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createKeyOptions.getKeyType())
                .setKeyAttributes(createKeyAttributes(createKeyOptions))
                .setKeyOps(createKeyOptions.getKeyOperations())
                .setTags(createKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createKeyOptions.getReleasePolicy()));

            return createKeyVaultKey(clientImpl.createKey(createKeyOptions.getName(), keyCreateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new key and stores it in the key vault. The create key operation can be used to create any key type in
     * Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the key is
     * created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createKeyOptions} parameter and its {@link CreateKeyOptions#getName() name} value are required. The
     * {@link CreateKeyOptions#getExpiresOn() expires} and {@link CreateKeyOptions#getNotBefore() notBefore} values are
     * optional. The {@link CreateKeyOptions#isEnabled()} enabled} field is set to {@code true} by default if not
     * specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC},
     * {@link KeyType#EC_HSM EC-HSM}, {@link KeyType#RSA RSA}, {@link KeyType#RSA_HSM RSA-HSM}, {@link KeyType#OCT OCT},
     * and {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new key which activates in one day and expires in one year. Prints out details of the response
     * returned by the service and the newly created key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestOptions -->
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the key being
     * created. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created key.
     *
     * @throws HttpResponseException If {@code createKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(createKeyOptions, "'createKeyOptions' cannot be null.");

            if (isNullOrEmpty(createKeyOptions.getName())) {
                throw new IllegalArgumentException("'createKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createKeyOptions.getKeyType())
                .setKeyAttributes(createKeyAttributes(createKeyOptions))
                .setKeyOps(createKeyOptions.getKeyOperations())
                .setTags(createKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createKeyOptions.getReleasePolicy()));

            return mapResponse(
                clientImpl.createKeyWithResponse(createKeyOptions.getName(), keyCreateParameters, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new RSA key and stores it in the key vault. The create RSA key operation can be used to create any RSA
     * key type in Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the
     * key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createRsaKeyOptions} parameter and its {@link CreateRsaKeyOptions#getName() name} value are
     * required. The {@link CreateRsaKeyOptions#getKeySize() key size},
     * {@link CreateRsaKeyOptions#getExpiresOn() expires}, and {@link CreateRsaKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateRsaKeyOptions#isEnabled()} enabled} field is set to {@code true} by default
     * if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#RSA RSA} and
     * {@link KeyType#RSA_HSM RSA-HSM}</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out the details of the newly
     * created RSA key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createRsaKey#CreateRsaKeyOptions -->
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the RSA
     * key being created. It is required and cannot be {@code null}.
     * @return The newly created RSA key.
     *
     * @throws HttpResponseException If {@code createRsaKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateRsaKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createRsaKey(CreateRsaKeyOptions createRsaKeyOptions) {
        try {
            Objects.requireNonNull(createRsaKeyOptions, "'createRsaKeyOptions' cannot be null.");

            if (isNullOrEmpty(createRsaKeyOptions.getName())) {
                throw new IllegalArgumentException("'createRsaKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createRsaKeyOptions.getKeyType())
                .setKeySize(createRsaKeyOptions.getKeySize())
                .setPublicExponent(createRsaKeyOptions.getPublicExponent())
                .setKeyOps(createRsaKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createRsaKeyOptions))
                .setTags(createRsaKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createRsaKeyOptions.getReleasePolicy()));

            return createKeyVaultKey(clientImpl.createKey(createRsaKeyOptions.getName(), keyCreateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new RSA key and stores it in the key vault. The create RSA key operation can be used to create any RSA
     * key type in Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the
     * key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createRsaKeyOptions} parameter and its {@link CreateRsaKeyOptions#getName() name} value are
     * required. The {@link CreateRsaKeyOptions#getKeySize() key size},
     * {@link CreateRsaKeyOptions#getExpiresOn() expires}, and {@link CreateRsaKeyOptions#getNotBefore() notBefore}
     * values are optional. The {@link CreateRsaKeyOptions#isEnabled()} enabled} field is set to {@code true} by default
     * if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#RSA RSA} and
     * {@link KeyType#RSA_HSM RSA-HSM}</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new RSA key which activates in one day and expires in one year. Prints out details of the response
     * returned by the service and the newly created RSA key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestOptions -->
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the RSA
     * key being created. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created RSA key.
     *
     * @throws HttpResponseException If {@code createRsaKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateRsaKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(createRsaKeyOptions, "'createRsaKeyOptions' cannot be null.");

            if (isNullOrEmpty(createRsaKeyOptions.getName())) {
                throw new IllegalArgumentException("'createRsaKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createRsaKeyOptions.getKeyType())
                .setKeySize(createRsaKeyOptions.getKeySize())
                .setPublicExponent(createRsaKeyOptions.getPublicExponent())
                .setKeyOps(createRsaKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createRsaKeyOptions))
                .setTags(createRsaKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createRsaKeyOptions.getReleasePolicy()));

            return mapResponse(
                clientImpl.createKeyWithResponse(createRsaKeyOptions.getName(), keyCreateParameters, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new EC key and stores it in the key vault. The create EC key operation can be used to create any EC key
     * type in Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the key
     * is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createEcKeyOptions} parameter and its {@link CreateEcKeyOptions#getName() name} value are required.
     * The {@link CreateEcKeyOptions#getCurveName() key curve} can be optionally specified. If not specified, the
     * default value {@link KeyCurveName#P_256 P-256} is used. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled()} enabled} field is set to {@code true} by default if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC} and
     * {@link KeyType#EC_HSM EC-HSM}</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new EC key with a {@link KeyCurveName#P_384 P-384} web key curve which activates in one day and
     * expires in one year. Prints out the details of the newly created EC key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createEcKey#CreateOctKeyOptions -->
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the EC key
     * being created. It is required and cannot be {@code null}.
     * @return The newly created EC key.
     *
     * @throws HttpResponseException If {@code createEcKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateEcKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createEcKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createEcKey(CreateEcKeyOptions createEcKeyOptions) {
        try {
            Objects.requireNonNull(createEcKeyOptions, "'createEcKeyOptions' cannot be null.");

            if (isNullOrEmpty(createEcKeyOptions.getName())) {
                throw new IllegalArgumentException("'createEcKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createEcKeyOptions.getKeyType())
                .setKeyOps(createEcKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createEcKeyOptions))
                .setTags(createEcKeyOptions.getTags())
                .setCurve(createEcKeyOptions.getCurveName())
                .setReleasePolicy(mapKeyReleasePolicy(createEcKeyOptions.getReleasePolicy()));

            return createKeyVaultKey(clientImpl.createKey(createEcKeyOptions.getName(), keyCreateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new EC key and stores it in the key vault. The create EC key operation can be used to create any EC key
     * type in Azure Key Vault or Managed HSM. If a key with the provided name already exists, a new version of the key
     * is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createEcKeyOptions} parameter and its {@link CreateEcKeyOptions#getName() name} value are required.
     * The {@link CreateEcKeyOptions#getCurveName() key curve} can be optionally specified. If not specified, the
     * default value {@link KeyCurveName#P_256 P-256} is used. The {@link CreateEcKeyOptions#getExpiresOn() expires} and
     * {@link CreateEcKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateEcKeyOptions#isEnabled()} enabled} field is set to {@code true} by default if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#EC EC} and
     * {@link KeyType#EC_HSM EC-HSM}</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new EC key with a {@link KeyCurveName#P_384 P-384} web key curve which activates in one day and
     * expires in one year. Prints out details of the response returned by the service and the newly created EC key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestOptions -->
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the EC key
     * being created. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created EC key.
     *
     * @throws HttpResponseException If {@code createEcKeyOptions} is malformed
     * @throws IllegalArgumentException If {@link CreateEcKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createEcKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(createEcKeyOptions, "'createEcKeyOptions' cannot be null.");

            if (isNullOrEmpty(createEcKeyOptions.getName())) {
                throw new IllegalArgumentException("'createEcKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createEcKeyOptions.getKeyType())
                .setKeyOps(createEcKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createEcKeyOptions))
                .setTags(createEcKeyOptions.getTags())
                .setCurve(createEcKeyOptions.getCurveName())
                .setReleasePolicy(mapKeyReleasePolicy(createEcKeyOptions.getReleasePolicy()));

            return mapResponse(
                clientImpl.createKeyWithResponse(createEcKeyOptions.getName(), keyCreateParameters, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new symmetric key and stores it in the key vault. If a key with the provided name already exists, a new
     * version of the key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createOctKeyOptions} parameter and its {@link CreateOctKeyOptions#getName() name} value are
     * required. The {@link CreateOctKeyOptions#getExpiresOn() expires and
     * {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by default if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#OCT OCT} and
     * {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new symmetric key with a which activates in one day and expires in one year. Prints out the details
     * of the newly created symmetric key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions -->
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * symmetric key being created. It is required and cannot be {@code null}.
     * @return The newly created symmetric key.
     *
     * @throws HttpResponseException If {@code createOctKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateOctKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey createOctKey(CreateOctKeyOptions createOctKeyOptions) {
        try {
            Objects.requireNonNull(createOctKeyOptions, "'createOctKeyOptions' cannot be null.");

            if (isNullOrEmpty(createOctKeyOptions.getName())) {
                throw new IllegalArgumentException("'createOctKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(
                createOctKeyOptions.getKeyType()).setKeySize(createOctKeyOptions.getKeySize())
                .setKeyOps(createOctKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createOctKeyOptions))
                .setTags(createOctKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createOctKeyOptions.getReleasePolicy()));

            return createKeyVaultKey(clientImpl.createKey(createOctKeyOptions.getName(), keyCreateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Creates a new symmetric key and stores it in the key vault. If a key with the provided name already exists, a new
     * version of the key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createOctKeyOptions} parameter and its {@link CreateOctKeyOptions#getName() name} value are
     * required. The {@link CreateOctKeyOptions#getExpiresOn() expires and
     * {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by default if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#OCT OCT} and
     * {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new symmetric key with a which activates in one day and expires in one year. Prints out details of
     * the response returned by the service and the newly created symmetric key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestOptions -->
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * symmetric key being created. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return The newly created symmetric key.
     *
     * @throws HttpResponseException If {@code createOctKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateOctKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(createOctKeyOptions, "'createOctKeyOptions' cannot be null.");

            if (isNullOrEmpty(createOctKeyOptions.getName())) {
                throw new IllegalArgumentException("'createOctKeyOptions.getName()' cannot be null or empty.");
            }

            KeyCreateParameters keyCreateParameters = new KeyCreateParameters(
                createOctKeyOptions.getKeyType()).setKeySize(createOctKeyOptions.getKeySize())
                .setKeyOps(createOctKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createOctKeyOptions))
                .setTags(createOctKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createOctKeyOptions.getReleasePolicy()));

            return mapResponse(
                clientImpl.createKeyWithResponse(createOctKeyOptions.getName(), keyCreateParameters, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Imports an externally created {@link JsonWebKey} and stores it in the key vault. The import key operation may be
     * used to import any key type into Azure Key Vault or Managed HSM. If a key with the provided name already exists,
     * a new version of the key is created. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a key into the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey -->
     *
     * @param name The name for the key to be imported. It is required and cannot be {@code null} or empty.
     * @param keyMaterial The {@link JsonWebKey} being imported. It is required and cannot be {@code null}.
     * @return The imported key as a {@link KeyVaultKey}.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     * @throws NullPointerException If {@code keyMaterial} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(String name, JsonWebKey keyMaterial) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            Objects.requireNonNull(keyMaterial, "'keyMaterial' cannot be null.");

            return createKeyVaultKey(clientImpl.importKey(name, new KeyImportParameters(mapJsonWebKey(keyMaterial))));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Imports an externally created {@link JsonWebKey} and stores it in the key vault. The import key operation may be
     * used to import any key type into Azure Key Vault or Managed HSM. If a key with the provided name already exists,
     * a new version of the key is created. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code importKeyOptions} parameter and its {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} values are required. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. The {@link ImportKeyOptions#isEnabled() enabled} field is set to
     * {@code true} and the {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by default
     * if not specified.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a key into the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.importKey#ImportKeyOptions -->
     *
     * @param importKeyOptions The {@link ImportKeyOptions options object} containing information about the
     * {@link JsonWebKey} being imported. It is required and cannot be {@code null}.
     * @return The imported key as a {@link KeyVaultKey}.
     *
     * @throws HttpResponseException If {@code importKeyOptions} is malformed.
     * @throws IllegalArgumentException If the provided {@link ImportKeyOptions#getName()} is {@code null} or an empty
     * string.
     * @throws NullPointerException If either of the provided {@code importKeyOptions} or
     * {@link ImportKeyOptions#getKey()} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey importKey(ImportKeyOptions importKeyOptions) {
        try {
            Objects.requireNonNull(importKeyOptions, "'importKeyOptions' cannot be null.");
            Objects.requireNonNull(importKeyOptions.getKey(), "'importKeyOptions.getKey()' cannot be null.");

            if (isNullOrEmpty(importKeyOptions.getName())) {
                throw new IllegalArgumentException("'importKeyOptions.getName()' cannot be null or empty.");
            }

            KeyImportParameters keyImportParameters = new KeyImportParameters(mapJsonWebKey(importKeyOptions.getKey()))
                .setHsm(importKeyOptions.isHardwareProtected())
                .setKeyAttributes(createKeyAttributes(importKeyOptions))
                .setTags(importKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(importKeyOptions.getReleasePolicy()));

            return createKeyVaultKey(clientImpl.importKey(importKeyOptions.getName(), keyImportParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Imports an externally created {@link JsonWebKey} and stores it in the key vault. The import key operation may be
     * used to import any key type into Azure Key Vault or Managed HSM. If a key with the provided name already exists,
     * a new version of the key is created. This operation requires the {@code keys/import} permission.
     *
     * <p>The {@code importKeyOptions} parameter and its {@link ImportKeyOptions#getName() name} and
     * {@link ImportKeyOptions#getKey() key material} values are required. The
     * {@link ImportKeyOptions#getExpiresOn() expires} and {@link ImportKeyOptions#getNotBefore() notBefore} values
     * in {@code keyImportOptions} are optional. The {@link ImportKeyOptions#isEnabled() enabled} field is set to
     * {@code true} and the {@link ImportKeyOptions#isHardwareProtected() hsm} field is set to {@code false} by default
     * if not specified.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a key into the key vault. Prints out details of the response returned by the service and the imported
     * key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestOptions -->
     *
     * @param importKeyOptions The options object containing information about the {@link JsonWebKey} being imported. It
     * is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the imported key as a
     * {@link KeyVaultKey}.
     *
     * @throws HttpResponseException If {@code importKeyOptions} is malformed.
     * @throws IllegalArgumentException If the key name provided in {@code importKeyOptions} is {@code null} or an empty
     * string.
     * @throws NullPointerException If either of the provided {@code importKeyOptions} object or the key it contains is
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> importKeyWithResponse(ImportKeyOptions importKeyOptions,
        RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(importKeyOptions, "'importKeyOptions' cannot be null.");
            Objects.requireNonNull(importKeyOptions.getKey(), "'importKeyOptions.getKey()' cannot be null.");

            if (isNullOrEmpty(importKeyOptions.getName())) {
                throw new IllegalArgumentException("'importKeyOptions.getName()' cannot be null or empty.");
            }

            KeyImportParameters keyImportParameters = new KeyImportParameters(mapJsonWebKey(importKeyOptions.getKey()))
                .setHsm(importKeyOptions.isHardwareProtected())
                .setKeyAttributes(createKeyAttributes(importKeyOptions))
                .setTags(importKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(importKeyOptions.getReleasePolicy()));

            return mapResponse(
                clientImpl.importKeyWithResponse(importKeyOptions.getName(), keyImportParameters, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the public part of a given key, as well as the key's properties. The get key operation is applicable to all
     * key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a key in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKey#String -->
     *
     * @param name The name of the key to retrieve. It is required and cannot be {@code null} or empty.
     * @return The requested key.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name) {
        return getKey(name, "");
    }

    /**
     * Gets the public part of a specific version of a given key, as well as the key's properties. The get key operation
     * is applicable to all key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a key in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKey#String-String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKey#String-String -->
     *
     * @param name The name of the key to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the key to retrieve. If this is an empty string or {@code null}, this call is
     * equivalent to calling {@link KeyClient#getKey(String)}, with the latest version being retrieved.
     * @return The requested key.
     *
     * @throws HttpResponseException If a key with the given {@code name} and {@code version} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey(String name, String version) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createKeyVaultKey(clientImpl.getKey(name, version));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the public part of a specific version of a given key, as well as the key's properties. The get key operation
     * is applicable to all key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a key in the key vault. Prints out details of the response returned by the service
     * and the requested key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestOptions -->
     *
     * @param name The name of the key to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the key to retrieve. If this is an empty string or {@code null}, the latest version
     * will be retrieved.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested key.
     *
     * @throws HttpResponseException If a key with the given {@code name} and {@code version} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getKeyWithResponse(name, version, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes and operations associated with the specified key, but not the cryptographic key material
     * in the key vault of a given key. Key attributes that are not specified in the request are left unchanged. This
     * operation requires the {@code keys/set} permission.
     *
     * <p>The {@code keyProperties} parameter and its {@link KeyProperties#getName() name} value are required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a key and updates its expiry time and operations in the key vault, then prints out
     * the updated key's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyProperties#KeyProperties-KeyOperation -->
     *
     * @param keyProperties The key properties to update. It is required and cannot be {@code null}.
     * @param keyOperations The key operations to associate with the key.
     * @return The updated key.
     *
     * @throws HttpResponseException If a key with the given {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link KeyProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code keyProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey updateKeyProperties(KeyProperties keyProperties, List<KeyOperation> keyOperations) {
        try {
            Objects.requireNonNull(keyProperties, "'keyProperties' cannot be null.");

            if (isNullOrEmpty(keyProperties.getName())) {
                throw new IllegalArgumentException("'keyProperties.getName()' cannot be null or empty.");
            }

            KeyUpdateParameters keyUpdateParameters = new KeyUpdateParameters().setKeyOps(keyOperations)
                .setKeyAttributes(createKeyAttributes(keyProperties))
                .setTags(keyProperties.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(keyProperties.getReleasePolicy()));

            return createKeyVaultKey(
                clientImpl.updateKey(keyProperties.getName(), keyProperties.getVersion(), keyUpdateParameters));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the attributes and operations associated with the specified key, but not the cryptographic key material
     * in the key vault of a given key. Key attributes that are not specified in the request are left unchanged. This
     * operation requires the {@code keys/set} permission.
     *
     * <p>The {@code keyProperties} parameter and its {@link KeyProperties#getName() name} value are required.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a key and updates its expiry time and operations, then prints out details of the
     * response returned by the service and the updated key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestOptions-KeyOperation -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestOptions-KeyOperation -->
     *
     * @param keyProperties The key properties to update. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @param keyOperations The key operations to associate with the key.
     * @return A response object whose {@link Response#getValue() value} contains the updated key.
     *
     * @throws HttpResponseException If a key with the given {@link KeyProperties#getName() name} and
     * {@link KeyProperties#getVersion() version} doesn't exist in the key vault.
     * @throws IllegalArgumentException If {@link KeyProperties#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code keyProperties} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> updateKeyPropertiesWithResponse(KeyProperties keyProperties,
        List<KeyOperation> keyOperations, RequestOptions requestOptions) {

        try {
            Objects.requireNonNull(keyProperties, "'keyProperties' cannot be null.");

            if (isNullOrEmpty(keyProperties.getName())) {
                throw new IllegalArgumentException("'keyProperties.getName()' cannot be null or empty.");
            }

            KeyUpdateParameters keyUpdateParameters = new KeyUpdateParameters().setKeyOps(keyOperations)
                .setKeyAttributes(createKeyAttributes(keyProperties))
                .setTags(keyProperties.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(keyProperties.getReleasePolicy()));

            return mapResponse(clientImpl.updateKeyWithResponse(keyProperties.getName(), keyProperties.getVersion(),
                keyUpdateParameters, requestOptions), KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Deletes a key of any type from the key vault. If soft-delete is enabled on the key vault then the key is placed
     * in the deleted state and requires to be purged for permanent deletion. Otherwise, the key is permanently deleted.
     * All versions of a key are deleted. This cannot be applied to individual versions of a key. This operation removes
     * the cryptographic material associated with the key, which means the key is not usable for {@code Sign/Verify},
     * {@code Wrap/Unwrap} or {@code Encrypt/Decrypt} operations. This operation requires the {@code keys/delete}
     * permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Deletes a key from the key vault and prints out its recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String -->
     *
     * @param name The name of the key to be deleted.
     * @return A {@link Poller} to poll on and retrieve the deleted key with.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedKey, Void> beginDeleteKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createDeletedKey(clientImpl.deleteKey(name))),
                pollingContext -> deletePollOperation(name, pollingContext),
                (pollingContext, firstResponse) -> null,
                pollingContext -> null);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<DeletedKey> deletePollOperation(String name, PollingContext<DeletedKey> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedKey(clientImpl.getDeletedKey(name)));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    pollingContext.getLatestResponse().getValue());
            } else {
                // This means either vault has soft-delete disabled or permission is not granted for the get deleted key
                // operation. In both cases deletion operation was successful when activation operation succeeded before
                // reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (RuntimeException e) {
            // This means either vault has soft-delete disabled or permission is not granted for the get deleted key
            // operation. In both cases deletion operation was successful when activation operation succeeded before
            // reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Gets information about a deleted key. This operation is only applicable for soft-delete enabled vaults and
     * requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted key from the key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String -->
     *
     * @param name The name of the deleted key to retrieve. It is required and cannot be {@code null} or empty.
     * @return The requested deleted key.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DeletedKey getDeletedKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createDeletedKey(clientImpl.getDeletedKey(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the public part of a deleted key. The get deleted Key operation is only applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted key from the key vault enabled for soft-delete. Prints details of the response returned by the
     * service and the deleted key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKey#String-RequestOptions -->
     *
     * @param name The name of the deleted key to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the deleted key.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedKey> getDeletedKeyWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getDeletedKeyWithResponse(name, requestOptions),
                KeyVaultKeysModelsUtils::createDeletedKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted key without the possibility of recovery. This operation can only be performed on a
     * key vault <b>enabled for soft-delete</b> and requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted key from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String -->
     *
     * @param name The name of the deleted key to be purged. It is required and cannot be {@code null} or empty.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            clientImpl.purgeDeletedKey(name);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Permanently removes a deleted key without the possibility of recovery. This operation can only be performed on a
     * key vault <b>enabled for soft-delete</b> and requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted key from a key vault enabled for soft-delete and prints out details of the response returned
     * by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestOptions -->
     *
     * @param name The name of the deleted key to be purged. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and HTTP headers related to the operation.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedKeyWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.purgeDeletedKeyWithResponse(name, requestOptions);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Recovers the latest version of a deleted key in the key vault. The recover deleted key operation is only
     * applicable for soft-delete enabled vaults. An attempt to recover an non-deleted key will return an error.
     * Consider this the inverse of the delete operation on soft-delete enabled vaults. This operation requires the
     * {@code keys/recover} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Recovers a deleted key from key vault enabled for soft-delete and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.recoverDeletedKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.recoverDeletedKey#String -->
     *
     * @param name The name of the deleted key to be recovered.
     * @return A {@link Poller} to poll on and retrieve recovered key with.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    // TODO (vcolin7): Uncomment when creating a Poller is supported in azure-core-v2.
    /*@ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return Poller.createPoller(
                Duration.ofSeconds(1),
                pollingContext -> new PollResponse<>(LongRunningOperationStatus.NOT_STARTED,
                    createKeyVaultKey(clientImpl.recoverDeletedKey(name))),
                pollingContext -> recoverPollOperation(name, pollingContext),
                (pollingContext, firstResponse) -> null,
                pollingContext -> null);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }*/

    private PollResponse<KeyVaultKey> recoverPollOperation(String keyName, PollingContext<KeyVaultKey> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createKeyVaultKey(clientImpl.getKey(keyName, "")));
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return new PollResponse<>(LongRunningOperationStatus.IN_PROGRESS,
                    pollingContext.getLatestResponse().getValue());
            } else {
                // This means permission is not granted for the get key operation. In both cases recovery operation
                // was successful when activation operation succeeded before reaching here.
                return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                    pollingContext.getLatestResponse().getValue());
            }
        } catch (RuntimeException e) {
            // This means permission is not granted for the get deleted key operation. In both cases deletion
            // operation was successful when activation operation succeeded before reaching here.
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                pollingContext.getLatestResponse().getValue());
        }
    }

    /**
     * Requests a backup of the specified key   that this operation does not return key material in a form that can be
     * used outside the Azure Key Vault or Managed HSM system, as the returned key material is either protected to an
     * Azure Key Vault HSM or to Azure Key Vault itself. The intent of this operation is to allow a client to generate a
     * key in a key vault instance, backup the key, and then restore it into another key vault instance. The backup
     * operation may be used to export, in protected form, key type in Azure Key Vault or Managed HSM. Individual
     * versions of a key cannot be backed up. {@code Backup/Restore} can be performed within geographical boundaries
     * only; meaning that a backup from one geographical area cannot be restored to another geographical area. For
     * example, a backup from the US geographical area cannot be restored in an EU geographical area. This operation
     * requires the {@code key/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a key from the key vault and prints out the length of the key's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.backupKey#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.backupKey#String -->
     *
     * @param name The name of the key to back up.
     * @return The backed up key blob.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] backupKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.backupKey(name).getValue();
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Requests a backup of the specified key be downloaded to the client. The key backup operation exports a key from
     * the key vault in a protected form. Note that this operation does not return key material in a form that can be
     * used outside the Azure Key Vault or Managed HSM system, as the returned key material is either protected to an
     * Azure Key Vault HSM or to Azure Key Vault itself. The intent of this operation is to allow a client to generate a
     * key in a key vault instance, backup the key, and then restore it into another key vault instance. The backup
     * operation may be used to export, in protected form, key type in Azure Key Vault or Managed HSM. Individual
     * versions of a key cannot be backed up. {@code Backup/Restore} can be performed within geographical boundaries
     * only; meaning that a backup from one geographical area cannot be restored to another geographical area. For
     * example, a backup from the US geographical area cannot be restored in an EU geographical area. This operation
     * requires the {@code key/backup} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Backs up a key from the key vault. Prints out details of the response returned by the service and the length
     * of the key's backup blob.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestOptions -->
     *
     * @param name The name of the key to back up.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the backed up key blob.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupKeyWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.backupKeyWithResponse(name, requestOptions), BackupKeyResult::getValue);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up key to a key vault. Imports a previously backed up key into Azure Key Vault or Managed HSM,
     * restoring the key, its key identifier, attributes and access control policies. The restore operation may be used
     * to import a previously backed up key. Individual versions of a key cannot be restored. The key is restored in its
     * entirety with the same key name as it had when it was backed up. If the key name is not available in the target
     * key vault, the restore operation will be rejected. While the key name is retained during restore, the final key
     * identifier will change if the key is restored to a different vault. Restore will restore all versions and
     * preserve version identifiers. The restore operation is subject to security constraints: The target key vault must
     * be owned by the same Microsoft Azure Subscription as the source key vault. This operation requires the
     * {@code keys/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a key in the key vault from its backup and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackup#byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackup#byte -->
     *
     * @param backup The backup blob associated with the key. It is required and cannot be {@code null}.
     * @return The restored key.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     * @throws NullPointerException If the provided {@code backup} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey restoreKeyBackup(byte[] backup) {
        try {
            Objects.requireNonNull(backup, "'backup' cannot be null.");

            return createKeyVaultKey(clientImpl.restoreKey(new KeyRestoreParameters(backup)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Restores a backed up key to a key vault. Imports a previously backed up key into Azure Key Vault or Managed HSM,
     * restoring the key, its key identifier, attributes and access control policies. The restore operation may be used
     * to import a previously backed up key. Individual versions of a key cannot be restored. The key is restored in its
     * entirety with the same key name as it had when it was backed up. If the key name is not available in the target
     * key vault, the restore operation will be rejected. While the key name is retained during restore, the final key
     * identifier will change if the key is restored to a different vault. Restore will restore all versions and
     * preserve version identifiers. The restore operation is subject to security constraints: The target key vault must
     * be owned by the same Microsoft Azure Subscription as the source key vault. This operation requires the
     * {@code keys/restore} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Restores a key in the key vault from its backup. Prints out details of the response returned by the service
     * and the restored key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestOptions -->
     *
     * @param backup The backup blob associated with the key. It is required and cannot be {@code null}.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the restored key.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     * @throws NullPointerException If the provided {@code backup} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, RequestOptions requestOptions) {
        try {
            Objects.requireNonNull(backup, "'backup' cannot be null.");

            return mapResponse(clientImpl.restoreKeyWithResponse(new KeyRestoreParameters(backup), requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * List all keys in the key vault. Each key is represented by a properties object containing the key identifier,
     * attributes, and tags. The key material and individual key versions are not included in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys</strong></p>
     * <p>Lists the keys in the key vault and gets the key material for each one's latest version by looping though the
     * properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys -->
     *
     * <p><strong>Iterate through keys by page</strong></p>
     * <p>Iterates through the keys in the key vault by page and gets the key material for each one's latest version by
     * looping though the properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the keys in the vault. A properties object contains
     * all the information about the key, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys() {
        return listPropertiesOfKeys(RequestOptions.none());
    }

    /**
     * List all keys in the key vault. Each key is represented by a properties object containing the key identifier,
     * attributes, and tags. The key material and individual key versions are not included in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys</strong></p>
     * <p>Lists the keys in the key vault and gets the key material for each one's latest version by looping though the
     * properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestOptions -->
     *
     * <p><strong>Iterate through keys by page</strong></p>
     * <p>Iterates through the keys in the key vault by page and gets the key material for each one's latest version by
     * looping though the properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestOptions -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the keys in the vault. A properties object contains
     * all the information about the key, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys(RequestOptions requestOptions) {
        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getKeysSinglePage(null, requestOptions),
                (pagingOptions, nextLink) -> clientImpl.getKeysNextSinglePage(nextLink, requestOptionsForNextPage),
                KeyVaultKeysModelsUtils::createKeyProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all deleted keys in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through deleted keys</strong></p>
     * <p>Lists the deleted keys in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys -->
     *
     * <p><strong>Iterate through deleted keys by page</strong></p>
     * <p>Iterates through the deleted keys in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage -->
     *
     * @return A {@link PagedIterable} of the deleted keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys() {
        return listDeletedKeys(RequestOptions.none());
    }

    /**
     * Lists all deleted keys in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through deleted keys</strong></p>
     * <p>Lists the deleted keys in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestOptions -->
     *
     * <p><strong>Iterate through deleted keys by page</strong></p>
     * <p>Iterates through the deleted keys in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestOptions -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of the deleted keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys(RequestOptions requestOptions) {
        try {
            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getDeletedKeysSinglePage(null, requestOptions),
                (pagingOptions, nextLink) -> clientImpl.getDeletedKeysNextSinglePage(nextLink,
                    requestOptionsForNextPage),
                KeyVaultKeysModelsUtils::createDeletedKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Lists all versions of the specified key in the key vault. Each key version is represented by a properties object
     * containing the key identifier, attributes, and tags. The key material and individual key versions are not
     * included in the response. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys versions</strong></p>
     * <p>Lists the versions of a key in the key vault and gets each one's key material by looping though the properties
     * objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String -->
     *
     * <p><strong>Iterate through keys versions by page</strong></p>
     * <p>Iterates through the versions of a key in the key vault by page and gets each one's key material by looping
     * through the properties objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String -->
     *
     * @param name The name of the key. It is required and cannot be {@code null} or empty.
     * @return {@link PagedIterable} of properties objects of all the versions of the specified key in the vault. A
     * properties object contains all the information about the key, except its key material. The {@link PagedIterable}
     * will be empty if no key with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name) {
        return listPropertiesOfKeyVersions(name, RequestOptions.none());
    }

    /**
     * Lists all versions of the specified key in the key vault. Each key version is represented by a properties object
     * containing the key identifier, attributes, and tags. The key material and individual key versions are not
     * included in the response. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys versions</strong></p>
     * <p>Lists the versions of a key in the key vault and gets each one's key material by looping though the properties
     * objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestOptions -->
     *
     * <p><strong>Iterate through keys versions by page</strong></p>
     * <p>Iterates through the versions of a key in the key vault by page and gets each one's key material by looping
     * through the properties objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestOptions -->
     *
     * @param name The name of the key. It is required and cannot be {@code null} or empty.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of properties objects of all the versions of the specified key in the vault. A
     * properties object contains all the information about the key, except its key material. The {@link PagedIterable}
     * will be empty if no key with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            RequestOptions requestOptionsForNextPage = new RequestOptions();

            requestOptionsForNextPage.setContext(requestOptions != null && requestOptions.getContext() != null
                ? requestOptions.getContext()
                : Context.none());

            return mapPages(pagingOptions -> clientImpl.getKeyVersionsSinglePage(name, null, requestOptions),
                (pagingOptions, nextLink) -> clientImpl.getKeyVersionsNextSinglePage(nextLink,
                    requestOptionsForNextPage),
                KeyVaultKeysModelsUtils::createKeyProperties);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a number of bytes containing random values from a managed HSM. Prints out the retrieved bytes as a
     * base64URL-encoded string.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int -->
     *
     * @param count The number of random bytes to request.
     * @return The requested number of bytes containing random values from managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] getRandomBytes(int count) {
        try {
            return clientImpl.getRandomBytesWithResponse(new GetRandomBytesRequest(count), RequestOptions.none())
                .getValue().getValue();
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a number of bytes containing random values from a managed HSM. Prints out details of the response 
     * returned by the service and the retrieved bytes as a base64URL-encoded string.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestOptions -->
     *
     * @param count The requested number of random bytes.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested number of bytes with
     * random values from a managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> getRandomBytesWithResponse(int count, RequestOptions requestOptions) {
        try {
            return mapResponse(clientImpl.getRandomBytesWithResponse(new GetRandomBytesRequest(count), requestOptions),
                RandomBytes::getValue);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Releases the latest version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints out the signed object that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String -->
     *
     * @param name The name of the key to release.
     * @param targetAttestationToken The attestation assertion for the target of the key release.
     * @return A result object containing the released key.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If either of the provided {@code name} or {@code targetAttestationToken} is
     * {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String targetAttestationToken) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.release(name, "", new KeyReleaseParameters(targetAttestationToken));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Releases a specific version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints the signed object that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String-String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String-String -->
     *
     * @param name The name of the key to release.
     * @param version The version of the key to release. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyClient#releaseKey(String, String)}, with the latest key version being released.
     * @param targetAttestationToken The attestation assertion for the target of the key release.
     * @return A result object containing the released key.
     *
     * @throws HttpResponseException If a key with the given {@code name} and {@code version} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If either the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReleaseKeyResult releaseKey(String name, String version, String targetAttestationToken) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return clientImpl.release(name, version, new KeyReleaseParameters(targetAttestationToken));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Releases a specific version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints out details of the response returned by the service, as well as the signed object
     * that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestOptions -->
     *
     * @param name The name of the key to release.
     * @param version The version of the key to release. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyClient#releaseKey(String, String)}, with the latest key version being released.
     * @param targetAttestationToken The attestation assertion for the target of the key release.
     * @param releaseKeyOptions Additional options for releasing a key.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains an object containing the released key.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If either of the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version, String targetAttestationToken,
        ReleaseKeyOptions releaseKeyOptions, RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            KeyReleaseParameters keyReleaseParameters = new KeyReleaseParameters(targetAttestationToken)
                .setEnc(releaseKeyOptions == null ? null : releaseKeyOptions.getAlgorithm())
                .setNonce(releaseKeyOptions == null ? null : releaseKeyOptions.getNonce());

            return clientImpl.releaseWithResponse(name, version, keyReleaseParameters, requestOptions);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Rotates a key. The rotate key operation will do so based on the key's rotation policy. This operation requires
     * the {@code keys/rotate} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Rotates a key and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String -->
     *
     * @param name The name of key to be rotated. The service will generate a new version in the specified key.
     * @return The new version of the rotated key.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey rotateKey(String name) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return createKeyVaultKey(clientImpl.rotateKey(name));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Rotates a key. The rotate key operation will do so based on the key's rotation policy. This operation requires
     * the {@code keys/rotate} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Rotates a key. Prints out the details of the response returned by the service and the rotated key's.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestOptions -->
     *
     * @param name The name of key to be rotated. The service will generate a new version in the specified key.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the new version of the rotated key.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> rotateKeyWithResponse(String name, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(name)) {
                throw new IllegalArgumentException("'name' cannot be null or empty.");
            }

            return mapResponse(clientImpl.rotateKeyWithResponse(name, requestOptions),
                KeyVaultKeysModelsUtils::createKeyVaultKey);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the rotation policy for the specified key. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves the rotation policy of a given key and prints out the policy's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String -->
     *
     * @param keyName The name of the key to get the policy of.
     * @return The key's rotation policy.
     *
     * @throws HttpResponseException If the key for the provided {@code keyName} does not exist.
     * @throws IllegalArgumentException If the provided {@code keyName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyRotationPolicy getKeyRotationPolicy(String keyName) {
        try {
            if (isNullOrEmpty(keyName)) {
                throw new IllegalArgumentException("'keyName' cannot be null or empty.");
            }

            return mapKeyRotationPolicyImpl(clientImpl.getKeyRotationPolicy(keyName));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the rotation policy for the specified key. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves the rotation policy of a given key. Prints out details of the response returned by the service and
     * the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestOptions -->
     *
     * @param keyName The name of the key to get the policy of.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the key's rotation policy.
     *
     * @throws HttpResponseException If the key for the provided {@code keyName} does not exist.
     * @throws IllegalArgumentException If the provided {@code keyName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> getKeyRotationPolicyWithResponse(String keyName, RequestOptions requestOptions) {
        try {
            if (isNullOrEmpty(keyName)) {
                throw new IllegalArgumentException("'keyName' cannot be null or empty.");
            }

            return mapResponse(clientImpl.getKeyRotationPolicyWithResponse(keyName, requestOptions),
                KeyVaultKeysModelsUtils::mapKeyRotationPolicyImpl);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the rotation policy for the specified key. This operation requires the {@code keys/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates the rotation policy of a given key and prints out the policy's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicy -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicy -->
     *
     * @param keyName The name of the key to update the policy of.
     * @param keyRotationPolicy The rotation policy to update.
     * @return The updated key's rotation policy.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyRotationPolicy updateKeyRotationPolicy(String keyName, KeyRotationPolicy keyRotationPolicy) {
        try {
            if (isNullOrEmpty(keyName)) {
                throw new IllegalArgumentException("'keyName' cannot be null or empty.");
            }

            return mapKeyRotationPolicyImpl(
                clientImpl.updateKeyRotationPolicy(keyName, mapKeyRotationPolicy(keyRotationPolicy)));
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Updates the rotation policy for the specified key. This operation requires the {@code keys/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates the rotation policy of a given key. Prints out details of the response returned by the service and the
     * updated policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestOptions -->
     *
     * @param keyName The name of the key to update the policy of.
     * @param keyRotationPolicy The rotation policy to update.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the updated key's rotation policy.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> updateKeyRotationPolicyWithResponse(String keyName,
        KeyRotationPolicy keyRotationPolicy, RequestOptions requestOptions) {

        try {
            if (isNullOrEmpty(keyName)) {
                throw new IllegalArgumentException("'keyName' cannot be null or empty.");
            }

            return mapResponse(
                clientImpl.updateKeyRotationPolicyWithResponse(keyName, mapKeyRotationPolicy(keyRotationPolicy),
                    requestOptions), KeyVaultKeysModelsUtils::mapKeyRotationPolicyImpl);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    private static <T, S> Response<S> mapResponse(Response<T> response, Function<T, S> mapper) {
        if (response == null) {
            return null;
        }

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            mapper.apply(response.getValue()));
    }

    private static <T, S> PagedIterable<S> mapPages(Function<PagingOptions, PagedResponse<T>> firstPageRetriever,
        BiFunction<PagingOptions, String, PagedResponse<T>> nextPageRetriever, Function<T, S> mapper) {

        return new PagedIterable<>(pageSize -> mapPagedResponse(firstPageRetriever.apply(pageSize), mapper),
            (continuationToken, pageSize) -> mapPagedResponse(nextPageRetriever.apply(continuationToken, pageSize),
                mapper));
    }

    private static <T, S> PagedResponse<S> mapPagedResponse(PagedResponse<T> pagedResponse, Function<T, S> mapper) {
        if (pagedResponse == null) {
            return null;
        }

        return new PagedResponse<>(pagedResponse.getRequest(), pagedResponse.getStatusCode(),
            pagedResponse.getHeaders(),
            pagedResponse.getValue()
                .stream()
                .map(mapper)
                .collect(Collectors.toCollection(() -> new ArrayList<>(pagedResponse.getValue().size()))),
            pagedResponse.getContinuationToken(), null, null, null, null);
    }
}
