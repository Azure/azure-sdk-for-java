// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a resource's usage and quota.
 */
@Fluent
public final class ResourceCounter {
    /*
     * The resource usage amount.
     */
    @JsonProperty(value = "usage", required = true)
    private long usage;

    /*
     * The resource amount quota.
     */
    @JsonProperty(value = "quota")
    private Long quota;

    /**
     * Constructor of {@link ResourceCounter}.
     *
     * @param usage The resource usage amount.
     */
    @JsonCreator
    public ResourceCounter(@JsonProperty(value = "usage", required = true) long usage) {
        this.usage = usage;
    }

    /**
     * Get the usage property: The resource usage amount.
     *
     * @return the usage value.
     */
    public long getUsage() {
        return this.usage;
    }

    /**
     * Get the quota property: The resource amount quota.
     *
     * @return the quota value.
     */
    public Long getQuota() {
        return this.quota;
    }

    /**
     * Set the quota property: The resource amount quota.
     *
     * @param quota the quota value to set.
     * @return the ResourceCounter object itself.
     */
    public ResourceCounter setQuota(Long quota) {
        this.quota = quota;
        return this;
    }
}
