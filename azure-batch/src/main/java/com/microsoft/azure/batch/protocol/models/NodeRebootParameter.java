/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;


/**
 * Parameters for a ComputeNodeOperations.Reboot request.
 */
public class NodeRebootParameter {
    /**
     * When to reboot the compute node and what to do with currently running
     * tasks. The default value is requeue. Possible values include:
     * 'requeue', 'terminate', 'taskcompletion', 'retaineddata'.
     */
    private ComputeNodeRebootOption nodeRebootOption;

    /**
     * Get the nodeRebootOption value.
     *
     * @return the nodeRebootOption value
     */
    public ComputeNodeRebootOption nodeRebootOption() {
        return this.nodeRebootOption;
    }

    /**
     * Set the nodeRebootOption value.
     *
     * @param nodeRebootOption the nodeRebootOption value to set
     * @return the NodeRebootParameter object itself.
     */
    public NodeRebootParameter withNodeRebootOption(ComputeNodeRebootOption nodeRebootOption) {
        this.nodeRebootOption = nodeRebootOption;
        return this;
    }

}
