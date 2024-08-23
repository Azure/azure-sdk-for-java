// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VirtualWanInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

/**
 * Entry point for Virtual Network management API in Azure.
 */
@Fluent
public interface VirtualWan
    extends GroupableResource<NetworkManager, VirtualWanInner>, Refreshable<VirtualWan>, Updatable<VirtualWan.Update> {

    /** @return returns true if current virtual wan is a security vpn. */
    Boolean disabledVpnEncryption();

    /** @return the virtualWanType property of Virtual Wan. */
    String virtualWanType();

    /** @return returns true if allow Branch To Branch Traffic. */
    Boolean allowBranchToBranchTraffic();


    /** The entirety of a VirtualWan definition. */
    interface Definition
        extends DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /** Grouping of VirtualWan definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of a VirtualWan definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the VirtualWan definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * The stage of a VirtualWan definition which contains all the minimum required inputs for the resource to be
         * created (via {@link VirtualWan.DefinitionStages.WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<VirtualWan>, DefinitionWithTags<WithCreate> {

            /**
             * Enables encryption of VPN
             *
             * @return the next stage of the definition
             */
            WithCreate enableVpnEncryption();

            /**
             * Set the type of a virtual wan.
             *
             * @param virtualWanType the type of virtualWan value to set. Basic/Standard
             * @return the next stage of the definition
             */
            WithCreate withVirtualWanType(String virtualWanType);

            /**
             * Set witch allow branch to branch traffic
             *
             * @param allowBranchToBranchTraffic true/false
             * @return the next stage of the definition
             */
            WithCreate withAllowBranchToBranchTraffic(Boolean allowBranchToBranchTraffic);
        }
    }

    /**
     * The template for a VirtualWan update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link VirtualWan.Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update
        extends Appliable<VirtualWan>,
        Resource.UpdateWithTags<VirtualWan.Update> {

        /**
         * Disabled encryption of VPN
         *
         * @return the next stage of the vpn update
         */
        Update disableVpnEncryption();

        /**
         * Set witch allow branch to branch traffic
         *
         * @param allowBranchToBranchTraffic true/false
         * @return the next stage of the vpn update
         */
        Update withAllowBranchToBranchTraffic(Boolean allowBranchToBranchTraffic);
    }
}
