/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.ApplicationGatewayIPConfigurationInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * An immutable client-side representation of an application gateway IP conifguration.
 */
@Fluent()
public interface ApplicationGatewayIpConfiguration extends
    Wrapper<ApplicationGatewayIPConfigurationInner>,
    ChildResource<ApplicationGateway> {

    /**
     * Grouping of application gateway IP configuration definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway IP configuration definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithContainingSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway IP configuration definition allowing to specify the subnet the application gateway is on.
         * @param <ParentT> the parent type
         */
        interface WithContainingSubnet<ParentT> {
            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address from.
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withContainingSubnet(Subnet subnet);

            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address from.
             * @param network an existing virtual network
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withContainingSubnet(Network network, String subnetName);

            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address from.
             * @param networkId the resource ID of an existing virtual network
             * @param subnetName the name of a subset within the selected network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withContainingSubnet(String networkId, String subnetName);
        }

        /** The final stage of the application gateway IP configuration definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }
    }

    /** The entirety of an application gateway IP configuration definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithContainingSubnet<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of application gateway IP configuration update stages.
     */
    interface UpdateStages {
    }

    /**
     * The entirety of an application gateway IP configuration update as part of an application gateway update.
     */
    interface Update extends
        Settable<ApplicationGateway.Update> {
    }

    /**
     * Grouping of application gateway IP configuration definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway IP configuration definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithContainingSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway IP configuration definition allowing to specify the subnet the application gateway is on.
         * @param <ParentT> the parent type
         */
        interface WithContainingSubnet<ParentT> {
            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address from.
             * @param subnet an existing subnet
             * @return the next stage of the subnet definition
             */
            WithAttach<ParentT> withContainingSubnet(Subnet subnet);
        }

        /** The final stage of an application gateway IP configuration definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the definition
         * can be attached to the parent application gateway definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of an application gateway IP configuration definition as part of an application gateway update.
     * @param <ParentT> the return type of the final {@link WithAttach<>#attach()}
     */
    interface UpdateDefinition<ParentT> extends
       UpdateDefinitionStages.Blank<ParentT>,
       UpdateDefinitionStages.WithContainingSubnet<ParentT>,
       UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
