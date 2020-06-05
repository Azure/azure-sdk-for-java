// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasId;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.inner.RecommendedElasticPoolInner;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure SQL Recommended ElasticPool. */
@Fluent
public interface RecommendedElasticPool
    extends Refreshable<RecommendedElasticPool>,
        HasInner<RecommendedElasticPoolInner>,
        HasResourceGroup,
        HasName,
        HasId,
        HasManager<SqlServerManager> {

    /** @return name of the SQL Server to which this database belongs */
    String sqlServerName();

    /**
     * @return the edition of the Azure SQL Recommended Elastic Pool. The ElasticPoolEditions enumeration contains all
     *     the valid editions. Possible values include: 'Basic', 'Standard', 'Premium'.
     */
    ElasticPoolEdition databaseEdition();

    /** @return the DTU for the SQL Azure Recommended Elastic Pool. */
    double dtu();

    /** @return the minimum DTU for the database. */
    double databaseDtuMin();

    /** @return the maximum DTU for the database. */
    double databaseDtuMax();

    /** @return storage size in megabytes. */
    double storageMB();

    /** @return the observation period start (ISO8601 format). */
    OffsetDateTime observationPeriodStart();

    /** @return the observation period start (ISO8601 format). */
    OffsetDateTime observationPeriodEnd();

    /** @return maximum observed DTU. */
    double maxObservedDtu();

    /** @return maximum observed storage in megabytes. */
    double maxObservedStorageMB();

    /** @return the list of Azure SQL Databases in this pool. Expanded property. */
    List<TrackedResource> databases();

    /**
     * Fetches list of databases by making call to Azure.
     *
     * @return list of the databases in recommended elastic pool
     */
    List<SqlDatabase> listDatabases();

    /**
     * Fetches list of databases by making call to Azure.
     *
     * @return a representation of the deferred computation of the databases in this recommended elastic pool
     */
    PagedFlux<SqlDatabase> listDatabasesAsync();

    /**
     * Get a specific database in the recommended database.
     *
     * @param databaseName name of the database to be fetched
     * @return information on the database recommended in recommended elastic pool
     */
    SqlDatabase getDatabase(String databaseName);

    /**
     * Get a specific database in the recommended database.
     *
     * @param databaseName name of the database to be fetched
     * @return a representation of the deferred computation to get the database in the recommended elastic pool
     */
    Mono<SqlDatabase> getDatabaseAsync(String databaseName);

    /**
     * Fetches list of metrics information by making call to Azure.
     *
     * @return list of the databases in recommended elastic pool
     */
    List<RecommendedElasticPoolMetric> listMetrics();
}
