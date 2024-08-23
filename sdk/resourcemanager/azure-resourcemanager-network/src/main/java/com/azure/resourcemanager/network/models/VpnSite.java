// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VpnSiteInner;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;

/**
 * Entry point for Virtual Network management API in Azure.
 */
@Fluent
public interface VpnSite
    extends GroupableResource<NetworkManager, VpnSiteInner>, Refreshable<VpnSite>, Updatable<VpnSite.Update> {

    /** @return returns true if current VPN site is a security site. */
    boolean isSecuritySite();

    /** @return the AddressPrefixes value of VPN site. */
    List<String> addressPrefixes();

    /** @return the virtualWan property of VPN site. */
    VirtualWan virtualWan();

    /** @return the vpnSiteLinks property of VPN site. */
    List<VpnSiteLinkInner> vpnSiteLinks();

    /** @return the o365Policy property of VPN site. */
    O365PolicyProperties o365Policy();

    /** @return the device property of VPN site. */
    DeviceProperties device();

    /** The entirety of a VPN definition. */
    interface Definition
        extends DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /** Grouping of VPN definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of a VPN definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the VPN definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithCreate> {
        }

        /**
         * The stage of a route VPN definition which contains all the minimum required inputs for the resource to be
         * created (via {@link VpnSite.DefinitionStages.WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<VpnSite>, DefinitionWithTags<WithCreate> {

            /**
             * Set the virtualWan property: The id of VirtualWAN to which the vpn belongs.
             *
             * @param subResourceId the id of virtualWan value to set.
             * @return the next stage of the definition
             */
            WithCreate withVirtualWan(String subResourceId);

            /**
             * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
             *
             * @param cidr the addressSpace value to set.
             * @return the next stage of the definition
             */
            WithCreate withAddressSpace(String cidr);

            /**
             * Set the isSecuritySite property: IsSecuritySite flag.
             *
             * @param isSecuritySite the isSecuritySite value to set.
             * @return the next stage of the definition
             */
            WithCreate withIsSecuritySite(Boolean isSecuritySite);

            /**
             * Set the vpnSiteLinks property: List of all vpn site links.
             *
             * @param vpnSiteLinks the vpnSiteLinks value to set.
             * @return the next stage of the definition
             */
            WithCreate withVpnSiteLinks(List<VpnSiteLinkInner> vpnSiteLinks);

            /**
             * Set the o365Policy property: Office365 Policy.
             *
             * @param o365Policy the o365Policy value to set.
             * @return the next stage of the definition
             */
            WithCreate withO365Policy(O365PolicyProperties o365Policy);

            /**
             * Set the deviceProperties property: VPN device properties.
             *
             * @param deviceProperties the deviceProperties value to set.
             * @return the next stage of the definition
             */
            WithCreate withDevice(DeviceProperties deviceProperties);
        }
    }

    /**
     * The template for a VPN update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link VpnSite.Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update
        extends Appliable<VpnSite>,
            Resource.UpdateWithTags<VpnSite.Update> {

        /**
         * Set the virtualWan property: The id of VirtualWAN to which the vpnSite belongs.
         *
         * @param subResourceId the id of virtualWan value to set.
         * @return the next stage of the vpn update
         */
        Update withVirtualWan(String subResourceId);

        /**
         * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
         *
         * @param cidr the addressSpace value to set.
         * @return the next stage of the vpn update
         */
        Update withAddressSpace(String cidr);

        /**
         * Set the vpnSiteLinks property: List of all vpn site links.
         *
         * @param vpnSiteLinks the vpnSiteLinks value to set.
         * @return the next stage of the vpn update
         */
        Update withVpnSiteLinks(List<VpnSiteLinkInner> vpnSiteLinks);

        /**
         * Set the o365Policy property: Office365 Policy.
         *
         * @param o365Policy the o365Policy value to set.
         * @return the next stage of the vpn update
         */
        Update withO365Policy(O365PolicyProperties o365Policy);

        /**
         * Set the deviceProperties property: VPN device properties.
         *
         * @param deviceProperties the deviceProperties value to set.
         * @return the next stage of the vpn update
         */
        Update withDevice(DeviceProperties deviceProperties);
    }
}
