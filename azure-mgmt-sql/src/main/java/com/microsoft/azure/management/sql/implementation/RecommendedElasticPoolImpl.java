/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.RecommendedElasticPool;
import com.microsoft.azure.management.sql.RecommendedElasticPoolMetric;
import com.microsoft.azure.management.sql.SqlDatabase;
import org.joda.time.DateTime;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for RecommendedElasticPool and its parent interfaces.
 */
@LangDefinition
class RecommendedElasticPoolImpl
        extends RefreshableWrapperImpl<RecommendedElasticPoolInner, RecommendedElasticPool>
        implements RecommendedElasticPool {

    private final ResourceId resourceId;
    private final SqlServerManager manager;

    protected RecommendedElasticPoolImpl(RecommendedElasticPoolInner innerObject, SqlServerManager manager) {
        super(innerObject);
        this.resourceId = ResourceId.fromString(this.inner().id());
        this.manager = manager;
    }

    @Override
    protected Observable<RecommendedElasticPoolInner> getInnerAsync() {
        return this.manager().inner().recommendedElasticPools().getAsync(
                this.resourceGroupName(), this.sqlServerName(), this.name());
    }

    @Override
    public SqlServerManager manager() {
        return this.manager;
    }

    @Override
    public String sqlServerName() {
        return this.resourceId.parent().name();
    }

    @Override
    public ElasticPoolEditions databaseEdition() {
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
    public DateTime observationPeriodStart() {
        return this.inner().observationPeriodStart();
    }

    @Override
    public DateTime observationPeriodEnd() {
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
    public List<SqlDatabase> databases() {
        ArrayList<SqlDatabase> databases = new ArrayList<>();

        for (DatabaseInner databaseInner : this.inner().databases()) {
            databases.add(new SqlDatabaseImpl(databaseInner.name(), databaseInner, this.manager()));
        }

        return databases;
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        final RecommendedElasticPoolImpl self = this;
        PagedListConverter<DatabaseInner, SqlDatabase> converter = new PagedListConverter<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase typeConvert(DatabaseInner databaseInner) {

                return new SqlDatabaseImpl(databaseInner.name(), databaseInner, self.manager());
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.manager().inner().recommendedElasticPools().listDatabases(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner databaseInner = this.manager().inner().recommendedElasticPools().getDatabases(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.name(),
                databaseName);

        return new SqlDatabaseImpl(databaseInner.name(), databaseInner, this.manager());
    }

    @Override
    public List<RecommendedElasticPoolMetric> listMetrics() {
        PagedListConverter<RecommendedElasticPoolMetricInner, RecommendedElasticPoolMetric> converter = new PagedListConverter<RecommendedElasticPoolMetricInner, RecommendedElasticPoolMetric>() {
            @Override
            public RecommendedElasticPoolMetric typeConvert(RecommendedElasticPoolMetricInner recommendedElasticPoolMetricInner) {

                return new RecommendedElasticPoolMetricImpl(recommendedElasticPoolMetricInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.manager().inner().recommendedElasticPools().listMetrics(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
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
        return this.resourceId.resourceGroupName();
    }
}