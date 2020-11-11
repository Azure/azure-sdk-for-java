// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/** A representation of the Azure SQL Virtual Network rule operations. */
@Fluent
public interface SqlVirtualNetworkRuleOperations
    extends SupportsCreating<SqlVirtualNetworkRuleOperations.DefinitionStages.WithSqlServer>,
        SqlChildrenOperations<SqlVirtualNetworkRule> {

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlVirtualNetworkRuleOperationsDefinition
        extends SqlVirtualNetworkRuleOperations.DefinitionStages.WithSqlServer,
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithSubnet,
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithServiceEndpoint,
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithCreate {
    }

    /** Grouping of all the SQL Virtual Network Rule definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server Virtual Network Rule definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithSubnet withExistingSqlServer(
                String resourceGroupName, String sqlServerName);

            /**
             * Sets the parent SQL server for the new Virtual Network Rule.
             *
             * @param sqlServerId the parent SQL server ID
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithSubnet withExistingSqlServerId(String sqlServerId);

            /**
             * Sets the parent SQL server for the new Virtual Network Rule.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithSubnet withExistingSqlServer(SqlServer sqlServer);
        }

        /** The SQL Virtual Network Rule definition to set the virtual network ID and the subnet name. */
        interface WithSubnet {
            /**
             * Sets the virtual network ID and the subnet name for the SQL server Virtual Network Rule.
             *
             * @param networkId the virtual network ID to be used
             * @param subnetName the name of the subnet within the virtual network to be used
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithServiceEndpoint withSubnet(
                String networkId, String subnetName);
        }

        /**
         * The SQL Virtual Network Rule definition to set ignore flag for the missing subnet's SQL service endpoint
         * entry.
         */
        interface WithServiceEndpoint extends SqlVirtualNetworkRuleOperations.DefinitionStages.WithCreate {
            /**
             * Sets the flag to ignore the missing subnet's SQL service endpoint entry.
             *
             * <p>Virtual Machines in the subnet will not be able to connect to the SQL server until Microsoft.Sql
             * service endpoint is added to the subnet
             *
             * @return The next stage of the definition.
             */
            SqlVirtualNetworkRuleOperations.DefinitionStages.WithCreate ignoreMissingSqlServiceEndpoint();
        }
        /** The final stage of the SQL Virtual Network Rule definition. */
        interface WithCreate extends Creatable<SqlVirtualNetworkRule> {
        }
    }

    /** Grouping of the Azure SQL Server Virtual Network Rule common actions. */
    interface SqlVirtualNetworkRuleActionsDefinition extends SqlChildrenActionsDefinition<SqlVirtualNetworkRule> {
        /**
         * Begins the definition of a new SQL Virtual Network Rule to be added to this server.
         *
         * @param virtualNetworkRuleName the name of the new SQL Virtual Network Rule
         * @return the first stage of the new SQL Virtual Network Rule definition
         */
        SqlVirtualNetworkRuleOperations.DefinitionStages.WithSubnet define(String virtualNetworkRuleName);
    }
}
