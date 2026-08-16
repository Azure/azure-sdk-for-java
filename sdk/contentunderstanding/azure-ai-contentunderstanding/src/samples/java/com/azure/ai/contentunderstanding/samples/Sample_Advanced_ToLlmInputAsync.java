// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.ToLlmInputOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Async sample demonstrating advanced usage of the {@link LlmInputHelper#toLlmInput} helper.
 *
 * <p>This is the async counterpart of {@link Sample_Advanced_ToLlmInput}. See that class for detailed
 * documentation on each scenario.
 */
public class Sample_Advanced_ToLlmInputAsync {

    public static void main(String[] args) {
        // BEGIN: com.azure.ai.contentunderstanding.sampleAdvancedAsync.buildClient
        String endpoint = System.getenv("CONTENTUNDERSTANDING_ENDPOINT");
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");

        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder().endpoint(endpoint);

        ContentUnderstandingAsyncClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildAsyncClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        // END: com.azure.ai.contentunderstanding.sampleAdvancedAsync.buildClient

        // ================================================================
        // 1. OUTPUT OPTIONS — Fields-only, markdown-only, metadata
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputAsync
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        System.out.println("============================================================");
        System.out.println("OUTPUT OPTIONS (Async)");
        System.out.println("============================================================");
        System.out.println("Analyzing invoice for output option demos...");
        System.out.println("  URL: " + invoiceUrl + "\n");

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> invoicePoller
            = client.beginAnalyze("prebuilt-invoice", Arrays.asList(new AnalysisInput().setUrl(invoiceUrl)));

        AnalysisResult result = invoicePoller.last()
            .flatMap(pollResponse -> pollResponse.getFinalResult())
            .block();

        String text = LlmInputHelper.toLlmInput(result);
        System.out.println("Default output (fields + markdown):");
        System.out.println(text);
        // END:ContentUnderstandingToLlmInputAsync

        // BEGIN:ContentUnderstandingToLlmInputOptionsAsync
        String fieldsOnly
            = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeMarkdown(false));
        System.out.println("\n--- Fields only (includeMarkdown=false) ---");
        System.out.println(fieldsOnly);

        String markdownOnly
            = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeFields(false));
        System.out.println("\n--- Markdown only (includeFields=false) ---");
        System.out.println(markdownOnly);

        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "invoice.pdf");
        metadata.put("department", "finance");
        String withMetadata = LlmInputHelper.toLlmInput(result, metadata);
        System.out.println("\n--- With metadata ---");
        System.out.println(withMetadata);
        // END:ContentUnderstandingToLlmInputOptionsAsync

        // ================================================================
        // 2. MULTI-PAGE PDF WITH CONTENT RANGE
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputContentRangeAsync
        String multiPageUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/mixed_financial_invoices.pdf";

        System.out.println("\n============================================================");
        System.out.println("MULTI-PAGE PDF WITH CONTENT RANGE (Async)");
        System.out.println("============================================================");

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> multiPagePoller
            = client.beginAnalyze("prebuilt-documentSearch",
                Arrays.asList(new AnalysisInput().setUrl(multiPageUrl).setContentRange(new ContentRange("2-3,5"))));

        AnalysisResult multiPageResult = multiPagePoller.last()
            .flatMap(pollResponse -> pollResponse.getFinalResult())
            .block();

        String multiPageText = LlmInputHelper.toLlmInput(multiPageResult);
        System.out.println("Output:");
        System.out.println(multiPageText);
        // END:ContentUnderstandingToLlmInputContentRangeAsync

        // ================================================================
        // 3. MULTI-SEGMENT VIDEO
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputVideoAsync
        String videoUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/videos/sdk_samples/FlightSimulator.mp4";

        System.out.println("\n============================================================");
        System.out.println("MULTI-SEGMENT VIDEO (Async)");
        System.out.println("============================================================");

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> videoPoller
            = client.beginAnalyze("prebuilt-videoSearch", Arrays.asList(new AnalysisInput().setUrl(videoUrl)));

        AnalysisResult videoResult = videoPoller.last()
            .flatMap(pollResponse -> pollResponse.getFinalResult())
            .block();

        String videoText = LlmInputHelper.toLlmInput(videoResult);
        System.out.println("Video produced " + videoResult.getContents().size() + " segment(s)");
        System.out.println("\nOutput:");
        System.out.println(videoText);
        // END:ContentUnderstandingToLlmInputVideoAsync

        // ================================================================
        // 4. AUDIO WITH CONTENT RANGE
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputAudioAsync
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/audio/callCenterRecording.mp3";

        System.out.println("\n============================================================");
        System.out.println("AUDIO WITH CONTENT RANGE (Async)");
        System.out.println("============================================================");

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> audioPoller = client.beginAnalyze(
            "prebuilt-audioSearch",
            Arrays.asList(new AnalysisInput().setUrl(audioUrl).setContentRange(new ContentRange("0-10000"))));

        AnalysisResult audioResult = audioPoller.last()
            .flatMap(pollResponse -> pollResponse.getFinalResult())
            .block();

        Map<String, Object> audioMetadata = new LinkedHashMap<>();
        audioMetadata.put("source", "callCenterRecording.mp3");
        String audioText
            = LlmInputHelper.toLlmInput(audioResult, audioMetadata);
        System.out.println("Output:");
        System.out.println(audioText);
        // END:ContentUnderstandingToLlmInputAudioAsync
    }
}
