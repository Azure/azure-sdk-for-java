/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.trafficmanager;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.trafficmanager.implementation.GeographicHierarchiesInner;
import com.microsoft.azure.management.trafficmanager.implementation.TrafficManager;

/**
 * Entry point to Azure traffic manager geographic hierarchy management API in Azure.
 */
@Beta(Beta.SinceVersion.V1_2_0)
@Fluent
public interface GeographicHierarchies extends
        HasManager<TrafficManager>,
        HasInner<GeographicHierarchiesInner> {
    /**
     * @return the root of the Geographic Hierarchy used by the Geographic traffic routing method.
     */
    @Beta(Beta.SinceVersion.V1_2_0)
    GeographicLocation getRoot();
}
