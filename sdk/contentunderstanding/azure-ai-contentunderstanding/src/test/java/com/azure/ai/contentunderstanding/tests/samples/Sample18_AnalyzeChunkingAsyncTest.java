// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzer;
import com.azure.ai.contentunderstanding.models.ContentSpan;
import com.azure.ai.contentunderstanding.models.DocumentChunk;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.ai.contentunderstanding.samples.Sample18_AnalyzeChunking;
import com.azure.core.util.polling.LongRunningOperationStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Sample18_AnalyzeChunkingAsyncTest extends ContentUnderstandingPreviewClientTestBase {
    @Test
    public void testAnalyzeChunkingAsync() throws Exception {
        String analyzerId = testResourceNamer.randomName("semantic_chunking_", 50);
        try {
            ContentAnalyzer createdAnalyzer = contentUnderstandingAsyncClient
                .beginCreateAnalyzer(analyzerId,
                    PreviewSampleTestSupport.createChunkingAnalyzer(getModelProfile().getCompletionModel()), true)
                .last()
                .flatMap(response -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                    return response.getFinalResult();
                })
                .block();
            assertNotNull(createdAnalyzer);

            ContentAnalyzer persistedAnalyzer = contentUnderstandingAsyncClient.getAnalyzer(analyzerId).block();
            assertNotNull(persistedAnalyzer);
            assertNotNull(persistedAnalyzer.getConfig());
            SemanticChunkingStrategy strategy
                = assertInstanceOf(SemanticChunkingStrategy.class, persistedAnalyzer.getConfig().getChunkingStrategy());
            assertEquals(300, strategy.getMaxTokens());

            AnalysisResult result = contentUnderstandingAsyncClient
                .beginAnalyzeBinary(analyzerId, PreviewSampleTestSupport.readSample("sample_invoice.pdf"))
                .last()
                .flatMap(response -> {
                    assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, response.getStatus());
                    return response.getFinalResult();
                })
                .block();
            assertNotNull(result);
            assertNotNull(result.getContents());
            assertFalse(result.getContents().isEmpty());
            DocumentContent document = assertInstanceOf(DocumentContent.class, result.getContents().get(0));
            assertNotNull(document.getMarkdown());
            assertNotNull(document.getChunks());
            assertTrue(document.getChunks().size() >= 2, "Chunking with maxTokens=300 should produce multiple chunks");
            List<String> chunkMarkdowns = Sample18_AnalyzeChunking.renderChunks(document);
            assertEquals(document.getChunks().size(), chunkMarkdowns.size());
            for (DocumentChunk chunk : document.getChunks()) {
                assertNotNull(chunk.getSpans());
                assertFalse(chunk.getSpans().isEmpty());
                for (ContentSpan span : chunk.getSpans()) {
                    assertTrue(span.getLength() > 0);
                    assertTrue(span.getOffset() >= 0);
                    assertTrue((long) span.getOffset() + span.getLength() <= document.getMarkdown().length());
                }
            }
            assertTrue(chunkMarkdowns.get(0).contains("INVOICE"));
            assertTrue(chunkMarkdowns.get(0).contains("CONTOSO"));
            assertTrue(String.join("\n", chunkMarkdowns).contains("Consulting Services"));
            String lastChunk = chunkMarkdowns.get(chunkMarkdowns.size() - 1);
            assertTrue(lastChunk.contains("AMOUNT DUE") || lastChunk.contains("THANK YOU"));
        } finally {
            contentUnderstandingAsyncClient.deleteAnalyzer(analyzerId).onErrorResume(error -> {
                System.out.println("Note: Failed to delete analyzer '" + analyzerId + "': " + error.getMessage());
                return Mono.empty();
            }).block();
        }
    }
}
