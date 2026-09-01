// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample01_AnalyzeBinaryOptionsAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeBinaryOptionsAsync() throws Exception {
        AnalyzeBinaryOptions options
            = new AnalyzeBinaryOptions().setContentRange(ContentRange.pagesFrom(3)).setContentType("application/pdf");
        AnalysisResult result = contentUnderstandingAsyncClient
            .beginAnalyzeBinary("prebuilt-documentSearch",
                PreviewSampleTestSupport.readSample("mixed_financial_invoices.pdf"), options)
            .last()
            .flatMap(response -> response.getFinalResult())
            .block();
        assertNotNull(result);
        assertNotNull(result.getContents());
        DocumentContent document = (DocumentContent) result.getContents().get(0);
        assertNotNull(document.getPages());
        assertEquals(8, document.getPages().size());
        assertEquals(3, document.getStartPageNumber());
        assertEquals(10, document.getEndPageNumber());
        assertTrue(document.getMarkdown().length() > 0);
    }
}
