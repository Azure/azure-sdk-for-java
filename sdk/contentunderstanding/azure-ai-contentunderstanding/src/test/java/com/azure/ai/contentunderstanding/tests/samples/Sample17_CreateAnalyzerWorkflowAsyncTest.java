// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentNumberField;
import com.azure.ai.contentunderstanding.models.ContentStringField;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample17_CreateAnalyzerWorkflowAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testCreateAnalyzerWorkflowRoundTripAsync() {
        String defaultId = testResourceNamer.randomName("workflow_roundtrip_default_", 50);
        String agenticId = testResourceNamer.randomName("workflow_roundtrip_agentic_", 50);
        String completionModel = getModelProfile().getCompletionModel();
        try {
            ContentAnalyzer defaultAnalyzer = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(defaultId, PreviewSampleTestSupport.createDefaultWorkflowAnalyzer(completionModel),
                    true)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Default workflow analyzer creation"))
                .block();
            ContentAnalyzer agenticAnalyzer = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(agenticId, PreviewSampleTestSupport.createAgenticWorkflowAnalyzer(completionModel),
                    true)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Agentic workflow analyzer creation"))
                .block();

            assertNotNull(defaultAnalyzer);
            assertNotNull(agenticAnalyzer);
            assertNotNull(defaultAnalyzer.getConfig().getWorkflow());
            assertNotNull(agenticAnalyzer.getConfig().getWorkflow());
            assertFalse(
                defaultAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));
            assertTrue(
                agenticAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));
        } finally {
            delete(defaultId);
            delete(agenticId);
        }
    }

    @Test
    public void testCreateAnalyzerWorkflowAsync() throws Exception {
        String defaultId = testResourceNamer.randomName("workflow_default_", 50);
        String agenticId = testResourceNamer.randomName("workflow_agentic_", 50);
        String completionModel = getModelProfile().getCompletionModel();
        try {
            ContentAnalyzer defaultAnalyzer = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(defaultId, PreviewSampleTestSupport.createDefaultWorkflowAnalyzer(completionModel),
                    true)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Default workflow analyzer creation"))
                .block();
            ContentAnalyzer agenticAnalyzer = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(agenticId, PreviewSampleTestSupport.createAgenticWorkflowAnalyzer(completionModel),
                    true)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Agentic workflow analyzer creation"))
                .block();
            assertNotNull(defaultAnalyzer);
            assertNotNull(agenticAnalyzer);
            assertNotNull(defaultAnalyzer.getConfig().getWorkflow());
            assertNotNull(agenticAnalyzer.getConfig().getWorkflow());
            assertFalse(
                defaultAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));
            assertTrue(
                agenticAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));

            BinaryData invoice = PreviewSampleTestSupport.readSample("workflow_invoice_20_items.pdf");
            AnalysisResult defaultResult = contentUnderstandingAsyncClient.beginAnalyzeBinary(defaultId, invoice)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Default workflow analysis"))
                .block();
            AnalysisResult agenticResult = contentUnderstandingAsyncClient.beginAnalyzeBinary(agenticId, invoice)
                .last()
                .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(),
                    "Agentic workflow analysis"))
                .block();
            assertNotNull(defaultResult);
            assertNotNull(agenticResult);
            assertFalse(defaultResult.getContents().isEmpty());
            assertFalse(agenticResult.getContents().isEmpty());
            assertTrue(defaultResult.getContents().get(0) instanceof DocumentContent);
            assertTrue(agenticResult.getContents().get(0) instanceof DocumentContent);
            DocumentContent defaultContent = (DocumentContent) defaultResult.getContents().get(0);
            DocumentContent agenticContent = (DocumentContent) agenticResult.getContents().get(0);
            assertEquals("INV-2048", ((ContentStringField) defaultContent.getFields().get("InvoiceId")).getValue());
            assertEquals("INV-2048", ((ContentStringField) agenticContent.getFields().get("InvoiceId")).getValue());
            Double agenticAverage
                = ((ContentNumberField) agenticContent.getFields().get("AverageItemPrice")).getValue();
            assertNotNull(agenticAverage);
            assertEquals(20.5, agenticAverage, 0.01);
        } finally {
            delete(defaultId);
            delete(agenticId);
        }
    }

    private static <T> Mono<T> requireSuccessfulResult(LongRunningOperationStatus status, Mono<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono
                .error(new IllegalStateException(operationName + " completed unsuccessfully with status: " + status));
        }
        return finalResult
            .switchIfEmpty(Mono.error(new IllegalStateException(operationName + " completed without a final result.")));
    }

    private void delete(String analyzerId) {
        contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId).onErrorResume(error -> {
            System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
            return Mono.empty();
        }).block();
    }
}
