/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceId;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.RecommendedElasticPool;
import com.microsoft.azure.management.sql.RecommendedElasticPoolMetric;
import com.microsoft.azure.management.sql.SqlDatabase;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for RecommendedElasticPool and its parent interfaces.
 */
@LangDefinition
class RecommendedElasticPoolImpl
        extends WrapperImpl<RecommendedElasticPoolInner>
        implements RecommendedElasticPool {

    private final DatabasesInner databasesInner;
    private final RecommendedElasticPoolsInner recommendedElasticPoolsInner;
    private final ResourceId resourceId;

    protected RecommendedElasticPoolImpl(
            RecommendedElasticPoolInner innerObject,
            DatabasesInner databasesInner,
            RecommendedElasticPoolsInner recommendedElasticPoolsInner) {
        super(innerObject);
        this.databasesInner = databasesInner;
        this.recommendedElasticPoolsInner = recommendedElasticPoolsInner;
        this.resourceId = ResourceId.parseResourceId(this.inner().id());
    }

    @Override
    public RecommendedElasticPool refresh() {
        this.setInner(this.recommendedElasticPoolsInner.get(this.resourceGroupName(), this.sqlServerName(), this.name()));
        return this;
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
            databases.add(new SqlDatabaseImpl(databaseInner.name(), databaseInner, this.databasesInner));
        }

        return databases;
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        final RecommendedElasticPoolImpl self = this;
        PagedListConverter<DatabaseInner, SqlDatabase> converter = new PagedListConverter<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase typeConvert(DatabaseInner databaseInner) {

                return new SqlDatabaseImpl(databaseInner.name(), databaseInner, self.databasesInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.recommendedElasticPoolsInner.listDatabases(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner databaseInner = this.recommendedElasticPoolsInner.getDatabases(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.name(),
                databaseName);

        return new SqlDatabaseImpl(databaseInner.name(), databaseInner, this.databasesInner);
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
                this.recommendedElasticPoolsInner.listMetrics(
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