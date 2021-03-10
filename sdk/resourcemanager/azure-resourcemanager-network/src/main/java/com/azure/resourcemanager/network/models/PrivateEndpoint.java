// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure private endpoint. */
public interface PrivateEndpoint extends
    GroupableResource<NetworkManager, PrivateEndpointInner>,
    Refreshable<PrivateEndpoint>,
    Updatable<PrivateEndpoint.Update> {

    /** A client-side representation of a private endpoint connection. */
    interface PrivateLinkServiceConnection extends
        HasInnerModel<com.azure.resourcemanager.network.models.PrivateLinkServiceConnection>,
        ChildResource<PrivateEndpoint> {

        /**
         * @return the state of the connection.
         */
        PrivateLinkServiceConnectionState state();

        /**
         * @return the resource ID that this connection connects to.
         */
        String privateLinkResourceId();

        /**
         * @return the collection of sub resource names.
         */
        List<PrivateLinkSubResourceName> subResourceNames();

        /**
         * @return the request message.
         */
        String requestMessage();

        /**
         * @return the provisioning state.
         */
        ProvisioningState provisioningState();

        /**
         * @return whether connection is manual approval.
         */
        boolean isManualApproval();

        /** Grouping of private endpoint connection definition stages. */
        interface DefinitionStages {
            /**
             * The first stage of the definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface Blank<ParentT> extends PrivateLinkServiceConnection.DefinitionStages.WithPrivateLinkServiceResource<ParentT> {
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
                PrivateLinkServiceConnection.DefinitionStages.WithSubResource<ParentT> withResource(Resource privateLinkServiceResource);
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
                PrivateLinkServiceConnection.DefinitionStages.WithApprovalMethod<ParentT> withSubResource(PrivateLinkSubResourceName subResourceName);
            }

            /**
             * The stage of the definition allowing to specify the approval method.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithApprovalMethod<ParentT> extends PrivateLinkServiceConnection.DefinitionStages.WithAttach<ParentT> {
                /**
                 * Specifies the approval method.
                 *
                 * @param requestMessage the request message for manual approval
                 * @return the next stage of the definition
                 */
                PrivateLinkServiceConnection.DefinitionStages.WithAttach<ParentT> withManualApproval(String requestMessage);
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
            PrivateLinkServiceConnection.DefinitionStages.Blank<ParentT>,
            PrivateLinkServiceConnection.DefinitionStages.WithPrivateLinkServiceResource<ParentT>,
            PrivateLinkServiceConnection.DefinitionStages.WithSubResource<ParentT>,
            PrivateLinkServiceConnection.DefinitionStages.WithApprovalMethod<ParentT>,
            PrivateLinkServiceConnection.DefinitionStages.WithAttach<ParentT> {
        }
    }

    /**
     * @return the reference of the subnet.
     */
    SubResource subnet();

    /**
     * @return the collection of reference of the network interfaces.
     */
    List<SubResource> networkInterfaces();

    /**
     * @return the provisioning state.
     */
    ProvisioningState provisioningState();

    /**
     * @return the collection of the private link service connections.
     */
    Map<String, PrivateLinkServiceConnection> privateLinkServiceConnections();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithSubnet,
        DefinitionStages.WithPrivateLinkServiceConnection,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the private endpoint definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the private endpoint definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of a private endpoint definition allowing to specify the resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithSubnet> {
        }

        /**
         * The stage of a private endpoint definition allowing to specify the subnet.
         */
        interface WithSubnet {
            /**
             * Specifies the subnet, from which the private IP will be allocated.
             *
             * @param subnet the subnet from which the private IP will be allocated
             * @return the next stage of private endpoint definition
             */
            WithPrivateLinkServiceConnection withSubnet(Subnet subnet);
        }

        /**
         * The stage of a private endpoint definition allowing to specify the private endpoint connection.
         */
        interface WithPrivateLinkServiceConnection {
            /**
             * Specifies the connection to remote resource.
             *
             * @param name the name of the connection
             * @return the next stage of private endpoint definition
             */
            PrivateLinkServiceConnection.DefinitionStages.Blank<WithCreate> definePrivateLinkServiceConnection(
                String name);
        }

        /**
         * A private endpoint definition with sufficient inputs to create a new private endpoint in the cloud, but
         * exposing additional optional inputs to specify.
         */
        interface WithCreate extends Creatable<PrivateEndpoint> {
        }
    }

    /** The template for a private endpoint update operation, containing all the settings that can be modified. */
    interface Update extends
        Appliable<PrivateEndpoint>,
        Resource.UpdateWithTags<Update> {

    }

    /**
     * Grouping of all the private endpoint update stages.
     */
    interface UpdateStages {

    }
}
