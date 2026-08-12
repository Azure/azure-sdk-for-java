// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.core.cryptography.AsyncKeyEncryptionKeyResolver;
import com.azure.core.util.CoreUtils;
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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_KEY_SIZE_BITS;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_ENV_VAR;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.EMPTY_BUFFER;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

class DecryptorV2 extends Decryptor {
    private static final ClientLogger LOGGER = new ClientLogger(DecryptorV2.class);

    protected DecryptorV2(AsyncKeyEncryptionKeyResolver keyResolver, AsyncKeyEncryptionKey keyWrapper,
        EncryptionData encryptionData) {
        super(keyResolver, keyWrapper, encryptionData);
    }

    @Override
    Flux<ByteBuffer> decrypt(Flux<ByteBuffer> encryptedFlux, EncryptedBlobRange encryptedBlobRange, boolean padding,
        String requestUri, AtomicLong totalInputBytes, byte[] contentEncryptionKey) {
        // Buffer an exact region with the nonce and tag
        final int authenticatedRegionDataLength = (int) encryptionData.getEncryptedRegionInfo().getDataLength();
        final int nonceLength = encryptionData.getEncryptedRegionInfo().getNonceLength();
        BufferStagingArea stagingArea = new BufferStagingArea(authenticatedRegionDataLength + TAG_LENGTH + nonceLength,
            authenticatedRegionDataLength + TAG_LENGTH + nonceLength);

        /*
         * Each CSEv2 region is encrypted under a unique, sequential nonce equal to the region's index (see
         * EncryptorV2). Because the nonce is stored alongside the ciphertext, individual regions of otherwise
         * untampered ciphertext can be rearranged without invalidating any single region's authentication tag,
         * silently corrupting the decrypted plaintext. Detect this by asserting that the nonce of each region matches
         * its expected sequential value. The first downloaded region depends on the requested range. This behavior can
         * be disabled for data recovery via a compatibility switch.
         */
        final boolean detectRegionReorder = !cseV2AllowMisorderedAuthRegions();
        final long initialRegion = authenticatedRegionDataLength == 0
            ? 0
            : encryptedBlobRange.getOriginalRange().getOffset() / authenticatedRegionDataLength;

        return encryptedFlux.flatMapSequential(stagingArea::write, 1, 1)
            .concatWith(Flux.defer(stagingArea::flush))
            .index()
            .flatMapSequential(indexedAggregator -> {
                // Get the IV out of the beginning of the aggregator
                byte[] gmcIv = indexedAggregator.getT2().getFirstNBytes(nonceLength);

                if (detectRegionReorder) {
                    long expectedRegion = initialRegion + indexedAggregator.getT1();
                    RuntimeException reorderError = validateRegionNonce(gmcIv, nonceLength, expectedRegion);
                    if (reorderError != null) {
                        return Mono.error(reorderError);
                    }
                }

                Cipher gmcCipher;
                try {
                    gmcCipher = getCipher(contentEncryptionKey, gmcIv, false);
                } catch (InvalidKeyException e) {
                    return Mono.error(LOGGER.logExceptionAsError(Exceptions.propagate(e)));
                }

                ByteBuffer decryptedRegion = ByteBuffer.allocate(authenticatedRegionDataLength);
                return indexedAggregator.getT2().asFlux().map(buffer -> {
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

    /**
     * Validates that the nonce of an authenticated region matches the nonce that would have been produced for the
     * expected sequential region index during encryption. A mismatch indicates that the region has been reordered or
     * otherwise tampered with.
     *
     * @param actualNonce The nonce read from the downloaded region.
     * @param nonceLength The length of the nonce.
     * @param expectedRegion The expected 0-based region index.
     * @return A {@link RuntimeException} describing the tampering if the nonce is out of order, or {@code null} if the
     * nonce is valid.
     */
    private RuntimeException validateRegionNonce(byte[] actualNonce, int nonceLength, long expectedRegion) {
        // Cannot reconstruct the expected nonce if it is too short to hold the region index. This should never happen
        // for CSEv2 (nonce length is 12), so treat it as unverifiable rather than a failure.
        if (nonceLength < Long.BYTES || actualNonce.length < Long.BYTES) {
            return null;
        }

        // Reconstruct the nonce exactly as EncryptorV2 does: an 8-byte big-endian region index followed by zero
        // padding to the nonce length.
        byte[] expectedNonce = ByteBuffer.allocate(nonceLength).putLong(expectedRegion).array();
        if (Arrays.equals(expectedNonce, actualNonce)) {
            return null;
        }

        long actualRegion = ByteBuffer.wrap(actualNonce).getLong();
        return LOGGER.logExceptionAsError(new IllegalStateException(
            "Encountered an out-of-order authenticated region while decrypting client-side encrypted (v2) content. "
                + "This may indicate that the blob's authenticated regions have been rearranged or otherwise tampered "
                + "with. Expected region " + expectedRegion + " but found region " + actualRegion + ". To recover data "
                + "from an affected blob, set the \"" + CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_ENV_VAR
                + "\" environment variable (or the \"" + CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME
                + "\" system property) to \"true\"."));
    }

    /**
     * Whether detection of reordered client-side encryption v2 authenticated regions should be disabled.
     * <p>
     * This is a data-recovery escape hatch, read live from a system property or environment variable. When enabled, the
     * client will not throw when it encounters authenticated regions that appear to have been rearranged, allowing
     * (potentially tampered) plaintext to be recovered.
     * <p>
     * {@link com.azure.core.util.Configuration} is intentionally not used here because the global configuration caches
     * the first value it reads for a given name, which would prevent the switch from being honored if it is set after
     * the first read.
     *
     * @return {@code true} if reordered authenticated regions should be allowed, {@code false} otherwise.
     */
    private static boolean cseV2AllowMisorderedAuthRegions() {
        String value = System.getProperty(CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME);
        if (CoreUtils.isNullOrEmpty(value)) {
            value = System.getenv(CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_ENV_VAR);
        }
        return Boolean.parseBoolean(value);
    }

    @Override
    protected Mono<byte[]> getKeyEncryptionKey() {
        return super.getKeyEncryptionKey().flatMap(keyBytes -> {
            /*
             * Reverse the process in EncryptedBlobAsyncClient. The first three bytes of the unwrapped key
             * are the protocol version. Verify its integrity.
             */
            ByteArrayInputStream keyStream = new ByteArrayInputStream(keyBytes);
            byte[] protocolBytes = new byte[3];
            try {
                keyStream.read(protocolBytes);
                if (ByteBuffer.wrap(encryptionData.getEncryptionAgent().getProtocol().getBytes(StandardCharsets.UTF_8))
                    .compareTo(ByteBuffer.wrap(protocolBytes)) != 0) {
                    return Mono.error(LOGGER.logExceptionAsError(
                        new IllegalStateException("Padded wrapped key did not match protocol version")));
                }
                // Ignore the next five bytes that were used as padding to 8-byte align
                for (int i = 0; i < 5; i++) {
                    keyStream.read();
                }
                if (keyStream.available() != (AES_KEY_SIZE_BITS / 8)) {
                    return Mono.error(LOGGER
                        .logExceptionAsError(new IllegalStateException("Wrapped key bytes were incorrect length")));
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
    protected Cipher getCipher(byte[] contentEncryptionKey, byte[] iv, boolean padding) throws InvalidKeyException {
        SecretKey keySpec = new SecretKeySpec(contentEncryptionKey, 0, contentEncryptionKey.length, AES);
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, new GCMParameterSpec(TAG_LENGTH * 8, iv));
            return cipher;
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(e));
        }
    }
}
