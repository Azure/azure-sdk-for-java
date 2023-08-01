// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.trafficmanager.TrafficManager;

/** Entry point to Azure traffic manager geographic hierarchy management API in Azure. */
public interface GeographicHierarchies extends HasManager<TrafficManager> {
    /** @return the root of the Geographic Hierarchy used by the Geographic traffic routing method. */
    GeographicLocation getRoot();
}
