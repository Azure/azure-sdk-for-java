// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

public final class QuickPulseDependencyDocument extends QuickPulseDocument {
    private String name;
    private String target;
    private boolean success;
    private String duration;
    private String resultCode;
    private String commandName;
    private String dependencyTypeName;
    private String operationName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

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

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getCommandName() {
        return commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getDependencyTypeName() {
        return dependencyTypeName;
    }

    public void setDependencyTypeName(String dependencyTypeName) {
        this.dependencyTypeName = dependencyTypeName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return toJsonShared(jsonWriter.writeStartObject()).writeStringField("Name", name)
            .writeStringField("Target", target)
            .writeBooleanField("Success", success)
            .writeStringField("Duration", duration)
            .writeStringField("ResultCode", resultCode)
            .writeStringField("CommandName", commandName)
            .writeStringField("DependencyTypeName", dependencyTypeName)
            .writeStringField("OperationName", operationName)
            .writeEndObject();
    }

    public static QuickPulseDependencyDocument fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            QuickPulseDependencyDocument document = new QuickPulseDependencyDocument();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if (fromJsonShared(document, fieldName, reader)) {
                    continue;
                }

                if ("Name".equals(fieldName)) {
                    document.name = reader.getString();
                } else if ("Target".equals(fieldName)) {
                    document.target = reader.getString();
                } else if ("Success".equals(fieldName)) {
                    document.success = reader.getBoolean();
                } else if ("Duration".equals(fieldName)) {
                    document.duration = reader.getString();
                } else if ("ResultCode".equals(fieldName)) {
                    document.resultCode = reader.getString();
                } else if ("CommandName".equals(fieldName)) {
                    document.commandName = reader.getString();
                } else if ("DependencyTypeName".equals(fieldName)) {
                    document.dependencyTypeName = reader.getString();
                } else if ("OperationName".equals(fieldName)) {
                    document.operationName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return document;
        });
    }
}
