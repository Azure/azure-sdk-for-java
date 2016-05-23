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
 * Entry point to public IP address management
 */
public interface PublicIpAddresses extends
    SupportsCreating<PublicIpAddress.DefinitionBlank>,
    SupportsListing<PublicIpAddress>,
    SupportsListingByGroup<PublicIpAddress>,
    SupportsGetting<PublicIpAddress>,
    SupportsGettingByGroup<PublicIpAddress>,
    SupportsDeleting,
    SupportsDeletingByGroup {

    /**
     * Entry point to public IP address management within a specific resource group
     */
    public interface InGroup extends
            SupportsListing<PublicIpAddress>,
            SupportsCreating<PublicIpAddress.DefinitionBlank>,
            SupportsDeleting {
    }
}
