// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/** Response containing the status of each telemetry item. */
@Fluent
public final class ExportResult {
    /*
     * The number of items received.
     */
    @JsonProperty(value = "itemsReceived")
    private Integer itemsReceived;

    /*
     * The number of items accepted.
     */
    @JsonProperty(value = "itemsAccepted")
    private Integer itemsAccepted;

    /*
     * An array of error detail objects.
     */
    @JsonProperty(value = "errors")
    private List<TelemetryErrorDetails> errors;

    /**
     * Get the itemsReceived property: The number of items received.
     *
     * @return the itemsReceived value.
     */
    public Integer getItemsReceived() {
        return this.itemsReceived;
    }

    /**
     * Set the itemsReceived property: The number of items received.
     *
     * @param itemsReceived the itemsReceived value to set.
     * @return the ExportResult object itself.
     */
    public ExportResult setItemsReceived(Integer itemsReceived) {
        this.itemsReceived = itemsReceived;
        return this;
    }

    /**
     * Get the itemsAccepted property: The number of items accepted.
     *
     * @return the itemsAccepted value.
     */
    public Integer getItemsAccepted() {
        return this.itemsAccepted;
    }

    /**
     * Set the itemsAccepted property: The number of items accepted.
     *
     * @param itemsAccepted the itemsAccepted value to set.
     * @return the ExportResult object itself.
     */
    public ExportResult setItemsAccepted(Integer itemsAccepted) {
        this.itemsAccepted = itemsAccepted;
        return this;
    }

    /**
     * Get the errors property: An array of error detail objects.
     *
     * @return the errors value.
     */
    public List<TelemetryErrorDetails> getErrors() {
        return this.errors;
    }

    /**
     * Set the errors property: An array of error detail objects.
     *
     * @param errors the errors value to set.
     * @return the ExportResult object itself.
     */
    public ExportResult setErrors(List<TelemetryErrorDetails> errors) {
        this.errors = errors;
        return this;
    }
}
