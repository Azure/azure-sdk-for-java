// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

/** An interface representing a model's ability to have floating IP support. */
public interface HasFloatingIP {
    /** @return the state of the floating IP enablement */
    boolean floatingIPEnabled();

    /** Grouping of definition stages involving enabling or disabling floating IP support. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to control floating IP support.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFloatingIP<ReturnT> {
            /**
             * Enables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPEnabled();

            /**
             * Disables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPDisabled();

            /**
             * Sets the floating IP enablement.
             *
             * @param enabled true if floating IP should be enabled
             * @return the next stage of the definition
             */
            ReturnT withFloatingIP(boolean enabled);
        }
    }

    /** Grouping of update stages involving enabling or disabling floating IP support. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to control floating IP support.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFloatingIP<ReturnT> {
            /**
             * Enables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPEnabled();

            /**
             * Disables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPDisabled();

            /**
             * Sets the floating IP enablement.
             *
             * @param enabled true if floating IP should be enabled
             * @return the next stage of the definition
             */
            ReturnT withFloatingIP(boolean enabled);
        }
    }

    /**
     * Grouping of definition stages applicable as part of a load balancer update, involving enabling or disabling
     * floating IP support.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to control floating IP support.
         *
         * @param <ReturnT> the next stage of the definition
         */
        interface WithFloatingIP<ReturnT> {
            /**
             * Enables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPEnabled();

            /**
             * Disables floating IP support.
             *
             * @return the next stage of the definition
             */
            ReturnT withFloatingIPDisabled();

            /**
             * Sets the floating IP enablement.
             *
             * @param enabled true if floating IP should be enabled
             * @return the next stage of the definition
             */
            ReturnT withFloatingIP(boolean enabled);
        }
    }
}
