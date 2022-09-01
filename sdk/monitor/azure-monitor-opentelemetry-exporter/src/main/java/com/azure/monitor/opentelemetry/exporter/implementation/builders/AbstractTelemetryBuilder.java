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

import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import reactor.util.annotation.Nullable;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTelemetryBuilder {

    private static final int MAX_PROPERTY_KEY_LENGTH = 150;
    private static final int MAX_PROPERTY_VALUE_LENGTH = 8192;

    protected static final int MAX_MEASUREMENT_KEY_LENGTH = 150;

    protected static final int MAX_NAME_LENGTH = 1024;
    protected static final int MAX_ID_LENGTH = 512;

    private final TelemetryItem telemetryItem;

    protected AbstractTelemetryBuilder(MonitorDomain data, String telemetryName, String baseType) {

        telemetryItem = new TelemetryItem();
        telemetryItem.setVersion(1);
        telemetryItem.setName(telemetryName);

        data.setVersion(2);

        MonitorBase monitorBase = new MonitorBase();
        telemetryItem.setData(monitorBase);
        monitorBase.setBaseType(baseType);
        monitorBase.setBaseData(data);
    }

    public void setTime(OffsetDateTime time) {
        telemetryItem.setTime(time);
    }

    public void setSampleRate(float sampleRate) {
        telemetryItem.setSampleRate(sampleRate);
    }

    public void setConnectionString(String connectionString) {
        telemetryItem.setConnectionString(connectionString);
    }

    public void setConnectionString(ConnectionString connectionString) {
        telemetryItem.setConnectionString(connectionString);
    }

    public void setConnectionString(StatsbeatConnectionString connectionString) {
        telemetryItem.setConnectionString(connectionString);
    }

    public void addTag(String key, String value) {
        Map<String, String> tags = telemetryItem.getTags();
        if (tags == null) {
            tags = new HashMap<>();
            telemetryItem.setTags(tags);
        }
        tags.put(key, value);
    }

    public void addProperty(@Nullable String key, @Nullable String value) {
        if (key == null || key.isEmpty() || key.length() > MAX_PROPERTY_KEY_LENGTH || value == null) {
            // TODO (trask) log
            return;
        }
        getProperties()
            .put(key, TelemetryTruncation.truncatePropertyValue(value, MAX_PROPERTY_VALUE_LENGTH, key));
    }

    public TelemetryItem build() {
        return telemetryItem;
    }

    protected abstract Map<String, String> getProperties();
}
