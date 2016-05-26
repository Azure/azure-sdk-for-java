/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsDeletingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.ResourcesInGroup;
import com.microsoft.azure.management.resources.fluentcore.collection.SupportsGetting;


/**
 * Entry point to public IP address management
 */
public interface PublicIpAddresses extends
    ResourcesInGroup<PublicIpAddress, PublicIpAddress.DefinitionBlank>,
    SupportsListingByGroup<PublicIpAddress>,
    SupportsGetting<PublicIpAddress>,
    SupportsGettingByGroup<PublicIpAddress>,
    SupportsDeletingByGroup {

    /**
     * Entry point to public IP address management within a specific resource group
     */
    public interface InGroup extends 
        ResourcesInGroup<PublicIpAddress, PublicIpAddress.DefinitionAfterGroup>{
    }
}
