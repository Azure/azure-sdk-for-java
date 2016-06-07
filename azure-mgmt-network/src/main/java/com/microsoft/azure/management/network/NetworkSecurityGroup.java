/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;


import java.util.List;

import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * Network security group.
 */
public interface NetworkSecurityGroup extends
        GroupableResource,
        Refreshable<NetworkSecurityGroup>,
        Wrapper<NetworkSecurityGroupInner>,
        Updatable<NetworkSecurityGroup.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/
    /**
     * @return list of security rules associated with this network security group
     */
    List<NetworkSecurityRule> securityRules();

    /**
     * @return list of default security rules associated with this network security group
     */
    List<NetworkSecurityRule> defaultSecurityRules();

    // Fluent interfaces for creating NSGs

    /**
     * The entirety of the network security group definition.
     */
    interface Definitions extends
        DefinitionBlank,
        DefinitionWithGroup,
        DefinitionCreatable {
    }

    /**
     * The first stage of the definition.
     */
    interface DefinitionBlank
        extends GroupableResource.DefinitionWithRegion<DefinitionWithGroup> {
    }

    /**
     * The stage of the definition allowing to specify the resource group.
     */
    interface DefinitionWithGroup
        extends GroupableResource.DefinitionWithGroup<DefinitionCreatable> {
    }

    /**
     * The stage of the definition allowing to define a new security rule.
     * <p>
     * When the security rule definition is complete enough, use {@link Attachable#attach()} to attach it to
     * this network security group.
     */
    interface DefinitionWithRule {
        /**
         * Starts the definition of a new security rule.
         * @param name the name for the new security rule
         * @return the first stage of the security rule definition
         */
        NetworkSecurityRule.DefinitionBlank<DefinitionCreatable> defineRule(String name);
    }

    /**
     * The stage of the resource definition allowing to add or remove security rules.
     */
    interface UpdateWithRule {
        Update withoutRule(String name);
        //TODO defineRule(String name)...
    }

    /**
     * The stage of the definition which contains all the minimum required inputs for
     * the resource to be created (via {@link DefinitionCreatable#create()}), but also allows
     * for any other optional settings to be specified.
     */
    interface DefinitionCreatable extends
        Creatable<NetworkSecurityGroup>,
        Resource.DefinitionWithTags<DefinitionCreatable>,
        DefinitionWithRule {
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     * <p>
     * Call {@link Update#apply()} to apply the changes to the resource in Azure.
     */
    interface Update extends
        Appliable<NetworkSecurityGroup>,
        Resource.UpdateWithTags<Update>,
        UpdateWithRule {
    }
}
