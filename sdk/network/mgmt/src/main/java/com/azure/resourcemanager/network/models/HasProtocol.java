// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/**
 * An interface representing a model's ability to reference a transport protocol.
 *
 * @param <ProtocolT> the protocol type of the value
 */
@Fluent()
public interface HasProtocol<ProtocolT> {
    /** @return the protocol */
    ProtocolT protocol();

    /** Grouping of definition stages involving specifying the protocol. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the protocol.
         *
         * @param <ReturnT> the next stage of the definition
         * @param <ProtocolT> the type of the protocol value
         */
        interface WithProtocol<ReturnT, ProtocolT> {
            /**
             * Specifies the transport protocol.
             *
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            ReturnT withProtocol(ProtocolT protocol);
        }
    }

    /** Grouping of update stages involving modifying the protocol. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify the transport protocol.
         *
         * @param <ReturnT> the next stage of the update
         * @param <ProtocolT> the type of the protocol value
         */
        interface WithProtocol<ReturnT, ProtocolT> {
            /**
             * Specifies the transport protocol.
             *
             * @param protocol a transport protocol
             * @return the next stage of the update
             */
            ReturnT withProtocol(ProtocolT protocol);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a load balancer update, involving specifying the protocol.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the protocol.
         *
         * @param <ReturnT> the next stage of the definition
         * @param <ProtocolT> the protocol type of the value
         */
        interface WithProtocol<ReturnT, ProtocolT> {
            /**
             * Specifies the transport protocol.
             *
             * @param protocol a transport protocol
             * @return the next stage of the definition
             */
            ReturnT withProtocol(ProtocolT protocol);
        }
    }
}
