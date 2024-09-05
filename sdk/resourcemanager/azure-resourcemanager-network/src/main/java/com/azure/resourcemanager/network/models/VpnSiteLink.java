// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/**
 * Entry point for Virtual Network management API in Azure.
 */
@Fluent
public interface VpnSiteLink extends HasInnerModel<VpnSiteLinkInner>, ChildResource<VpnSite> {

    /** @return the name of VPN site link. */
    String name();

    /** @return the ip address of VPN site link. */
    String ipAddress();

    /** @return the fqdn of VPN site link. */
    String fqdn();

    /** @return the link provider properties of VPN site link. */
    VpnLinkProviderProperties linkProperties();

    /** @return the bgp settings of VPN site link. */
    VpnLinkBgpSettings bgpProperties();

    /**
     * The entirety of a VPN site link definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithIpAddress<ParentT>,
        DefinitionStages.WithLinkProperties<ParentT>,
        DefinitionStages.WithBgpProperties<ParentT> {
    }

    /** Grouping of VPN site link definition stages applicable as part of a vpn site creation. */
    interface DefinitionStages {
        /**
         * The first stage of a VPN site link definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithIpAddress<ParentT> {
        }

        /**
         * The stage of the VPN site link definition allowing the ip address or fqdn to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithIpAddress<ParentT> {
            /**
             * Specifies the ip address to which this VPN site link applies.
             *
             * @param ipAddress IP addresses
             * @return the next stage of the definition
             */
            WithLinkProperties<ParentT> withIpAddress(String ipAddress);

            /**
             * Specifies the fqdn to which this VPN site link applies.
             *
             * @param fqdn fqdn
             * @return the next stage of the definition
             */
            WithLinkProperties<ParentT> withFqdn(String fqdn);
        }

        /**
         * The stage of the VPN site link definition allowing the link properties to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithLinkProperties<ParentT> {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param providerName the name of vpn site provider
             * @param speedInMbps the value of vpn site link speed
             * @return the next stage of the definition
             */
            WithBgpProperties<ParentT> withLinkProperties(String providerName, Integer speedInMbps);
        }

        /**
         * The stage of the VPN site link definition allowing the bgp properties to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBgpProperties<ParentT> {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param bgpPeeringAddress the ip address of Bgp setting
             * @param asn the value of Bgp asn
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBgpProperties(String bgpPeeringAddress,  Long asn);
        }

        /**
         * The final stage of the VPN site link definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the security rule definition can be
         * attached to the parent VPN site definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of a VPN site link definition as part of a vpn site update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithIpAddress<ParentT>,
        UpdateDefinitionStages.WithLinkProperties<ParentT>,
        UpdateDefinitionStages.WithBgpProperties<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of VPN site link definition stages applicable as part of a vpn site update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a VPN site link description as part of an update of a vpn site.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithIpAddress<ParentT> {
        }

        /**
         * The stage of the VPN site link definition allowing the ip address or fqdn to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithIpAddress<ParentT> {
            /**
             * Specifies the ip address to which this VPN site link applies.
             *
             * @param ipAddress IP addresses
             * @return the next stage of the definition
             */
            WithLinkProperties<ParentT> withIpAddress(String ipAddress);

            /**
             * Specifies the fqdn to which this VPN site link applies.
             *
             * @param fqdn fqdn
             * @return the next stage of the definition
             */
            WithLinkProperties<ParentT> withFqdn(String fqdn);
        }

        /**
         * The stage of the VPN site link definition allowing the link properties to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithLinkProperties<ParentT> {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param providerName the name of vpn site provider
             * @param speedInMbps the value of vpn site link speed
             * @return the next stage of the definition
             */
            WithBgpProperties<ParentT> withLinkProperties(String providerName, Integer speedInMbps);
        }

        /**
         * The stage of the VPN site link definition allowing the bgp properties to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBgpProperties<ParentT> {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param bgpPeeringAddress the ip address of Bgp setting
             * @param asn the value of Bgp asn
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withBgpProperties(String bgpPeeringAddress, Long asn);
        }

        /**
         * The final stage of the VPN site link definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the security rule definition can be
         * attached to the parent VPN site definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of a VPN site link update as part of a VPN site update. */
    interface Update
        extends UpdateStages.WithIpAddress,
        UpdateStages.WithLinkProperties,
        UpdateStages.WithBgpProperties,
        Settable<VpnSite.Update> {
    }

    /** Grouping of VPN site link update stages. */
    interface UpdateStages {
        /** The stage of the VPN site link description allowing ip address or fqdn to be specified. */
        interface WithIpAddress {

            /**
             * Specifies the ip address to which this vpn site link applies.
             *
             * @param ipAddress IP addresses
             * @return the next stage of the definition
             */
            Update withIpAddress(String ipAddress);

            /**
             * Specifies the fqdn to which this vpn site link applies.
             *
             * @param fqdn fqdn
             * @return the next stage of the definition
             */
            Update withFqdn(String fqdn);
        }

        /** The stage of the VPN site link description allowing link properties to be specified. */
        interface WithLinkProperties {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param providerName the name of vpn site provider
             * @param speedInMbps the value of vpn site link speed
             * @return the next stage of the definition
             */
            Update withLinkProperties(String providerName, Integer speedInMbps);
        }

        /** The stage of the VPN site link description allowing bgp properties to be specified. */
        interface WithBgpProperties {
            /**
             * Specifies the VPN site link properties to which this vpn site link applies.
             *
             * @param bgpPeeringAddress the ip address of Bgp setting
             * @param asn the value of Bgp asn
             * @return the next stage of the definition
             */
            Update withBgpProperties(String bgpPeeringAddress, Long asn);
        }
    }
}
