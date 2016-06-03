/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Represents metric limits set on a web app.
 */
public class SiteLimits {
    /**
     * Maximum allowed CPU usage percentage.
     */
    private Double maxPercentageCpu;

    /**
     * Maximum allowed memory usage in MB.
     */
    private Long maxMemoryInMb;

    /**
     * Maximum allowed disk size usage in MB.
     */
    private Long maxDiskSizeInMb;

    /**
     * Get the maxPercentageCpu value.
     *
     * @return the maxPercentageCpu value
     */
    public Double maxPercentageCpu() {
        return this.maxPercentageCpu;
    }

    /**
     * Set the maxPercentageCpu value.
     *
     * @param maxPercentageCpu the maxPercentageCpu value to set
     * @return the SiteLimits object itself.
     */
    public SiteLimits withMaxPercentageCpu(Double maxPercentageCpu) {
        this.maxPercentageCpu = maxPercentageCpu;
        return this;
    }

    /**
     * Get the maxMemoryInMb value.
     *
     * @return the maxMemoryInMb value
     */
    public Long maxMemoryInMb() {
        return this.maxMemoryInMb;
    }

    /**
     * Set the maxMemoryInMb value.
     *
     * @param maxMemoryInMb the maxMemoryInMb value to set
     * @return the SiteLimits object itself.
     */
    public SiteLimits withMaxMemoryInMb(Long maxMemoryInMb) {
        this.maxMemoryInMb = maxMemoryInMb;
        return this;
    }

    /**
     * Get the maxDiskSizeInMb value.
     *
     * @return the maxDiskSizeInMb value
     */
    public Long maxDiskSizeInMb() {
        return this.maxDiskSizeInMb;
    }

    /**
     * Set the maxDiskSizeInMb value.
     *
     * @param maxDiskSizeInMb the maxDiskSizeInMb value to set
     * @return the SiteLimits object itself.
     */
    public SiteLimits withMaxDiskSizeInMb(Long maxDiskSizeInMb) {
        this.maxDiskSizeInMb = maxDiskSizeInMb;
        return this;
    }

}
