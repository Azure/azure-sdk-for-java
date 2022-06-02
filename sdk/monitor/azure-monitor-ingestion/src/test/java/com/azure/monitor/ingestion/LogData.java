package com.azure.monitor.ingestion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public class LogData {

    @JsonProperty(value = "Time")
    private OffsetDateTime time;


    @JsonProperty(value = "ExtendedColumn")
    private String extendedColumn;


    @JsonProperty(value = "AdditionalContext")
    private String additionalContext;

    public OffsetDateTime getTime() {
        return time;
    }

    public LogData setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public String getExtendedColumn() {
        return extendedColumn;
    }

    public LogData setExtendedColumn(String extendedColumn) {
        this.extendedColumn = extendedColumn;
        return this;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public LogData setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
        return this;
    }
}
