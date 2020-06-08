// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.ElasticPoolEdition;
import com.azure.resourcemanager.sql.models.RecommendedElasticPool;
import com.azure.resourcemanager.sql.models.RecommendedElasticPoolMetric;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.TrackedResource;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseInner;
import com.azure.resourcemanager.sql.fluent.inner.RecommendedElasticPoolInner;
import com.azure.resourcemanager.sql.fluent.inner.RecommendedElasticPoolMetricInner;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/** Implementation for RecommendedElasticPool and its parent interfaces. */
class RecommendedElasticPoolImpl extends RefreshableWrapperImpl<RecommendedElasticPoolInner, RecommendedElasticPool>
    implements RecommendedElasticPool {

    private final SqlServerImpl sqlServer;

    protected RecommendedElasticPoolImpl(RecommendedElasticPoolInner innerObject, SqlServerImpl sqlServer) {
        super(innerObject);
        this.sqlServer = sqlServer;
    }

    @Override
    protected Mono<RecommendedElasticPoolInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getRecommendedElasticPools()
            .getAsync(this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public SqlServerManager manager() {
        return this.sqlServer.manager();
    }

    @Override
    public String sqlServerName() {
        return this.sqlServer.name();
    }

    @Override
    public ElasticPoolEdition databaseEdition() {
        return this.inner().databaseEdition();
    }

    @Override
    public double dtu() {
        return this.inner().dtu();
    }

    @Override
    public double databaseDtuMin() {
        return this.inner().databaseDtuMin();
    }

    @Override
    public double databaseDtuMax() {
        return this.inner().databaseDtuMax();
    }

    @Override
    public double storageMB() {
        return this.inner().storageMB();
    }

    @Override
    public OffsetDateTime observationPeriodStart() {
        return this.inner().observationPeriodStart();
    }

    @Override
    public OffsetDateTime observationPeriodEnd() {
        return this.inner().observationPeriodEnd();
    }

    @Override
    public double maxObservedDtu() {
        return this.inner().maxObservedDtu();
    }

    @Override
    public double maxObservedStorageMB() {
        return this.inner().maxObservedStorageMB();
    }

    @Override
    public List<TrackedResource> databases() {
        return this.inner().databases();
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        List<SqlDatabase> databasesList = new ArrayList<>();
        PagedIterable<DatabaseInner> databaseInners =
            this
                .sqlServer
                .manager()
                .inner()
                .getDatabases()
                .listByElasticPool(this.sqlServer.resourceGroupName(), this.sqlServer.name(), this.name());
        for (DatabaseInner inner : databaseInners) {
            databasesList.add(new SqlDatabaseImpl(inner.name(), this.sqlServer, inner, this.manager()));
        }
        return Collections.unmodifiableList(databasesList);
    }

    @Override
    public PagedFlux<SqlDatabase> listDatabasesAsync() {
        final RecommendedElasticPoolImpl self = this;
        return this
            .sqlServer
            .manager()
            .inner()
            .getDatabases()
            .listByElasticPoolAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), this.name())
            .mapPage(
                databaseInner ->
                    new SqlDatabaseImpl(databaseInner.name(), self.sqlServer, databaseInner, self.manager()));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner databaseInner =
            this
                .sqlServer
                .manager()
                .inner()
                .getDatabases()
                .get(this.sqlServer.resourceGroupName(), this.sqlServer.name(), databaseName);

        return new SqlDatabaseImpl(databaseInner.name(), this.sqlServer, databaseInner, this.manager());
    }

    @Override
    public Mono<SqlDatabase> getDatabaseAsync(String databaseName) {
        final RecommendedElasticPoolImpl self = this;
        return this
            .sqlServer
            .manager()
            .inner()
            .getDatabases()
            .getAsync(this.sqlServer.resourceGroupName(), this.sqlServer.name(), databaseName)
            .map(
                databaseInner ->
                    new SqlDatabaseImpl(databaseInner.name(), self.sqlServer, databaseInner, self.manager()));
    }

    @Override
    public List<RecommendedElasticPoolMetric> listMetrics() {
        List<RecommendedElasticPoolMetric> recommendedElasticPoolMetrics = new ArrayList<>();
        PagedIterable<RecommendedElasticPoolMetricInner> recommendedElasticPoolMetricInners =
            this
                .sqlServer
                .manager()
                .inner()
                .getRecommendedElasticPools()
                .listMetrics(this.resourceGroupName(), this.sqlServerName(), this.name());
        for (RecommendedElasticPoolMetricInner inner : recommendedElasticPoolMetricInners) {
            recommendedElasticPoolMetrics.add(new RecommendedElasticPoolMetricImpl(inner));
        }
        return Collections.unmodifiableList(recommendedElasticPoolMetrics);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String resourceGroupName() {
        return this.sqlServer.resourceGroupName();
    }
}
