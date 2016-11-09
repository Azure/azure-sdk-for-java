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
import com.microsoft.azure.management.sql.implementation.ElasticPoolInner;
import org.joda.time.DateTime;

/**
 * An immutable client-side representation of an Azure SQL ElasticPool.
 */
@Fluent
public interface SqlElasticPool extends
        IndependentChildResource,
        Refreshable<SqlElasticPool>,
        Updatable<SqlElasticPool.Update>,
        Wrapper<ElasticPoolInner> {

    /**
     * @return the SQL Server name to which this elastic pool belongs
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
             * @return The next stage of definition.
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
             * @return The next stage of definition.
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
             * @return The next stage of definition.
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
             * @return The next stage of definition.
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
             * @return The next stage of definition.
             */
            SqlElasticPool.DefinitionStages.WithCreate withStorageCapacity(int storageMB);
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
                WithStorageCapacity {
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
    }
}
