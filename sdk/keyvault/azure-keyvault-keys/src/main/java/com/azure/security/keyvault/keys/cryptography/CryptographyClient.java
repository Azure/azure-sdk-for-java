package com.azure.security.keyvault.keys.cryptography;

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
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = KeyClientBuilder.class, isAsync = true, serviceInterfaces = CryptographyService.class)
public final class CryptographyClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private JsonWebKey key;
    private final CryptographyService service;
    private final ClientLogger logger = new ClientLogger(com.azure.security.keyvault.keys.cryptography.CryptographyClient.class);
    private String endpoint;
    private String version;
    private String keyName;
    private EcKeyCryptographyClient ecKeyCryptographyClient;
    private RsaKeyCryptographyClient rsaKeyCryptographyClient;


    /**
     * Creates a CryptographyClient that uses {@code pipeline} to service requests
     *
     * @param key the JsonWebKey to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CryptographyClient(JsonWebKey key, HttpPipeline pipeline) {
        Objects.requireNonNull(key);
        this.key = key;
        service = RestProxy.create(CryptographyService.class, pipeline);
        ecKeyCryptographyClient = new EcKeyCryptographyClient(key, service);
        rsaKeyCryptographyClient = new RsaKeyCryptographyClient(key, service);
    }

    CryptographyClient(String kid, HttpPipeline pipeline) {
        Objects.requireNonNull(kid);
        service = RestProxy.create(CryptographyService.class, pipeline);
        ecKeyCryptographyClient = new EcKeyCryptographyClient(kid, service);
        rsaKeyCryptographyClient = new RsaKeyCryptographyClient(kid, service);
        unpackId(kid);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> getKey() {
        if (version == null) {
            version = "";
        }
        return withContext(context -> getKey(keyName, version, context));
    }

    private Mono<Response<Key>> getKey(String name, String version, Context context) {
        return service.getKey(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Retrieving key - {}",  name))
                .doOnSuccess(response -> logger.info("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }


    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used. The encrypt
     * operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public portion of the key is used
     * for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encryption. Possible values include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return withContext(context -> encryptAsync(algorithm, plaintext, context));
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used. The encrypt
     * operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public portion of the key is used
     * for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encryption. Possible values include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @return The {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return encryptAsync(algorithm, plaintext).block();
    }


    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.encryptAsync(algorithm, plaintext, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.encryptAsync(algorithm, plaintext, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a single block of data may be
     * decrypted, the size of this block is dependent on the target key and the algorithm to be used. The decrypt operation
     * is supported for both asymmetric and symmetric keys. This operation requires the keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @return A {@link Mono} containing the decrypted blob.
     */
    public Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return withContext(context -> decryptAsync(algorithm, cipherText, context));
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a single block of data may be
     * decrypted, the size of this block is dependent on the target key and the algorithm to be used. The decrypt operation
     * is supported for both asymmetric and symmetric keys. This operation requires the keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the specified encrypted content. Possible values include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @return The decrypted blob.
     */
    public byte[] decrypt(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return decryptAsync(algorithm, cipherText).block();
    }

    Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.decryptAsync(algorithm, cipherText, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.decryptAsync(algorithm, cipherText, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }
     }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512} and {@link SignatureAlgorithm#RSNULL RSNULL}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest) {
        return withContext(context -> signAsync(algorithm, digest, context));
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512} and {@link SignatureAlgorithm#RSNULL RSNULL}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @return The {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        return signAsync(algorithm, digest).block();
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.signAsync(algorithm, digest, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.signAsync(algorithm, digest, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512} and {@link SignatureAlgorithm#RSNULL RSNULL}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @return A {@link Mono} containing a {@link Boolean} indicating the signature verification result.
     */
    public Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return withContext(context -> verifyAsync(algorithm, digest, signature, context));
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512 ES512},
     * {@link SignatureAlgorithm#ES256K ES246K}, {@link SignatureAlgorithm#PS256 PS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512}, {@link SignatureAlgorithm#RS256 RS256}, {@link SignatureAlgorithm#RS384 RS384},
     * {@link SignatureAlgorithm#RS512 RS512} and {@link SignatureAlgorithm#RSNULL RSNULL}</p>
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public Boolean verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verifyAsync(algorithm, digest, signature).block();
    }

    Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
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
    public Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key) {
        return withContext(context -> wrapKeyAsync(algorithm, key, context));
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
     * @return The {@link KeyWrapResult} whose {@link KeyWrapResult#encryptedKey() encrypted key} contains the wrapped key result.
     */
    public KeyWrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKeyAsync(algorithm, key).block();
    }

    Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        if(this.key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.wrapKeyAsync(algorithm, key, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.wrapKeyAsync(algorithm, key, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", this.key.kty().toString()));
        }
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for unwrapping the specified encrypted key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @return A {@link Mono} containing a the unwrapped key content.
     */
    public Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return withContext(context -> unwrapKeyAsync(algorithm, encryptedKey, context));
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for unwrapping the specified encrypted key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @return The unwrapped key content.
     */
    public byte[] unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return unwrapKeyAsync(algorithm, encryptedKey).block();
    }

    Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }
    }

    private void unpackId(String keyId) {
        if (keyId != null && keyId.length() > 0) {
            try {
                URL url = new URL(keyId);
                String[] tokens = url.getPath().split("/");
                this.endpoint = url.getProtocol() + "://" + url.getHost();
                this.keyName = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


}
