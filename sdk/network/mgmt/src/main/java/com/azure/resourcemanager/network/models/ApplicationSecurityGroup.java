// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/** Application security group. */
@Fluent
public interface ApplicationSecurityGroup
    extends GroupableResource<NetworkManager, ApplicationSecurityGroupInner>,
        Refreshable<ApplicationSecurityGroup>,
        Updatable<ApplicationSecurityGroup.Update> {
    /**
     * @return the resource GUID property of the application security group resource. It uniquely identifies a resource,
     *     even if the user changes its name or migrate the resource across subscriptions or resource groups.
     */
    String resourceGuid();

    /** @return the provisioning state of the application security group resource */
    String provisioningState();

    // Fluent interfaces for creating Application Security Groups

    /** The entirety of the application security group definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /** Grouping of application security group definition stages. */
    interface DefinitionStages {
        /** The first stage of the definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<ApplicationSecurityGroup>, Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends Appliable<ApplicationSecurityGroup>, Resource.UpdateWithTags<Update> {
    }
}
