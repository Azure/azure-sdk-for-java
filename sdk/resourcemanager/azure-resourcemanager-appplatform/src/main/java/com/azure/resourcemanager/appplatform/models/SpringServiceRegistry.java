// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.fluent.models.ServiceRegistryResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

import java.util.List;

/** An immutable client-side representation of an Azure Spring Service Registry. */
@Fluent
public interface SpringServiceRegistry
    extends ExternalChildResource<SpringServiceRegistry, SpringService>, HasInnerModel<ServiceRegistryResourceInner> {

    /**
     * Gets CPU for the Service Registry.
     *
     * @return CPU for the Service Registry
     */
    Double cpu();

    /**
     * Gets memory for the Service Registry.
     *
     * @return memory for the Service Registry
     */
    Double memory();

    /**
     * Gets apps that have bindings to this Service Registry.
     *
     * @return apps that have bindings to this Service Registry
     */
    List<SpringApp> getAppBindings();
}
