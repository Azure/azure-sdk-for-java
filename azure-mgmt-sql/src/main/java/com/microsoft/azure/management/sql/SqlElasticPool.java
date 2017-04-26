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
import com.microsoft.azure.management.sql.implementation.ElasticPoolInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;

import org.joda.time.DateTime;

import java.util.List;

/**
 * An immutable client-side representation of an Azure SQL ElasticPool.
 */
@Fluent
public interface SqlElasticPool extends
        IndependentChildResource<SqlServerManager, ElasticPoolInner>,
        Refreshable<SqlElasticPool>,
        Updatable<SqlElasticPool.Update> {

    /**
     * @return name of the SQL Server to which this elastic pool belongs
     */
    String sqlServerName();

    /**
     * @return the creation date of the Azure SQL Elastic Pool
     */
    DateTime creationDate();

    /**
     * @return the state of the Azure SQL Elastic Pool
     */
    ElasticPoolState state();

    /**
     * @return the edition of Azure SQL Elastic Pool
     */
    ElasticPoolEditions edition();

    /**
     * @return The total shared DTU for the SQL Azure Database Elastic Pool
     */
    int dtu();

    /**
     * @return the maximum DTU any one SQL Azure database can consume.
     */
    int databaseDtuMax();

    /**
     * @return the minimum DTU all SQL Azure Databases are guaranteed
     */
    int databaseDtuMin();

    /**
     * @return the storage limit for the SQL Azure Database Elastic Pool in MB
     */
    int storageMB();

    /**
     * @return the information about elastic pool activities
     */
    List<ElasticPoolActivity> listActivities();

    /**
     * @return the information about elastic pool database activities
     */
    List<ElasticPoolDatabaseActivity> listDatabaseActivities();

    /**
     * @return the information about databases in elastic pool
     */
    List<SqlDatabase> listDatabases();

    /**
     * Gets the specific database in the elastic pool.
     *
     * @param databaseName name of the database to look into
     * @return the information about specific database in elastic pool
     */
    SqlDatabase getDatabase(String databaseName);

    /**
     * Deletes the elastic pool from the server.
     */
    void delete();

    /**************************************************************
     * Fluent interfaces to provision a Sql Elastic pool
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
            DefinitionStages.Blank,
            DefinitionStages.WithEdition,
            DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         */
        interface Blank extends SqlElasticPool.DefinitionStages.WithEdition {
        }

        /**
         * The SQL Elastic Pool definition to set the edition for database.
         */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Elastic Pool.
             *
             * @param edition edition to be set for elastic pool.
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withEdition(ElasticPoolEditions edition);
        }

        /**
         * The SQL Elastic Pool definition to set the minimum DTU for database.
         */
        interface WithDatabaseDtuMin {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @param databaseDtuMin minimum DTU for all SQL Azure databases
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withDatabaseDtuMin(int databaseDtuMin);
        }

        /**
         * The SQL Elastic Pool definition to set the maximum DTU for one database.
         */
        interface WithDatabaseDtuMax {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @param databaseDtuMax maximum DTU any one SQL Azure Database can consume
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withDatabaseDtuMax(int databaseDtuMax);
        }

        /**
         * The SQL Elastic Pool definition to set the number of shared DTU for elastic pool.
         */
        interface WithDtu {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withDtu(int dtu);
        }

        /**
         * The SQL Elastic Pool definition to set the storage limit for the SQL Azure Database Elastic Pool in MB.
         */
        interface WithStorageCapacity {
            /**
             * Sets the storage limit for the SQL Azure Database Elastic Pool in MB.
             *
             * @param storageMB storage limit for the SQL Azure Database Elastic Pool in MB
             * @return The next stage of the definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withStorageCapacity(int storageMB);
        }

        /**
         * The SQL Elastic Pool definition to add the Database in the elastic pool.
         */
        interface WithDatabase {
            /**
             * Creates a new database in the SQL elastic pool.
             *
             * @param databaseName name of the new database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            WithCreate withNewDatabase(String databaseName);

            /**
             * Adds an existing database in the SQL elastic pool.
             *
             * @param databaseName name of the existing database to be added in the elastic pool
             * @return The next stage of the definition.
             */
            WithCreate withExistingDatabase(String databaseName);

            /**
             * Adds the database in the SQL elastic pool.
             *
             * @param database database instance to be added in SQL elastic pool
             * @return The next stage of the definition.
             */
            WithCreate withExistingDatabase(SqlDatabase database);
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<SqlElasticPool>,
                DefinitionWithTags<WithCreate>,
                WithDatabaseDtuMin,
                WithDatabaseDtuMax,
                WithDtu,
                WithStorageCapacity,
                WithDatabase {
        }
    }

    /**
     * The template for a SQLElasticPool update operation, containing all the settings that can be modified.
     */
    interface Update extends
            UpdateStages.WithDatabaseDtuMax,
            UpdateStages.WithDatabaseDtuMin,
            UpdateStages.WithDtu,
            UpdateStages.WithStorageCapacity,
            UpdateStages.WithDatabase,
            Appliable<SqlElasticPool> {
    }

    /**
     * Grouping of all the SQLElasticPool update stages.
     */
    interface UpdateStages {

        /**
         * The SQL Elastic Pool definition to set the minimum DTU for database.
         */
        interface WithDatabaseDtuMin {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @param databaseDtuMin minimum DTU for all SQL Azure databases
             * @return The next stage of definition.
             */
            Update withDatabaseDtuMin(int databaseDtuMin);
        }

        /**
         * The SQL Elastic Pool definition to set the maximum DTU for one database.
         */
        interface WithDatabaseDtuMax {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @param databaseDtuMax maximum DTU any one SQL Azure Database can consume
             * @return The next stage of definition.
             */
            Update withDatabaseDtuMax(int databaseDtuMax);
        }

        /**
         * The SQL Elastic Pool definition to set the number of shared DTU for elastic pool.
         */
        interface WithDtu {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return The next stage of definition.
             */
            Update withDtu(int dtu);
        }

        /**
         * The SQL Elastic Pool definition to set the storage limit for the SQL Azure Database Elastic Pool in MB.
         */
        interface WithStorageCapacity {
            /**
             * Sets the storage limit for the SQL Azure Database Elastic Pool in MB.
             *
             * @param storageMB storage limit for the SQL Azure Database Elastic Pool in MB
             * @return The next stage of definition.
             */
            Update withStorageCapacity(int storageMB);
        }

        /**
         * The SQL Elastic Pool definition to add the Database in the elastic pool.
         */
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
