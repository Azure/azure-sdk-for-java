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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES;
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

    private static byte[] encrypt(byte[] cek, byte[] plaintext) {
        SecretKey key = new SecretKeySpec(cek, AES);
        EncryptorV2 encryptor = new EncryptorV2(key,
            new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_DATA_LENGTH),
            ENCRYPTION_PROTOCOL_V2);
        List<ByteBuffer> buffers = encryptor.encrypt(Flux.just(ByteBuffer.wrap(plaintext))).collectList().block();
        return toBytes(buffers);
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
}
