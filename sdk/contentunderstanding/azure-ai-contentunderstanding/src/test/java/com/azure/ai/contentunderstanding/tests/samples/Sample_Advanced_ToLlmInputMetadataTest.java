// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample_Advanced_ToLlmInputMetadataTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testToLlmInputMetadataFromAnalysisResult() throws Exception {
        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller = contentUnderstandingClient
            .beginAnalyzeBinary("prebuilt-layout", PreviewSampleTestSupport.readSample("sample_metadata.pdf"));
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, poller.waitForCompletion().getStatus());

        AnalysisResult result = poller.getFinalResult();
        assertNotNull(result);
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
        String output = LlmInputHelper.toLlmInput(result);
        assertTrue(output.contains("mimeType: application/pdf"));
        assertTrue(output.contains("metadata:"));
        assertTrue(output.contains("author: Contoso Metadata Team"));
        assertTrue(output.contains("contentType: application/pdf"));
        assertTrue(output.contains("language: en-US"));
        assertTrue(output.contains("pageCount: '1'"));
        assertTrue(output.contains("title: Contoso Metadata Extraction Sample"));
    }
}
