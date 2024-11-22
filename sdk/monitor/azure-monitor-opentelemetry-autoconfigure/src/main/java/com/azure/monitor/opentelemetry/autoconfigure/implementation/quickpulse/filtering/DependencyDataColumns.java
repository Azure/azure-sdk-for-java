// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class DependencyDataColumns extends TelemetryColumns {
    private final String Target;
    private final long Duration; // in microseconds
    private final boolean Success;
    private final String Name;
    private final int ResultCode;
    private final String Type;
    private final String Data;

    public DependencyDataColumns(RemoteDependencyData rdData) {
        super();
        setCustomDimensions(rdData.getProperties(), rdData.getMeasurements());
        this.Target = rdData.getTarget();
        this.Duration = FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration());
        this.Success = rdData.isSuccess();
        this.Name = rdData.getName();
        this.Type = rdData.getType();
        this.Data = rdData.getData();
        int resultCode;
        try {
            resultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            resultCode = -1;
        }
        this.ResultCode = resultCode;
    }

    // To be used for tests only
    public DependencyDataColumns(String target, long duration, boolean success, String name, int resultCode,
        String type, String data) {
        super();
        this.Target = target;
        this.Duration = duration;
        this.Success = success;
        this.Name = name;
        this.ResultCode = resultCode;
        this.Type = type;
        this.Data = data;
    }

    public String getTarget() {
        return this.Target;
    }

    public long getDuration() {
        return this.Duration;
    }

    public boolean getSuccess() {
        return this.Success;
    }

    public String getName() {
        return this.Name;
    }

    public String getData() {
        return this.Data;
    }

    public String getType() {
        return this.Type;
    }

    public int getResultCode() {
        return this.ResultCode;
    }

}
