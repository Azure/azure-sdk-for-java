// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.model;

/**
 * Base class for resource that can be updated.
 *
 * @param <T> the fluent type of the resource
 */
public interface Updatable<T> {
    /**
     * Begins an update for a new resource.
     * <p>
     * This is the beginning of the builder pattern used to update top level resources
     * in Azure. The final method completing the definition and starting the actual resource creation
     * process in Azure is {@link Appliable#apply()}.
     *
     * @return the stage of new resource update
     */
    T update();
}
