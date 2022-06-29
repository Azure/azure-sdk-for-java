// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.BufferStagingArea;
import com.azure.storage.common.implementation.UploadUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_KEY_SIZE_BITS;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.EMPTY_BUFFER;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.GCM_ENCRYPTION_REGION_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

class EncryptorV2 extends Encryptor {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptorV2.class);

    protected EncryptorV2(SecretKey aesKey) {
        super(aesKey);
    }

    @Override
    byte[] getKeyToWrap() {
        try {
            /*
             * Prevent a downgrade attack by prepending the protocol version to the key (padded to 8 bytes)
             * before wrapping. "2.0\0\0\0\0\0<key>"
             */
            ByteArrayOutputStream keyStream = new ByteArrayOutputStream((AES_KEY_SIZE_BITS / 8) + 8);
            // This will always be three bytes
            keyStream.write(ENCRYPTION_PROTOCOL_V2.getBytes(StandardCharsets.UTF_8));
            // Key wrapping requires 8-byte alignment. Pad will 0s
            for (int i = 0; i < 5; i++) {
                keyStream.write(0);
            }
            keyStream.write(aesKey.getEncoded());
            return keyStream.toByteArray();
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    @Override
    protected EncryptionData buildEncryptionData(Map<String, String> keyWrappingMetadata, WrappedKey wrappedKey) {
        return super.buildEncryptionData(keyWrappingMetadata, wrappedKey)
            .setEncryptionAgent(new EncryptionAgent(ENCRYPTION_PROTOCOL_V2,
                EncryptionAlgorithm.AES_GCM_256))
            .setEncryptedRegionInfo(new EncryptedRegionInfo(
                GCM_ENCRYPTION_REGION_LENGTH, NONCE_LENGTH));
    }

    private Cipher getCipher(int index) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] iv = ByteBuffer.allocate(NONCE_LENGTH).putLong(index).array();

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
        return cipher;
    }

    @Override
    protected Flux<ByteBuffer> encrypt(Flux<ByteBuffer> plainTextFlux) {
        Flux<ByteBuffer> encryptedTextFlux;
        BufferStagingArea stagingArea =
            new BufferStagingArea(GCM_ENCRYPTION_REGION_LENGTH, GCM_ENCRYPTION_REGION_LENGTH);

        encryptedTextFlux =
            UploadUtils.chunkSource(plainTextFlux,
                    new com.azure.storage.common.ParallelTransferOptions()
                        .setBlockSizeLong((long) GCM_ENCRYPTION_REGION_LENGTH))
                .flatMapSequential(stagingArea::write)
                .concatWith(Flux.defer(stagingArea::flush))
                .index()
                .flatMapSequential(tuple -> {
                    Cipher gcmCipher;
                    try {
                        // We use the index as the nonce as a counter guarantees each nonce is used
                        // only once with a given key.
                        gcmCipher = getCipher(tuple.getT1().intValue());
                    } catch (GeneralSecurityException e) {
                        throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                    }

                    // Expected size of each encryption region after calling doFinal. Last one may
                    // be less, will never be more.
                    ByteBuffer encryptedRegion = ByteBuffer.allocate(
                        GCM_ENCRYPTION_REGION_LENGTH + TAG_LENGTH);

                    // Each flux is at most 1 BufferAggregator of 4mb
                    Flux<ByteBuffer> cipherTextWithTag = tuple.getT2()
                        .asFlux()
                        .map(buffer -> {
                            // Write into the preallocated buffer and always return this buffer.
                            try {
                                gcmCipher.update(buffer, encryptedRegion);
                            } catch (ShortBufferException e) {
                                throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                            }
                            return encryptedRegion;
                        })
                        .then(Mono.fromCallable(() -> {
                            // We have already written all the data to the cipher. Passing in a final
                            // empty buffer allows us to force completion and return the filled buffer.
                            gcmCipher.doFinal(EMPTY_BUFFER, encryptedRegion);
                            encryptedRegion.flip();
                            return encryptedRegion;
                        })).flux();

                    return Flux.concat(Flux.just(ByteBuffer.wrap(gcmCipher.getIV())),
                        cipherTextWithTag);
                }, 1, 1);
        return encryptedTextFlux;
    }
}
