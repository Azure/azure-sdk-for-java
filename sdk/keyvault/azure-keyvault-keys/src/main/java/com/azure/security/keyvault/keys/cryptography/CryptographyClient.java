// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyUnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.models.Key;

/**
 * The CryptographyClient provides synchronous methods to perform cryptographic operations using asymmetric and
 * symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the configured key.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.instantiation}
 *
 * @see CryptographyClientBuilder
 */
@ServiceClient(builder = CryptographyClientBuilder.class, serviceInterfaces = CryptographyService.class)
public final class CryptographyClient {
    private final CryptographyAsyncClient client;

    /**
     * Creates a KeyClient that uses {@code pipeline} to service requests
     *
     * @param client The {@link CryptographyAsyncClient} that the client routes its request through.
     */
    CryptographyClient(CryptographyAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the key configured in the client. Prints out the returned key details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKey}
     *
     * @throws ResourceNotFoundException when the configured key doesn't exist in the key vault.
     * @return The requested {@link Key key}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Key getKey() {
        return getKeyWithResponse(Context.NONE).value();
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires the {@code keys/get} permission.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the key configured in the client. Prints out the returned key details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.getKeyWithResponse#Context}
     *
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException when the configured key doesn't exist in the key vault.
     * @return A {@link Response} whose {@link Response#value() value} contains the requested {@link Key key}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Key> getKeyWithResponse(Context context) {
        return client.getKeyWithResponse(context).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Prints out the encrypted content details.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#symmetric-encrypt}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param iv The initialization vector
     * @param authenticationData The authentication data
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @throws NullPointerException if {@code algorithm} or  {@code plainText} is null.
     * @return A {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] authenticationData) {
        return encrypt(algorithm, plaintext, iv, authenticationData, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#symmetric-encrypt-Context}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param iv The initialization vector
     * @param authenticationData The authentication data
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @throws NullPointerException if {@code algorithm} or  {@code plainText} is null.
     * @return A {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] authenticationData, Context context) {
        return client.encrypt(algorithm, plaintext, context, iv, authenticationData).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.encrypt#asymmetric-encrypt}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @throws ResourceNotFoundException if the key cannot be found for encryption.
     * @throws NullPointerException if {@code algorithm} or  {@code plainText} is null.
     * @return The {@link EncryptResult} whose {@link EncryptResult#cipherText() cipher text} contains the encrypted content.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return encrypt(algorithm, plaintext, null, null, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#symmetric-decrypt}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @param iv The initialization vector.
     * @param authenticationData The authentication data.
     * @param authenticationTag The authentication tag.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @throws NullPointerException if {@code algorithm} or {@code cipherText} is null.
     * @return The decrypted blob.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag) {
        return decrypt(algorithm, cipherText, iv, authenticationData, authenticationTag, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#symmetric-decrypt-Context}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @param iv The initialization vector.
     * @param authenticationData The authentication data.
     * @param authenticationTag The authentication tag.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @throws NullPointerException if {@code algorithm} or {@code cipherText} is null.
     * @return The decrypted blob.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Context context) {
        return client.decrypt(algorithm, cipherText, iv, authenticationData, authenticationTag, context).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.decrypt#asymmetric-decrypt}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param cipherText The content to be decrypted.
     * @throws ResourceNotFoundException if the key cannot be found for decryption.
     * @throws NullPointerException if {@code algorithm} or {@code cipherText} is null.
     * @return The decrypted blob.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return decrypt(algorithm, cipherText, null, null, null, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Sings the digest. Subscribes to the call asynchronously and prints out the signature details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws NullPointerException if {@code algorithm} or {@code digest} is null.
     * @return A {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        return client.sign(algorithm, digest, context).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Sings the digest. Subscribes to the call asynchronously and prints out the signature details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws NullPointerException if {@code algorithm} or {@code digest} is null.
     * @return A {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        return client.sign(algorithm, digest, Context.NONE).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verify(algorithm, digest, signature, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        return client.verify(algorithm, digest, signature, context).block();
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.wrap-key}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws NullPointerException if {@code algorithm} or {@code key} is null.
     * @return The {@link KeyWrapResult} whose {@link KeyWrapResult#encryptedKey() encrypted key} contains the wrapped key result.
     */
    public KeyWrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKey(algorithm, key, Context.NONE);
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.wrap-key-Context}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws NullPointerException if {@code algorithm} or {@code key} is null.
     * @return The {@link KeyWrapResult} whose {@link KeyWrapResult#encryptedKey() encrypted key} contains the wrapped key result.
     */
    public KeyWrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        return client.wrapKey(algorithm, key, context).block();
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.unwrap-key}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws NullPointerException if {@code algorithm} or {@code encryptedKey} is null.
     * @return The unwrapped key content.
     */
    public KeyUnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return unwrapKey(algorithm, encryptedKey, Context.NONE);
    }

    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is the reverse of the wrap operation.
     * The unwrap operation supports asymmetric and symmetric keys to unwrap. This operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP} and {@link KeyWrapAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.unwrap-key-Context}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for wrap operation.
     * @throws NullPointerException if {@code algorithm} or {@code encryptedKey} is null.
     * @return The unwrapped key content.
     */
    public KeyUnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        return client.unwrapKey(algorithm, encryptedKey, context).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign-data}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     * @return A {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data) {
        return signData(algorithm, data, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.sign-data-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     * @return A {@link SignResult} whose {@link SignResult#signature() signature} contains the created signature.
     */
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        return client.signData(algorithm, data, context).block();
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
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify-data}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return verifyData(algorithm, data, signature, Context.NONE);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.cryptographyclient.verify-data-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     * @return The {@link Boolean} indicating the signature verification result.
     */
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        return client.verifyData(algorithm, data, signature, context).block();
    }

    CryptographyServiceClient getServiceClient() {
        return client.getCryptographyServiceClient();
    }
}
