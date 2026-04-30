// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation.contentvalidation;

import com.azure.core.util.Context;
import com.azure.core.util.ProgressListener;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ParallelTransferOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.CONTENT_VALIDATION_MODE_KEY;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_CRC64_CHECKSUM_HEADER_CONTEXT;
import static com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants.USE_STRUCTURED_MESSAGE_CONTEXT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ContentValidationModeResolverTests {

    private static String modeOnContext(Context context, ContentValidationAlgorithm algorithm, long contentLength,
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
        return Stream.of(Arguments.of(null, 1024), Arguments.of(ContentValidationAlgorithm.NONE, 1024),
            Arguments.of(null, 8 * 1024 * 1024), Arguments.of(ContentValidationAlgorithm.NONE, 8 * 1024 * 1024));
    }

    @ParameterizedTest
    @MethodSource("singlePartDoesNotSetModeSupplier")
    public void singlePartDoesNotSetModeForNullOrNone(ContentValidationAlgorithm algorithm, long length) {
        assertEquals(null, modeOnContext(Context.NONE, algorithm, length, false));
    }

    @Test
    public void singlePartSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.CRC64, underThreshold, false));
    }

    @Test
    public void singlePartAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, modeOnContext(Context.NONE, ContentValidationAlgorithm.CRC64,
            MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER, false));
    }

    @Test
    public void singlePartLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.CRC64, overThreshold, false));
    }

    @Test
    public void singlePartAutoSmallUploadUsesCrc64Header() {
        long underThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.AUTO, underThreshold, false));
    }

    @Test
    public void singlePartAutoAtExact4MBBoundaryUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT, modeOnContext(Context.NONE, ContentValidationAlgorithm.AUTO,
            MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER, false));
    }

    @Test
    public void singlePartAutoLargeUploadUsesStructuredMessage() {
        long overThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER + 1;
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.AUTO, overThreshold, false));
    }

    @Test
    public void addContentValidationModeNullContextUsesNone() {
        long underThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        assertEquals(USE_CRC64_CHECKSUM_HEADER_CONTEXT,
            modeOnContext(null, ContentValidationAlgorithm.CRC64, underThreshold, false));
    }

    // ===========================================================================================
    // addContentValidationMode (Context) — chunked
    // ===========================================================================================

    static Stream<Arguments> chunkedDoesNotSetModeSupplier() {
        return Stream.of(Arguments.of((ContentValidationAlgorithm) null),
            Arguments.of(ContentValidationAlgorithm.NONE));
    }

    @ParameterizedTest
    @MethodSource("chunkedDoesNotSetModeSupplier")
    public void chunkedDoesNotSetModeForNonCrc64Algorithms(ContentValidationAlgorithm algorithm) {
        assertEquals(null, modeOnContext(Context.NONE, algorithm, 1024, true));
    }

    @Test
    public void chunkedCrc64AlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.CRC64, 1024, true));
    }

    @Test
    public void chunkedAutoAlwaysUsesStructuredMessage() {
        assertEquals(USE_STRUCTURED_MESSAGE_CONTEXT,
            modeOnContext(Context.NONE, ContentValidationAlgorithm.AUTO, 1024, true));
    }

    // ===========================================================================================
    // addContentValidationMode (Mono)
    // ===========================================================================================

    @Test
    public void addContentValidationModeMonoWritesReactorContextForCrc64() {
        long underThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just(ctx.get(CONTENT_VALIDATION_MODE_KEY)));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            ContentValidationAlgorithm.CRC64, underThreshold, false);
        StepVerifier.create(augmented).expectNext(USE_CRC64_CHECKSUM_HEADER_CONTEXT).verifyComplete();
    }

    @Test
    public void addContentValidationModeMonoWritesReactorContextForAuto() {
        long underThreshold = MAXIMUM_SINGLE_SHOT_UPLOAD_SIZE_TO_USE_CRC64_HEADER - 1;
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just(ctx.get(CONTENT_VALIDATION_MODE_KEY)));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            ContentValidationAlgorithm.AUTO, underThreshold, false);
        StepVerifier.create(augmented).expectNext(USE_CRC64_CHECKSUM_HEADER_CONTEXT).verifyComplete();
    }

    @Test
    public void addContentValidationModeMonoLeavesChainUnchangedWhenNoMode() {
        Mono<String> source = Mono.deferContextual(ctx -> Mono.just("ok"));
        Mono<String> augmented = ContentValidationModeResolver.addContentValidationMode(source,
            ContentValidationAlgorithm.NONE, 1024, false);
        StepVerifier.create(augmented).expectNext("ok").verifyComplete();
    }

    // ===========================================================================================
    // validateTransactionalChecksumOptions (boolean computeMd5)
    // ===========================================================================================

    @Test
    public void validateComputeMd5PassesForCompatibleOptions() {
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(true, null));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            ContentValidationAlgorithm.AUTO));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            ContentValidationAlgorithm.NONE));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateTransactionalChecksumOptions(false,
            ContentValidationAlgorithm.CRC64));
    }

    @Test
    public void validateComputeMd5ThrowsWhenAlgorithmNone() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, ContentValidationAlgorithm.NONE));
    }

    @Test
    public void validateComputeMd5ThrowsForCrc64() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, ContentValidationAlgorithm.CRC64));
    }

    @Test
    public void validateComputeMd5ThrowsForAuto() {
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateTransactionalChecksumOptions(true, ContentValidationAlgorithm.AUTO));
    }

    // ===========================================================================================
    // validateProgressWithContentValidation
    // ===========================================================================================

    @Test
    public void validateProgressWithContentValidationPassesWhenNoProgress() {
        assertDoesNotThrow(() -> ContentValidationModeResolver
            .validateProgressWithContentValidation((ProgressListener) null, ContentValidationAlgorithm.CRC64));
        assertDoesNotThrow(() -> ContentValidationModeResolver
            .validateProgressWithContentValidation((ProgressListener) null, ContentValidationAlgorithm.AUTO));
    }

    @Test
    public void validateProgressWithContentValidationPassesWhenNoneOrNullAlgorithm() {
        ProgressListener listener = l -> {
        };
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateProgressWithContentValidation(listener, null));
        assertDoesNotThrow(() -> ContentValidationModeResolver.validateProgressWithContentValidation(listener,
            ContentValidationAlgorithm.NONE));
    }

    @Test
    public void validateProgressWithContentValidationPassesWhenParallelOptionsNull() {
        assertDoesNotThrow(() -> ContentValidationModeResolver
            .validateProgressWithContentValidation((ParallelTransferOptions) null, ContentValidationAlgorithm.CRC64));
    }

    @Test
    public void validateProgressWithContentValidationThrowsForCrc64() {
        ProgressListener listener = l -> {
        };
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateProgressWithContentValidation(listener, ContentValidationAlgorithm.CRC64));
        assertEquals(ContentValidationModeResolver.PROGRESS_CONFLICTS_TRANSFER_CONTENT_VALIDATION_MESSAGE,
            ex.getMessage());
    }

    @Test
    public void validateProgressWithContentValidationThrowsForAuto() {
        ProgressListener listener = l -> {
        };
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateProgressWithContentValidation(listener, ContentValidationAlgorithm.AUTO));
    }

    @Test
    public void validateProgressWithContentValidationParallelOptionsDelegatesToListener() {
        ParallelTransferOptions opts = new ParallelTransferOptions().setProgressListener(l -> {
        });
        assertThrows(IllegalArgumentException.class, () -> ContentValidationModeResolver
            .validateProgressWithContentValidation(opts, ContentValidationAlgorithm.CRC64));
    }
}
