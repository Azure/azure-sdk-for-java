// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * An interface representing a model's ability to reference a host name.
 * The hostname supports wildcard format, e.g. "*.contoso.com", but is available only for {@link ApplicationGatewaySkuName#STANDARD_V2} and {@link ApplicationGatewaySkuName#WAF_V2} SKU
 */
@Fluent
public interface HasHostname {
    /**
     * @return the associated host name, or the first one if there exists multiple host names
     */
    String hostname();

    /** @return the associated host names */
    List<String> hostnames();

    /** Grouping of definition stages involving specifying the host name. */
    interface DefinitionStages {
        /**
         * The stage of a definition allowing to specify host names.
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

            /**
             * Specifies up to 5 hostnames to reference.
             * @param hostnames existing frontend hostnames on this load balancer
             * @return the next stage of the definition
             */
            ReturnT withHostnames(List<String> hostnames);
        }
    }

    /** Grouping of update stages involving specifying the host name. */
    interface UpdateStages {
        /**
         * The stage of an update allowing to specify host names.
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

            /**
             * Specifies up to 5 hostnames to reference.
             * @param hostnames existing host names
             * @return the next stage of the definition
             */
            ReturnT withHostnames(List<String> hostnames);
        }
    }

    /** Grouping of definition stages applicable as part of a parent resource update. */
    interface UpdateDefinitionStages {
        /**
         * The stage of a definition allowing to specify host names.
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

            /**
             * Specifies up to 5 hostnames to reference.
             * @param hostnames existing host names
             * @return the next stage of the definition
             */
            ReturnT withHostnames(List<String> hostnames);
        }
    }
}
