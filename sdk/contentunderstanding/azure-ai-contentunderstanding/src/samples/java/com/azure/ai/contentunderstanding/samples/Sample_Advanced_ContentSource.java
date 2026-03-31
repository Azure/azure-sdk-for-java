// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualSource;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentSource;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.ai.contentunderstanding.models.PointF;
import com.azure.ai.contentunderstanding.models.Rectangle;
import com.azure.ai.contentunderstanding.models.RectangleF;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Demonstrates how to access and use {@link ContentSource} grounding references
 * from analysis results. Content sources identify the exact location in the original
 * content where a field value was extracted from.
 *
 * <p>For document/image content, sources are {@link DocumentSource} instances
 * with page number, polygon coordinates, and a computed bounding box.</p>
 *
 * <p>For audio/video content, sources are {@link AudioVisualSource} instances
 * with a timestamp and an optional bounding box.</p>
 */
public class Sample_Advanced_ContentSource {

    public static void main(String[] args) {
        String endpoint = System.getenv("CONTENTUNDERSTANDING_ENDPOINT");
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint);

        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }

        // =====================================================================
        // Part 1: Document ContentSource from analysis
        // =====================================================================
        documentContentSourceFromAnalysis(client);

        // =====================================================================
        // Part 2: ContentSource.parseAll() round-trip and multi-segment parsing
        // =====================================================================
        contentSourceParseRoundTrip(client);
    }

    /**
     * Analyzes an invoice and iterates over field grounding sources,
     * casting each to {@link DocumentSource} to access page, polygon, and bounding box.
     */
    // BEGIN: com.azure.ai.contentunderstanding.advanced.contentsource.fromanalysis
    private static void documentContentSourceFromAnalysis(ContentUnderstandingClient client) {
        // Analyze an invoice to get fields with grounding sources.
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> operation
            = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

        AnalysisResult result = operation.getFinalResult();
        DocumentContent documentContent = (DocumentContent) result.getContents().get(0);

        // Iterate over all fields and access their grounding sources.
        for (Map.Entry<String, ContentField> entry : documentContent.getFields().entrySet()) {
            String fieldName = entry.getKey();
            ContentField field = entry.getValue();

            System.out.println("Field: " + fieldName + " = " + field.getValue());

            // Sources identify where the field value appears in the original content.
            // For documents, each source is a DocumentSource with page number and polygon.
            List<ContentSource> sources = field.getSources();
            if (sources != null) {
                for (ContentSource source : sources) {
                    if (source instanceof DocumentSource) {
                        DocumentSource docSource = (DocumentSource) source;
                        System.out.println("  Source: page " + docSource.getPageNumber());

                        // Polygon: the precise region (rotated quadrilateral) around the text.
                        List<PointF> polygon = docSource.getPolygon();
                        String coords = polygon.stream()
                            .map(p -> String.format("(%.4f,%.4f)", p.getX(), p.getY()))
                            .collect(Collectors.joining(", "));
                        System.out.println("  Polygon: [" + coords + "]");

                        // BoundingBox: axis-aligned rectangle computed from the polygon —
                        // convenient for drawing highlight overlays.
                        RectangleF bbox = docSource.getBoundingBox();
                        System.out.printf("  BoundingBox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n",
                            bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
                    }
                }
            }
        }
    }
    // END: com.azure.ai.contentunderstanding.advanced.contentsource.fromanalysis

    /**
     * Demonstrates round-tripping field sources through {@link ContentSource#toRawString(List)}
     * and {@link ContentSource#parseAll(String)}, plus multi-segment and page-only parsing.
     */
    // BEGIN: com.azure.ai.contentunderstanding.advanced.contentsource.parse
    private static void contentSourceParseRoundTrip(ContentUnderstandingClient client) {
        // Analyze an invoice to get a field with grounding sources.
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> operation
            = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

        AnalysisResult result = operation.getFinalResult();
        DocumentContent documentContent = (DocumentContent) result.getContents().get(0);

        // Find a field that has grounding sources.
        ContentField fieldWithSource = documentContent.getFields().values().stream()
            .filter(f -> f.getSources() != null)
            .findFirst()
            .orElseThrow();

        // Convert the parsed sources back to their wire-format string.
        String sourceString = ContentSource.toRawString(fieldWithSource.getSources());
        System.out.println("Source wire format: " + sourceString);

        // Parse the wire-format string back into typed ContentSource instances.
        List<ContentSource> roundTripped = ContentSource.parseAll(sourceString);
        DocumentSource roundTrippedDoc = (DocumentSource) roundTripped.get(0);
        System.out.println("Round-tripped: page " + roundTrippedDoc.getPageNumber()
            + ", polygon points: " + roundTrippedDoc.getPolygon().size());
        RectangleF bbox = roundTrippedDoc.getBoundingBox();
        System.out.printf("  BoundingBox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n",
            bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());

        // Find a field with multiple source segments (e.g., multi-line addresses).
        ContentField multiSourceField = documentContent.getFields().values().stream()
            .filter(f -> f.getSources() != null && f.getSources().size() > 1)
            .findFirst()
            .orElseThrow();
        String multiSourceString = ContentSource.toRawString(multiSourceField.getSources());
        System.out.println("Multi-segment wire format: " + multiSourceString);

        List<ContentSource> multiParsed = ContentSource.parseAll(multiSourceString);
        String pageNumbers = multiParsed.stream()
            .filter(s -> s instanceof DocumentSource)
            .map(s -> String.valueOf(((DocumentSource) s).getPageNumber()))
            .collect(Collectors.joining(", "));
        System.out.println("Multi-segment: " + multiParsed.size() + " sources on pages " + pageNumbers);

        // ContentSource.parseAll() also handles page-only format (no polygon coordinates).
        // However, DocumentSource.parse() requires all 9 parameters (page + 8 coordinates).
        // Use the full wire format from a real source for round-trip demonstrations.
        int realPageNumber = ((DocumentSource) fieldWithSource.getSources().get(0)).getPageNumber();
        System.out.println("Page number from source: " + realPageNumber);
    }
    // END: com.azure.ai.contentunderstanding.advanced.contentsource.parse

    // TODO: AudioVisualContentSource — demonstrate real AudioVisualSource grounding
    // from audio/video analysis. The CU service does not currently return AudioVisualSource
    // grounding (field.getSources()) for AI-generated audio fields. Once the service supports
    // timestamp-level source grounding for audio/video content, implement a method here that:
    //   1. Analyzes an audio/video file with a custom analyzer (estimateFieldSourceAndConfidence = true)
    //   2. Iterates over fields and casts getSources() elements to AudioVisualSource
    //   3. Shows AudioVisualSource.getTime() (Duration) and AudioVisualSource.getBoundingBox() (optional Rectangle)
    //   4. Demonstrates ContentSource.parseAll() with AV(...) format strings
    //
    // Example of AudioVisualSource parsing (SDK-side API works, just no live source data yet):
    //
    //   List<AudioVisualSource> avSources = AudioVisualSource.parse("AV(1500);AV(3200)");
    //   for (AudioVisualSource avSource : avSources) {
    //       Duration time = avSource.getTime();              // e.g., PT1.5S (1500 ms)
    //       Rectangle box = avSource.getBoundingBox();        // null for audio-only
    //       System.out.println("Timestamp: " + time.toMillis() + " ms, BoundingBox: " + (box != null ? box : "none"));
    //   }
    //
    //   // With bounding box (e.g., face detection in video):
    //   List<AudioVisualSource> avWithBox = AudioVisualSource.parse("AV(5000,100,200,50,60)");
    //   Rectangle bbox = avWithBox.get(0).getBoundingBox();   // Rectangle(x=100, y=200, w=50, h=60)
    //
    // See AudioVisualSource and ContentSource.parseAll() for the SDK-side API.
}
