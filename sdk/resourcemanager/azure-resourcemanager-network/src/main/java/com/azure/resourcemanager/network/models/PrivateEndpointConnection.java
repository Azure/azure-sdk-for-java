// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** A client-side representation of a private endpoint connection. */
public interface PrivateEndpointConnection extends
    HasInnerModel<PrivateLinkServiceConnection>, ChildResource<PrivateEndpoint> {

    /**
     * @return the state of the connection.
     */
    PrivateLinkServiceConnectionState state();

    /**
     * @return the provisioning state.
     */
    ProvisioningState provisioningState();

    /** Grouping of private endpoint connection definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithPrivateLinkServiceResource<ParentT> {
        }

        /**
         * The stage of the definition allowing to specify the resource of the private link service.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPrivateLinkServiceResource<ParentT> {
            /**
             * Specifies the resource of the private link service.
             *
             * @param privateLinkServiceResource the resource of the private link service
             * @return the next stage of the definition
             */
            WithSubResource<ParentT> withResource(Resource privateLinkServiceResource);
        }

        /**
         * The stage of the definition allowing to specify the sub resource that this private endpoint
         * should connect to.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSubResource<ParentT> {
            /**
             * Specifies the sub resource.
             *
             * @param subResourceName the name of the sub resource
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withSubResource(PrivateLinkSubResourceName subResourceName);
        }

        /**
         * The final stage of the definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the definition can be attached
         * to the parent definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The entirety of the definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithPrivateLinkServiceResource<ParentT>,
        DefinitionStages.WithSubResource<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }
}
