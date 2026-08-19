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
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample17_CreateAnalyzerWorkflowTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testCreateAnalyzerWorkflowRoundTrip() {
        String defaultId = testResourceNamer.randomName("workflow_roundtrip_default_", 50);
        String agenticId = testResourceNamer.randomName("workflow_roundtrip_agentic_", 50);
        String completionModel = getModelProfile().getCompletionModel();
        try {
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus, ContentAnalyzer> defaultPoller
                = contentUnderstandingClient.beginCreateAnalyzer(defaultId,
                    PreviewSampleTestSupport.createDefaultWorkflowAnalyzer(completionModel), true);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                defaultPoller.waitForCompletion().getStatus());
            ContentAnalyzer defaultAnalyzer = defaultPoller.getFinalResult();
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus, ContentAnalyzer> agenticPoller
                = contentUnderstandingClient.beginCreateAnalyzer(agenticId,
                    PreviewSampleTestSupport.createAgenticWorkflowAnalyzer(completionModel), true);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                agenticPoller.waitForCompletion().getStatus());
            ContentAnalyzer agenticAnalyzer = agenticPoller.getFinalResult();

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
    public void testCreateAnalyzerWorkflow() throws Exception {
        String defaultId = testResourceNamer.randomName("workflow_default_", 50);
        String agenticId = testResourceNamer.randomName("workflow_agentic_", 50);
        String completionModel = getModelProfile().getCompletionModel();
        try {
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus, ContentAnalyzer> defaultCreatePoller
                = contentUnderstandingClient.beginCreateAnalyzer(defaultId,
                    PreviewSampleTestSupport.createDefaultWorkflowAnalyzer(completionModel), true);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                defaultCreatePoller.waitForCompletion().getStatus());
            ContentAnalyzer defaultAnalyzer = defaultCreatePoller.getFinalResult();
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus, ContentAnalyzer> agenticCreatePoller
                = contentUnderstandingClient.beginCreateAnalyzer(agenticId,
                    PreviewSampleTestSupport.createAgenticWorkflowAnalyzer(completionModel), true);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                agenticCreatePoller.waitForCompletion().getStatus());
            ContentAnalyzer agenticAnalyzer = agenticCreatePoller.getFinalResult();

            assertNotNull(defaultAnalyzer);
            assertNotNull(agenticAnalyzer);
            assertNotNull(defaultAnalyzer.getConfig().getWorkflow());
            assertNotNull(agenticAnalyzer.getConfig().getWorkflow());
            assertFalse(
                defaultAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));
            assertTrue(
                agenticAnalyzer.getConfig().getWorkflow().toString().toLowerCase(Locale.ROOT).startsWith("agentic"));

            BinaryData invoice = PreviewSampleTestSupport.readSample("workflow_invoice_20_items.pdf");
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> defaultAnalysisPoller
                = contentUnderstandingClient.beginAnalyzeBinary(defaultId, invoice);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                defaultAnalysisPoller.waitForCompletion().getStatus());
            AnalysisResult defaultResult = defaultAnalysisPoller.getFinalResult();
            SyncPoller<com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> agenticAnalysisPoller
                = contentUnderstandingClient.beginAnalyzeBinary(agenticId, invoice);
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED,
                agenticAnalysisPoller.waitForCompletion().getStatus());
            AnalysisResult agenticResult = agenticAnalysisPoller.getFinalResult();

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

    private void delete(String analyzerId) {
        try {
            contentUnderstandingClient.deleteAnalyzer(analyzerId);
        } catch (RuntimeException error) {
            System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
        }
    }
}
