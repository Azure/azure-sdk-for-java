// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.samples;

import com.azure.ai.contentunderstanding.ContentUnderstandingClient;
import com.azure.ai.contentunderstanding.ContentUnderstandingClientBuilder;
import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.UsageDetails;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;

/**
 * Demonstrates inline URL analysis without long-running operation polling.
 *
 * <p><b>Supported service API version:</b> {@code 2026-06-01-preview}.</p>
 *
 * <p>Content Understanding provides two analysis patterns:</p>
 * <ul>
 *   <li><b>Long-running operation (LRO):</b> {@code beginAnalyze} starts an operation and polls until the result is
 *       ready. Use it for larger files or more pages, broader analyzer coverage, operation lifecycle APIs, or results
 *       retained for up to 24 hours unless deleted earlier.</li>
 *   <li><b>Inline:</b> {@code analyzeInline} returns {@link ContentAnalyzerInlineResponse} in one HTTP call with no
 *       polling, preserving the analysis result and usage details. Use it for smaller inputs within the inline limits.
 *       With no polling or wait tied to a polling interval, inline analysis is faster than the corresponding LRO path
 *       under these limits. Inline results are not persisted, and a non-succeeded inline status throws
 *       {@code HttpResponseException}, like a failed completed LRO.</li>
 * </ul>
 *
 * <p>Inline analysis supports {@code prebuilt-digitalParse}, {@code prebuilt-read}, {@code prebuilt-layout}, and
 * custom document analyzers without fields. For current limits, see
 * <a href="https://aka.ms/cu-doc-limits">Content Understanding service limits</a>. For binary input and page-range
 * handling, see {@link Sample20_AnalyzeBinaryInline}.</p>
 *
 * <p>Inline analysis uses the {@code DocumentPages*Inline} billing meters rather than the corresponding LRO page
 * meters. See the
 * <a href="https://learn.microsoft.com/azure/ai-services/content-understanding/pricing-explainer">Content
 * Understanding pricing explainer</a> for the meter that applies to each analyzer and input.</p>
 */
public class Sample19_AnalyzeInline {
    public static void main(String[] args) {
        String endpoint = SampleEnvironmentConfiguration.requireEnvironmentValue("CONTENTUNDERSTANDING_ENDPOINT",
            System.getenv("CONTENTUNDERSTANDING_ENDPOINT"));
        String key = System.getenv("CONTENTUNDERSTANDING_KEY");
        ContentUnderstandingClientBuilder builder = new ContentUnderstandingClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW);
        ContentUnderstandingClient client;
        if (key != null && !key.trim().isEmpty()) {
            client = builder.credential(new AzureKeyCredential(key)).buildClient();
        } else {
            client = builder.credential(new DefaultAzureCredentialBuilder().build()).buildClient();
        }
        AnalysisInput input = new AnalysisInput().setUrl(
            "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf");

        ContentAnalyzerInlineResponse inlineResponse
            = client.analyzeInline("prebuilt-layout", Collections.singletonList(input));

        AnalysisResult result = inlineResponse.getResult();
        if (result == null || result.getContents() == null || result.getContents().isEmpty()
            || result.getContents().get(0) == null
            || result.getContents().get(0).getMarkdown() == null
            || result.getContents().get(0).getMarkdown().trim().isEmpty()) {
            throw new IllegalStateException("Inline analysis did not return Markdown content.");
        }
        System.out.println(result.getContents().get(0).getMarkdown());

        UsageDetails usage = inlineResponse.getUsage();
        if (usage == null || usage.getDocumentPagesStandardInline() == null
            || usage.getDocumentPagesStandardInline() <= 0) {
            throw new IllegalStateException("prebuilt-layout inline analysis did not return standard inline usage.");
        }
        if (usage.getDocumentPagesStandard() != null || usage.getDocumentPagesMinimalInline() != null
            || usage.getDocumentPagesBasicInline() != null) {
            throw new IllegalStateException("prebuilt-layout inline analysis returned unexpected usage meters.");
        }
        System.out.println("Document pages (standard inline): " + usage.getDocumentPagesStandardInline());
        System.out.println("Contextualization tokens: " + usage.getContextualizationTokens());
    }
}
