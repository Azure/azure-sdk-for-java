package com.microsoft.azure.management.resources.fluentcore.model;


import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;

import java.util.List;

/**
 * Represents result of batch of create operations.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 * @param <ResourceT> the type of the resource in this batch.
 */
public interface BatchCreateOperationResult<ResourceT extends Resource> {
    /**
     * @return the collection of created resources in this batch.
     */
    List<ResourceT> resources();
}
