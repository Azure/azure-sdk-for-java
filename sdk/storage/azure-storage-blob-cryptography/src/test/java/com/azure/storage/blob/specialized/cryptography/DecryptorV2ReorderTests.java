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
    // Region index at which EncryptorV2's int-truncated nonce repeats (2^32). Reorder detection no longer special-cases
    // this boundary; the constant is kept only to exercise decryption at and around it.
    private static final long NONCE_REPEAT_REGION = 1L << 32;
    private static final Random RANDOM = new Random();

    @AfterEach
    public void clearSwitch() {
        System.clearProperty(CryptographyConstants.ALLOW_MISORDERED_REGIONS_PROPERTY);
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
        System.setProperty(CryptographyConstants.ALLOW_MISORDERED_REGIONS_PROPERTY, "true");

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

    @Test
    public void decryptsLastRegionBeforeNonceRepeat() {
        // Region 2^32 - 1 is the last region before the Java encoder's int-truncated nonce repeats; it decrypts.
        byte[] cek = randomBytes(32);
        long lastUniqueRegion = NONCE_REPEAT_REGION - 1;
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH);

        byte[] ciphertext = encryptRegionAt(cek, lastUniqueRegion, plaintext);
        byte[] recovered = decrypt(cek, ciphertext, lastUniqueRegion * REGION_DATA_LENGTH);

        assertArrayEquals(plaintext, recovered);
    }

    @Test
    public void decryptsValidBlobAtNonceRepeatRegion() {
        // At region 2^32 the Java encoder's nonce repeats (reuses region 0's nonce), but the reorder check reconstructs
        // the Java nonce with the same int truncation, so a valid blob still matches and decrypts. Reorder detection
        // does not special-case very large region counts (the nonce-reuse itself is an encryptor concern handled
        // separately); it must not reject valid content here.
        byte[] cek = randomBytes(32);
        long wrapRegion = NONCE_REPEAT_REGION;
        byte[] plaintext = randomBytes(REGION_DATA_LENGTH);
        byte[] ciphertext = encryptRegionAt(cek, wrapRegion, plaintext);

        byte[] recovered = decrypt(cek, ciphertext, wrapRegion * REGION_DATA_LENGTH);
        assertArrayEquals(plaintext, recovered);
    }

    // ---- Cross-SDK interoperability: Java must still decrypt (and detect reorders in) blobs written by other SDKs,
    // which encode the region counter into the nonce differently. ----

    @Test
    public void pythonEncodedBlobDecryptsWithoutFalseReorder() {
        byte[] cek = randomBytes(32);
        byte[][] plaintext = {
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH) };
        byte[] ciphertext = buildBlob(cek, DecryptorV2ReorderTests::pythonNonce, plaintext);

        assertArrayEquals(flatten(plaintext), decrypt(cek, ciphertext, 0));
    }

    @Test
    public void dotnetEncodedBlobDecryptsWithoutFalseReorder() {
        byte[] cek = randomBytes(32);
        byte[][] plaintext = {
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH) };
        byte[] ciphertext = buildBlob(cek, DecryptorV2ReorderTests::dotnetNonce, plaintext);

        assertArrayEquals(flatten(plaintext), decrypt(cek, ciphertext, 0));
    }

    @Test
    public void detectsReorderInPythonEncodedBlob() {
        byte[] cek = randomBytes(32);
        byte[][] plaintext = {
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH) };
        byte[] ciphertext = buildBlob(cek, DecryptorV2ReorderTests::pythonNonce, plaintext);
        swapRegions(ciphertext, 2, 3, REGION_TOTAL_LENGTH);

        assertThrows(IllegalStateException.class, () -> decrypt(cek, ciphertext, 0));
    }

    @Test
    public void detectsReorderInDotnetEncodedBlob() {
        byte[] cek = randomBytes(32);
        byte[][] plaintext = {
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH),
            randomBytes(REGION_DATA_LENGTH) };
        byte[] ciphertext = buildBlob(cek, DecryptorV2ReorderTests::dotnetNonce, plaintext);
        swapRegions(ciphertext, 1, 2, REGION_TOTAL_LENGTH);

        assertThrows(IllegalStateException.class, () -> decrypt(cek, ciphertext, 0));
    }

    @Test
    public void detectsSchemeSwitchMidBlob() {
        // A blob whose first region uses one SDK's nonce scheme and a later region uses another's cannot be a single
        // valid blob; the scheme is locked after the first region, so the switch is detected.
        byte[] cek = randomBytes(32);
        byte[] region0 = encryptRegionWithNonce(cek, dotnetNonce(0), randomBytes(REGION_DATA_LENGTH));
        byte[] region1 = encryptRegionWithNonce(cek, javaNonce(1), randomBytes(REGION_DATA_LENGTH));

        assertThrows(IllegalStateException.class, () -> decrypt(cek, concat(region0, region1), 0));
    }

    @Test
    public void rejectsMixedNonceEncodingsAcrossCollidingValueSpace() {
        // The supported SDK nonce encodings share a value space, so accepting them independently per region would
        // weaken reorder detection. For example the Java nonce for region 1 is byte-identical to the .NET nonce for
        // region 16,777,215, so a region could be moved across that boundary and still pass a naive per-region union
        // check. Detection must instead lock onto a single encoding and enforce it for every region. (Mirrors the
        // Python SDK's test_decrypt_rejects_mixed_nonce_encodings.)
        assertArrayEquals(javaNonce(1), dotnetNonce(16_777_215));

        byte[] cek = randomBytes(32);
        // Region 0 uses the Java/Python encoding (all zeros); region 1 uses the .NET encoding. A per-region union
        // check would accept both, but single-encoding enforcement rejects the mix.
        byte[] region0 = encryptRegionWithNonce(cek, javaNonce(0), randomBytes(REGION_DATA_LENGTH));
        byte[] region1 = encryptRegionWithNonce(cek, dotnetNonce(1), randomBytes(REGION_DATA_LENGTH));

        assertThrows(IllegalStateException.class, () -> decrypt(cek, concat(region0, region1), 0));
    }

    @Test
    public void singleRegionRangedDownloadCannotDetectCollisionSubstitution() {
        // KNOWN LIMITATION (documented in DecryptorV2): a download containing only one region cannot cross-check
        // regions to establish which SDK's nonce scheme the blob uses. Because the schemes share a value space
        // (javaNonce(1) == dotnetNonce(16,777,215)), a lone region's nonce is valid for its position under more than
        // one scheme. Here a Java-encoded region 1 is served for a single-region ranged read of region 16,777,215; it
        // matches the .NET scheme for that position, so it is accepted rather than flagged. A full or multi-region
        // download anchors the scheme from earlier regions and DOES detect such substitutions (see
        // rejectsMixedNonceEncodingsAcrossCollidingValueSpace). This matches the other SDKs' cross-SDK detection.
        assertArrayEquals(javaNonce(1), dotnetNonce(16_777_215));

        byte[] cek = randomBytes(32);
        byte[] region1Plaintext = randomBytes(REGION_DATA_LENGTH);
        byte[] substituted = encryptRegionWithNonce(cek, javaNonce(1), region1Plaintext);

        long collidingOffset = 16_777_215L * REGION_DATA_LENGTH;
        byte[] recovered = decrypt(cek, substituted, collidingOffset);

        // The substitution is not detected on a single-region ranged download; the region still decrypts under its
        // inline nonce. (A multi-region download would have anchored the scheme and rejected this.)
        assertArrayEquals(region1Plaintext, recovered);
    }

    @Test
    public void sharedValidatorEnforcesSchemeAcrossChunks() {
        // A download operation may span multiple decrypt() calls (parallel downloadToFile / chunked openInputStream).
        // A shared validator must intersect the candidate encodings across chunks; otherwise the encoding could change
        // at a chunk boundary and, at an encoding collision, let a relocated region pass. Mirrors the Python SDK's
        // test_nonce_validator_enforces_single_encoding_across_chunks.
        assertArrayEquals(javaNonce(1), dotnetNonce(16_777_215));

        byte[] cek = randomBytes(32);
        CseV2NonceOrderValidator shared = new CseV2NonceOrderValidator();

        // First chunk: two Java-encoded regions resolve the shared encoding to Java.
        byte[] chunk1 = concat(encryptRegionWithNonce(cek, javaNonce(0), randomBytes(REGION_DATA_LENGTH)),
            encryptRegionWithNonce(cek, javaNonce(1), randomBytes(REGION_DATA_LENGTH)));
        decrypt(cek, chunk1, 0, shared);

        // Later chunk: a region relocated to the colliding .NET index carries Java's region-1 nonce. On a fresh
        // per-chunk validator its only consistent encoding is .NET and it would pass; the shared validator (already
        // resolved to Java) rejects it.
        byte[] relocated = encryptRegionWithNonce(cek, javaNonce(1), randomBytes(REGION_DATA_LENGTH));
        long collidingOffset = 16_777_215L * REGION_DATA_LENGTH;
        assertThrows(IllegalStateException.class, () -> decrypt(cek, relocated, collidingOffset, shared));
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
     * Encrypts a single region using the Java SDK nonce scheme (region index truncated to an int, written big-endian).
     * Produces {@code nonce || ciphertext || tag}. Used to craft ciphertext for arbitrary (very large) region indices
     * without materializing all preceding regions.
     */
    private static byte[] encryptRegionAt(byte[] cek, long regionIndex, byte[] plaintext) {
        return encryptRegionWithNonce(cek, javaNonce(regionIndex), plaintext);
    }

    private static byte[] encryptRegionWithNonce(byte[] cek, byte[] nonce, byte[] plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(cek, AES), new GCMParameterSpec(TAG_LENGTH * 8, nonce));
            return concat(nonce, cipher.doFinal(plaintext));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    /** Builds a multi-region CSEv2 blob whose region i uses the nonce produced by {@code nonceForRegion}. */
    private static byte[] buildBlob(byte[] cek, java.util.function.LongFunction<byte[]> nonceForRegion,
        byte[][] regionPlaintext) {
        byte[] result = new byte[0];
        for (int i = 0; i < regionPlaintext.length; i++) {
            result = concat(result, encryptRegionWithNonce(cek, nonceForRegion.apply(i), regionPlaintext[i]));
        }
        return result;
    }

    // Independent implementations of each SDK's nonce scheme, serving as cross-SDK test vectors.

    private static byte[] javaNonce(long regionIndex) {
        return ByteBuffer.allocate(NONCE_LENGTH).putLong((int) regionIndex).array();
    }

    private static byte[] pythonNonce(long regionIndex) {
        // 12-byte big-endian integer (counter in the low bytes), 0-based.
        byte[] nonce = new byte[NONCE_LENGTH];
        for (int i = 0; i < Long.BYTES; i++) {
            nonce[NONCE_LENGTH - 1 - i] = (byte) (regionIndex >>> (Byte.SIZE * i));
        }
        return nonce;
    }

    private static byte[] dotnetNonce(long regionIndex) {
        // Four zero bytes then an 8-byte little-endian counter, 1-based.
        byte[] nonce = new byte[NONCE_LENGTH];
        long counter = regionIndex + 1;
        for (int i = 0; i < Long.BYTES; i++) {
            nonce[(NONCE_LENGTH - Long.BYTES) + i] = (byte) (counter >>> (Byte.SIZE * i));
        }
        return nonce;
    }

    private static byte[] flatten(byte[][] arrays) {
        byte[] result = new byte[0];
        for (byte[] a : arrays) {
            result = concat(result, a);
        }
        return result;
    }

    private static byte[] decrypt(byte[] cek, byte[] ciphertext, long offset) {
        return decrypt(cek, ciphertext, offset, new CseV2NonceOrderValidator());
    }

    private static byte[] decrypt(byte[] cek, byte[] ciphertext, long offset, CseV2NonceOrderValidator validator) {
        EncryptionData encryptionData = new EncryptionData()
            .setEncryptionAgent(new EncryptionAgent(ENCRYPTION_PROTOCOL_V2, EncryptionAlgorithm.AES_GCM_256))
            .setEncryptedRegionInfo(new EncryptedRegionInfo(REGION_DATA_LENGTH, NONCE_LENGTH));
        DecryptorV2 decryptor = new DecryptorV2(null, null, encryptionData, validator);
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
