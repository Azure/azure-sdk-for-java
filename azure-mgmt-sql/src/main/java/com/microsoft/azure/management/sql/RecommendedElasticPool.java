/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasId;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.sql.implementation.RecommendedElasticPoolInner;
import com.microsoft.azure.management.sql.implementation.SqlServerManager;

import org.joda.time.DateTime;

import java.util.List;

/**
 * An immutable client-side representation of an Azure SQL Recommended ElasticPool.
 */
@Fluent
public interface RecommendedElasticPool extends
        Refreshable<RecommendedElasticPool>,
        HasInner<RecommendedElasticPoolInner>,
        HasResourceGroup,
        HasName,
        HasId,
        HasManager<SqlServerManager> {

    /**
     * @return name of the SQL Server to which this database belongs
     */
    String sqlServerName();

    /**
     * @return the edition of the Azure SQL Recommended Elastic Pool. The
     * ElasticPoolEditions enumeration contains all the valid editions.
     * Possible values include: 'Basic', 'Standard', 'Premium'.
     */
    ElasticPoolEditions databaseEdition();

    /**
     * @return the DTU for the SQL Azure Recommended Elastic Pool.
     */
    double dtu();

    /**
     * @return the minimum DTU for the database.
     */
    double databaseDtuMin();

    /**
     * @return the maximum DTU for the database.
     */
    double databaseDtuMax();

    /**
     * @return storage size in megabytes.
     */
    double storageMB();

    /**
     * @return the observation period start (ISO8601 format).
     */
    DateTime observationPeriodStart();

    /**
     * @return the observation period start (ISO8601 format).
     */
    DateTime observationPeriodEnd();

    /**
     * @return maximum observed DTU.
     */
    double maxObservedDtu();

    /**
     * @return maximum observed storage in megabytes.
     */
    double maxObservedStorageMB();

    /**
     * @return the list of Azure SQL Databases in this pool. Expanded property.
     */
    List<SqlDatabase> databases();

    /**
     * Fetches list of databases by making call to Azure.
     * @return list of the databases in recommended elastic pool
     */
    List<SqlDatabase> listDatabases();

    /**
     * Get a specific database in the recommended database.
     *
     * @param databaseName name of the database to be fetched
     * @return information on the database recommended in recommended elastic pool
     */
    SqlDatabase getDatabase(String databaseName);

    /**
     * Fetches list of metrics information by making call to Azure.
     * @return list of the databases in recommended elastic pool
     */
    List<RecommendedElasticPoolMetric> listMetrics();

}
