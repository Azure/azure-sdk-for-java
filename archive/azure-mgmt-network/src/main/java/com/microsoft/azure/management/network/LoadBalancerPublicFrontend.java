/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasPublicIPAddress;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;

/**
 * A client-side representation of a public frontend of an Internet-facing load balancer.
 */
@Fluent()
@Beta
public interface LoadBalancerPublicFrontend extends
    LoadBalancerFrontend,
    HasPublicIPAddress {

    /**
     * Grouping of public frontend definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a public frontend definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithPublicIPAddress<ParentT> {
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicIPAddress<ParentT> extends HasPublicIPAddress.DefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /**
         * The final stage of a public frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }
    }

    /** The entirety of a public frontend definition.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.WithPublicIPAddress<ParentT> {
    }

    /**
     * Grouping of public frontend update stages.
     */
    interface UpdateStages {
        /**
         * The stage of a public frontend update allowing to specify an existing public IP address.
         */
        interface WithPublicIPAddress extends HasPublicIPAddress.UpdateStages.WithPublicIPAddress<Update> {
        }
    }

    /**
     * The entirety of a public frontend update as part of an Internet-facing load balancer update.
     */
    interface Update extends
        Settable<LoadBalancer.Update>,
        UpdateStages.WithPublicIPAddress {
    }

    /**
     * Grouping of public frontend definition stages applicable as part of an Internet-facing load balancer update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a public frontend definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithPublicIPAddress<ParentT> {
        }

        /**
         * The stage of a public frontend definition allowing to specify an existing public IP address.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPublicIPAddress<ParentT> extends HasPublicIPAddress.UpdateDefinitionStages.WithPublicIPAddress<WithAttach<ParentT>> {
        }

        /** The final stage of the public frontend definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the frontend definition
         * can be attached to the parent load balancer definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of a public frontend definition as part of an Internet-facing load balancer update.
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinitionStages.Blank<ParentT>,
        UpdateDefinitionStages.WithAttach<ParentT>,
        UpdateDefinitionStages.WithPublicIPAddress<ParentT> {
    }
}
