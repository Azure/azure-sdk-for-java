// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests.samples;

import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.models.AudioVisualContent;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerAnalyzeOperationStatus;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentPage;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.ToLlmInputOptions;
import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Async sample test for Sample_Advanced_ToLlmInputAsync.
 *
 * <p>Validates the four scenarios in the sample using the async client.
 * See {@link Sample_Advanced_ToLlmInputTest} for detailed scenario descriptions.
 */
public class Sample_Advanced_ToLlmInputAsyncTest extends ContentUnderstandingClientTestBase {

    private AnalysisResult analyzeAsync(String analyzerId, List<AnalysisInput> inputs) {
        PollerFlux<ContentAnalyzerAnalyzeOperationStatus, AnalysisResult> poller
            = contentUnderstandingAsyncClient.beginAnalyze(analyzerId, inputs);
        return poller.last().flatMap(pollResponse -> pollResponse.getFinalResult()).block();
    }

    // Section 1 — output option flags and custom metadata
    @Test
    public void testToLlmInputOutputOptionsAsync() {
        // BEGIN:ContentUnderstandingToLlmInputAsync
        String invoiceUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/invoice.pdf";

        AnalysisResult result = analyzeAsync("prebuilt-invoice", Arrays.asList(new AnalysisInput().setUrl(invoiceUrl)));
        // END:ContentUnderstandingToLlmInputAsync

        // BEGIN:Assertion_ContentUnderstandingToLlmInputAsync
        assertNotNull(result, "Analysis result should not be null");
        assertNotNull(result.getContents(), "Contents should not be null");
        assertFalse(result.getContents().isEmpty(), "Contents should not be empty");

        AnalysisContent content = result.getContents().get(0);
        assertTrue(content instanceof DocumentContent, "Invoice analysis should return DocumentContent");
        DocumentContent doc = (DocumentContent) content;
        String markdown = doc.getMarkdown() != null ? doc.getMarkdown() : "";
        assertFalse(markdown.trim().isEmpty(), "Invoice analysis should return non-empty markdown");
        System.out.println("[PASS] Invoice analyzed (" + markdown.length() + " markdown chars)");

        // Default: fields + markdown
        String defaultText = LlmInputHelper.toLlmInput(result);
        assertTrue(defaultText.startsWith("---"), "Default output should start with YAML front matter");
        assertTrue(defaultText.contains("\n---\n"), "Default output should close YAML front matter");
        assertTrue(defaultText.contains("contentType: document"),
            "Default output should declare contentType: document");
        assertTrue(defaultText.contains("fields:"), "Default output should include 'fields:' block");
        assertTrue(defaultText.contains(markdown), "Default output should include markdown body");
        System.out.println("[PASS] Default output: fields + markdown (" + defaultText.length() + " chars)");

        // Fields-only
        String fieldsOnly = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeMarkdown(false));
        assertTrue(fieldsOnly.contains("fields:"), "Fields-only output should still include 'fields:' block");
        assertFalse(fieldsOnly.contains(markdown), "Fields-only output should not contain the markdown body");
        System.out.println("[PASS] Fields-only output validated (" + fieldsOnly.length() + " chars)");

        // Markdown-only
        String markdownOnly = LlmInputHelper.toLlmInput(result, null, new ToLlmInputOptions().setIncludeFields(false));
        assertFalse(markdownOnly.contains("fields:"), "Markdown-only output should not include a 'fields:' block");
        assertTrue(markdownOnly.contains(markdown), "Markdown-only output should still include the markdown body");
        System.out.println("[PASS] Markdown-only output validated (" + markdownOnly.length() + " chars)");

        // Custom metadata
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "invoice.pdf");
        metadata.put("department", "finance");
        String withMetadata = LlmInputHelper.toLlmInput(result, metadata);
        assertTrue(withMetadata.contains("source: invoice.pdf"), "Metadata 'source' key should appear in front matter");
        assertTrue(withMetadata.contains("department: finance"),
            "Metadata 'department' key should appear in front matter");
        assertTrue(withMetadata.indexOf("contentType: document") < withMetadata.indexOf("source: invoice.pdf"),
            "Custom metadata should appear after 'contentType' in front matter");
        assertTrue(withMetadata.indexOf("source: invoice.pdf") < withMetadata.indexOf("fields:"),
            "Custom metadata should appear before the 'fields:' block in front matter");
        System.out.println("[PASS] Custom metadata injected into YAML front matter");
        // END:Assertion_ContentUnderstandingToLlmInputAsync
    }

    // Section 2 — multi-page PDF with content range
    @Test
    public void testToLlmInputMultiPageContentRangeAsync() {
        // BEGIN:ContentUnderstandingToLlmInputContentRangeAsync
        String multiPageUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/document/mixed_financial_invoices.pdf";

        AnalysisResult result = analyzeAsync("prebuilt-documentSearch",
            Arrays.asList(new AnalysisInput().setUrl(multiPageUrl).setContentRange(new ContentRange("2-3,5"))));
        // END:ContentUnderstandingToLlmInputContentRangeAsync

        // BEGIN:Assertion_ContentUnderstandingToLlmInputContentRangeAsync
        assertNotNull(result, "Analysis result should not be null");
        assertNotNull(result.getContents(), "Contents should not be null");
        assertFalse(result.getContents().isEmpty(), "Contents should not be empty");

        DocumentContent doc = (DocumentContent) result.getContents().get(0);
        List<Integer> pageNumbers
            = doc.getPages().stream().map(DocumentPage::getPageNumber).sorted().collect(Collectors.toList());
        assertEquals(Arrays.asList(2, 3, 5), pageNumbers, "contentRange '2-3,5' should return pages [2, 3, 5]");
        System.out.println("[PASS] Range analysis returned pages " + pageNumbers);

        String text = LlmInputHelper.toLlmInput(result);
        assertTrue(text.startsWith("---"), "Output should start with YAML front matter");
        assertTrue(text.contains("contentType: document"), "Output should declare contentType: document");
        assertTrue(text.contains("pages:"), "Output should include a 'pages' key in front matter");
        assertTrue(text.contains("2-3, 5") || text.contains("'2-3, 5'"),
            "'pages' value should be '2-3, 5' (original page numbers preserved)");

        // Page markers in the markdown body should use the original page numbers
        assertFalse(text.contains("<!-- page 1 -->"),
            "Page marker '<!-- page 1 -->' should not appear — we only requested pages 2-3, 5");
        assertTrue(text.contains("<!-- page 2 -->"),
            "Page marker '<!-- page 2 -->' should appear in the markdown body");
        assertTrue(text.contains("<!-- page 3 -->"),
            "Page marker '<!-- page 3 -->' should appear in the markdown body");
        assertTrue(text.contains("<!-- page 5 -->"),
            "Page marker '<!-- page 5 -->' should appear in the markdown body");
        System.out.println("[PASS] Page markers verified: <!-- page 2 -->, <!-- page 3 -->, <!-- page 5 -->");

        System.out
            .println("[PASS] toLlmInput output validated (" + text.length() + " chars, pages='2-3, 5' preserved)");
        // END:Assertion_ContentUnderstandingToLlmInputContentRangeAsync
    }

    // Section 3 — multi-segment video
    @Test
    public void testToLlmInputMultiSegmentVideoAsync() {
        // BEGIN:ContentUnderstandingToLlmInputVideoAsync
        String videoUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/videos/sdk_samples/FlightSimulator.mp4";

        AnalysisResult result
            = analyzeAsync("prebuilt-videoSearch", Arrays.asList(new AnalysisInput().setUrl(videoUrl)));
        // END:ContentUnderstandingToLlmInputVideoAsync

        // BEGIN:Assertion_ContentUnderstandingToLlmInputVideoAsync
        assertNotNull(result, "Video analysis result should not be null");
        assertNotNull(result.getContents(), "Contents should not be null");
        assertFalse(result.getContents().isEmpty(), "Contents should not be empty");
        assertTrue(result.getContents().stream().allMatch(c -> c instanceof AudioVisualContent),
            "Video analysis should return AudioVisualContent items");

        int segmentCount = result.getContents().size();
        System.out.println("[PASS] Video analyzed: " + segmentCount + " segment(s)");

        String text = LlmInputHelper.toLlmInput(result);
        assertTrue(text.startsWith("---"), "Output should start with YAML front matter");
        assertTrue(text.contains("contentType: audioVisual"), "Output should declare contentType: audioVisual");

        if (segmentCount > 1) {
            int expectedDividers = segmentCount - 1;
            assertEquals(expectedDividers, countOccurrences(text, "*****"),
                segmentCount + " segments should produce " + expectedDividers + " '*****' dividers");
            assertEquals(segmentCount, countOccurrences(text, "timeRange:"),
                "Each of " + segmentCount + " segments should declare a 'timeRange'");
            System.out.println("[PASS] Multi-segment output: " + segmentCount + " timeRange entries, "
                + expectedDividers + " '*****' dividers");
        } else {
            assertFalse(text.contains("*****"), "Single-segment output should not contain '*****' divider");
            System.out.println("[PASS] Single-segment video output validated");
        }

        System.out.println("[PASS] toLlmInput output validated (" + text.length() + " chars)");
        // END:Assertion_ContentUnderstandingToLlmInputVideoAsync
    }

    // Section 4 — audio with content range and custom metadata
    @Test
    public void testToLlmInputAudioWithContentRangeAsync() {
        // BEGIN:ContentUnderstandingToLlmInputAudioAsync
        String audioUrl
            = "https://raw.githubusercontent.com/Azure-Samples/azure-ai-content-understanding-assets/main/audio/callCenterRecording.mp3";

        AnalysisResult result = analyzeAsync("prebuilt-audioSearch",
            Arrays.asList(new AnalysisInput().setUrl(audioUrl).setContentRange(new ContentRange("0-10000"))));
        // END:ContentUnderstandingToLlmInputAudioAsync

        // BEGIN:Assertion_ContentUnderstandingToLlmInputAudioAsync
        assertNotNull(result, "Audio analysis result should not be null");
        assertNotNull(result.getContents(), "Contents should not be null");
        assertFalse(result.getContents().isEmpty(), "Contents should not be empty");
        assertTrue(result.getContents().stream().allMatch(c -> c instanceof AudioVisualContent),
            "Audio analysis should return AudioVisualContent items");
        System.out.println("[PASS] Audio analyzed: " + result.getContents().size() + " segment(s)");

        Map<String, Object> audioMetadata = new LinkedHashMap<>();
        audioMetadata.put("source", "callCenterRecording.mp3");
        String text = LlmInputHelper.toLlmInput(result, audioMetadata);
        assertTrue(text.startsWith("---"), "Output should start with YAML front matter");
        assertTrue(text.contains("contentType: audioVisual"), "Output should declare contentType: audioVisual");
        assertTrue(text.contains("source: callCenterRecording.mp3"),
            "Custom metadata 'source' key should appear in front matter");
        assertTrue(text.indexOf("contentType: audioVisual") < text.indexOf("source: callCenterRecording.mp3"),
            "Custom metadata should appear after 'contentType' in front matter");
        System.out
            .println("[PASS] toLlmInput output validated (" + text.length() + " chars, includes source metadata)");
        // END:Assertion_ContentUnderstandingToLlmInputAudioAsync
    }

    private static int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
