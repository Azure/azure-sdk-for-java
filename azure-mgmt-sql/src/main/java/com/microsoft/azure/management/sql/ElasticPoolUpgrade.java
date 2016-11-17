/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

import java.util.List;


/**
 * An immutable client-side representation of an Azure SQL ElasticPoolUpgrade.
 */
@Fluent
public interface ElasticPoolUpgrade extends
        Wrapper<UpgradeRecommendedElasticPoolProperties> {
    /**
     * Interface definition for the upgrade flow with all methods.
     */
    interface UpgradeDefinition extends
            UpgradeDefinitions.Blank,
            UpgradeDefinitions.WithEdition,
            UpgradeDefinitions.WithDtu,
            UpgradeDefinitions.WithStorageMb,
            UpgradeDefinitions.WithDatabaseDtuMax,
            UpgradeDefinitions.WithDatabaseDtuMin,
            UpgradeDefinitions.WithDatabaseCollection,
            UpgradeDefinitions.WithIncludeAllDatabases,
            UpgradeDefinitions.Attach {
    }

    /**
     * Collection of all definitions for upgrade operation.
     */
    interface UpgradeDefinitions {
        /**
         * First stage of the interface to start upgrade of ElasticPool.
         */
        interface Blank extends WithEdition {
        }

        /**
         * Stage to specify the target edition for elastic pool.
         */
        interface WithEdition {
            /**
             * Sets the target edition for the elastic pool.
             *
             * @param targetEdition target edition for elastic pool
             * @return Next stage of the upgrade operation
             */
            Attach withEdition(TargetElasticPoolEditions targetEdition);
        }

        /**
         * Stage to specify total shared DTU for the SQL Azure Database Elastic Pool.
         */
        interface WithDtu {
            /**
             * Sets the total shared DTU for the SQL Azure Database Elastic Pool.
             *
             * @param dtu total shared DTU for the SQL Azure Database Elastic Pool
             * @return Next stage of the upgrade operation
             */
            Attach withDtu(int dtu);
        }

        /**
         * Stage to specify storage limit for the SQL Azure Database Elastic Pool in MB.
         */
        interface WithStorageMb {
            /**
             * Sets the storage limit for the SQL Azure Database Elastic Pool in MB.
             *
             * @param storageMb limit of storage in MBs
             * @return Next stage of the upgrade operation
             */
            Attach withStorageMb(int storageMb);
        }

        /**
         * Stage to specify minimum DTU all SQL Azure Databases are guaranteed.
         */
        interface WithDatabaseDtuMin {
            /**
             * Sets the minimum DTU all SQL Azure Databases are guaranteed.
             *
             * @param databaseDtuMin number of minimum DTU all SQL Azure Databases are guaranteed
             * @return Next stage of the upgrade operation
             */
            Attach withDatabaseDtuMin(int databaseDtuMin);
        }

        /**
         * Stage to specify maximum DTU any one SQL Azure Database can consume.
         */
        interface WithDatabaseDtuMax {
            /**
             * Sets the maximum DTU any one SQL Azure Database can consume.
             *
             * @param databaseDtuMax number of maximum DTU any one SQL Azure Database can consume
             * @return Next stage of the upgrade operation
             */
            Attach withDatabaseDtuMax(int databaseDtuMax);
        }

        /**
         * Stage to specify list of database names to be put in the Azure SQL
         * Elastic Pool being upgraded.
         */
        interface WithDatabaseCollection {
            /**
             * Sets the list of database names to be put in the Azure SQL Elastic Pool being upgraded.
             *
             * @param databaseCollection list of names of databases to be included in elastic pool
             * @return Next stage of the upgrade operation
             */
            Attach withDatabaseCollection(List<String> databaseCollection);
        }

        /**
         * Stage to specify whether all databases to be put in the Azure SQL Elastic Pool being upgraded.
         */
        interface WithIncludeAllDatabases {
            /**
             * Sets whether all databases to be put in the Azure SQL Elastic Pool being upgraded.
             *
             * @param includeAllDatabases whether all databases to be put in the Azure SQL Elastic Pool being upgraded
             * @return Next stage of the upgrade operation
             */
            Attach withIncludeAllDatabases(boolean includeAllDatabases);
        }

        /**
         * Last stage of the upgrade operation with all the optional properties.
         */
        interface Attach extends
                WithEdition,
                WithDtu,
                WithStorageMb,
                WithDatabaseDtuMin,
                WithDatabaseDtuMax,
                WithDatabaseCollection,
                WithIncludeAllDatabases {
            /**
             * @return the next stage in server upgrade.
             */
            ServerUpgrade.UpgradeDefinitions.Schedule attach();
        }
    }
}

