// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.specialized.cryptography.EncryptionVersion;
import reactor.core.publisher.Flux;

import javax.crypto.SecretKey;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Map;


abstract class Encryptor {
    private static final ClientLogger LOGGER = new ClientLogger(Encryptor.class);

    protected final SecretKey aesKey;

    protected Encryptor(SecretKey aesKey) {
        this.aesKey = aesKey;
    }

    abstract byte[] getKeyToWrap();

    protected abstract Flux<ByteBuffer> encrypt(Flux<ByteBuffer> plaintext) throws GeneralSecurityException;

    protected EncryptionData buildEncryptionData(Map<String, String> keyWrappingMetadata,
        WrappedKey wrappedKey) {
        return new EncryptionData()
            .setEncryptionMode("Full Blob")
            .setKeyWrappingMetadata(keyWrappingMetadata)
            .setWrappedContentKey(wrappedKey);
    }

    static Encryptor getEncryptor(EncryptionVersion version, SecretKey aesKey) throws GeneralSecurityException {
        switch (version) {
            case V1:
                return new EncryptorV1(aesKey);
            case V2:
                return new EncryptorV2(aesKey);
            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Invalid encryption version: "
                    + version));
        }
    }
}
