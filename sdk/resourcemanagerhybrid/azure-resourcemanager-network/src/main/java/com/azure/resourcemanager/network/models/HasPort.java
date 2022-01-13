// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to have a port number. */
@Fluent
public interface HasPort {
    /** @return the port number */
    int port();

    /** Grouping of definition stages involving specifying a port number. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify the port number.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPort<ReturnT> {
            /**
             * Specifies the port number.
             *
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            ReturnT withPort(int portNumber);
        }
    }

    /** Grouping of update stages involving specifying the port number. */
    interface UpdateStages {
        /**
         * The stage of a definition allowing to specify a port number.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithPort<ReturnT> {
            /**
             * Specifies the port number.
             *
             * @param portNumber a port number
             * @return the next stage of the update
             */
            ReturnT withPort(int portNumber);
        }
    }

    /** Grouping of definition stages of a parent resource update involving specifying a port number. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify the port number.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithPort<ReturnT> {
            /**
             * Specifies the port number.
             *
             * @param portNumber a port number
             * @return the next stage of the definition
             */
            ReturnT withPort(int portNumber);
        }
    }
}
