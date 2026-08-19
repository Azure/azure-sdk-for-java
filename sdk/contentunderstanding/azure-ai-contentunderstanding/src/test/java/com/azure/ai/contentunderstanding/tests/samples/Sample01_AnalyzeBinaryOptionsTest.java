// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample01_AnalyzeBinaryOptionsTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeBinaryOptions() throws Exception {
        AnalyzeBinaryOptions options
            = new AnalyzeBinaryOptions().setContentRange(ContentRange.pagesFrom(3)).setContentType("application/pdf");
        AnalysisResult result
            = contentUnderstandingClient
                .beginAnalyzeBinary("prebuilt-documentSearch",
                    PreviewSampleTestSupport.readSample("mixed_financial_invoices.pdf"), options)
                .getFinalResult();
        DocumentContent document = (DocumentContent) result.getContents().get(0);
        assertTrue(document.getStartPageNumber() >= 3);
        assertTrue(document.getEndPageNumber() >= document.getStartPageNumber());
    }
}
