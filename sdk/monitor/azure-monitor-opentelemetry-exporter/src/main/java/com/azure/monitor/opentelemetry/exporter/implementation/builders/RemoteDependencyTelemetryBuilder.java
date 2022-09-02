/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.RemoteDependencyData;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class RemoteDependencyTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_DATA_LENGTH = 8192;
    private static final int MAX_RESULT_CODE_LENGTH = 1024;
    private static final int MAX_DEPENDENCY_TYPE_LENGTH = 1024;
    private static final int MAX_TARGET_NAME_LENGTH = 1024;

    private final RemoteDependencyData data;

    private RemoteDependencyTelemetryBuilder(RemoteDependencyData data) {
        super(data, "RemoteDependency", "RemoteDependencyData");
        this.data = data;
    }

    public static RemoteDependencyTelemetryBuilder create() {
        return new RemoteDependencyTelemetryBuilder(new RemoteDependencyData());
    }

    public void setId(String id) {
        data.setId(truncateTelemetry(id, MAX_ID_LENGTH, "RemoteDependency.id"));
    }

    public void setName(String name) {
        data.setName(truncateTelemetry(name, MAX_NAME_LENGTH, "RemoteDependency.name"));
    }

    public void setResultCode(String resultCode) {
        data.setResultCode(
            truncateTelemetry(resultCode, MAX_RESULT_CODE_LENGTH, "RemoteDependency.resultCode"));
    }

    public void setData(String data) {
        this.data.setData(truncateTelemetry(data, MAX_DATA_LENGTH, "RemoteDependency.data"));
    }

    public void setType(String type) {
        data.setType(truncateTelemetry(type, MAX_DEPENDENCY_TYPE_LENGTH, "RemoteDependency.type"));
    }

    public void setTarget(String target) {
        data.setTarget(truncateTelemetry(target, MAX_TARGET_NAME_LENGTH, "RemoteDependency.target"));
    }

    public void setDuration(String duration) {
        data.setDuration(duration);
    }

    public void setSuccess(Boolean success) {
        data.setSuccess(success);
    }

    public void addMeasurement(@Nullable String key, Double value) {
        if (key == null || key.isEmpty() || key.length() > MAX_MEASUREMENT_KEY_LENGTH) {
            // TODO (trask) log
            return;
        }
        Map<String, Double> measurements = data.getMeasurements();
        if (measurements == null) {
            measurements = new HashMap<>();
            data.setMeasurements(measurements);
        }
        measurements.put(key, value);
    }

    @Override
    protected Map<String, String> getProperties() {
        Map<String, String> properties = data.getProperties();
        if (properties == null) {
            properties = new HashMap<>();
            data.setProperties(properties);
        }
        return properties;
    }
}
