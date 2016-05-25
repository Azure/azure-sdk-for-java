/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsCreating;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsDeleting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsListing;


/**
 * Entry point to virtual network management
 */
public interface Networks extends
    SupportsCreating<Network.DefinitionBlank>,
    SupportsListing<Network>,
    SupportsListingByGroup<Network>,
    SupportsGetting<Network>,
    SupportsGettingByGroup<Network>,
    SupportsDeleting,
    SupportsDeletingByGroup {

    /**
     * Entry point to virtual network management within a specific resource group
     */
    public interface InGroup extends
            SupportsListing<Network>,
            SupportsCreating<Network.DefinitionBlank>,
            SupportsDeleting {
    }
}
