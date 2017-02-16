/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.cdn.implementation.EdgeNodeInner;

import java.util.List;
import java.util.Map;

/**
 * Provides information about edge node of CDN service.
 */
@LangDefinition
public class EdgeNode {
    private EdgeNodeInner inner;

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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String id() {
        return this.inner.id();
    }


    /**
     * Edge node resource name.
     *
     * @return the name of the resource
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String name() {
        return this.inner.name();
    }

    /**
     * Edge node type string.
     *
     * @return the type of the resource
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String type() {
        return this.inner.type();
    }

    /**
     * Edge node location string.
     *
     * @return the resource location string
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String location() {
        return this.inner.location();
    }

    /**
     * Edge node tags.
     *
     * @return the tags of edge node
     */
    public Map<String, String> getTags() {
        return this.inner.getTags();
    }

    /**
     * Get the ipAddressGroups value.
     *
     * @return the ipAddressGroups value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public List<IpAddressGroup> ipAddressGroups() {
        return this.inner.ipAddressGroups();
    }
}
