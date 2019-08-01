package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = KeyClientBuilder.class, isAsync = true, serviceInterfaces = CryptographyService.class)
public final class CryptographyAsyncClient {
    private JsonWebKey key;
    private CryptographyService service;
    private String version;
    private EcKeyCryptographyClient ecKeyCryptographyClient;
    private RsaKeyCryptographyClient rsaKeyCryptographyClient;
    private CryptographyServiceClient cryptographyServiceClient;
    private SymmetricKeyCryptographyClient symmetricKeyCryptographyClient;
    private final ClientLogger logger = new ClientLogger(CryptographyAsyncClient.class);

    /**
     * Creates a CryptographyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param key the JsonWebKey to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CryptographyAsyncClient(JsonWebKey key, HttpPipeline pipeline) {
        Objects.requireNonNull(key);
        this.key = key;
        service = RestProxy.create(CryptographyService.class, pipeline);
        if(!Strings.isNullOrEmpty(key.kid())) {
            unpackAndValidateId(key.kid());
            cryptographyServiceClient = new CryptographyServiceClient(key.kid(), service);
        }
        initializeCryptoClients();
    }

    /**
     * Creates a CryptographyAsyncClient that uses {@code pipeline} to service requests
     *
     * @param kid THe Azure Key vault key identifier to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CryptographyAsyncClient(String kid, HttpPipeline pipeline) {
        unpackAndValidateId(kid);
        service = RestProxy.create(CryptographyService.class, pipeline);
        cryptographyServiceClient = new CryptographyServiceClient(kid, service);
        ecKeyCryptographyClient = new EcKeyCryptographyClient(cryptographyServiceClient);
        rsaKeyCryptographyClient = new RsaKeyCryptographyClient(cryptographyServiceClient);
        symmetricKeyCryptographyClient = new SymmetricKeyCryptographyClient(cryptographyServiceClient);
    }

    private void initializeCryptoClients() {
        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                rsaKeyCryptographyClient = new RsaKeyCryptographyClient(key, cryptographyServiceClient);
                break;
            case EC:
            case EC_HSM:
                ecKeyCryptographyClient = new EcKeyCryptographyClient(key, cryptographyServiceClient);
                break;
            case OCT:
                symmetricKeyCryptographyClient = new SymmetricKeyCryptographyClient(key, cryptographyServiceClient);
                break;
            default:
                throw new IllegalArgumentException(String.format("The Json Web Key Type: %s  is not supported.", key.kty().toString()));
        }
    }


    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires the {@code keys/get} permission.
     *
     * @throws ResourceNotFoundException when the configured key doesn't exist in the key vault.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> getKey() {
        return withContext(context -> getKey(context));
    }

    Mono<Response<Key>> getKey(Context context) {
        return cryptographyServiceClient.getKey(context);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used. The encrypt
     * operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public portion of the key is used
     * for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values
     * for assymetric keys include: {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} </p>
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return withContext(context -> encrypt(algorithm, plaintext, context, null, null));
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used. The encrypt
     * operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public portion of the key is used
     * for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values
     * for assymetric keys include: {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} </p>
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param iv The initialization vector
     * @param authenticationData The authentication data
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] authenticationData) {
        return withContext(context -> encrypt(algorithm, plaintext, context, iv, authenticationData));
    }

    Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context, byte[] iv, byte[] authenticationData) {
        Objects.requireNonNull(algorithm);

        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.encrypt(algorithm, plaintext, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.ENCRYPT)){
            return Mono.error(new UnsupportedOperationException(String.format("Encrypt Operation is not supported for key with id %s", key.kid())));
        }

            switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.encryptAsync(algorithm, plaintext, iv, authenticationData, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.encryptAsync(algorithm, plaintext, iv, authenticationData, context, key);
            case OCT:
                return symmetricKeyCryptographyClient.encryptAsync(algorithm, plaintext, iv, authenticationData, context, key);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not allowed for Key Type: %s", key.kty().toString()));
        }
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a single block of data may be
     * decrypted, the size of this block is dependent on the target key and the algorithm to be used. The decrypt operation
     * is supported for both asymmetric and symmetric keys. This operation requires the keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values
     * for assymetric keys include: {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} </p>
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @return A {@link Mono} containing the decrypted blob.
     */
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return withContext(context -> decrypt(algorithm, cipherText, null, null, null, context));
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a single block of data may be
     * decrypted, the size of this block is dependent on the target key and the algorithm to be used. The decrypt operation
     * is supported for both asymmetric and symmetric keys. This operation requires the keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values
     * for assymetric keys include: {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512} </p>
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @return A {@link Mono} containing the decrypted blob.
     */
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag) {
        return withContext(context -> decrypt(algorithm, cipherText, iv, authenticationData, authenticationTag, context));
    }

    Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Context context) {
        Objects.requireNonNull(algorithm);
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.decrypt(algorithm, cipherText, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.DECRYPT)){
            return Mono.error(new UnsupportedOperationException(String.format("Decrypt Operation is not allowed for key with id %s", key.kid())));
        }

        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.decryptAsync(algorithm, cipherText, iv, authenticationData, authenticationTag, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.decryptAsync(algorithm, cipherText, iv, authenticationData, authenticationTag, context, key);
            case OCT:
                return symmetricKeyCryptographyClient.decryptAsync(algorithm, cipherText, iv, authenticationData, authenticationTag, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Decrypt operation is not supported for Key Type: %s", key.kty().toString())));
        }
     }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest) {
        return withContext(context -> sign(algorithm, digest, context));
    }

    Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm);
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.sign(algorithm, digest, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.SIGN)){
            return Mono.error(new UnsupportedOperationException(String.format("Sign Operation is not allowed for key with id %s", key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.signAsync(algorithm, digest, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.signAsync(algorithm, digest, context, key);
            case OCT:
                return symmetricKeyCryptographyClient.signAsync(algorithm, digest, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Sign operaiton is not supported for Key Type: %s", key.kty().toString())));
        }

    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @return A {@link Mono} containing a {@link Boolean} indicating the signature verification result.
     */
    public Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return withContext(context -> verify(algorithm, digest, signature, context));
    }

    Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.verify(algorithm, digest, signature, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.VERIFY)){
            return Mono.error(new UnsupportedOperationException(String.format("Verify Operation is not allowed for key with id %s", key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context, key);
            case OCT:
                return symmetricKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Verify operation is not supported for Key Type: %s", key.kty().toString())));
        }

    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @return A {@link Mono} containing a {@link KeyWrapResult} whose {@link KeyWrapResult#encryptedKey() encrypted key} contains the wrapped key result.
     */
    public Mono<KeyWrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return withContext(context -> wrapKey(algorithm, key, context));
    }

    Mono<KeyWrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.wrapKey(algorithm, key, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.WRAP_KEY)){
            return Mono.error(new UnsupportedOperationException(String.format("Wrap Key Operation is not allowed for key with id %s", this.key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.wrapKeyAsync(algorithm, key, context, this.key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.wrapKeyAsync(algorithm, key, context, this.key);
            case OCT:
                return symmetricKeyCryptographyClient.wrapKeyAsync(algorithm, key, context, this.key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", this.key.kty().toString())));
        }
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}</p>
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @return A {@link Mono} containing a the unwrapped key content.
     */
    public Mono<KeyUnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return withContext(context -> unwrapKey(algorithm, encryptedKey, context));
    }

    Mono<KeyUnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm);
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.unwrapKey(algorithm, encryptedKey, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.WRAP_KEY)){
            return Mono.error(new UnsupportedOperationException(String.format("Unwrap Key Operation is not allowed for key with id %s", this.key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context, key);
            case OCT:
                return symmetricKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString())));
        }
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data) {
        return withContext(context -> signData(algorithm, data, context));
    }

    Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        Objects.requireNonNull(algorithm);
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.signData(algorithm, data, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.SIGN)){
            return Mono.error(new UnsupportedOperationException(String.format("Sign Operation is not allowed for key with id %s", this.key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.signDataAsync(algorithm, data, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.signDataAsync(algorithm, data, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString())));
        }
    }

    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384} and
     * {@link SignatureAlgorithm#RS512 RS512}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return withContext(context -> verifyData(algorithm, data, signature, context));
    }

    Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm);
        boolean keyAvailableLocally = ensureValidKeyAvailable();

        if(!keyAvailableLocally) {
            return cryptographyServiceClient.verifyData(algorithm, data, signature, context);
        }

        if (!checkKeyPermissions(this.key.keyOps(), KeyOperation.VERIFY)){
            return Mono.error(new UnsupportedOperationException(String.format("Verify Operation is not allowed for key with id %s", this.key.kid())));
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.verifyDataAsync(algorithm, data, signature, context, key);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.verifyDataAsync(algorithm, data, signature, context, key);
            default:
                return Mono.error(new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString())));
        }
    }

    private void unpackAndValidateId(String keyId) {
        if (keyId != null && keyId.length() > 0) {
            try {
                URL url = new URL(keyId);
                String[] tokens = url.getPath().split("/");
                String endpoint = url.getProtocol() + "://" + url.getHost();
                String keyName = (tokens.length >= 3 ? tokens[2] : null);
                version = (tokens.length >= 4 ? tokens[3] : null);
                if(Strings.isNullOrEmpty(endpoint)) {
                    throw new IllegalArgumentException("Key endpoint in key id is invalid");
                } else if (Strings.isNullOrEmpty(keyName)) {
                    throw new IllegalArgumentException("Key name in key id is invalid");
                } else if(Strings.isNullOrEmpty(version)) {
                    throw new IllegalArgumentException("Key version in key id is invalid");
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Key Id is invalid");
        }
    }

    private boolean checkKeyPermissions(List<KeyOperation> operations, KeyOperation keyOperation) {
        if (operations.contains(keyOperation)) {
            return true;
        }
        return false;
    }

    private boolean ensureValidKeyAvailable() {
        boolean keyAvailableLocally = true;
        if(key == null) {
            try {
                this.key = getKey().block().value().keyMaterial();
                keyAvailableLocally = this.key.isValid();
            } catch (HttpResponseException e) {
                logger.info("Failed to retrieve key from key vault");
                keyAvailableLocally = false;
            }
        }
        return keyAvailableLocally;
    }

}
