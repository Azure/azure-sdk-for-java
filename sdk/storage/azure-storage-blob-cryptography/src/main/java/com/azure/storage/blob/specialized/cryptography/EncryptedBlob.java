// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

final class EncryptedBlob {

    /**
     * {@link EncryptionData} to decrypt EncryptedBlob
     */
    private final EncryptionData encryptionData;

    /**
     * The encrypted blob content as a Flowable ByteBuffer
     */
    private final Flux<ByteBuffer> ciphertextFlux;

    /**
     * Creates a new EncryptedBlob with given EncryptedData and Flowable ByteBuffer
     *
     * @param encryptionData A {@link EncryptionData}
     * @param ciphertextFlux A Flowable ByteBuffer
     */
    EncryptedBlob(EncryptionData encryptionData, Flux<ByteBuffer> ciphertextFlux) {
        this.encryptionData = encryptionData;
        this.ciphertextFlux = ciphertextFlux;
    }

    /**
     * @return This EncryptedBlob's EncryptedData.
     */
    EncryptionData getEncryptionData() {
        return this.encryptionData;
    }

    /**
     * @return This EncryptedBlob's Flowable ByteBuffer.
     */
    Flux<ByteBuffer> getCiphertextFlux() {
        return this.ciphertextFlux;
    }
}
