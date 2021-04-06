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


/**
 * The LocalCryptographyClient provides synchronous methods to perform cryptographic operations using asymmetric and
 * symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the
 * configured key.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.instantiation}
 *
 * @see LocalCryptographyClientBuilder
 */
public class LocalCryptographyClient {
    private final LocalCryptographyAsyncClient client;

    /**
     * Creates a LocalCryptographyClient for local cryptography operations.
     *
     * @param client The {@link LocalCryptographyAsyncClient} that the client routes its request through.
     */
    LocalCryptographyClient(LocalCryptographyAsyncClient client) {
        this.client = client;
    }


    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public
     * portion of the key is used for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the
     * specified encrypted content. Possible values for asymmetric keys include:
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.encrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @return The {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text} contains the encrypted
     *     content.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code decryptOptions} or {@code plaintext} is {@code null}.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return client.encrypt(algorithm, plaintext).block();
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys public
     * portion of the key is used for encryption. This operation requires the keys/encrypt permission.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for decrypting the
     * specified encrypted content. Possible values for asymmetric keys include:
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.encrypt#EncryptParameters}
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     * @return The {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text} contains the encrypted
     *     content.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code encryptParameters} is {@code null}.
     */
    public EncryptResult encrypt(EncryptParameters encryptParameters) {
        return client.encrypt(encryptParameters).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.decrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @return The decrypted blob.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} is {@code null}.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        return client.decrypt(algorithm, ciphertext).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.decrypt#DecryptParameters}
     *
     * @param decryptParameters The parameters to use in the decryption operation.
     * @return The decrypted blob.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     * @throws NullPointerException If {@code decryptParameters} is {@code null}.
     */
    public DecryptResult decrypt(DecryptParameters decryptParameters) {
        return client.decrypt(decryptParameters).block();
    }

    /**
     * Creates a signature from a digest using the configured key. The sign operation supports both asymmetric and
     * symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Sings the digest. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.sign#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code digest} is null.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        return client.sign(algorithm, digest).block();
    }

    /**
     * Verifies a signature using the configured key. The verify operation supports both symmetric keys and asymmetric
     * keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires
     * the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K} </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the specified digest. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verify#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @return The {@link Boolean} indicating the signature verification result.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
     */
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return client.verify(algorithm, digest, signature).block();
    }


    /**
     * Wraps a symmetric key using the configured key. The wrap operation supports wrapping a symmetric key with both
     * symmetric and asymmetric keys. This operation requires the keys/wrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5}, {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP}
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW},
     * {@link KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Wraps the key content. Subscribes to the call asynchronously and prints out the wrapped key details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.wrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped
     * @return The {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped
     *     key result.
     * @throws UnsupportedOperationException if the wrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code key} is null.
     */
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return client.wrapKey(algorithm, key).block();
    }


    /**
     * Unwraps a symmetric key using the configured key that was initially used for wrapping that key. This operation is
     * the reverse of the wrap operation. The unwrap operation supports asymmetric and symmetric keys to unwrap. This
     * operation requires the keys/unwrapKey permission.
     *
     * <p>The {@link KeyWrapAlgorithm wrap algorithm} indicates the type of algorithm to use for wrapping the specified
     * key content. Possible values for asymmetric keys include:
     * {@link KeyWrapAlgorithm#RSA1_5 RSA1_5} and  {@link KeyWrapAlgorithm#RSA_OAEP RSA_OAEP}.
     * Possible values for symmetric keys include: {@link KeyWrapAlgorithm#A128KW A128KW}, {@link
     * KeyWrapAlgorithm#A192KW A192KW} and {@link KeyWrapAlgorithm#A256KW A256KW}</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Unwraps the key content. Subscribes to the call asynchronously and prints out the unwrapped key details when a
     * response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.unwrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @return The unwrapped key content.
     * @throws UnsupportedOperationException if the unwrap operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code encryptedKey} is null.
     */
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return client.unwrapKey(algorithm, encryptedKey).block();
    }

    /**
     * Creates a signature from the raw data using the configured key. The sign data operation supports both asymmetric
     * and symmetric keys. This operation requires the keys/sign permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K}.</p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Signs the raw data. Subscribes to the call asynchronously and prints out the signature details when a response
     * has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.signData#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     */
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data) {
        return client.signData(algorithm, data).block();
    }


    /**
     * Verifies a signature against the raw data using the configured key. The verify operation supports both symmetric
     * keys and asymmetric keys.
     * In case of asymmetric keys public portion of the key is used to verify the signature . This operation requires
     * the keys/verify permission.
     *
     * <p>The {@link SignatureAlgorithm signature algorithm} indicates the type of algorithm to use to create the
     * signature from the digest. Possible values include:
     * {@link SignatureAlgorithm#ES256 ES256}, {@link SignatureAlgorithm#ES384 E384},
     * {@link SignatureAlgorithm#ES512 ES512}, {@link SignatureAlgorithm#ES256K ES246K}. </p>
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Verifies the signature against the raw data. Subscribes to the call asynchronously and prints out the
     * verification details when a response has been received.</p>
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.LocalCryptographyClient.verifyData#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @return The {@link VerifyResult} holding the signature verification result.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     */
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return client.verifyData(algorithm, data, signature).block();
    }
}
