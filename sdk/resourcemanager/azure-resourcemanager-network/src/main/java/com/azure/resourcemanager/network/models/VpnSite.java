// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VpnSiteInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
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
    extends GroupableResource<NetworkManager, VpnSiteInner>,
    Refreshable<VpnSite>,
    Updatable<VpnSite.Update>,
    UpdatableWithTags<VpnSite> {

    /** @return returns true if current VPN site is a security site. */
    boolean isSecuritySiteEnabled();

    /** @return the AddressPrefixes value of VPN site. */
    List<String> addressPrefixes();

    /** @return the virtualWan property of VPN site. */
    VirtualWan virtualWan();

    /** @return the vpnSiteLinks property of VPN site. */
    List<VpnSiteLink> vpnSiteLinks();

    /** @return the o365Policy property of VPN site. */
    O365PolicyProperties o365Policy();

    /** @return the device property of VPN site. */
    DeviceProperties device();

    /** The entirety of the VPN site definition. */
    interface Definition
        extends DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate {
    }

    /** Grouping of VPN site definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of a VPN site definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the VPN site definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAddressSpace> {
        }

        /**
         * The stage of the address space definition allowing to specify the VPN site configuration.
         */
        interface WithAddressSpace {
            /**
             * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
             *
             * @param cidr the addressSpace value to set.
             * @return the next stage of the definition
             */
            WithVpnSiteLinks withAddressSpace(String cidr);

            /**
             * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
             *
             * @param cidrList the addressSpace list to set.
             * @return the next stage of the definition
             */
            WithVpnSiteLinks withAddressSpace(List<String> cidrList);
        }

        /**
         * The stage of the vpn site links definition allowing to specify the VPN site configuration.
         */
        interface WithVpnSiteLinks {
            /**
             * Starts the definition of a new vpn site link.
             *
             * @param name the name for the new vpn site link.
             * @return the first stage of the vpn site link definition
             */
            VpnSiteLink.DefinitionStages.Blank<WithCreate> defineVpnSiteLink(String name);
        }

        /**
         * The stage of the virtual wan definition allowing to specify the VPN site configuration.
         */
        interface WithVirtualWan {
            /**
             * Set the virtualWan property: The id of VirtualWAN to which the VPN site belongs.
             *
             * @param virtualWanId the id of virtualWan to set.
             * @return the next stage of the definition
             */
            WithCreate withVirtualWan(String virtualWanId);

            /**
             * Set the virtualWan property: The instance of VirtualWAN to which the VPN site belongs.
             *
             * @param virtualWan the instance of virtualWan to set.
             * @return the next stage of the definition
             */
            WithCreate withVirtualWan(VirtualWan virtualWan);

            /**
             * Set the virtualWan property: The creatable of VirtualWAN to which the VPN site belongs.
             *
             * @param creatable the creatable of virtualWan to set.
             * @return the next stage of the definition
             */
            WithCreate withVirtualWan(Creatable<VirtualWan> creatable);

        }

        /**
         * The stage of the site security definition allowing to specify the VPN site configuration.
         */
        interface WithSecuritySite {
            /**
             * Enables security vpn site.
             *
             * @return the next stage of the definition
             */
            WithCreate enableSecuritySite();
        }

        /**
         * The stage of the O365Policy definition allowing to specify the VPN site configuration.
         */
        interface WithO365Policy {
            /**
             * Starts the definition of a new O365Policy.
             *
             * @return the next stage of the definition
             */
            O365Policy.DefinitionStages.Blank<WithCreate> defineO365Policy();
        }

        /**
         * The stage of the vpn device definition allowing to specify the VPN site configuration.
         */
        interface WithDevice {
            /**
             * Set the deviceProperties property: VPN device properties.
             *
             * @return the next stage of the definition
             */
            Device.DefinitionStages.Blank<WithCreate> defineDevice();
        }

        /** The entirety of a vpn site update as part of a VPN site update. */
        interface WithCreate
            extends Creatable<VpnSite>,
            Resource.DefinitionWithTags<WithCreate>,
            DefinitionStages.WithAddressSpace,
            DefinitionStages.WithVirtualWan,
            DefinitionStages.WithVpnSiteLinks,
            DefinitionStages.WithSecuritySite,
            DefinitionStages.WithO365Policy,
            DefinitionStages.WithDevice {

            /**
             * Begins creating the VPN site resource.
             *
             * @return the accepted create operation
             */
            Accepted<VpnSite> beginCreate();
        }
    }

    /** Grouping of VPN site update stages. */
    interface UpdateStages {
        /**
         * The stage of the virtual wan definition allowing to specify the VPN site configuration.
         */
        interface WithVirtualWan {
            /**
             * Set the virtualWan property: The id of VirtualWAN to which the VPN site belongs.
             *
             * @param virtualWanId the id of virtualWan to set.
             * @return the next stage of the definition
             */
            Update withVirtualWan(String virtualWanId);

            /**
             * Set the virtualWan property: The instance of VirtualWAN to which the VPN site belongs.
             *
             * @param virtualWan the instance of virtualWan to set.
             * @return the next stage of the definition
             */
            Update withVirtualWan(VirtualWan virtualWan);

            /**
             * Set the virtualWan property: The creatable of VirtualWAN to which the VPN site belongs.
             *
             * @param creatable the creatable of virtualWan to set.
             * @return the next stage of the definition
             */
            Update withVirtualWan(Creatable<VirtualWan> creatable);
        }

        /**
         * The stage of the vpn site links definition allowing to specify the VPN site configuration.
         */
        interface WithVpnSiteLinks {
            /**
             * Begins the definition of a new vpn site link to be added to this vpn site.
             *
             * @param name the name of the new vpn site link
             * @return the first stage of the new vpn site link definition
             */
            VpnSiteLink.UpdateDefinitionStages.Blank<Update> defineVpnSiteLink(String name);

            /**
             * Begins the description of an update of an existing vpn site link of this vpn site.
             *
             * @param name the name of an existing vpn site link
             * @return the first stage of the vpn site link update description
             */
            VpnSiteLink.Update updateVpnSiteLink(String name);
        }

        /**
         * The stage of the address space definition allowing to specify the VPN site configuration.
         */
        interface WithAddressSpace {
            /**
             * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
             *
             * @param cidr the addressSpace value to set.
             * @return the next stage of the definition
             */
            Update withAddressSpace(String cidr);

            /**
             * Set the addressSpace property: The AddressSpace that contains an array of IP address ranges.
             *
             * @param cidrList the addressSpace list to set.
             * @return the next stage of the definition
             */
            Update withAddressSpace(List<String> cidrList);
        }

        /**
         * The stage of the O365Policy definition allowing to specify the VPN site configuration.
         */
        interface WithO365Policy {
            /**
             * Set the O365Policy: VPN O365Policy properties.
             *
             * @return the next stage of the update
             */
            O365Policy.Update updateO365Policy();
        }

        /**
         * The stage of the vpn device definition allowing to specify the VPN site configuration.
         */
        interface WithDevice {
            /**
             * Set the deviceProperties property: VPN device properties.
             *
             * @return the next stage of the update
             */
            Device.Update updateDevice();
        }
    }

    /** The template for a VPN site update operation, containing all the settings that can be modified. */
    interface Update
        extends Appliable<VpnSite>,
        Resource.UpdateWithTags<Update>,
        UpdateStages.WithVirtualWan,
        UpdateStages.WithVpnSiteLinks,
        UpdateStages.WithAddressSpace,
        UpdateStages.WithO365Policy,
        UpdateStages.WithDevice {

        /**
         * Executes the update request.
         *
         * @return the updated resource.
         */
        VpnSite apply();
    }
}
