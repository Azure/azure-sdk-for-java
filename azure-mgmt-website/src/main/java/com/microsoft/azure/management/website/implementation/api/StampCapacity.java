/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * Class containing stamp capacity information.
 */
public class StampCapacity {
    /**
     * Name of the stamp.
     */
    private String name;

    /**
     * Available capacity (# of machines, bytes of storage etc...).
     */
    private Long availableCapacity;

    /**
     * Total capacity (# of machines, bytes of storage etc...).
     */
    private Long totalCapacity;

    /**
     * Name of the unit.
     */
    private String unit;

    /**
     * Shared/Dedicated workers. Possible values include: 'Shared',
     * 'Dedicated', 'Dynamic'.
     */
    private ComputeModeOptions computeMode;

    /**
     * Size of the machines. Possible values include: 'Default', 'Small',
     * 'Medium', 'Large'.
     */
    private WorkerSizeOptions workerSize;

    /**
     * Size Id of machines:
     * 0 - Small
     * 1 - Medium
     * 2 - Large.
     */
    private Integer workerSizeId;

    /**
     * If true it includes basic sites
     * Basic sites are not used for capacity allocation.
     */
    private Boolean excludeFromCapacityAllocation;

    /**
     * Is capacity applicable for all sites?.
     */
    private Boolean isApplicableForAllComputeModes;

    /**
     * Shared or Dedicated.
     */
    private String siteMode;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the availableCapacity value.
     *
     * @return the availableCapacity value
     */
    public Long availableCapacity() {
        return this.availableCapacity;
    }

    /**
     * Set the availableCapacity value.
     *
     * @param availableCapacity the availableCapacity value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withAvailableCapacity(Long availableCapacity) {
        this.availableCapacity = availableCapacity;
        return this;
    }

    /**
     * Get the totalCapacity value.
     *
     * @return the totalCapacity value
     */
    public Long totalCapacity() {
        return this.totalCapacity;
    }

    /**
     * Set the totalCapacity value.
     *
     * @param totalCapacity the totalCapacity value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withTotalCapacity(Long totalCapacity) {
        this.totalCapacity = totalCapacity;
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
     * @return the StampCapacity object itself.
     */
    public StampCapacity withUnit(String unit) {
        this.unit = unit;
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
     * @return the StampCapacity object itself.
     */
    public StampCapacity withComputeMode(ComputeModeOptions computeMode) {
        this.computeMode = computeMode;
        return this;
    }

    /**
     * Get the workerSize value.
     *
     * @return the workerSize value
     */
    public WorkerSizeOptions workerSize() {
        return this.workerSize;
    }

    /**
     * Set the workerSize value.
     *
     * @param workerSize the workerSize value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withWorkerSize(WorkerSizeOptions workerSize) {
        this.workerSize = workerSize;
        return this;
    }

    /**
     * Get the workerSizeId value.
     *
     * @return the workerSizeId value
     */
    public Integer workerSizeId() {
        return this.workerSizeId;
    }

    /**
     * Set the workerSizeId value.
     *
     * @param workerSizeId the workerSizeId value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withWorkerSizeId(Integer workerSizeId) {
        this.workerSizeId = workerSizeId;
        return this;
    }

    /**
     * Get the excludeFromCapacityAllocation value.
     *
     * @return the excludeFromCapacityAllocation value
     */
    public Boolean excludeFromCapacityAllocation() {
        return this.excludeFromCapacityAllocation;
    }

    /**
     * Set the excludeFromCapacityAllocation value.
     *
     * @param excludeFromCapacityAllocation the excludeFromCapacityAllocation value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withExcludeFromCapacityAllocation(Boolean excludeFromCapacityAllocation) {
        this.excludeFromCapacityAllocation = excludeFromCapacityAllocation;
        return this;
    }

    /**
     * Get the isApplicableForAllComputeModes value.
     *
     * @return the isApplicableForAllComputeModes value
     */
    public Boolean isApplicableForAllComputeModes() {
        return this.isApplicableForAllComputeModes;
    }

    /**
     * Set the isApplicableForAllComputeModes value.
     *
     * @param isApplicableForAllComputeModes the isApplicableForAllComputeModes value to set
     * @return the StampCapacity object itself.
     */
    public StampCapacity withIsApplicableForAllComputeModes(Boolean isApplicableForAllComputeModes) {
        this.isApplicableForAllComputeModes = isApplicableForAllComputeModes;
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
     * @return the StampCapacity object itself.
     */
    public StampCapacity withSiteMode(String siteMode) {
        this.siteMode = siteMode;
        return this;
    }

}
