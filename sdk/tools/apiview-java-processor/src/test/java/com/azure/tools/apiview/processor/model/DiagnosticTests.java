// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

public class DiagnosticTests {
    @Test
    public void multipleDiagnosticsRoundTrip() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            List<Diagnostic> diagnostics = Arrays.asList(
                new Diagnostic(DiagnosticKind.WARNING, "first", "First diagnostic", "https://example.com/help"),
                new Diagnostic(DiagnosticKind.ERROR, "second", "Second diagnostic"));
            byte[] json = serializeDiagnostics(diagnostics);

            try (JsonReader reader = JsonProviders.createReader(json)) {
                List<Diagnostic> actual = reader.readArray(Diagnostic::fromJson);

                assertEquals(2, actual.size());
                assertArrayEquals(json, serializeDiagnostics(actual));
            }
        });
    }

    @Test
    public void skipsUnknownNestedValuesAndPreservesFollowingField() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            String json = "{\"diagnostic\":{\"Unknown\":{\"Text\":\"Ignored\",\"Values\":[1,{\"Nested\":true}]},"
                + "\"Text\":\"Expected\",\"TargetId\":\"target\",\"Level\":1},\"next\":\"Preserved\"}";

            try (JsonReader reader = JsonProviders.createReader(json)) {
                assertEquals(JsonToken.START_OBJECT, reader.nextToken());
                assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
                assertEquals("diagnostic", reader.getFieldName());
                reader.nextToken();

                Diagnostic diagnostic = Diagnostic.fromJson(reader);

                assertEquals("Expected", diagnostic.getText());
                assertEquals(JsonToken.END_OBJECT, reader.currentToken());
                assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
                assertEquals("next", reader.getFieldName());
                reader.nextToken();
                assertEquals("Preserved", reader.getString());
            }
        });
    }

    private static byte[] serializeDiagnostics(List<Diagnostic> diagnostics) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            writer.writeStartArray();
            for (Diagnostic diagnostic : diagnostics) {
                writer.writeJson(diagnostic);
            }
            writer.writeEndArray();
        }
        return output.toByteArray();
    }
}
