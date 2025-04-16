// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography;

import com.azure.v2.security.keyvault.keys.KeyClient;
import com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyClientImpl;
import com.azure.v2.security.keyvault.keys.cryptography.implementation.LocalKeyCryptographyClient;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import io.clientcore.core.annotations.ReturnType;
import io.clientcore.core.annotations.ServiceClient;
import io.clientcore.core.annotations.ServiceMethod;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.createLocalClient;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.isThrowableRetryable;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.retrieveJwkAndCreateLocalClient;

/**
 * This class provides methods to perform cryptographic operations using asymmetric and symmetric keys. The client
 * supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the configured key.
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
 * <p><strong>Sample: Construct Cryptography Client</strong></p>
 * <p>The following code sample demonstrates the creation of a {@link CryptographyClient}, using the
 * {@link CryptographyClientBuilder} to configure it.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.instantiation -->
 *
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.withJsonWebKey.instantiation -->
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.withJsonWebKey.instantiation -->
 *
 * <p>When a {@link CryptographyClient} gets created using a {@code Azure Key Vault key identifier}, the first time a
 * cryptographic operation is attempted, the client will attempt to retrieve the key material from the service, cache
 * it, and perform all future cryptographic operations locally, deferring to the service when that's not possible. If
 * key retrieval and caching fails because of a non-retryable error, the client will not make any further attempts and
 * will fall back to performing all cryptographic operations on the service side. Conversely, when a
 * {@link CryptographyClient} created using a {@link JsonWebKey JSON Web Key}, all cryptographic operations will be
 * performed locally.</p>
 *
 * <br>
 * <hr>
 *
 * <h2>Encrypt Data</h2>
 * The {@link CryptographyClient} can be used to encrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to encrypt data using the
 * {@link CryptographyClient#encrypt(EncryptionAlgorithm, byte[])} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
 *
 * <br>
 * <hr>
 *
 * <h2>Decrypt Data</h2>
 * The {@link CryptographyClient} can be used to decrypt data.
 *
 * <p><strong>Code Sample:</strong></p>
 * <p>The following code sample demonstrates how to decrypt data using the
 * {@link CryptographyClient#decrypt(EncryptionAlgorithm, byte[])} API.</p>
 * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
 *
 * @see com.azure.v2.security.keyvault.keys.cryptography
 * @see CryptographyClientBuilder
 */
@ServiceClient(
    builder = CryptographyClientBuilder.class,
    serviceInterfaces = { KeyClientImpl.KeyClientService.class, SecretMinClientImpl.SecretMinClientService.class })
public class CryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClient.class);
    private final CryptographyClientImpl clientImpl;

    private volatile boolean skipLocalClientCreation;
    private volatile LocalKeyCryptographyClient localKeyCryptographyClient;

    final String keyId;

    /**
     * Creates an instance of {@link CryptographyClient}.
     *
     * @param clientImpl The implementation client.
     * @param disableKeyCaching Indicates if local key caching should be disabled and all cryptographic operations
     * deferred to the service.
     */
    CryptographyClient(CryptographyClientImpl clientImpl, boolean disableKeyCaching) {
        this.clientImpl = clientImpl;
        this.keyId = clientImpl.getKeyId();
        this.skipLocalClientCreation = disableKeyCaching;
    }

    /**
     * Creates an instance of {@link CryptographyClient} that uses a {@link JsonWebKey} to perform local cryptography
     * operations.
     *
     * @param jsonWebKey The JSON Web Key to use for local cryptography operations.
     *
     * @throws NullPointerException If {@code jsonWebKey} is {@code null}.
     * @throws IllegalArgumentException If the provided {@code jsonWebKey} is not valid, or if either of the key
     * operations or key type properties is not configured.
     */
    CryptographyClient(JsonWebKey jsonWebKey) {
        try {
            Objects.requireNonNull(jsonWebKey, "The JSON Web Key is required.");

            if (!jsonWebKey.isValid()) {
                throw new IllegalArgumentException("The JSON Web Key is not valid.");
            }

            if (jsonWebKey.getKeyOps() == null) {
                throw new IllegalArgumentException("The JSON Web Key's key operations property is not configured.");
            }

            if (jsonWebKey.getKeyType() == null) {
                throw new IllegalArgumentException("The JSON Web Key's key type property is not configured.");
            }

            this.clientImpl = null;
            this.keyId = jsonWebKey.getId();
            this.localKeyCryptographyClient = createLocalClient(jsonWebKey, null);
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        }
    }

    /**
     * Gets the public part of a given key, as well as the key's properties. The get key operation is applicable to all
     * key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the key the client is configured with from the service and prints out its details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKey -->
     *
     * @return The requested key.
     *
     * @throws HttpResponseException If the key this client is configured with doesn't exist in the key vault.
     * @throws UnsupportedOperationException If operating in local-only mode (using a client created using a
     * {@link JsonWebKey} instance).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey() {
        return getKeyWithResponse(RequestOptions.none()).getValue();
    }

    /**
     * Gets the public part of a specific version of a given key, as well as the key's properties. The get key operation
     * is applicable to all key types in Azure Key Vault or Managed HSM and requires the {@code keys/get} permission.
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Gets the key the client is configured with from the service. Prints out details of the response returned by
     * the service and the requested key.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#RequestOptions -->
     *
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A response object whose {@link Response#getValue() value} contains the requested key.
     *
     * @throws HttpResponseException If the key this client is configured with doesn't exist in the key vault.
     * @throws UnsupportedOperationException If operating in local-only mode (using a client created using a
     * {@link JsonWebKey} instance).
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(RequestOptions requestOptions) {
        if (clientImpl != null) {
            try {
                return clientImpl.getKeyWithResponse(requestOptions);
            } catch (RuntimeException e) {
                throw LOGGER.logThrowableAsError(e);
            }
        } else {
            throw LOGGER.logThrowableAsError(
                new UnsupportedOperationException("Operation not supported when operating in local-only mode."));
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for encrypting the specified {@code plaintext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Encrypts the content and prints out the result's details'.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     *
     * @return A result object whose {@link EncryptResult#getCipherText() ciphertext} contains the encrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.encrypt(algorithm, plaintext, RequestOptions.none());
            } else {
                return clientImpl.encrypt(algorithm, plaintext);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for encrypting the specified {@code plaintext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Encrypts the content and prints out the result's details'.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-RequestOptions -->
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object whose {@link EncryptResult#getCipherText() ciphertext} contains the encrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.encrypt(encryptParameters, requestOptions);
            } else {
                return clientImpl.encrypt(encryptParameters, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for decrypting the specified {@code ciphertext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Decrypts the encrypted content prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte -->
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted. Microsoft recommends you not use CBC without first ensuring the
     * integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing vulnerabilities
     * with CBC-mode symmetric decryption using padding</a> for more information.
     *
     * @return A result object whose {@link DecryptResult#getPlainText() plaintext} contains the decrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.decrypt(algorithm, ciphertext, RequestOptions.none());
            } else {
                return clientImpl.decrypt(algorithm, ciphertext, RequestOptions.none());
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use for decrypting the specified {@code ciphertext}.
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5},</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128CBC A128CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A192CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC A256CBC}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBCPAD A256CBCPAD}</li>
     *     <li>{@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}</li>
     *     <li>{@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}</li>
     *     <li>{@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</li>
     *     <li>{@link EncryptionAlgorithm#A128GCM A128GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A192GCM A192GCM}</li>
     *     <li>{@link EncryptionAlgorithm#A256GCM A256GCM}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Decrypts the encrypted content prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-RequestOptions -->
     *
     * @param decryptParameters The parameters to use in the decryption operation. Microsoft recommends you not use CBC
     * without first ensuring the integrity of the ciphertext using an HMAC, for example.
     * See <a href="https://docs.microsoft.com/dotnet/standard/security/vulnerabilities-cbc-mode">Timing vulnerabilities
     * with CBC-mode symmetric decryption using padding</a> for more information.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object whose {@link DecryptResult#getPlainText() plaintext} contains the decrypted content.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code plaintext} is {@code null}.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.decrypt(decryptParameters, requestOptions);
            } else {
                return clientImpl.decrypt(decryptParameters, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which the signature is to be created.
     *
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code digest} is {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.sign(algorithm, digest, RequestOptions.none());
            } else {
                return clientImpl.sign(algorithm, digest, RequestOptions.none());
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-RequestOptions -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.sign(algorithm, digest, requestOptions);
            } else {
                return clientImpl.sign(algorithm, digest, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the specified digest prints out the verification details when a response has
     * been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     *
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verify(algorithm, digest, signature, RequestOptions.none());
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the specified digest prints out the verification details when a response has
     * been received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-RequestOptions -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException If the key cannot be found for verifying.
     * @throws NullPointerException If any of the provided {@code algorithm}, {@code digest}, or {@code signature} is
     * {@code null}.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.verify(algorithm, digest, signature, requestOptions);
            } else {
                return clientImpl.verify(algorithm, digest, signature, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Wraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     *
     * @return A result object whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped key result.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code key} is {@code null}.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKey(algorithm, key, RequestOptions.none());
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Wraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-RequestOptions -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped key result.
     *
     * @throws HttpResponseException If the key to be used for encryption doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code key} is {@code null}.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.wrapKey(algorithm, key, requestOptions);
            } else {
                return clientImpl.wrapKey(algorithm, key, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap.
     * This operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Unwraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     *
     * @return A result object whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key result.
     *
     * @throws HttpResponseException If the key cannot be found for wrap operation.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code encryptedKey} is {@code null}.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return unwrapKey(algorithm, encryptedKey, RequestOptions.none());
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap.
     * This operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content.</p>
     *
     * <p>Possible values for asymmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#RSA1_5 RSA1_5}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP}</li>
     *     <li>{@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</li>
     * </ul>
     *
     * <p>Possible values for symmetric keys include:</p>
     * <ul>
     *     <li>{@link EncryptionAlgorithm#A128KW A128KW}</li>
     *     <li>{@link EncryptionAlgorithm#A192KW A192KW}</li>
     *     <li>{@link EncryptionAlgorithm#A256KW A256KW}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Unwraps the key content and prints out the result's details</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-RequestOptions -->
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key result.
     *
     * @throws HttpResponseException If the key cannot be found for wrap operation.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code encryptedKey} is {@code null}.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.unwrapKey(algorithm, encryptedKey, requestOptions);
            } else {
                return clientImpl.unwrapKey(algorithm, encryptedKey, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        }
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
     * </p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Signs the digest and prints out the result's details.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     *
     * @return A result object that contains the created {@link SignResult#getSignature() signature}.
     *
     * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
     * @throws NullPointerException If either of the provided {@code algorithm} or {@code data} is {@code null}.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data) {
        return signData(algorithm, data, RequestOptions.none());
    }

     /**
      * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
      * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
      *
      * <p>The {@code algorithm} indicates the type of algorithm to use to create the signature from the {@code digest}.
      * </p>
      *
      * <p>Possible values include:</p>
      * <ul>
      *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
      *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
      *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
      *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
      *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
      *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
      *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
      *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
      *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
      *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
      * </ul>
      *
      * <p><strong>Code Sample</strong></p>
      * <p>Signs the digest and prints out the result's details.</p>
      * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestOptions -->
      * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-RequestOptions -->
      *
      * @param algorithm The algorithm to use for signing.
      * @param data The content from which signature is to be created.
      *
      * @return A result object that contains the created {@link SignResult#getSignature() signature}.
      *
      * @throws HttpResponseException If the key to be used for signing doesn't exist in the key vault.
      * @throws NullPointerException If either of the provided {@code algorithm} or {@code data} is {@code null}.
      * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
      */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.signData(algorithm, data, requestOptions);
            } else {
                return clientImpl.signData(algorithm, data, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the raw data prints out the verification details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     *
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws HttpResponseException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return verifyData(algorithm, data, signature, RequestOptions.none());
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@code algorithm} indicates the type of algorithm to use to verify the {@code signature}.</p>
     *
     * <p>Possible values include:</p>
     * <ul>
     *     <li>{@link SignatureAlgorithm#ES256 ES256}</li>
     *     <li>{@link SignatureAlgorithm#ES384 ES384}</li>
     *     <li>{@link SignatureAlgorithm#ES512 ES512}</li>
     *     <li>{@link SignatureAlgorithm#ES256K ES256K}</li>
     *     <li>{@link SignatureAlgorithm#PS256 PS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     *     <li>{@link SignatureAlgorithm#RS256 RS256}</li>
     *     <li>{@link SignatureAlgorithm#RS384 RS384}</li>
     *     <li>{@link SignatureAlgorithm#RS512 RS512}</li>
     * </ul>
     *
     * <p><strong>Code Sample</strong></p>
     * <p>Verifies the signature against the raw data prints out the verification details when a response has been
     * received.</p>
     * <!-- src_embed com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestOptions -->
     * <!-- end com.azure.v2.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-RequestOptions -->
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @param requestOptions Additional options that are passed through the HTTP pipeline during the service call.
     *
     * @return A result object indicating if the signature verification result is {@link VerifyResult#isValid() valid}.
     *
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     * @throws HttpResponseException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, RequestOptions requestOptions) {
        try {
            if (isLocalClientAvailable()) {
                return localKeyCryptographyClient.verifyData(algorithm, data, signature, requestOptions);
            } else {
                return clientImpl.verifyData(algorithm, data, signature, requestOptions);
            }
        } catch (RuntimeException e) {
            throw LOGGER.logThrowableAsError(e);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new UncheckedIOException(e));
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logThrowableAsError(new RuntimeException(e));
        }
    }

    private boolean isLocalClientAvailable() {
        if (!skipLocalClientCreation && localKeyCryptographyClient == null) {
            try {
                localKeyCryptographyClient = retrieveJwkAndCreateLocalClient(clientImpl);
            } catch (Throwable t) {
                if (isThrowableRetryable(t)) {
                    LOGGER.atVerbose().log("Could not set up local cryptography for this operation. Defaulting "
                        + "to service-side cryptography.", t);
                } else {
                    skipLocalClientCreation = true;

                    LOGGER.atVerbose().log("Could not set up local cryptography. Defaulting to service-side"
                        + " cryptography for all operations.", t);
                }
            }
        }

        return localKeyCryptographyClient != null;
    }
}
