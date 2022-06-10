// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

/** An interface representing a model's ability to reference a host name. */
@Fluent
public interface HasHostname {
    /** @return the associated host name */
    String hostname();

    /** Grouping of definition stages involving specifying the host name. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify a host name.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithHostname<ReturnT> {
            /**
             * Specifies the hostname to reference.
             *
             * @param hostname an existing frontend name on this load balancer
             * @return the next stage of the definition
             */
            ReturnT withHostname(String hostname);
        }
    }

    /** Grouping of update stages involving specifying the host name. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to specify a host name.
         *
         * @param <ReturnT> the next stage of the update
         */
        interface WithHostname<ReturnT> {
            /**
             * Specifies the host name.
             *
             * @param hostname an existing host name
             * @return the next stage of the update
             */
            ReturnT withHostname(String hostname);
        }
    }

    /** Grouping of definition stages applicable as part of a parent resource update. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify a host name.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithHostname<ReturnT> {
            /**
             * Specifies the host name to reference.
             *
             * @param hostname an existing host name
             * @return the next stage of the definition
             */
            ReturnT withHostname(String hostname);
        }
    }
}
