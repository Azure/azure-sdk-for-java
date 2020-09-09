// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.collection;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/**
 * Providing access to creating Azure top level resources.
 * <p>
 * (Note: this interface is not intended to be implemented by user code)
 *
 * @param <T> the initial blank definition interface
 */
public interface SupportsCreating<T> {
    /**
     * Begins a definition for a new resource.
     * <p>
     * This is the beginning of the builder pattern used to create top level resources
     * in Azure. The final method completing the definition and starting the actual resource creation
     * process in Azure is {@link Creatable#create()}.
     * <p>
     * Note that the {@link Creatable#create()} method is
     * only available at the stage of the resource definition that has the minimum set of input
     * parameters specified. If you do not see {@link Creatable#create()} among the available methods, it
     * means you have not yet specified all the required input settings. Input settings generally begin
     * with the word "with", for example: <code>.withNewResourceGroup()</code> and return the next stage
     * of the resource definition, as an interface in the "fluent interface" style.
     *
     * @param name the name of the new resource
     * @return the first stage of the new resource definition
     */
    T define(String name);
}
