// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.keys.models.KeyType.EC;
import static com.azure.security.keyvault.keys.models.KeyType.EC_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.OCT;
import static com.azure.security.keyvault.keys.models.KeyType.OCT_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.RSA;
import static com.azure.security.keyvault.keys.models.KeyType.RSA_HSM;

/**
 * The {@link CryptographyAsyncClient} provides asynchronous methods to perform cryptographic operations using
 * asymmetric and symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify
 * operations using the configured key.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.instantiation}
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.withJsonWebKey.instantiation}
 *
 * @see CryptographyClientBuilder
 */
@ServiceClient(builder = CryptographyClientBuilder.class, isAsync = true, serviceInterfaces = CryptographyService.class)
public class CryptographyAsyncClient {
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";
    static final String SECRETS_COLLECTION = "secrets";

    JsonWebKey key;

    private final ClientLogger logger = new ClientLogger(CryptographyAsyncClient.class);
    private final CryptographyService service;
    private final HttpPipeline pipeline;
    private final String keyId;

    private CryptographyServiceClient cryptographyServiceClient;
    private LocalKeyCryptographyClient localKeyCryptographyClient;
    private String keyCollection;

    /**
     * Creates a {@link CryptographyAsyncClient} that uses a given {@link HttpPipeline pipeline} to service requests.
     *
     * @param keyId The Azure Key Vault key identifier to use for cryptography operations.
     * @param pipeline {@link HttpPipeline} that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     */
    CryptographyAsyncClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version) {
        unpackAndValidateId(keyId);

        this.keyId = keyId;
        this.pipeline = pipeline;
        this.service = RestProxy.create(CryptographyService.class, pipeline);
        this.cryptographyServiceClient = new CryptographyServiceClient(keyId, service, version);
        this.key = null;
    }

    /**
     * Creates a {@link CryptographyAsyncClient} that uses a {@link JsonWebKey} to perform local cryptography
     * operations.
     *
     * @param jsonWebKey The {@link JsonWebKey} to use for local cryptography operations.
     */
    CryptographyAsyncClient(JsonWebKey jsonWebKey) {
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

        this.key = jsonWebKey;
        this.keyId = jsonWebKey.getId();
        this.pipeline = null;
        this.service = null;
        this.cryptographyServiceClient = null;

        initializeCryptoClients();
    }

    private void initializeCryptoClients() {
        if (localKeyCryptographyClient != null) {
            return;
        }

        if (key.getKeyType().equals(RSA) || key.getKeyType().equals(RSA_HSM)) {
            this.localKeyCryptographyClient = new RsaKeyCryptographyClient(this.key, this.cryptographyServiceClient);
        } else if (key.getKeyType().equals(EC) || key.getKeyType().equals(EC_HSM)) {
            this.localKeyCryptographyClient = new EcKeyCryptographyClient(this.key, this.cryptographyServiceClient);
        } else if (key.getKeyType().equals(OCT) || key.getKeyType().equals(OCT_HSM)) {
            this.localKeyCryptographyClient = new AesKeyCryptographyClient(this.key, this.cryptographyServiceClient);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "The JSON Web Key type: %s is not supported.", this.key.getKeyType().toString())));
        }
    }

    /**
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    Mono<String> getKeyId() {
        return Mono.defer(() -> Mono.just(this.keyId));
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKey}
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultKey> getKey() {
        try {
            return getKeyWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.getKeyWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultKey>> getKeyWithResponse() {
        try {
            return withContext(this::getKeyWithResponse);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> getKeyWithResponse(Context context) {
        if (cryptographyServiceClient != null) {
            return cryptographyServiceClient.getKey(context);
        } else {
            throw logger.logExceptionAsError(new UnsupportedOperationException(
                "Operation not supported when in operating local-only mode"));
        }
    }

    Mono<JsonWebKey> getSecretKey() {
        try {
            return withContext(context -> cryptographyServiceClient.getSecretKey(context))
                .flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     *
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text}
     * contains the encrypted content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        Objects.requireNonNull(algorithm, "'algorithm' cannot be null.");
        Objects.requireNonNull(plaintext, "'plaintext' cannot be null.");

        return encrypt(algorithm, plaintext, null);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptParameters}
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     *
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text}
     * contains the encrypted content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EncryptResult> encrypt(EncryptParameters encryptParameters) {
        Objects.requireNonNull(encryptParameters, "'encryptParameters' cannot be null.");

        try {
            return withContext(context -> encrypt(encryptParameters, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.encrypt(algorithm, plaintext, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.ENCRYPT)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Encrypt operation is missing permission/not supported for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.encryptAsync(algorithm, plaintext, context, key);
        });
    }

    Mono<EncryptResult> encrypt(EncryptParameters encryptParameters, Context context) {
        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.encrypt(encryptParameters, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.ENCRYPT)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Encrypt operation is missing permission/not supported for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.encryptAsync(encryptParameters, context, key);
        });
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm
     * to be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
     * the {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting
     * the specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     *
     * @return A {@link Mono} containing the decrypted blob.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        Objects.requireNonNull(algorithm, "'algorithm' cannot be null.");
        Objects.requireNonNull(algorithm, "'ciphertext' cannot be null.");

        return decrypt(algorithm, ciphertext, null);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm
     * to be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
     * the {@code keys/decrypt} permission for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting
     * the specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A128GCM A128GCM}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384},
     * {@link EncryptionAlgorithm#A192GCM A192GCM}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD}, {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} and
     * {@link EncryptionAlgorithm#A256GCM A256GCM}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#DecryptParameters}
     *
     * @param decryptParameters The parameters to use in the decryption operation.
     *
     * @return A {@link Mono} containing the decrypted blob.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DecryptResult> decrypt(DecryptParameters decryptParameters) {
        Objects.requireNonNull(decryptParameters, "'decryptParameters' cannot be null.");

        try {
            return withContext(context -> decrypt(decryptParameters, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.decrypt(algorithm, ciphertext, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.DECRYPT)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Decrypt operation is not allowed for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.decryptAsync(algorithm, ciphertext, context, key);
        });
    }

    Mono<DecryptResult> decrypt(DecryptParameters decryptParameters, Context context) {
        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.decrypt(decryptParameters, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.DECRYPT)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Decrypt operation is not allowed for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.decryptAsync(decryptParameters, context, key);
        });
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384} and {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sings the digest. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     *
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     * the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code digest} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest) {
        try {
            return withContext(context -> sign(algorithm, digest, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content to be signed cannot be null.");

        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.sign(algorithm, digest, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.SIGN)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Sign operation is not allowed for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.signAsync(algorithm, digest, context, key);
        });
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature. This operation
     * requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include: {@link SignatureAlgorithm#ES256 ES256},
     * {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256},
     * {@link SignatureAlgorithm#RS384 RS384}, {@link SignatureAlgorithm#RS512 RS512},
     * {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     *
     * @return A {@link Mono} containing a {@link VerifyResult}
     * {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException If {@code algorithm}, {@code digest} or {@code signature} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for verifying.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        try {
            return withContext(context -> verify(algorithm, digest, signature, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.verify(algorithm, digest, signature, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.VERIFY)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Verify operation is not allowed for key with id: %s", key.getId()))));
            }

            return localKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context, key);
        });
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the {@code keys/wrapKey} permission for non-local
     * operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128KW A128KW},
     * {@link EncryptionAlgorithm#A192KW A192KW} and {@link EncryptionAlgorithm#A256KW A256KW}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a
     * response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     *
     * @return A {@link Mono} containing a {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted key}
     * contains the wrapped key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code key} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<WrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        try {
            return withContext(context -> wrapKey(algorithm, key, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<WrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.wrapKey(algorithm, key, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.WRAP_KEY)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Wrap Key operation is not allowed for key with id: %s", this.key.getId()))));
            }

            return localKeyCryptographyClient.wrapKeyAsync(algorithm, key, context, this.key);
        });
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation
     * is the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap. This
     * operation requires the {@code keys/unwrapKey} permission for non-local operations.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for unwrapping the
     * specified encrypted key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW},
     * {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when
     * a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     *
     * @return A {@link Mono} containing an {@link UnwrapResult} whose {@link UnwrapResult#getKey() decrypted
     * key} contains the unwrapped key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code encryptedKey} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<UnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        try {
            return withContext(context -> unwrapKey(algorithm, encryptedKey, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<UnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.unwrapKey(algorithm, encryptedKey, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.UNWRAP_KEY)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Unwrap Key operation is not allowed for key with id: %s", this.key.getId()))));
            }

            return localKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context, key);
        });
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the {@code keys/sign} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to sign the digest.
     * Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384} and {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a
     * response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     *
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     * the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code data} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data) {
        try {
            return withContext(context -> signData(algorithm, data, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to be signed cannot be null.");

        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.signData(algorithm, data, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.SIGN)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Sign Operation is not allowed for key with id: %s", this.key.getId()))));
            }

            return localKeyCryptographyClient.signDataAsync(algorithm, data, context, key);
        });
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys. In case of asymmetric keys public portion of the key is used to verify the signature.
     * This operation requires the {@code keys/verify} permission for non-local operations.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K},
     * {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384} and {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     *
     * @return A {@link Mono} containing a {@link VerifyResult}
     * {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException If {@code algorithm}, {@code data} or {@code signature} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for verifying.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        try {
            return withContext(context -> verifyData(algorithm, data, signature, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");
        return ensureValidKeyAvailable().flatMap(available -> {
            if (!available) {
                return cryptographyServiceClient.verifyData(algorithm, data, signature, context);
            }

            if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.VERIFY)) {
                return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                    "Verify operation is not allowed for key with id: %s", this.key.getId()))));
            }
            return localKeyCryptographyClient.verifyDataAsync(algorithm, data, signature, context, key);
        });
    }

    private void unpackAndValidateId(String keyId) {
        if (CoreUtils.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'keyId' cannot be null or empty."));
        }

        try {
            URL url = new URL(keyId);
            String[] tokens = url.getPath().split("/");
            String endpoint = url.getProtocol() + "://" + url.getHost();

            if (url.getPort() != -1) {
                endpoint += ":" + url.getPort();
            }

            String keyName = (tokens.length >= 3 ? tokens[2] : null);
            this.keyCollection = (tokens.length >= 2 ? tokens[1] : null);

            if (Strings.isNullOrEmpty(endpoint)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Key endpoint in key identifier is invalid."));
            } else if (Strings.isNullOrEmpty(keyName)) {
                throw logger.logExceptionAsError(
                    new IllegalArgumentException("Key name in key identifier is invalid."));
            }
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The key identifier is malformed.", e));
        }
    }

    private boolean checkKeyPermissions(List<KeyOperation> operations, KeyOperation keyOperation) {
        return operations.contains(keyOperation);
    }

    private Mono<Boolean> ensureValidKeyAvailable() {
        boolean keyNotAvailable = (key == null && keyCollection != null);
        boolean keyNotValid = (key != null && !key.isValid());

        if (keyNotAvailable || keyNotValid) {
            if (keyCollection.equals(SECRETS_COLLECTION)) {
                return getSecretKey().map(jsonWebKey -> {
                    key = (jsonWebKey);

                    if (key.isValid()) {
                        initializeCryptoClients();
                        return true;
                    } else {
                        return false;
                    }
                });
            } else {
                return getKey().map(keyVaultKey -> {
                    key = (keyVaultKey.getKey());

                    if (key.isValid()) {
                        initializeCryptoClients();
                        return true;
                    } else {
                        return false;
                    }
                });
            }
        } else {
            return Mono.defer(() -> Mono.just(true));
        }
    }

    CryptographyServiceClient getCryptographyServiceClient() {
        return cryptographyServiceClient;
    }

    void setCryptographyServiceClient(CryptographyServiceClient serviceClient) {
        this.cryptographyServiceClient = serviceClient;
    }
}
