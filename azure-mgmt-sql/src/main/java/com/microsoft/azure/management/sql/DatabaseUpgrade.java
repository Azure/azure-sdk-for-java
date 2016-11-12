/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;


/**
 * An immutable client-side representation of an Azure SQL DatabaseUpgrade.
 */
@Fluent
public interface DatabaseUpgrade extends
        Wrapper<RecommendedDatabaseProperties> {

    /**
     * Interface definition for the upgrade flow with all methods.
     */
    interface UpgradeDefinition extends
            UpgradeDefinitions.Blank,
            UpgradeDefinitions.WithTargetEdition,
            UpgradeDefinitions.WithTargetServiceLevelObjective,
            UpgradeDefinitions.Attach {
    }
    /**
     * Collection of all definitions for upgrade operation.
     */
    interface UpgradeDefinitions {
        /**
         * First stage of the interface to start upgrade of ElasticPool.
         */
        interface Blank extends Attach {
        }

        /**
         * Stage to specify the target edition for the Azure SQL Database being upgraded.
         */
        interface WithTargetEdition {
            /**
             * Sets the target edition for the Azure SQL Database being upgraded.
             *
             * @param targetEdition target edition for the database.
             * @return Next stage of the upgrade operation
             */
            WithTargetServiceLevelObjective withTargetEdition(TargetDatabaseEditions targetEdition);
        }

        /**
         * Stage to specify the target Service Level Objective for the Azure SQL Database being
         * upgraded.
         */
        interface WithTargetServiceLevelObjective {
            /**
             * Sets the target Service Level Objective for the Azure SQL Database being
             * upgraded.
             *
             * @param targetServiceLevelObjective target objective level for the SQL database
             * @return Next stage of the upgrade operation
             */
            Attach withTargetServiceLevelObjective(String targetServiceLevelObjective);
        }

        /**
         * Last stage of the upgrade operation with all the optional properties.
         */
        interface Attach extends
                WithTargetEdition,
                WithTargetServiceLevelObjective {
            /**
             * @return the next stage in server upgrade.
             */
            ServerUpgrade.UpgradeDefinitions.Schedule attach();
        }
    }
}

