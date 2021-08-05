// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to require server name indication. */
@Fluent
public interface HasServerNameIndication {
    /** @return true if server name indication (SNI) is required, else false. */
    boolean requiresServerNameIndication();

    /** Grouping of definition stages involving requiring the server name indication. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to require server name indication (SNI).
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithServerNameIndication<ReturnT> {
            /**
             * Requires server name indication (SNI).
             *
             * @return the next stage of the definition
             */
            ReturnT withServerNameIndication();

            /**
             * Ensures server name indication (SNI) is not required.
             *
             * @return the next stage of the definition
             */
            ReturnT withoutServerNameIndication();
        }
    }

    /** Grouping of update stages involving requiring the server name indication. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to require server name indication (SNI).
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithServerNameIndication<ReturnT> {
            /**
             * Requires server name indication (SNI).
             *
             * @return the next stage of the update
             */
            ReturnT withServerNameIndication();

            /**
             * Ensures server name indication (SNI) is not required.
             *
             * @return the next stage of the update
             */
            ReturnT withoutServerNameIndication();
        }
    }

    /** Grouping of definition stages applicable as part of a parent resource update. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to require server name indication (SNI).
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithServerNameIndication<ReturnT> {
            /**
             * Requires server name indication (SNI).
             *
             * @return the next stage of the definition
             */
            ReturnT withServerNameIndication();

            /**
             * Ensures server name indication (SNI) is not required.
             *
             * @return the next stage of the definition
             */
            ReturnT withoutServerNameIndication();
        }
    }
}
