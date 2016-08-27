/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

/**
 * The base interface for common members of inbound NAT-related interfaces.
 */
interface InboundNatCommon {

    /**
     * @return the transport protocol
     */
    TransportProtocol protocol();

    /**
     * @return the backend port number
     */
    int backendPort();

    /**
     * @return the frontend IP configuration associated with this NAT rule
     */
    Frontend frontend();

    /**
     * Grouping of inbound NAT rule definition stages.
     */
    interface DefinitionStages {
        /**
         * The stage of an inbound NAT definition allowing to specify the backend port.
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

        /**
         * The stage of an inbound NAT definition allowing to specify a frontend for the rule to apply to.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend for the inbound NAT to apply to.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            ReturnT withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT definition allowing to specify the transport protocol.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithProtocol<ReturnT> {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            ReturnT withProtocol(TransportProtocol protocol);
        }
    }

    /**
     * Grouping of inbound NAT update stages.
     */
    interface UpdateStages {
        /**
         * The stage of an inbound NAT update allowing to specify the backend port.
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

        /**
         * The stage of an inbound NAT update allowing to specify a frontend.
         * @param <ReturnT> the next stage of the update
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the update
             */
            ReturnT withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT update allowing to specify the transport protocol.
         * @param <ReturnT> the next stage of the update
         */
        interface WithProtocol<ReturnT> {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the update
             */
            ReturnT withProtocol(TransportProtocol protocol);
        }
    }

    /**
     * Grouping of inbound NAT definition stages applicable as part of a load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of an inbound NAT definition allowing to specify the backend port.
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

        /**
         * The stage of an inbound NAT definition allowing to specify a frontend for the rule to apply to.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFrontend<ReturnT> {
            /**
             * Specifies the frontend for the inbound NAT to apply to.
             * @param frontendName an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            ReturnT withFrontend(String frontendName);
        }

        /**
         * The stage of an inbound NAT definition allowing to specify the transport protocol.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithProtocol<ReturnT> {
            /**
             * Specifies the transport protocol.
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            ReturnT withProtocol(TransportProtocol protocol);
        }
    }

    /**
     * The fragment of the resource definition as part of the parent load balancer update allowing to specify frontend related settings.
     * @param <ReturnT> the next stage of the definition
     */
    interface UpdateDefinition<ReturnT> extends
        UpdateDefinitionStages.WithProtocol<ReturnT>,
        UpdateDefinitionStages.WithFrontend<ReturnT> {
    }
}
