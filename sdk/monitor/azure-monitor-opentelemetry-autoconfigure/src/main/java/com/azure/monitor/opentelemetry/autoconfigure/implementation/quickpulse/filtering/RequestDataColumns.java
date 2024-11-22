// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class RequestDataColumns extends TelemetryColumns {
    private final String url;
    private final long duration; // microseconds
    private final int responseCode;
    private final boolean success;
    private final String name;

    public RequestDataColumns(RequestData requestData) {
        super();
        setCustomDimensions(requestData.getProperties(), requestData.getMeasurements());
        this.url = requestData.getUrl();
        this.duration = FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration());
        int responseCode;
        try {
            responseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            responseCode = -1;
        }
        this.responseCode = responseCode;
        this.success = requestData.isSuccess();
        this.name = requestData.getName();
    }

    // To be used in tests only
    public RequestDataColumns(String url, long duration, int responseCode, boolean success, String name) {
        super();
        this.url = url;
        this.duration = duration;
        this.responseCode = responseCode;
        this.success = success;
        this.name = name;
    }

    @Override
    public Object getFieldValue(String fieldName) {
        if (fieldName.equals(KnownRequestColumns.DURATION)) {
            return this.duration;
        } else if (fieldName.equals(KnownRequestColumns.SUCCESS)) {
            return this.success;
        } else if (fieldName.equals(KnownRequestColumns.NAME)) {
            return this.name;
        } else if (fieldName.equals(KnownRequestColumns.RESPONSE_CODE)) {
            return this.responseCode;
        } else if (fieldName.equals(KnownRequestColumns.URL)) {
            return this.url;
        } else {
            return null;
        }
    }

    // To be used in tests only
    public String getUrl() {
        return this.url;
    }

    // To be used in tests only
    public long getDuration() {
        return this.duration;
    }

    // To be used in tests only
    public int getResponseCode() {
        return this.responseCode;
    }

    // To be used in tests only
    public boolean getSuccess() {
        return this.success;
    }

    // To be used in tests only
    public String getName() {
        return this.name;
    }

}
