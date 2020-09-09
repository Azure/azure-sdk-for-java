// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/** An interface representing a model's ability to references a frontend. */
@Fluent
public interface HasFrontend {
    /** @return the associated frontend */
    LoadBalancerFrontend frontend();

    /** Grouping of definition stages involving specifying the frontend. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify a load balancer frontend.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend to receive network traffic from.
             *
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            ReturnT fromFrontend(String frontendName);

            /**
             * Specifies that network traffic should be received on a new public IP address that is to be created along
             * with the load balancer in the same region and resource group but under the provided leaf DNS label,
             * assuming it is available.
             *
             * <p>A new automatically-named public frontend will be implicitly created on this load balancer for each
             * such new public IP address, so make sure to use a unique DNS label.
             *
             * @param leafDnsLabel a unique leaf DNS label to create the public IP address under
             * @return the next stage of the definition
             */
            ReturnT fromNewPublicIPAddress(String leafDnsLabel);

            /**
             * Specifies that network traffic should be received on a new public IP address that is to be created along
             * with the load balancer based on the provided definition.
             *
             * <p>A new automatically-named public frontend will be implicitly created on this load balancer for each
             * such new public IP address.
             *
             * @param pipDefinition a definition for the new public IP
             * @return the next stage of the definition
             */
            ReturnT fromNewPublicIPAddress(Creatable<PublicIpAddress> pipDefinition);

            /**
             * Specifies that network traffic should be received on a new public IP address that is to be automatically
             * created woth default settings along with the load balancer.
             *
             * <p>A new automatically-named public frontend will be implicitly created on this load balancer for each
             * such new public IP address.
             *
             * @return the next stage of the definition
             */
            ReturnT fromNewPublicIPAddress();

            /**
             * Specifies an existing public IP address to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this public IP address, that is the frontend
             * that will be used. Else, an automatically named new public frontend will be created implicitly on the
             * load balancer.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT fromExistingPublicIPAddress(PublicIpAddress publicIPAddress);

            /**
             * Specifies an existing public IP address to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this public IP address, that is the frontend
             * that will be used. Else, an automatically named new public frontend will be created implicitly on the
             * load balancer.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT fromExistingPublicIPAddress(String resourceId);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param network an existing network
             * @param subnetName the name of an existing subnet within the specified network
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(Network network, String subnetName);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param networkResourceId the resource ID of an existing network
             * @param subnetName the name of an existing subnet within the specified network
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(String networkResourceId, String subnetName);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(Subnet subnet);
        }
    }

    /** Grouping of update stages involving specifying the frontend. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to specify a frontend.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend.
             *
             * @param frontendName an existing frontend name from this load balancer
             * @return the next stage of the update
             */
            ReturnT fromFrontend(String frontendName);
        }
    }

    /** Grouping of definition stages applicable as part of a resource update involving modifying the frontend. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify a frontend from to associate.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend to associate.
             *
             * @param frontendName an existing frontend name
             * @return the next stage of the definition
             */
            ReturnT fromFrontend(String frontendName);

            /**
             * Specifies an existing public IP address to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this public IP address, that is the frontend
             * that will be used. Else, an automatically named new public frontend will be created implicitly on the
             * load balancer.
             *
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT fromExistingPublicIPAddress(PublicIpAddress publicIPAddress);

            /**
             * Specifies an existing public IP address to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this public IP address, that is the frontend
             * that will be used. Else, an automatically named new public frontend will be created implicitly on the
             * load balancer.
             *
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            ReturnT fromExistingPublicIPAddress(String resourceId);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param network an existing network
             * @param subnetName the name of an existing subnet within the specified network
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(Network network, String subnetName);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param networkResourceId the resource ID of an existing network
             * @param subnetName the name of an existing subnet within the specified network
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(String networkResourceId, String subnetName);

            /**
             * Specifies an existing private subnet to receive network traffic from.
             *
             * <p>If this load balancer already has a frontend referencing this subnet, that is the frontend that will
             * be used. Else, an automatically named new private frontend will be created implicitly on the load
             * balancer.
             *
             * @param subnet an existing subnet
             * @return the next stage of the definition
             */
            ReturnT fromExistingSubnet(Subnet subnet);
        }
    }
}
