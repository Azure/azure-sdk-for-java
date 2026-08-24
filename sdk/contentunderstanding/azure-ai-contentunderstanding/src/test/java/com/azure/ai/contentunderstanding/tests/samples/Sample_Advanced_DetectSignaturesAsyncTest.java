// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentSignature;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample_Advanced_DetectSignaturesAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testDetectSignaturesAsync() throws Exception {
        AnalysisResult result = contentUnderstandingAsyncClient
            .beginAnalyzeBinary("prebuilt-layout", PreviewSampleTestSupport.readSample("sample_signature.png"))
            .last()
            .flatMap(response -> {
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                return response.getFinalResult()
                    .switchIfEmpty(Mono.error(new AssertionError("Signature analysis returned no final result.")));
            })
            .block();
        assertNotNull(result);
        assertNotNull(result.getContents());
        assertFalse(result.getContents().isEmpty());
        assertInstanceOf(DocumentContent.class, result.getContents().get(0));
        DocumentContent document = (DocumentContent) result.getContents().get(0);
        assertNotNull(document.getSignatures());
        assertTrue(document.getSignatures().size() >= 2);
        for (DocumentSignature signature : document.getSignatures()) {
            assertNotNull(signature.getId());
            assertFalse(signature.getId().trim().isEmpty());
            assertNotNull(signature.getSource());
            assertFalse(signature.getSource().trim().isEmpty());
            if (signature.getSpan() != null) {
                assertNotNull(document.getMarkdown());
                int offset = signature.getSpan().getOffset();
                int length = signature.getSpan().getLength();
                assertTrue(offset >= 0);
                assertTrue(length > 0);
                assertTrue(offset <= document.getMarkdown().length() - length);
                String markdownFragment = document.getMarkdown().substring(offset, offset + length);
                assertTrue(markdownFragment.contains("(signatures/" + signature.getId() + ")"));
            }
        }
    }
}
