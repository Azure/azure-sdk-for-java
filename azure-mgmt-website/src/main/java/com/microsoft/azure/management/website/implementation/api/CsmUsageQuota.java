/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;

/**
 * Usage of the quota resource.
 */
public class CsmUsageQuota {
    /**
     * Units of measurement for the quota resourse.
     */
    private String unit;

    /**
     * Next reset time for the resource counter.
     */
    private DateTime nextResetTime;

    /**
     * The current value of the resource counter.
     */
    private Long currentValue;

    /**
     * The resource limit.
     */
    private Long limit;

    /**
     * Quota name.
     */
    private LocalizableString name;

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
     * @return the CsmUsageQuota object itself.
     */
    public CsmUsageQuota withUnit(String unit) {
        this.unit = unit;
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
     * @return the CsmUsageQuota object itself.
     */
    public CsmUsageQuota withNextResetTime(DateTime nextResetTime) {
        this.nextResetTime = nextResetTime;
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
     * @return the CsmUsageQuota object itself.
     */
    public CsmUsageQuota withCurrentValue(Long currentValue) {
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
     * @return the CsmUsageQuota object itself.
     */
    public CsmUsageQuota withLimit(Long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public LocalizableString name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the CsmUsageQuota object itself.
     */
    public CsmUsageQuota withName(LocalizableString name) {
        this.name = name;
        return this;
    }

}
