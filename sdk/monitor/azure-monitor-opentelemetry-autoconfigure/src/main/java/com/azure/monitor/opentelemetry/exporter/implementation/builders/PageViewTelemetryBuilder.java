// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.builders;

import com.azure.monitor.opentelemetry.exporter.implementation.models.PageViewData;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.azure.monitor.opentelemetry.exporter.implementation.builders.TelemetryTruncation.truncateTelemetry;

public final class PageViewTelemetryBuilder extends AbstractTelemetryBuilder {

    private static final int MAX_URL_LENGTH = 2048;

    private final PageViewData data;

    public static PageViewTelemetryBuilder create() {
        return new PageViewTelemetryBuilder(new PageViewData());
    }

    private PageViewTelemetryBuilder(PageViewData data) {
        super(data, "PageView", "PageViewData");
        this.data = data;
    }

    public void setId(String id) {
        data.setId(truncateTelemetry(id, MAX_ID_LENGTH, "PageView.id"));
    }

    public void setName(String name) {
        data.setName(truncateTelemetry(name, MAX_NAME_LENGTH, "PageView.name"));
    }

    public void setUrl(String url) {
        data.setUrl(truncateTelemetry(url, MAX_URL_LENGTH, "PageView.url"));
    }

    public void setDuration(String duration) {
        data.setDuration(duration);
    }

    public void setReferredUri(String referredUri) {
        data.setReferredUri(truncateTelemetry(referredUri, MAX_URL_LENGTH, "PageView.referredUri"));
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
