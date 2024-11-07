// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.GCM_ENCRYPTION_REGION_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EncryptedBlobLengthTests extends BlobCryptographyTestBase {

    //to prevent having to cast int -> long everywhere
    private static final long FOUR_MB = 4 * Constants.MB;
    private static final long SIXTEEN_MB = 16 * Constants.MB;
    private static final long ONE_MB = Constants.MB;
    private static final long ONE_KB = Constants.KB;

    @ParameterizedTest
    @MethodSource("correctAdjustedLengthV1Supplier")
    public void correctAdjustedLengthV1(Long encryptedLength) {
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setEncryptionAgent(new EncryptionAgent("1.0", null));

        Long newLength = EncryptedBlobLength.computeAdjustedBlobLength(encryptionData, encryptedLength);
        assertEquals(encryptedLength, newLength);
    }

    private static Stream<Arguments> correctAdjustedLengthV1Supplier() {
        return Stream.of(Arguments.of(FOUR_MB), Arguments.of(SIXTEEN_MB));
    }

    @ParameterizedTest
    @MethodSource("correctAdjustedLengthV2Supplier")
    public void correctAdjustedLengthV2(Long encryptedLength, Long expectedLength) {
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setEncryptionAgent(new EncryptionAgent("2.0", null));
        encryptionData.setEncryptedRegionInfo(new EncryptedRegionInfo(GCM_ENCRYPTION_REGION_LENGTH, NONCE_LENGTH));

        Long newLength = EncryptedBlobLength.computeAdjustedBlobLength(encryptionData, encryptedLength);
        assertEquals(expectedLength, newLength);
    }

    private static Stream<Arguments> correctAdjustedLengthV2Supplier() {
        return Stream.of(Arguments.of(FOUR_MB + 28, FOUR_MB), Arguments.of(FOUR_MB + 57, FOUR_MB + 1),
            Arguments.of(SIXTEEN_MB + 112, SIXTEEN_MB), Arguments.of(28L, 0L), Arguments.of(0L, 0L),
            Arguments.of(ONE_MB + 28, ONE_MB));
    }

    @ParameterizedTest
    @MethodSource("correctAdjustedLengthVariableRegionSupplier")
    public void correctAdjustedLengthVariableRegion(Long encryptedLength, Long expectedLength, Long regionLength) {
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setEncryptionAgent(new EncryptionAgent("2.1", null));
        encryptionData.setEncryptedRegionInfo(new EncryptedRegionInfo(regionLength, NONCE_LENGTH));

        Long newLength = EncryptedBlobLength.computeAdjustedBlobLength(encryptionData, encryptedLength);
        assertEquals(expectedLength, newLength);
    }

    private static Stream<Arguments> correctAdjustedLengthVariableRegionSupplier() {
        return Stream.of(Arguments.of(FOUR_MB + 112, FOUR_MB, ONE_MB),
            Arguments.of(SIXTEEN_MB + 448, SIXTEEN_MB, ONE_MB), Arguments.of(FOUR_MB + 114688, FOUR_MB, ONE_KB),
            Arguments.of(SIXTEEN_MB + 458752, SIXTEEN_MB, ONE_KB), Arguments.of(FOUR_MB + 448, FOUR_MB, 256 * ONE_KB),
            Arguments.of(SIXTEEN_MB + 1792, SIXTEEN_MB, 256 * ONE_KB), Arguments.of(ONE_MB + 28672, ONE_MB, ONE_KB),
            Arguments.of(ONE_MB + 28, ONE_MB, ONE_MB));
    }

    @Test
    public void badProtocol() {
        EncryptionData encryptionData = new EncryptionData();
        encryptionData.setEncryptionAgent(new EncryptionAgent("garbage", null));

        assertThrows(IllegalArgumentException.class,
            () -> EncryptedBlobLength.computeAdjustedBlobLength(encryptionData, null));
    }

}
