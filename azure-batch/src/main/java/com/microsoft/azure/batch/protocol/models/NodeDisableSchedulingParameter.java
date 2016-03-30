/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Parameters for a ComputeNodeOperations.DisableScheduling request.
 */
public class NodeDisableSchedulingParameter {
    /**
     * Gets or sets what to do with currently running tasks when disable task
     * scheduling on the compute node. The default value is requeue. Possible
     * values include: 'requeue', 'terminate', 'taskcompletion'.
     */
    private DisableComputeNodeSchedulingOption nodeDisableSchedulingOption;

    /**
     * Get the nodeDisableSchedulingOption value.
     *
     * @return the nodeDisableSchedulingOption value
     */
    public DisableComputeNodeSchedulingOption getNodeDisableSchedulingOption() {
        return this.nodeDisableSchedulingOption;
    }

    /**
     * Set the nodeDisableSchedulingOption value.
     *
     * @param nodeDisableSchedulingOption the nodeDisableSchedulingOption value to set
     */
    public void setNodeDisableSchedulingOption(DisableComputeNodeSchedulingOption nodeDisableSchedulingOption) {
        this.nodeDisableSchedulingOption = nodeDisableSchedulingOption;
    }

}
