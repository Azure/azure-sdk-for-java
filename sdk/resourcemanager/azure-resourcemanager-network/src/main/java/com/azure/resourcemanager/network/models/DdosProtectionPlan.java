// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.DdosProtectionPlanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.List;

/** DDoS protection plan. */
@Fluent
public interface DdosProtectionPlan
    extends GroupableResource<NetworkManager, DdosProtectionPlanInner>,
        Refreshable<DdosProtectionPlan>,
        Updatable<DdosProtectionPlan.Update> {
    /**
     * @return the resource GUID property of the DDoS protection plan resource. It uniquely identifies a resource, even
     *     if the user changes its name or migrate the resource across subscriptions or resource groups.
     */
    String resourceGuid();

    /** @return the provisioning state of the DDoS protection plan resource */
    String provisioningState();

    /**
     * @return the list of virtual networks associated with the DDoS protection plan resource. This list is read-only.
     */
    List<SubResource> virtualNetworks();

    // Fluent interfaces for creating DDoS protection plan

    /** The entirety of the DDoS protection plan definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /** Grouping of DDoS protection plan definition stages. */
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
        interface WithCreate extends Creatable<DdosProtectionPlan>, Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends Appliable<DdosProtectionPlan>, Resource.UpdateWithTags<Update> {
    }
}
