// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentUnderstandingCommonApiControlFlowTest {
    @Test
    public void syncSuccessfulResultIsReturned() {
        assertEquals("result", ContentUnderstandingCommonApiTestBase
            .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> "result", "Test"));
    }

    @Test
    public void syncFailedAndCancelledStatusesDoNotFetchResult() {
        for (LongRunningOperationStatus status : new LongRunningOperationStatus[] {
            LongRunningOperationStatus.FAILED,
            LongRunningOperationStatus.USER_CANCELLED }) {
            AtomicBoolean fetched = new AtomicBoolean();

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ContentUnderstandingCommonApiTestBase.requireSuccessfulResult(status, () -> {
                    fetched.set(true);
                    return "result";
                }, "Test"));

            assertTrue(exception.getMessage().contains(status.toString()));
            assertFalse(fetched.get());
        }
    }

    @Test
    public void syncEmptyFinalResultIsRejected() {
        IllegalStateException exception
            = assertThrows(IllegalStateException.class, () -> ContentUnderstandingCommonApiTestBase
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, () -> null, "Test"));

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void asyncSuccessfulResultIsReturned() {
        assertEquals("result",
            ContentUnderstandingCommonApiTestBase
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.just("result"), "Test")
                .block());
    }

    @Test
    public void asyncFailedAndCancelledStatusesDoNotSubscribeToResult() {
        for (LongRunningOperationStatus status : new LongRunningOperationStatus[] {
            LongRunningOperationStatus.FAILED,
            LongRunningOperationStatus.USER_CANCELLED }) {
            AtomicBoolean subscribed = new AtomicBoolean();
            Mono<String> finalResult = Mono.just("result").doOnSubscribe(ignored -> subscribed.set(true));

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> ContentUnderstandingCommonApiTestBase.requireSuccessfulResult(status, finalResult, "Test")
                    .block());

            assertTrue(exception.getMessage().contains(status.toString()));
            assertFalse(subscribed.get());
        }
    }

    @Test
    public void asyncEmptyFinalResultIsRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingCommonApiTestBase
                .requireSuccessfulResult(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, Mono.empty(), "Test")
                .block());

        assertTrue(exception.getMessage().contains("without a final result"));
    }

    @Test
    public void operationIdIsRequired() {
        ContentAnalyzerAnalyzeOperationStatus valid
            = BinaryData.fromString("{\"id\":\"operation-1\",\"status\":\"Succeeded\"}")
                .toObject(ContentAnalyzerAnalyzeOperationStatus.class);
        ContentAnalyzerAnalyzeOperationStatus blank = BinaryData.fromString("{\"id\":\" \",\"status\":\"Succeeded\"}")
            .toObject(ContentAnalyzerAnalyzeOperationStatus.class);

        assertEquals("operation-1", ContentUnderstandingCommonApiTestBase.requireOperationId(valid, "Test"));
        assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingCommonApiTestBase.requireOperationId(null, "Test"));
        assertThrows(IllegalStateException.class,
            () -> ContentUnderstandingCommonApiTestBase.requireOperationId(blank, "Test"));
    }
}
