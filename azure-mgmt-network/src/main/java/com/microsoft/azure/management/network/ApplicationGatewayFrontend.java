/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayFrontendIPConfigurationInner;
import com.microsoft.azure.management.network.model.HasPrivateIpAddress;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway frontend.
 */
@Fluent()
public interface ApplicationGatewayFrontend extends
    Wrapper<ApplicationGatewayFrontendIPConfigurationInner>,
    ChildResource<ApplicationGateway>,
    HasPrivateIpAddress,
    HasSubnet,
    HasPublicIpAddress {

    /**
     * @return true if the frontend is accessible via a public IP address
     */
    boolean isPublic();

    /**
     * @return true is the frontend is accessible via an private IP address
     */
    boolean isPrivate();

    /**
     * @return the associated subnet
     */
    Subnet getSubnet();

    /**
     * Grouping of private frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.DefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Assigns the specified subnet to this private frontend.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.DefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of a private frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinitionAlt<ParentT>,
            HasPrivateIpAddress.DefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }
    }

    /** The entirety of a private frontend definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithSubnet<ParentT>,
        DefinitionStages.WithPublicIpAddress<ParentT> {
    }

    /**
     * Grouping of private frontend update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a private frontend update allowing to specify a subnet from the selected network.
         */
        interface WithSubnet {
            /**
             * Assigns the specified subnet to this private frontend.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            Update withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of a public frontend update allowing to specify an existing public IP address.
         */
        interface WithPublicIpAddress extends HasPublicIpAddress.UpdateStages.WithExistingPublicIpAddress<Update> {
        }
    }

    /**
     * The entirety of a private frontend update as part of a load balancer update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update>,
        UpdateStages.WithSubnet,
        UpdateStages.WithPublicIpAddress,
        HasPrivateIpAddress.UpdateStages.WithPrivateIpAddress<Update> {
    }

    /**
     * Grouping of private frontend definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a private frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.UpdateDefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of a private frontend definition allowing to specify a subnet from the selected network.
         * @param <ParentT> the next stage of the parent definition
         */
        interface WithSubnet<ParentT> {
            /**
             * Assigns the specified subnet to this private frontend.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /** The final stage of an internal frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent application gateway  definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdateAlt<ParentT>,
            WithPublicIpAddress<ParentT>,
            HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }
    }

    /** The entirety of a private frontend definition as part of an application gateway update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithSubnet<ParentT> {
    }
}
