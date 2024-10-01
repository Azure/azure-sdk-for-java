// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2_1;

abstract class Decryptor {
    private static final ClientLogger LOGGER = new ClientLogger(Decryptor.class);

    protected AsyncKeyEncryptionKeyResolver keyResolver;
    protected AsyncKeyEncryptionKey keyWrapper;
    protected EncryptionData encryptionData;

    protected Decryptor(AsyncKeyEncryptionKeyResolver keyResolver, AsyncKeyEncryptionKey keyWrapper,
        EncryptionData encryptionData) {
        this.keyResolver = keyResolver;
        this.keyWrapper = keyWrapper;
        this.encryptionData = encryptionData;
    }

    /**
     * Gets the CEK from the encryption data.
     *
     * @return The raw bytes of the encryption key.
     * @throws NullPointerException If the key resolver cannot resolve the key id from the encryption data.
     * @throws IllegalArgumentException If the configured key id does not match the key id from the encryption data.
     */
    protected Mono<byte[]> getKeyEncryptionKey() {
        /*
         * 1. Invoke the key resolver if specified to get the key. If the resolver is specified but does not have a
         * mapping for the key id, an error should be thrown. This is important for key rotation scenario.
         * 2. If resolver is not specified but a key is specified, match the key id on the key and use it.
         */
        Mono<? extends AsyncKeyEncryptionKey> keyMono;

        if (this.keyResolver != null) {
            keyMono = this.keyResolver.buildAsyncKeyEncryptionKey(encryptionData.getWrappedContentKey().getKeyId())
                .onErrorResume(NullPointerException.class, e -> {
                    /*
                     * keyResolver returns null if it cannot find the key, but Reactor throws on null values
                     * passing through workflows, so we propagate this case with an IllegalArgumentException
                     */
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                });
        } else {
            keyMono = this.keyWrapper.getKeyId().flatMap(keyId -> {
                if (encryptionData.getWrappedContentKey().getKeyId().equals(keyId)) {
                    return Mono.just(this.keyWrapper);
                } else {
                    throw LOGGER.logExceptionAsError(Exceptions.propagate(new IllegalArgumentException("Key mismatch. "
                        + "The key id stored on the service does not match the specified key.")));
                }
            });
        }

        return keyMono.flatMap(keyEncryptionKey -> keyEncryptionKey.unwrapKey(
            encryptionData.getWrappedContentKey().getAlgorithm(),
            encryptionData.getWrappedContentKey().getEncryptedKey()
        ));
    }

    /**
     * Gets a cipher able to perform decryption for the specified version.
     *
     * @param contentEncryptionKey The CEK.
     * @param iv The iv.
     * @param padding Whether the ciphertext is padded
     * @return {@link Cipher}
     * @throws InvalidKeyException If the key is invalid.
     */
    protected abstract Cipher getCipher(byte[] contentEncryptionKey, byte[] iv, boolean padding)
        throws InvalidKeyException;

    abstract Flux<ByteBuffer> decrypt(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange,
        boolean padding, String requestUri, AtomicLong totalInputBytes, byte[] contentEncryptionKey);

    static Decryptor getDecryptor(AsyncKeyEncryptionKeyResolver keyResolver,
        AsyncKeyEncryptionKey keyWrapper, EncryptionData encryptionData) {
        if (encryptionData == null) {
            return new NoOpDecryptor(keyResolver, keyWrapper, null);
        }
        switch (encryptionData.getEncryptionAgent().getProtocol()) {
            case ENCRYPTION_PROTOCOL_V1:
                return new DecryptorV1(keyResolver, keyWrapper, encryptionData);
            case ENCRYPTION_PROTOCOL_V2:
            case ENCRYPTION_PROTOCOL_V2_1:
                return new DecryptorV2(keyResolver, keyWrapper, encryptionData);
            default:
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Encryption protocol not recognized: "
                        + encryptionData.getEncryptionAgent().getProtocol()));
        }
    }
}
