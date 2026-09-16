// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.UsageDetails;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample19_AnalyzeInlineAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeInlineAsync() {
        // Inline analysis returns the complete inline response with no polling;
        // a non-Succeeded outcome throws an HttpResponseException, like a failed LRO.
        AnalysisInput input = new AnalysisInput().setUrl(
            "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf");
        ContentAnalyzerInlineResponse response
            = contentUnderstandingAsyncClient.analyzeInline("prebuilt-layout", Collections.singletonList(input))
                .block();
        assertNotNull(response);

        AnalysisResult result = response.getResult();
        assertNotNull(result);
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
        assertNotNull(result.getContents().get(0));
        String markdown = result.getContents().get(0).getMarkdown();
        assertNotNull(markdown);
        assertFalse(markdown.trim().isEmpty());

        UsageDetails usage = response.getUsage();
        assertNotNull(usage);
        assertNotNull(usage.getDocumentPagesStandardInline());
        assertTrue(usage.getDocumentPagesStandardInline() > 0);
        assertNull(usage.getDocumentPagesStandard());
        assertNull(usage.getDocumentPagesMinimalInline());
        assertNull(usage.getDocumentPagesBasicInline());
    }
}
