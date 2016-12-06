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
     * @return true if the frontend is accessible via a public IP address, else false
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
     * Grouping of application gateway frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway frontend definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify a subnet from the selected network to make this
         * application gateway visible to.
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
         * The stage of an application gateway frontend definition allowing to specify the private IP address this application gateway
         * should be available at within the selected subnet.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPrivateIp<ParentT> extends HasPrivateIpAddress.DefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to make
         * the application gateway available at as Internet-facing.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.DefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of an application gateway frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinitionAlt<ParentT>,
            WithSubnet<ParentT>,
            WithPrivateIp<ParentT> {
        }
    }

    /** The entirety of an application gateway frontend definition.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithPublicIpAddress<ParentT> {
    }

    /**
     * Grouping of application gateway frontend update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to make
         * the application gateway available at as Internet-facing.
         */
        interface WithPublicIpAddress extends HasPublicIpAddress.UpdateStages.WithExistingPublicIpAddress<Update> {
        }
    }

    /**
     * The entirety of an application gateway frontend update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update>,
        UpdateStages.WithPublicIpAddress {
    }

    /**
     * Grouping of application gateway frontend definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gatewway frontend definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to make
         * the application gateway available at as Internet-facing.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.UpdateDefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify a subnet from the selected network to make this
         * application gateway visible to.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.UpdateDefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Assigns the specified subnet to this private frontend.
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify the private IP address this application gateway
         * should be available at within the selected virtual network.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithPrivateIp<ParentT> extends HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIpAddress<WithAttach<ParentT>> {
        }

        /** The final stage of an application gateway frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent application gateway definition.
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdateAlt<ParentT>,
            WithPublicIpAddress<ParentT>,
            WithSubnet<ParentT>,
            WithPrivateIp<ParentT> {
        }
    }

    /** The entirety of an application gateway frontend definition as part of an application gateway update.
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
