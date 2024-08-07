// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Map;

public class QuickPulseDocument implements JsonSerializable<QuickPulseDocument> {
    private String type;
    private String documentType;
    private String version;
    private String operationId;
    private Map<String, String> properties;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("__type", type)
            .writeStringField("DocumentType", documentType)
            .writeStringField("Version", version)
            .writeStringField("OperationId", operationId)
            .writeMapField("Properties", properties, JsonWriter::writeString, true)
            .writeEndObject();
    }

    JsonWriter toJsonShared(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStringField("__type", type)
            .writeStringField("DocumentType", documentType)
            .writeStringField("Version", version)
            .writeStringField("OperationId", operationId)
            .writeMapField("Properties", properties, JsonWriter::writeString, true);
    }

    public static QuickPulseDocument fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            QuickPulseDocument document = new QuickPulseDocument();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (!fromJsonShared(document, fieldName, reader)) {
                    reader.skipChildren();
                }
            }

            return document;
        });
    }

    static boolean fromJsonShared(QuickPulseDocument document, String fieldName, JsonReader jsonReader)
        throws IOException {
        if ("__type".equals(fieldName)) {
            document.type = jsonReader.getString();
            return true;
        } else if ("DocumentType".equals(fieldName)) {
            document.documentType = jsonReader.getFieldName();
            return true;
        } else if ("Version".equals(fieldName)) {
            document.version = jsonReader.getString();
            return true;
        } else if ("OperationId".equals(fieldName)) {
            document.operationId = jsonReader.getFieldName();
            return true;
        } else if ("Properties".equals(fieldName)) {
            document.properties = jsonReader.readMap(JsonReader::getString);
            return true;
        }

        return false;
    }
}
