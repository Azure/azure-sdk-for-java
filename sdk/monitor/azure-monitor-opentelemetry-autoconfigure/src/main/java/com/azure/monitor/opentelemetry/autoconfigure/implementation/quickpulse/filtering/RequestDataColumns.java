package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RequestData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class RequestDataColumns extends TelemetryColumns {
    private String Url;
    private long Duration; // microseconds
    private int ResponseCode;
    private boolean Success;
    private String Name;

    public RequestDataColumns(RequestData requestData) {
        super();
        setCustomDimensions(requestData.getProperties(), requestData.getMeasurements());
        this.Url = requestData.getUrl();
        this.Duration = FormattedDuration.getDurationFromTelemetryItemDurationString(requestData.getDuration());
        try {
            this.ResponseCode = Integer.parseInt(requestData.getResponseCode());
        } catch (NumberFormatException e) {
            this.ResponseCode = -1;
        }
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

}
