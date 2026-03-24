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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentValidationBehaviorUtilTests {

    // ===========================================================================================
    // getBehaviorForSinglePartUpload
    // ===========================================================================================

    static Stream<Arguments> singlePartReturnsNullSupplier() {
        return Stream.of(Arguments.of(null, 1024), Arguments.of(StorageChecksumAlgorithm.NONE, 1024),
            Arguments.of(StorageChecksumAlgorithm.MD5, 1024), Arguments.of(null, 8 * 1024 * 1024),
            Arguments.of(StorageChecksumAlgorithm.NONE, 8 * 1024 * 1024),
            Arguments.of(StorageChecksumAlgorithm.MD5, 8 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("singlePartReturnsNullSupplier")
    public void singlePartReturnsNullForNonCrc64Algorithms(StorageChecksumAlgorithm algorithm, long length) {
        assertNull(ContentValidationBehaviorUtil.getBehaviorForSinglePartUpload(algorithm, length));
    }

    @Test
    public void singlePartSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT, ContentValidationBehaviorUtil
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.CRC64, underThreshold));
    }

    @Test
    public void singlePartAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationBehaviorUtil.getBehaviorForSinglePartUpload(
            StorageChecksumAlgorithm.CRC64, MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER));
    }

    @Test
    public void singlePartLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationBehaviorUtil
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.CRC64, overThreshold));
    }

    @Test
    public void singlePartAutoSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT, ContentValidationBehaviorUtil
            .getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.AUTO, underThreshold));
    }

    @Test
    public void singlePartAutoAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, ContentValidationBehaviorUtil.getBehaviorForSinglePartUpload(
            StorageChecksumAlgorithm.AUTO, MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER));
    }

    @Test
    public void singlePartAutoLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationBehaviorUtil.getBehaviorForSinglePartUpload(StorageChecksumAlgorithm.AUTO, overThreshold));
    }

    // ===========================================================================================
    // getBehaviorForChunkedUpload
    // ===========================================================================================

    static Stream<Arguments> chunkedReturnsNullSupplier() {
        return Stream.of(Arguments.of((StorageChecksumAlgorithm) null), Arguments.of(StorageChecksumAlgorithm.NONE),
            Arguments.of(StorageChecksumAlgorithm.MD5));
    }

    @ParameterizedTest
    @MethodSource("chunkedReturnsNullSupplier")
    public void chunkedReturnsNullForNonCrc64Algorithms(StorageChecksumAlgorithm algorithm) {
        assertNull(ContentValidationBehaviorUtil.getBehaviorForChunkedUpload(algorithm));
    }

    @Test
    public void chunkedCrc64AlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationBehaviorUtil.getBehaviorForChunkedUpload(StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void chunkedAutoAlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            ContentValidationBehaviorUtil.getBehaviorForChunkedUpload(StorageChecksumAlgorithm.AUTO));
    }

    // ===========================================================================================
    // hasConflictingTransactionalContentValidation
    // ===========================================================================================

    @Test
    public void noConflictWhenBothNull() {
        assertFalse(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(null, null));
    }

    @Test
    public void noConflictWhenOnlyMd5() {
        assertFalse(
            ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 }, null));
    }

    @Test
    public void noConflictWhenOnlyCrc64() {
        assertFalse(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(null,
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void noConflictWhenMd5WithNone() {
        assertFalse(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.NONE));
    }

    @Test
    public void conflictWhenMd5WithCrc64() {
        assertTrue(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void conflictWhenMd5WithAuto() {
        assertTrue(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.AUTO));
    }

    @Test
    public void conflictWhenMd5WithMd5Algorithm() {
        assertTrue(ContentValidationBehaviorUtil.hasConflictingTransactionalContentValidation(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.MD5));
    }
}
