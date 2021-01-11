// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.cosmos.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.cosmos.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

/** A private endpoint connection. */
@Fluent
public interface PrivateEndpointConnection
    extends HasInnerModel<PrivateEndpointConnectionInner>,
        ExternalChildResource<PrivateEndpointConnection, CosmosDBAccount> {
    /**
     * Get private endpoint which the connection belongs to.
     *
     * @return the privateEndpoint value
     */
    PrivateEndpointProperty privateEndpoint();

    /**
     * Get connection State of the Private Endpoint Connection.
     *
     * @return the privateLinkServiceConnectionState value
     */
    PrivateLinkServiceConnectionStateProperty privateLinkServiceConnectionState();

    /** Grouping of private endpoint connection definition stages as a port of cosmos db account definition. */
    interface DefinitionStages {
        /**
         * The first stage of a private endpoint connection definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithState<ParentT> {
        }

        /**
         * The final stage of the private endpoint connection definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT>, WithState<ParentT> {
        }

        /**
         * The stage of the private endpoint connection definition allowing to set state.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithState<ParentT> {
            /**
             * Specifies state property.
             *
             * @param property a private link service connection state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withStateProperty(PrivateLinkServiceConnectionStateProperty property);

            /**
             * Specifies status of state property.
             *
             * @param status the status of state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withStatus(String status);

            /**
             * Specifies description of state property.
             *
             * @param description the description of state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withDescription(String description);
        }
    }

    /**
     * The entirety of a private endpoint connection definition as a part of parent definition.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithState<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of private endpoint connection definition stages as part of parent cosmos db account update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a private endpoint connection definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithState<ParentT> {
        }

        /**
         * The final stage of the private endpoint connection definition.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT>, WithState<ParentT> {
        }

        /**
         * The stage of the private endpoint connection definition allowing to set state.
         *
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface WithState<ParentT> {
            /**
             * Specifies state property.
             *
             * @param property a private link service connection state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withStateProperty(PrivateLinkServiceConnectionStateProperty property);

            /**
             * Specifies status of state property.
             *
             * @param status the status of state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withStatus(String status);

            /**
             * Specifies description of state property.
             *
             * @param description the description of state property
             * @return the next stage of definition
             */
            WithAttach<ParentT> withDescription(String description);
        }
    }

    /**
     * The entirety of a private endpoint connection definition definition as a part of parent update.
     *
     * @param <ParentT> the stage of the parent update to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithState<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of private endpoint connection update stages. */
    interface UpdateStages {
        /** The stage of the private endpoint connection update allowing to specify state. */
        interface WithState {
            /**
             * Specifies state property.
             *
             * @param property a private link service connection state property
             * @return the next stage of update
             */
            Update withStateProperty(PrivateLinkServiceConnectionStateProperty property);

            /**
             * Specifies status of state property.
             *
             * @param status the status of state property
             * @return the next stage of update
             */
            Update withStatus(String status);

            /**
             * Specifies description of state property.
             *
             * @param description the description of state property
             * @return the next stage of update
             */
            Update withDescription(String description);
        }
    }

    /** The entirety of private endpoint connection update as a part of parent virtual machine update. */
    interface Update extends Settable<CosmosDBAccount.Update>, UpdateStages.WithState {
    }
}
