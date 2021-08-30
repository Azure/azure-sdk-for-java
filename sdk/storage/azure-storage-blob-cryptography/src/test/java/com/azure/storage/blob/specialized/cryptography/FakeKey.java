// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import reactor.core.publisher.Mono;

/**
 * Extremely basic key resolver to test client side encryption
 */
public final class FakeKey implements AsyncKeyEncryptionKey, IKey {

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

    @Override
    public String getDefaultEncryptionAlgorithm() {
        return null;
    }

    @Override
    public String getDefaultKeyWrapAlgorithm() {
        return null;
    }

    @Override
    public String getDefaultSignatureAlgorithm() {
        return null;
    }

    @Override
    public String getKid() {
        return keyId;
    }

    @Override
    public ListenableFuture<byte[]> decryptAsync(byte[] bytes, byte[] bytes1, byte[] bytes2, byte[] bytes3, String s) {
        return null;
    }

    @Override
    public ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(byte[] bytes, byte[] bytes1, byte[] bytes2,
        String s) {
        return null;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ListenableFuture<Pair<byte[], String>> wrapKeyAsync(byte[] bytes, String s) {
        return Futures.immediateFuture(Pair.of(xor(bytes, randomData), s));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public ListenableFuture<byte[]> unwrapKeyAsync(byte[] bytes, String s) {
        return Futures.immediateFuture(xor(bytes, randomData));
    }

    @Override
    public ListenableFuture<Pair<byte[], String>> signAsync(byte[] bytes, String s) {
        return null;
    }

    @Override
    public ListenableFuture<Boolean> verifyAsync(byte[] bytes, byte[] bytes1, String s) {
        return null;
    }

    @Override
    public void close() {

    }
}
