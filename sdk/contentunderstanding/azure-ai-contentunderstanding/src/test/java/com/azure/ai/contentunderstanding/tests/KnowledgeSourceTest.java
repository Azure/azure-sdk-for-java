// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.KnowledgeSource;
import com.azure.ai.contentunderstanding.models.KnowledgeSourceKind;
import com.azure.ai.contentunderstanding.models.LabeledDataKnowledgeSource;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KnowledgeSourceTest {
    @Test
    public void labeledDataDiscriminatorDeserializesKnownSubtype() {
        KnowledgeSource source = parseKnowledgeSource("{\"kind\":\"labeledData\","
            + "\"containerUrl\":\"https://example.test/container\",\"prefix\":\"receipts/\","
            + "\"fileListPath\":\"training/files.jsonl\"}");

        assertTrue(source instanceof LabeledDataKnowledgeSource);
        LabeledDataKnowledgeSource labeledSource = (LabeledDataKnowledgeSource) source;
        assertEquals(KnowledgeSourceKind.LABELED_DATA, labeledSource.getKind());
        assertEquals("https://example.test/container", labeledSource.getContainerUrl());
        assertEquals("receipts/", labeledSource.getPrefix());
        assertEquals("training/files.jsonl", labeledSource.getFileListPath());
    }

    @Test
    public void labeledDataSerializationRoundTripsAllProperties() {
        LabeledDataKnowledgeSource source
            = new LabeledDataKnowledgeSource().setContainerUrl("https://example.test/container")
                .setPrefix("receipts/")
                .setFileListPath("training/files.jsonl");

        String serialized = serializeKnowledgeSource(source);
        assertTrue(serialized.contains("\"kind\":\"labeledData\""));
        assertTrue(serialized.contains("\"containerUrl\":\"https://example.test/container\""));
        assertTrue(serialized.contains("\"prefix\":\"receipts/\""));
        assertTrue(serialized.contains("\"fileListPath\":\"training/files.jsonl\""));

        LabeledDataKnowledgeSource roundTripped = (LabeledDataKnowledgeSource) parseKnowledgeSource(serialized);
        assertEquals(KnowledgeSourceKind.LABELED_DATA, roundTripped.getKind());
        assertEquals("https://example.test/container", roundTripped.getContainerUrl());
        assertEquals("receipts/", roundTripped.getPrefix());
        assertEquals("training/files.jsonl", roundTripped.getFileListPath());
    }

    @Test
    public void unrecognizedDiscriminatorPreservesKind() {
        KnowledgeSource source = parseKnowledgeSource("{\"kind\":\"futureKnowledgeSource\"}");

        assertEquals(KnowledgeSourceKind.fromString("futureKnowledgeSource"), source.getKind());
    }

    @Test
    public void unrecognizedDiscriminatorPreservesKindAcrossRoundTrip() {
        KnowledgeSource source = parseKnowledgeSource("{\"kind\":\"futureKnowledgeSource\",\"futureValue\":42}");

        String serialized = serializeKnowledgeSource(source);
        assertTrue(serialized.contains("\"kind\":\"futureKnowledgeSource\""));
        KnowledgeSource roundTripped = parseKnowledgeSource(serialized);
        assertEquals(KnowledgeSourceKind.fromString("futureKnowledgeSource"), roundTripped.getKind());
    }

    @Test
    public void baseTypeSerializesDefaultKind() {
        KnowledgeSource source = new KnowledgeSource();

        assertEquals(KnowledgeSourceKind.fromString("KnowledgeSource"), source.getKind());
        assertTrue(serializeKnowledgeSource(source).contains("\"kind\":\"KnowledgeSource\""));
    }

    private static KnowledgeSource parseKnowledgeSource(String json) {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return KnowledgeSource.fromJson(reader);
        } catch (IOException exception) {
            throw new RuntimeException("Failed to parse knowledge source JSON", exception);
        }
    }

    private static String serializeKnowledgeSource(KnowledgeSource source) {
        try {
            StringWriter output = new StringWriter();
            JsonWriter writer = JsonProviders.createWriter(output);
            source.toJson(writer);
            writer.flush();
            return output.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to serialize knowledge source JSON", exception);
        }
    }

}
