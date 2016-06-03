/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import org.joda.time.Period;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a ComputeNodeOperations.Remove request.
 */
public class NodeRemoveParameter {
    /**
     * A list containing the id of the compute nodes to be removed from the
     * specified pool.
     */
    @JsonProperty(required = true)
    private List<String> nodeList;

    /**
     * The timeout for removal of compute nodes to the pool. The default value
     * is 10 minutes.
     */
    private Period resizeTimeout;

    /**
     * When compute nodes may be removed from the pool. Possible values
     * include: 'requeue', 'terminate', 'taskcompletion', 'retaineddata'.
     */
    private ComputeNodeDeallocationOption nodeDeallocationOption;

    /**
     * Get the nodeList value.
     *
     * @return the nodeList value
     */
    public List<String> nodeList() {
        return this.nodeList;
    }

    /**
     * Set the nodeList value.
     *
     * @param nodeList the nodeList value to set
     * @return the NodeRemoveParameter object itself.
     */
    public NodeRemoveParameter withNodeList(List<String> nodeList) {
        this.nodeList = nodeList;
        return this;
    }

    /**
     * Get the resizeTimeout value.
     *
     * @return the resizeTimeout value
     */
    public Period resizeTimeout() {
        return this.resizeTimeout;
    }

    /**
     * Set the resizeTimeout value.
     *
     * @param resizeTimeout the resizeTimeout value to set
     * @return the NodeRemoveParameter object itself.
     */
    public NodeRemoveParameter withResizeTimeout(Period resizeTimeout) {
        this.resizeTimeout = resizeTimeout;
        return this;
    }

    /**
     * Get the nodeDeallocationOption value.
     *
     * @return the nodeDeallocationOption value
     */
    public ComputeNodeDeallocationOption nodeDeallocationOption() {
        return this.nodeDeallocationOption;
    }

    /**
     * Set the nodeDeallocationOption value.
     *
     * @param nodeDeallocationOption the nodeDeallocationOption value to set
     * @return the NodeRemoveParameter object itself.
     */
    public NodeRemoveParameter withNodeDeallocationOption(ComputeNodeDeallocationOption nodeDeallocationOption) {
        this.nodeDeallocationOption = nodeDeallocationOption;
        return this;
    }

}
