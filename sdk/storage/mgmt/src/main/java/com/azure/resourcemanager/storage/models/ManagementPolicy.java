// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.storage.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.inner.ManagementPolicyInner;
import java.time.OffsetDateTime;
import java.util.List;

/** Type representing ManagementPolicy. */
@Fluent
public interface ManagementPolicy
    extends HasInner<ManagementPolicyInner>,
        Indexable,
        Refreshable<ManagementPolicy>,
        Updatable<ManagementPolicy.Update>,
        HasManager<StorageManager> {
    /** @return the id value. */
    String id();

    /** @return the lastModifiedTime value. */
    OffsetDateTime lastModifiedTime();

    /** @return the name value. */
    String name();

    /** @return the policy value. */
    ManagementPolicySchema policy();

    /** @return the type value. */
    String type();

    /** @return the list of rules for this policy */
    List<PolicyRule> rules();

    /** The entirety of the ManagementPolicy definition. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithStorageAccount,
            DefinitionStages.WithRule,
            DefinitionStages.WithCreate {
    }

    /** Grouping of ManagementPolicy definition stages. */
    interface DefinitionStages {
        /** The first stage of a ManagementPolicy definition. */
        interface Blank extends WithStorageAccount {
        }

        /** The stage of the managementpolicy definition allowing to specify StorageAccount. */
        interface WithStorageAccount {
            /**
             * Specifies resourceGroupName, accountName.
             *
             * @param resourceGroupName The name of the resource group within the user's subscription. The name is case
             *     insensitive
             * @param accountName The name of the storage account within the specified resource group. Storage account
             *     names must be between 3 and 24 characters in length and use numbers and lower-case letters only
             * @return the next definition stage
             */
            WithRule withExistingStorageAccount(String resourceGroupName, String accountName);
        }

        /** The stage of the management policy definition allowing to specify a rule to add to the management policy. */
        interface WithRule {
            /**
             * The function that defines a rule to attach to this policy.
             *
             * @param name the name of the rule we are going to define
             * @return the next definition stage
             */
            PolicyRule.DefinitionStages.Blank defineRule(String name);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<ManagementPolicy>, ManagementPolicy.DefinitionStages.WithRule {
        }
    }
    /** The template for a ManagementPolicy update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<ManagementPolicy>, UpdateStages.WithPolicy, UpdateStages.Rule {
    }

    /** Grouping of ManagementPolicy update stages. */
    interface UpdateStages {
        /** The stage of the management policy update allowing to specify Policy. */
        interface WithPolicy {
            /**
             * Specifies policy.
             *
             * @param policy The Storage Account ManagementPolicy, in JSON format. See more details in:
             *     https://docs.microsoft.com/en-us/azure/storage/common/storage-lifecycle-managment-concepts
             * @return the next update stage
             */
            Update withPolicy(ManagementPolicySchema policy);
        }

        /** The stage of the management policy update allowing to update a rule. */
        interface Rule {
            /**
             * The function that updates a rule whose name is the inputted parameter name.
             *
             * @param name the name of the rule to be updated.
             * @return the next stage of the management policy rule update.
             */
            PolicyRule.Update updateRule(String name);

            /**
             * The function that removes a rule whose name is the inputted parameter name.
             *
             * @param name the name of the rule to be removed.
             * @return the next stage of the management policy update.
             */
            Update withoutRule(String name);
        }
    }
}
