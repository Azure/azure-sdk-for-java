// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

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
import com.azure.security.keyvault.keys.models.JsonWebKey;
import reactor.core.publisher.Mono;

abstract class LocalKeyCryptographyClient {
    CryptographyServiceClient serviceClient;

    LocalKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    LocalKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    abstract Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context, JsonWebKey jsonWebKey);

    abstract Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, Context context,
                                              JsonWebKey jsonWebKey);

    abstract Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key);

    abstract Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
                                            Context context, JsonWebKey key);

    abstract Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context,
                                           JsonWebKey jsonWebKey);

    abstract Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context,
                                               JsonWebKey jsonWebKey);

    abstract Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context, JsonWebKey key);

    abstract Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
                                                Context context, JsonWebKey key);

}
