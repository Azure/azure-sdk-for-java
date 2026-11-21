package com.azure.tools.apiview.processor.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class Diagnostic implements JsonSerializable<Diagnostic> {
    private static int diagnosticIdCounter = 1;

    private String diagnosticId;
    private final String text;
    private final String helpLinkUri;
    private final String targetId;
    private final DiagnosticKind level;

    public Diagnostic(DiagnosticKind level, String targetId, String text) {
        this(level, targetId, text, null);
    }

    public Diagnostic(DiagnosticKind level, String targetId, String text, String helpLinkUri) {
        this.diagnosticId = "AZ_JAVA_" + diagnosticIdCounter++;
        this.targetId = targetId;
        this.text = text;
        this.level = level;
        this.helpLinkUri = helpLinkUri;
    }

    public String getText() {
        return text;
    }

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

    public static Diagnostic fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String diagnosticId = null;
            String text = null;
            String helpLinkUri = null;
            String targetId = null;
            DiagnosticKind level = null;

            while (reader.nextToken() != null) {
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
