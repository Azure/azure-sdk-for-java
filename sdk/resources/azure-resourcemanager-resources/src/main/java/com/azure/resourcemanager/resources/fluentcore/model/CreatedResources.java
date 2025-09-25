// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

import java.util.Map;

/**
 * Represents the results of batch of create operations.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 * @param <ResourceT> the type of the resource in this batch
 */
public interface CreatedResources<ResourceT extends Indexable> extends Map<String, ResourceT> {
    /**
     * Gets a created resource with the given key.
     *
     * @param key the key of the resource
     * @return the created resource
     */
    Indexable createdRelatedResource(String key);
}
