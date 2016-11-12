/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.ServerUpgradeStartParametersInner;
import org.joda.time.DateTime;


/**
 * An immutable client-side representation of an Azure SQL ServerUpgrade.
 */
@Fluent
public interface ServerUpgrade extends
        Wrapper<ServerUpgradeStartParametersInner> {

    /**
     * Interface definition for the upgrade flow with all methods.
     */
    interface UpgradeDefinition extends
            UpgradeDefinitions.Blank,
            UpgradeDefinitions.WithScheduleUpgradeAfterUtcDateTime,
            UpgradeDefinitions.WithVersion,
            UpgradeDefinitions.WithDatabase,
            UpgradeDefinitions.WithElasticPool,
            UpgradeDefinitions.Schedule {
    }

    /**
     * Collection of all definitions for upgrade operation.
     */
    interface UpgradeDefinitions {
        /**
         * First stage of the interface to start upgrade of ElasticPool.
         */
        interface Blank extends WithScheduleUpgradeAfterUtcDateTime {
        }

        /**
         * Stage to specify the version for the Azure SQL Server being upgraded.
         */
        interface WithVersion {
            /**
             * Sets the version for the Azure SQL Server being upgraded.
             * @param version new version of the SQL Server to be upgraded too
             * @return Next stage for upgrade operation
             */
            Schedule withVersion(String version);
        }

        /**
         * Stage to specify the earliest time to upgrade the Azure SQL Server (ISO8601 format).
         */
        interface WithScheduleUpgradeAfterUtcDateTime {
            /**
             * Sets the earliest time to upgrade the Azure SQL Server (ISO8601 format).
             * @param scheduleUpgradeAfterUtcDateTime time after which the upgrade should start
             * @return  Next stage for upgrade operation
             */
            Schedule withScheduleUpgradeAfterUtcDateTime(DateTime scheduleUpgradeAfterUtcDateTime);
        }

        /**
         * Stage to specify database upgrade.
         */
        interface WithDatabase {
            /**
             * Specify the database to be upgraded.
             * @param databaseName name of the database to be upgraded
             * @return stage to specify upgrade properties for database.
             */
            DatabaseUpgrade.UpgradeDefinitions.Blank updateDatabase(String databaseName);
        }

        /**
         * Specify the elastic pool upgrade.
         */
        interface WithElasticPool {
            /**
             * Specify the elastic pool to be upgraded.
             * @param elasticPoolName name fo the elastic pool to be upgraded
             * @return stage to specify upgrade properties for elastic pool
             */
            ElasticPoolUpgrade.UpgradeDefinitions.Blank updateElasticPool(String elasticPoolName);
        }

        /**
         * Last stage of the upgrade operation with all the optional properties.
         */
        interface Schedule extends
                WithVersion,
                WithDatabase,
                WithElasticPool {
            /**
             * Schedules the upgrade with Azure SQL.
             */
            void schedule();
        }
    }
}

