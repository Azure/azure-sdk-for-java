package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

public class RequestDataColumns extends TelemetryColumns {
    private String url;
    private long duration; // microseconds
    private int responseCode;
    private boolean success;
    private String name;

    public RequestDataColumns(RequestData requestData) {
        this.url = requestData.getUrl();
        this.duration = FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration());
        try {
            this.responseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            this.responseCode = -1;
        }
        this.success = requestData.isSuccess();
        this.name = requestData.getName();
    }

}
