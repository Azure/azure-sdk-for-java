// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.BufferStagingArea;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_KEY_SIZE_BITS;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.EMPTY_BUFFER;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

class DecryptorV2 extends Decryptor {
    private static final ClientLogger LOGGER = new ClientLogger(DecryptorV2.class);

    protected DecryptorV2(AsyncKeyEncryptionKeyResolver keyResolver, AsyncKeyEncryptionKey keyWrapper,
        EncryptionData encryptionData) {
        super(keyResolver, keyWrapper, encryptionData);
    }

    @Override
    Flux<ByteBuffer> decrypt(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange,
        boolean padding, String requestUri, AtomicLong totalInputBytes, byte[] contentEncryptionKey) {
        // Buffer an exact region with the nonce and tag
        final int gcmEncryptionRegionLength = encryptionData.getEncryptedRegionInfo().getDataLength();
        final int nonceLength = encryptionData.getEncryptedRegionInfo().getNonceLength();
        BufferStagingArea stagingArea =
            new BufferStagingArea(gcmEncryptionRegionLength + TAG_LENGTH + nonceLength,
                gcmEncryptionRegionLength + TAG_LENGTH + nonceLength);

        return encryptedFlux
            .flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .flatMapSequential(aggregator -> {
                // Get the IV out of the beginning of the aggregator
                byte[] gmcIv = aggregator.getFirstNBytes(nonceLength);

                Cipher gmcCipher;
                try {
                    gmcCipher = getCipher(contentEncryptionKey, gmcIv, false);
                } catch (InvalidKeyException e) {
                    return Mono.error(LOGGER.logExceptionAsError(Exceptions.propagate(e)));
                }

                ByteBuffer decryptedRegion = ByteBuffer.allocate(gcmEncryptionRegionLength);
                return aggregator.asFlux()
                    .map(buffer -> {
                        // Write into the preallocated buffer and always return this buffer.
                        try {
                            gmcCipher.update(buffer, decryptedRegion);
                        } catch (ShortBufferException e) {
                            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                        }
                        return decryptedRegion;
                    }).then(Mono.fromCallable(() -> {
                        // We have already written all the data to the cipher. Passing in a final
                        // empty buffer allows us to force completion and return the filled buffer.
                        gmcCipher.doFinal(EMPTY_BUFFER, decryptedRegion);
                        decryptedRegion.flip();
                        return decryptedRegion;
                    })).flux();
            });
    }

    @Override
    protected Mono<byte[]> getKeyEncryptionKey() {
        return super.getKeyEncryptionKey()
            .flatMap(keyBytes -> {
                /*
                 * Reverse the process in EncryptedBlobAsyncClient. The first three bytes of the unwrapped key
                 * are the protocol version. Verify its integrity.
                 */
                ByteArrayInputStream keyStream = new ByteArrayInputStream(keyBytes);
                byte[] protocolBytes = new byte[3];
                try {
                    keyStream.read(protocolBytes);
                    if (ByteBuffer.wrap(ENCRYPTION_PROTOCOL_V2.getBytes(StandardCharsets.UTF_8))
                        .compareTo(ByteBuffer.wrap(protocolBytes)) != 0) {
                        return Mono.error(LOGGER.logExceptionAsError(
                            new IllegalStateException("Padded wrapped key did not match protocol version")));
                    }
                    // Ignore the next five bytes that were used as padding to 8-byte align
                    for (int i = 0; i < 5; i++) {
                        keyStream.read();
                    }
                    if (keyStream.available() != (AES_KEY_SIZE_BITS / 8)) {
                        return Mono.error(LOGGER.logExceptionAsError(
                            new IllegalStateException("Wrapped key bytes were incorrect length")));
                    }
                    byte[] strippedKeyBytes = new byte[AES_KEY_SIZE_BITS / 8];
                    // The remaining bytes are the key
                    keyStream.read(strippedKeyBytes);
                    return Mono.just(strippedKeyBytes);
                } catch (IOException e) {
                    return Mono.error(LOGGER.logThrowableAsError(e));
                }
            });
    }

    @Override
    protected Cipher getCipher(byte[] contentEncryptionKey, byte[] iv, boolean padding)
        throws InvalidKeyException {
        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length,
            AES);
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH * 8, iv));
            return cipher;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
    }
}
