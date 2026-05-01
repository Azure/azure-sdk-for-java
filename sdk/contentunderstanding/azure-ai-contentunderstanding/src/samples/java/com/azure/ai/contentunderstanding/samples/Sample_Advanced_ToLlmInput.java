// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.ToLlmInputOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sample demonstrating advanced usage of the {@link LlmInputHelper#toLlmInput} helper.
 *
 * <p>For a basic introduction to {@code toLlmInput}, see Sample01_AnalyzeBinary (document analysis),
 * Sample03_AnalyzeInvoice (field extraction), and Sample05_CreateClassifier (classification).
 *
 * <h3>About toLlmInput</h3>
 *
 * <p>When using Content Understanding with large language models, you typically need to convert the
 * structured {@link AnalysisResult} into a text format that an LLM can consume. The
 * {@code toLlmInput} helper handles this conversion automatically:
 *
 * <ul>
 *   <li><b>YAML front matter</b> with content type, extracted fields, page numbers, and optional metadata</li>
 *   <li><b>Markdown body</b> with the document content and page markers</li>
 * </ul>
 *
 * <p>The helper supports all content types (documents, images, audio, video) and handles
 * multi-segment results (e.g., video with multiple scenes) by rendering each segment with its
 * time range. For classification results, it automatically skips the parent document and renders
 * each categorized child with its category label.
 *
 * <h3>Scenarios demonstrated</h3>
 *
 * <ol>
 *   <li><b>Output options</b> — Fields-only, markdown-only, and custom metadata</li>
 *   <li><b>Multi-page PDF with content range</b> — Analyze specific pages and verify page markers</li>
 *   <li><b>Multi-segment video</b> — Analyze a video with multiple segments and time ranges</li>
 *   <li><b>Audio with content range</b> — Analyze a specific time range of an audio file</li>
 * </ol>
 *
 * <p>For classification results, see Sample05_CreateClassifier.
 */
public class Sample_Advanced_ToLlmInput {

    public static void main(String[] args) {
        // BEGIN: com.azure.ai.contentunderstanding.sampleAdvanced.buildClient
        String endpoint = System.getenv("CONTENTUNDERSTANDING_ENDPOINT");
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint);

        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        // END: com.azure.ai.contentunderstanding.sampleAdvanced.buildClient

        // ================================================================
        // 1. OUTPUT OPTIONS — Fields-only, markdown-only, metadata
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInput
        // First, analyze an invoice to get a result we can demonstrate options with.
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        System.out.println("============================================================");
        System.out.println("OUTPUT OPTIONS");
        System.out.println("============================================================");
        System.out.println("Analyzing invoice for output option demos...");
        System.out.println("  URL: " + invoiceUrl + "\n");

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> invoicePoller
            = client.beginAnalyze("prebuilt-invoice", Arrays.asList(new AnalysisInput().setUrl(invoiceUrl)));

        AnalysisResult result = invoicePoller.getFinalResult();

        // Convert to LLM-ready text (YAML front matter + markdown)
        String text = LlmInputHelper.toLlmInput(result);
        System.out.println("Default output (fields + markdown):");
        System.out.println(text);
        // END:ContentUnderstandingToLlmInput

        // BEGIN:ContentUnderstandingToLlmInputOptions
        // Fields-only mode — smaller token footprint when you only need structured data.
        // Useful for agentic workflows where the LLM only needs extracted values.
        String fieldsOnly
            = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeMarkdown(false));
        System.out.println("\n--- Fields only (includeMarkdown=false) ---");
        System.out.println(fieldsOnly);

        // Markdown-only mode — when you only need the document text.
        // Useful for summarization or when fields are not relevant.
        String markdownOnly
            = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeFields(false));
        System.out.println("\n--- Markdown only (includeFields=false) ---");
        System.out.println(markdownOnly);

        // Custom metadata — add your own key-value pairs to the YAML front matter.
        // Useful for RAG pipelines to track document source, department, batch, etc.
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "invoice.pdf");
        metadata.put("department", "finance");
        String withMetadata = LlmInputHelper.toLlmInput(result, metadata);
        System.out.println("\n--- With metadata ---");
        System.out.println(withMetadata);
        // END:ContentUnderstandingToLlmInputOptions

        // ================================================================
        // 2. MULTI-PAGE PDF WITH CONTENT RANGE
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputContentRange
        String multiPageUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/mixed_financial_invoices.pdf";

        System.out.println("\n============================================================");
        System.out.println("MULTI-PAGE PDF WITH CONTENT RANGE");
        System.out.println("============================================================");

        // Analyze specific pages using ContentRange.
        // Page markers in the output will use the original document page numbers,
        // so even though we only requested pages 2-3 and 5, the markers will say
        // <!-- page 2 -->, <!-- page 3 -->, <!-- page 5 --> (not 1, 2, 3).
        System.out.println("Analyzing pages 2-3 and 5 of a multi-page PDF...");
        System.out.println("  URL: " + multiPageUrl);
        System.out.println("  contentRange: '2-3,5'\n");

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> multiPagePoller
            = client.beginAnalyze("prebuilt-documentSearch",
                Arrays.asList(new AnalysisInput().setUrl(multiPageUrl).setContentRange(new ContentRange("2-3,5"))));

        AnalysisResult multiPageResult = multiPagePoller.getFinalResult();

        String multiPageText = LlmInputHelper.toLlmInput(multiPageResult);
        System.out.println("Output:");
        System.out.println(multiPageText);
        // END:ContentUnderstandingToLlmInputContentRange

        // ================================================================
        // 3. MULTI-SEGMENT VIDEO
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputVideo
        String videoUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/videos/sdk_samples/FlightSimulator.mp4";

        System.out.println("\n============================================================");
        System.out.println("MULTI-SEGMENT VIDEO");
        System.out.println("============================================================");

        // Analyze a video — the result may contain multiple segments.
        // toLlmInput renders each segment with its time range in the front matter
        // (e.g., timeRange: 00:00 – 00:15) and separates segments with ***** dividers.
        System.out.println("Analyzing video...");
        System.out.println("  URL: " + videoUrl + "\n");

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> videoPoller
            = client.beginAnalyze("prebuilt-videoSearch", Arrays.asList(new AnalysisInput().setUrl(videoUrl)));

        AnalysisResult videoResult = videoPoller.getFinalResult();

        String videoText = LlmInputHelper.toLlmInput(videoResult);
        System.out.println("Video produced " + videoResult.getContents().size() + " segment(s)");
        System.out.println("\nOutput:");
        System.out.println(videoText);
        // END:ContentUnderstandingToLlmInputVideo

        // ================================================================
        // 4. AUDIO WITH CONTENT RANGE
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputAudio
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/audio/callCenterRecording.mp3";

        System.out.println("\n============================================================");
        System.out.println("AUDIO WITH CONTENT RANGE");
        System.out.println("============================================================");

        // Analyze a specific time range of an audio file (first 10 seconds).
        // For audio, ContentRange uses milliseconds: "0-10000" means 0s to 10s.
        System.out.println("Analyzing first 10 seconds of audio...");
        System.out.println("  URL: " + audioUrl);
        System.out.println("  contentRange: '0-10000'\n");

        SyncPoller<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> audioPoller = client.beginAnalyze(
            "prebuilt-audioSearch",
            Arrays.asList(new AnalysisInput().setUrl(audioUrl).setContentRange(new ContentRange("0-10000"))));

        AnalysisResult audioResult = audioPoller.getFinalResult();

        // Include metadata to track the source file in RAG pipelines
        Map<String, Object> audioMetadata = new LinkedHashMap<>();
        audioMetadata.put("source", "callCenterRecording.mp3");
        String audioText
            = LlmInputHelper.toLlmInput(audioResult, audioMetadata);
        System.out.println("Output:");
        System.out.println(audioText);
        // END:ContentUnderstandingToLlmInputAudio
    }
}
