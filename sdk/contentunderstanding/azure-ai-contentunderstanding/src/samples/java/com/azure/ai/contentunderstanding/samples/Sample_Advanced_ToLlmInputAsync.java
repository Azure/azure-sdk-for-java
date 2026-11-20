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
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollerFlux;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Paths;

/**
 * Async sample demonstrating advanced usage of the {@link LlmInputHelper#toLlmInput} helper.
 *
 * <p>This is the async counterpart of {@link Sample_Advanced_ToLlmInput}. See that class for detailed
 * documentation on each scenario.</p>
 *
 * <p>The output is suitable for LLM prompts, vector databases, and agent tool output. Only the service-extracted
 * analysis-result metadata scenario requires service API version {@code 2026-06-01-preview}; the other scenarios use
 * the service version configured on the client.</p>
 *
 * <p>Service-extracted values remain under {@code metadata}, while caller-provided values remain under
 * {@code customMetadata}:</p>
 * <pre>{@code
 * ---
 * metadata:
 *   author: Megan Bowen
 *   contentType: application/pdf
 *   language: en-US
 *   pageCount: 1
 *   title: Contoso Metadata Extraction Sample
 * customMetadata:
 *   source: invoice.pdf
 *   department: finance
 * ---
 * }</pre>
 *
 * <p>Configure model deployment defaults before running the document search, video, and audio scenarios; see
 * {@link Sample00_UpdateDefaultsAsync}. API key authentication is intended for local testing; prefer
 * {@link DefaultAzureCredentialBuilder} for production applications.</p>
 */
public class Sample_Advanced_ToLlmInputAsync {

    public static void main(String[] args) {
        // BEGIN: com.azure.ai.contentunderstanding.sampleAdvancedAsync.buildClient
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
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

        Mono<AnalysisResult> invoiceAnalysis = requireSuccessfulAnalysis(invoicePoller, "Invoice analysis")
            .map(result -> {
                String text = LlmInputHelper.toLlmInput(result);
                System.out.println("Default output (fields + markdown):");
                System.out.println(text);
                return result;
            });
        // END:ContentUnderstandingToLlmInputAsync

        // BEGIN:ContentUnderstandingToLlmInputOptionsAsync
        Mono<Boolean> workflow = invoiceAnalysis.map(result -> {
            String fieldsOnly
                = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeMarkdown(false));
            System.out.println("\n--- Fields only (includeMarkdown=false) ---");
            System.out.println(fieldsOnly);

            String markdownOnly
                = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeFields(false));
            System.out.println("\n--- Markdown only (includeFields=false) ---");
            System.out.println(markdownOnly);

            Map<String, Object> customMetadata = new LinkedHashMap<>();
            customMetadata.put("source", "invoice.pdf");
            customMetadata.put("department", "finance");
            String withCustomMetadata = LlmInputHelper.toLlmInput(result, customMetadata);
            System.out.println("\n--- With customMetadata ---");
            System.out.println(withCustomMetadata);
            return Boolean.TRUE;
        });
        // END:ContentUnderstandingToLlmInputOptionsAsync

        // BEGIN:ContentUnderstandingToLlmInputMetadataFromAnalysisResultPreviewAsync
        BinaryData metadataPdf = BinaryData.fromFile(Paths.get("src/samples/resources/sample_metadata.pdf"));
        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> metadataPoller
            = client.beginAnalyzeBinary("prebuilt-layout", metadataPdf);
        workflow = workflow.then(requireSuccessfulAnalysis(metadataPoller, "Metadata analysis").map(metadataResult -> {
            String analysisMetadataText = LlmInputHelper.toLlmInput(metadataResult);
            System.out.println("\n--- Preview metadata from analysis result ---");
            System.out.println(analysisMetadataText);
            return Boolean.TRUE;
        }));
        // END:ContentUnderstandingToLlmInputMetadataFromAnalysisResultPreviewAsync

        // ================================================================
        // 2. MULTI-PAGE PDF WITH CONTENT RANGE
        // ================================================================

        // BEGIN:ContentUnderstandingToLlmInputContentRangeAsync
        String multiPageUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/mixed_financial_invoices.pdf";

        System.out.println("\n============================================================");
        System.out.println("MULTI-PAGE PDF WITH CONTENT RANGE (Async)");
        System.out.println("============================================================");

        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> multiPagePoller = client.beginAnalyze(
            "prebuilt-documentSearch",
            Arrays.asList(new AnalysisInput().setUrl(multiPageUrl)
                .setContentRange(ContentRange.combine(ContentRange.pages(2, 3), ContentRange.page(5)))));

        workflow = workflow
            .then(requireSuccessfulAnalysis(multiPagePoller, "Multi-page analysis").map(multiPageResult -> {
                String multiPageText = LlmInputHelper.toLlmInput(multiPageResult);
                System.out.println("Output:");
                System.out.println(multiPageText);
                return Boolean.TRUE;
            }));
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

        workflow = workflow.then(requireSuccessfulAnalysis(videoPoller, "Video analysis").map(videoResult -> {
            String videoText = LlmInputHelper.toLlmInput(videoResult);
            System.out.println("Video produced " + videoResult.getContents().size() + " segment(s)");
            System.out.println("\nOutput:");
            System.out.println(videoText);
            return Boolean.TRUE;
        }));
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

        workflow = workflow.then(requireSuccessfulAnalysis(audioPoller, "Audio analysis").map(audioResult -> {
            Map<String, Object> audioCustomMetadata = new LinkedHashMap<>();
            audioCustomMetadata.put("source", "callCenterRecording.mp3");
            String audioText = LlmInputHelper.toLlmInput(audioResult, audioCustomMetadata);
            System.out.println("Output:");
            System.out.println(audioText);
            return Boolean.TRUE;
        }));
        // END:ContentUnderstandingToLlmInputAudioAsync

        Boolean completed = workflow.block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("ToLlmInput workflow returned no result.");
        }
    }

    static Mono<AnalysisResult> requireSuccessfulAnalysis(
        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller, String operationName) {
        return poller.last()
            .flatMap(response -> requireSuccessfulResult(response.getStatus(), response.getFinalResult(), operationName))
            .map(result -> Sample_Advanced_ToLlmInput.requireAnalysisContents(result, operationName));
    }

    static <T> Mono<T> requireSuccessfulResult(LongRunningOperationStatus status, Mono<T> finalResult,
        String operationName) {
        if (status != LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            return Mono
                .error(new IllegalStateException(operationName + " completed unsuccessfully with status: " + status));
        }
        return finalResult
            .switchIfEmpty(Mono.error(new IllegalStateException(operationName + " completed without a final result.")));
    }
}
