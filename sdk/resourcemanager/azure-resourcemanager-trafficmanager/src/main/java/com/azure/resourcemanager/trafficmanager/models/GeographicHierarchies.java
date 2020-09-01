/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.trafficmanager.implementation.GeographicHierarchiesInner;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to Azure traffic manager geographic hierarchy management API in Azure.
 */
@Fluent
public interface GeographicHierarchies extends
        HasManager<TrafficManager>,
        HasInner<GeographicHierarchiesInner> {
    /**
     * @return the root of the Geographic Hierarchy used by the Geographic traffic routing method.
     */
    GeographicLocation getRoot();
}
