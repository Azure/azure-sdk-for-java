/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.TopologyInner;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import org.joda.time.DateTime;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Topology info object, associated with network watcher.
 */
@Fluent
@Beta
public interface Topology extends HasInner<TopologyInner> {
    /**
     * @return GUID representing the id
     */
    String id();

    /**
     * @return the datetime when the topology was initially created for the resource
     * group.
     */
    DateTime createdDateTime();

    /**
     * @return the datetime when the topology was last modified
     */
    DateTime lastModified();

    /**
     * @return The resources in this topology
     */
    Map<String, TopologyResource> resources();
}
