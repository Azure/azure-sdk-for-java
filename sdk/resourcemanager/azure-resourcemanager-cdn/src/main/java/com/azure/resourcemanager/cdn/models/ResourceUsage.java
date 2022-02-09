// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.models;

import com.azure.resourcemanager.cdn.fluent.models.ResourceUsageInner;

/**
 * Provides information about CDN resource usages.
 */
public class ResourceUsage {
    private final ResourceUsageInner inner;

    /**
     * Construct ResourceUsage object from server response object.
     *
     * @param inner server response object containing resource usages.
     */
    public ResourceUsage(ResourceUsageInner inner) {
        this.inner = inner;
    }

    /**
     * Resource type of the usages.
     *
     * @return type of the usages
     */
    public String resourceType() {
        return this.inner.resourceType();
    }

    /**
     * Unit of the usage. e.g. Count.
     *
     * @return unit of the usage
     */
    public String unit() {
        return this.inner.unit();
    }

    /**
     * Actual value of the resource type.
     *
     * @return value of the resource type
     */
    public int currentValue() {
        return this.inner.currentValue();
    }

    /**
     * Quota of the resource type.
     *
     * @return quota of the resource type
     */
    public int limit() {
        return this.inner.limit();
    }

}
