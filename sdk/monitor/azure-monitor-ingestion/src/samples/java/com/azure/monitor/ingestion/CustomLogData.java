// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public final class CustomLogData {

    @JsonProperty(value = "Time")
    private OffsetDateTime time;


    @JsonProperty(value = "ExtendedColumn")
    private String extendedColumn;


    @JsonProperty(value = "AdditionalContext")
    private String additionalContext;

    public OffsetDateTime getTime() {
        return time;
    }

    public CustomLogData setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    public String getExtendedColumn() {
        return extendedColumn;
    }

    public CustomLogData setExtendedColumn(String extendedColumn) {
        this.extendedColumn = extendedColumn;
        return this;
    }

    public String getAdditionalContext() {
        return additionalContext;
    }

    public CustomLogData setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
        return this;
    }
}
