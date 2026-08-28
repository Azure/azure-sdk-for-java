// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.core.test.TestMode;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Sample_Advanced_ExtractDocumentMetadataAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testExtractDocumentMetadataAsync() throws Exception {
        Boolean completed
            = assertMetadata("sample_metadata.pdf", "application/pdf").then(assertMetadata("sample_metadata.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document")).block();
        assertEquals(Boolean.TRUE, completed);
    }

    private Mono<Boolean> assertMetadata(String fileName, String contentType) throws Exception {
        return contentUnderstandingAsyncClient
            .beginAnalyzeBinary("prebuilt-layout", PreviewSampleTestSupport.readSample(fileName))
            .last()
            .flatMap(response -> {
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                return response.getFinalResult()
                    .switchIfEmpty(Mono.error(new AssertionError(fileName + " analysis returned no final result.")));
            })
            .map(result -> {
                assertNotNull(result);
                assertNotNull(result.getContents());
                assertFalse(result.getContents().isEmpty());
                assertInstanceOf(DocumentContent.class, result.getContents().get(0));
                DocumentContent document = (DocumentContent) result.getContents().get(0);
                assertNotNull(document.getMetadata());
                assertFalse(document.getMetadata().isEmpty());
                assertEquals("Contoso Metadata Team", document.getMetadata().get("author"));
                assertEquals(contentType, document.getMetadata().get("contentType"));
                assertEquals("Contoso Metadata Extraction Sample", document.getMetadata().get("title"));
                assertEquals("1", document.getMetadata().get("pageCount"));
                if (fileName.endsWith(".pdf")) {
                    assertEquals("en-US", document.getMetadata().get("language"));
                } else {
                    assertEquals("207", document.getMetadata().get("characterCount"));
                    assertEquals("29", document.getMetadata().get("wordCount"));
                    assertEquals("2026-07-16T19:00:00Z", document.getMetadata().get("createdAt"));
                    assertEquals("2026-07-16T20:30:00Z", document.getMetadata().get("lastModifiedAt"));
                    String expectedLastModifiedBy = getTestMode() == TestMode.PLAYBACK ? "Sanitized" : "Megan Bowen";
                    assertEquals(expectedLastModifiedBy, document.getMetadata().get("lastModifiedBy"));
                }
                return Boolean.TRUE;
            });
    }
}
