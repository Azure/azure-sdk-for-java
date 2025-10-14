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
import com.azure.v2.security.keyvault.keys.implementation.models.DeletedKeyBundle;
import com.azure.v2.security.keyvault.keys.implementation.models.GetRandomBytesRequest;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyBundle;
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
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.paging.PagedIterable;
import io.clientcore.core.http.paging.PagedResponse;
import io.clientcore.core.http.paging.PagingOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.time.Duration;
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
 * <pre>
 * KeyClient keyClient = new KeyClientBuilder&#40;&#41;
 *     .endpoint&#40;&quot;&lt;your-key-vault-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultKey key = keyClient.createKey&#40;&quot;keyName&quot;, KeyType.EC&#41;;
 * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * KeyVaultKey keyWithVersionValue = keyClient.getKey&#40;&quot;keyName&quot;&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, keyWithVersionValue.getName&#40;&#41;,
 *     keyWithVersionValue.getId&#40;&#41;&#41;;
 * </pre>
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
 * <pre>
 * Poller&lt;DeletedKey, Void&gt; deleteKeyPoller = keyClient.beginDeleteKey&#40;&quot;keyName&quot;&#41;;
 * PollResponse&lt;DeletedKey&gt; deleteKeyPollResponse = deleteKeyPoller.poll&#40;&#41;;
 *
 * &#47;&#47; Deleted date only works for SoftDelete Enabled Key Vault.
 * DeletedKey deletedKey = deleteKeyPollResponse.getValue&#40;&#41;;
 *
 * System.out.printf&#40;&quot;Key delete date: %s%n&quot;, deletedKey.getDeletedOn&#40;&#41;&#41;;
 * System.out.printf&#40;&quot;Deleted key's recovery id: %s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
 *
 * &#47;&#47; Key is being deleted on the server.
 * deleteKeyPoller.waitForCompletion&#40;&#41;;
 * &#47;&#47; Key is deleted
 * </pre>
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
            throw LOGGER.throwableAtError().log("'keyName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return KeyVaultKeysUtils
            .getCryptographyClientBuilder(keyName, keyVersion, clientImpl.getVaultBaseUrl(),
                clientImpl.getHttpPipeline(), clientImpl.getServiceVersion())
            .buildClient();
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
     * <pre>
     * KeyVaultKey key = keyClient.createKey&#40;&quot;keyName&quot;, KeyType.EC&#41;;
     * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;
     * </pre>
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
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
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
     * <pre>
     * CreateKeyOptions createKeyOptions = new CreateKeyOptions&#40;&quot;keyName&quot;, KeyType.RSA&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * KeyVaultKey optionsKey = keyClient.createKey&#40;createKeyOptions&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, optionsKey.getName&#40;&#41;, optionsKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(createKeyOptions, "'createKeyOptions' cannot be null.");

        if (isNullOrEmpty(createKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createKeyOptions.getKeyType())
            .setKeyAttributes(createKeyAttributes(createKeyOptions))
            .setKeyOps(createKeyOptions.getKeyOperations())
            .setTags(createKeyOptions.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(createKeyOptions.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.createKeyWithResponse(createKeyOptions.getName(),
            keyCreateParameters, RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestContext -->
     * <pre>
     * CreateKeyOptions createKeyOptions = new CreateKeyOptions&#40;&quot;keyName&quot;, KeyType.RSA&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; createKeyResponse = keyClient.createKeyWithResponse&#40;createKeyOptions, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and: id %s%n&quot;, createKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     createKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createKeyWithResponse#CreateKeyOptions-RequestContext -->
     *
     * @param createKeyOptions The {@link CreateKeyOptions options object} containing information about the key being
     * created. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created key.
     *
     * @throws HttpResponseException If {@code createKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createKeyWithResponse(CreateKeyOptions createKeyOptions,
        RequestContext requestContext) {

        Objects.requireNonNull(createKeyOptions, "'createKeyOptions' cannot be null.");

        if (isNullOrEmpty(createKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters = new KeyCreateParameters(createKeyOptions.getKeyType())
            .setKeyAttributes(createKeyAttributes(createKeyOptions))
            .setKeyOps(createKeyOptions.getKeyOperations())
            .setTags(createKeyOptions.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(createKeyOptions.getReleasePolicy()));

        return mapResponse(
            clientImpl.createKeyWithResponse(createKeyOptions.getName(), keyCreateParameters, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
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
     * <pre>
     * CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setKeySize&#40;2048&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * KeyVaultKey rsaKey = keyClient.createRsaKey&#40;createRsaKeyOptions&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, rsaKey.getName&#40;&#41;, rsaKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(createRsaKeyOptions, "'createRsaKeyOptions' cannot be null.");

        if (isNullOrEmpty(createRsaKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createRsaKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createRsaKeyOptions.getKeyType()).setKeySize(createRsaKeyOptions.getKeySize())
                .setPublicExponent(createRsaKeyOptions.getPublicExponent())
                .setKeyOps(createRsaKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createRsaKeyOptions))
                .setTags(createRsaKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createRsaKeyOptions.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.createKeyWithResponse(createRsaKeyOptions.getName(),
            keyCreateParameters, RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestContext -->
     * <pre>
     * CreateRsaKeyOptions createRsaKeyOptions = new CreateRsaKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setKeySize&#40;2048&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; createRsaKeyResponse =
     *     keyClient.createRsaKeyWithResponse&#40;createRsaKeyOptions, reqContext&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and: id %s%n&quot;, createRsaKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     createRsaKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createRsaKeyWithResponse#CreateRsaKeyOptions-RequestContext -->
     *
     * @param createRsaKeyOptions The {@link CreateRsaKeyOptions options object} containing information about the RSA
     * key being created. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created RSA key.
     *
     * @throws HttpResponseException If {@code createRsaKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateRsaKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createRsaKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createRsaKeyWithResponse(CreateRsaKeyOptions createRsaKeyOptions,
        RequestContext requestContext) {
        Objects.requireNonNull(createRsaKeyOptions, "'createRsaKeyOptions' cannot be null.");

        if (isNullOrEmpty(createRsaKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createRsaKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createRsaKeyOptions.getKeyType()).setKeySize(createRsaKeyOptions.getKeySize())
                .setPublicExponent(createRsaKeyOptions.getPublicExponent())
                .setKeyOps(createRsaKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createRsaKeyOptions))
                .setTags(createRsaKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createRsaKeyOptions.getReleasePolicy()));

        return mapResponse(
            clientImpl.createKeyWithResponse(createRsaKeyOptions.getName(), keyCreateParameters, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
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
     * <pre>
     * CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setCurveName&#40;KeyCurveName.P_384&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * KeyVaultKey ecKey = keyClient.createEcKey&#40;createEcKeyOptions&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, ecKey.getName&#40;&#41;, ecKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(createEcKeyOptions, "'createEcKeyOptions' cannot be null.");

        if (isNullOrEmpty(createEcKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createEcKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createEcKeyOptions.getKeyType()).setKeyOps(createEcKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createEcKeyOptions))
                .setTags(createEcKeyOptions.getTags())
                .setCurve(createEcKeyOptions.getCurveName())
                .setReleasePolicy(mapKeyReleasePolicy(createEcKeyOptions.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.createKeyWithResponse(createEcKeyOptions.getName(),
            keyCreateParameters, RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestContext -->
     * <pre>
     * CreateEcKeyOptions createEcKeyOptions = new CreateEcKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setCurveName&#40;KeyCurveName.P_384&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * Response&lt;KeyVaultKey&gt; createEcKeyResponse =
     *     keyClient.createEcKeyWithResponse&#40;createEcKeyOptions, reqContext&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and: id %s%n&quot;, createEcKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     createEcKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createEcKeyWithResponse#CreateEcKeyOptions-RequestContext -->
     *
     * @param createEcKeyOptions The {@link CreateEcKeyOptions options object} containing information about the EC key
     * being created. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the newly created EC key.
     *
     * @throws HttpResponseException If {@code createEcKeyOptions} is malformed
     * @throws IllegalArgumentException If {@link CreateEcKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createEcKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createEcKeyWithResponse(CreateEcKeyOptions createEcKeyOptions,
        RequestContext requestContext) {
        Objects.requireNonNull(createEcKeyOptions, "'createEcKeyOptions' cannot be null.");

        if (isNullOrEmpty(createEcKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createEcKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createEcKeyOptions.getKeyType()).setKeyOps(createEcKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createEcKeyOptions))
                .setTags(createEcKeyOptions.getTags())
                .setCurve(createEcKeyOptions.getCurveName())
                .setReleasePolicy(mapKeyReleasePolicy(createEcKeyOptions.getReleasePolicy()));

        return mapResponse(
            clientImpl.createKeyWithResponse(createEcKeyOptions.getName(), keyCreateParameters, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
    }

    /**
     * Creates a new symmetric key and stores it in the key vault. If a key with the provided name already exists, a new
     * version of the key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createOctKeyOptions} parameter and its {@link CreateOctKeyOptions#getName() name} value are
     * required. The {@link CreateOctKeyOptions#getExpiresOn()} expires and
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
     * <pre>
     * CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * KeyVaultKey octKey = keyClient.createOctKey&#40;createOctKeyOptions&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and id: %s%n&quot;, octKey.getName&#40;&#41;, octKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(createOctKeyOptions, "'createOctKeyOptions' cannot be null.");

        if (isNullOrEmpty(createOctKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createOctKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createOctKeyOptions.getKeyType()).setKeySize(createOctKeyOptions.getKeySize())
                .setKeyOps(createOctKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createOctKeyOptions))
                .setTags(createOctKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createOctKeyOptions.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.createKeyWithResponse(createOctKeyOptions.getName(),
            keyCreateParameters, RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
        }
    }

    /**
     * Creates a new symmetric key and stores it in the key vault. If a key with the provided name already exists, a new
     * version of the key is created. It requires the {@code keys/create} permission.
     *
     * <p>The {@code createOctKeyOptions} parameter and its {@link CreateOctKeyOptions#getName() name} value are
     * required. The {@link CreateOctKeyOptions#getExpiresOn()} expires and
     * {@link CreateOctKeyOptions#getNotBefore() notBefore} values are optional. The
     * {@link CreateOctKeyOptions#isEnabled() enabled} field is set to {@code true} by default if not specified.</p>
     *
     * <p>The {@code keyType} indicates the type of key to create. Possible values include: {@link KeyType#OCT OCT} and
     * {@link KeyType#OCT_HSM OCT-HSM}.</p>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Creates a new symmetric key with a which activates in one day and expires in one year. Prints out details of
     * the response returned by the service and the newly created symmetric key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestContext -->
     * <pre>
     * CreateOctKeyOptions createOctKeyOptions = new CreateOctKeyOptions&#40;&quot;keyName&quot;&#41;
     *     .setNotBefore&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;1&#41;&#41;
     *     .setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusYears&#40;1&#41;&#41;;
     * Response&lt;KeyVaultKey&gt; createOctKeyResponse =
     *     keyClient.createOctKeyWithResponse&#40;createOctKeyOptions, reqContext&#41;;
     *
     * System.out.printf&#40;&quot;Created key with name: %s and: id %s%n&quot;, createOctKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     createOctKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.createOctKey#CreateOctKeyOptions-RequestContext -->
     *
     * @param createOctKeyOptions The {@link CreateOctKeyOptions options object} containing information about the
     * symmetric key being created. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return The newly created symmetric key.
     *
     * @throws HttpResponseException If {@code createOctKeyOptions} is malformed.
     * @throws IllegalArgumentException If {@link CreateOctKeyOptions#getName()} is {@code null} or an empty string.
     * @throws NullPointerException If {@code createOctKeyOptions} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> createOctKeyWithResponse(CreateOctKeyOptions createOctKeyOptions,
        RequestContext requestContext) {

        Objects.requireNonNull(createOctKeyOptions, "'createOctKeyOptions' cannot be null.");

        if (isNullOrEmpty(createOctKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'createOctKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyCreateParameters keyCreateParameters
            = new KeyCreateParameters(createOctKeyOptions.getKeyType()).setKeySize(createOctKeyOptions.getKeySize())
                .setKeyOps(createOctKeyOptions.getKeyOperations())
                .setKeyAttributes(createKeyAttributes(createOctKeyOptions))
                .setTags(createOctKeyOptions.getTags())
                .setReleasePolicy(mapKeyReleasePolicy(createOctKeyOptions.getReleasePolicy()));

        return mapResponse(
            clientImpl.createKeyWithResponse(createOctKeyOptions.getName(), keyCreateParameters, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
    }

    /**
     * Imports an externally created {@link JsonWebKey} and stores it in the key vault. The import key operation may be
     * used to import any key type into Azure Key Vault or Managed HSM. If a key with the provided name already exists,
     * a new version of the key is created. This operation requires the {@code keys/import} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Imports a key into the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.importKey#String-JsonWebKey -->
     * <pre>
     * KeyVaultKey key = keyClient.importKey&#40;&quot;keyName&quot;, jsonWebKeyToImport&#41;;
     *
     * System.out.printf&#40;&quot;Imported key with name: %s and id: %s%n&quot;, key.getName&#40;&#41;, key.getId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        Objects.requireNonNull(keyMaterial, "'keyMaterial' cannot be null.");

        try (Response<KeyBundle> response = clientImpl.importKeyWithResponse(name,
            new KeyImportParameters(mapJsonWebKey(keyMaterial)), RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <pre>
     * ImportKeyOptions options = new ImportKeyOptions&#40;&quot;keyName&quot;, jsonWebKeyToImport&#41;
     *     .setHardwareProtected&#40;false&#41;;
     * KeyVaultKey importedKey = keyClient.importKey&#40;options&#41;;
     *
     * System.out.printf&#40;&quot;Imported key with name: %s and id: %s%n&quot;, importedKey.getName&#40;&#41;,
     *     importedKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(importKeyOptions, "'importKeyOptions' cannot be null.");

        if (isNullOrEmpty(importKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'importKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        Objects.requireNonNull(importKeyOptions.getKey(), "'importKeyOptions.getKey()' cannot be null.");

        KeyImportParameters keyImportParameters = new KeyImportParameters(mapJsonWebKey(importKeyOptions.getKey()))
            .setHsm(importKeyOptions.isHardwareProtected())
            .setKeyAttributes(createKeyAttributes(importKeyOptions))
            .setTags(importKeyOptions.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(importKeyOptions.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.importKeyWithResponse(importKeyOptions.getName(),
            keyImportParameters, RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestContext -->
     * <pre>
     * ImportKeyOptions importKeyOptions = new ImportKeyOptions&#40;&quot;keyName&quot;, jsonWebKeyToImport&#41;
     *     .setHardwareProtected&#40;false&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; response = keyClient.importKeyWithResponse&#40;importKeyOptions, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Imported key with name: %s and id: %s%n&quot;, response.getValue&#40;&#41;.getName&#40;&#41;,
     *     response.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.importKeyWithResponse#ImportKeyOptions-RequestContext -->
     *
     * @param importKeyOptions The options object containing information about the {@link JsonWebKey} being imported. It
     * is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
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
        RequestContext requestContext) {

        Objects.requireNonNull(importKeyOptions, "'importKeyOptions' cannot be null.");

        if (isNullOrEmpty(importKeyOptions.getName())) {
            throw LOGGER.throwableAtError()
                .log("'importKeyOptions.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        Objects.requireNonNull(importKeyOptions.getKey(), "'importKeyOptions.getKey()' cannot be null.");

        KeyImportParameters keyImportParameters = new KeyImportParameters(mapJsonWebKey(importKeyOptions.getKey()))
            .setHsm(importKeyOptions.isHardwareProtected())
            .setKeyAttributes(createKeyAttributes(importKeyOptions))
            .setTags(importKeyOptions.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(importKeyOptions.getReleasePolicy()));

        return mapResponse(
            clientImpl.importKeyWithResponse(importKeyOptions.getName(), keyImportParameters, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
    }

    /**
     * Gets the public part of a given key, as well as the key's properties. The get key operation is applicable to all
     * key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the latest version of a key in the key vault and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKey#String -->
     * <pre>
     * KeyVaultKey keyWithVersionValue = keyClient.getKey&#40;&quot;keyName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, keyWithVersionValue.getName&#40;&#41;,
     *     keyWithVersionValue.getId&#40;&#41;&#41;;
     * </pre>
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
     * <pre>
     * String keyVersion = &quot;&lt;key-version&gt;&quot;;
     * KeyVaultKey keyWithVersion = keyClient.getKey&#40;&quot;keyName&quot;, keyVersion&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, keyWithVersion.getName&#40;&#41;,
     *     keyWithVersion.getId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createKeyVaultKey(clientImpl.getKeyWithResponse(name, version, RequestContext.none()).getValue());
    }

    /**
     * Gets the public part of a specific version of a given key, as well as the key's properties. The get key operation
     * is applicable to all key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a specific version of a key in the key vault. Prints out details of the response returned by the service
     * and the requested key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestContext -->
     * <pre>
     * String keyVersion = &quot;&lt;key-version&gt;&quot;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; getKeyResponse =
     *     keyClient.getKeyWithResponse&#40;&quot;keyName&quot;, keyVersion, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved key with name: %s and: id %s%n&quot;, getKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     getKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKeyWithResponse#String-String-RequestContext -->
     *
     * @param name The name of the key to retrieve. It is required and cannot be {@code null} or empty.
     * @param version The version of the key to retrieve. If this is an empty string or {@code null}, the latest version
     * will be retrieved.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested key.
     *
     * @throws HttpResponseException If a key with the given {@code name} and {@code version} doesn't exist in the key
     * vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(String name, String version, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getKeyWithResponse(name, version, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
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
     * <pre>
     * KeyVaultKey key = keyClient.getKey&#40;&quot;keyName&quot;&#41;;
     *
     * key.getProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     *
     * KeyVaultKey updatedKey = keyClient.updateKeyProperties&#40;key.getProperties&#40;&#41;,
     *     Arrays.asList&#40;KeyOperation.ENCRYPT, KeyOperation.DECRYPT&#41;&#41;;
     *
     * System.out.printf&#40;&quot;Key is updated with name %s and id %s %n&quot;, updatedKey.getName&#40;&#41;, updatedKey.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(keyProperties, "'keyProperties' cannot be null.");

        if (isNullOrEmpty(keyProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'keyProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyUpdateParameters keyUpdateParameters = new KeyUpdateParameters().setKeyOps(keyOperations)
            .setKeyAttributes(createKeyAttributes(keyProperties))
            .setTags(keyProperties.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(keyProperties.getReleasePolicy()));

        try (Response<KeyBundle> response = clientImpl.updateKeyWithResponse(keyProperties.getName(),
            keyUpdateParameters, keyProperties.getVersion(), RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestContext-KeyOperation -->
     * <pre>
     * KeyVaultKey key = keyClient.getKey&#40;&quot;keyName&quot;&#41;;
     *
     * key.getProperties&#40;&#41;.setExpiresOn&#40;OffsetDateTime.now&#40;&#41;.plusDays&#40;60&#41;&#41;;
     *
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; updateKeyResponse = keyClient.updateKeyPropertiesWithResponse&#40;key.getProperties&#40;&#41;,
     *     Arrays.asList&#40;KeyOperation.ENCRYPT, KeyOperation.DECRYPT&#41;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Updated key with name: %s and id: %s%n&quot;, updateKeyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     updateKeyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyPropertiesWithResponse#KeyProperties-RequestContext-KeyOperation -->
     *
     * @param keyProperties The key properties to update. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
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
        List<KeyOperation> keyOperations, RequestContext requestContext) {
        Objects.requireNonNull(keyProperties, "'keyProperties' cannot be null.");

        if (isNullOrEmpty(keyProperties.getName())) {
            throw LOGGER.throwableAtError()
                .log("'keyProperties.getName()' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyUpdateParameters keyUpdateParameters = new KeyUpdateParameters().setKeyOps(keyOperations)
            .setKeyAttributes(createKeyAttributes(keyProperties))
            .setTags(keyProperties.getTags())
            .setReleasePolicy(mapKeyReleasePolicy(keyProperties.getReleasePolicy()));

        return mapResponse(clientImpl.updateKeyWithResponse(keyProperties.getName(), keyUpdateParameters,
            keyProperties.getVersion(), requestContext), KeyVaultKeysModelsUtils::createKeyVaultKey);
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
     * <pre>
     * Poller&lt;DeletedKey, Void&gt; deleteKeyPoller = keyClient.beginDeleteKey&#40;&quot;keyName&quot;&#41;;
     * PollResponse&lt;DeletedKey&gt; deleteKeyPollResponse = deleteKeyPoller.poll&#40;&#41;;
     *
     * &#47;&#47; Deleted date only works for SoftDelete Enabled Key Vault.
     * DeletedKey deletedKey = deleteKeyPollResponse.getValue&#40;&#41;;
     *
     * System.out.printf&#40;&quot;Key delete date: %s%n&quot;, deletedKey.getDeletedOn&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Deleted key's recovery id: %s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
     *
     * &#47;&#47; Key is being deleted on the server.
     * deleteKeyPoller.waitForCompletion&#40;&#41;;
     * &#47;&#47; Key is deleted
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.deleteKey#String -->
     *
     * @param name The name of the key to be deleted.
     * @return A {@link Poller} to poll on and retrieve the deleted key with.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<DeletedKey, Void> beginDeleteKey(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1), pollingContext -> deleteKeyActivationOperation(name),
            pollingContext -> deleteKeyPollOperation(name, pollingContext), (pollingContext, firstResponse) -> null,
            pollingContext -> null);
    }

    private PollResponse<DeletedKey> deleteKeyActivationOperation(String name) {
        try (Response<DeletedKeyBundle> response = clientImpl.deleteKeyWithResponse(name, RequestContext.none())) {
            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, createDeletedKey(response.getValue()));
        }
    }

    private PollResponse<DeletedKey> deleteKeyPollOperation(String name, PollingContext<DeletedKey> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createDeletedKey(clientImpl.getDeletedKeyWithResponse(name, RequestContext.none()).getValue()));
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
     * <pre>
     * DeletedKey deletedKey = keyClient.getDeletedKey&#40;&quot;keyName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Deleted key's recovery id: %s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return createDeletedKey(clientImpl.getDeletedKeyWithResponse(name, RequestContext.none()).getValue());
    }

    /**
     * Gets the public part of a deleted key. The get deleted Key operation is only applicable for soft-delete enabled
     * vaults. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a deleted key from the key vault enabled for soft-delete. Prints details of the response returned by the
     * service and the deleted key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;DeletedKey&gt; deletedKeyResponse = keyClient.getDeletedKeyWithResponse&#40;&quot;keyName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Deleted key with recovery id: %s%n&quot;, deletedKeyResponse.getValue&#40;&#41;.getRecoveryId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getDeletedKeyWithResponse#String-RequestContext -->
     *
     * @param name The name of the deleted key to retrieve. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the deleted key.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DeletedKey> getDeletedKeyWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getDeletedKeyWithResponse(name, requestContext),
            KeyVaultKeysModelsUtils::createDeletedKey);
    }

    /**
     * Permanently removes a deleted key without the possibility of recovery. This operation can only be performed on a
     * key vault <b>enabled for soft-delete</b> and requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted key from a key vault enabled for soft-delete.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String -->
     * <pre>
     * keyClient.purgeDeletedKey&#40;&quot;deletedKeyName&quot;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKey#String -->
     *
     * @param name The name of the deleted key to be purged. It is required and cannot be {@code null} or empty.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void purgeDeletedKey(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        clientImpl.purgeDeletedKeyWithResponse(name, RequestContext.none());
    }

    /**
     * Permanently removes a deleted key without the possibility of recovery. This operation can only be performed on a
     * key vault <b>enabled for soft-delete</b> and requires the {@code keys/purge} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Purges a deleted key from a key vault enabled for soft-delete and prints out details of the response returned
     * by the service.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;Void&gt; purgeDeletedKeyResponse =
     *     keyClient.purgeDeletedKeyWithResponse&#40;&quot;deletedKeyName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Purge response status code: %d%n&quot;, purgeDeletedKeyResponse.getStatusCode&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.purgeDeletedKeyWithResponse#String-RequestContext -->
     *
     * @param name The name of the deleted key to be purged. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object containing the status code and HTTP headers related to the operation.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> purgeDeletedKeyWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return clientImpl.purgeDeletedKeyWithResponse(name, requestContext);
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
     * <pre>
     * Poller&lt;KeyVaultKey, Void&gt; recoverKeyPoller = keyClient.beginRecoverDeletedKey&#40;&quot;deletedKeyName&quot;&#41;;
     *
     * PollResponse&lt;KeyVaultKey&gt; recoverKeyPollResponse = recoverKeyPoller.poll&#40;&#41;;
     *
     * KeyVaultKey recoveredKey = recoverKeyPollResponse.getValue&#40;&#41;;
     * System.out.printf&#40;&quot;Recovered key name: %s%n&quot;, recoveredKey.getName&#40;&#41;&#41;;
     * System.out.printf&#40;&quot;Recovered key id: %s%n&quot;, recoveredKey.getId&#40;&#41;&#41;;
     *
     * &#47;&#47; Key is being recovered on the server.
     * recoverKeyPoller.waitForCompletion&#40;&#41;;
     * &#47;&#47; Key is recovered
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.recoverDeletedKey#String -->
     *
     * @param name The name of the deleted key to be recovered.
     * @return A {@link Poller} to poll on and retrieve recovered key with.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public Poller<KeyVaultKey, Void> beginRecoverDeletedKey(String name) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return Poller.createPoller(Duration.ofSeconds(1), pollingContext -> recoverKeyPollOperation(name),
            pollingContext -> recoverKeyPollOperation(name, pollingContext), (pollingContext, firstResponse) -> null,
            pollingContext -> null);
    }

    private PollResponse<KeyVaultKey> recoverKeyPollOperation(String name) {
        try (Response<KeyBundle> response = clientImpl.recoverDeletedKeyWithResponse(name, RequestContext.none())) {
            return new PollResponse<>(LongRunningOperationStatus.NOT_STARTED, createKeyVaultKey(response.getValue()));
        }
    }

    private PollResponse<KeyVaultKey> recoverKeyPollOperation(String keyName,
        PollingContext<KeyVaultKey> pollingContext) {
        try {
            return new PollResponse<>(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                createKeyVaultKey(clientImpl.getKeyWithResponse(keyName, "", RequestContext.none()).getValue()));
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
     * <pre>
     * byte[] keyBackup = keyClient.backupKey&#40;&quot;keyName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Key backup byte array length: %s%n&quot;, keyBackup.length&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<BackupKeyResult> response = clientImpl.backupKeyWithResponse(name, RequestContext.none())) {
            return response.getValue().getValue();
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;byte[]&gt; backupKeyResponse = keyClient.backupKeyWithResponse&#40;&quot;keyName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Key backup byte array length: %s%n&quot;, backupKeyResponse.getValue&#40;&#41;.length&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.backupKeyWithResponse#String-RequestContext -->
     *
     * @param name The name of the key to back up.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the backed up key blob.
     *
     * @throws HttpResponseException If a key with the given {@code name} doesn't exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> backupKeyWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.backupKeyWithResponse(name, requestContext), BackupKeyResult::getValue);
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
     * <pre>
     * byte[] keyBackupByteArray = &#123;&#125;;
     * KeyVaultKey keyResponse = keyClient.restoreKeyBackup&#40;keyBackupByteArray&#41;;
     * System.out.printf&#40;&quot;Restored key with name: %s and: id %s%n&quot;, keyResponse.getName&#40;&#41;, keyResponse.getId&#40;&#41;&#41;;
     * </pre>
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
        Objects.requireNonNull(backup, "'backup' cannot be null.");

        try (Response<KeyBundle> response
            = clientImpl.restoreKeyWithResponse(new KeyRestoreParameters(backup), RequestContext.none())) {

            return createKeyVaultKey(response.getValue());
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
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyVaultKey&gt; keyResponse = keyClient.restoreKeyBackupWithResponse&#40;keyBackupByteArray, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Restored key with name: %s and: id %s%n&quot;,
     *     keyResponse.getValue&#40;&#41;.getName&#40;&#41;, keyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.restoreKeyBackupWithResponse#byte-RequestContext -->
     *
     * @param backup The backup blob associated with the key. It is required and cannot be {@code null}.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the restored key.
     *
     * @throws HttpResponseException If the {@code backup} blob is malformed.
     * @throws NullPointerException If the provided {@code backup} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> restoreKeyBackupWithResponse(byte[] backup, RequestContext requestContext) {
        Objects.requireNonNull(backup, "'backup' cannot be null.");

        return mapResponse(clientImpl.restoreKeyWithResponse(new KeyRestoreParameters(backup), requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
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
     * <pre>
     * keyClient.listPropertiesOfKeys&#40;&#41;.forEach&#40;keyProperties -&gt; &#123;
     *     KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved key with name: %s and type: %s%n&quot;, key.getName&#40;&#41;, key.getKeyType&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys -->
     *
     * <p><strong>Iterate through keys by page</strong></p>
     * <p>Iterates through the keys in the key vault by page and gets the key material for each one's latest version by
     * looping though the properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage -->
     * <pre>
     * keyClient.listPropertiesOfKeys&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;keyProperties -&gt; &#123;
     *         KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Retrieved key with name: %s and type: %s%n&quot;, key.getName&#40;&#41;, key.getKeyType&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage -->
     *
     * @return A {@link PagedIterable} of properties objects of all the keys in the vault. A properties object contains
     * all the information about the key, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys() {
        return listPropertiesOfKeys(RequestContext.none());
    }

    /**
     * List all keys in the key vault. Each key is represented by a properties object containing the key identifier,
     * attributes, and tags. The key material and individual key versions are not included in the response. This
     * operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys</strong></p>
     * <p>Lists the keys in the key vault and gets the key material for each one's latest version by looping though the
     * properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyClient.listPropertiesOfKeys&#40;requestContext&#41;.forEach&#40;keyProperties -&gt; &#123;
     *     KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved key with name: %s and type: %s%n&quot;, key.getName&#40;&#41;,
     *         key.getKeyType&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys#RequestContext -->
     *
     * <p><strong>Iterate through keys by page</strong></p>
     * <p>Iterates through the keys in the key vault by page and gets the key material for each one's latest version by
     * looping though the properties objects and calling {@link KeyClient#getKey(String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyClient.listPropertiesOfKeys&#40;reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;keyProperties -&gt; &#123;
     *         KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *         System.out.printf&#40;&quot;Retrieved key with name: %s and type: %s%n&quot;, key.getName&#40;&#41;,
     *             key.getKeyType&#40;&#41;&#41;;
     *     &#125;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeys.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of properties objects of all the keys in the vault. A properties object contains
     * all the information about the key, except its key material.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeys(RequestContext requestContext) {
        return mapPages(pagingOptions -> clientImpl.getKeysSinglePage(null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getKeysNextSinglePage(nextLink, requestContext),
            KeyVaultKeysModelsUtils::createKeyProperties);
    }

    /**
     * Lists all deleted keys in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through deleted keys</strong></p>
     * <p>Lists the deleted keys in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys -->
     * <pre>
     * keyClient.listDeletedKeys&#40;&#41;.forEach&#40;deletedKey -&gt; &#123;
     *     System.out.printf&#40;&quot;Deleted key's recovery id:%s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys -->
     *
     * <p><strong>Iterate through deleted keys by page</strong></p>
     * <p>Iterates through the deleted keys in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage -->
     * <pre>
     * keyClient.listDeletedKeys&#40;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;deletedKey -&gt;
     *         System.out.printf&#40;&quot;Deleted key's recovery id:%s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage -->
     *
     * @return A {@link PagedIterable} of the deleted keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys() {
        return listDeletedKeys(RequestContext.none());
    }

    /**
     * Lists all deleted keys in the key vault currently available for recovery. This operation is applicable for key
     * vaults <b>enabled for soft-delete</b> and requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through deleted keys</strong></p>
     * <p>Lists the deleted keys in a key vault enabled for soft-delete and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyClient.listDeletedKeys&#40;requestContext&#41;.forEach&#40;deletedKey -&gt; &#123;
     *     System.out.printf&#40;&quot;Deleted key's recovery id:%s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys#RequestContext -->
     *
     * <p><strong>Iterate through deleted keys by page</strong></p>
     * <p>Iterates through the deleted keys in the key vault by page and prints out each one's recovery id.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyClient.listDeletedKeys&#40;reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;deletedKey -&gt;
     *         System.out.printf&#40;&quot;Deleted key's recovery id:%s%n&quot;, deletedKey.getRecoveryId&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listDeletedKeys.iterableByPage#RequestContext -->
     *
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of the deleted keys in the vault.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<DeletedKey> listDeletedKeys(RequestContext requestContext) {
        return mapPages(pagingOptions -> clientImpl.getDeletedKeysSinglePage(null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getDeletedKeysNextSinglePage(nextLink, requestContext),
            KeyVaultKeysModelsUtils::createDeletedKey);
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
     * <pre>
     * keyClient.listPropertiesOfKeyVersions&#40;&quot;keyName&quot;&#41;.forEach&#40;keyProperties -&gt; &#123;
     *     KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved key version: %s with name: %s and type: %s%n&quot;,
     *         key.getProperties&#40;&#41;.getVersion&#40;&#41;, key.getName&#40;&#41;, key.getKeyType&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String -->
     *
     * <p><strong>Iterate through keys versions by page</strong></p>
     * <p>Iterates through the versions of a key in the key vault by page and gets each one's key material by looping
     * through the properties objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String -->
     * <pre>
     * keyClient.listPropertiesOfKeyVersions&#40;&quot;keyName&quot;&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;keyProperties -&gt;
     *         System.out.printf&#40;&quot;Key name: %s. Key version: %s.%n&quot;, keyProperties.getName&#40;&#41;,
     *             keyProperties.getVersion&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
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
        return listPropertiesOfKeyVersions(name, RequestContext.none());
    }

    /**
     * Lists all versions of the specified key in the key vault. Each key version is represented by a properties object
     * containing the key identifier, attributes, and tags. The key material and individual key versions are not
     * included in the response. This operation requires the {@code keys/list} permission.
     *
     * <p><strong>Iterate through keys versions</strong></p>
     * <p>Lists the versions of a key in the key vault and gets each one's key material by looping though the properties
     * objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * for &#40;KeyProperties keyProperties : keyClient.listPropertiesOfKeyVersions&#40;&quot;keyName&quot;, requestContext&#41;&#41; &#123;
     *     KeyVaultKey key = keyClient.getKey&#40;keyProperties.getName&#40;&#41;, keyProperties.getVersion&#40;&#41;&#41;;
     *
     *     System.out.printf&#40;&quot;Retrieved key version: %s with name: %s and type: %s%n&quot;,
     *         key.getProperties&#40;&#41;.getVersion&#40;&#41;, key.getName&#40;&#41;, key.getKeyType&#40;&#41;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions#String-RequestContext -->
     *
     * <p><strong>Iterate through keys versions by page</strong></p>
     * <p>Iterates through the versions of a key in the key vault by page and gets each one's key material by looping
     * through the properties objects and calling {@link KeyClient#getKey(String, String)}.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestContext -->
     * <pre>
     * RequestContext reqContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * keyClient.listPropertiesOfKeyVersions&#40;&quot;keyName&quot;, reqContext&#41;.iterableByPage&#40;&#41;.forEach&#40;pagedResponse -&gt; &#123;
     *     System.out.printf&#40;&quot;Got response details. Url: %s. Status code: %d.%n&quot;,
     *         pagedResponse.getRequest&#40;&#41;.getUri&#40;&#41;, pagedResponse.getStatusCode&#40;&#41;&#41;;
     *
     *     pagedResponse.getValue&#40;&#41;.forEach&#40;keyProperties -&gt;
     *         System.out.printf&#40;&quot;Key name: %s. Key version: %s.%n&quot;, keyProperties.getName&#40;&#41;,
     *             keyProperties.getVersion&#40;&#41;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.listPropertiesOfKeyVersions.iterableByPage#String-RequestContext -->
     *
     * @param name The name of the key. It is required and cannot be {@code null} or empty.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return {@link PagedIterable} of properties objects of all the versions of the specified key in the vault. A
     * properties object contains all the information about the key, except its key material. The {@link PagedIterable}
     * will be empty if no key with the given {@code name} exists in key vault.
     *
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or an empty string.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyProperties> listPropertiesOfKeyVersions(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapPages(pagingOptions -> clientImpl.getKeyVersionsSinglePage(name, null, requestContext),
            (pagingOptions, nextLink) -> clientImpl.getKeyVersionsNextSinglePage(nextLink, requestContext),
            KeyVaultKeysModelsUtils::createKeyProperties);
    }

    /**
     * Gets the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a number of bytes containing random values from a managed HSM. Prints out the retrieved bytes as a
     * base64URL-encoded string.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int -->
     * <pre>
     * int amount = 16;
     * byte[] randomBytes = keyClient.getRandomBytes&#40;amount&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved %d random bytes: %s%n&quot;, amount, Arrays.toString&#40;randomBytes&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytes#int -->
     *
     * @param count The number of random bytes to request.
     * @return The requested number of bytes containing random values from managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public byte[] getRandomBytes(int count) {
        return clientImpl.getRandomBytesWithResponse(new GetRandomBytesRequest(count), RequestContext.none())
            .getValue()
            .getValue();
    }

    /**
     * Gets the requested number of bytes containing random values from a managed HSM.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets a number of bytes containing random values from a managed HSM. Prints out details of the response
     * returned by the service and the retrieved bytes as a base64URL-encoded string.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestContext -->
     * <pre>
     * int amountOfBytes = 16;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;byte[]&gt; response = keyClient.getRandomBytesWithResponse&#40;amountOfBytes, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response received successfully with status code: %d. Retrieved %d random bytes: %s%n&quot;,
     *     response.getStatusCode&#40;&#41;, amountOfBytes, Arrays.toString&#40;response.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getRandomBytesWithResponse#int-RequestContext -->
     *
     * @param count The requested number of random bytes.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the requested number of bytes with
     * random values from a managed HSM.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<byte[]> getRandomBytesWithResponse(int count, RequestContext requestContext) {
        return mapResponse(clientImpl.getRandomBytesWithResponse(new GetRandomBytesRequest(count), requestContext),
            RandomBytes::getValue);
    }

    /**
     * Releases the latest version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints out the signed object that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String -->
     * <pre>
     * String targetAttestationToken = &quot;someAttestationToken&quot;;
     * ReleaseKeyResult releaseKeyResult = keyClient.releaseKey&#40;&quot;keyName&quot;, targetAttestationToken&#41;;
     *
     * System.out.printf&#40;&quot;Signed object containing released key: %s%n&quot;, releaseKeyResult&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<ReleaseKeyResult> response = clientImpl.releaseWithResponse(name,
            new KeyReleaseParameters(targetAttestationToken), "", RequestContext.none())) {

            return response.getValue();
        }
    }

    /**
     * Releases a specific version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints the signed object that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKey#String-String-String -->
     * <pre>
     * String myKeyVersion = &quot;&lt;key-version&gt;&quot;;
     * String myTargetAttestationToken = &quot;someAttestationToken&quot;;
     * ReleaseKeyResult releaseKeyVersionResult =
     *     keyClient.releaseKey&#40;&quot;keyName&quot;, myKeyVersion, myTargetAttestationToken&#41;;
     *
     * System.out.printf&#40;&quot;Signed object containing released key: %s%n&quot;, releaseKeyVersionResult&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<ReleaseKeyResult> response = clientImpl.releaseWithResponse(name,
            new KeyReleaseParameters(targetAttestationToken), version, RequestContext.none())) {

            return response.getValue();
        }
    }

    /**
     * Releases a specific version of a key. The key must be exportable. This operation requires the
     * {@code keys/release} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Releases a key and prints out details of the response returned by the service, as well as the signed object
     * that contains the released key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestContext -->
     * <pre>
     * String releaseKeyVersion = &quot;&lt;key-version&gt;&quot;;
     * String someTargetAttestationToken = &quot;someAttestationToken&quot;;
     * ReleaseKeyOptions releaseKeyOptions = new ReleaseKeyOptions&#40;&#41;
     *     .setAlgorithm&#40;KeyExportEncryptionAlgorithm.RSA_AES_KEY_WRAP_256&#41;
     *     .setNonce&#40;&quot;someNonce&quot;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;ReleaseKeyResult&gt; releaseKeyResultResponse = keyClient.releaseKeyWithResponse&#40;&quot;keyName&quot;,
     *     releaseKeyVersion, someTargetAttestationToken, releaseKeyOptions, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response received successfully with status code: %d. Signed object containing&quot;
     *         + &quot;released key: %s%n&quot;, releaseKeyResultResponse.getStatusCode&#40;&#41;,
     *     releaseKeyResultResponse.getValue&#40;&#41;.getValue&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.releaseKeyWithResponse#String-String-String-ReleaseKeyOptions-RequestContext -->
     *
     * @param name The name of the key to release.
     * @param version The version of the key to release. If this is empty or {@code null}, this call is equivalent to
     * calling {@link KeyClient#releaseKey(String, String)}, with the latest key version being released.
     * @param targetAttestationToken The attestation assertion for the target of the key release.
     * @param releaseKeyOptions Additional options for releasing a key.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains an object containing the released key.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If either of the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReleaseKeyResult> releaseKeyWithResponse(String name, String version, String targetAttestationToken,
        ReleaseKeyOptions releaseKeyOptions, RequestContext requestContext) {

        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        KeyReleaseParameters keyReleaseParameters = new KeyReleaseParameters(targetAttestationToken)
            .setEnc(releaseKeyOptions == null ? null : releaseKeyOptions.getAlgorithm())
            .setNonce(releaseKeyOptions == null ? null : releaseKeyOptions.getNonce());

        return clientImpl.releaseWithResponse(name, keyReleaseParameters, version, requestContext);
    }

    /**
     * Rotates a key. The rotate key operation will do so based on the key's rotation policy. This operation requires
     * the {@code keys/rotate} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Rotates a key and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String -->
     * <pre>
     * KeyVaultKey key = keyClient.rotateKey&#40;&quot;keyName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Rotated key with name: %s and version:%s%n&quot;, key.getName&#40;&#41;,
     *     key.getProperties&#40;&#41;.getVersion&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<KeyBundle> response = clientImpl.rotateKeyWithResponse(name, RequestContext.none())) {
            return createKeyVaultKey(response.getValue());
        }
    }

    /**
     * Rotates a key. The rotate key operation will do so based on the key's rotation policy. This operation requires
     * the {@code keys/rotate} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Rotates a key. Prints out the details of the response returned by the service and the rotated key's.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     * Response&lt;KeyVaultKey&gt; keyResponse = keyClient.rotateKeyWithResponse&#40;&quot;keyName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response received successfully with status code: %d. Rotated key with name: %s and&quot;
     *         + &quot;version: %s%n&quot;, keyResponse.getStatusCode&#40;&#41;, keyResponse.getValue&#40;&#41;.getName&#40;&#41;,
     *     keyResponse.getValue&#40;&#41;.getProperties&#40;&#41;.getVersion&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.rotateKeyWithResponse#String-RequestContext -->
     *
     * @param name The name of key to be rotated. The service will generate a new version in the specified key.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the new version of the rotated key.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> rotateKeyWithResponse(String name, RequestContext requestContext) {
        if (isNullOrEmpty(name)) {
            throw LOGGER.throwableAtError().log("'name' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.rotateKeyWithResponse(name, requestContext),
            KeyVaultKeysModelsUtils::createKeyVaultKey);
    }

    /**
     * Gets the rotation policy for the specified key. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves the rotation policy of a given key and prints out the policy's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicy#String -->
     * <pre>
     * KeyRotationPolicy keyRotationPolicy = keyClient.getKeyRotationPolicy&#40;&quot;keyName&quot;&#41;;
     *
     * System.out.printf&#40;&quot;Retrieved key rotation policy with id: %s%n&quot;, keyRotationPolicy.getId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(keyName)) {
            throw LOGGER.throwableAtError().log("'keyName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapKeyRotationPolicyImpl(
            clientImpl.getKeyRotationPolicyWithResponse(keyName, RequestContext.none()).getValue());
    }

    /**
     * Gets the rotation policy for the specified key. This operation requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Retrieves the rotation policy of a given key. Prints out details of the response returned by the service and
     * the policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestContext -->
     * <pre>
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyRotationPolicy&gt; keyRotationPolicyResponse =
     *     keyClient.getKeyRotationPolicyWithResponse&#40;&quot;keyName&quot;, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response received successfully with status code: %d. Retrieved key rotation policy&quot;
     *     + &quot;with id: %s%n&quot;, keyRotationPolicyResponse.getStatusCode&#40;&#41;, keyRotationPolicyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.getKeyRotationPolicyWithResponse#String-RequestContext -->
     *
     * @param keyName The name of the key to get the policy of.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the key's rotation policy.
     *
     * @throws HttpResponseException If the key for the provided {@code keyName} does not exist.
     * @throws IllegalArgumentException If the provided {@code keyName} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> getKeyRotationPolicyWithResponse(String keyName, RequestContext requestContext) {
        if (isNullOrEmpty(keyName)) {
            throw LOGGER.throwableAtError().log("'keyName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.getKeyRotationPolicyWithResponse(keyName, requestContext),
            KeyVaultKeysModelsUtils::mapKeyRotationPolicyImpl);
    }

    /**
     * Updates the rotation policy for the specified key. This operation requires the {@code keys/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates the rotation policy of a given key and prints out the policy's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicy#String-KeyRotationPolicy -->
     * <pre>
     * List&lt;KeyRotationLifetimeAction&gt; lifetimeActions = new ArrayList&lt;&gt;&#40;&#41;;
     * KeyRotationLifetimeAction rotateLifetimeAction = new KeyRotationLifetimeAction&#40;KeyRotationPolicyAction.ROTATE&#41;
     *     .setTimeAfterCreate&#40;&quot;P90D&quot;&#41;;
     * KeyRotationLifetimeAction notifyLifetimeAction = new KeyRotationLifetimeAction&#40;KeyRotationPolicyAction.NOTIFY&#41;
     *     .setTimeBeforeExpiry&#40;&quot;P45D&quot;&#41;;
     *
     * lifetimeActions.add&#40;rotateLifetimeAction&#41;;
     * lifetimeActions.add&#40;notifyLifetimeAction&#41;;
     *
     * KeyRotationPolicy keyRotationPolicy = new KeyRotationPolicy&#40;&#41;
     *     .setLifetimeActions&#40;lifetimeActions&#41;
     *     .setExpiresIn&#40;&quot;P6M&quot;&#41;;
     *
     * KeyRotationPolicy updatedPolicy =
     *     keyClient.updateKeyRotationPolicy&#40;&quot;keyName&quot;, keyRotationPolicy&#41;;
     *
     * System.out.printf&#40;&quot;Updated key rotation policy with id: %s%n&quot;, updatedPolicy.getId&#40;&#41;&#41;;
     * </pre>
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
        if (isNullOrEmpty(keyName)) {
            throw LOGGER.throwableAtError().log("'keyName' cannot be null or empty.", IllegalArgumentException::new);
        }

        try (Response<com.azure.v2.security.keyvault.keys.implementation.models.KeyRotationPolicy> response
            = clientImpl.updateKeyRotationPolicyWithResponse(keyName, mapKeyRotationPolicy(keyRotationPolicy),
                RequestContext.none())) {

            return mapKeyRotationPolicyImpl(response.getValue());
        }
    }

    /**
     * Updates the rotation policy for the specified key. This operation requires the {@code keys/update} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Updates the rotation policy of a given key. Prints out details of the response returned by the service and the
     * updated policy.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestContext -->
     * <pre>
     * List&lt;KeyRotationLifetimeAction&gt; myLifetimeActions = new ArrayList&lt;&gt;&#40;&#41;;
     * KeyRotationLifetimeAction myRotateLifetimeAction = new KeyRotationLifetimeAction&#40;KeyRotationPolicyAction.ROTATE&#41;
     *     .setTimeAfterCreate&#40;&quot;P90D&quot;&#41;;
     * KeyRotationLifetimeAction myNotifyLifetimeAction = new KeyRotationLifetimeAction&#40;KeyRotationPolicyAction.NOTIFY&#41;
     *     .setTimeBeforeExpiry&#40;&quot;P45D&quot;&#41;;
     *
     * myLifetimeActions.add&#40;myRotateLifetimeAction&#41;;
     * myLifetimeActions.add&#40;myNotifyLifetimeAction&#41;;
     *
     * KeyRotationPolicy myKeyRotationPolicy = new KeyRotationPolicy&#40;&#41;
     *     .setLifetimeActions&#40;myLifetimeActions&#41;
     *     .setExpiresIn&#40;&quot;P6M&quot;&#41;;
     * RequestContext requestContext = RequestContext.builder&#40;&#41;
     *     .putMetadata&#40;&quot;key1&quot;, &quot;value1&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Response&lt;KeyRotationPolicy&gt; keyRotationPolicyResponse =
     *     keyClient.updateKeyRotationPolicyWithResponse&#40;&quot;keyName&quot;, myKeyRotationPolicy, requestContext&#41;;
     *
     * System.out.printf&#40;&quot;Response received successfully with status code: %d. Updated key rotation policy&quot;
     *     + &quot;with id: %s%n&quot;, keyRotationPolicyResponse.getStatusCode&#40;&#41;, keyRotationPolicyResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.v2.security.keyvault.keys.KeyClient.updateKeyRotationPolicyWithResponse#String-KeyRotationPolicy-RequestContext -->
     *
     * @param keyName The name of the key to update the policy of.
     * @param keyRotationPolicy The rotation policy to update.
     * @param requestContext Additional information that is passed through the HTTP pipeline during the service call.
     * @return A response object whose {@link Response#getValue() value} contains the updated key's rotation policy.
     *
     * @throws HttpResponseException If a key with the given {@code name} does not exist in the key vault.
     * @throws IllegalArgumentException If the provided {@code name} is {@code null} or empty.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyRotationPolicy> updateKeyRotationPolicyWithResponse(String keyName,
        KeyRotationPolicy keyRotationPolicy, RequestContext requestContext) {

        if (isNullOrEmpty(keyName)) {
            throw LOGGER.throwableAtError().log("'keyName' cannot be null or empty.", IllegalArgumentException::new);
        }

        return mapResponse(clientImpl.updateKeyRotationPolicyWithResponse(keyName,
            mapKeyRotationPolicy(keyRotationPolicy), requestContext),
            KeyVaultKeysModelsUtils::mapKeyRotationPolicyImpl);
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
