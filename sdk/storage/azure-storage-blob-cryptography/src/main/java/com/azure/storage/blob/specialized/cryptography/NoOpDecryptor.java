// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.concurrent.atomic.AtomicLong;

class NoOpDecryptor extends Decryptor {

    protected NoOpDecryptor(AsyncKeyEncryptionKeyResolver keyResolver, AsyncKeyEncryptionKey keyWrapper, EncryptionData encryptionData) {
        super(keyResolver, keyWrapper, encryptionData);
    }

    @Override
    protected Cipher getCipher(byte[] contentEncryptionKey, byte[] iv, boolean padding) throws InvalidKeyException {
        return null;
    }

    @Override
    Flux<ByteBuffer> decrypt(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange, boolean padding,
        String requestUri, AtomicLong totalInputBytes, byte[] contentEncryptionKey) {
        return encryptedFlux;
    }

    @Override
    protected Mono<byte[]> getKeyEncryptionKey() {
        return Mono.just(new byte[0]);
    }
}
