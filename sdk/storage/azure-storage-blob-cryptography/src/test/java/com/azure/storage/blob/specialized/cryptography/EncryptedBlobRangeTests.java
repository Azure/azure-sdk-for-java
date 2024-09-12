// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.GCM_ENCRYPTION_REGION_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncryptedBlobRangeTests extends BlobCryptographyTestBase {
    // This test checks that the EncryptedBlobRange cna be properly constructed from a BlobRange
    @ParameterizedTest
    @CsvSource(value = {",,bytes=0-", "3,,bytes=0-", "17,,bytes=0-", "34,,bytes=16-", "47,,bytes=16-", "48,,bytes=32-",
        "2,6,bytes=0-15", "18,2,bytes=0-31", "38,17,bytes=16-63"})
    public void testConstructor(Integer offset, Integer count, String expectedString) {
        EncryptedBlobRange ebr = new EncryptedBlobRange(getBlobRange(offset, count),
            new EncryptionData().setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V1)));

        assertEquals(expectedString, ebr.toBlobRange().toString());
    }

    @ParameterizedTest
    @MethodSource("constructorV2Supplier")
    public void constructorV2(Integer offset, Integer count, String expectedString) {
        EncryptedBlobRange ebr = new EncryptedBlobRange(getBlobRange(offset, count),
            new EncryptionData()
                .setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V2))
                .setEncryptedRegionInfo(new EncryptedRegionInfo(GCM_ENCRYPTION_REGION_LENGTH, NONCE_LENGTH)));

        assertEquals(expectedString, ebr.toBlobRange().toString());
    }

    private static Stream<Arguments> constructorV2Supplier() {
        return Stream.of(
            Arguments.of(null, null, "bytes=0-"), // Both null
            Arguments.arguments(3, null, "bytes=0-"), // Only offset specified. First region
            Arguments.of(GCM_ENCRYPTION_REGION_LENGTH - 1, null, "bytes=0-"), // 4mb + 28 - 1
            Arguments.of(GCM_ENCRYPTION_REGION_LENGTH + 1024, null, "bytes=4194332-"), // Second region
            Arguments.of(2 * GCM_ENCRYPTION_REGION_LENGTH, GCM_ENCRYPTION_REGION_LENGTH, "bytes=8388664-12582995"), // Third region exact
            Arguments.of(2, 6, "bytes=0-4194331"), // Two parameters specified. All in first region. 4mb + 28
            Arguments.of(5000000, 5000000, "bytes=4194332-12582995"), // Second to third region.
            Arguments.of(5000000, 20165000, "bytes=4194332-25165991") // Second to fifth region
        );
    }

    // This test checks that the encrypted blob range can be correctly constructed from a BlobRange header string
    @ParameterizedTest
    @CsvSource(value = {",", "3,", "17,", "34,", "47,", "48,", "2,6", "18,2", "38,17"})
    public void testFromBlobRangeHeader(Integer offset, Integer count) {
        EncryptionData encryptionDataV1 = new EncryptionData()
            .setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V1));
        EncryptionData encryptionDataV2 = new EncryptionData()
            .setEncryptionAgent(new EncryptionAgent().setProtocol(ENCRYPTION_PROTOCOL_V2))
            .setEncryptedRegionInfo(new EncryptedRegionInfo(GCM_ENCRYPTION_REGION_LENGTH, NONCE_LENGTH));
        BlobRange range = getBlobRange(offset, count);

        EncryptedBlobRange encryptedRangeFromBlobRangeV1 = new EncryptedBlobRange(range, encryptionDataV1);
        EncryptedBlobRange encryptedRangeFromHeaderV1 =
            EncryptedBlobRange.getEncryptedBlobRangeFromHeader(range.toHeaderValue(), encryptionDataV1);
        EncryptedBlobRange encryptedRangeFromBlobRangeV2 = new EncryptedBlobRange(range, encryptionDataV2);
        EncryptedBlobRange encryptedRangeFromHeaderV2 =
            EncryptedBlobRange.getEncryptedBlobRangeFromHeader(range.toHeaderValue(), encryptionDataV2);

        assertEquals(encryptedRangeFromHeaderV1.toBlobRange().toHeaderValue(),
            encryptedRangeFromBlobRangeV1.toBlobRange().toHeaderValue());
        assertEquals(encryptedRangeFromHeaderV2.toBlobRange().toHeaderValue(),
            encryptedRangeFromBlobRangeV2.toBlobRange().toHeaderValue());
    }

    private static BlobRange getBlobRange(Integer offset, Integer count) {
        if (offset == null && count == null) {
            return new BlobRange(0);
        } else if (count == null) {
            return new BlobRange(offset);
        } else {
            return new BlobRange(offset, (long) count);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {
        16,
        4 * Constants.KB,
        4 * Constants.MB,
        Constants.GB
    })
    public void encryptedBlobRangeFromEncryptionData(int regionLength) {
        long dataSize = 4 * Constants.MB;
        EncryptionData encryptionData = new EncryptionData()
            .setEncryptionAgent(new EncryptionAgent(ENCRYPTION_PROTOCOL_V2, EncryptionAlgorithm.AES_GCM_256))
            .setEncryptedRegionInfo(new EncryptedRegionInfo(regionLength, NONCE_LENGTH));

        EncryptedBlobRange encryptedBlobRange = new EncryptedBlobRange(new BlobRange(0, dataSize), encryptionData);

        int expectedRegionCount = (int) (dataSize - 1) / regionLength;
        int expectedAdjustedDownloadCount = (expectedRegionCount + 1) * (NONCE_LENGTH + regionLength + TAG_LENGTH);

        // check if the region length is being used correctly with the expected adjusted download count
        assertEquals(expectedAdjustedDownloadCount, encryptedBlobRange.getAdjustedDownloadCount());
    }
}
