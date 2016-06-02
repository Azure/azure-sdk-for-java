/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes Compute Resource Usage.
 */
public class UsageInner {
    /**
     * Gets or sets an enum describing the unit of measurement.
     */
    @JsonProperty(required = true)
    private String unit;

    /**
     * Gets or sets the current value of the usage.
     */
    @JsonProperty(required = true)
    private int currentValue;

    /**
     * Gets or sets the limit of usage.
     */
    @JsonProperty(required = true)
    private long limit;

    /**
     * Gets or sets the name of the type of usage.
     */
    @JsonProperty(required = true)
    private UsageName name;

    /**
     * Creates an instance of UsageInner class.
     */
    public UsageInner() {
        unit = "Count";
    }

    /**
     * Get the unit value.
     *
     * @return the unit value
     */
    public String unit() {
        return this.unit;
    }

    /**
     * Set the unit value.
     *
     * @param unit the unit value to set
     * @return the UsageInner object itself.
     */
    public UsageInner withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Get the currentValue value.
     *
     * @return the currentValue value
     */
    public int currentValue() {
        return this.currentValue;
    }

    /**
     * Set the currentValue value.
     *
     * @param currentValue the currentValue value to set
     * @return the UsageInner object itself.
     */
    public UsageInner withCurrentValue(int currentValue) {
        this.currentValue = currentValue;
        return this;
    }

    /**
     * Get the limit value.
     *
     * @return the limit value
     */
    public long limit() {
        return this.limit;
    }

    /**
     * Set the limit value.
     *
     * @param limit the limit value to set
     * @return the UsageInner object itself.
     */
    public UsageInner withLimit(long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public UsageName name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the UsageInner object itself.
     */
    public UsageInner withName(UsageName name) {
        this.name = name;
        return this;
    }

}
