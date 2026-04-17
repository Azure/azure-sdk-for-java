package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.List;

public class QuickPulseMonitoringDataPoints implements JsonSerializable<QuickPulseEnvelope> {
    private List<QuickPulseEnvelope> monitoringDataPoints;

    public QuickPulseMonitoringDataPoints(List<QuickPulseEnvelope> monitoringDataPoints) {
        this.monitoringDataPoints = monitoringDataPoints;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeArray(monitoringDataPoints, JsonWriter::writeJson, false);
    }

    public static QuickPulseMonitoringDataPoints fromJson(JsonReader jsonReader) throws IOException {
        List<QuickPulseEnvelope> dataPoints = jsonReader.readArray(QuickPulseEnvelope::fromJson);
        return new QuickPulseMonitoringDataPoints(dataPoints);
    }
}
