// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentSource;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.ai.contentunderstanding.models.PointF;
import com.azure.ai.contentunderstanding.models.RectangleF;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Async tests for {@link com.azure.ai.contentunderstanding.samples.Sample_Advanced_ContentSourceAsync}.
 * Verifies DocumentSource grounding and ContentSource round-trip parsing using the async client.
 */
public class Sample_Advanced_ContentSourceAsyncTest extends ContentUnderstandingClientTestBase {

    @Test
    public void testDocumentContentSourceFromAnalysisAsync() {
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> operation
            = contentUnderstandingAsyncClient.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

        AnalysisResult result = operation.last().flatMap(pollResponse -> {
            if (pollResponse.getStatus().isComplete()) {
                return pollResponse.getFinalResult();
            } else {
                return Mono.error(
                    new RuntimeException("Polling completed unsuccessfully with status: " + pollResponse.getStatus()));
            }
        }).block();

        assertNotNull(result, "Analysis result should not be null");
        assertNotNull(result.getContents(), "Result should contain contents");
        assertEquals(1, result.getContents().size(), "Invoice should have exactly one content element");

        assertInstanceOf(DocumentContent.class, result.getContents().get(0), "Content should be DocumentContent");
        DocumentContent documentContent = (DocumentContent) result.getContents().get(0);
        assertNotNull(documentContent.getFields(), "Document should have fields");

        boolean hasDocumentSource = false;
        for (Map.Entry<String, ContentField> entry : documentContent.getFields().entrySet()) {
            String fieldName = entry.getKey();
            ContentField field = entry.getValue();

            System.out.println("Field: " + fieldName + " = " + field.getValue());

            List<ContentSource> sources = field.getSources();
            if (sources != null) {
                for (ContentSource source : sources) {
                    assertInstanceOf(DocumentSource.class, source,
                        "Sources for document fields should be DocumentSource, got "
                            + source.getClass().getSimpleName());
                    hasDocumentSource = true;
                    DocumentSource docSource = (DocumentSource) source;
                    System.out.println("  Source: page " + docSource.getPageNumber());
                    assertTrue(docSource.getPageNumber() >= 1,
                        "Page number should be >= 1, got " + docSource.getPageNumber());

                    List<PointF> polygon = docSource.getPolygon();
                    assertNotNull(polygon, "Polygon should not be null for document sources with coordinates");
                    assertTrue(polygon.size() >= 3, "Polygon should have at least 3 points, got " + polygon.size());
                    String coords = polygon.stream()
                        .map(p -> String.format("(%.4f,%.4f)", p.getX(), p.getY()))
                        .collect(Collectors.joining(", "));
                    System.out.println("  Polygon: [" + coords + "]");

                    RectangleF bbox = docSource.getBoundingBox();
                    assertNotNull(bbox, "BoundingBox should be computed from polygon");
                    assertTrue(bbox.getWidth() > 0, "BoundingBox width should be > 0");
                    assertTrue(bbox.getHeight() > 0, "BoundingBox height should be > 0");
                    System.out.printf("  BoundingBox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n", bbox.getX(), bbox.getY(),
                        bbox.getWidth(), bbox.getHeight());
                }
            }
        }
        assertTrue(hasDocumentSource, "At least one field should have DocumentSource grounding");
    }

    @Test
    public void testContentSourceParseRoundTripAsync() {
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> operation
            = contentUnderstandingAsyncClient.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

        AnalysisResult result = operation.last().flatMap(pollResponse -> {
            if (pollResponse.getStatus().isComplete()) {
                return pollResponse.getFinalResult();
            } else {
                return Mono.error(
                    new RuntimeException("Polling completed unsuccessfully with status: " + pollResponse.getStatus()));
            }
        }).block();

        DocumentContent documentContent = (DocumentContent) result.getContents().get(0);

        // --- DocumentSource.parse() — typed method for multi-segment ---
        ContentField multiSourceField = documentContent.getFields()
            .values()
            .stream()
            .filter(f -> f.getSources() != null && f.getSources().size() > 1)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No field with multiple sources found"));
        String multiWireFormat = ContentSource.toRawString(multiSourceField.getSources());
        System.out.println("Multi-segment wire format: " + multiWireFormat);

        List<DocumentSource> docSources = DocumentSource.parse(multiWireFormat);
        assertEquals(multiSourceField.getSources().size(), docSources.size(),
            "DocumentSource.parse() count should match original source count");
        for (DocumentSource ds : docSources) {
            assertTrue(ds.getPageNumber() >= 1, "Page number should be >= 1");
            RectangleF bbox = ds.getBoundingBox();
            assertNotNull(bbox, "BoundingBox should not be null");
            assertTrue(bbox.getWidth() > 0, "BoundingBox width should be > 0");
            assertTrue(bbox.getHeight() > 0, "BoundingBox height should be > 0");
            System.out.printf("  parse -> page %d, bbox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n", ds.getPageNumber(),
                bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
        }

        // --- ContentSource.parseAll() round-trip ---
        ContentField fieldWithSource = documentContent.getFields()
            .values()
            .stream()
            .filter(f -> f.getSources() != null)
            .findFirst()
            .orElseThrow(() -> new AssertionError("No field with sources found"));

        String wireFormat = ContentSource.toRawString(fieldWithSource.getSources());
        assertNotNull(wireFormat, "Wire format should not be null");
        assertFalse(wireFormat.isEmpty(), "Wire format should not be empty");
        System.out.println("Wire format: " + wireFormat);

        List<ContentSource> parsed = ContentSource.parseAll(wireFormat);
        assertNotNull(parsed, "Parsed sources should not be null");
        assertTrue(parsed.size() >= 1, "Parsed should have at least one source");

        for (ContentSource cs : parsed) {
            assertInstanceOf(DocumentSource.class, cs, "Parsed source should be DocumentSource");
            DocumentSource ds = (DocumentSource) cs;
            assertTrue(ds.getPageNumber() >= 1, "Page number should be >= 1");
            assertNotNull(ds.getPolygon(), "Polygon should not be null");
            assertTrue(ds.getPolygon().size() >= 3,
                "Polygon should have at least 3 points, got " + ds.getPolygon().size());
            System.out
                .println("  parseAll -> page " + ds.getPageNumber() + ", polygon points: " + ds.getPolygon().size());
        }

        // --- Page-only format: D(page) via DocumentSource.parse() ---
        List<DocumentSource> pageOnly = DocumentSource.parse("D(1)");
        assertEquals(1, pageOnly.size(), "Page-only should parse to 1 source");
        DocumentSource pageOnlyDoc = pageOnly.get(0);
        assertEquals(1, pageOnlyDoc.getPageNumber(), "Page-only page number should be 1");
        assertNull(pageOnlyDoc.getPolygon(), "Page-only polygon should be null");
        assertNull(pageOnlyDoc.getBoundingBox(), "Page-only boundingBox should be null");
        assertEquals("D(1)", pageOnlyDoc.getRawValue(), "Page-only round-trip should match");
        System.out.println("Page-only: D(1) -> page=" + pageOnlyDoc.getPageNumber() + ", polygon="
            + pageOnlyDoc.getPolygon() + ", boundingBox=" + pageOnlyDoc.getBoundingBox());
    }
}
