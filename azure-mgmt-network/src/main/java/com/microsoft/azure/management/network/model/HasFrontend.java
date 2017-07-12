/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.PublicIPAddress;

/**
 * An interface representing a model's ability to references a frontend.
 */
@Fluent
public interface HasFrontend  {
    /**
     * @return the associated frontend
     */
    LoadBalancerFrontend frontend();

    /**
     * Grouping of definition stages involving specifying the frontend.
     */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify a load balancer frontend.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend to receive network traffic from.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromFrontend(String frontendName);

            /**
             * Selects the load balancer's default frontend as the frontend to receive network traffic from.
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromDefaultFrontend();

            /**
             * Specifies an existing public IP address to receive network traffic from.
             * <p>
             * If this load balancer already has a frontend referencing this public IP address, that is the frontend that will be used.
             * Else, an automatically named new frontend will be created implicitly on the load balancer.
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromExistingPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Specifies an existing public IP address to receive network traffic from.
             * <p>
             * If this load balancer already has a frontend referencing this public IP address, that is the frontend that will be used.
             * Else, an automatically named new frontend will be created implicitly on the load balancer.
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromExistingPublicIPAddress(String resourceId);
        }
    }

    /**
     * Grouping of update stages involving specifying the frontend.
     */
    interface UpdateStages {
        /**
         * The stage of an update allowing to specify a frontend.
         * @param <ReturnT> the next stage of the update
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend.
             * @param frontendName an existing frontend name from this load balancer
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromFrontend(String frontendName);

            /**
             * Associates with the default frontend.
             * @return the next stage of the update
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromDefaultFrontend();
        }
    }

    /**
     * Grouping of definition stages applicable as part of a resource update involving modifying the frontend.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify a frontend from to associate.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend to associate.
             * @param frontendName an existing frontend name
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromFrontend(String frontendName);

            /**
             * Associates with the default frontend.
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromDefaultFrontend();

            /**
             * Specifies an existing public IP address to receive network traffic from.
             * <p>
             * If this load balancer already has a frontend referencing this public IP address, that is the frontend that will be used.
             * Else, an automatically named new frontend will be created implicitly on the load balancer.
             * @param publicIPAddress an existing public IP address
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromExistingPublicIPAddress(PublicIPAddress publicIPAddress);

            /**
             * Specifies an existing public IP address to receive network traffic from.
             * <p>
             * If this load balancer already has a frontend referencing this public IP address, that is the frontend that will be used.
             * Else, an automatically named new frontend will be created implicitly on the load balancer.
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            @Beta(SinceVersion.V1_2_0)
            ReturnT fromExistingPublicIPAddress(String resourceId);
        }
    }
}
