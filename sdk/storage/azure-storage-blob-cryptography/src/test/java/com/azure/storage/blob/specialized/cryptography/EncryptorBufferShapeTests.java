// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.test.utils.TestUtils;
import com.azure.storage.common.test.shared.ShapedByteBuffer;
import com.azure.storage.common.test.shared.TestBufferShape;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Flux;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_CBC_PKCS5PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.AES_GCM_NO_PADDING;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Offline tests verifying that client-side encryption encrypts only the bytes between each source buffer's position and
 * limit, and doesn't modify the source buffers, for every kind of buffer (see {@link TestBufferShape}).
 */
public class EncryptorBufferShapeTests {
    // Small regions keep the tests fast; the region logic doesn't depend on the absolute size.
    private static final int REGION_LENGTH = 16;

    @ParameterizedTest
    @MethodSource("encryptV2Supplier")
    public void encryptV2EncryptsOnlyExposedBytes(TestBufferShape shape, int length, int expectedRegions)
        throws Exception {
        byte[] content = randomBytes(length);
        ShapedByteBuffer source = shape.wrap(content);
        SecretKey key = newKey();

        byte[] ciphertext = concat(encryptorV2(key).encrypt(Flux.just(source.getBuffer())).collectList().block());

        TestUtils.assertArraysEqual(content, decryptV2(key, ciphertext, expectedRegions));
        source.assertUnchanged();
    }

    private static Stream<Arguments> encryptV2Supplier() {
        return Stream.of(TestBufferShape.values())
            .flatMap(shape -> Stream.of(
                // shape | length | expected regions
                Arguments.of(shape, REGION_LENGTH / 2, 1), // A single partial region.
                Arguments.of(shape, REGION_LENGTH, 1), // Exactly one region.
                Arguments.of(shape, 2 * REGION_LENGTH + 5, 3), // Several regions, with a partial last one.
                Arguments.of(shape, 3 * REGION_LENGTH, 3))); // Several regions, an exact multiple of the length.
    }

    @Test
    public void encryptV2EncryptsOnlyExposedBytesOfMixedBuffers() throws Exception {
        byte[] content = randomBytes(5 * REGION_LENGTH + 5);
        // Pieces that don't align with regions, so regions span buffers of different kinds.
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 11);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        SecretKey key = newKey();

        byte[] ciphertext = concat(encryptorV2(key).encrypt(Flux.fromIterable(buffers)).collectList().block());

        TestUtils.assertArraysEqual(content, decryptV2(key, ciphertext, 6));
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @ParameterizedTest
    @EnumSource(TestBufferShape.class)
    public void encryptV1EncryptsOnlyExposedBytes(TestBufferShape shape) throws Exception {
        byte[] content = randomBytes(3 * REGION_LENGTH + 5);
        ShapedByteBuffer source = shape.wrap(content);
        SecretKey key = newKey();
        EncryptorV1 encryptor = new EncryptorV1(key);

        byte[] ciphertext = concat(encryptor.encrypt(Flux.just(source.getBuffer())).collectList().block());

        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(getV1Iv(encryptor)));
        TestUtils.assertArraysEqual(content, cipher.doFinal(ciphertext));
        source.assertUnchanged();
    }

    @Test
    public void encryptV2SameBuffersAgainEncryptsSameContent() throws Exception {
        byte[] content = randomBytes(5 * REGION_LENGTH + 5);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 11);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        SecretKey key = newKey();

        for (int attempt = 1; attempt <= 2; attempt++) {
            byte[] ciphertext = concat(encryptorV2(key).encrypt(Flux.fromIterable(buffers)).collectList().block());

            TestUtils.assertArraysEqual(content, decryptV2(key, ciphertext, 6));
        }
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    @Test
    public void encryptV1SameBuffersAgainEncryptsSameContent() throws Exception {
        byte[] content = randomBytes(5 * REGION_LENGTH + 5);
        List<ShapedByteBuffer> sources = TestBufferShape.mixed(content, 11);
        List<ByteBuffer> buffers = new ArrayList<>();
        sources.forEach(source -> buffers.add(source.getBuffer()));
        SecretKey key = newKey();

        for (int attempt = 1; attempt <= 2; attempt++) {
            EncryptorV1 encryptor = new EncryptorV1(key);
            byte[] ciphertext = concat(encryptor.encrypt(Flux.fromIterable(buffers)).collectList().block());

            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5PADDING);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(getV1Iv(encryptor)));
            TestUtils.assertArraysEqual(content, cipher.doFinal(ciphertext));
        }
        sources.forEach(ShapedByteBuffer::assertUnchanged);
    }

    private static EncryptorV2 encryptorV2(SecretKey key) {
        return new EncryptorV2(key,
            new BlobClientSideEncryptionOptions().setAuthenticatedRegionDataLengthInBytes(REGION_LENGTH),
            ENCRYPTION_PROTOCOL_V2);
    }

    /*
     * Decrypts V2 ciphertext, where each authenticated region is its nonce, followed by the encrypted region data and
     * its tag.
     */
    private static byte[] decryptV2(SecretKey key, byte[] ciphertext, int expectedRegions) throws Exception {
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

    private static byte[] getV1Iv(EncryptorV1 encryptor) throws ReflectiveOperationException {
        Field cipherField = EncryptorV1.class.getDeclaredField("cipher");
        cipherField.setAccessible(true);
        return ((Cipher) cipherField.get(encryptor)).getIV();
    }

    private static SecretKey newKey() {
        byte[] keyBytes = new byte[32];
        new Random(1).nextBytes(keyBytes);
        return new SecretKeySpec(keyBytes, CryptographyConstants.AES);
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new Random(length).nextBytes(bytes);
        return bytes;
    }

    private static byte[] concat(List<ByteBuffer> buffers) {
        assertNotNull(buffers);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (ByteBuffer buffer : buffers) {
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            out.write(bytes, 0, bytes.length);
        }
        return out.toByteArray();
    }
}
