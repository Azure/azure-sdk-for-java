// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingAsyncClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.UsageDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Demonstrates inline binary analysis with the asynchronous client.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Use {@code beginAnalyzeBinary} for larger files or more pages, broader analyzer coverage, operation lifecycle
 * APIs, or results retained for up to 24 hours unless deleted earlier. Use {@code analyzeBinaryInline} for smaller
 * inputs that fit the inline limits. With no polling or wait tied to a polling interval, inline analysis is faster than
 * the corresponding LRO path under these limits. Inline returns {@link ContentAnalyzerInlineResponse} in one HTTP call,
 * preserving the analysis result and usage details. It does not persist the result and throws
 * {@link HttpResponseException} when the service rejects the request or the inline status is not succeeded.</p>
 *
 * <p>Inline analysis supports {@code prebuilt-digitalParse}, {@code prebuilt-read}, {@code prebuilt-layout}, and
 * custom document analyzers without fields. Document input is limited to five pages per request. The sample uses
 * {@link ContentRange#pages(int, int)} for a successful five-page window and demonstrates the error returned when a
 * range exceeds that limit. For current limits, see
 * <a href="https://aka.ms/cu-doc-limits">Content Understanding service limits</a>. For URL input, see
 * {@link Sample19_AnalyzeInlineAsync}.</p>
 *
 * <p>Use {@link AnalyzeBinaryOptions} when you need a content range, content type, processing location, or other binary
 * request options.</p>
 *
 * <p>Inline analysis uses the {@code DocumentPages*Inline} billing meters rather than the corresponding LRO page
 * meters. See the
 * <a href="https://learn.microsoft.com/azure/ai-services/content-understanding/pricing-explainer">Content
 * Understanding pricing explainer</a> for the meter that applies to each analyzer and input.</p>
 */
public class Sample20_AnalyzeBinaryInlineAsync {
    public static void main(String[] args) throws Exception {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingAsyncClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildAsyncClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildAsyncClient();
        }
        BinaryData input
            = BinaryData.fromBytes(Files.readAllBytes(Paths.get("src/samples/resources/sample_invoice.pdf")));
        Mono<Boolean> analysis = client.analyzeBinaryInline("prebuilt-layout", input)
            .map(inlineResponse -> {
                AnalysisResult result = requireAnalysisResult(inlineResponse, "Inline binary analysis");
                System.out.println(result.getContents().get(0).getMarkdown());

                UsageDetails usage = requireStandardInlineUsage(inlineResponse);
                System.out.println("Document pages (standard inline): " + usage.getDocumentPagesStandardInline());
                System.out.println("Contextualization tokens: " + usage.getContextualizationTokens());
                return Boolean.TRUE;
            })
            .switchIfEmpty(Mono.error(new IllegalStateException("Inline binary analysis returned no response.")));
        BinaryData multiPageInput = BinaryData.fromBytes(
            Files.readAllBytes(Paths.get("src/samples/resources/mixed_financial_invoices.pdf")));
        AnalyzeBinaryOptions rangeOptions = new AnalyzeBinaryOptions().setContentRange(ContentRange.pages(1, 5));
        Mono<Boolean> rangeAnalysis
            = client.analyzeBinaryInline("prebuilt-layout", multiPageInput, rangeOptions)
                .map(inlineResponse -> {
                    AnalysisResult rangeResult
                        = requireAnalysisResult(inlineResponse, "Inline binary range analysis");
                    if (!(rangeResult.getContents().get(0) instanceof DocumentContent)) {
                        throw new IllegalStateException(
                            "Inline binary range analysis did not return document content.");
                    }

                    DocumentContent document = (DocumentContent) rangeResult.getContents().get(0);
                    if (document.getPages() == null || document.getPages().size() != 5
                        || document.getStartPageNumber() != 1 || document.getEndPageNumber() != 5) {
                        throw new IllegalStateException(
                            "Inline binary range analysis did not return the requested pages 1-5.");
                    }
                    System.out.println("Inline analysis returned " + document.getPages().size() + " pages ("
                        + document.getStartPageNumber() + "-" + document.getEndPageNumber() + ").");
                    return Boolean.TRUE;
                })
                .switchIfEmpty(
                    Mono.error(new IllegalStateException("Inline binary range analysis returned no response.")));

        AnalyzeBinaryOptions overLimit = new AnalyzeBinaryOptions().setContentRange(ContentRange.pagesFrom(3));
        Mono<Boolean> overLimitAnalysis
            = client.analyzeBinaryInline("prebuilt-layout", multiPageInput, overLimit)
                .flatMap(ignored -> Mono.<Boolean>error(
                    new IllegalStateException("Expected inline analysis to reject an 8-page range.")))
                .onErrorResume(HttpResponseException.class, exception -> {
                    String errorText = exception.getMessage() + " " + exception.getValue();
                    if (exception.getResponse() == null || exception.getResponse().getStatusCode() != 400
                        || !errorText.contains("InputPageCountExceeded")) {
                        return Mono.error(exception);
                    }
                    System.out.println("Inline analysis rejected pages 3-10 as expected: " + exception.getMessage());
                    return Mono.just(Boolean.TRUE);
                })
                .switchIfEmpty(
                    Mono.error(new IllegalStateException("Inline binary over-limit analysis returned no response.")));

        Boolean completed = analysis.then(rangeAnalysis).then(overLimitAnalysis).block();
        if (!Boolean.TRUE.equals(completed)) {
            throw new IllegalStateException("Inline binary analysis workflow returned no result.");
        }
    }

    private static AnalysisResult requireAnalysisResult(ContentAnalyzerInlineResponse inlineResponse,
        String operationName) {
        AnalysisResult result = inlineResponse.getResult();
        if (result == null || result.getContents() == null || result.getContents().isEmpty()
            || result.getContents().get(0) == null
            || result.getContents().get(0).getMarkdown() == null
            || result.getContents().get(0).getMarkdown().trim().isEmpty()) {
            throw new IllegalStateException(operationName + " did not return Markdown content.");
        }
        return result;
    }

    private static UsageDetails requireStandardInlineUsage(ContentAnalyzerInlineResponse inlineResponse) {
        UsageDetails usage = inlineResponse.getUsage();
        if (usage == null || usage.getDocumentPagesStandardInline() == null
            || usage.getDocumentPagesStandardInline() <= 0) {
            throw new IllegalStateException(
                "prebuilt-layout inline binary analysis did not return standard inline usage.");
        }
        if (usage.getDocumentPagesStandard() != null || usage.getDocumentPagesMinimalInline() != null
            || usage.getDocumentPagesBasicInline() != null) {
            throw new IllegalStateException(
                "prebuilt-layout inline binary analysis returned unexpected usage meters.");
        }
        return usage;
    }
}
