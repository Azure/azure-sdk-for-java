// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.VirtualNetworkGatewayIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of an virtual network gateway IP configuration. */
@Fluent
public interface VirtualNetworkGatewayIpConfiguration
    extends HasInner<VirtualNetworkGatewayIpConfigurationInner>, ChildResource<VirtualNetworkGateway> {
    /** @return the resource ID of the virtual network the application gateway is in */
    String networkId();

    /** @return the resource id of associated public IP address */
    String publicIpAddressId();

    /** @return the name of the subnet the virtual network gateway is in */
    String subnetName();

    /** @return the private IP allocation method. Possible values are: 'Static' and 'Dynamic'. */
    IpAllocationMethod privateIpAllocationMethod();

    /** @return the subnet the virtual network gateway is in Note, this results in a separate call to Azure. */
    Subnet getSubnet();

    /** Grouping of virtual network gateway IP configuration definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an virtual network gateway IP configuration definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of virtual network gateway IP configuration definition allowing to specify the subnet the virtual
         * network gateway is on.
         *
         * @param <ParentT> the stage of the virtual network gateway definition to return to after attaching this
         *     definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.DefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Specifies an existing subnet the virtual network gateway should be part of and get its private IP address
             * from.
             *
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Subnet subnet);

            /**
             * Specifies an existing subnet the virtual network gateway should be part of and get its private IP address
             * from.
             *
             * @param network an existing virtual network
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of virtual network gateway frontend definition allowing to specify an existing public IP address to
         * make the virtual network gateway available at as Internet-facing.
         *
         * @param <ParentT> the stage of the parent virtual network gateway definition to return to after attaching this
         *     definition
         */
        interface WithPublicIPAddress<ParentT>
            extends HasPublicIpAddress.DefinitionStages.WithExistingPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of the virtual network gateway IP configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent virtual network gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of virtual network gateway IP configuration definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithPublicIPAddress<ParentT> {
    }

    /** Grouping of application gateway IP configuration update stages. */
    interface UpdateStages {
    }

    /** The entirety of an application gateway IP configuration update as part of a virtual network gateway update. */
    interface Update extends Settable<VirtualNetworkGateway.Update> {
    }

    /**
     * Grouping of application gateway IP configuration definition stages applicable as part of a virtual network
     * gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a virtual network gateway IP configuration definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
        }

        /**
         * The final stage of a virtual network gateway IP configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway IP configuration definition as part of a virtual network gateway update.
     *
     * @param <ParentT> the parent type
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>, UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
