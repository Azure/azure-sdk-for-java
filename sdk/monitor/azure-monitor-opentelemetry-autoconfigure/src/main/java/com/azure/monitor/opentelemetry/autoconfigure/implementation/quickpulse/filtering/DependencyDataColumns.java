package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;


public class DependencyDataColumns extends TelemetryColumns {
    private String target;
    private long duration; // in microseconds
    private boolean success;
    private String name;
    private int resultCode;
    private String type;
    private String data;


    public DependencyDataColumns(RemoteDependencyData rdData) {
        super();
        setCustomDimensions(rdData.);
        this.target = rdData.getTarget();
        this.duration = FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration());
        this.success = rdData.isSuccess();
        this.name = rdData.getName();
        this.type = rdData.getType();
        this.data = rdData.getData();
        try {
            this.resultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            this.resultCode = -1;
        }


    }



}
