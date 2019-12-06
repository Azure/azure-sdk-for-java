// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.keys.models.KeyType.EC;
import static com.azure.security.keyvault.keys.models.KeyType.EC_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.RSA;
import static com.azure.security.keyvault.keys.models.KeyType.RSA_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.OCT;

/**
 * The CryptographyAsyncClient provides asynchronous methods to perform cryptographic operations using asymmetric and
 * symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the
 * configured key.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.instantiation}
 *
 * @see CryptographyClientBuilder
 */
@ServiceClient(builder = CryptographyClientBuilder.class, isAsync = true, serviceInterfaces = CryptographyService.class)
public class CryptographyAsyncClient {
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";
    static final String SECRETS_COLLECTION = "secrets";
    JsonWebKey key;
    private final CryptographyService service;
    private CryptographyServiceClient cryptographyServiceClient;
    private LocalKeyCryptographyClient localKeyCryptographyClient;
    private final ClientLogger logger = new ClientLogger(CryptographyAsyncClient.class);
    private String keyCollection;

    /**
     * Creates a CryptographyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param key the JsonWebKey to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     */
    CryptographyAsyncClient(JsonWebKey key, HttpPipeline pipeline, CryptographyServiceVersion version) {
        Objects.requireNonNull(key);
        if (!key.isValid()) {
            throw new IllegalArgumentException("Json Web Key is not valid");
        }
        if (key.getKeyOps() == null) {
            throw new IllegalArgumentException("Json Web Key's key operations property is not configured");
        }

        if (key.getKeyType() == null) {
            throw new IllegalArgumentException("Json Web Key's key type property is not configured");
        }
        this.key = key;
        service = RestProxy.create(CryptographyService.class, pipeline);
        if (!Strings.isNullOrEmpty(key.getId())) {
            unpackAndValidateId(key.getId());
            cryptographyServiceClient = new CryptographyServiceClient(key.getId(), service);
        } else {
            cryptographyServiceClient = null;
        }
        initializeCryptoClients();
    }

    /**
     * Creates a CryptographyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param keyId THe Azure Key vault key identifier to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     * @param version {@link CryptographyServiceVersion} of the service to be used when making requests.
     */
    CryptographyAsyncClient(String keyId, HttpPipeline pipeline, CryptographyServiceVersion version) {
        unpackAndValidateId(keyId);
        service = RestProxy.create(CryptographyService.class, pipeline);
        cryptographyServiceClient = new CryptographyServiceClient(keyId, service);
        this.key = null;
    }

    private void initializeCryptoClients() {
        if (localKeyCryptographyClient != null) {
            return;
        }
        if (key.getKeyType().equals(RSA) || key.getKeyType().equals(RSA_HSM)) {
            localKeyCryptographyClient = new RsaKeyCryptographyClient(key, cryptographyServiceClient);
        } else if (key.getKeyType().equals(EC) || key.getKeyType().equals(EC_HSM)) {
            localKeyCryptographyClient = new EcKeyCryptographyClient(key, cryptographyServiceClient);
        } else if (key.getKeyType().equals(OCT)) {
            localKeyCryptographyClient = new SymmetricKeyCryptographyClient(key, cryptographyServiceClient);
        } else {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(
                "The Json Web Key Type: %s is not supported.", key.getKeyType().toString())));
        }
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.cryptographyclient.getKeyWithResponse}
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the requested
     *     {@link KeyVaultKey key}.
     * @throws ResourceNotFoundException when the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<KeyVaultKey>> getKeyWithResponse() {
        try {
            return withContext(context -> getKeyWithResponse(context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKey}
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     * @throws ResourceNotFoundException when the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<KeyVaultKey> getKey() {
        try {
            return getKeyWithResponse().flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<KeyVaultKey>> getKeyWithResponse(Context context) {
        return cryptographyServiceClient.getKey(context);
    }

    Mono<JsonWebKey> getSecretKey() {
        try {
            return withContext(context -> cryptographyServiceClient.getSecretKey(context)).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public
     * portion of the key is used for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting the
     * specified {@code plaintext}. Possible values for assymetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC} and
     * {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text}
     *     contains the encrypted content.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @throws UnsupportedOperationException if the encrypt operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or  {@code plainText} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        try {
            return withContext(context -> encrypt(algorithm, plaintext, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


    Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plain text content to be encrypted cannot be null.");

        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.encrypt(algorithm, plaintext, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.ENCRYPT)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Encrypt Operation is missing "
                + "permission/not supported for key with id %s", key.getId()))));
        }
        return localKeyCryptographyClient.encryptAsync(algorithm, plaintext, context, key);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the
     * specified encrypted content. Possible values for assymetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link
     * EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256}, {@link EncryptionAlgorithm#A192CBC A192CBC},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link
     * EncryptionAlgorithm#A256CBC A256CBC} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @return A {@link Mono} containing the decrypted blob.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @throws UnsupportedOperationException if the decrypt operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code cipherText} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText) {
        try {
            return withContext(context -> decrypt(algorithm, cipherText, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(cipherText, "Cipher text content to be decrypted cannot be null.");
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.decrypt(algorithm, cipherText, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.DECRYPT)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Decrypt Operation is not allowed for "
                + "key with id %s", key.getId()))));
        }
        return localKeyCryptographyClient.decryptAsync(algorithm, cipherText, context, key);
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.sign#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     *     the created signature.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code digest} is null.
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
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.sign(algorithm, digest, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.SIGN)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Sign Operation is not allowed for key "
                + "with id %s", key.getId()))));
        }

        return localKeyCryptographyClient.signAsync(algorithm, digest, context, key);
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature . This operation
     * requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512
     * ES512}, {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link
     * SignatureAlgorithm#RS384 RS384}, {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384} and {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @return A {@link Mono} containing a {@link Boolean} indicating the signature verification result.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
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
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.verify(algorithm, digest, signature, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.VERIFY)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Verify Operation is not allowed for "
                + "key with id %s", key.getId()))));
        }
        return localKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context, key);
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link
     * KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @return A {@link Mono} containing a {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted
     *     key} contains the wrapped key result.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException if the wrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code key} is null.
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
        Objects.requireNonNull(algorithm, "Key Wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.wrapKey(algorithm, key, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.WRAP_KEY)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Wrap Key Operation is not allowed for "
                + "key with id %s", this.key.getId()))));
        }

        return localKeyCryptographyClient.wrapKeyAsync(algorithm, key, context, this.key);
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is
     * the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey
     * permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for unwrapping the
     * specified encrypted key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link
     * KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link
     * KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @return A {@link Mono} containing a the unwrapped key content.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException if the unwrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code encryptedKey} is null.
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
        Objects.requireNonNull(algorithm, "Key Wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.unwrapKey(algorithm, encryptedKey, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.WRAP_KEY)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Unwrap Key Operation is not allowed "
                + "for key with id %s", this.key.getId()))));
        }
        return localKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context, key);
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to sign the digest.
     * Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512
     * ES512}, {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256},
     * {@link SignatureAlgorithm#RS384 RS384}, {@link SignatureAlgorithm#RS512 RS512}, {@link
     * SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.signData#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     *     the created signature.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
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

        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.signData(algorithm, data, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.SIGN)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format("Sign Operation is not allowed for key "
                + "with id %s", this.key.getId()))));
        }
        return localKeyCryptographyClient.signDataAsync(algorithm, data, context, key);
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires
     * the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512
     * ES512}, {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link
     * SignatureAlgorithm#RS384 RS384}, {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256},
     * {@link SignatureAlgorithm#RS384 RS384} and {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @return The {@link Boolean} indicating the signature verification result.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
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
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if (!keyAvailableLocally) {
            return cryptographyServiceClient.verifyData(algorithm, data, signature, context);
        }

        if (!checkKeyPermissions(this.key.getKeyOps(), KeyOperation.VERIFY)) {
            return Mono.error(logger.logExceptionAsError(new UnsupportedOperationException(String.format(
                "Verify Operation is not allowed for key with id %s", this.key.getId()))));
        }
        return localKeyCryptographyClient.verifyDataAsync(algorithm, data, signature, context, key);
    }

    private void unpackAndValidateId(String keyId) {
        if (CoreUtils.isNullOrEmpty(keyId)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Key Id is invalid"));
        }
        try {
            URL url = new URL(keyId);
            String[] tokens = url.getPath().split("/");
            String endpoint = url.getProtocol() + "://" + url.getHost();
            String keyName = (tokens.length >= 3 ? tokens[2] : null);
            String version = (tokens.length >= 4 ? tokens[3] : null);
            this.keyCollection = (tokens.length >= 2 ? tokens[1] : null);
            if (Strings.isNullOrEmpty(endpoint)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Key endpoint in key id is invalid"));
            } else if (Strings.isNullOrEmpty(keyName)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Key name in key id is invalid"));
            } else if (Strings.isNullOrEmpty(version)) {
                throw logger.logExceptionAsError(new IllegalArgumentException("Key version in key id is invalid"));
            }
        } catch (MalformedURLException e) {
            throw logger.logExceptionAsError(new IllegalArgumentException("The key identifier is malformed", e));
        }
    }

    private boolean checkKeyPermissions(List<KeyOperation> operations, KeyOperation keyOperation) {
        return operations.contains(keyOperation);
    }

    private boolean ensureValidKeyAvailable() {
        boolean keyAvailableLocally = true;
        if (this.key == null && keyCollection != null) {
            try {
                if (keyCollection.equals(SECRETS_COLLECTION)) {
                    this.key = getSecretKey().block();
                } else {
                    KeyVaultKey keyVaultKey = getKey().block();
                    this.key = keyVaultKey.getKey();
                }
                keyAvailableLocally = this.key.isValid();
                initializeCryptoClients();
            } catch (HttpResponseException | NullPointerException e) {
                logger.warning("Failed to retrieve key from key vault");
                logger.logExceptionAsWarning(e);
                keyAvailableLocally = false;
            }
        }
        return keyAvailableLocally;
    }

    CryptographyServiceClient getCryptographyServiceClient() {
        return cryptographyServiceClient;
    }

    void setCryptographyServiceClient(CryptographyServiceClient serviceClient) {
        this.cryptographyServiceClient = serviceClient;
    }
}
