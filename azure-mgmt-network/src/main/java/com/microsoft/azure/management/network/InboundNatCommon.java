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
     * Grouping of inbound NAT definition stages.
     */
    interface DefinitionStages {
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
        UpdateDefinitionStages.WithProtocol<ReturnT> {
    }
}
