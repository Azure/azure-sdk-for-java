// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public class QuickPulseEnvelope implements JsonSerializable<QuickPulseEnvelope> {
    private List<QuickPulseDocument> documents;
    private String instrumentationKey;
    private List<QuickPulseMetrics> metrics;
    private int invariantVersion;
    private String timeStamp;
    private String version;
    private String streamId;
    private String machineName;
    private String instance;
    private String roleName;

    public List<QuickPulseDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<QuickPulseDocument> documents) {
        this.documents = documents;
    }

    public String getInstrumentationKey() {
        return instrumentationKey;
    }

    public void setInstrumentationKey(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    public List<QuickPulseMetrics> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<QuickPulseMetrics> metrics) {
        this.metrics = metrics;
    }

    public int getInvariantVersion() {
        return invariantVersion;
    }

    public void setInvariantVersion(int invariantVersion) {
        this.invariantVersion = invariantVersion;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeArrayField("Documents", documents, JsonWriter::writeJson)
            .writeStringField("InstrumentationKey", instrumentationKey)
            .writeArrayField("Metrics", metrics, JsonWriter::writeJson)
            .writeIntField("InvariantVersion", invariantVersion)
            .writeStringField("Timestamp", timeStamp)
            .writeStringField("Version", version)
            .writeStringField("StreamId", streamId)
            .writeStringField("MachineName", machineName)
            .writeStringField("Instance", instance)
            .writeStringField("RoleName", roleName)
            .writeEndObject();
    }

    public static QuickPulseEnvelope fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            QuickPulseEnvelope envelope = new QuickPulseEnvelope();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("Documents".equals(fieldName)) {
                    envelope.documents = reader.readArray(QuickPulseDocument::fromJson);
                } else if ("InstrumentationKey".equals(fieldName)) {
                    envelope.instrumentationKey = reader.getString();
                } else if ("Metrics".equals(fieldName)) {
                    envelope.metrics = reader.readArray(QuickPulseMetrics::fromJson);
                } else if ("InvariantVersion".equals(fieldName)) {
                    envelope.invariantVersion = reader.getInt();
                } else if ("Timestamp".equals(fieldName)) {
                    envelope.timeStamp = reader.getString();
                } else if ("Version".equals(fieldName)) {
                    envelope.version = reader.getString();
                } else if ("StreamId".equals(fieldName)) {
                    envelope.streamId = reader.getString();
                } else if ("MachineName".equals(fieldName)) {
                    envelope.machineName = reader.getString();
                } else if ("Instance".equals(fieldName)) {
                    envelope.instance = reader.getString();
                } else if ("RoleName".equals(fieldName)) {
                    envelope.roleName = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return envelope;
        });
    }
}
