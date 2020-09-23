// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasSubnet;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A client-side representation of an application gateway frontend. */
@Fluent()
public interface ApplicationGatewayFrontend
    extends HasInnerModel<ApplicationGatewayFrontendIpConfiguration>,
        ChildResource<ApplicationGateway>,
        HasPrivateIpAddress,
        HasSubnet,
        HasPublicIpAddress {

    /** @return true if the frontend is accessible via a public IP address, else false */
    boolean isPublic();

    /** @return true is the frontend is accessible via an private IP address */
    boolean isPrivate();

    /** @return the associated subnet */
    Subnet getSubnet();

    /** Grouping of application gateway frontend definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of an application gateway frontend definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify a subnet from the selected
         * network to make this application gateway visible to.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.DefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Assigns the specified subnet to this private frontend.
             *
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify the private IP address this
         * application gateway should be available at within the selected subnet.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithPrivateIP<ParentT>
            extends HasPrivateIpAddress.DefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to
         * make the application gateway available at as Internet-facing.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithPublicIPAddress<ParentT>
            extends HasPublicIpAddress.DefinitionStages.WithExistingPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of an application gateway frontend definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the frontend definition can be
         * attached to the parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinitionAlt<ParentT>, WithSubnet<ParentT>, WithPrivateIP<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway frontend definition.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithPublicIPAddress<ParentT> {
    }

    /** Grouping of application gateway frontend update stages. */
    interface UpdateStages {
        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to
         * make the application gateway available at as Internet-facing.
         */
        interface WithPublicIPAddress extends HasPublicIpAddress.UpdateStages.WithExistingPublicIPAddress<Update> {
        }
    }

    /** The entirety of an application gateway frontend update as part of an application gateway update. */
    interface Update extends Settable<ApplicationGateway.Update>, UpdateStages.WithPublicIPAddress {
    }

    /**
     * Grouping of application gateway frontend definition stages applicable as part of an application gateway update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of an application gateway frontend definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface Blank<ParentT> extends WithSubnet<ParentT> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify an existing public IP address to
         * make the application gateway available at as Internet-facing.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicIPAddress<ParentT>
            extends HasPublicIpAddress.UpdateDefinitionStages.WithExistingPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify a subnet from the selected
         * network to make this application gateway visible to.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithSubnet<ParentT> extends HasSubnet.UpdateDefinitionStages.WithSubnet<WithAttach<ParentT>> {
            /**
             * Assigns the specified subnet to this private frontend.
             *
             * @param network the virtual network the subnet exists in
             * @param subnetName the name of a subnet
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingSubnet(Network network, String subnetName);
        }

        /**
         * The stage of an application gateway frontend definition allowing to specify the private IP address this
         * application gateway should be available at within the selected virtual network.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithPrivateIP<ParentT>
            extends HasPrivateIpAddress.UpdateDefinitionStages.WithPrivateIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of an application gateway frontend definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the frontend definition can be
         * attached to the parent application gateway definition.
         *
         * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
         *     definition
         */
        interface WithAttach<ParentT>
            extends Attachable.InUpdateAlt<ParentT>,
                WithPublicIPAddress<ParentT>,
                WithSubnet<ParentT>,
                WithPrivateIP<ParentT> {
        }
    }

    /**
     * The entirety of an application gateway frontend definition as part of an application gateway update.
     *
     * @param <ParentT> the stage of the parent application gateway definition to return to after attaching this
     *     definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>, UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
