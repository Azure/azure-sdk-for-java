// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.PrivateEndpointInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure private endpoint. */
public interface PrivateEndpoint extends
    GroupableResource<NetworkManager, PrivateEndpointInner>,
    Refreshable<PrivateEndpoint>,
    Updatable<PrivateEndpoint.Update> {

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
     * @return the collection of the private endpoint connections.
     */
    Map<String, PrivateEndpointConnection> privateEndpointConnections();

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithSubnet,
        DefinitionStages.WithPrivateEndpointConnection,
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
            WithPrivateEndpointConnection withSubnet(Subnet subnet);
        }

        /**
         * The stage of a private endpoint definition allowing to specify the private endpoint connection.
         */
        interface WithPrivateEndpointConnection {
            /**
             * Specifies the connection to remote resource.
             *
             * @param name the name of the connection
             * @return the next stage of private endpoint definition
             */
            PrivateEndpointConnection.DefinitionStages.Blank<WithCreate> defineConnection(String name);
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
