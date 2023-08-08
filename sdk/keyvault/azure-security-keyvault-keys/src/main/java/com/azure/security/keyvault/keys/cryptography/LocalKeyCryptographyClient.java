// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
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
import reactor.core.publisher.Mono;

abstract class LocalKeyCryptographyClient {
    final CryptographyClientImpl serviceClient;

    LocalKeyCryptographyClient(CryptographyClientImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    LocalKeyCryptographyClient(JsonWebKey key, CryptographyClientImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    abstract Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey, Context context);

    abstract EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey, Context context);

    abstract Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context);

    abstract EncryptResult encrypt(EncryptParameters encryptParameters, JsonWebKey jsonWebKey, Context context);

    abstract Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey, Context context);

    abstract DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext, JsonWebKey jsonWebKey, Context context);

    abstract Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context);

    abstract DecryptResult decrypt(DecryptParameters decryptParameters, JsonWebKey jsonWebKey, Context context);

    abstract Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context);

    abstract SignResult sign(SignatureAlgorithm algorithm, byte[] digest, JsonWebKey key, Context context);

    abstract Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
                                            JsonWebKey key, Context context);

    abstract VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, JsonWebKey key,
                                 Context context);

    abstract Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey,
                                           Context context);

    abstract WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, JsonWebKey jsonWebKey, Context context);

    abstract Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey jsonWebKey,
                                    Context context);

    abstract UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, JsonWebKey jsonWebKey,
                                    Context context);

    abstract Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context);

    abstract SignResult signData(SignatureAlgorithm algorithm, byte[] data, JsonWebKey key, Context context);

    abstract Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
                                                JsonWebKey key, Context context);

    abstract VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, JsonWebKey key,
                                     Context context);

}
