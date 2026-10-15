// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.ContentUnderstandingServiceVersion;
import com.azure.ai.contentunderstanding.models.AnalysisInput;
import com.azure.ai.contentunderstanding.models.AnalyzeBinaryOptions;
import com.azure.ai.contentunderstanding.models.AnalyzeOptions;
import com.azure.ai.contentunderstanding.models.ChunkingStrategyKind;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerConfig;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerInlineResponse;
import com.azure.ai.contentunderstanding.models.ContentAnalyzerWorkflow;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentRange;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentSource;
import com.azure.ai.contentunderstanding.models.OperationState;
import com.azure.ai.contentunderstanding.models.ProcessingLocation;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.ai.contentunderstanding.models.SemanticRole;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for models added in the 2026-06-01-preview service version.
 */
public class PreviewModelsTest {
    private static final String INLINE_RESPONSE_JSON = "{" + "\"status\":\"Succeeded\"," + "\"result\":{"
        + "\"analyzerId\":\"preview-analyzer\"," + "\"apiVersion\":\"2026-06-01-preview\","
        + "\"infos\":[{\"code\":\"PartialResult\",\"message\":\"Input was truncated\"}],"
        + "\"stringEncoding\":\"utf16\"," + "\"contents\":[{" + "\"kind\":\"document\","
        + "\"mimeType\":\"application/pdf\"," + "\"metadata\":{\"author\":\"Contoso\"}," + "\"startPageNumber\":1,"
        + "\"endPageNumber\":2," + "\"signatures\":[{" + "\"id\":\"signature-1\","
        + "\"source\":\"D(1,0,0,1,0,1,1,0,1)\"," + "\"span\":{\"offset\":2,\"length\":4},"
        + "\"elements\":[\"/paragraphs/0\"]," + "\"role\":\"title\"}]," + "\"segments\":[{"
        + "\"segmentId\":\"segment-1\"," + "\"category\":\"invoice\"," + "\"span\":{\"offset\":0,\"length\":10},"
        + "\"startPageNumber\":1," + "\"endPageNumber\":1," + "\"confidence\":0.98,"
        + "\"source\":\"D(1,0,0,1,0,1,1,0,1)\"}]," + "\"chunks\":[{" + "\"spans\":[{\"offset\":0,\"length\":10}],"
        + "\"source\":\"D(1,0,0,1,0,1,1,0,1)\"}]" + "}]}," + "\"usage\":{"
        + "\"documentPagesMinimalInline\":1,\"documentPagesBasicInline\":3,"
        + "\"documentPagesStandardInline\":2,\"contextualizationTokens\":1000,"
        + "\"advancedContextualizationTokens\":250}" + "}";

    @Test
    public void analyzeOptionsAreFluent() {
        Map<String, String> modelDeployments = Collections.singletonMap("gpt-5.2", "my-gpt");
        AnalyzeOptions options = new AnalyzeOptions();

        assertNull(options.getModelDeployments());
        assertNull(options.isInputTruncationAllowed());
        assertNull(options.getProcessingLocation());

        assertSame(options, options.setModelDeployments(modelDeployments));
        assertSame(options, options.setInputTruncationAllowed(true));
        assertSame(options, options.setProcessingLocation(ProcessingLocation.GEOGRAPHY));
        assertSame(modelDeployments, options.getModelDeployments());
        assertTrue(options.isInputTruncationAllowed());
        assertEquals(ProcessingLocation.GEOGRAPHY, options.getProcessingLocation());
    }

    @Test
    public void analyzeBinaryOptionsAreFluent() {
        ContentRange contentRange = ContentRange.pages(1, 3);
        AnalyzeBinaryOptions options = new AnalyzeBinaryOptions();

        assertNull(options.getContentRange());
        assertNull(options.isInputTruncationAllowed());
        assertNull(options.getContentType());
        assertNull(options.getProcessingLocation());

        assertSame(options, options.setContentRange(contentRange));
        assertSame(options, options.setInputTruncationAllowed(true));
        assertSame(options, options.setContentType("application/pdf"));
        assertSame(options, options.setProcessingLocation(ProcessingLocation.GEOGRAPHY));
        assertEquals(contentRange, options.getContentRange());
        assertTrue(options.isInputTruncationAllowed());
        assertEquals("application/pdf", options.getContentType());
        assertEquals(ProcessingLocation.GEOGRAPHY, options.getProcessingLocation());
    }

    @Test
    public void previewConfigurationAndInputRangeAreAccessible() {
        SemanticChunkingStrategy strategy = new SemanticChunkingStrategy().setMaxTokens(512);
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setWorkflow(ContentAnalyzerWorkflow.AGENTIC)
            .setAllowInputTruncation(true)
            .setAllowInPageSegments(true)
            .setChunkingStrategy(strategy);
        AnalysisInput input
            = new AnalysisInput().setUrl("https://example.test/document.pdf").setContentRange(ContentRange.pages(2, 4));

        assertEquals(ContentAnalyzerWorkflow.AGENTIC, config.getWorkflow());
        assertTrue(config.isAllowInputTruncation());
        assertTrue(config.isAllowInPageSegments());
        assertSame(strategy, config.getChunkingStrategy());
        assertEquals(ChunkingStrategyKind.SEMANTIC, strategy.getKind());
        assertEquals(512, strategy.getMaxTokens());
        assertTrue(BinaryData.fromObject(config).toString().contains("\"workflow\":\"agentic\""));
        String inputJson = BinaryData.fromObject(input).toString();
        assertTrue(inputJson.contains("\"range\":\"2-4\""));
        assertFalse(inputJson.contains("allowInputTruncation"));
    }

    @Test
    public void omittedWorkflowIsNotSerialized() {
        ContentAnalyzerConfig config = new ContentAnalyzerConfig().setReturnDetails(true);

        assertNull(config.getWorkflow());
        assertFalse(BinaryData.fromObject(config).toString().contains("\"workflow\""));
    }

    @Test
    public void previewEnumsAndServiceVersionAreExported() {
        assertEquals(ContentAnalyzerWorkflow.DEFAULT, ContentAnalyzerWorkflow.fromString("default"));
        assertEquals(ContentAnalyzerWorkflow.AGENTIC, ContentAnalyzerWorkflow.fromString("agentic"));
        assertTrue(ContentAnalyzerWorkflow.values().contains(ContentAnalyzerWorkflow.DEFAULT));
        assertEquals(ChunkingStrategyKind.SEMANTIC, ChunkingStrategyKind.fromString("semantic"));
        assertEquals(ContentUnderstandingServiceVersion.V2026_06_01_PREVIEW,
            ContentUnderstandingServiceVersion.getLatest());
        assertEquals("2026-06-01-preview", ContentUnderstandingServiceVersion.getLatest().getVersion());
    }

    @Test
    public void inlineResponseDeserializesPreviewOutputFields() {
        ContentAnalyzerInlineResponse response = parseInlineResponse(INLINE_RESPONSE_JSON);

        assertEquals(OperationState.SUCCEEDED, response.getStatus());
        assertNotNull(response.getResult());
        assertEquals("preview-analyzer", response.getResult().getAnalyzerId());
        assertEquals("2026-06-01-preview", response.getResult().getApiVersion());
        assertEquals("PartialResult", response.getResult().getInfos().get(0).getCode());
        assertNull(response.getUsage().getDocumentPagesStandard());
        assertEquals(1, response.getUsage().getDocumentPagesMinimalInline());
        assertEquals(3, response.getUsage().getDocumentPagesBasicInline());
        assertEquals(2, response.getUsage().getDocumentPagesStandardInline());
        assertEquals(1000, response.getUsage().getContextualizationTokens());
        assertEquals(250, response.getUsage().getAdvancedContextualizationTokens());

        DocumentContent content = (DocumentContent) response.getResult().getContents().get(0);
        assertEquals("Contoso", content.getMetadata().get("author"));
        assertEquals("signature-1", content.getSignatures().get(0).getId());
        assertEquals("/paragraphs/0", content.getSignatures().get(0).getElements().get(0));
        assertEquals(SemanticRole.TITLE, content.getSignatures().get(0).getRole());
        assertEquals(2, content.getSignatures().get(0).getSpan().getOffset());
        assertEquals("segment-1", content.getSegments().get(0).getSegmentId());
        assertEquals(0.98, content.getSegments().get(0).getConfidence());
        assertEquals("D(1,0,0,1,0,1,1,0,1)", content.getSegments().get(0).getSource());
        assertEquals(10, content.getChunks().get(0).getSpans().get(0).getLength());
        assertEquals("D(1,0,0,1,0,1,1,0,1)", content.getChunks().get(0).getSource());
    }

    @Test
    public void optionalInfosRemainNullWhenAbsent() {
        ContentAnalyzerInlineResponse response
            = parseInlineResponse("{\"status\":\"Succeeded\",\"result\":{\"contents\":[]}}");

        assertNull(response.getResult().getInfos());
    }

    @Test
    public void extractGenerateAndClassifyFieldsDeserializeGrounding() {
        ContentAnalyzerInlineResponse response = parseInlineResponse("{\"status\":\"Succeeded\",\"result\":{"
            + "\"contents\":[{\"kind\":\"document\",\"startPageNumber\":1,\"endPageNumber\":1," + "\"fields\":{"
            + "\"company_name\":{\"type\":\"string\",\"valueString\":\"Contoso\","
            + "\"confidence\":0.96,\"source\":\"D(1,0,0,1,0,1,1,0,1)\"},"
            + "\"document_summary\":{\"type\":\"string\",\"valueString\":\"Invoice summary\","
            + "\"confidence\":0.91,\"source\":\"D(1,0,0,1,0,1,1,0,1)\"},"
            + "\"document_type\":{\"type\":\"string\",\"valueString\":\"invoice\","
            + "\"confidence\":0.98,\"source\":\"D(1,0,0,1,0,1,1,0,1)\"}}}]}}}");

        DocumentContent content = (DocumentContent) response.getResult().getContents().get(0);
        ContentField companyName = content.getFields().get("company_name");
        ContentField summary = content.getFields().get("document_summary");
        ContentField documentType = content.getFields().get("document_type");

        assertEquals(0.96, companyName.getConfidence());
        assertEquals(0.91, summary.getConfidence());
        assertEquals(0.98, documentType.getConfidence());
        assertTrue(companyName.getSources().get(0) instanceof DocumentSource);
        assertTrue(summary.getSources().get(0) instanceof DocumentSource);
        assertTrue(documentType.getSources().get(0) instanceof DocumentSource);
    }

    private static ContentAnalyzerInlineResponse parseInlineResponse(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return ContentAnalyzerInlineResponse.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to parse inline response JSON", exception);
        }
    }
}
