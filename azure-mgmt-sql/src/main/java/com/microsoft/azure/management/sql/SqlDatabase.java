/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.DatabaseInner;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * An immutable client-side representation of an Azure SQL Database.
 */
@Fluent
public interface SqlDatabase extends
        IndependentChildResource,
        Refreshable<SqlDatabase>,
        Updatable<SqlDatabase.Update>,
        Wrapper<DatabaseInner> {
    /**
     * @return name of the SQL Server to which this database belongs
     */
    String sqlServerName();

    /**
     * @return the collation of the Azure SQL Database
     */
    String collation();

    /**
     * @return the creation date of the Azure SQL Database
     */
    DateTime creationDate();

    /**
     * @return the current Service Level Objective Id of the Azure SQL Database, this is the Id of the
     * Service Level Objective that is currently active
     */
    UUID currentServiceObjectiveId();


    /**
     * @return the Id of the Azure SQL Database
     */
    String databaseId();

    /**
     * @return the recovery period start date of the Azure SQL Database. This
     * records the start date and time when recovery is available for this
     * Azure SQL Database.
     */
    DateTime earliestRestoreDate();

    /**
     * @return the edition of the Azure SQL Database
     */
    DatabaseEditions edition();

    /**
     *
     * @return the configured Service Level Objective Id of the Azure SQL
     * Database, this is the Service Level Objective that is being applied to
     * the Azure SQL Database
     */
    UUID requestedServiceObjectiveId();

    /**
     * @return the max size of the Azure SQL Database expressed in bytes.
     */
    long maxSizeBytes();

    /**
     * @return the name of the configured Service Level Objective of the Azure
     * SQL Database, this is the Service Level Objective that is being
     * applied to the Azure SQL Database
     */
    ServiceObjectiveName requestedServiceObjectiveName();

    /**
     * @return the Service Level Objective of the Azure SQL Database.
     */
    ServiceObjectiveName serviceLevelObjective();

    /**
     * @return the status of the Azure SQL Database
     */
    String status();

    /**
     * @return the elasticPoolName value
     */
    String elasticPoolName();

    /**
     * @return the defaultSecondaryLocation value
     */
    String defaultSecondaryLocation();

    /**
     * @return the upgradeHint value
     */
    UpgradeHintInterface getUpgradeHint();

    /**
     * @return true if this Database is SqlWarehouse
     */
    boolean isDataWarehouse();

    /**
     * @return SqlWarehouse instance for more operations
     */
    SqlWarehouse castToWarehouse();

    /**
     * @return returns the list of all restore points on the database
     */
    List<RestorePoint> listRestorePoints();

    /**
     * @return returns the list of usages (DatabaseMetrics) of the database
     */
    List<DatabaseMetric> listUsages();

    /**
     * Gets an Azure SQL Database Transparent Data Encryption for the database.
     *
     * @return an Azure SQL Database Transparent Data Encryption for the database
     */
    TransparentDataEncryption getTransparentDataEncryption();

    /**
     * @return information about service tier advisors for specified database
     */
    Map<String, ServiceTierAdvisor> listServiceTierAdvisors();

    /**
     * @return all the replication links associated with the database
     */
    Map<String, ReplicationLink> listReplicationLinks();

    /**
     * Deletes the existing SQL database.
     */
    void delete();

    /**************************************************************
     * Fluent interfaces to provision a Sql Database
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithCreate,
        DefinitionStages.WithCollation,
        DefinitionStages.WithEdition,
        DefinitionStages.WithElasticPoolName,
        DefinitionStages.WithSourceDatabaseId,
        DefinitionStages.WithCreateMode,
        DefinitionStages.WithCreateWithLessOptions,
        DefinitionStages.WithExistingDatabase {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         */
        interface Blank extends WithElasticPoolName {
        }

        /**
         * The SQL Database definition to set the elastic pool for database.
         */
        interface WithElasticPoolName {
            /**
             * Specifies database to be created without elastic pool.
             *
             * @return The next stage of the definition.
             */
            WithExistingDatabase withoutElasticPool();

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolName for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabase withExistingElasticPool(String elasticPoolName);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param sqlElasticPool for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabase withExistingElasticPool(SqlElasticPool sqlElasticPool);

            /**
             * Sets the new elastic pool for the SQLDatabase, this will create a new elastic pool while creating database.
             *
             * @param sqlElasticPool creatable definition for new elastic pool to be created for the SQL Database
             * @return The next stage of the definition.
             */
            WithExistingDatabase withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool);
        }

        /**
         * The stage to decide whether using existing database or not.
         */
        interface WithExistingDatabase extends WithSourceDatabaseId {
            /**
             * Sets the creation flow to ask relevant question when source database is not specified.
             *
             * @return The next stage of the definition.
             */
            WithCreate withoutSourceDatabaseId();
        }

        /**
         * The SQL Database definition to set the source database id for database.
         */
        interface WithSourceDatabaseId {
            /**
             * Sets the resource if of source database for the SQL Database.
             * Collation, Edition, and MaxSizeBytes must remain the same while the link is
             * active. Values specified for these parameters will be ignored.
             *
             * @param sourceDatabaseId id of the source database
             * @return The next stage of the definition.
             */
            WithCreateMode withSourceDatabase(String sourceDatabaseId);

            /**
             * Sets the resource if of source database for the SQL Database.
             * Collation, Edition, and MaxSizeBytes must remain the same while the link is
             * active. Values specified for these parameters will be ignored.
             *
             * @param sourceDatabase instance of the source database
             * @return The next stage of the definition.
             */
            WithCreateMode withSourceDatabase(SqlDatabase sourceDatabase);
        }

        /**
         * The SQL Database definition to set the create mode for database.
         */
        interface WithCreateMode {
            /**
             * Sets the create mode for the SQL Database.
             *
             * @param createMode create mode for the database, should not be default in this flow
             * @return The next stage of the definition.
             */
            WithCreateWithLessOptions withMode(CreateMode createMode);
        }

        /**
         * The SQL Database definition to set the collation for database.
         */
        interface WithCollation {
            /**
             * Sets the collation for the SQL Database.
             *
             * @param collation collation to be set for database
             * @return The next stage of the definition
             */
            WithCreate withCollation(String collation);
        }

        /**
         * The SQL Database definition to set the edition for database.
         */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Database.
             *
             * @param edition edition to be set for database
             * @return The next stage of the definition
             */
            WithCreate withEdition(DatabaseEditions edition);
        }

        /**
         * The SQL Database definition to set the Max Size in Bytes for database.
         */
        interface WithMaxSizeBytes {
            /**
             * Sets the max size in bytes for SQL Database.
             *
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only
             * the following sizes are supported (in addition to limitations being
             * placed on each edition): { 100 MB | 500 MB |1 GB | 5 GB | 10 GB | 20
             * GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the definition.
             */
            WithCreate withMaxSizeBytes(long maxSizeBytes);
        }

        /**
         * The SQL Database definition to set the service level objective.
         */
        interface WithServiceObjective {
            /**
             * Sets the service level objective for the SQL Database.
             *
             * @param serviceLevelObjective service level objected for the SQL Database
             * @return The next stage of the definition.
             */
            WithCreate withServiceObjective(ServiceObjectiveName serviceLevelObjective);
        }

        /**
         * A SQL Database definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                WithMaxSizeBytes,
                WithServiceObjective,
                WithCollation,
                WithEdition,
                WithCreateWithLessOptions {
        }

        /**
         * A SQL Database definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreateWithLessOptions extends
            Creatable<SqlDatabase>,
            DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for a SQLDatabase modifyState operation, containing all the settings that can be modified.
     */
    interface Update extends
            UpdateStages.WithEdition,
            UpdateStages.WithElasticPoolName,
            UpdateStages.WithMaxSizeBytes,
            UpdateStages.WithServiceObjective,
            Appliable<SqlDatabase> {
    }

    /**
     * Grouping of all the SQLDatabase modifyState stages.
     */
    interface UpdateStages {
        /**
         * The SQL Database definition to set the edition for database.
         */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Database.
             *
             * @param edition edition to be set for database
             * @return The next stage of the update.
             */
            Update withEdition(DatabaseEditions edition);
        }

        /**
         * The SQL Database definition to set the Max Size in Bytes for database.
         */
        interface WithMaxSizeBytes {
            /**
             * Sets the max size in bytes for SQL Database.
             * @param maxSizeBytes max size of the Azure SQL Database expressed in bytes. Note: Only
             * the following sizes are supported (in addition to limitations being
             * placed on each edition): { 100 MB | 500 MB |1 GB | 5 GB | 10 GB | 20
             * GB | 30 GB … 150 GB | 200 GB … 500 GB }
             * @return The next stage of the update.
             */
            Update withMaxSizeBytes(long maxSizeBytes);
        }

        /**
         * The SQL Database definition to set the service level objective.
         */
        interface WithServiceObjective {
            /**
             * Sets the service level objective for the SQL Database.
             *
             * @param serviceLevelObjective service level objected for the SQL Database
             * @return The next stage of the update.
             */
            Update withServiceObjective(ServiceObjectiveName serviceLevelObjective);
        }

        /**
         * The SQL Database definition to set the elastic pool for database.
         */
        interface WithElasticPoolName {
            /**
             * Removes database from it's elastic pool.
             *
             * @return The next stage of the update.
             */
            WithEdition withoutElasticPool();

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param elasticPoolName for the SQL Database
             * @return The next stage of the update.
             */
            Update withExistingElasticPool(String elasticPoolName);

            /**
             * Sets the existing elastic pool for the SQLDatabase.
             *
             * @param sqlElasticPool for the SQL Database
             * @return The next stage of the update.
             */
            Update withExistingElasticPool(SqlElasticPool sqlElasticPool);

            /**
             * Sets the new elastic pool for the SQLDatabase, this will create a new elastic pool while creating database.
             *
             * @param sqlElasticPool creatable definition for new elastic pool to be created for the SQL Database
             * @return The next stage of the update.
             */
            Update withNewElasticPool(Creatable<SqlElasticPool> sqlElasticPool);
        }
    }
}

