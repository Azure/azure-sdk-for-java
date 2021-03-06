/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.streamanalytics.v2020_03_01_preview;

import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The properties associated with a Stream Analytics cluster.
 */
public class ClusterProperties {
    /**
     * The date this cluster was created.
     */
    @JsonProperty(value = "createdDate", access = JsonProperty.Access.WRITE_ONLY)
    private DateTime createdDate;

    /**
     * Unique identifier for the cluster.
     */
    @JsonProperty(value = "clusterId", access = JsonProperty.Access.WRITE_ONLY)
    private String clusterId;

    /**
     * Possible values include: 'Succeeded', 'Failed', 'Canceled',
     * 'InProgress'.
     */
    @JsonProperty(value = "provisioningState")
    private ClusterProvisioningState provisioningState;

    /**
     * Represents the number of streaming units currently being used on the
     * cluster.
     */
    @JsonProperty(value = "capacityAllocated", access = JsonProperty.Access.WRITE_ONLY)
    private Integer capacityAllocated;

    /**
     * Represents the sum of the SUs of all streaming jobs associated with the
     * cluster. If all of the jobs were running, this would be the capacity
     * allocated.
     */
    @JsonProperty(value = "capacityAssigned", access = JsonProperty.Access.WRITE_ONLY)
    private Integer capacityAssigned;

    /**
     * Get the date this cluster was created.
     *
     * @return the createdDate value
     */
    public DateTime createdDate() {
        return this.createdDate;
    }

    /**
     * Get unique identifier for the cluster.
     *
     * @return the clusterId value
     */
    public String clusterId() {
        return this.clusterId;
    }

    /**
     * Get possible values include: 'Succeeded', 'Failed', 'Canceled', 'InProgress'.
     *
     * @return the provisioningState value
     */
    public ClusterProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set possible values include: 'Succeeded', 'Failed', 'Canceled', 'InProgress'.
     *
     * @param provisioningState the provisioningState value to set
     * @return the ClusterProperties object itself.
     */
    public ClusterProperties withProvisioningState(ClusterProvisioningState provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get represents the number of streaming units currently being used on the cluster.
     *
     * @return the capacityAllocated value
     */
    public Integer capacityAllocated() {
        return this.capacityAllocated;
    }

    /**
     * Get represents the sum of the SUs of all streaming jobs associated with the cluster. If all of the jobs were running, this would be the capacity allocated.
     *
     * @return the capacityAssigned value
     */
    public Integer capacityAssigned() {
        return this.capacityAssigned;
    }

}
