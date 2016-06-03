/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * A locality hint that can be used by the Batch service to select a compute
 * node on which to start a task.
 */
public class AffinityInformation {
    /**
     * An opaque string representing the location of a compute node or a task
     * that has run previously. You can pass the AffinityId of a compute node
     * or task to indicate that this task needs to be placed close to the
     * node or task.
     */
    private String affinityId;

    /**
     * Get the affinityId value.
     *
     * @return the affinityId value
     */
    public String affinityId() {
        return this.affinityId;
    }

    /**
     * Set the affinityId value.
     *
     * @param affinityId the affinityId value to set
     * @return the AffinityInformation object itself.
     */
    public AffinityInformation withAffinityId(String affinityId) {
        this.affinityId = affinityId;
        return this;
    }

}
