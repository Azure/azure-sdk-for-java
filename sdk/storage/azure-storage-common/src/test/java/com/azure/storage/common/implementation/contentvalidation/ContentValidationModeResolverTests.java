// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.storage.common.StorageChecksumAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentValidationModeResolverTests {

    // ===========================================================================================
    // getBehaviorForSinglePartUpload
    // ===========================================================================================

    static Stream<Arguments> singlePartReturnsNullSupplier() {
        return Stream.of(Arguments.of(null, 1024), Arguments.of(StorageChecksumAlgorithm.NONE, 1024),
            Arguments.of(null, 8 * 1024 * 1024), Arguments.of(StorageChecksumAlgorithm.NONE, 8 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("singlePartReturnsNullSupplier")
    public void singlePartReturnsNullForNonCrc64Algorithms(StorageChecksumAlgorithm algorithm, long length) {
        assertNull(ContentValidationModeResolver.getBehaviorForSinglePartUpload(algorithm, length));
    }

    @Test
    public void singlePartSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT, ContentValidationModeResolver
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.CRC64, underThreshold));
    }

    @Test
    public void singlePartAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationModeResolver.getBehaviorForSinglePartUpload(
            StorageChecksumAlgorithm.CRC64, MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER));
    }

    @Test
    public void singlePartLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationModeResolver
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.CRC64, overThreshold));
    }

    @Test
    public void singlePartAutoSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT, ContentValidationModeResolver
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.AUTO, underThreshold));
    }

    @Test
    public void singlePartAutoAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationModeResolver.getBehaviorForSinglePartUpload(
            StorageChecksumAlgorithm.AUTO, MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER));
    }

    @Test
    public void singlePartAutoLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationModeResolver.getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.AUTO, overThreshold));
    }

    // ===========================================================================================
    // getBehaviorForChunkedUpload
    // ===========================================================================================

    static Stream<Arguments> chunkedReturnsNullSupplier() {
        return Stream.of(Arguments.of((StorageChecksumAlgorithm) null), Arguments.of(StorageChecksumAlgorithm.NONE));
    }

    @ParameterizedTest
    @MethodSource("chunkedReturnsNullSupplier")
    public void chunkedReturnsNullForNonCrc64Algorithms(StorageChecksumAlgorithm algorithm) {
        assertNull(ContentValidationModeResolver.getBehaviorForChunkedUpload(algorithm));
    }

    @Test
    public void chunkedCrc64AlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationModeResolver.getBehaviorForChunkedUpload(StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void chunkedAutoAlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationModeResolver.getBehaviorForChunkedUpload(StorageChecksumAlgorithm.AUTO));
    }

    // ===========================================================================================
    // hasConflictingTransactionalContentValidation
    // ===========================================================================================

    @Test
    public void noConflictWhenBothNull() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(null, null));
    }

    @Test
    public void noConflictWhenOnlyMd5() {
        assertFalse(
            ContentValidationModeResolver.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 }, null));
    }

    @Test
    public void noConflictWhenOnlyCrc64() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(null,
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void noConflictWhenMd5WithNone() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.NONE));
    }

    @Test
    public void conflictWhenMd5WithCrc64() {
        assertTrue(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void conflictWhenMd5WithAuto() {
        assertTrue(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.AUTO));
    }

    // ===========================================================================================
    // hasConflictingTransactionalContentValidation (computeMd5 overload)
    // ===========================================================================================

    @Test
    public void computeMd5NoConflictWhenAlgorithmNull() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(true, null));
    }

    @Test
    public void computeMd5NoConflictWhenAlgorithmNone() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(true,
            StorageChecksumAlgorithm.NONE));
    }

    @Test
    public void computeMd5ConflictWhenAlgorithmCrc64() {
        assertTrue(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(true,
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void noComputeMd5NoConflictWithCrc64() {
        assertFalse(ContentValidationModeResolver.hasConflictingTransactionalContentValidation(false,
            StorageChecksumAlgorithm.CRC64));
    }

    // ===========================================================================================
    // validateTransactionalChecksumOptions (sync)
    // ===========================================================================================

    @Test
    public void validateSyncPassesForCompatibleOptions() {
        ContentValidationModeResolver.validateTransactionalChecksumOptions(null, StorageChecksumAlgorithm.CRC64);
        ContentValidationModeResolver.validateTransactionalChecksumOptions(new byte[] { 1 }, null);
        ContentValidationModeResolver.validateTransactionalChecksumOptions(false, StorageChecksumAlgorithm.AUTO);
    }

    @Test
    public void validateSyncThrowsForContentMd5AndAlgorithm() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(new byte[] { 1 }, StorageChecksumAlgorithm.CRC64));
        assertEquals(ContentValidationModeResolver.CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE,
            ex.getMessage());
    }

    @Test
    public void validateSyncThrowsForComputeMd5AndAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, StorageChecksumAlgorithm.AUTO));
    }
}
