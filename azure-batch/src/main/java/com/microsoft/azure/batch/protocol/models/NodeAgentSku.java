/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;

/**
 * A node agent SKU supported by the Batch service. The Batch node agent is a
 * program that runs on each node in the pool, and provides the
 * command-and-control interface between the node and the Batch service.
 * There are different implementations of the node agent, known as SKUs, for
 * different operating systems.
 */
public class NodeAgentSku {
    /**
     * The node agent SKU id.
     */
    private String id;

    /**
     * The list of images verified to be compatible with this node agent SKU.
     * This collection is not exhaustive (the node agent may be compatible
     * with other images).
     */
    private List<ImageReference> verifiedImageReferences;

    /**
     * The type of operating system compatible with the node agent SKU.
     * Possible values include: 'linux', 'windows', 'unmapped'.
     */
    private OSType osType;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the NodeAgentSku object itself.
     */
    public NodeAgentSku withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the verifiedImageReferences value.
     *
     * @return the verifiedImageReferences value
     */
    public List<ImageReference> verifiedImageReferences() {
        return this.verifiedImageReferences;
    }

    /**
     * Set the verifiedImageReferences value.
     *
     * @param verifiedImageReferences the verifiedImageReferences value to set
     * @return the NodeAgentSku object itself.
     */
    public NodeAgentSku withVerifiedImageReferences(List<ImageReference> verifiedImageReferences) {
        this.verifiedImageReferences = verifiedImageReferences;
        return this;
    }

    /**
     * Get the osType value.
     *
     * @return the osType value
     */
    public OSType osType() {
        return this.osType;
    }

    /**
     * Set the osType value.
     *
     * @param osType the osType value to set
     * @return the NodeAgentSku object itself.
     */
    public NodeAgentSku withOsType(OSType osType) {
        this.osType = osType;
        return this;
    }

}
