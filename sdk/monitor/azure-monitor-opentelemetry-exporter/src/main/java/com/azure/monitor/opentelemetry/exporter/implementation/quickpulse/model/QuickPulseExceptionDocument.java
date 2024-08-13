// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public class QuickPulseExceptionDocument extends QuickPulseDocument {
    private String exception;
    private String exceptionMessage;
    private String exceptionType;

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeStringField("Exception", exception)
            .writeStringField("ExceptionMessage", exceptionMessage)
            .writeStringField("ExceptionType", exceptionType)
            .writeEndObject();
    }

    public static QuickPulseExceptionDocument fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            QuickPulseExceptionDocument document = new QuickPulseExceptionDocument();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(document, fieldName, reader)) {
                    continue;
                }

                if ("Exception".equals(fieldName)) {
                    document.exception = reader.getString();
                } else if ("ExceptionMessage".equals(fieldName)) {
                    document.exceptionMessage = reader.getString();
                } else if ("ExceptionType".equals(fieldName)) {
                    document.exceptionType = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return document;
        });
    }
}
