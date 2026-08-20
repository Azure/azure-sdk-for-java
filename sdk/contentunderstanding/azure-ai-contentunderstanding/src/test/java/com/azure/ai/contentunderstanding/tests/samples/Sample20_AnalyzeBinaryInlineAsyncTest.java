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
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample20_AnalyzeBinaryInlineAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeBinaryInlineAsync() throws Exception {
        BinaryData input = PreviewSampleTestSupport.readSample("mixed_financial_invoices.pdf");
        AnalyzeBinaryOptions options = new AnalyzeBinaryOptions().setContentRange(ContentRange.pages(1, 5));
        Mono<Boolean> analysis
            = contentUnderstandingAsyncClient.analyzeBinaryInline("prebuilt-layout", input, options).map(response -> {
                assertNotNull(response);
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
                return Boolean.TRUE;
            }).switchIfEmpty(Mono.error(new AssertionError("Inline binary analysis returned no response.")));

        AnalyzeBinaryOptions overLimit = new AnalyzeBinaryOptions().setContentRange(ContentRange.pagesFrom(3));
        Mono<Boolean> overLimitAnalysis
            = contentUnderstandingAsyncClient.analyzeBinaryInline("prebuilt-layout", input, overLimit)
                .flatMap(response -> Mono
                    .<Boolean>error(new AssertionError("Expected inline analysis to reject an 8-page range.")))
                .onErrorResume(HttpResponseException.class, exception -> {
                    assertNotNull(exception.getResponse());
                    assertEquals(400, exception.getResponse().getStatusCode());
                    assertTrue(
                        (exception.getMessage() + " " + exception.getValue()).contains("InputPageCountExceeded"));
                    return Mono.just(Boolean.TRUE);
                })
                .switchIfEmpty(
                    Mono.error(new AssertionError("Inline binary over-limit analysis returned no response.")));

        assertEquals(Boolean.TRUE, analysis.then(overLimitAnalysis).block());
    }
}
