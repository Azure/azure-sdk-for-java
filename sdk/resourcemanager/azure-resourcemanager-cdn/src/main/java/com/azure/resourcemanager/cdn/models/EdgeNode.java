// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.resourcemanager.cdn.fluent.models.EdgeNodeInner;

import java.util.List;

/**
 * Provides information about edge node of CDN service.
 */
public class EdgeNode {
    private final EdgeNodeInner inner;

    /**
     * Construct edge node object from server response object.
     *
     * @param inner server response object containing edge nodes.
     */
    public EdgeNode(EdgeNodeInner inner) {
        this.inner = inner;
    }

    /**
     * Edge node resource ID string.
     *
     * @return the resource ID string
     */
    public String id() {
        return this.inner.id();
    }


    /**
     * Edge node resource name.
     *
     * @return the name of the resource
     */
    public String name() {
        return this.inner.name();
    }

    /**
     * Edge node type string.
     *
     * @return the type of the resource
     */
    public String type() {
        return this.inner.type();
    }

    /**
     * Get the ipAddressGroups value.
     *
     * @return the ipAddressGroups value
     */
    public List<IpAddressGroup> ipAddressGroups() {
        return this.inner.ipAddressGroups();
    }
}
