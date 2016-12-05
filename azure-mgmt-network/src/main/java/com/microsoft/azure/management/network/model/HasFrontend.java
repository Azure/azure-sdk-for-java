/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.LoadBalancerFrontend;

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
             * Specifies the frontend to associate.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            ReturnT withFrontend(String frontendName);
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
            ReturnT withFrontend(String frontendName);
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
            ReturnT withFrontend(String frontendName);
        }
    }
}
