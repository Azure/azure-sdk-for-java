package com.azure.monitor.opentelemetry.autoconfigure.implementation.quickpulse.filtering;

import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.models.RemoteDependencyData;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.utils.FormattedDuration;

// Casing of private fields is to match the names of fields passed down via filtering configuration
public class DependencyDataColumns extends TelemetryColumns {
    private String Target;
    private long Duration; // in microseconds
    private boolean Success;
    private String Name;
    private int ResultCode;
    private String Type;
    private String Data;


    public DependencyDataColumns(RemoteDependencyData rdData) {
        super();
        setCustomDimensions(rdData.getProperties(), rdData.getMeasurements());
        this.Target = rdData.getTarget();
        this.Duration = FormattedDuration.getDurationFromTelemetryItemDurationString(rdData.getDuration());
        this.Success = rdData.isSuccess();
        this.Name = rdData.getName();
        this.Type = rdData.getType();
        this.Data = rdData.getData();
        try {
            this.ResultCode = Integer.parseInt(rdData.getResultCode());
        } catch (NumberFormatException e) {
            this.ResultCode = -1;
        }
    }

    // To be used for tests only
    public DependencyDataColumns(String target, long duration, boolean success, String name, int resultCode, String type, String data) {
        super();
        this.Target = target;
        this.Duration = duration;
        this.Success = success;
        this.Name = name;
        this.ResultCode = resultCode;
        this.Type = type;
        this.Data = data;
    }

    // To be used in tests only
    public void setSuccess(boolean success) {
        this.Success = success;
    }

    // To be used in tests only
    public void setResultCode(int responseCode) {
        this.ResultCode = responseCode;
    }

    // To be used in tests only
    public void setDuration(long duration) {
        this.Duration = duration;
    }

    // to be used in tests only
    public void setData(String data) {
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
