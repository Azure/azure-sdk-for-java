/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.implementation.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An immutable client-side representation of a child model of a virtual network.
 */
@Fluent()
public interface NewChildModel extends
    HasInner<SubnetInner>,
    ChildResource<NewTopLevelModel> {

    String addressPrefix();

    /**
     * Grouping of child model definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAddressPrefix<ParentT> {
        }

        /**
         * The stage of a subnet definition allowing to specify the address prefix.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the address prefix for the subnet.
             * @param cidr an address prefix expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);  
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }
    }

    /** The entirety of a Subnet definition.
     * @param <ParentT> the return type of the final {@link DefinitionStages.WithAttach#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithAddressPrefix<ParentT>,
        DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Grouping of child model update stages.
     */
    interface UpdateStages {
        interface WithAddressPrefix {
            Update withAddressPrefix(String cidr);
        }
    }

    /**
     * The entirety of a subnet update as part of a network update.
     */
    interface Update extends
        UpdateStages.WithAddressPrefix,
        Settable<NewTopLevelModel.Update> {
    }

    /**
     * Grouping of subnet definition stages applicable as part of a virtual network update.
     */
    interface UpdateDefinitionStages {
        /**
         * The first stage of the subnet definition.
         * @param <ParentT> the stage of the parent update to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithAttach<ParentT> {
        }

        /**
         * The stage of a subnet definition allowing to specify the address prefix.
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAddressPrefix<ParentT> {
            /**
             * Specifies the address prefix for the subnet.
             * @param cidr an address prefix expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAddressPrefix(String cidr);  
        }

        /** The final stage of the subnet definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the subnet definition
         * can be attached to the parent virtual network definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }
    }

    /** The entirety of a subnet definition as part of a virtual network update.
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT> extends
       UpdateDefinitionStages.Blank<ParentT>,
       UpdateDefinitionStages.WithAddressPrefix<ParentT>,
       UpdateDefinitionStages.WithAttach<ParentT> {
    }
}
