// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A review diagnostic attached to an API navigation identifier.
 */
public class Diagnostic implements JsonSerializable<Diagnostic> {
    private static final AtomicInteger DIAGNOSTIC_ID_COUNTER = new AtomicInteger(1);

    private String diagnosticId;
    private final String text;
    private final String helpLinkUri;
    private final String targetId;
    private final DiagnosticKind level;

    /**
     * Creates a diagnostic.
     *
     * @param level the level
     * @param targetId the target id
     * @param text the text
     */
    public Diagnostic(DiagnosticKind level, String targetId, String text) {
        this(level, targetId, text, null);
    }

    /**
     * Creates a diagnostic.
     *
     * @param level the level
     * @param targetId the target id
     * @param text the text
     * @param helpLinkUri the help link uri
     */
    public Diagnostic(DiagnosticKind level, String targetId, String text, String helpLinkUri) {
        this.diagnosticId = "AZ_JAVA_" + DIAGNOSTIC_ID_COUNTER.getAndIncrement();
        this.targetId = targetId;
        this.text = text;
        this.level = level;
        this.helpLinkUri = helpLinkUri;
    }

    /**
     * Returns the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the help link uri.
     *
     * @return the help link uri
     */
    public String getHelpLinkUri() {
        return helpLinkUri;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeStringField("DiagnosticId", diagnosticId)
            .writeStringField("Text", text)
            .writeStringField("HelpLinkUri", helpLinkUri)
            .writeStringField("TargetId", targetId);

        if (level != null) {
            jsonWriter.writeIntField("Level", level.getLevel());
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads this model from its JSON representation.
     *
     * @param jsonReader the json reader
     * @return the diagnostic read from JSON
     * @throws IOException if the operation cannot be completed
     */
    public static Diagnostic fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String diagnosticId = null;
            String text = null;
            String helpLinkUri = null;
            String targetId = null;
            DiagnosticKind level = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("DiagnosticId".equals(fieldName)) {
                    diagnosticId = reader.getString();
                } else if ("Text".equals(fieldName)) {
                    text = reader.getString();
                } else if ("HelpLinkUri".equals(fieldName)) {
                    helpLinkUri = reader.getString();
                } else if ("TargetId".equals(fieldName)) {
                    targetId = reader.getString();
                } else if ("Level".equals(fieldName)) {
                    level = DiagnosticKind.fromInt(reader.getInt());
                } else {
                    reader.skipChildren();
                }
            }

            Diagnostic diagnostic = new Diagnostic(level, targetId, text, helpLinkUri);
            diagnostic.diagnosticId = diagnosticId;

            return diagnostic;
        });
    }
}
