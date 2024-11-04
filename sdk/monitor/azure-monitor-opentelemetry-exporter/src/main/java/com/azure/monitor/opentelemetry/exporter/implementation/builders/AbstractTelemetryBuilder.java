// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.configuration.ConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.configuration.StatsbeatConnectionString;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorBase;
import com.azure.monitor.opentelemetry.exporter.implementation.models.MonitorDomain;
import com.azure.monitor.opentelemetry.exporter.implementation.models.TelemetryItem;
import io.opentelemetry.sdk.resources.Resource;
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

    public void setResource(Resource resource) {
        telemetryItem.setResource(resource);
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
        getProperties().put(key, TelemetryTruncation.truncatePropertyValue(value, MAX_PROPERTY_VALUE_LENGTH, key));
    }

    public TelemetryItem build() {
        return telemetryItem;
    }

    protected abstract Map<String, String> getProperties();
}
