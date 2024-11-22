// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class RequestDataColumns extends TelemetryColumns {
    private final String Url;
    private final long Duration; // microseconds
    private final int ResponseCode;
    private final boolean Success;
    private final String Name;

    public RequestDataColumns(RequestData requestData) {
        super();
        setCustomDimensions(requestData.getProperties(), requestData.getMeasurements());
        this.Url = requestData.getUrl();
        this.Duration = FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration());
        int responseCode;
        try {
            responseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            responseCode = -1;
        }
        this.ResponseCode = responseCode;
        this.Success = requestData.isSuccess();
        this.Name = requestData.getName();
    }

    // To be used in tests only
    public RequestDataColumns(String url, long duration, int responseCode, boolean success, String name) {
        super();
        this.Url = url;
        this.Duration = duration;
        this.ResponseCode = responseCode;
        this.Success = success;
        this.Name = name;
    }

    // To be used in tests only
    public String getUrl() {
        return this.Url;
    }

    // To be used in tests only
    public long getDuration() {
        return this.Duration;
    }

    // To be used in tests only
    public int getResponseCode() {
        return this.ResponseCode;
    }

    // To be used in tests only
    public boolean getSuccess() {
        return this.Success;
    }

    // To be used in tests only
    public String getName() {
        return this.Name;
    }

}
