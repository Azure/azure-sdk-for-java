// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import reactor.core.publisher.Mono;

/**
 * Extremely basic key resolver to test client side encryption
 */
public final class FakeKey implements AsyncKeyEncryptionKey {

    private final String keyId;
    private final byte[] randomData;

    FakeKey(String keyId, byte[] randomData) {
        this.keyId = keyId;
        this.randomData = randomData;
    }

    @Override
    public Mono<String> getKeyId() {
        return Mono.just(keyId);
    }

    @Override
    public Mono<byte[]> wrapKey(String algorithm, byte[] key) {
        return Mono.just(xor(key, randomData));
    }

    @Override
    public Mono<byte[]> unwrapKey(String algorithm, byte[] encryptedKey) {
        return Mono.just(xor(encryptedKey, randomData));
    }

    private byte[] xor(byte[] arr1, byte[] arr2) {
        byte[] ret = new byte[arr1.length];
        for (int i = 0; i < arr1.length; i++) {
            ret[i] = (byte) (arr1[i] ^ arr2[i]);
        }
        return ret;
    }
}
