// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class DependencyDataColumns extends TelemetryColumns {
    private final String target;
    private final long duration; // in microseconds
    private final boolean success;
    private final String name;
    private final int resultCode;
    private final String type;
    private final String data;

    public DependencyDataColumns(RemoteDependencyData rdData) {
        super();
        setCustomDimensions(rdData.getProperties(), rdData.getMeasurements());
        this.target = rdData.getTarget();
        this.duration = FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration());
        this.success = rdData.isSuccess();
        this.name = rdData.getName();
        this.type = rdData.getType();
        this.data = rdData.getData();
        int resultCode;
        try {
            resultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            resultCode = -1;
        }
        this.resultCode = resultCode;
    }

    // To be used for tests only
    public DependencyDataColumns(String target, long duration, boolean success, String name, int resultCode,
        String type, String data) {
        super();
        this.target = target;
        this.duration = duration;
        this.success = success;
        this.name = name;
        this.resultCode = resultCode;
        this.type = type;
        this.data = data;
    }

    @Override
    public Object getFieldValue(String fieldName) {

        if (fieldName.equals(KnownDependencyColumns.TARGET)) {
            return this.target;
        } else if (fieldName.equals(KnownDependencyColumns.DURATION)) {
            return this.duration;
        } else if (fieldName.equals(KnownDependencyColumns.SUCCESS)) {
            return this.success;
        } else if (fieldName.equals(KnownDependencyColumns.NAME)) {
            return this.name;
        } else if (fieldName.equals(KnownDependencyColumns.RESULT_CODE)) {
            return this.resultCode;
        } else if (fieldName.equals(KnownDependencyColumns.TYPE)) {
            return this.type;
        } else if (fieldName.equals(KnownDependencyColumns.DATA)) {
            return this.data;
        } else {
            return null;
        }
    }

    public String getTarget() {
        return this.target;
    }

    public long getDuration() {
        return this.duration;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public String getName() {
        return this.name;
    }

    public String getData() {
        return this.data;
    }

    public String getType() {
        return this.type;
    }

    public int getResultCode() {
        return this.resultCode;
    }

}
