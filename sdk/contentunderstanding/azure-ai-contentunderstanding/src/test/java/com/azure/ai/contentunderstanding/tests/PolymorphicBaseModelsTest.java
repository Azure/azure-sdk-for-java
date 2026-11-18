// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.AnalysisContent;
import com.azure.ai.contentunderstanding.models.AnalysisContentKind;
import com.azure.ai.contentunderstanding.models.ChunkingStrategy;
import com.azure.ai.contentunderstanding.models.ChunkingStrategyKind;
import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentFieldType;
import com.azure.ai.contentunderstanding.models.ContentStringField;
import com.azure.ai.contentunderstanding.models.DocumentChartFigure;
import com.azure.ai.contentunderstanding.models.DocumentContent;
import com.azure.ai.contentunderstanding.models.DocumentFigure;
import com.azure.ai.contentunderstanding.models.DocumentFigureKind;
import com.azure.ai.contentunderstanding.models.SemanticChunkingStrategy;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PolymorphicBaseModelsTest {
    @Test
    public void knownDiscriminatorsStillDeserializeKnownSubtypes() {
        assertTrue(parseAnalysisContent("{\"kind\":\"document\",\"mimeType\":\"application/pdf\","
            + "\"startPageNumber\":1,\"endPageNumber\":1}") instanceof DocumentContent);
        assertTrue(
            parseChunkingStrategy("{\"kind\":\"semantic\",\"maxTokens\":300}") instanceof SemanticChunkingStrategy);
        assertTrue(parseContentField("{\"type\":\"string\",\"valueString\":\"value\"}") instanceof ContentStringField);
        assertTrue(parseDocumentFigure(
            "{\"kind\":\"chart\",\"id\":\"chart-1\",\"content\":{\"type\":\"bar\"}}") instanceof DocumentChartFigure);
    }

    @Test
    public void unrecognizedAnalysisContentPreservesBaseProperties() {
        AnalysisContent content = parseAnalysisContent(
            "{\"kind\":\"futureContent\"," + "\"mimeType\":\"application/x-future\",\"analyzerId\":\"analyzer\","
                + "\"category\":\"category\",\"path\":\"input/1\",\"markdown\":\"content\","
                + "\"metadata\":{\"author\":\"Contoso\"}}");

        assertEquals(AnalysisContentKind.fromString("futureContent"), content.getKind());
        assertEquals("application/x-future", content.getMimeType());
        assertEquals("analyzer", content.getAnalyzerId());
        assertEquals("category", content.getCategory());
        assertEquals("input/1", content.getPath());
        assertEquals("content", content.getMarkdown());
        assertEquals("Contoso", content.getMetadata().get("author"));
    }

    @Test
    public void unrecognizedContentFieldPreservesBaseProperties() {
        ContentField field = parseContentField("{\"type\":\"futureField\","
            + "\"spans\":[{\"offset\":2,\"length\":4}],\"confidence\":0.75," + "\"source\":\"D(1)\"}");

        assertEquals(ContentFieldType.fromString("futureField"), field.getType());
        assertEquals(1, field.getSpans().size());
        assertEquals(2, field.getSpans().get(0).getOffset());
        assertEquals(4, field.getSpans().get(0).getLength());
        assertEquals(0.75, field.getConfidence());
        assertEquals("D(1)", field.getSources().get(0).getRawValue());
    }

    @Test
    public void unrecognizedChunkingStrategyPreservesKind() {
        ChunkingStrategy strategy = parseChunkingStrategy("{\"kind\":\"futureChunking\"}");

        assertEquals(ChunkingStrategyKind.fromString("futureChunking"), strategy.getKind());
    }

    @Test
    public void unrecognizedDocumentFigurePreservesBaseProperties() {
        DocumentFigure figure = parseDocumentFigure("{\"kind\":\"futureFigure\",\"id\":\"figure-1\","
            + "\"source\":\"D(1)\",\"span\":{\"offset\":5,\"length\":8},"
            + "\"elements\":[\"/paragraphs/0\"],\"description\":\"Future figure\"," + "\"role\":\"supplementary\"}");

        assertEquals(DocumentFigureKind.fromString("futureFigure"), figure.getKind());
        assertEquals("figure-1", figure.getId());
        assertEquals("D(1)", figure.getSource());
        assertEquals(5, figure.getSpan().getOffset());
        assertEquals(8, figure.getSpan().getLength());
        assertEquals(1, figure.getElements().size());
        assertEquals("Future figure", figure.getDescription());
        assertEquals("supplementary", figure.getRole().toString());
    }

    @Test
    public void unrecognizedAnalysisContentRoundTripsBaseProperties() {
        AnalysisContent content = parseAnalysisContent(
            "{\"kind\":\"futureContent\",\"mimeType\":\"application/x-future\",\"analyzerId\":\"analyzer\","
                + "\"category\":\"category\",\"path\":\"input/1\",\"markdown\":\"content\","
                + "\"metadata\":{\"author\":\"Contoso\"}}");
        AnalysisContent roundTripped = parseAnalysisContent(serialize(content));

        assertEquals(AnalysisContentKind.fromString("futureContent"), roundTripped.getKind());
        assertEquals("application/x-future", roundTripped.getMimeType());
        assertEquals("analyzer", roundTripped.getAnalyzerId());
        assertEquals("category", roundTripped.getCategory());
        assertEquals("input/1", roundTripped.getPath());
        assertEquals("content", roundTripped.getMarkdown());
        assertEquals("Contoso", roundTripped.getMetadata().get("author"));
    }

    @Test
    public void unrecognizedContentFieldRoundTripsBaseProperties() {
        ContentField field = parseContentField("{\"type\":\"futureField\","
            + "\"spans\":[{\"offset\":2,\"length\":4}],\"confidence\":0.75,\"source\":\"D(1)\"}");
        ContentField roundTripped = parseContentField(serialize(field));

        assertEquals(ContentFieldType.fromString("futureField"), roundTripped.getType());
        assertEquals(1, roundTripped.getSpans().size());
        assertEquals(2, roundTripped.getSpans().get(0).getOffset());
        assertEquals(4, roundTripped.getSpans().get(0).getLength());
        assertEquals(0.75, roundTripped.getConfidence());
        assertEquals("D(1)", roundTripped.getSources().get(0).getRawValue());
    }

    @Test
    public void unrecognizedChunkingStrategyAndDocumentFigureRoundTrip() {
        ChunkingStrategy strategy = parseChunkingStrategy("{\"kind\":\"futureChunking\"}");
        DocumentFigure figure = parseDocumentFigure("{\"kind\":\"futureFigure\",\"id\":\"figure-1\","
            + "\"source\":\"D(1)\",\"span\":{\"offset\":5,\"length\":8},"
            + "\"elements\":[\"/paragraphs/0\"],\"description\":\"Future figure\",\"role\":\"supplementary\"}");

        ChunkingStrategy roundTrippedStrategy = parseChunkingStrategy(serialize(strategy));
        DocumentFigure roundTrippedFigure = parseDocumentFigure(serialize(figure));

        assertEquals(ChunkingStrategyKind.fromString("futureChunking"), roundTrippedStrategy.getKind());
        assertEquals(DocumentFigureKind.fromString("futureFigure"), roundTrippedFigure.getKind());
        assertEquals("figure-1", roundTrippedFigure.getId());
        assertEquals("D(1)", roundTrippedFigure.getSource());
        assertEquals(5, roundTrippedFigure.getSpan().getOffset());
        assertEquals("Future figure", roundTrippedFigure.getDescription());
    }

    @Test
    public void missingDiscriminatorUsesFallbackAndUnknownPropertiesAreIgnored() {
        AnalysisContent content = parseAnalysisContent(
            "{\"mimeType\":\"application/x-future\"," + "\"markdown\":\"content\",\"futureObject\":{\"nested\":true}}");

        assertEquals("application/x-future", content.getMimeType());
        assertEquals("content", content.getMarkdown());
        String serialized = serialize(content);
        assertFalse(serialized.contains("futureObject"));
        assertTrue(serialized.contains("\"mimeType\":\"application/x-future\""));
    }

    private static AnalysisContent parseAnalysisContent(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return AnalysisContent.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static ContentField parseContentField(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return ContentField.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static ChunkingStrategy parseChunkingStrategy(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return ChunkingStrategy.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static DocumentFigure parseDocumentFigure(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return DocumentFigure.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    private static String serialize(JsonSerializable<?> model) {
        try {
            StringWriter output = new StringWriter();
            JsonWriter writer = JsonProviders.createWriter(output);
            model.toJson(writer);
            writer.flush();
            return output.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to serialize polymorphic model fixture.", exception);
        }
    }
}
