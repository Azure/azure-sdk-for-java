// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample_Advanced_AnalysisDiagnosticsTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalysisDiagnostics() {
        AnalysisInput input = new AnalysisInput().setUrl(
            "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-dotnet/main/ContentUnderstanding.Common/data/invoice.pdf");
        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
            = contentUnderstandingClient.beginAnalyze("prebuilt-invoice", Collections.singletonList(input));
        LongRunningOperationStatus status = poller.waitForCompletion().getStatus();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, status);

        AnalysisResult result = poller.getFinalResult();
        assertNotNull(result);
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
        assertNotNull(result.getInfos());
        assertFalse(result.getInfos().isEmpty());
        assertTrue(result.getInfos().stream().anyMatch(info -> "LLMStats".equals(info.getCode())));
        assertTrue(result.getInfos()
            .stream()
            .filter(info -> "LLMStats".equals(info.getCode()))
            .allMatch(info -> info.getMessage() != null && !info.getMessage().trim().isEmpty()));
    }
}
