// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

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
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import io.clientcore.core.http.models.RequestContext;

/**
 * The base class for all local key cryptography clients. This class provides the common functionality for
 * encrypting, decrypting, signing, and verifying data using a local key.
 */
public abstract class LocalKeyCryptographyClient {
    final CryptographyClientImpl implClient;
    final JsonWebKey jsonWebKey;

    LocalKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        this.jsonWebKey = jsonWebKey;
        this.implClient = implClient;
    }

    /**
     * Encrypts the given plaintext using the specified encryption algorithm.
     *
     * @param algorithm The encryption algorithm to use.
     * @param plaintext The plaintext to encrypt.
     * @param requestContext The request context for the operation.
     * @return The result of the encryption operation.
     */
    public abstract EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext,
        RequestContext requestContext);

    /**
     * Encrypts the given plaintext using the specified encryption parameters.
     *
     * @param encryptParameters The encryption parameters to use.
     * @param requestContext The request context for the operation.
     * @return The result of the encryption operation.
     */
    public abstract EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext);

    /**
     * Decrypts the given ciphertext using the specified decryption algorithm.
     *
     * @param algorithm The decryption algorithm to use.
     * @param plaintext The ciphertext to decrypt.
     * @param requestContext The request context for the operation.
     * @return The result of the decryption operation.
     */
    public abstract DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext,
        RequestContext requestContext);

    /**
     * Decrypts the given ciphertext using the specified decryption parameters.
     *
     * @param decryptParameters The decryption parameters to use.
     * @param requestContext The request context for the operation.
     * @return The result of the decryption operation.
     */
    public abstract DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext);

    /**
     * Signs the given digest using the specified signature algorithm.
     *
     * @param algorithm The signature algorithm to use.
     * @param digest The digest to sign.
     * @param requestContext The request context for the operation.
     * @return The result of the signing operation.
     */
    public abstract SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext);

    /**
     * Verifies the given signature using the specified signature algorithm.
     *
     * @param algorithm The signature algorithm to use.
     * @param digest The digest to verify.
     * @param signature The signature to verify.
     * @param requestContext The request context for the operation.
     * @return The result of the verification operation.
     */
    public abstract VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext);

    /**
     * Wraps the given key using the specified key wrap algorithm.
     *
     * @param algorithm The key wrap algorithm to use.
     * @param keyToWrap The key to wrap.
     * @param requestContext The request context for the operation.
     * @return The result of the wrapping operation.
     */
    public abstract WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestContext requestContext);

    /**
     * Unwraps the given key using the specified key wrap algorithm.
     *
     * @param algorithm The key wrap algorithm to use.
     * @param encryptedKey The encrypted key to unwrap.
     * @param requestContext The request context for the operation.
     * @return The result of the unwrapping operation.
     */
    public abstract UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey,
        RequestContext requestContext);

    /**
     * Signs the given data using the specified signature algorithm.
     *
     * @param algorithm The signature algorithm to use.
     * @param data The data to sign.
     * @param requestContext The request context for the operation.
     * @return The result of the signing operation.
     */
    public abstract SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext);

    /**
     * Verifies the given signature using the specified signature algorithm.
     *
     * @param algorithm The signature algorithm to use.
     * @param data The data to verify.
     * @param signature The signature to verify.
     * @param requestContext The request context for the operation.
     * @return The result of the verification operation.
     */
    public abstract VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext);

    /**
     * Gets the JSON Web Key associated with this client.
     *
     * @return The JSON Web Key.
     */
    public JsonWebKey getJsonWebKey() {
        return jsonWebKey;
    }
}
