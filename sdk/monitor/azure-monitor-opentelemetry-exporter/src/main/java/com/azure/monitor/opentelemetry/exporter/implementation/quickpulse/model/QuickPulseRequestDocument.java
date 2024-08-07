// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class QuickPulseRequestDocument extends QuickPulseDocument {
    private String name;
    private boolean success;
    private String duration;
    private String responseCode;
    private String operationName;
    private String url;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject())
            .writeStringField("Name", name)
            .writeBooleanField("Success", success)
            .writeStringField("Duration", duration)
            .writeStringField("ResponseCode", responseCode)
            .writeStringField("OperationName", operationName)
            .writeStringField("Url", url)
            .writeEndObject();
    }

    public static QuickPulseRequestDocument fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            QuickPulseRequestDocument document = new QuickPulseRequestDocument();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(document, fieldName, reader)) {
                    continue;
                }

                if ("Name".equals(fieldName)) {
                    document.name = reader.getString();
                } else if ("Success".equals(fieldName)) {
                    document.success = reader.getBoolean();
                } else if ("Duration".equals(fieldName)) {
                    document.duration = reader.getString();
                } else if ("ResponseCode".equals(fieldName)) {
                    document.responseCode = reader.getString();
                } else if ("OperationName".equals(fieldName)) {
                    document.operationName = reader.getString();
                } else if ("Url".equals(fieldName)) {
                    document.url = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return document;
        });
    }
}
