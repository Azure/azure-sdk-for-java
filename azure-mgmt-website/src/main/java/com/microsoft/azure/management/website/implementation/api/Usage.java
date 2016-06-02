/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Class that represents usage of the quota resource.
 */
@JsonFlatten
public class Usage extends Resource {
    /**
     * Friendly name shown in the UI.
     */
    @JsonProperty(value = "properties.displayName")
    private String displayName;

    /**
     * Name of the quota.
     */
    @JsonProperty(value = "properties.name")
    private String usageName;

    /**
     * Name of the quota resource.
     */
    @JsonProperty(value = "properties.resourceName")
    private String resourceName;

    /**
     * Units of measurement for the quota resource.
     */
    @JsonProperty(value = "properties.unit")
    private String unit;

    /**
     * The current value of the resource counter.
     */
    @JsonProperty(value = "properties.currentValue")
    private Long currentValue;

    /**
     * The resource limit.
     */
    @JsonProperty(value = "properties.limit")
    private Long limit;

    /**
     * Next reset time for the resource counter.
     */
    @JsonProperty(value = "properties.nextResetTime")
    private DateTime nextResetTime;

    /**
     * ComputeMode used for this usage. Possible values include: 'Shared',
     * 'Dedicated', 'Dynamic'.
     */
    @JsonProperty(value = "properties.computeMode")
    private ComputeModeOptions computeMode;

    /**
     * SiteMode used for this usage.
     */
    @JsonProperty(value = "properties.siteMode")
    private String siteMode;

    /**
     * Get the displayName value.
     *
     * @return the displayName value
     */
    public String displayName() {
        return this.displayName;
    }

    /**
     * Set the displayName value.
     *
     * @param displayName the displayName value to set
     * @return the Usage object itself.
     */
    public Usage withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    /**
     * Get the usageName value.
     *
     * @return the usageName value
     */
    public String usageName() {
        return this.usageName;
    }

    /**
     * Set the usageName value.
     *
     * @param usageName the usageName value to set
     * @return the Usage object itself.
     */
    public Usage withUsageName(String usageName) {
        this.usageName = usageName;
        return this;
    }

    /**
     * Get the resourceName value.
     *
     * @return the resourceName value
     */
    public String resourceName() {
        return this.resourceName;
    }

    /**
     * Set the resourceName value.
     *
     * @param resourceName the resourceName value to set
     * @return the Usage object itself.
     */
    public Usage withResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
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
     * @return the Usage object itself.
     */
    public Usage withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    /**
     * Get the currentValue value.
     *
     * @return the currentValue value
     */
    public Long currentValue() {
        return this.currentValue;
    }

    /**
     * Set the currentValue value.
     *
     * @param currentValue the currentValue value to set
     * @return the Usage object itself.
     */
    public Usage withCurrentValue(Long currentValue) {
        this.currentValue = currentValue;
        return this;
    }

    /**
     * Get the limit value.
     *
     * @return the limit value
     */
    public Long limit() {
        return this.limit;
    }

    /**
     * Set the limit value.
     *
     * @param limit the limit value to set
     * @return the Usage object itself.
     */
    public Usage withLimit(Long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the nextResetTime value.
     *
     * @return the nextResetTime value
     */
    public DateTime nextResetTime() {
        return this.nextResetTime;
    }

    /**
     * Set the nextResetTime value.
     *
     * @param nextResetTime the nextResetTime value to set
     * @return the Usage object itself.
     */
    public Usage withNextResetTime(DateTime nextResetTime) {
        this.nextResetTime = nextResetTime;
        return this;
    }

    /**
     * Get the computeMode value.
     *
     * @return the computeMode value
     */
    public ComputeModeOptions computeMode() {
        return this.computeMode;
    }

    /**
     * Set the computeMode value.
     *
     * @param computeMode the computeMode value to set
     * @return the Usage object itself.
     */
    public Usage withComputeMode(ComputeModeOptions computeMode) {
        this.computeMode = computeMode;
        return this;
    }

    /**
     * Get the siteMode value.
     *
     * @return the siteMode value
     */
    public String siteMode() {
        return this.siteMode;
    }

    /**
     * Set the siteMode value.
     *
     * @param siteMode the siteMode value to set
     * @return the Usage object itself.
     */
    public Usage withSiteMode(String siteMode) {
        this.siteMode = siteMode;
        return this;
    }

}
