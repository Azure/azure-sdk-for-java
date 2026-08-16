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
        boolean hasPolygonSource = false;
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

                    // DocumentSource supports both polygon (D(page,x1,y1,...)) and page-only
                    // (D(page)) wire formats. When Polygon is present, BoundingBox must also be
                    // computed; when Polygon is null, BoundingBox is null too.
                    List<PointF> polygon = docSource.getPolygon();
                    RectangleF bbox = docSource.getBoundingBox();
                    if (polygon != null) {
                        assertTrue(polygon.size() >= 3, "Polygon should have at least 3 points, got " + polygon.size());
                        String coords = polygon.stream()
                            .map(p -> String.format("(%.4f,%.4f)", p.getX(), p.getY()))
                            .collect(Collectors.joining(", "));
                        System.out.println("  Polygon: [" + coords + "]");

                        assertNotNull(bbox, "BoundingBox should be computed when Polygon is present");
                        assertTrue(bbox.getWidth() > 0, "BoundingBox width should be > 0");
                        assertTrue(bbox.getHeight() > 0, "BoundingBox height should be > 0");
                        System.out.printf("  BoundingBox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n", bbox.getX(), bbox.getY(),
                            bbox.getWidth(), bbox.getHeight());
                        hasPolygonSource = true;
                    } else {
                        assertNull(bbox, "BoundingBox should be null when Polygon is null");
                    }
                }
            }
        }
        assertTrue(hasDocumentSource, "At least one field should have DocumentSource grounding");
        assertTrue(hasPolygonSource, "At least one DocumentSource should have polygon coordinates");
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

        assertNotNull(result, "Analysis result should not be null");
        assertNotNull(result.getContents(), "Result should contain contents");
        assertFalse(result.getContents().isEmpty(), "Result contents should not be empty");
        assertInstanceOf(DocumentContent.class, result.getContents().get(0), "Content should be DocumentContent");
        DocumentContent documentContent = (DocumentContent) result.getContents().get(0);
        assertNotNull(documentContent.getFields(), "Document should have fields");

        // --- DocumentSource.parse() — typed method for multi-segment ---
        // Prefer a real multi-source field; fall back to deterministically constructing a
        // multi-segment wire string from a single source so the test doesn't depend on the
        // service returning a multi-source field shape.
        ContentField multiSourceField = documentContent.getFields()
            .values()
            .stream()
            .filter(f -> f.getSources() != null && f.getSources().size() > 1)
            .findFirst()
            .orElse(null);

        String multiWireFormat;
        int expectedSegmentCount;
        if (multiSourceField != null) {
            multiWireFormat = ContentSource.toRawString(multiSourceField.getSources());
            expectedSegmentCount = multiSourceField.getSources().size();
        } else {
            ContentSource singleSource = documentContent.getFields()
                .values()
                .stream()
                .filter(f -> f.getSources() != null && !f.getSources().isEmpty())
                .map(f -> f.getSources().get(0))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No field with sources found"));
            multiWireFormat = singleSource.getRawValue() + ";" + singleSource.getRawValue();
            expectedSegmentCount = 2;
        }
        System.out.println("Multi-segment wire format: " + multiWireFormat);

        List<DocumentSource> docSources = DocumentSource.parse(multiWireFormat);
        assertEquals(expectedSegmentCount, docSources.size(),
            "DocumentSource.parse() should produce one DocumentSource per segment");
        for (DocumentSource ds : docSources) {
            assertTrue(ds.getPageNumber() >= 1, "Page number should be >= 1");
            RectangleF bbox = ds.getBoundingBox();
            if (ds.getPolygon() != null) {
                assertNotNull(bbox, "BoundingBox should be computed when Polygon is present");
                assertTrue(bbox.getWidth() > 0, "BoundingBox width should be > 0");
                assertTrue(bbox.getHeight() > 0, "BoundingBox height should be > 0");
                System.out.printf("  parse -> page %d, bbox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n", ds.getPageNumber(),
                    bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
            } else {
                assertNull(bbox, "BoundingBox should be null when Polygon is null");
                System.out.println("  parse -> page " + ds.getPageNumber() + " (page-only)");
            }
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
            if (ds.getPolygon() != null) {
                assertTrue(ds.getPolygon().size() >= 3,
                    "Polygon should have at least 3 points, got " + ds.getPolygon().size());
                System.out.println(
                    "  parseAll -> page " + ds.getPageNumber() + ", polygon points: " + ds.getPolygon().size());
            } else {
                System.out.println("  parseAll -> page " + ds.getPageNumber() + " (page-only)");
            }
        }
    }
}
