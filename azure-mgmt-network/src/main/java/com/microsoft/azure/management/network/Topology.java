/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.TopologyInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Topology info object, associated with network watcher.
 */
@Fluent
@Beta(SinceVersion.V1_2_0)
public interface Topology extends HasParent<NetworkWatcher>,
        HasInner<TopologyInner>,
        Refreshable<Topology> {
    /**
     * @return GUID representing the id
     */
    String id();

    /**
     * @return name of resource group this topology represents
     */
    String resourceGroupName();

    /**
     * @return the datetime when the topology was initially created for the resource
     * group.
     */
    DateTime createdTime();

    /**
     * @return the datetime when the topology was last modified
     */
    DateTime lastModifiedTime();

    /**
     * @return The resources in this topology
     */
    Map<String, TopologyResource> resources();
}
