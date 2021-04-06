// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
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
import reactor.core.publisher.Mono;


/**
 * The {@link CryptographyClient} provides synchronous methods to perform cryptographic operations using asymmetric and
 * symmetric keys. The client supports encrypt, decrypt, wrap key, unwrap key, sign and verify operations using the
 * configured key.
 *
 * <p><strong>Samples to construct the sync client</strong></p>
 *
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.instantiation}
 * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.withJsonWebKey.instantiation}
 *
 * @see CryptographyClientBuilder
 */
@ServiceClient(builder = CryptographyClientBuilder.class, serviceInterfaces = CryptographyService.class)
public class CryptographyClient {
    private final CryptographyAsyncClient client;

    /**
     * Creates a {@link CryptographyClient} that uses a given {@link HttpPipeline pipeline} to service requests.
     *
     * @param client The {@link CryptographyAsyncClient} that the client routes its request through.
     */
    CryptographyClient(CryptographyAsyncClient client) {
        this.client = client;
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.getKey}
     *
     * @return A {@link Mono} containing the requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultKey getKey() {
        return getKeyWithResponse(Context.NONE).getValue();
    }

    /**
     * Gets the public part of the configured key. The get key operation is applicable to all key types and it requires
     * the {@code keys/get} permission for non-local operations.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets the configured key in the client. Subscribes to the call asynchronously and prints out the returned key
     * details when a response has been received.</p>
     *
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.getKeyWithResponse#Context}
     *
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * requested {@link KeyVaultKey key}.
     *
     * @throws ResourceNotFoundException When the configured key doesn't exist in the key vault.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultKey> getKeyWithResponse(Context context) {
        return client.getKeyWithResponse(context).block();
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports a
     * single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting
     * the specified {@code plaintext}. Possible values for asymmetric keys include:
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     *
     * @return The {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text} contains the encrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return encrypt(algorithm, plaintext, Context.NONE);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting
     * the specified {@code plaintext}. Possible values for asymmetric keys include:
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptionAlgorithm-byte-Context}
     *
     * @param algorithm The algorithm to be used for encryption.
     * @param plaintext The content to be encrypted.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return The {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text} contains the encrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        return encrypt(new EncryptParameters(algorithm, plaintext, null, null), context);
    }

    /**
     * Encrypts an arbitrary sequence of bytes using the configured key. Note that the encrypt operation only supports
     * a single block of data, the size of which is dependent on the target key and the encryption algorithm to be used.
     * The encrypt operation is supported for both symmetric keys and asymmetric keys. In case of asymmetric keys, the
     * public portion of the key is used for encryption. This operation requires the {@code keys/encrypt} permission
     * for non-local operations.
     *
     * <p>The {@link EncryptionAlgorithm encryption algorithm} indicates the type of algorithm to use for encrypting
     * the specified {@code plaintext}. Possible values for asymmetric keys include:
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.encrypt#EncryptParameters-Context}
     *
     * @param encryptParameters The parameters to use in the encryption operation.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return The {@link EncryptResult} whose {@link EncryptResult#getCipherText() cipher text} contains the encrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code plaintext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the encrypt operation is not supported or configured on the key.
     */
    public EncryptResult encrypt(EncryptParameters encryptParameters, Context context) {
        return client.encrypt(encryptParameters, context).block();
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     *
     * @return The {@link DecryptResult} whose {@link DecryptResult#getPlainText() plain text} contains the decrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext) {
        return decrypt(new DecryptParameters(algorithm, ciphertext, null, null, null), Context.NONE);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#EncryptionAlgorithm-byte-Context}
     *
     * @param algorithm The algorithm to be used for decryption.
     * @param ciphertext The content to be decrypted.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return The {@link DecryptResult} whose {@link DecryptResult#getPlainText() plain text} contains the decrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        return decrypt(new DecryptParameters(algorithm, ciphertext, null, null, null), context);
    }

    /**
     * Decrypts a single block of encrypted data using the configured key and specified algorithm. Note that only a
     * single block of data may be decrypted, the size of this block is dependent on the target key and the algorithm to
     * be used. The decrypt operation is supported for both asymmetric and symmetric keys. This operation requires
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.decrypt#DecryptParameters-Context}
     *
     * @param decryptParameters The parameters to use in the decryption operation.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return The {@link DecryptResult} whose {@link DecryptResult#getPlainText() plain text} contains the decrypted
     * content.
     *
     * @throws NullPointerException If {@code algorithm} or {@code ciphertext} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for decryption.
     * @throws UnsupportedOperationException If the decrypt operation is not supported or configured on the key.
     */
    public DecryptResult decrypt(DecryptParameters decryptParameters, Context context) {
        return client.decrypt(decryptParameters, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     *
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code digest} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        return client.sign(algorithm, digest, Context.NONE).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.sign#SignatureAlgorithm-byte-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature is to be created.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     *
     * @throws NullPointerException If {@code algorithm} or {@code digest} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for signing.
     * @throws UnsupportedOperationException If the sign operation is not supported or configured on the key.
     */
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        return client.sign(algorithm, digest, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     *
     * @return A {@link VerifyResult} {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code digest} or {@code signature} is null.
     */
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verify(algorithm, digest, signature, Context.NONE);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.verify#SignatureAlgorithm-byte-byte-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param digest The content from which signature was created.
     * @param signature The signature to be verified.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return A {@link VerifyResult} {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException If {@code algorithm}, {@code digest} or {@code signature} is {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for verifying.
     * @throws UnsupportedOperationException If the verify operation is not supported or configured on the key.
     */
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        return client.verify(algorithm, digest, signature, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     *
     * @return The {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped
     * key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code key} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKey(algorithm, key, Context.NONE);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.wrapKey#KeyWrapAlgorithm-byte-Context}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param key The key content to be wrapped.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return The {@link WrapResult} whose {@link WrapResult#getEncryptedKey() encrypted key} contains the wrapped
     * key result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code key} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for encryption.
     * @throws UnsupportedOperationException If the wrap operation is not supported or configured on the key.
     */
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        return client.wrapKey(algorithm, key, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     *
     * @return An {@link UnwrapResult} whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key
     * result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code encryptedKey} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return unwrapKey(algorithm, encryptedKey, Context.NONE);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.unwrapKey#KeyWrapAlgorithm-byte-Context}
     *
     * @param algorithm The encryption algorithm to use for wrapping the key.
     * @param encryptedKey The encrypted key content to unwrap.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return An {@link UnwrapResult} whose {@link UnwrapResult#getKey() decrypted key} contains the unwrapped key
     * result.
     *
     * @throws NullPointerException If {@code algorithm} or {@code encryptedKey} are {@code null}.
     * @throws ResourceNotFoundException If the key cannot be found for wrap operation.
     * @throws UnsupportedOperationException If the unwrap operation is not supported or configured on the key.
     */
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        return client.unwrapKey(algorithm, encryptedKey, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     *
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     *
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     */
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data) {
        return signData(algorithm, data, Context.NONE);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.signData#SignatureAlgorithm-byte-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The content from which signature is to be created.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return A {@link SignResult} whose {@link SignResult#getSignature() signature} contains the created signature.
     *
     * @throws NullPointerException if {@code algorithm} or {@code data} is null.
     * @throws ResourceNotFoundException if the key cannot be found for signing.
     * @throws UnsupportedOperationException if the sign operation is not supported or configured on the key.
     */
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        return client.signData(algorithm, data, context).block();
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     *
     * @return A {@link VerifyResult} {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     */
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature) {
        return verifyData(algorithm, data, signature, Context.NONE);
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
     * {@codesnippet com.azure.security.keyvault.keys.cryptography.CryptographyClient.verifyData#SignatureAlgorithm-byte-byte-Context}
     *
     * @param algorithm The algorithm to use for signing.
     * @param data The raw content against which signature is to be verified.
     * @param signature The signature to be verified.
     * @param context Additional context that is passed through the {@link HttpPipeline} during the service call.
     *
     * @return A {@link VerifyResult} {@link VerifyResult#isValid() indicating the signature verification result}.
     *
     * @throws NullPointerException if {@code algorithm}, {@code data} or {@code signature} is null.
     * @throws ResourceNotFoundException if the key cannot be found for verifying.
     * @throws UnsupportedOperationException if the verify operation is not supported or configured on the key.
     */
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        return client.verifyData(algorithm, data, signature, context).block();
    }

    CryptographyServiceClient getServiceClient() {
        return client.getCryptographyServiceClient();
    }
}
