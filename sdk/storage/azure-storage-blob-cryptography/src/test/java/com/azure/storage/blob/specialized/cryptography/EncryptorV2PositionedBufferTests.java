// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Flux;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Offline tests verifying that client-side encryption of a {@link ByteBuffer} whose position is not zero encrypts only
 * the bytes between its position and limit, including when the buffer is split into multiple authenticated regions.
 */
public class EncryptorV2PositionedBufferTests {
    private static final int REGION_LENGTH = 16;

    @ParameterizedTest
    @CsvSource({
        // position | remaining | expected regions
        "1,16,1", // Fits in a single region.
        "1,20,2",
        "5,40,3",
        "17,41,3", // Split into multiple regions.
        "1,32,2",
        "3,48,3" // An exact multiple of the region length.
    })
    public void encryptPositionedBuffer(int position, int remaining, int expectedRegions) throws Exception {
        byte[] backing = new byte[position + remaining + 3];
        new Random(position + remaining).nextBytes(backing);
        ByteBuffer buffer = ByteBuffer.wrap(backing, position, remaining);

        byte[] keyBytes = new byte[32];
        new Random(remaining).nextBytes(keyBytes);
        SecretKey key = new SecretKeySpec(keyBytes, CryptographyConstants.AES);
        EncryptorV2 encryptor = new EncryptorV2(key,
            new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_LENGTH),
            ENCRYPTION_PROTOCOL_V2);

        List<ByteBuffer> emitted = encryptor.encrypt(Flux.just(buffer)).collectList().block();
        Assertions.assertNotNull(emitted);

        TestUtils.assertArraysEqual(Arrays.copyOfRange(backing, position, position + remaining),
            decrypt(key, concat(emitted), expectedRegions));
    }

    /*
     * Decrypts the ciphertext, where each authenticated region is its nonce, followed by the encrypted region data and
     * its tag.
     */
    private static byte[] decrypt(SecretKey key, byte[] ciphertext, int expectedRegions) throws Exception {
        ByteArrayOutputStream plaintext = new ByteArrayOutputStream();
        int offset = 0;
        int regions = 0;
        while (offset < ciphertext.length) {
            int regionLength = Math.min(NONCE_LENGTH + REGION_LENGTH + TAG_LENGTH, ciphertext.length - offset);
            byte[] nonce = Arrays.copyOfRange(ciphertext, offset, offset + NONCE_LENGTH);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            byte[] region = cipher.doFinal(ciphertext, offset + NONCE_LENGTH, regionLength - NONCE_LENGTH);
            plaintext.write(region, 0, region.length);
            offset += regionLength;
            regions++;
        }
        assertEquals(expectedRegions, regions);
        return plaintext.toByteArray();
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
}
