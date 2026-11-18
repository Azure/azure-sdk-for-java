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
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

class EncryptorV2 extends Encryptor {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptorV2.class);
    private final BlobClientSideEncryptionOptions encryptionOptions;
    private final String encryptionProtocol;

    protected EncryptorV2(SecretKey aesKey, BlobClientSideEncryptionOptions encryptionOptions,
        String encryptionProtocol) {
        super(aesKey);
        this.encryptionOptions = encryptionOptions;
        this.encryptionProtocol = encryptionProtocol;
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
            keyStream.write(encryptionProtocol.getBytes(StandardCharsets.UTF_8));
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
            .setEncryptionAgent(new EncryptionAgent(encryptionProtocol, EncryptionAlgorithm.AES_GCM_256))
            .setEncryptedRegionInfo(
                new EncryptedRegionInfo(encryptionOptions.getAuthenticatedRegionDataLengthInBytes(), NONCE_LENGTH));
    }

    /**
     * Computes the {@link CryptographyConstants#NONCE_LENGTH}-byte GCM nonce for a CSEv2 authenticated region from its
     * zero-based sequential index. The index is written as an 8-byte big-endian value into the leading bytes of the
     * nonce and the remaining bytes are left zero. The full 64-bit index is used (rather than a truncated 32-bit value)
     * so that every region within a blob is guaranteed a unique nonce, which AES-GCM requires to remain secure.
     *
     * @param index The zero-based region index.
     * @return The nonce bytes for the region.
     */
    static byte[] computeRegionNonce(long index) {
        return ByteBuffer.allocate(NONCE_LENGTH).putLong(index).array();
    }

    Cipher getCipher(long index) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        byte[] iv = computeRegionNonce(index);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(TAG_LENGTH * 8, iv));
        return cipher;
    }

    @Override
    protected Flux<ByteBuffer> encrypt(Flux<ByteBuffer> plainTextFlux) {
        Flux<ByteBuffer> encryptedTextFlux;
        long authenticatedRegionDataLength = encryptionOptions.getAuthenticatedRegionDataLengthInBytes();
        BufferStagingArea stagingArea
            = new BufferStagingArea(authenticatedRegionDataLength, authenticatedRegionDataLength);

        encryptedTextFlux
            = UploadUtils
                .chunkSource(plainTextFlux,
                    new com.azure.storage.common.ParallelTransferOptions()
                        .setBlockSizeLong(authenticatedRegionDataLength))
                .flatMapSequential(stagingArea::write, 1, 1)
                .concatWith(Flux.defer(stagingArea::flush))
                .index()
                .flatMapSequential(tuple -> {
                    Cipher gcmCipher;
                    try {
                        // We use the full 64-bit region index as the nonce counter so that each nonce is used only
                        // once with a given key. Truncating the index to 32 bits (it would widen back to a long here
                        // via primitive widening) would break AES-GCM security in two ways once a blob grows large:
                        //  1. Nonces would repeat every 2^32 regions, since region N and region N + 2^32 would collide
                        //     - GCM nonce reuse under a single key.
                        //  2. Half of the truncated indices would be negative. Every other 2^31-sized band (the ranges
                        //     [2^31, 2^32), [3*2^31, 2^33), and so on, alternating up to 2^63) has bit 31 set, so those
                        //     indices sign-extend when widened from 32 to 64 bits, producing nonces with a leading
                        //     0xFFFFFFFF prefix rather than 0x00000000 in computeRegionNonce().
                        gcmCipher = getCipher(tuple.getT1());
                    } catch (GeneralSecurityException e) {
                        throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                    }

                    // Expected size of each encryption region after calling doFinal. Last one may
                    // be less, will never be more.
                    ByteBuffer encryptedRegion = ByteBuffer.allocate((int) authenticatedRegionDataLength + TAG_LENGTH);

                    // Each flux is at most 1 BufferAggregator of 4mb
                    Flux<ByteBuffer> cipherTextWithTag = tuple.getT2().asFlux().map(buffer -> {
                        // Write into the preallocated buffer and always return this buffer.
                        try {
                            gcmCipher.update(buffer, encryptedRegion);
                        } catch (ShortBufferException e) {
                            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
                        }
                        return encryptedRegion;
                    }).then(Mono.fromCallable(() -> {
                        // We have already written all the data to the cipher. Passing in a final
                        // empty buffer allows us to force completion and return the filled buffer.
                        gcmCipher.doFinal(EMPTY_BUFFER, encryptedRegion);
                        encryptedRegion.flip();
                        return encryptedRegion;
                    })).flux();

                    return Flux.concat(Flux.just(ByteBuffer.wrap(gcmCipher.getIV())), cipherTextWithTag);
                }, 1, 1);
        return encryptedTextFlux;
    }
}
