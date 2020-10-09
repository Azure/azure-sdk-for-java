// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.models.VirtualNetworkRuleInner;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Server Virtual Network Rule. */
@Fluent
public interface SqlVirtualNetworkRule
    extends ExternalChildResource<SqlVirtualNetworkRule, SqlServer>,
        HasInnerModel<VirtualNetworkRuleInner>,
        HasResourceGroup,
        Refreshable<SqlVirtualNetworkRule>,
        Updatable<SqlVirtualNetworkRule.Update> {

    /** @return name of the SQL Server to which this Virtual Network Rule belongs */
    String sqlServerName();

    /** @return the subnet ID of the Azure SQL Server Virtual Network Rule. */
    String subnetId();

    /**
     * @return the Azure SQL Server Virtual Network Rule state; possible values include: 'Initializing', 'InProgress',
     *     'Ready', 'Deleting', 'Unknown'
     */
    String state();

    /** @return the parent SQL server ID */
    String parentId();

    /** Deletes the virtual network rule. */
    void delete();

    /**
     * Deletes the virtual network rule asynchronously.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**************************************************************
     * Fluent interfaces to provision a SQL Virtual Network Rule
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface SqlVirtualNetworkRuleDefinition<ParentT>
        extends SqlVirtualNetworkRule.DefinitionStages.Blank<ParentT>,
            SqlVirtualNetworkRule.DefinitionStages.WithSubnet<ParentT>,
            SqlVirtualNetworkRule.DefinitionStages.WithServiceEndpoint<ParentT>,
            SqlVirtualNetworkRule.DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of all the SQL Virtual Network Rule definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server Virtual Network Rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends SqlVirtualNetworkRule.DefinitionStages.WithSubnet<ParentT> {
        }

        /** The SQL Virtual Network Rule definition to set the virtual network ID and the subnet name. */
        interface WithSubnet<ParentT> {
            /**
             * Sets the virtual network ID and the subnet name for the SQL server Virtual Network Rule.
             *
             * @param networkId the virtual network ID to be used
             * @param subnetName the name of the subnet within the virtual network to be used
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRule.DefinitionStages.WithServiceEndpoint<ParentT> withSubnet(
                String networkId, String subnetName);
        }

        /**
         * The SQL Virtual Network Rule definition to set ignore flag for the missing subnet's SQL service endpoint
         * entry.
         */
        interface WithServiceEndpoint<ParentT> extends SqlVirtualNetworkRule.DefinitionStages.WithAttach<ParentT> {
            /**
             * Sets the flag to ignore the missing subnet's SQL service endpoint entry.
             *
             * <p>Virtual Machines in the subnet will not be able to connect to the SQL server until Microsoft.Sql
             * service endpoint is added to the subnet
             *
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRule.DefinitionStages.WithAttach<ParentT> ignoreMissingSqlServiceEndpoint();
        }

        /**
         * The final stage of the SQL Virtual Network Rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the SQL Virtual Network Rule
         * definition can be attached to the parent SQL Server definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {
        }
    }

    /**
     * The template for a SQL Virtual Network Rule update operation, containing all the settings that can be modified.
     */
    interface Update
        extends SqlVirtualNetworkRule.UpdateStages.WithSubnet,
            SqlVirtualNetworkRule.UpdateStages.WithServiceEndpoint,
            Appliable<SqlVirtualNetworkRule> {
    }

    /** Grouping of all the SQL Virtual Network Rule update stages. */
    interface UpdateStages {
        /** The SQL Virtual Network Rule definition to set the virtual network ID and the subnet name. */
        interface WithSubnet {
            /**
             * Sets the virtual network ID and the subnet name for the SQL server Virtual Network Rule.
             *
             * @param networkId the virtual network ID to be used
             * @param subnetName the name of the subnet within the virtual network to be used
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRule.Update withSubnet(String networkId, String subnetName);
        }

        /**
         * The SQL Virtual Network Rule definition to set ignore flag for the missing subnet's SQL service endpoint
         * entry.
         */
        interface WithServiceEndpoint {
            /**
             * Sets the flag to ignore the missing subnet's SQL service endpoint entry.
             *
             * <p>Virtual Machines in the subnet will not be able to connect to the SQL server until Microsoft.Sql
             * service endpoint is added to the subnet
             *
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRule.Update ignoreMissingSqlServiceEndpoint();
        }
    }
}
