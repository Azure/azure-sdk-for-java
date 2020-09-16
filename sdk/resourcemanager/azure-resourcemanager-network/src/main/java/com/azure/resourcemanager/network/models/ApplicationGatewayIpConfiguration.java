// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.inner.ApplicationGatewayIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of an application gateway IP configuration. */
@Fluent()
public interface ApplicationGatewayIpConfiguration
    extends HasInner<ApplicationGatewayIpConfigurationInner>, ChildResource<ApplicationGateway> {

    /** @return the resource ID of the virtual network the application gateway is in */
    String networkId();

    /** @return the name of the subnet the application gateway is in */
    String subnetName();

    /**
     * @return the subnet the application gateway is in
     *     <p>Note, this results in a separate call to Azure.
     */
    Subnet getSubnet();

    /** Grouping of application gateway IP configuration definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway IP configuration definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway IP configuration definition allowing to specify the subnet the
         * application gateway is on.
         *
         * @param <ParentT> the stage of the application gateway definition to return to after attaching this definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.DefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Subnet subnet);

            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param network an existing virtual network
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The final stage of the application gateway IP configuration definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached to the
         * parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway IP configuration definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithSubnet<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of application gateway IP configuration update stages. */
    interface UpdateStages {
        /**
         * The stage of an application gateway IP configuration update allowing to modify the subnet the application
         * gateway is part of.
         */
        interface WithSubnet extends HasSubnet.UpdateStages.WithSubnet<Update> {
            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param subnet an existing subnet
             * @return the next stage of the update
             */
            Update withExistingSubnet(Subnet subnet);

            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param network an existing virtual network
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the update
             */
            Update withExistingSubnet(Network network, String subnetName);
        }
    }

    /** The entirety of an application gateway IP configuration update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGateway.Update>, UpdateStages.WithSubnet {
    }

    /**
     * Grouping of application gateway IP configuration definition stages applicable as part of an application gateway
     * update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway IP configuration definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway IP configuration definition allowing to specify the subnet the
         * application gateway is on.
         *
         * @param <ParentT> the parent type
         */
        interface WithSubnet<ParentT> extends HasSubnet.UpdateDefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Subnet subnet);

            /**
             * Specifies an existing subnet the application gateway should be part of and get its private IP address
             * from.
             *
             * @param network an existing virtual network
             * @param subnetName the name of a subnet within the selected network
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The final stage of an application gateway IP configuration definition.
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
     * The entirety of an application gateway IP configuration definition as part of an application gateway update.
     *
     * @param <ParentT> the parent type
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithSubnet<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
