// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

/**
 * The LocalCryptographyAsyncClient provides asynchronous methods to perform cryptographic operations locally using
 * asymmetric and symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify
 * operations using the configured key.
 *
 * <p><strong>Samples to construct the async client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.async.LocalCryptographyAsyncClient.instantiation}
 *
 * @see LocalCryptographyClientBuilder
 */
public class LocalCryptographyAsyncClient {
    private final CryptographyAsyncClient cryptographyAsyncClient;

    /**
     * Creates a LocalCryptographyAsyncClient for local cryptography operations.
     *
     * @param jsonWebKey the json web key to use for cryptography operations.
     */
    LocalCryptographyAsyncClient(JsonWebKey jsonWebKey) {
        cryptographyAsyncClient = new CryptographyAsyncClient(jsonWebKey, null, null);
    }

    Mono<String> getKeyId() {
        return cryptographyAsyncClient.getKeyId();
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public
     * portion of the key is used for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text}
     * contains the encrypted content.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or  {@code plaintext} is {@code null}.
     */
    public Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return cryptographyAsyncClient.encrypt(algorithm, plaintext);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public
     * portion of the key is used for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting the
     * specified {@code plaintext}. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and
     * {@link EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Encrypts the content. Subscribes to the call asynchronously and prints out the encrypted content details when
     * a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.encrypt#EncryptParameters}
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     * @return A {@link Mono} containing a {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text}
     * contains the encrypted content.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     * @throws NullPointerException if {@code encryptParameters} is {@code null}.
     */
    public Mono<EncryptResult> encrypt(EncryptParameters encryptParameters) {
        return cryptographyAsyncClient.encrypt(encryptParameters);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the
     * specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link
     * EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @return A {@link Mono} containing the decrypted blob.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     */
    public Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        return cryptographyAsyncClient.decrypt(algorithm, ciphertext);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires the
     * keys/decrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the
     * specified encrypted content. Possible values for asymmetric keys include:
     * {@link EncryptionAlgorithm#RSA1_5 RSA1_5}, {@link EncryptionAlgorithm#RSA_OAEP RSA_OAEP} and {@link
     * EncryptionAlgorithm#RSA_OAEP_256 RSA_OAEP_256}.
     *
     * Possible values for symmetric keys include: {@link EncryptionAlgorithm#A128CBC A128CBC},
     * {@link EncryptionAlgorithm#A128CBCPAD A128CBCPAD}, {@link EncryptionAlgorithm#A128CBC_HS256 A128CBC-HS256},
     * {@link EncryptionAlgorithm#A192CBC A192CBC}, {@link EncryptionAlgorithm#A192CBCPAD A192CBCPAD},
     * {@link EncryptionAlgorithm#A192CBC_HS384 A192CBC-HS384}, {@link EncryptionAlgorithm#A256CBC A256CBC},
     * {@link EncryptionAlgorithm#A256CBCPAD A256CBPAD} and {@link EncryptionAlgorithm#A256CBC_HS512 A256CBC-HS512}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Decrypts the encrypted content. Subscribes to the call asynchronously and prints out the decrypted content
     * details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.decrypt#DecryptParameters}
     *
     * @param decryptParameters The parameters to use in the decryption operation.
     * @return A {@link Mono} containing the decrypted blob.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code decryptParameters} is {@code null}.
     */
    public Mono<DecryptResult> decrypt(DecryptParameters decryptParameters) {
        return cryptographyAsyncClient.decrypt(decryptParameters);
    }


    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512} and {@link SignatureAlgorithm#ES256K ES256K}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the digest. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.sign#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     * the created signature.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code digest} is null.
     */
    public Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest) {
        return cryptographyAsyncClient.sign(algorithm, digest);
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys. In case of asymmetric keys public portion of the key is used to verify the signature . This operation
     * requires the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to verify the
     * signature. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512
     * ES512} and {@link SignatureAlgorithm#ES256K ES256K}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verify#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param signature The signature to be verified.
     * @return A {@link Mono} containing a {@link Boolean} indicating the signature verification result.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
     */
    public Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return cryptographyAsyncClient.verify(algorithm, digest, signature);
    }

    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5} and {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link
     * KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}. </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.wrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @return A {@link Mono} containing a {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted
     * key} contains the wrapped key result.
     * @throws UnsupportedOperationException if the wrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code key} is null.
     */
    public Mono<WrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return cryptographyAsyncClient.wrapKey(algorithm, key);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.unwrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @return A {@link Mono} containing a the unwrapped key content.
     * @throws UnsupportedOperationException if the unwrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code encryptedKey} is null.
     */
    public Mono<UnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return cryptographyAsyncClient.unwrapKey(algorithm, encryptedKey);
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to sign the digest.
     * Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384}, {@link SignatureAlgorithm#ES512
     * ES512}, {@link SignatureAlgorithm#ES256K ES256K}. </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.signData#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @return A {@link Mono} containing a {@link SignResult} whose {@link SignResult#getSignature() signature} contains
     * the created signature.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     */
    public Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data) {
        return cryptographyAsyncClient.signData(algorithm, data);
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
     * ES512}, {@link SignatureAlgorithm#ES256K ES256K}. </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyAsyncClient.verifyData#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @return The {@link Boolean} indicating the signature verification result.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     */
    public Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return cryptographyAsyncClient.verifyData(algorithm, data, signature);
    }
}
