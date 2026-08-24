// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ALLOW_MISORDERED_REGIONS_ENV_VAR;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ALLOW_MISORDERED_REGIONS_PROPERTY;

/**
 * Detects rearrangement of client-side encryption v2 authenticated regions by validating each region's nonce against
 * the value expected for its sequential position.
 * <p>
 * Each CSEv2 region is encrypted under a unique, sequential nonce derived from the region's index. Because the nonce is
 * stored alongside the ciphertext, individual regions of otherwise untampered ciphertext can be rearranged without
 * invalidating any single region's GCM tag, silently corrupting the decrypted plaintext. Validating that each region's
 * nonce matches its position detects this.
 * <p>
 * CSEv2 is cross-SDK interoperable and each Azure Storage SDK encodes the region counter into the nonce differently
 * (see {@link NonceScheme}). Decryption itself is unaffected (it uses the inline nonce), but reorder validation must
 * reconstruct the expected nonce, which requires knowing the encoder's scheme. This validator begins by considering all
 * known schemes and intersects the set of schemes still consistent with every region seen so far, collapsing to a
 * single scheme once enough regions have been read. Once collapsed, subsequent regions are checked lock-free against
 * the resolved scheme.
 * <p>
 * A single instance is intended to be shared across an entire logical download operation (which may span multiple
 * concurrent HTTP range requests, e.g. a parallel {@code downloadToFile} or a chunked {@code openInputStream}). Sharing
 * the instance means the scheme is enforced consistently across the whole download rather than being re-established per
 * chunk; otherwise, because the schemes share a value space, a region relocated across a chunk boundary to a colliding
 * position could pass validation. The intersection is performed atomically and is order-independent, so it is safe
 * under concurrent region processing.
 */
final class CseV2NonceOrderValidator {
    private static final ClientLogger LOGGER = new ClientLogger(CseV2NonceOrderValidator.class);

    private final boolean validationEnabled;
    private final AtomicReference<EnumSet<NonceScheme>> candidateSchemes
        = new AtomicReference<>(EnumSet.allOf(NonceScheme.class));

    CseV2NonceOrderValidator() {
        // Read the data-recovery bypass switch once per download operation.
        this.validationEnabled = !cseV2AllowMisorderedAuthRegions();
    }

    /**
     * Validates that the nonce of an authenticated region is consistent with the region occupying its sequential
     * position, under a single recognized cross-SDK nonce scheme enforced across the whole download. A region whose
     * nonce matches none of the schemes for its position - or that is inconsistent with the scheme established by
     * earlier regions - indicates a reorder or other tampering.
     *
     * @param actualNonce The nonce read from the downloaded region.
     * @param nonceLength The length of the nonce.
     * @param region The 0-based index of this region's position in the blob.
     * @throws RuntimeException If the region is invalid or its integrity cannot be verified.
     */
    void validateRegion(byte[] actualNonce, int nonceLength, long region) {
        if (!validationEnabled) {
            return;
        }

        // Cannot reconstruct the expected nonce if it is too short to hold the region index. This should never happen
        // for CSEv2 (nonce length is 12), so treat it as unverifiable rather than a failure.
        if (nonceLength < Long.BYTES || actualNonce.length < Long.BYTES) {
            return;
        }

        // Fast path: once the scheme has collapsed to a single encoding, verify directly without mutating shared state.
        EnumSet<NonceScheme> current = candidateSchemes.get();
        if (current.size() == 1) {
            NonceScheme locked = current.iterator().next();
            if (!Arrays.equals(locked.expectedNonce(region, nonceLength), actualNonce)) {
                throw LOGGER.logExceptionAsError(reorderException());
            }
            return;
        }

        // Determine which schemes are consistent with this region at its position.
        EnumSet<NonceScheme> matchesHere = EnumSet.noneOf(NonceScheme.class);
        for (NonceScheme scheme : NonceScheme.values()) {
            if (Arrays.equals(scheme.expectedNonce(region, nonceLength), actualNonce)) {
                matchesHere.add(scheme);
            }
        }

        if (matchesHere.isEmpty()) {
            throw LOGGER.logExceptionAsError(reorderException());
        }

        // Intersect the shared candidate set with the schemes matching this region. Atomic and order-independent, so it
        // remains correct even if regions are processed concurrently. A valid blob keeps its own scheme in the set at
        // every region; a reorder that looks valid under a different scheme for a single region is caught here because
        // it is inconsistent with the scheme the rest of the download uses.
        EnumSet<NonceScheme> remaining = candidateSchemes.updateAndGet(existing -> {
            EnumSet<NonceScheme> next = EnumSet.copyOf(existing);
            next.retainAll(matchesHere);
            return next;
        });
        if (remaining.isEmpty()) {
            throw LOGGER.logExceptionAsError(reorderException());
        }
    }

    private static RuntimeException reorderException() {
        return new IllegalStateException(
            "Encountered an out-of-order authenticated region while decrypting client-side encrypted (v2) content. "
                + "This may indicate that the blob's authenticated regions have been rearranged or otherwise tampered "
                + "with." + recoveryInstruction());
    }

    private static String recoveryInstruction() {
        return "To recover data from an affected blob, set the \"" + ALLOW_MISORDERED_REGIONS_ENV_VAR
            + "\" environment variable (or the \"" + ALLOW_MISORDERED_REGIONS_PROPERTY
            + "\" system property) to \"true\".";
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
        String value = System.getProperty(ALLOW_MISORDERED_REGIONS_PROPERTY);
        if (CoreUtils.isNullOrEmpty(value)) {
            value = System.getenv(ALLOW_MISORDERED_REGIONS_ENV_VAR);
        }
        return Boolean.parseBoolean(value);
    }

    /**
     * The recognized ways Azure Storage client-side encryption v2 SDKs encode a region's sequential counter into its
     * GCM nonce. Decryption uses the inline nonce and is unaffected by these differences, but reorder detection must
     * reconstruct the expected nonce, which requires knowing the encoder's scheme. The complete set of SDKs that
     * produce CSEv2 content is .NET, Java, and Python.
     */
    private enum NonceScheme {
        /**
         * Java: the region index written as an 8-byte big-endian value in the first 8 bytes, remaining bytes zero.
         * 0-based. See {@link EncryptorV2}.
         */
        JAVA {
            @Override
            byte[] expectedNonce(long regionIndex, int nonceLength) {
                byte[] nonce = new byte[nonceLength];
                for (int i = 0; i < Long.BYTES; i++) {
                    nonce[i] = (byte) (regionIndex >>> (Long.SIZE - Byte.SIZE - Byte.SIZE * i));
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
}
