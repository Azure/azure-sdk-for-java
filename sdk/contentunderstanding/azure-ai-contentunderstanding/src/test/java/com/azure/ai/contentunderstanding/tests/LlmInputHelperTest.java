// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AnalysisResult;
import com.azure.ai.contentunderstanding.LlmInputHelper;
import com.azure.ai.contentunderstanding.ToLlmInputOptions;
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
        assertTrue(output.contains("contentType: document"));
        assertTrue(output.contains("fields:"));
        assertTrue(output.contains("VendorName: CONTOSO"));
        assertTrue(output.contains("InvoiceDate: '2019-11-15'"));
        assertTrue(output.contains("Amount: 165"));
        assertTrue(output.contains("CurrencyCode: USD"));
        assertTrue(output.contains("Hello world"));
        assertTrue(output.contains("<!-- page 1 -->"));
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
        assertTrue(output.contains("contentType: document"));
    }

    // -----------------------------------------------------------------------
    // Metadata
    // -----------------------------------------------------------------------

    @Test
    public void toLlmInputWithMetadata() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("source", "invoice.pdf");
        meta.put("department", "finance");
        String output = LlmInputHelper.toLlmInput(result, meta);

        assertTrue(output.contains("source: invoice.pdf"));
        assertTrue(output.contains("department: finance"));
        int contentTypeIdx = output.indexOf("contentType:");
        int sourceIdx = output.indexOf("source:");
        assertTrue(sourceIdx > contentTypeIdx);
    }

    @Test
    public void toLlmInputWithStructuredMetadata() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> routing = new LinkedHashMap<>();
        routing.put("team", "sdk");
        routing.put("priority", Arrays.asList("p1", "customer"));

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("source", "invoice.pdf");
        meta.put("routing", routing);

        String output = LlmInputHelper.toLlmInput(result, meta);

        assertTrue(output.contains("source: invoice.pdf"));
        assertTrue(output.contains("routing:\n  team: sdk"));
        assertTrue(output.contains("  priority:\n  - p1"));
        assertTrue(output.contains("  - customer"));
        assertFalse(output.contains("{team=sdk"));
        assertFalse(output.contains("[p1, customer]"));
    }

    @Test
    public void toLlmInputReservedMetadataThrows() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("fields", "bad");
        assertThrows(IllegalArgumentException.class, () -> LlmInputHelper.toLlmInput(result, meta));
    }

    @Test
    public void toLlmInputReservedContentTypeThrows() {
        AnalysisResult result = parseResult(SINGLE_DOC_RESULT);
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("contentType", "custom");
        assertThrows(IllegalArgumentException.class, () -> LlmInputHelper.toLlmInput(result, meta));
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

        assertTrue(output.contains("rai_warnings:"));
        assertTrue(output.contains("code: LLMStats"));
        assertTrue(output.contains("message: 'latency: 2s'"));
    }

    // -----------------------------------------------------------------------
    // Page markers
    // -----------------------------------------------------------------------

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
        assertTrue(output.contains("<!-- page 2 -->"));
        assertTrue(output.contains("<!-- page 3 -->"));
        assertTrue(output.contains("<!-- page 4 -->"));
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

        assertTrue(output.contains("<!-- page 3 -->"));
        assertTrue(output.contains("<!-- page 4 -->"));
        assertTrue(output.contains("<!-- page 5 -->"));
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

        assertTrue(output.contains("contentType: audioVisual"));
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

        assertTrue(output.contains("contentType: document"));
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

        assertTrue(output.contains("contentType: audioVisual"));
        assertTrue(output.contains("source: recording.mp3"));
        assertTrue(output.contains("Summary: A call recording"));
    }
}
