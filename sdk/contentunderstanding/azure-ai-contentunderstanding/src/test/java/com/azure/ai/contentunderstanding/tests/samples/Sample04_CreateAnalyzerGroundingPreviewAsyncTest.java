// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentStringField;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample04_CreateAnalyzerGroundingPreviewAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testExtractGenerateAndClassifyGroundingAsync() throws Exception {
        String analyzerId = testResourceNamer.randomName("grounding_", 50);
        try {
            contentUnderstandingAsyncClient
                .beginCreateAnalyzer(analyzerId,
                    PreviewSampleTestSupport.createGroundingAnalyzer(getModelProfile().getCompletionModel()), true)
                .last()
                .flatMap(response -> response.getFinalResult())
                .block();
            AnalysisResult result = contentUnderstandingAsyncClient
                .beginAnalyzeBinary(analyzerId, PreviewSampleTestSupport.readSample("sample_invoice.pdf"))
                .last()
                .flatMap(response -> response.getFinalResult())
                .block();
            DocumentContent content = (DocumentContent) result.getContents().get(0);

            assertGrounded(content.getFields().get("company_name"));
            assertGrounded(content.getFields().get("document_summary"));
            ContentField documentType = content.getFields().get("document_type");
            assertGrounded(documentType);
            assertTrue(documentType instanceof ContentStringField);
            assertTrue(Arrays.asList("invoice", "receipt", "contract", "report", "other")
                .contains(((ContentStringField) documentType).getValue()));
        } finally {
            contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId).onErrorResume(error -> Mono.empty()).block();
        }
    }

    private static void assertGrounded(ContentField field) {
        assertNotNull(field);
        assertNotNull(field.getConfidence());
        assertTrue(field.getConfidence() >= 0 && field.getConfidence() <= 1);
        assertNotNull(field.getSources());
        assertFalse(field.getSources().isEmpty());
    }
}
