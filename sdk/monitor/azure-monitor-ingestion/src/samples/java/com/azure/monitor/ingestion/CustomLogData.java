// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * An object representing custom log data as defined in the data collection rule's stream declaration.
 */
public final class CustomLogData {

    @JsonProperty(value = "Time")
    private OffsetDateTime time;


    @JsonProperty(value = "ExtendedColumn")
    private String extendedColumn;


    @JsonProperty(value = "AdditionalContext")
    private String additionalContext;

    /**
     * Returns the time of creation of this log.
     * @return the time of creation of this log.
     */
    public OffsetDateTime getTime() {
        return time;
    }

    /**
     * Sets the time of creation of this log.
     * @param time the time of creation of this log.
     * @return the updated {@link CustomLogData}.
     */
    public CustomLogData setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * Returns the extended column field of this log.
     * @return the extended column field of this log.
     */
    public String getExtendedColumn() {
        return extendedColumn;
    }

    /**
     * Sets the extended column field of this log.
     * @param extendedColumn the extended column field of this log.
     * @return the updated {@link CustomLogData}.
     */
    public CustomLogData setExtendedColumn(String extendedColumn) {
        this.extendedColumn = extendedColumn;
        return this;
    }

    /**
     * Returns the additional context field of this log.
     * @return the additional context field of this log.
     */
    public String getAdditionalContext() {
        return additionalContext;
    }

    /**
     * Sets the additional context field of this log.
     * @param additionalContext the additional context field of this log.
     * @return the updated {@link CustomLogData}.
     */
    public CustomLogData setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
        return this;
    }
}
