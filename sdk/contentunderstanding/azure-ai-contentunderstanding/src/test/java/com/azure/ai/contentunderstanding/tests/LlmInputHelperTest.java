// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.ToLlmInputOptions;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LlmInputHelper}.
 */
public class LlmInputHelperTest {

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static AnalysisResult parseResult(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return AnalysisResult.fromJson(reader);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse test JSON", e);
        }
    }

    private static final String SINGLE_DOC_RESULT = "{" + "\"analyzerId\": \"prebuilt-invoice\","
        + "\"apiVersion\": \"2025-11-01\"," + "\"createdAt\": \"2026-01-01T00:00:00Z\","
        + "\"stringEncoding\": \"utf16\"," + "\"contents\": [{" + "  \"kind\": \"document\","
        + "  \"mimeType\": \"application/pdf\"," + "  \"startPageNumber\": 1," + "  \"endPageNumber\": 1,"
        + "  \"markdown\": \"Hello world\"," + "  \"fields\": {"
        + "    \"VendorName\": {\"type\": \"string\", \"valueString\": \"CONTOSO\"},"
        + "    \"InvoiceDate\": {\"type\": \"date\", \"valueDate\": \"2019-11-15\"},"
        + "    \"TotalAmount\": {\"type\": \"object\", \"valueObject\": {"
        + "      \"Amount\": {\"type\": \"number\", \"valueNumber\": 165},"
        + "      \"CurrencyCode\": {\"type\": \"string\", \"valueString\": \"USD\"}" + "    }}" + "  }" + "}]" + "}";

    // -----------------------------------------------------------------------
    // Null / empty
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputNullResultThrowsNpe() {
        assertThrows(NullPointerException.class, () -> LlmInputHelper.toLlmInput(null));
    }

    @Test
    public void toLlmInputEmptyContentsReturnsEmpty() {
        AnalysisResult result = parseResult("{\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[]}");
        assertEquals("", LlmInputHelper.toLlmInput(result));
    }

    // -----------------------------------------------------------------------
    // Single document
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputSingleDocumentDefaultOptions() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.startsWith("---\n"));
        assertTrue(output.contains("mimeType: application/pdf"));
        assertTrue(output.contains("fields:"));
        assertTrue(output.contains("VendorName: CONTOSO"));
        assertTrue(output.contains("InvoiceDate: '2019-11-15'"));
        assertTrue(output.contains("Amount: 165"));
        assertTrue(output.contains("CurrencyCode: USD"));
        assertTrue(output.contains("Hello world"));
        assertTrue(output.contains("<!-- InputPageNumber: 1 -->"));
    }

    @Test
    public void toLlmInputUsesDetectedMimeType() {
        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT));

        assertTrue(output.contains("mimeType: application/pdf"));
        assertFalse(output.contains("\ncontentType: document\n"));
    }

    @Test
    public void toLlmInputMissingMimeTypeUsesUnknown() {
        AnalysisResult result
            = parseResult("{\"apiVersion\":\"v\",\"contents\":[{" + "\"kind\":\"document\",\"markdown\":\"text\"}]}");

        assertTrue(LlmInputHelper.toLlmInput(result).contains("mimeType: unknown"));
    }

    @Test
    public void toLlmInputFieldsOnly() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        ToLlmInputOptions opts = new ToLlmInputOptions().setIncludeMarkdown(false);
        String output = LlmInputHelper.toLlmInput(result, null, opts);

        assertTrue(output.contains("fields:"));
        assertTrue(output.contains("VendorName: CONTOSO"));
        assertFalse(output.contains("Hello world"));
    }

    @Test
    public void toLlmInputMarkdownOnly() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        ToLlmInputOptions opts = new ToLlmInputOptions().setIncludeFields(false);
        String output = LlmInputHelper.toLlmInput(result, null, opts);

        assertFalse(output.contains("fields:"));
        assertFalse(output.contains("VendorName"));
        assertTrue(output.contains("Hello world"));
        assertTrue(output.contains("mimeType: application/pdf"));
    }

    @Test
    public void toLlmInputWithBothContentOptionsDisabledKeepsFrontMatterAndWarnings() {
        AnalysisResult result = parseResult("{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"warnings\":[{\"code\":\"PartialResult\",\"message\":\"Input was truncated\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"startPageNumber\":1,\"endPageNumber\":1,"
            + "\"markdown\":\"Hidden markdown\","
            + "\"fields\":{\"Name\":{\"type\":\"string\",\"valueString\":\"Hidden field\"}}}]}");
        ToLlmInputOptions options = new ToLlmInputOptions().setIncludeFields(false).setIncludeMarkdown(false);

        String output = LlmInputHelper.toLlmInput(result, null, options);

        assertTrue(output.contains("mimeType: unknown"));
        assertTrue(output.contains("pages: 1"));
        assertTrue(output.contains("warnings:"));
        assertFalse(output.contains("fields:"));
        assertFalse(output.contains("Hidden field"));
        assertFalse(output.contains("Hidden markdown"));
    }

    // -----------------------------------------------------------------------
    // Metadata
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputWithCustomMetadata() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("source", "invoice.pdf");
        customMetadata.put("department", "finance");
        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertTrue(output.contains("customMetadata:\n"));
        assertTrue(output.contains("source: invoice.pdf"));
        assertTrue(output.contains("department: finance"));
        int mimeTypeIdx = output.indexOf("mimeType:");
        int customMetadataIdx = output.indexOf("customMetadata:");
        int sourceIdx = output.indexOf("source:");
        assertTrue(customMetadataIdx > mimeTypeIdx);
        assertTrue(sourceIdx > customMetadataIdx);
    }

    @Test
    public void toLlmInputCustomAndServiceMetadataMatchesCrossLanguageGolden() {
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\","
            + "\"metadata\":{\"author\":\"Contoso\"}}]}");
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("source", "invoice.pdf");
        customMetadata.put("department", "finance");

        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertEquals("---\nmimeType: application/pdf\ncustomMetadata:\n  source: invoice.pdf\n"
            + "  department: finance\nmetadata:\n  author: Contoso\npages: 1\n---\n"
            + "<!-- InputPageNumber: 1 -->\n\ntext", output);
    }

    @Test
    public void toLlmInputWithStructuredCustomMetadata() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> routing = new LinkedHashMap<>();
        routing.put("team", "sdk");
        routing.put("priority", Arrays.asList("p1", "customer"));

        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("source", "invoice.pdf");
        customMetadata.put("routing", routing);

        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertTrue(output.contains("customMetadata:\n"));
        assertTrue(output.contains("  source: invoice.pdf"));
        assertTrue(output.contains("  routing:\n    team: sdk"));
        assertTrue(output.contains("    priority:\n    - p1"));
        assertTrue(output.contains("    - customer"));
        assertFalse(output.contains("{team=sdk"));
        assertFalse(output.contains("[p1, customer]"));
    }

    @Test
    public void toLlmInputPreservesEmptyAndNullCustomMetadataValues() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> nullOnly = new LinkedHashMap<>();
        nullOnly.put("value", null);
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("emptyMap", Collections.emptyMap());
        customMetadata.put("emptyList", Collections.emptyList());
        customMetadata.put("nullOnly", nullOnly);

        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertTrue(output.contains("customMetadata:"));
        assertTrue(output.contains("emptyMap: {}"));
        assertTrue(output.contains("emptyList: []"));
        assertTrue(output.contains("nullOnly:\n    value: null"));
    }

    @Test
    public void toLlmInputIndentsMetadataDelimiterLines() {
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"Document body\","
            + "\"metadata\":{\"description\":\"Q3 notes\\n---\\nreviewer: bob\"}}]}");

        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("  description: 'Q3 notes\n    ---\n    reviewer: bob'"));
        assertEquals(2, Arrays.stream(output.split("\n", -1)).filter("---"::equals).count());
    }

    @Test
    public void toLlmInputRendersCustomMetadataArraysAsSequences() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("tags", new String[] { "p1", "customer" });

        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertTrue(output.contains("customMetadata:\n"));
        assertTrue(output.contains("  tags:\n  - p1\n  - customer"));
    }

    @Test
    public void toLlmInputRendersEmptyCustomMetadataDictionary() {
        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), Collections.emptyMap());

        assertTrue(output.contains("customMetadata: {}"));
    }

    @Test
    public void toLlmInputPreservesNullSequenceItems() {
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("values", Arrays.asList("first", null, "last"));

        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), customMetadata);

        assertTrue(output.contains("values:\n  - first\n  - null\n  - last"));
    }

    @Test
    public void toLlmInputRendersBinaryDataMetadataAsStructuredYaml() {
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("details", BinaryData.fromString("{\"missing\":null,\"tags\":[]}"));

        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), customMetadata);

        assertTrue(output.contains("details:\n    missing: null\n    tags: []"));
    }

    @Test
    public void toLlmInputIndentsCustomMetadataDelimiterLines() {
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("comments", "First section\n---\nSecond section");

        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), customMetadata);

        assertTrue(output.contains("  comments: 'First section\n    ---\n    Second section'"));
        assertEquals(2, Arrays.stream(output.split("\n", -1)).filter("---"::equals).count());
    }

    @Test
    public void toLlmInputUsesMimeTypeForUnknownContentKind() {
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"futureKind\",\"mimeType\":\"application/octet-stream\",\"markdown\":\"text\"}]}");

        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("mimeType: application/octet-stream\n"));
        assertFalse(output.contains("contentType:"));
    }

    @Test
    public void toLlmInputOmitsEmptyCategory() {
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"document\",\"mimeType\":\"application/pdf\",\"category\":\"\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]}");

        String output = LlmInputHelper.toLlmInput(result);

        assertFalse(output.contains("\ncategory:"));
    }

    @Test
    public void toLlmInputQuotesYamlLikeCustomMetadataStrings() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("boolLike", "true");
        metadata.put("dateLike", "2019-11-15");
        metadata.put("numberLike", "123");
        metadata.put("specialStart", "# heading");
        metadata.put("empty", "");

        String output = LlmInputHelper.toLlmInput(result, metadata);

        assertTrue(output.contains("boolLike: 'true'"));
        assertTrue(output.contains("dateLike: '2019-11-15'"));
        assertTrue(output.contains("numberLike: '123'"));
        assertTrue(output.contains("specialStart: '# heading'"));
        assertTrue(output.contains("empty: ''"));
    }

    @Test
    public void toLlmInputIncludesAnalysisMetadata() {
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"Metadata sample\","
            + "\"metadata\":{\"author\":\"Contoso Metadata Team\",\"pageCount\":\"1\","
            + "\"contentType\":\"application/pdf\"}}]}");

        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("metadata:\n"));
        assertTrue(output.contains("  author: Contoso Metadata Team\n"));
        assertTrue(output.contains("  pageCount: '1'\n"));
        assertTrue(output.contains("  contentType: application/pdf\n"));
    }

    @Test
    public void toLlmInputKeepsAnalysisMetadataJsonStringOpaque() {
        String jsonValue = "{\"document\":{\"createdAt\":\"2026-07-16T19:00:00Z\","
            + "\"tags\":[\"finance\",\"invoice\"],\"properties\":{\"pageCount\":1}}}";
        AnalysisResult result = parseResult("{\"apiVersion\":\"2026-06-01-preview\",\"contents\":[{"
            + "\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"Metadata sample\"," + "\"metadata\":{\"xmp\":\""
            + jsonValue.replace("\"", "\\\"") + "\"}}]}");

        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("metadata:\n"));
        assertTrue(output.contains(jsonValue));
        assertFalse(output.contains("\n    document:"));
        assertFalse(output.contains("pageCount: 1"));
    }

    @Test
    public void toLlmInputAllowsHelperOwnedCustomMetadataKeys() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        for (String helperOwnedKey : Arrays.asList("mimeType", "metadata", "fields", "pages")) {
            Map<String, Object> customMetadata = new LinkedHashMap<>();
            customMetadata.put(helperOwnedKey, "caller-value");

            String output = LlmInputHelper.toLlmInput(result, customMetadata);

            assertTrue(output.contains("customMetadata:\n"));
            assertTrue(output.contains("  " + helperOwnedKey + ": caller-value\n"));
            assertTrue(output.contains("mimeType: application/pdf\n"));
            if ("pages".equals(helperOwnedKey)) {
                assertTrue(output.contains("pages: 1\n"));
                assertTrue(output.contains("  pages: caller-value\n"));
            }
        }
    }

    // -----------------------------------------------------------------------
    // Warnings
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputWithWarnings() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"warnings\":[{\"code\":\"LLMStats\",\"message\":\"latency: 2s\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("warnings:"));
        assertTrue(output.contains("code: LLMStats"));
        assertTrue(output.contains("message: 'latency: 2s'"));
    }

    @Test
    public void toLlmInputUsesWarningsKeyAndIncludesTarget() {
        String json = "{\"apiVersion\":\"v\","
            + "\"warnings\":[{\"code\":\"PartialResult\",\"message\":\"Input was truncated\","
            + "\"target\":\"inputs[0]\"}]," + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]}";

        String output = LlmInputHelper.toLlmInput(parseResult(json));

        assertTrue(output.contains("warnings:"));
        assertFalse(output.contains("rai_warnings:"));
        assertTrue(output.contains("target: inputs[0]"));
    }

    @Test
    public void llmStatsWarningFilteredFromWarnings() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"warnings\":["
            + "{\"code\":\"Telemetry\",\"message\":\"LLMStats: completion calls: 2; embedding calls: 1\"},"
            + "{\"code\":\"ContentWarning\",\"message\":\"Potentially sensitive content.\"}" + "],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("warnings:"));
        assertFalse(output.contains("LLMStats:"));
        assertTrue(output.contains("Potentially sensitive content."));
    }

    @Test
    public void llmStatsWarningOnlyOmitsWarningsBlock() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"warnings\":[{\"code\":\"Telemetry\",\"message\":\"LLMStats: completion latency: 7.71s\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertFalse(output.contains("warnings:"));
        assertFalse(output.contains("LLMStats:"));
    }

    @Test
    public void llmStatsFilterIsCaseSensitive() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"warnings\":[{\"code\":\"ContentWarning\",\"message\":\"llmstats: keep as a real warning\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("warnings:"));
        assertTrue(output.contains("llmstats: keep as a real warning"));
    }

    @Test
    public void llmStatsTextInMarkdownBodyIsPreserved() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"warnings\":[{\"code\":\"Telemetry\",\"message\":\"LLMStats: remove this warning text\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,"
            + "\"markdown\":\"A log excerpt:\\n- LLMStats: keep this body text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertFalse(output.contains("warnings:"));
        assertTrue(output.contains("LLMStats: keep this body text"));
        assertFalse(output.contains("LLMStats: remove this warning text"));
    }

    @Test
    public void llmStatsWarningFilteredWithLeadingWhitespace() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"warnings\":[{\"code\":\"Telemetry\",\"message\":\"  LLMStats: completion calls: 2\"}],"
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\"}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertFalse(output.contains("warnings:"));
        assertFalse(output.contains("LLMStats:"));
    }

    // -----------------------------------------------------------------------
    // Page markers
    // -----------------------------------------------------------------------

    @Test
    public void pageMarkersNotDuplicatedWhenServiceProvidesMarkers() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":1,\"endPageNumber\":2,"
            + "  \"markdown\":\"<!-- InputPageNumber: 1 -->\\n\\nFirst page text.\\n\\n<!-- InputPageNumber: 2 -->\\n\\nSecond page text.\","
            + "  \"pages\":[" + "    {\"pageNumber\":1,\"spans\":[{\"offset\":0,\"length\":47}]},"
            + "    {\"pageNumber\":2,\"spans\":[{\"offset\":49,\"length\":48}]}" + "  ]" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertEquals(1, countOccurrences(output, "<!-- InputPageNumber: 1 -->"));
        assertEquals(1, countOccurrences(output, "<!-- InputPageNumber: 2 -->"));
    }

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(needle, idx)) != -1) {
            count++;
            idx += needle.length();
        }
        return count;
    }

    @Test
    public void toLlmInputMultiPageWithSpans() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":2,\"endPageNumber\":4,"
            + "  \"markdown\":\"Page two content\\n\\nPage three content\\n\\nPage four content\"," + "  \"pages\":["
            + "    {\"pageNumber\":2,\"spans\":[{\"offset\":0,\"length\":16}]},"
            + "    {\"pageNumber\":3,\"spans\":[{\"offset\":18,\"length\":18}]},"
            + "    {\"pageNumber\":4,\"spans\":[{\"offset\":38,\"length\":17}]}" + "  ]" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("pages: 2-4"));
        assertTrue(output.contains("<!-- InputPageNumber: 2 -->"));
        assertTrue(output.contains("<!-- InputPageNumber: 3 -->"));
        assertTrue(output.contains("<!-- InputPageNumber: 4 -->"));
    }

    @Test
    public void toLlmInputMultiPageWithPageBreaks() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":3,\"endPageNumber\":5,"
            + "  \"markdown\":\"Page 3 text<!-- PageBreak -->Page 4 text<!-- PageBreak -->Page 5 text\"" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("<!-- InputPageNumber: 3 -->"));
        assertTrue(output.contains("<!-- InputPageNumber: 4 -->"));
        assertTrue(output.contains("<!-- InputPageNumber: 5 -->"));
        assertFalse(output.contains("<!-- PageBreak -->"));
    }

    @Test
    public void toLlmInputNonConsecutivePages() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":2,\"endPageNumber\":5,"
            + "  \"markdown\":\"P2<!-- PageBreak -->P3<!-- PageBreak -->P5\"," + "  \"pages\":["
            + "    {\"pageNumber\":2,\"spans\":[{\"offset\":0,\"length\":2}]},"
            + "    {\"pageNumber\":3,\"spans\":[{\"offset\":19,\"length\":2}]},"
            + "    {\"pageNumber\":5,\"spans\":[{\"offset\":38,\"length\":2}]}" + "  ]" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("pages: 2-3, 5"));
    }

    @Test
    public void toLlmInputComplexPages() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":11," + "\"markdown\":\"text\"," + "\"pages\":["
            + "  {\"pageNumber\":1,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":2,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":3,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":5,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":9,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":10,\"spans\":[{\"offset\":0,\"length\":1}]},"
            + "  {\"pageNumber\":11,\"spans\":[{\"offset\":0,\"length\":4}]}" + "]}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("pages: 1-3, 5, 9-11"));
    }

    // -----------------------------------------------------------------------
    // Audio/visual content
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputSingleAudioVisual() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"audioVisual\",\"mimeType\":\"video/mp4\"," + "  \"startTimeMs\":0,\"endTimeMs\":5000,"
            + "  \"markdown\":\"Transcript text\","
            + "  \"fields\":{\"Summary\":{\"type\":\"string\",\"valueString\":\"A video\"}}" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("mimeType: video/mp4"));
        assertFalse(output.contains("timeRange:"));
        assertTrue(output.contains("Summary: A video"));
        assertTrue(output.contains("Transcript text"));
    }

    @Test
    public void toLlmInputMultiSegmentAudioVisual() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":["
            + "  {\"kind\":\"audioVisual\",\"mimeType\":\"video/mp4\"," + "   \"startTimeMs\":0,\"endTimeMs\":5000,"
            + "   \"markdown\":\"Seg 1\",\"fields\":{\"Summary\":{\"type\":\"string\",\"valueString\":\"First\"}}},"
            + "  {\"kind\":\"audioVisual\",\"mimeType\":\"video/mp4\"," + "   \"startTimeMs\":5000,\"endTimeMs\":15000,"
            + "   \"markdown\":\"Seg 2\",\"fields\":{\"Summary\":{\"type\":\"string\",\"valueString\":\"Second\"}}}"
            + "]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("*****"));
        assertTrue(output.contains("timeRange: 00:00 \u2013 00:05"));
        assertTrue(output.contains("timeRange: 00:05 \u2013 00:15"));
        assertTrue(output.contains("Seg 1"));
        assertTrue(output.contains("Seg 2"));
    }

    // -----------------------------------------------------------------------
    // Classification
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputClassificationWithSegments() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":1,\"endPageNumber\":3," + "  \"markdown\":\"Invoice contentReceipt content\","
            + "  \"segments\":[" + "    {\"segmentId\":\"seg1\",\"category\":\"Invoice\","
            + "     \"span\":{\"offset\":0,\"length\":15}," + "     \"startPageNumber\":1,\"endPageNumber\":1},"
            + "    {\"segmentId\":\"seg2\",\"category\":\"Receipt\"," + "     \"span\":{\"offset\":15,\"length\":15},"
            + "     \"startPageNumber\":2,\"endPageNumber\":3}" + "  ]" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("*****"));
        assertTrue(output.contains("category: Invoice"));
        assertTrue(output.contains("category: Receipt"));
        assertTrue(output.contains("Invoice content"));
        assertTrue(output.contains("Receipt content"));
        assertEquals(2, countOccurrences(output, "mimeType: application/pdf"));
    }

    @Test
    public void toLlmInputClassificationWithRouting() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":["
            + "  {\"kind\":\"document\",\"mimeType\":\"application/pdf\"," + "   \"path\":\"input1\","
            + "   \"startPageNumber\":1,\"endPageNumber\":3," + "   \"markdown\":\"Invoice contentReceipt content\","
            + "   \"segments\":[" + "     {\"segmentId\":\"seg1\",\"category\":\"Invoice\","
            + "      \"span\":{\"offset\":0,\"length\":15}," + "      \"startPageNumber\":1,\"endPageNumber\":1},"
            + "     {\"segmentId\":\"seg2\",\"category\":\"Receipt\"," + "      \"span\":{\"offset\":15,\"length\":15},"
            + "      \"startPageNumber\":2,\"endPageNumber\":3}" + "   ]},"
            + "  {\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "   \"path\":\"input1/seg1\",\"category\":\"Invoice\"," + "   \"startPageNumber\":1,\"endPageNumber\":1,"
            + "   \"markdown\":\"Invoice content\","
            + "   \"fields\":{\"Amount\":{\"type\":\"number\",\"valueNumber\":100}}}" + "]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("Amount: 100"));
        assertTrue(output.contains("category: Receipt"));
        int invoiceIdx = output.indexOf("category: Invoice");
        int receiptIdx = output.indexOf("category: Receipt");
        assertTrue(invoiceIdx < receiptIdx, "Invoice should come before Receipt");
    }

    @Test
    public void toLlmInputClassificationDeduplicatesByPathNotCategory() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\",\"contents\":["
            + "{\"kind\":\"document\",\"path\":\"input1\",\"startPageNumber\":1,\"endPageNumber\":2,"
            + "\"markdown\":\"Inv1.\\n\\nInv2.\",\"segments\":["
            + "{\"segmentId\":\"segment1\",\"category\":\"Invoice\","
            + "\"span\":{\"offset\":0,\"length\":5},\"startPageNumber\":1,\"endPageNumber\":1},"
            + "{\"segmentId\":\"segment2\",\"category\":\"Invoice\","
            + "\"span\":{\"offset\":7,\"length\":5},\"startPageNumber\":2,\"endPageNumber\":2}]},"
            + "{\"kind\":\"document\",\"path\":\"input1/segment1\",\"category\":\"Invoice\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"Inv1.\","
            + "\"fields\":{\"Vendor\":{\"type\":\"string\",\"valueString\":\"A\"}}}]}";

        String output = LlmInputHelper.toLlmInput(parseResult(json));
        String[] blocks = output.split("\\*\\*\\*\\*\\*");

        assertEquals(2, blocks.length);
        assertTrue(blocks[0].contains("Vendor: A"));
        assertTrue(blocks[1].contains("Inv2."));
        assertFalse(blocks[1].contains("fields:"));
    }

    // -----------------------------------------------------------------------
    // YAML quoting (verified via toLlmInput output)
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputDateFieldQuoted() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        String output = LlmInputHelper.toLlmInput(result);
        assertTrue(output.contains("InvoiceDate: '2019-11-15'"), "Date values should be single-quoted in YAML");
    }

    @Test
    public void toLlmInputStringFieldNotQuoted() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        String output = LlmInputHelper.toLlmInput(result);
        assertTrue(output.contains("VendorName: CONTOSO"), "Plain string values should not be quoted");
        assertTrue(output.contains("CurrencyCode: USD"));
    }

    @Test
    public void toLlmInputNumericFieldNotQuoted() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        String output = LlmInputHelper.toLlmInput(result);
        assertTrue(output.contains("Amount: 165"), "Numeric values should not be quoted");
    }

    // -----------------------------------------------------------------------
    // Array fields
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputArrayField() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":1,\"endPageNumber\":1," + "  \"markdown\":\"Invoice\","
            + "  \"fields\":{\"LineItems\":{\"type\":\"array\",\"valueArray\":["
            + "    {\"type\":\"object\",\"valueObject\":{"
            + "      \"Description\":{\"type\":\"string\",\"valueString\":\"Consulting\"},"
            + "      \"Quantity\":{\"type\":\"number\",\"valueNumber\":2}" + "    }},"
            + "    {\"type\":\"object\",\"valueObject\":{"
            + "      \"Description\":{\"type\":\"string\",\"valueString\":\"Support\"},"
            + "      \"Quantity\":{\"type\":\"number\",\"valueNumber\":1}" + "    }}" + "  ]}}" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("LineItems:"));
        assertTrue(output.contains("- Description: Consulting"));
        assertTrue(output.contains("  Quantity: 2"));
        assertTrue(output.contains("- Description: Support"));
        assertTrue(output.contains("  Quantity: 1"));
    }

    @Test
    public void toLlmInputJsonFieldPreservesStructuredYaml() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "  \"startPageNumber\":1,\"endPageNumber\":1," + "  \"markdown\":\"Invoice\","
            + "  \"fields\":{\"Data\":{\"type\":\"json\",\"valueJson\":{"
            + "    \"key\":\"val\",\"items\":[1,2],\"active\":true,\"nested\":{\"score\":3.5}" + "  }}}" + "}]}" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("Data:"));
        assertTrue(output.contains("key: val"));
        assertTrue(output.contains("items:"));
        assertTrue(output.contains("- 1"));
        assertTrue(output.contains("- 2"));
        assertTrue(output.contains("active: true"));
        assertTrue(output.contains("nested:"));
        assertTrue(output.contains("score: 3.5"));
        assertFalse(output.contains("'{\"key\":\"val\""));
    }

    @Test
    public void toLlmInputRendersBooleanIntegerTimeAndSkipsNullFields() {
        String json = "{\"apiVersion\":\"v\",\"contents\":[{\"kind\":\"document\","
            + "\"mimeType\":\"application/pdf\",\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\","
            + "\"fields\":{" + "\"IsVerified\":{\"type\":\"boolean\",\"valueBoolean\":true},"
            + "\"Quantity\":{\"type\":\"integer\",\"valueInteger\":42},"
            + "\"MeetingTime\":{\"type\":\"time\",\"valueTime\":\"14:30:00\"},"
            + "\"Missing\":{\"type\":\"string\"}}}]}";

        String output = LlmInputHelper.toLlmInput(parseResult(json));

        assertTrue(output.contains("IsVerified: true"));
        assertTrue(output.contains("Quantity: 42"));
        assertTrue(output.contains("MeetingTime: 14:30:00"));
        assertFalse(output.contains("Missing:"));
    }

    @Test
    public void toLlmInputFormatsWholeDecimalAndNonFiniteNumbers() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("whole", 165.0);
        metadata.put("decimal", 3.14);
        metadata.put("infinity", Double.POSITIVE_INFINITY);
        metadata.put("notANumber", Double.NaN);

        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), metadata);

        assertTrue(output.contains("whole: 165"));
        assertTrue(output.contains("decimal: 3.14"));
        assertTrue(output.contains("infinity: Infinity"));
        assertTrue(output.contains("notANumber: NaN"));
    }

    @Test
    public void toLlmInputQuotesYamlSpecialCustomMetadataKeys() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("with: colon", "value1");
        metadata.put("with# hash", "value2");
        metadata.put("- dash_start", "value3");
        metadata.put("normal_key", "value4");

        String output = LlmInputHelper.toLlmInput(parseResult(SINGLE_DOC_RESULT), metadata);

        assertTrue(output.contains("'with: colon': value1"));
        assertTrue(output.contains("'with# hash': value2"));
        assertTrue(output.contains("'- dash_start': value3"));
        assertTrue(output.contains("normal_key: value4"));
    }

    @Test
    public void toLlmInputFormatsAudioVisualRangesBeyondOneHour() {
        String json = "{\"apiVersion\":\"v\",\"contents\":[" + "{\"kind\":\"audioVisual\",\"mimeType\":\"video/mp4\","
            + "\"startTimeMs\":3900000,\"endTimeMs\":7505000,\"markdown\":\"Long video\"},"
            + "{\"kind\":\"audioVisual\",\"mimeType\":\"video/mp4\","
            + "\"startTimeMs\":7505000,\"endTimeMs\":7565000,\"markdown\":\"Closing segment\"}]}";

        String output = LlmInputHelper.toLlmInput(parseResult(json));

        assertTrue(output.contains("timeRange: 65:00 \u2013 125:05"));
        assertTrue(output.contains("timeRange: 125:05 \u2013 126:05"));
    }

    @Test
    public void toLlmInputSeparatesOrdinaryDocumentsAndPreservesInputOrder() {
        String json = "{\"apiVersion\":\"v\",\"contents\":["
            + "{\"kind\":\"document\",\"mimeType\":\"application/pdf\",\"startPageNumber\":2,\"endPageNumber\":2,"
            + "\"markdown\":\"Page two appears first.\"},"
            + "{\"kind\":\"document\",\"mimeType\":\"application/pdf\",\"startPageNumber\":1,\"endPageNumber\":1,"
            + "\"markdown\":\"Page one appears second.\"}]}";

        String output = LlmInputHelper.toLlmInput(parseResult(json));

        assertTrue(output.contains("\n\n*****\n\n"));
        assertTrue(output.indexOf("Page two appears first.") < output.indexOf("Page one appears second."));
    }

    @Test
    public void toLlmInputOmitsEmptyMetadataAndOrdersFrontMatterKeys() {
        String json = "{\"apiVersion\":\"v\",\"contents\":[{\"kind\":\"document\","
            + "\"mimeType\":\"application/pdf\",\"category\":\"Invoice\","
            + "\"startPageNumber\":1,\"endPageNumber\":1,\"markdown\":\"text\",\"metadata\":{},"
            + "\"fields\":{\"X\":{\"type\":\"string\",\"valueString\":\"value\"}}}]}";
        AnalysisResult result = parseResult(json);
        Map<String, Object> customMetadata = new LinkedHashMap<>();
        customMetadata.put("source", "invoice.pdf");

        String withoutCustomMetadata = LlmInputHelper.toLlmInput(result, null);
        String output = LlmInputHelper.toLlmInput(result, customMetadata);

        assertFalse(withoutCustomMetadata.contains("customMetadata:"));
        assertFalse(output.contains("\nmetadata:"));
        int mimeTypeIndex = output.indexOf("mimeType:");
        int customMetadataIndex = output.indexOf("customMetadata:");
        int categoryIndex = output.indexOf("category:");
        int pagesIndex = output.indexOf("pages:");
        int fieldsIndex = output.indexOf("fields:");
        assertTrue(mimeTypeIndex < customMetadataIndex);
        assertTrue(customMetadataIndex < categoryIndex);
        assertTrue(categoryIndex < pagesIndex);
        assertTrue(pagesIndex < fieldsIndex);
    }

    // -----------------------------------------------------------------------
    // No fields, no markdown
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputNoFieldsNoMarkdown() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\","
            + "\"contents\":[{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1}]" + "}";
        AnalysisResult result = parseResult(json);
        String output = LlmInputHelper.toLlmInput(result);

        assertTrue(output.contains("mimeType: application/pdf"));
        assertFalse(output.contains("fields:"));
        assertTrue(output.startsWith("---\n"));
        assertTrue(output.endsWith("---"));
    }

    // -----------------------------------------------------------------------
    // Audio with metadata
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputAudioWithMetadata() {
        String json = "{" + "\"analyzerId\":\"a\",\"apiVersion\":\"v\","
            + "\"createdAt\":\"2026-01-01T00:00:00Z\",\"stringEncoding\":\"utf16\"," + "\"contents\":[{"
            + "  \"kind\":\"audioVisual\",\"mimeType\":\"audio/mpeg\"," + "  \"startTimeMs\":0,\"endTimeMs\":10000,"
            + "  \"markdown\":\"Audio transcript\","
            + "  \"fields\":{\"Summary\":{\"type\":\"string\",\"valueString\":\"A call recording\"}}" + "}]" + "}";
        AnalysisResult result = parseResult(json);
        Map<String, Object> meta = Collections.singletonMap("source", "recording.mp3");
        String output = LlmInputHelper.toLlmInput(result, meta);

        assertTrue(output.contains("mimeType: audio/mpeg"));
        assertTrue(output.contains("source: recording.mp3"));
        assertTrue(output.contains("Summary: A call recording"));
    }
}
