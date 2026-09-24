// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerOperationStatus;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentContentSegment;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample_Advanced_ClassifyInPageSegmentsAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testClassifyInPageSegmentsAsync() throws Exception {
        String analyzerId = testResourceNamer.randomName("in_page_classifier_", 50);
        BinaryData documentData = PreviewSampleTestSupport.readSample("mixed_financial_docs_in_page.pdf");
        Mono<AsyncPollResponse<ContentAnalyzerOperationStatus, ContentAnalyzer>> createdAnalyzer
            = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(analyzerId,
                    PreviewSampleTestSupport.createInPageClassifier(getModelProfile().getCompletionModel()), true)
                .last()
                .flatMap(response -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                    return Mono.just(response);
                });

        Mono<Boolean> workflow = Mono.usingWhen(createdAnalyzer,
            createResponse -> createResponse.getFinalResult()
                .switchIfEmpty(Mono.error(new AssertionError("Analyzer creation returned no final result.")))
                .then(contentUnderstandingAsyncClient.beginAnalyzeBinary(analyzerId, documentData)
                    .last()
                    .flatMap(response -> {
                        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                        return response.getFinalResult()
                            .switchIfEmpty(Mono.error(new AssertionError("Analysis returned no final result.")));
                    }))
                .map(result -> {
                    assertNotNull(result);
                    assertNotNull(result.getContents());
                    assertFalse(result.getContents().isEmpty());
                    assertTrue(result.getContents().get(0) instanceof DocumentContent);
                    DocumentContent document = (DocumentContent) result.getContents().get(0);
                    assertEquals(1, document.getStartPageNumber());
                    assertEquals(1, document.getEndPageNumber());
                    assertNotNull(document.getSegments());
                    assertEquals(2, document.getSegments().size());
                    Set<String> categories = document.getSegments()
                        .stream()
                        .map(DocumentContentSegment::getCategory)
                        .collect(Collectors.toSet());
                    assertEquals(2, categories.size());
                    assertTrue(categories.contains("Invoice"));
                    assertTrue(categories.contains("BankStatement"));

                    String markdown = document.getMarkdown();
                    assertNotNull(markdown);
                    List<DocumentContentSegment> orderedSegments = new ArrayList<>(document.getSegments());
                    orderedSegments.sort(Comparator.comparingInt(segment -> segment.getSpan().getOffset()));
                    int expectedOffset = 0;
                    for (DocumentContentSegment segment : orderedSegments) {
                        assertEquals(1, segment.getStartPageNumber());
                        assertEquals(1, segment.getEndPageNumber());
                        assertNotNull(segment.getSource());
                        assertFalse(segment.getSource().trim().isEmpty());
                        assertNotNull(segment.getSpan());
                        assertEquals(expectedOffset, segment.getSpan().getOffset(),
                            "Segment spans should be contiguous without gaps or overlap");
                        assertTrue(segment.getSpan().getLength() > 0);
                        assertTrue(segment.getSpan().getOffset() + segment.getSpan().getLength() <= markdown.length(),
                            "Segment span should remain within markdown bounds");
                        expectedOffset += segment.getSpan().getLength();

                        String segmentText = markdown.substring(segment.getSpan().getOffset(),
                            segment.getSpan().getOffset() + segment.getSpan().getLength());
                        if ("Invoice".equals(segment.getCategory())) {
                            assertTrue(segmentText.contains("INVOICE"),
                                "Invoice segment markdown should contain 'INVOICE'");
                        } else if ("BankStatement".equals(segment.getCategory())) {
                            assertTrue(segmentText.contains("CONTOSO BANK"),
                                "BankStatement segment markdown should contain 'CONTOSO BANK'");
                        }
                    }
                    Set<String> sources = document.getSegments()
                        .stream()
                        .map(DocumentContentSegment::getSource)
                        .collect(Collectors.toSet());
                    assertEquals(2, sources.size(), "Each in-page segment should have a distinct source expression");
                    assertEquals(markdown.length(), expectedOffset,
                        "Segment spans should cover the markdown contiguously");

                    DocumentContentSegment invoiceSegment = document.getSegments()
                        .stream()
                        .filter(segment -> "Invoice".equals(segment.getCategory()))
                        .findFirst()
                        .orElse(null);
                    DocumentContentSegment bankStatementSegment = document.getSegments()
                        .stream()
                        .filter(segment -> "BankStatement".equals(segment.getCategory()))
                        .findFirst()
                        .orElse(null);
                    assertNotNull(invoiceSegment);
                    assertNotNull(bankStatementSegment);
                    assertEquals(0, invoiceSegment.getSpan().getOffset());
                    assertEquals(687, invoiceSegment.getSpan().getLength());
                    assertEquals(687, bankStatementSegment.getSpan().getOffset());
                    assertEquals(964, bankStatementSegment.getSpan().getLength());
                    return Boolean.TRUE;
                }),
            ignored -> contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId),
            (ignored, error) -> contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId),
            ignored -> contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId));

        assertEquals(Boolean.TRUE, workflow.block());
    }
}
