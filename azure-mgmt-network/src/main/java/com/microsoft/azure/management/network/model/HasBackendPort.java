/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangDefinition.MethodConversion;

/**
 * An interface representing a model's ability to reference a load balancer backend port.
 */
@LangDefinition()
public interface HasBackendPort  {
    /**
     * @return the backend port number the network traffic is sent to
     */
    int backendPort();

    /**
     * Grouping of definition stages involving specifying a backend port.
     */
    @LangDefinition(
                ContainerName = "Definition",
                ContainerFileName = "IDefinition",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the backend port.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies the backend port.
             * <p>
             * If not specified, the same backend port number is assumed as that used by the frontend.
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT withBackendPort(int port);
        }
    }

    /**
     * Grouping of update stages involving modifying a backend port.
     */
    @LangDefinition(
                ContainerName = "Update",
                ContainerFileName = "IUpdate",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify the backend port.
         * @param <ReturnT> the next stage of the update
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies the backend port.
             * @param port a port number
             * @return the next stage of the update
             */
            ReturnT withBackendPort(int port);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a load balancer update, involving modifying the backend port.
     */
    @LangDefinition(
                ContainerName = "UpdateDefinition",
                ContainerFileName = "IUpdateDefinition",
                IsContainerOnly = true,
                MethodConversionType = MethodConversion.OnlyMethod)
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the backend port.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithBackendPort<ReturnT> {
            /**
             * Specifies the backend port.
             * <p>
             * If not specified, the same backend port number is assumed as that used by the frontend.
             * @param port a port number
             * @return the next stage of the definition
             */
            ReturnT withBackendPort(int port);
        }
    }
}
