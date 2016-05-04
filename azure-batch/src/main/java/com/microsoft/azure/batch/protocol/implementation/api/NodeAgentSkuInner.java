/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;

/**
 * Information about supported node agent SKU.
 */
public class NodeAgentSkuInner {
    /**
     * Gets or sets the node agent SKU id.
     */
    private String id;

    /**
     * Gets the list of images verified to be compatible with the node agent
     * SKU. This collection is not exhaustive; the node agent SKU may be
     * compatible with other images.
     */
    private List<ImageReference> verifiedImageReferences;

    /**
     * Gets or sets the type of OS that the node Agent SKU is targeted
     * against. Possible values include: 'linux', 'windows', 'unmapped'.
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
     * @return the NodeAgentSkuInner object itself.
     */
    public NodeAgentSkuInner setId(String id) {
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
     * @return the NodeAgentSkuInner object itself.
     */
    public NodeAgentSkuInner setVerifiedImageReferences(List<ImageReference> verifiedImageReferences) {
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
     * @return the NodeAgentSkuInner object itself.
     */
    public NodeAgentSkuInner setOsType(OSType osType) {
        this.osType = osType;
        return this;
    }

}
