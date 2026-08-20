// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.List;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the CSEv2 region nonce counter in {@link EncryptorV2}.
 * <p>
 * Each authenticated region is encrypted under a nonce derived from its sequential index. The index must be encoded
 * using the full 64-bit value; truncating it to 32 bits causes nonces to repeat every 2^32 regions, which is AES-GCM
 * nonce reuse (a security failure) and also defeats sequential reorder detection. These tests exercise the encoding
 * directly and end-to-end through {@link EncryptorV2#encrypt(Flux)}.
 */
public class EncryptorV2NonceTests {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Test
    public void regionZeroNonceIsAllZeros() {
        assertArrayEquals(new byte[NONCE_LENGTH], EncryptorV2.computeRegionNonce(0));
    }

    @Test
    public void nonceEncodesRegionIndexAsBigEndianLongWithTrailingZeros() {
        for (long index : new long[] { 1, 2, 255, 256, 1_000_000, Integer.MAX_VALUE }) {
            byte[] nonce = EncryptorV2.computeRegionNonce(index);

            assertEquals(NONCE_LENGTH, nonce.length);
            // First 8 bytes are the big-endian index.
            byte[] expectedPrefix = ByteBuffer.allocate(Long.BYTES).putLong(index).array();
            byte[] actualPrefix = new byte[Long.BYTES];
            System.arraycopy(nonce, 0, actualPrefix, 0, Long.BYTES);
            assertArrayEquals(expectedPrefix, actualPrefix, "index=" + index);
            // Remaining bytes are zero.
            for (int i = Long.BYTES; i < NONCE_LENGTH; i++) {
                assertEquals(0, nonce[i], "trailing byte " + i + " for index=" + index);
            }
        }
    }

    @Test
    public void nonceUsesFullLongInsteadOfTruncatedInt() {
        // Region 2^31 exceeds the positive int range. The old encoder truncated the index to an int, producing
        // Integer.MIN_VALUE and, after sign extension, a nonce beginning with 0xFFFFFFFF. The full-long encoding must
        // instead leave the high four bytes zero.
        long index = 1L << 31;
        byte[] nonce = EncryptorV2.computeRegionNonce(index);

        for (int i = 0; i < 4; i++) {
            assertEquals(0, nonce[i], "high byte " + i + " should be zero, not sign-extended");
        }
        // Byte 4 holds the top bit of the 2^31 value.
        assertEquals((byte) 0x80, nonce[4]);

        // Explicitly confirm it differs from what a truncated-int counter would have produced.
        byte[] truncated = ByteBuffer.allocate(NONCE_LENGTH).putLong((int) index).array();
        assertFalse(java.util.Arrays.equals(truncated, nonce),
            "full-long nonce must differ from the truncated-int nonce at index 2^31");
    }

    @Test
    public void regionsExactlyNonceWrapApartHaveDistinctNonces() {
        // The core regression: with a truncated 32-bit counter, region N and region N + 2^32 share a nonce (GCM nonce
        // reuse). The full-long counter must give them distinct nonces.
        long wrap = 1L << 32;

        assertFalse(java.util.Arrays.equals(EncryptorV2.computeRegionNonce(0), EncryptorV2.computeRegionNonce(wrap)));
        assertFalse(
            java.util.Arrays.equals(EncryptorV2.computeRegionNonce(5), EncryptorV2.computeRegionNonce(wrap + 5)));

        // Region 2^32 encodes as {0,0,0,1, 0,0,0,0, 0,0,0,0}.
        byte[] expected = new byte[NONCE_LENGTH];
        expected[3] = 1;
        assertArrayEquals(expected, EncryptorV2.computeRegionNonce(wrap));
    }

    @Test
    public void allNoncesUniqueAcrossWrapBoundarySample() {
        // Sample indices straddling the old 2^32 wrap point must all be distinct.
        long[] indices = { 0, 1, 2, (1L << 31) - 1, 1L << 31, (1L << 32) - 1, 1L << 32, (1L << 32) + 1, 1L << 33 };
        for (int i = 0; i < indices.length; i++) {
            for (int j = i + 1; j < indices.length; j++) {
                assertFalse(
                    java.util.Arrays.equals(EncryptorV2.computeRegionNonce(indices[i]),
                        EncryptorV2.computeRegionNonce(indices[j])),
                    "nonces for " + indices[i] + " and " + indices[j] + " must differ");
            }
        }
    }

    @Test
    public void encryptEmitsSequentialRegionNonces() {
        // End-to-end: encrypt a multi-region blob with a small region size and confirm each region is prefixed with the
        // nonce for its sequential index.
        int regionLength = 16;
        int regionCount = 5;
        int plaintextLength = regionLength * (regionCount - 1) + 7; // last region is partial

        SecretKey key = new SecretKeySpec(randomBytes(32), CryptographyConstants.AES);
        BlobClientSideEncryptionOptions options
            = new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(regionLength);
        EncryptorV2 encryptor = new EncryptorV2(key, options, ENCRYPTION_PROTOCOL_V2);

        byte[] plaintext = randomBytes(plaintextLength);
        List<ByteBuffer> emitted = encryptor.encrypt(Flux.just(ByteBuffer.wrap(plaintext))).collectList().block();

        byte[] ciphertext = concat(emitted);
        int offset = 0;
        int remaining = plaintextLength;
        for (long region = 0; region < regionCount; region++) {
            byte[] nonce = new byte[NONCE_LENGTH];
            System.arraycopy(ciphertext, offset, nonce, 0, NONCE_LENGTH);
            assertArrayEquals(EncryptorV2.computeRegionNonce(region), nonce, "region " + region + " nonce");

            int regionData = Math.min(regionLength, remaining);
            offset += NONCE_LENGTH + regionData + TAG_LENGTH;
            remaining -= regionData;
        }
        assertEquals(ciphertext.length, offset, "consumed the entire ciphertext");
        assertTrue(remaining <= 0);
    }

    private static byte[] concat(List<ByteBuffer> buffers) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            out.write(bytes, 0, bytes.length);
        }
        return out.toByteArray();
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
