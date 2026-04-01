// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.Context;
import com.azure.storage.common.StorageChecksumAlgorithm;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_MODE_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContentValidationModeResolverTests {

    private static String modeOnContext(Context context, StorageChecksumAlgorithm algorithm, long contentLength,
        boolean chunkedUpload) {
        return ContentValidationModeResolver.addContentValidationMode(context, algorithm, contentLength, chunkedUpload)
            .getData(CONTENT_VALIDATION_MODE_KEY)
            .map(Object::toString)
            .orElse(null);
    }

    // ===========================================================================================
    // addContentValidationMode (Context) — single-part
    // ===========================================================================================

    static Stream<Arguments> singlePartDoesNotSetModeSupplier() {
        return Stream.of(Arguments.of(null, 1024), Arguments.of(StorageChecksumAlgorithm.NONE, 1024),
            Arguments.of(null, 8 * 1024 * 1024), Arguments.of(StorageChecksumAlgorithm.NONE, 8 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("singlePartDoesNotSetModeSupplier")
    public void singlePartDoesNotSetModeForNullOrNone(StorageChecksumAlgorithm algorithm, long length) {
        assertEquals(null, modeOnContext(Context.NONE, algorithm, length, false));
    }

    @Test
    public void singlePartSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.CRC64, underThreshold, false));
    }

    @Test
    public void singlePartAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, modeOnContext(Context.NONE, StorageChecksumAlgorithm.CRC64,
            MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER, false));
    }

    @Test
    public void singlePartLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.CRC64, overThreshold, false));
    }

    @Test
    public void singlePartAutoSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.AUTO, underThreshold, false));
    }

    @Test
    public void singlePartAutoAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, modeOnContext(Context.NONE, StorageChecksumAlgorithm.AUTO,
            MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER, false));
    }

    @Test
    public void singlePartAutoLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.AUTO, overThreshold, false));
    }

    @Test
    public void addContentValidationModeNullContextUsesNone() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(null, StorageChecksumAlgorithm.CRC64, underThreshold, false));
    }

    // ===========================================================================================
    // addContentValidationMode (Context) — chunked
    // ===========================================================================================

    static Stream<Arguments> chunkedDoesNotSetModeSupplier() {
        return Stream.of(Arguments.of((StorageChecksumAlgorithm) null), Arguments.of(StorageChecksumAlgorithm.NONE));
    }

    @ParameterizedTest
    @MethodSource("chunkedDoesNotSetModeSupplier")
    public void chunkedDoesNotSetModeForNonCrc64Algorithms(StorageChecksumAlgorithm algorithm) {
        assertEquals(null, modeOnContext(Context.NONE, algorithm, 1024, true));
    }

    @Test
    public void chunkedCrc64AlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.CRC64, 1024, true));
    }

    @Test
    public void chunkedAutoAlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, StorageChecksumAlgorithm.AUTO, 1024, true));
    }

    // ===========================================================================================
    // addContentValidationMode (Mono)
    // ===========================================================================================

    @Test
    public void addContentValidationModeMonoWritesReactorContextForCrc64() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just(ctx.get(CONTENT_VALIDATION_MODE_KEY)));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            StorageChecksumAlgorithm.CRC64, underThreshold, false);
        StepVerifier.create(augmented).expectNext(USE_CRC64_CHECKSUM_HEADER_CONTEXT).verifyComplete();
    }

    @Test
    public void addContentValidationModeMonoWritesReactorContextForAuto() {
        long underThreshold = MAXIMUM_SINGLE_PART_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just(ctx.get(CONTENT_VALIDATION_MODE_KEY)));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            StorageChecksumAlgorithm.AUTO, underThreshold, false);
        StepVerifier.create(augmented).expectNext(USE_CRC64_CHECKSUM_HEADER_CONTEXT).verifyComplete();
    }

    @Test
    public void addContentValidationModeMonoLeavesChainUnchangedWhenNoMode() {
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just("ok"));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            StorageChecksumAlgorithm.NONE, 1024, false);
        StepVerifier.create(augmented).expectNext("ok").verifyComplete();
    }

    // ===========================================================================================
    // validateTransactionalChecksumOptions (byte[])
    // ===========================================================================================

    @Test
    public void validateByteArrayPassesForCompatibleOptions() {
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(null,
            StorageChecksumAlgorithm.CRC64));
        assertDoesNotThrow(
            () -> ContentValidationModeResolver.validateTransactionalChecksumOptions(new byte[] { 1 }, null));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(new byte[] { 1, 2 },
            StorageChecksumAlgorithm.NONE));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(null, null));
    }

    @Test
    public void validateByteArrayThrowsForContentMd5AndCrc64() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(new byte[] { 1 }, StorageChecksumAlgorithm.CRC64));
        assertEquals(ContentValidationModeResolver.CONFLICTING_TRANSACTIONAL_CONTENT_VALIDATION_MESSAGE,
            ex.getMessage());
    }

    @Test
    public void validateByteArrayThrowsForContentMd5AndAuto() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(new byte[] { 1, 2 }, StorageChecksumAlgorithm.AUTO));
    }

    // ===========================================================================================
    // validateTransactionalChecksumOptions (boolean computeMd5)
    // ===========================================================================================

    @Test
    public void validateComputeMd5PassesForCompatibleOptions() {
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(true, null));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            StorageChecksumAlgorithm.AUTO));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            StorageChecksumAlgorithm.NONE));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void validateComputeMd5ThrowsWhenAlgorithmNone() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, StorageChecksumAlgorithm.NONE));
    }

    @Test
    public void validateComputeMd5ThrowsForCrc64() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, StorageChecksumAlgorithm.CRC64));
    }

    @Test
    public void validateComputeMd5ThrowsForAuto() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, StorageChecksumAlgorithm.AUTO));
    }
}
