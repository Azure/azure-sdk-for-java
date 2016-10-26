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
         * A resource definition allowing SQLServer to be attached with SQLDatabase.
         */
        interface WithSqlServer {
            /**
             * Creates a new SqlElasticPool resource under SQLServer.
             *
             * @param groupName the name of the resource group for SQLServer.
             * @param sqlServerName the name of the sQLServer.
             * @return the creatable for the child resource
             */
            Creatable<SqlElasticPool> withExistingSqlServer(String groupName, String sqlServerName);

            /**
             * Creates a new SqlElasticPool resource under SQLServer.
             *
             * @param sqlServerCreatable a creatable definition for the SQLServer
             * @return the creatable for the SQLDatabase
             */
            Creatable<SqlElasticPool> withNewSqlServer(Creatable<SqlServer> sqlServerCreatable);

            /**
             * Creates a new SqlElasticPool resource under SQLServer.
             *
             * @param existingSqlServer the SQLServer under which this SqlElasticPool to be created.
             * @return the creatable for the SQLDatabase
             */
            Creatable<SqlElasticPool> withExistingSqlServer(SqlServer existingSqlServer);
        }
        /**
         * A SQL Server definition with sufficient inputs to create a new
         * SQL Server in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                WithSqlServer,
                DefinitionWithTags<SqlElasticPool.DefinitionStages.WithCreate> {
        }
    }

    /**
     * The template for a SQLElasticPool update operation, containing all the settings that can be modified.
     */
    interface Update extends
            Appliable<SqlElasticPool> {
    }

    /**
     * Grouping of all the SQLElasticPool update stages.
     */
    interface UpdateStages {
    }
}
