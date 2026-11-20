// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.UsageDetails;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample20_AnalyzeBinaryInlineTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeBinaryInline() throws Exception {
        BinaryData input = PreviewSampleTestSupport.readSample("mixed_financial_invoices.pdf");
        AnalyzeBinaryOptions options = new AnalyzeBinaryOptions().setContentRange(ContentRange.pages(1, 5));
        ContentAnalyzerInlineResponse response
            = contentUnderstandingClient.analyzeBinaryInline("prebuilt-layout", input, options);

        AnalysisResult result = response.getResult();
        assertNotNull(result);
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
        assertTrue(result.getContents().get(0) instanceof DocumentContent);
        DocumentContent document = (DocumentContent) result.getContents().get(0);
        assertNotNull(document.getPages());
        assertEquals(5, document.getPages().size());
        assertEquals(1, document.getStartPageNumber());
        assertEquals(5, document.getEndPageNumber());
        assertNotNull(document.getMarkdown());
        assertFalse(document.getMarkdown().trim().isEmpty());

        UsageDetails usage = response.getUsage();
        assertNotNull(usage);
        assertNotNull(usage.getDocumentPagesStandardInline());
        assertTrue(usage.getDocumentPagesStandardInline() > 0);
        assertNull(usage.getDocumentPagesStandard());
        assertNull(usage.getDocumentPagesMinimalInline());
        assertNull(usage.getDocumentPagesBasicInline());

        AnalyzeBinaryOptions overLimit = new AnalyzeBinaryOptions().setContentRange(ContentRange.pagesFrom(3));
        HttpResponseException exception = assertThrows(HttpResponseException.class,
            () -> contentUnderstandingClient.analyzeBinaryInline("prebuilt-layout", input, overLimit));
        assertEquals(400, exception.getResponse().getStatusCode());
        assertTrue((exception.getMessage() + " " + exception.getValue()).contains("InputPageCountExceeded"));
    }
}
