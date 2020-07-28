// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsCreating;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;

/** A representation of the Azure SQL Elastic Pool operations. */
@Fluent
public interface SqlElasticPoolOperations
    extends SupportsCreating<SqlElasticPoolOperations.DefinitionStages.WithSqlServer>,
        SqlChildrenOperations<SqlElasticPool> {

    /** Container interface for all the definitions that need to be implemented. */
    interface SqlElasticPoolOperationsDefinition
        extends SqlElasticPoolOperations.DefinitionStages.WithSqlServer,
            SqlElasticPoolOperations.DefinitionStages.WithEdition,
            SqlElasticPoolOperations.DefinitionStages.WithBasicEdition,
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition,
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition {
    }

    /** Grouping of all the SQL Elastic Pool definition stages. */
    interface DefinitionStages {
        /** The first stage of the SQL Server Elastic Pool definition. */
        interface WithSqlServer {
            /**
             * Sets the parent SQL server name and resource group it belongs to.
             *
             * @param resourceGroupName the name of the resource group the parent SQL server
             * @param sqlServerName the parent SQL server name
             * @param location the parent SQL server location
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithEdition withExistingSqlServer(
                String resourceGroupName, String sqlServerName, String location);

            /**
             * Sets the parent SQL server for the new Elastic Pool.
             *
             * @param sqlServer the parent SQL server
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithEdition withExistingSqlServer(SqlServer sqlServer);
        }

        /** The SQL Elastic Pool definition to set the edition type. */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Elastic Pool.
             *
             * @deprecated use specific edition instead
             * @param edition edition to be set for Elastic Pool.
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPoolOperations.DefinitionStages.WithCreate withEdition(ElasticPoolEdition edition);

            /**
             * Sets the basic edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithBasicEdition withBasicPool();

            /**
             * Sets the standard edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition withStandardPool();

            /**
             * Sets the premium edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition withPremiumPool();
        }

        /** The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a basic pool. */
        interface WithBasicEdition extends SqlElasticPoolOperations.DefinitionStages.WithCreate {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithBasicEdition withReservedDtu(SqlElasticPoolBasicEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithBasicEdition withDatabaseDtuMax(
                SqlElasticPoolBasicMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithBasicEdition withDatabaseDtuMin(
                SqlElasticPoolBasicMinEDTUs eDTU);
        }

        /** The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a standard pool. */
        interface WithStandardEdition extends SqlElasticPoolOperations.DefinitionStages.WithCreate {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition withReservedDtu(
                SqlElasticPoolStandardEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition withDatabaseDtuMax(
                SqlElasticPoolStandardMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition withDatabaseDtuMin(
                SqlElasticPoolStandardMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithStandardEdition withStorageCapacity(
                SqlElasticPoolStandardStorage storageCapacity);
        }

        /** The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a premium pool. */
        interface WithPremiumEdition extends SqlElasticPoolOperations.DefinitionStages.WithCreate {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition withReservedDtu(
                SqlElasticPoolPremiumEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition withDatabaseDtuMax(
                SqlElasticPoolPremiumMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition withDatabaseDtuMin(
                SqlElasticPoolPremiumMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithPremiumEdition withStorageCapacity(
                SqlElasticPoolPremiumSorage storageCapacity);
        }

        /** The SQL Elastic Pool definition to set the minimum DTU for database. */
        interface WithDatabaseDtuMin {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMin minimum DTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPoolOperations.DefinitionStages.WithCreate withDatabaseDtuMin(double databaseDtuMin);
        }

        /** The SQL Elastic Pool definition to set the maximum DTU for one database. */
        interface WithDatabaseDtuMax {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMax maximum DTU any one SQL Azure Database can consume
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPoolOperations.DefinitionStages.WithCreate withDatabaseDtuMax(double databaseDtuMax);
        }

        /** The SQL Elastic Pool definition to set the number of shared DTU for elastic pool. */
        interface WithDtu {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @deprecated use specific edition instead
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPoolOperations.DefinitionStages.WithCreate withDtu(int dtu);
        }

        /**
         * The SQL Elastic Pool definition to set the storage limit for the SQL Azure Database Elastic Pool in Bytes.
         */
        interface WithStorageCapacity {
            /**
             * Sets the storage limit for the SQL Azure Database Elastic Pool in Bytes.
             *
             * @deprecated use specific edition instead
             * @param storageCapacity storage limit for the SQL Azure Database Elastic Pool in Bytes
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPoolOperations.DefinitionStages.WithCreate withStorageCapacity(Long storageCapacity);
        }

        /** The SQL Elastic Pool definition to add the Database in the Elastic Pool. */
        interface WithDatabase {
            /**
             * Creates a new database in the SQL elastic pool.
             *
             * @param databaseName name of the new database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithCreate withNewDatabase(String databaseName);

            /**
             * Adds an existing database in the SQL elastic pool.
             *
             * @param databaseName name of the existing database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithCreate withExistingDatabase(String databaseName);

            /**
             * Adds the database in the SQL elastic pool.
             *
             * @param database database instance to be added in SQL elastic pool
             * @return The next stage of the definition.
             */
            SqlElasticPoolOperations.DefinitionStages.WithCreate withExistingDatabase(SqlDatabase database);

            /**
             * Begins the definition of a new SQL Database to be added to this server.
             *
             * @param databaseName the name of the new SQL Database
             * @return the first stage of the new SQL Database definition
             */
            SqlDatabase.DefinitionStages.WithExistingDatabaseAfterElasticPool<WithCreate> defineDatabase(
                String databaseName);
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new SQL Elastic Pool in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends SqlElasticPoolOperations.DefinitionStages.WithDatabaseDtuMin,
                SqlElasticPoolOperations.DefinitionStages.WithDatabaseDtuMax,
                SqlElasticPoolOperations.DefinitionStages.WithDtu,
                SqlElasticPoolOperations.DefinitionStages.WithStorageCapacity,
                SqlElasticPoolOperations.DefinitionStages.WithDatabase,
                Resource.DefinitionWithTags<SqlElasticPoolOperations.DefinitionStages.WithCreate>,
                Creatable<SqlElasticPool> {
        }
    }

    /** Grouping of the Azure SQL Elastic Pool common actions. */
    interface SqlElasticPoolActionsDefinition extends SqlChildrenActionsDefinition<SqlElasticPool> {
        /**
         * Begins the definition of a new SQL Elastic Pool to be added to this server.
         *
         * @param elasticPoolName the name of the new SQL Elastic Pool
         * @return the first stage of the new SQL Elastic Pool definition
         */
        SqlElasticPoolOperations.DefinitionStages.WithEdition define(String elasticPoolName);
    }
}
