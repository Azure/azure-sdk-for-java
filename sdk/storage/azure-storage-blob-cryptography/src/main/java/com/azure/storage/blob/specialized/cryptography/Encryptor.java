// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_KEY_SIZE_BITS;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_MODE;

abstract class Encryptor {
    private static final ClientLogger LOGGER = new ClientLogger(Encryptor.class);

    protected final SecretKey aesKey;

    protected Encryptor() throws NoSuchAlgorithmException {
        this.aesKey = generateSecretKey();
    }

    abstract byte[] getKeyToWrap();

    protected abstract Flux<ByteBuffer> encrypt(Flux<ByteBuffer> plaintext) throws GeneralSecurityException;

    protected EncryptionData buildEncryptionData(Map<String, String> keyWrappingMetadata,
        WrappedKey wrappedKey) {
        return new EncryptionData()
            .setEncryptionMode(ENCRYPTION_MODE)
            .setKeyWrappingMetadata(keyWrappingMetadata)
            .setWrappedContentKey(wrappedKey);
    }

    static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES);
        keyGen.init(AES_KEY_SIZE_BITS);

        return keyGen.generateKey();
    }

    static Encryptor getEncryptor(EncryptionVersion version) throws GeneralSecurityException {
        switch (version) {
            case V1:
                return new EncryptorV1();
            case V2:
                return new EncryptorV2();
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid encryption version: "
                    + version));
        }
    }
}
