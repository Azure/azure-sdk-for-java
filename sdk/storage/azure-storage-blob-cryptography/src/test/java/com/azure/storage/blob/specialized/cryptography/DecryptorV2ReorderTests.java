// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.BlobRange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import reactor.core.publisher.Flux;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for detection of reordered client-side encryption v2 authenticated regions. These exercise
 * {@link EncryptorV2} and {@link DecryptorV2} directly and do not require a storage account.
 * <p>
 * The compatibility switch is a process-global system property, so this class is isolated and run on a single thread to
 * avoid interfering with (or being interfered with by) tests running in parallel.
 */
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
public class DecryptorV2ReorderTests {
    private static final int REGION_DATA_LENGTH = 1024;
    private static final int REGION_TOTAL_LENGTH = NONCE_LENGTH + REGION_DATA_LENGTH + TAG_LENGTH;
    private static final int REGION_COUNT = 4;
    private static final Random RANDOM = new Random();

    @AfterEach
    public void clearSwitch() {
        System.clearProperty(CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME);
    }

    @Test
    public void unmodifiedContentDecryptsSuccessfully() {
        byte[] cek = randomBytes(32);
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH * REGION_COUNT);
        byte[] ciphertext = encrypt(cek, plaintext);

        assertArrayEquals(plaintext, decrypt(cek, ciphertext, 0));
    }

    @Test
    public void detectsRegionReorder() {
        byte[] cek = randomBytes(32);
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH * REGION_COUNT);
        byte[] ciphertext = encrypt(cek, plaintext);

        // Swap two otherwise-untampered authenticated regions.
        swapRegions(ciphertext, 2, 3, REGION_TOTAL_LENGTH);

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> decrypt(cek, ciphertext, 0));
        assertTrue(e.getMessage().contains("out-of-order"), e.getMessage());
    }

    @Test
    public void detectsRegionReorderOnRangedDownload() {
        byte[] cek = randomBytes(32);
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH * REGION_COUNT);
        byte[] ciphertext = encrypt(cek, plaintext);

        // Simulate a ranged download that begins at region 1 by dropping the first region's ciphertext.
        byte[] ranged = Arrays.copyOfRange(ciphertext, REGION_TOTAL_LENGTH, ciphertext.length);

        // A ranged download starting at the correct region decrypts successfully (no false positive).
        byte[] expected = Arrays.copyOfRange(plaintext, REGION_DATA_LENGTH, plaintext.length);
        assertArrayEquals(expected, decrypt(cek, ranged, REGION_DATA_LENGTH));

        // Reordering within the ranged content is still detected, with the region index offset by the range.
        swapRegions(ranged, 1, 2, REGION_TOTAL_LENGTH);
        assertThrows(IllegalStateException.class, () -> decrypt(cek, ranged, REGION_DATA_LENGTH));
    }

    @Test
    public void compatSwitchAllowsReorder() {
        System.setProperty(CryptographyConstants.CSE_V2_ALLOW_MISORDERED_AUTH_REGIONS_SWITCH_NAME, "true");

        byte[] cek = randomBytes(32);
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH * REGION_COUNT);
        byte[] ciphertext = encrypt(cek, plaintext);
        swapRegions(ciphertext, 2, 3, REGION_TOTAL_LENGTH);

        // With the switch enabled, decryption does not throw and recovers plaintext in the received order.
        byte[] recovered = decrypt(cek, ciphertext, 0);

        byte[] expected = plaintext.clone();
        swapRegions(expected, 2, 3, REGION_DATA_LENGTH);
        assertArrayEquals(expected, recovered);
    }

    @Test
    public void decryptsRegionsAcrossIntegerMaxValueBoundary() {
        // EncryptorV2 truncates the region index to an int when producing the nonce, so region Integer.MAX_VALUE + 1
        // wraps to a negative (sign-extended) nonce. The reorder detection must replicate that truncation, otherwise
        // valid blobs whose region indices cross Integer.MAX_VALUE (reachable at ~32 GiB with the minimum 16-byte
        // region size) would be incorrectly rejected as reordered.
        byte[] cek = randomBytes(32);
        long firstRegion = Integer.MAX_VALUE;
        byte[] region0Plaintext = randomBytes(REGION_DATA_LENGTH);
        byte[] region1Plaintext = randomBytes(REGION_DATA_LENGTH);

        byte[] ciphertext = concat(encryptRegionAt(cek, firstRegion, region0Plaintext),
            encryptRegionAt(cek, firstRegion + 1, region1Plaintext));

        long offset = firstRegion * REGION_DATA_LENGTH;
        byte[] recovered = decrypt(cek, ciphertext, offset);

        assertArrayEquals(concat(region0Plaintext, region1Plaintext), recovered);
    }

    @Test
    public void detectsReorderAcrossIntegerMaxValueBoundary() {
        // Detection must still fire across the int boundary: place a region whose nonce belongs to a different index
        // than expected and confirm it is rejected.
        byte[] cek = randomBytes(32);
        long firstRegion = Integer.MAX_VALUE;

        byte[] ciphertext = concat(encryptRegionAt(cek, firstRegion, randomBytes(REGION_DATA_LENGTH)),
            // Expected index here is firstRegion + 1, but this region is nonced for firstRegion + 2.
            encryptRegionAt(cek, firstRegion + 2, randomBytes(REGION_DATA_LENGTH)));

        long offset = firstRegion * REGION_DATA_LENGTH;
        assertThrows(IllegalStateException.class, () -> decrypt(cek, ciphertext, offset));
    }

    private static byte[] encrypt(byte[] cek, byte[] plaintext) {
        SecretKey key = new SecretKeySpec(cek, AES);
        EncryptorV2 encryptor = new EncryptorV2(key,
            new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_DATA_LENGTH),
            ENCRYPTION_PROTOCOL_V2);
        List<ByteBuffer> buffers = encryptor.encrypt(Flux.just(ByteBuffer.wrap(plaintext))).collectList().block();
        return toBytes(buffers);
    }

    /**
     * Encrypts a single region using the same nonce scheme as {@link EncryptorV2}: the region index is truncated to an
     * int and written big-endian into the nonce. Produces {@code nonce || ciphertext || tag}. Used to craft ciphertext
     * for arbitrary (very large) region indices without materializing all preceding regions.
     */
    private static byte[] encryptRegionAt(byte[] cek, long regionIndex, byte[] plaintext) {
        try {
            byte[] nonce = ByteBuffer.allocate(NONCE_LENGTH).putLong((int) regionIndex).array();
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, AES), new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            return concat(nonce, cipher.doFinal(plaintext));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] decrypt(byte[] cek, byte[] ciphertext, long offset) {
        EncryptionData encryptionData = new EncryptionData()
            .setEncryptionAgent(new EncryptionAgent(ENCRYPTION_PROTOCOL_V2, EncryptionAlgorithm.AES_GCM_256))
            .setEncryptedRegionInfo(new EncryptedRegionInfo(REGION_DATA_LENGTH, NONCE_LENGTH));
        DecryptorV2 decryptor = new DecryptorV2(null, null, encryptionData);
        EncryptedBlobRange range = new EncryptedBlobRange(new BlobRange(offset), encryptionData);

        List<ByteBuffer> out
            = decryptor.decrypt(Flux.just(ByteBuffer.wrap(ciphertext)), range, false, "uri", new AtomicLong(0), cek)
                .collectList()
                .block();
        return toBytes(out);
    }

    private static byte[] toBytes(List<ByteBuffer> buffers) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            ByteBuffer duplicate = buffer.duplicate();
            byte[] arr = new byte[duplicate.remaining()];
            duplicate.get(arr);
            out.write(arr, 0, arr.length);
        }
        return out.toByteArray();
    }

    private static void swapRegions(byte[] buffer, int leftRegion, int rightRegion, int regionLength) {
        int leftOffset = leftRegion * regionLength;
        int rightOffset = rightRegion * regionLength;
        for (int i = 0; i < regionLength; i++) {
            byte temp = buffer[leftOffset + i];
            buffer[leftOffset + i] = buffer[rightOffset + i];
            buffer[rightOffset + i] = temp;
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static byte[] concat(byte[] left, byte[] right) {
        byte[] result = Arrays.copyOf(left, left.length + right.length);
        System.arraycopy(right, 0, result, left.length, right.length);
        return result;
    }
}
