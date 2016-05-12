/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.PublicIPAddressInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Provisionable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

public interface PublicIpAddress extends
        GroupableResource,
        Refreshable<PublicIpAddress>,
        Wrapper<PublicIPAddressInner> {

    /***********************************************************
     * Getters
     ***********************************************************/


    /**************************************************************
     * Fluent interfaces for provisioning
     **************************************************************/

    interface DefinitionBlank extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    interface DefinitionWithGroup extends GroupableResource.DefinitionWithGroup<DefinitionProvisionable> {
    }

    interface DefinitionProvisionable extends Provisionable<PublicIpAddress> {
    }
}

