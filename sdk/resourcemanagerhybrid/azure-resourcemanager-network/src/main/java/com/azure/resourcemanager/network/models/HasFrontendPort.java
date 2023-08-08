// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to reference a frontend port. */
@Fluent
public interface HasFrontendPort {
    /** @return the frontend port number the inbound network traffic is received on */
    int frontendPort();

    /** Grouping of definition stages involving specifying a frontend port. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the frontend port.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontendPort<ReturnT> {
            /**
             * Specifies the frontend port to receive network traffic on.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT fromFrontendPort(int port);
        }
    }

    /** Grouping of update stages involving modifying a frontend port. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to specify the frontend port.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithFrontendPort<ReturnT> {
            /**
             * Specifies the frontend port to receive network traffic on.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT fromFrontendPort(int port);
        }
    }

    /** Grouping of definition stages applicable as part of a resource update, involving modifying the frontend port. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the frontend port.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontendPort<ReturnT> {
            /**
             * Specifies the frontend port to receive network traffic on.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT fromFrontendPort(int port);
        }
    }
}
