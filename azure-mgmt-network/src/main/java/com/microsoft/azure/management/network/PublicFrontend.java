/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 * An immutable client-side representation of a public frontend of an Internet-facing load balancer.
 */
public interface PublicFrontend extends Frontend {

    /**
     * @return the resource ID of the public IP address associated with this frontend
     */
    String publicIpAddressId();

    /**
     * Grouping of public frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a public frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithPublicIpAddress<ParentT> {
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPublicIpAddress<ParentT> {
            /**
             * Associates the specified existing public IP address with this public frontend of the Internet-facing load balancer.
             * @param pip a public IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(PublicIpAddress pip);

            /**
             * Associates the specified existing public IP address with this public frontend of the intrenet-facing load balancer.
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(String resourceId);
        }

        /**
         * The final stage of a public frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }
    }

    /** The entirety of a public frontend definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithPublicIpAddress<ParentT> {
    }

    /**
     * Grouping of public frontend update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a public frontend update allowing to specify an existing public IP address.
         */
        interface WithPublicIpAddress {
            /**
             * Associates the specified existing public IP address with this public frontend of the Internet-facing load balancer.
             * @param pip a public IP address
             * @return the next stage of the update
             */
            Update withExistingPublicIpAddress(PublicIpAddress pip);

            /**
             * Associates the specified existing public IP address with this public frontend of the Internet-facing load balancer.
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the update
             */
            Update withExistingPublicIpAddress(String resourceId);
        }
    }

    /**
     * The entirety of a public frontend update as part of an Internet-facing load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithPublicIpAddress {
    }

    /**
     * Grouping of public frontend definition stages applicable as part of an Internet-facing load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a public frontend definition.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> extends WithPublicIpAddress<ParentT> {
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithPublicIpAddress<ParentT> {
            /**
             * Associates the specified existing public IP address with this frontend of the load balancer.
             * @param pip a public IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(PublicIpAddress pip);

            /**
             * Associates the specified existing public IP address with this public frontend of the Internet-facing load balancer.
             * @param resourceId the resource ID of an existing public IP address
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withExistingPublicIpAddress(String resourceId);
        }

        /** The final stage of the public frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of a public frontend definition as part of an Internet-facing load balancer update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithPublicIpAddress<ParentT> {
    }
}
