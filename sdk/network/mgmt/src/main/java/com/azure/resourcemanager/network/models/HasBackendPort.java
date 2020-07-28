// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to reference a backend port. */
@Fluent
public interface HasBackendPort {
    /** @return the backend port number the network traffic is sent to */
    int backendPort();

    /** Grouping of definition stages involving specifying a backend port. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the backend port.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies a backend port to send network traffic to.
             *
             * <p>If not specified, the same backend port number is assumed as that used by the frontend.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT toBackendPort(int port);
        }
    }

    /** Grouping of update stages involving modifying a backend port. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify the backend port.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies a backend port to send network traffic to.
             *
             * @param port a port number
             * @return the next stage of the update
             */
            ReturnT toBackendPort(int port);
        }
    }

    /** Grouping of definition stages applicable as part of a resource update, involving modifying the backend port. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the backend port.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies a backend port to send network traffic to.
             *
             * <p>If not specified, the same backend port number is assumed as that used by the frontend.
             *
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT toBackendPort(int port);
        }
    }
}
