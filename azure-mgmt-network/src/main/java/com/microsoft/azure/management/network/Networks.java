/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingById;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;


/**
 * Entry point to virtual network management API in Azure.
 */
public interface Networks extends
    SupportsCreating<Network.DefinitionStages.Blank>,
    SupportsListing<Network>,
    SupportsListingByGroup<Network>,
    SupportsGettingByGroup<Network>,
    SupportsGettingById<Network>,
    SupportsDeleting,
    SupportsDeletingByGroup {
}
