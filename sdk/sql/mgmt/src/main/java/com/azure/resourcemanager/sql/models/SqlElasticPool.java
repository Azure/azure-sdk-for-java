// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.sql.fluent.inner.ElasticPoolInner;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Elastic Pool. */
@Fluent
public interface SqlElasticPool
    extends ExternalChildResource<SqlElasticPool, SqlServer>,
        HasInner<ElasticPoolInner>,
        HasResourceGroup,
        Refreshable<SqlElasticPool>,
        Updatable<SqlElasticPool.Update> {

    /** @return name of the SQL Server to which this elastic pool belongs */
    String sqlServerName();

    /** @return the creation date of the Azure SQL Elastic Pool */
    OffsetDateTime creationDate();

    /** @return the state of the Azure SQL Elastic Pool */
    ElasticPoolState state();

    /** @return the edition of Azure SQL Elastic Pool */
    ElasticPoolEdition edition();

    /** @return The total shared DTU for the SQL Azure Database Elastic Pool */
    int dtu();

    /** @return the maximum DTU any one SQL Azure database can consume. */
    Double databaseDtuMax();

    /** @return the minimum DTU all SQL Azure Databases are guaranteed */
    Double databaseDtuMin();

    /** @return the storage capacity limit for the SQL Azure Database Elastic Pool in Bytes */
    Long storageCapacity();

    /** @return the parent SQL server ID */
    String parentId();

    /** @return the name of the region the resource is in */
    String regionName();

    /** @return the region the resource is in */
    Region region();

    // Actions

    /** @return the information about elastic pool activities */
    List<ElasticPoolActivity> listActivities();

    /** @return a representation of the deferred computation of the information about elastic pool activities */
    PagedFlux<ElasticPoolActivity> listActivitiesAsync();

    /** @return the information about elastic pool database activities */
    List<ElasticPoolDatabaseActivity> listDatabaseActivities();

    /** @return the information about elastic pool database activities */
    PagedFlux<ElasticPoolDatabaseActivity> listDatabaseActivitiesAsync();

    /**
     * Lists the database metrics for this SQL Elastic Pool.
     *
     * @param filter an OData filter expression that describes a subset of metrics to return
     * @return the elastic pool's database metrics
     */
    List<SqlDatabaseMetric> listDatabaseMetrics(String filter);

    /**
     * Asynchronously lists the database metrics for this SQL Elastic Pool.
     *
     * @param filter an OData filter expression that describes a subset of metrics to return
     * @return a representation of the deferred computation of this call
     */
    PagedFlux<SqlDatabaseMetric> listDatabaseMetricsAsync(String filter);

    /**
     * Lists the database metric definitions for this SQL Elastic Pool.
     *
     * @return the elastic pool's metric definitions
     */
    List<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitions();

    /**
     * Asynchronously lists the database metric definitions for this SQL Elastic Pool.
     *
     * @return a representation of the deferred computation of this call
     */
    PagedFlux<SqlDatabaseMetricDefinition> listDatabaseMetricDefinitionsAsync();

    /**
     * Lists the SQL databases in this SQL Elastic Pool.
     *
     * @return the information about databases in elastic pool
     */
    List<SqlDatabase> listDatabases();

    /**
     * Asynchronously lists the SQL databases in this SQL Elastic Pool.
     *
     * @return a representation of the deferred computation of this call
     */
    PagedFlux<SqlDatabase> listDatabasesAsync();

    /**
     * Gets the specific database in the elastic pool.
     *
     * @param databaseName name of the database to look into
     * @return the information about specific database in elastic pool
     */
    SqlDatabase getDatabase(String databaseName);

    /**
     * Adds a new SQL Database to the Elastic Pool.
     *
     * @param databaseName name of the database
     * @return the database
     */
    SqlDatabase addNewDatabase(String databaseName);

    /**
     * Adds an existing SQL Database to the Elastic Pool.
     *
     * @param databaseName name of the database
     * @return the database
     */
    SqlDatabase addExistingDatabase(String databaseName);

    /**
     * Adds an existing SQL Database to the Elastic Pool.
     *
     * @param database the database to be added
     * @return the database
     */
    SqlDatabase addExistingDatabase(SqlDatabase database);

    /**
     * Removes an existing SQL Database from the Elastic Pool.
     *
     * @param databaseName name of the database
     * @return the database
     */
    SqlDatabase removeDatabase(String databaseName);

    /** Deletes this SQL Elastic Pool from the parent SQL server. */
    void delete();

    /**
     * Deletes this SQL Elastic Pool asynchronously from the parent SQL server.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> deleteAsync();

    /**************************************************************
     * Fluent interfaces to provision a SQL Elastic Pool
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface SqlElasticPoolDefinition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithEdition<ParentT>,
            DefinitionStages.WithBasicEdition<ParentT>,
            DefinitionStages.WithStandardEdition<ParentT>,
            DefinitionStages.WithPremiumEdition<ParentT>,
            DefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of all the storage account definition stages. */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends SqlElasticPool.DefinitionStages.WithEdition<ParentT> {
        }

        /**
         * The SQL Elastic Pool definition to set the edition for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithEdition<ParentT> {
            /**
             * Sets the edition for the SQL Elastic Pool.
             *
             * @deprecated use specific edition instead
             * @param edition edition to be set for elastic pool.
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withEdition(ElasticPoolEdition edition);

            /**
             * Sets a custom sku for the SQL Elastic Pool.
             *
             * @param sku sku/edition to be set for elastic pool, all possible capabilities could be found by
             *     Sqlservers.getCapabilitiesByRegion(Region)
             * @return The next stage of the definition
             */
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withCustomEdition(Sku sku);

            /**
             * Sets the basic edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithBasicEdition<ParentT> withBasicPool();

            /**
             * Sets the standard edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithStandardEdition<ParentT> withStandardPool();

            /**
             * Sets the premium edition for the SQL Elastic Pool.
             *
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithPremiumEdition<ParentT> withPremiumPool();
        }

        /**
         * The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a basic pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithBasicEdition<ParentT> extends SqlElasticPool.DefinitionStages.WithAttach<ParentT> {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithBasicEdition<ParentT> withReservedDtu(SqlElasticPoolBasicEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithBasicEdition<ParentT> withDatabaseDtuMax(
                SqlElasticPoolBasicMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithBasicEdition<ParentT> withDatabaseDtuMin(
                SqlElasticPoolBasicMinEDTUs eDTU);
        }

        /**
         * The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a standard pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStandardEdition<ParentT> extends SqlElasticPool.DefinitionStages.WithAttach<ParentT> {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithStandardEdition<ParentT> withReservedDtu(
                SqlElasticPoolStandardEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithStandardEdition<ParentT> withDatabaseDtuMax(
                SqlElasticPoolStandardMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithStandardEdition<ParentT> withDatabaseDtuMin(
                SqlElasticPoolStandardMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithStandardEdition<ParentT> withStorageCapacity(
                SqlElasticPoolStandardStorage storageCapacity);
        }

        /**
         * The SQL Elastic Pool definition to set the eDTU and storage capacity limits for a premium pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPremiumEdition<ParentT> extends SqlElasticPool.DefinitionStages.WithAttach<ParentT> {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithPremiumEdition<ParentT> withReservedDtu(
                SqlElasticPoolPremiumEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithPremiumEdition<ParentT> withDatabaseDtuMax(
                SqlElasticPoolPremiumMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithPremiumEdition<ParentT> withDatabaseDtuMin(
                SqlElasticPoolPremiumMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithPremiumEdition<ParentT> withStorageCapacity(
                SqlElasticPoolPremiumSorage storageCapacity);
        }

        /**
         * The SQL Elastic Pool definition to set the minimum DTU for database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDatabaseDtuMin<ParentT> {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMin minimum DTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withDatabaseDtuMin(double databaseDtuMin);
        }

        /**
         * The SQL Elastic Pool definition to set the maximum DTU for one database.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDatabaseDtuMax<ParentT> {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMax maximum DTU any one SQL Azure Database can consume
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withDatabaseDtuMax(double databaseDtuMax);
        }

        /**
         * The SQL Elastic Pool definition to set the number of shared DTU for elastic pool.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDtu<ParentT> {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @deprecated use specific edition instead
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withDtu(int dtu);
        }

        /**
         * The SQL Elastic Pool definition to set the storage limit for the SQL Azure Database Elastic Pool in Bytes.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithStorageCapacity<ParentT> {
            /**
             * Sets the storage limit for the SQL Azure Database Elastic Pool in Bytes.
             *
             * @deprecated use specific edition instead
             * @param storageCapacity storage limit for the SQL Azure Database Elastic Pool in Bytes
             * @return The next stage of the definition.
             */
            @Deprecated
            SqlElasticPool.DefinitionStages.WithAttach<ParentT> withStorageCapacity(Long storageCapacity);
        }

        /**
         * The final stage of the SQL Elastic Pool definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the SQL Elastic Pool definition can be
         * attached to the parent SQL Server definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithAttach<ParentT>
            extends WithDatabaseDtuMin<ParentT>,
                WithDatabaseDtuMax<ParentT>,
                WithDtu<ParentT>,
                WithStorageCapacity<ParentT>,
                Attachable.InDefinition<ParentT> {
        }
    }

    /** The template for a SQL Elastic Pool update operation, containing all the settings that can be modified. */
    interface Update
        extends UpdateStages.WithReservedDTUAndStorageCapacity,
            UpdateStages.WithDatabaseDtuMax,
            UpdateStages.WithDatabaseDtuMin,
            UpdateStages.WithDtu,
            UpdateStages.WithStorageCapacity,
            UpdateStages.WithDatabase,
            Resource.UpdateWithTags<SqlElasticPool.Update>,
            Appliable<SqlElasticPool> {
    }

    /** Grouping of all the SQL Elastic Pool update stages. */
    interface UpdateStages {

        /** The SQL Elastic Pool definition to set the minimum DTU for database. */
        interface WithDatabaseDtuMin {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMin minimum DTU for all SQL Azure databases
             * @return The next stage of definition.
             */
            @Deprecated
            Update withDatabaseDtuMin(double databaseDtuMin);
        }

        /** The SQL Elastic Pool definition to set the maximum DTU for one database. */
        interface WithDatabaseDtuMax {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @deprecated use specific edition instead
             * @param databaseDtuMax maximum DTU any one SQL Azure Database can consume
             * @return The next stage of definition.
             */
            @Deprecated
            Update withDatabaseDtuMax(double databaseDtuMax);
        }

        /** The SQL Elastic Pool definition to set the number of shared DTU for elastic pool. */
        interface WithDtu {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @deprecated use specific edition instead
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return The next stage of definition.
             */
            @Deprecated
            Update withDtu(int dtu);
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
             * @return The next stage of definition.
             */
            @Deprecated
            Update withStorageCapacity(Long storageCapacity);
        }

        /** The SQL Elastic Pool update definition to set the eDTU and storage capacity limits. */
        interface WithReservedDTUAndStorageCapacity {
            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the update definition.
             */
            Update withReservedDtu(SqlElasticPoolBasicEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMax(SqlElasticPoolBasicMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMin(SqlElasticPoolBasicMinEDTUs eDTU);

            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the update definition.
             */
            Update withReservedDtu(SqlElasticPoolStandardEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMax(SqlElasticPoolStandardMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMin(SqlElasticPoolStandardMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the update definition.
             */
            Update withStorageCapacity(SqlElasticPoolStandardStorage storageCapacity);

            /**
             * Sets the total shared eDTU for the SQL Azure Database Elastic Pool.
             *
             * @param eDTU total shared eDTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the update definition.
             */
            Update withReservedDtu(SqlElasticPoolPremiumEDTUs eDTU);

            /**
             * Sets the maximum number of eDTU a database in the pool can consume.
             *
             * @param eDTU maximum eDTU a database in the pool can consume
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMax(SqlElasticPoolPremiumMaxEDTUs eDTU);

            /**
             * Sets the minimum number of eDTU for each database in the pool are regardless of its activity.
             *
             * @param eDTU minimum eDTU for all SQL Azure databases
             * @return The next stage of the update definition.
             */
            Update withDatabaseDtuMin(SqlElasticPoolPremiumMinEDTUs eDTU);

            /**
             * Sets the storage capacity for the SQL Azure Database Elastic Pool.
             *
             * @param storageCapacity storage capacity for the SQL Azure Database Elastic Pool
             * @return The next stage of the update definition.
             */
            Update withStorageCapacity(SqlElasticPoolPremiumSorage storageCapacity);
        }

        /** The SQL Elastic Pool definition to add the Database in the elastic pool. */
        interface WithDatabase {
            /**
             * Creates a new database in the SQL elastic pool.
             *
             * @param databaseName name of the new database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            Update withNewDatabase(String databaseName);

            /**
             * Adds an existing database in the SQL elastic pool.
             *
             * @param databaseName name of the existing database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            Update withExistingDatabase(String databaseName);

            /**
             * Adds the database in the SQL elastic pool.
             *
             * @param database database instance to be added in SQL elastic pool
             * @return The next stage of the definition.
             */
            Update withExistingDatabase(SqlDatabase database);
        }
    }
}
