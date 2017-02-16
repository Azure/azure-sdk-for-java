/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.cdn.implementation.ResourceUsageInner;

/**
 * Provides information about CDN resource usages.
 */
@LangDefinition
public class ResourceUsage {
    private ResourceUsageInner inner;

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
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String resourceType() {
        return this.inner.resourceType();
    }

    /**
     * Unit of the usage. e.g. Count.
     *
     * @return unit of the usage
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public String unit() {
        return this.inner.unit();
    }

    /**
     * Actual value of the resource type.
     *
     * @return value of the resource type
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public int currentValue() {
        return this.inner.currentValue();
    }

    /**
     * Quota of the resource type.
     *
     * @return quota of the resource type
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    public int limit() {
        return this.inner.limit();
    }

}
