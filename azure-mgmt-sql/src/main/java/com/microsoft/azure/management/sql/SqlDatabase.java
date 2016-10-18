/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.sql.implementation.DatabaseInner;
import com.microsoft.azure.management.sql.implementation.RecommendedIndexInner;
import com.microsoft.azure.management.sql.implementation.SchemaInner;
import com.microsoft.azure.management.sql.implementation.ServiceTierAdvisorInner;
import com.microsoft.azure.management.sql.implementation.TransparentDataEncryptionInner;
import com.microsoft.azure.management.sql.implementation.UpgradeHintInner;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;


/**
 * An immutable client-side representation of an Azure SQL Server.
 */
@Fluent
public interface SqlDatabase extends
        IndependentChildResource,
        Refreshable<SqlDatabase>,
        Updatable<SqlDatabase.Update>,
        Wrapper<DatabaseInner> {

    /**
     * @return the SQL Server name in which this database resides.
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
    String requestedServiceObjectiveName();

    /**
     * @return the Service Level Objective of the Azure SQL Database.
     */
    String serviceLevelObjective();

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
     * @return the serviceTierAdvisors value
     */
    List<ServiceTierAdvisorInner> serviceTierAdvisors();

    /**
     * @return the upgradeHint value
     */
    UpgradeHintInner upgradeHint();

    /**
     * @return the schemas value
     */
    List<SchemaInner> schemas();

    /**
     * @return the transparentDataEncryption value
     */
    List<TransparentDataEncryptionInner> transparentDataEncryption();

    /**
     * @return the recommendedIndex value
     */
    List<RecommendedIndexInner> recommendedIndex();

    /**************************************************************
     * Fluent interfaces to provision a SqlServer
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithCreate,
        DefinitionStages.WithCollation,
        DefinitionStages.WithEdition,
        DefinitionStages.WithElasticPoolName {
    }

    /**
     * Grouping of all the storage account definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the SQL Server definition.
         */
        interface Blank extends WithCollation {
        }

        /**
         * The SQL Database definition to set the collation for database.
         */
        interface WithCollation {
            WithEdition withCollation(String collation);
        }

        /**
         * The SQL Database definition to set the edition for database.
         */
        interface WithEdition {
            /**
             * Sets the edition for the SQL Database.
             * @param edition edition to be set for database.
             * @return The next stage of definition.
             */
            WithCreate withEdition(DatabaseEditions edition);
        }

        /**
         * The SQL Database definition to set the elastic pool for database.
         */
        interface WithElasticPoolName {
            /**
             * Sets the existing elastic pool for the SQLDatabase.
             * @param elasticPoolName of for the SQL Database.
             * @return The next stage of definition.
             */
            WithCreate withExistingElasticPoolName(String elasticPoolName);
        }

        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
            IndependentChildResource.DefinitionStages.WithParentResource<SqlDatabase, SqlServer>,
            DefinitionWithTags<WithCreate>,
            WithElasticPoolName {
        }
    }

    /**
     * The template for a SQLDatabase update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<SqlDatabase> {
    }

    /**
     * Grouping of all the SQLDatabase update stages.
     */
    interface UpdateStages {
    }
}

