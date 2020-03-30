/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.models.ConnectionMonitorsInner;
import com.azure.management.resources.fluentcore.arm.collection.SupportsGettingByName;
import com.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.azure.management.resources.fluentcore.collection.SupportsDeletingByName;
import com.azure.management.resources.fluentcore.collection.SupportsListing;
import com.azure.management.resources.fluentcore.model.HasInner;

/**
 * Entry point to connection monitors management API in Azure.
 */
@Fluent
public interface ConnectionMonitors extends
        SupportsCreating<ConnectionMonitor.DefinitionStages.WithSource>,
        SupportsListing<ConnectionMonitor>,
        SupportsGettingByName<ConnectionMonitor>,
        SupportsDeletingByName,
        HasInner<ConnectionMonitorsInner> {
}
