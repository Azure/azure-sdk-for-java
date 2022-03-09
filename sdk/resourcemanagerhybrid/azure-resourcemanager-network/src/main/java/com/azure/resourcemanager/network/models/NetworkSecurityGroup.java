// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.NetworkSecurityGroupInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Map;
import java.util.Set;

/** Network security group. */
@Fluent()
public interface NetworkSecurityGroup
    extends GroupableResource<NetworkManager, NetworkSecurityGroupInner>,
        Refreshable<NetworkSecurityGroup>,
        Updatable<NetworkSecurityGroup.Update>,
        UpdatableWithTags<NetworkSecurityGroup>,
        HasAssociatedSubnets {

    // Getters

    /** @return security rules associated with this network security group, indexed by their names */
    Map<String, NetworkSecurityRule> securityRules();

    /** @return default security rules associated with this network security group, indexed by their name */
    Map<String, NetworkSecurityRule> defaultSecurityRules();

    /** @return the IDs of the network interfaces associated with this network security group */
    Set<String> networkInterfaceIds();

    // Fluent interfaces for creating NSGs

    /** The entirety of the network security group definition. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate {
    }

    /** Grouping of network security group definition stages. */
    interface DefinitionStages {
        /** The first stage of the definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage allowing to define a new security rule.
         *
         * <p>When the security rule description is complete enough, use {@link Attachable#attach()} to attach it to
         * this network security group.
         */
        interface WithRule {
            /**
             * Starts the definition of a new security rule.
             *
             * @param name the name for the new security rule
             * @return the first stage of the security rule definition
             */
            NetworkSecurityRule.DefinitionStages.Blank<WithCreate> defineRule(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<NetworkSecurityGroup>,
                Resource.DefinitionWithTags<WithCreate>,
                DefinitionStages.WithRule {
        }
    }

    /** Grouping of network security group update stages. */
    interface UpdateStages {
        /** The stage of the resource definition allowing to add or remove security rules. */
        interface WithRule {
            /**
             * Removes an existing security rule.
             *
             * @param name the name of the security rule to remove
             * @return the next stage of the network security group description
             */
            Update withoutRule(String name);

            /**
             * Begins the definition of a new security rule to be added to this network security group.
             *
             * @param name the name of the new security rule
             * @return the first stage of the new security rule definition
             */
            NetworkSecurityRule.UpdateDefinitionStages.Blank<Update> defineRule(String name);

            /**
             * Begins the description of an update of an existing security rule of this network security group.
             *
             * @param name the name of an existing security rule
             * @return the first stage of the security rule update description
             */
            NetworkSecurityRule.Update updateRule(String name);
        }
    }

    /**
     * The template for an update operation, containing all the settings that can be modified.
     *
     * <p>Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends Appliable<NetworkSecurityGroup>, Resource.UpdateWithTags<Update>, UpdateStages.WithRule {
    }
}
