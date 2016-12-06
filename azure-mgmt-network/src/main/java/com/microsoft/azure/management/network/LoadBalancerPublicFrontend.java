/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasPublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 * An immutable client-side representation of a public frontend of an Internet-facing load balancer.
 */
@Fluent()
public interface LoadBalancerPublicFrontend extends
    LoadBalancerFrontend,
    HasPublicIpAddress {

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
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.DefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
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
        interface WithPublicIpAddress extends HasPublicIpAddress.UpdateStages.WithExistingPublicIpAddress<Update> {
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
        interface WithPublicIpAddress<ParentT> extends HasPublicIpAddress.UpdateDefinitionStages.WithExistingPublicIpAddress<WithAttach<ParentT>> {
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
