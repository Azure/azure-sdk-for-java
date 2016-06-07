/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Worker pool of a hostingEnvironment (App Service Environment).
 */
@JsonFlatten
public class WorkerPoolInner extends Resource {
    /**
     * Worker size id for referencing this worker pool.
     */
    @JsonProperty(value = "properties.workerSizeId")
    private Integer workerSizeId;

    /**
     * Shared or dedicated web app hosting. Possible values include: 'Shared',
     * 'Dedicated', 'Dynamic'.
     */
    @JsonProperty(value = "properties.computeMode")
    private ComputeModeOptions computeMode;

    /**
     * VM size of the worker pool instances.
     */
    @JsonProperty(value = "properties.workerSize")
    private String workerSize;

    /**
     * Number of instances in the worker pool.
     */
    @JsonProperty(value = "properties.workerCount")
    private Integer workerCount;

    /**
     * Names of all instances in the worker pool (read only).
     */
    @JsonProperty(value = "properties.instanceNames")
    private List<String> instanceNames;

    /**
     * The sku property.
     */
    private SkuDescription sku;

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
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withWorkerSizeId(Integer workerSizeId) {
        this.workerSizeId = workerSizeId;
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
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withComputeMode(ComputeModeOptions computeMode) {
        this.computeMode = computeMode;
        return this;
    }

    /**
     * Get the workerSize value.
     *
     * @return the workerSize value
     */
    public String workerSize() {
        return this.workerSize;
    }

    /**
     * Set the workerSize value.
     *
     * @param workerSize the workerSize value to set
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withWorkerSize(String workerSize) {
        this.workerSize = workerSize;
        return this;
    }

    /**
     * Get the workerCount value.
     *
     * @return the workerCount value
     */
    public Integer workerCount() {
        return this.workerCount;
    }

    /**
     * Set the workerCount value.
     *
     * @param workerCount the workerCount value to set
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withWorkerCount(Integer workerCount) {
        this.workerCount = workerCount;
        return this;
    }

    /**
     * Get the instanceNames value.
     *
     * @return the instanceNames value
     */
    public List<String> instanceNames() {
        return this.instanceNames;
    }

    /**
     * Set the instanceNames value.
     *
     * @param instanceNames the instanceNames value to set
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withInstanceNames(List<String> instanceNames) {
        this.instanceNames = instanceNames;
        return this;
    }

    /**
     * Get the sku value.
     *
     * @return the sku value
     */
    public SkuDescription sku() {
        return this.sku;
    }

    /**
     * Set the sku value.
     *
     * @param sku the sku value to set
     * @return the WorkerPoolInner object itself.
     */
    public WorkerPoolInner withSku(SkuDescription sku) {
        this.sku = sku;
        return this;
    }

}
