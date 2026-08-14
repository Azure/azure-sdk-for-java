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
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_KEY_SIZE_BITS;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_ENV_VAR;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.EMPTY_BUFFER;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

class DecryptorV2 extends Decryptor {
    private static final ClientLogger LOGGER = new ClientLogger(DecryptorV2.class);

    /*
     * EncryptorV2 truncates the region index to an int when producing each region's nonce (see EncryptorV2.getCipher),
     * so nonces are unique only for the first 2^32 regions. At or beyond this index the nonce repeats: two distinct
     * regions can share a nonce and each retain a valid GCM tag. That both defeats sequential reorder detection and is
     * GCM nonce reuse, so integrity cannot be verified past this boundary.
     */
    private static final long NONCE_WRAP_REGION_COUNT = 1L << 32;

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
         * Each CSEv2 region is encrypted under a unique, sequential nonce derived from the region's index (see
         * EncryptorV2). Because the nonce is stored alongside the ciphertext, individual regions of otherwise
         * untampered ciphertext can be rearranged without invalidating any single region's authentication tag,
         * silently corrupting the decrypted plaintext. Detect this by asserting that the nonce of each region matches
         * the value expected for its sequential position.
         *
         * CSEv2 is cross-SDK interoperable and each Azure Storage SDK encodes the region counter into the nonce
         * differently (see NonceScheme). Decryption itself is unaffected (it uses the inline nonce), but to validate
         * ordering we must recognize the scheme the blob was written with. We start by considering all known schemes
         * and intersect the set of schemes still consistent with every region seen so far. Intersection is
         * order-independent, so this remains correct even if regions are processed concurrently/out of order. If the
         * set ever becomes empty, a region is out of place. This behavior can be disabled for data recovery via a
         * compatibility switch.
         */
        final boolean detectRegionReorder = !cseV2AllowMisorderedAuthRegions();
        final AtomicReference<EnumSet<NonceScheme>> candidateSchemes
            = detectRegionReorder ? new AtomicReference<>(EnumSet.allOf(NonceScheme.class)) : null;
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
                    RuntimeException reorderError
                        = validateRegionNonce(gmcIv, nonceLength, expectedRegion, candidateSchemes);
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
     * Validates that the nonce of an authenticated region is consistent with the region occupying its expected
     * sequential position, under one of the recognized cross-SDK nonce schemes. A region whose nonce matches none of
     * the schemes for its position - or that is inconsistent with the scheme established by earlier regions - indicates
     * a reorder or other tampering. Also fails closed once the region index reaches the point where the Java encoder's
     * nonce begins to repeat, as integrity can no longer be guaranteed there.
     *
     * @param actualNonce The nonce read from the downloaded region.
     * @param nonceLength The length of the nonce.
     * @param expectedRegion The expected 0-based region index.
     * @param candidateSchemes The set of nonce schemes still consistent with all regions seen so far. Intersected in
     * place with the schemes matching this region; intersection is order-independent and therefore safe under
     * concurrent region processing.
     * @return A {@link RuntimeException} describing the tampering or unverifiable state if the region is invalid, or
     * {@code null} if the nonce is valid.
     */
    private RuntimeException validateRegionNonce(byte[] actualNonce, int nonceLength, long expectedRegion,
        AtomicReference<EnumSet<NonceScheme>> candidateSchemes) {
        // Cannot reconstruct the expected nonce if it is too short to hold the region index. This should never happen
        // for CSEv2 (nonce length is 12), so treat it as unverifiable rather than a failure.
        if (nonceLength < Long.BYTES || actualNonce.length < Long.BYTES) {
            return null;
        }

        // The Java encoder truncates the region index to an int, so its nonces begin to repeat at 2^32 regions. Past
        // that boundary a Java-encoded blob can no longer be verified (nonce reuse), while the Python and .NET encoders
        // use wider counters that do not repeat for any real blob size. So past the boundary we only consider the
        // non-wrapping schemes; if none match, we fail closed.
        boolean pastJavaNonceWrap = expectedRegion >= NONCE_WRAP_REGION_COUNT;

        EnumSet<NonceScheme> matchesHere = EnumSet.noneOf(NonceScheme.class);
        for (NonceScheme scheme : NonceScheme.values()) {
            if (pastJavaNonceWrap && scheme == NonceScheme.JAVA) {
                continue;
            }
            if (Arrays.equals(scheme.expectedNonce(expectedRegion, nonceLength), actualNonce)) {
                matchesHere.add(scheme);
            }
        }

        if (matchesHere.isEmpty()) {
            if (pastJavaNonceWrap) {
                return LOGGER.logExceptionAsError(new IllegalStateException("Cannot verify the integrity of client-side"
                    + " encrypted (v2) content at or beyond " + NONCE_WRAP_REGION_COUNT + " authenticated regions"
                    + " (region index " + expectedRegion + "). For content encrypted by this (Java) SDK the encryption"
                    + " nonce repeats past that point, resulting in GCM nonce reuse, and the blob is too large for its"
                    + " authenticated region size to be safely verified. " + recoveryInstruction()));
            }
            return reorderException(expectedRegion, actualNonce);
        }

        // Lock onto the scheme(s) consistent with every region so far. A valid blob keeps its own scheme in the set at
        // every region; a reorder that happens to look valid under a different scheme for a single region is caught
        // here because it is inconsistent with the scheme the rest of the blob uses.
        EnumSet<NonceScheme> remaining = candidateSchemes.updateAndGet(current -> {
            EnumSet<NonceScheme> next = EnumSet.copyOf(current);
            next.retainAll(matchesHere);
            return next;
        });
        if (remaining.isEmpty()) {
            return reorderException(expectedRegion, actualNonce);
        }

        return null;
    }

    private RuntimeException reorderException(long expectedRegion, byte[] actualNonce) {
        return LOGGER.logExceptionAsError(new IllegalStateException(
            "Encountered an out-of-order authenticated region while decrypting client-side encrypted (v2) content. "
                + "This may indicate that the blob's authenticated regions have been rearranged or otherwise tampered "
                + "with. The nonce at region index " + expectedRegion + " (0x" + bytesToHex(actualNonce) + ") does not "
                + "match any recognized client-side encryption nonce scheme for that position. "
                + recoveryInstruction()));
    }

    private static String recoveryInstruction() {
        return "To recover data from an affected blob, set the \"" + CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_ENV_VAR
            + "\" environment variable (or the \"" + CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME
            + "\" system property) to \"true\".";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }

    /**
     * The recognized ways Azure Storage client-side encryption v2 SDKs encode a region's sequential counter into its
     * GCM nonce. Decryption uses the inline nonce and is unaffected by these differences, but reorder detection must
     * reconstruct the expected nonce, which requires knowing the encoder's scheme. The complete set of SDKs that
     * produce CSEv2 content is .NET, Java, and Python.
     */
    private enum NonceScheme {
        /**
         * Java: the region index truncated to an int, written as an 8-byte big-endian (sign-extended) value in the
         * first 8 bytes, remaining bytes zero. 0-based. See {@link EncryptorV2}.
         */
        JAVA {
            @Override
            byte[] expectedNonce(long regionIndex, int nonceLength) {
                byte[] nonce = new byte[nonceLength];
                long value = (int) regionIndex;
                for (int i = 0; i < Long.BYTES; i++) {
                    nonce[i] = (byte) (value >>> (Long.SIZE - Byte.SIZE - Byte.SIZE * i));
                }
                return nonce;
            }
        },

        /**
         * Python: the region index encoded as a big-endian integer across the whole nonce (counter in the low bytes).
         * 0-based. See azure-storage-blob {@code encrypt_data_v2}.
         */
        PYTHON {
            @Override
            byte[] expectedNonce(long regionIndex, int nonceLength) {
                byte[] nonce = new byte[nonceLength];
                for (int i = 0; i < Long.BYTES; i++) {
                    nonce[nonceLength - 1 - i] = (byte) (regionIndex >>> (Byte.SIZE * i));
                }
                return nonce;
            }
        },

        /**
         * .NET: four zero bytes followed by an 8-byte little-endian counter. 1-based (the first region uses counter 1).
         * See Azure.Storage.Common {@code GcmAuthenticatedCryptographicTransform}.
         */
        DOTNET {
            @Override
            byte[] expectedNonce(long regionIndex, int nonceLength) {
                byte[] nonce = new byte[nonceLength];
                long counter = regionIndex + 1;
                for (int i = 0; i < Long.BYTES; i++) {
                    nonce[(nonceLength - Long.BYTES) + i] = (byte) (counter >>> (Byte.SIZE * i));
                }
                return nonce;
            }
        };

        /**
         * Produces the nonce this scheme assigns to the given region index.
         *
         * @param regionIndex The 0-based region index.
         * @param nonceLength The nonce length (12 for CSEv2). Must be at least {@link Long#BYTES}.
         * @return The expected nonce bytes.
         */
        abstract byte[] expectedNonce(long regionIndex, int nonceLength);
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
