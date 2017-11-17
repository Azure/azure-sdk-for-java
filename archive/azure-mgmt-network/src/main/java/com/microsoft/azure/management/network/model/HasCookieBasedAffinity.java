/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * An interface representing a model's ability to support cookie based affinity.
 */
@Fluent
public interface HasCookieBasedAffinity  {
    /**
     * @return the backend port number the network traffic is sent to
     */
    boolean cookieBasedAffinity();

    /**
     * Grouping of definition stages involving enabling cookie based affinity.
     */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to enable cookie based affinity.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithCookieBasedAffinity<ReturnT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the definition
             */
            ReturnT withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the definition
             */
            ReturnT withoutCookieBasedAffinity();
        }
    }

    /**
     * Grouping of update stages involving modifying cookie based affinity.
     */
    interface UpdateStages {
        /**
         * The stage of an update allowing to modify cookie based affinity.
         * @param <ReturnT> the next stage of the update
         */
        interface WithCookieBasedAffinity<ReturnT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            ReturnT withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the update
             */
            ReturnT withoutCookieBasedAffinity();
        }
    }

    /**
     * Grouping of definition stages applicable as part of a resource update, involving modifying cookie based affinity.
     */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to enable or disable cookie based affinity.
         * @param <ReturnT> the next stage of the definition
         */
        interface WithCookieBasedAffinity<ReturnT> {
            /**
             * Enables cookie based affinity.
             * @return the next stage of the update
             */
            ReturnT withCookieBasedAffinity();

            /**
             * Disables cookie based affinity.
             * @return the next stage of the update
             */
            ReturnT withoutCookieBasedAffinity();
        }
    }
}
