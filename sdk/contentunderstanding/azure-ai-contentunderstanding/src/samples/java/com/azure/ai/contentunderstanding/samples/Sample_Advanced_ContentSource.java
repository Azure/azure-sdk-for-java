// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisContent;
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
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Demonstrates how to access and use {@link ContentSource} grounding references
 * from analysis results. Content sources identify the exact location in the original
 * content where a field value was extracted from.
 *
 * <p><b>Supported service API version:</b> {@code 2025-11-01}.</p>
 *
 * <p>For document/image content, sources are {@link DocumentSource} instances
 * with page number, polygon coordinates, and a computed bounding box.</p>
 *
 * <p>For audio/video content, sources are {@link AudioVisualSource} instances
 * with a timestamp and an optional bounding box.</p>
 *
 * <p>Document sources use {@code D(page,x1,y1,...,xN,yN)} and audio/video sources use
 * {@code AV(timeMs[,x,y,w,h])}; semicolons separate multiple regions. Document coordinates use
 * {@link DocumentContent#getUnit()}, which is commonly inches for document input.</p>
 *
 * <p>For client and model deployment setup, see {@link Sample00_UpdateDefaults}. API key authentication is intended
 * for local testing; prefer {@link DefaultAzureCredentialBuilder} for production applications.</p>
 */
public class Sample_Advanced_ContentSource {

    public static void main(String[] args) {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2025_11_01);

        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }

        // Analyze an invoice once — reuse the result for all demonstrations.
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisInput input = new AnalysisInput();
        input.setUrl(invoiceUrl);

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> operation
            = client.beginAnalyze("prebuilt-invoice", Arrays.asList(input));

        AnalysisResult result = requireSuccessfulResult(operation.waitForCompletion().getStatus(),
            operation::getFinalResult, "Invoice analysis");
        DocumentContent documentContent = getDocumentContent(result);

        // =====================================================================
        // Part 1: Document ContentSource from analysis
        // =====================================================================
        documentContentSourceFromAnalysis(documentContent);

        // =====================================================================
        // Part 2: DocumentSource.parse() and ContentSource.parseAll() round-trip
        // =====================================================================
        contentSourceParseRoundTrip(documentContent);
    }

    static <T> T requireSuccessfulResult(LongRunningOperationStatus status, Supplier<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            throw new IllegalStateException(operationName + " completed unsuccessfully with status: " + status);
        }
        T result = finalResult.get();
        if (result == null) {
            throw new IllegalStateException(operationName + " completed without a final result.");
        }
        return result;
    }

    static DocumentContent getDocumentContent(AnalysisResult result) {
        if (result.getContents() != null) {
            for (AnalysisContent content : result.getContents()) {
                if (content instanceof DocumentContent) {
                    return (DocumentContent) content;
                }
            }
        }
        throw new IllegalStateException("Invoice analysis did not return document content.");
    }

    /**
     * Analyzes an invoice and iterates over field grounding sources,
     * casting each to {@link DocumentSource} to access page, polygon, and bounding box.
     */
    // BEGIN: com.azure.ai.contentunderstanding.advanced.contentsource.fromanalysis
    static void documentContentSourceFromAnalysis(DocumentContent documentContent) {
        if (documentContent.getFields() == null || documentContent.getFields().isEmpty()) {
            throw new IllegalStateException("Invoice analysis did not return fields.");
        }

        boolean hasDocumentSource = false;
        boolean hasPolygonSource = false;
        // Iterate over all fields and access their grounding sources.
        for (Map.Entry<String, ContentField> entry : documentContent.getFields().entrySet()) {
            String fieldName = entry.getKey();
            ContentField field = entry.getValue();
            if (field == null) {
                throw new IllegalStateException("Invoice analysis returned an empty field.");
            }

            System.out.println("Field: " + fieldName + " = " + field.getValue());

            // Sources identify where the field value appears in the original content.
            // For documents, each source is a DocumentSource with page number and polygon.
            List<ContentSource> sources = field.getSources();
            if (sources != null) {
                for (ContentSource source : sources) {
                    if (!(source instanceof DocumentSource)) {
                        throw new IllegalStateException("Invoice field source was not a DocumentSource.");
                    }
                    hasDocumentSource = true;
                    DocumentSource docSource = (DocumentSource) source;
                    if (docSource.getPageNumber() < 1) {
                        throw new IllegalStateException("DocumentSource returned an invalid page number.");
                    }
                    System.out.println("  Source: page " + docSource.getPageNumber());

                    // Polygon: the precise region (rotated quadrilateral) around the text.
                    // May be null for page-only D(page) wire-format sources.
                    List<PointF> polygon = docSource.getPolygon();
                    RectangleF bbox = docSource.getBoundingBox();
                    if (polygon != null) {
                        if (polygon.size() < 3 || bbox == null || bbox.getWidth() <= 0 || bbox.getHeight() <= 0) {
                            throw new IllegalStateException("DocumentSource returned invalid polygon geometry.");
                        }
                        String coords = polygon.stream()
                            .map(p -> String.format("(%.4f,%.4f)", p.getX(), p.getY()))
                            .collect(Collectors.joining(", "));
                        System.out.println("  Polygon: [" + coords + "]");
                        System.out.printf("  BoundingBox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n", bbox.getX(), bbox.getY(),
                            bbox.getWidth(), bbox.getHeight());
                        hasPolygonSource = true;
                    } else if (bbox != null) {
                        throw new IllegalStateException("Page-only DocumentSource unexpectedly returned a bounding box.");
                    }
                }
            }
        }
        if (!hasDocumentSource || !hasPolygonSource) {
            throw new IllegalStateException("Invoice analysis did not return polygon-backed DocumentSource grounding.");
        }
    }
    // END: com.azure.ai.contentunderstanding.advanced.contentsource.fromanalysis

    /**
     * Demonstrates the two public parse methods and {@link ContentSource#toRawString(List)}:
     * <ul>
     *   <li>{@link DocumentSource#parse(String)} — typed method, returns {@code List<DocumentSource>}</li>
     *   <li>{@link ContentSource#parseAll(String)} — base-class method, returns {@code List<ContentSource>}</li>
     * </ul>
     */
    // BEGIN: com.azure.ai.contentunderstanding.advanced.contentsource.parse
    static void contentSourceParseRoundTrip(DocumentContent documentContent) {
        ContentField fieldWithSource = documentContent.getFields().values().stream()
            .filter(f -> f.getSources() != null && !f.getSources().isEmpty())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No field with sources found"));
        ContentSource originalSource = fieldWithSource.getSources().get(0);
        if (!(originalSource instanceof DocumentSource)) {
            throw new IllegalStateException("Invoice field source was not a DocumentSource.");
        }
        DocumentSource originalDocumentSource = (DocumentSource) originalSource;
        String wireFormat = ContentSource.toRawString(fieldWithSource.getSources());
        if (wireFormat == null || wireFormat.trim().isEmpty()) {
            throw new IllegalStateException("ContentSource serialization returned an empty value.");
        }
        System.out.println("Source wire format: " + wireFormat);

        // DocumentSource.parse() is the typed convenience method. It returns List<DocumentSource>
        // directly — no casting needed. Use this when you know the source string contains only D() segments.
        List<DocumentSource> docSources = DocumentSource.parse(wireFormat);
        if (docSources.isEmpty() || docSources.get(0).getPageNumber() != originalDocumentSource.getPageNumber()) {
            throw new IllegalStateException("DocumentSource.parse() did not preserve the source page.");
        }
        for (DocumentSource ds : docSources) {
            RectangleF bbox = ds.getBoundingBox();
            if (ds.getPolygon() != null) {
                if (bbox == null || bbox.getWidth() <= 0 || bbox.getHeight() <= 0) {
                    throw new IllegalStateException("Parsed DocumentSource returned invalid bounding box geometry.");
                }
                System.out.printf("  parse -> page %d, bbox: x=%.4f, y=%.4f, w=%.4f, h=%.4f%n",
                    ds.getPageNumber(), bbox.getX(), bbox.getY(), bbox.getWidth(), bbox.getHeight());
            } else if (bbox != null) {
                throw new IllegalStateException("Parsed page-only DocumentSource unexpectedly returned a bounding box.");
            }
        }

        // ContentSource.parseAll() is the base-class method that handles both D() and AV() formats.
        // It returns List<ContentSource>, so you cast each element to the appropriate subclass.
        List<ContentSource> parsed = ContentSource.parseAll(wireFormat);
        if (parsed.isEmpty() || !(parsed.get(0) instanceof DocumentSource)
            || ((DocumentSource) parsed.get(0)).getPageNumber() != originalDocumentSource.getPageNumber()) {
            throw new IllegalStateException("ContentSource.parseAll() did not preserve the document source.");
        }
        DocumentSource roundTrippedDocumentSource = (DocumentSource) parsed.get(0);
        System.out.println("  parseAll -> DocumentSource: page " + roundTrippedDocumentSource.getPageNumber()
            + ", polygon points: "
            + (roundTrippedDocumentSource.getPolygon() != null ? roundTrippedDocumentSource.getPolygon().size() : 0));

        ContentField multiSourceField = documentContent.getFields().values().stream()
            .filter(f -> f.getSources() != null && f.getSources().size() > 1)
            .findFirst()
            .orElse(null);
        String multiWireFormat;
        int expectedSegmentCount;
        if (multiSourceField != null) {
            multiWireFormat = ContentSource.toRawString(multiSourceField.getSources());
            expectedSegmentCount = multiSourceField.getSources().size();
        } else {
            multiWireFormat = originalSource.getRawValue() + ";" + originalSource.getRawValue();
            expectedSegmentCount = 2;
        }
        List<ContentSource> multiParsed = ContentSource.parseAll(multiWireFormat);
        if (multiParsed.size() != expectedSegmentCount) {
            throw new IllegalStateException("Multi-segment ContentSource round trip changed the segment count.");
        }
        System.out.println("Multi-segment wire format: " + multiWireFormat);

        List<ContentSource> pageOnlySources
            = ContentSource.parseAll("D(" + originalDocumentSource.getPageNumber() + ")");
        if (pageOnlySources.size() != 1 || !(pageOnlySources.get(0) instanceof DocumentSource)) {
            throw new IllegalStateException("Page-only ContentSource parsing did not return one DocumentSource.");
        }
        DocumentSource pageOnly = (DocumentSource) pageOnlySources.get(0);
        if (pageOnly.getPageNumber() != originalDocumentSource.getPageNumber() || pageOnly.getPolygon() != null
            || pageOnly.getBoundingBox() != null) {
            throw new IllegalStateException("Page-only ContentSource parsing returned unexpected geometry.");
        }
        System.out.println("Page-only source: page " + pageOnly.getPageNumber() + ", polygon: none");
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
