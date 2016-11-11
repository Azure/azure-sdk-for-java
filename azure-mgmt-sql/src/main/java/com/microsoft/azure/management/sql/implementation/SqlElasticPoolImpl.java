/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.ElasticPoolActivity;
import com.microsoft.azure.management.sql.ElasticPoolDatabaseActivity;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.ElasticPoolState;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for SqlElasticPool and its parent interfaces.
 */
class SqlElasticPoolImpl
        extends IndependentChildResourceImpl<
                            SqlElasticPool,
                            SqlServer,
                            ElasticPoolInner,
        SqlElasticPoolImpl>
        implements SqlElasticPool,
            SqlElasticPool.Definition,
            SqlElasticPool.Update,
        IndependentChild.DefinitionStages.WithParentResource<SqlElasticPool, SqlServer> {
    private final ElasticPoolsInner innerCollection;
    private final DatabasesInner databasesInner;

    protected SqlElasticPoolImpl(String name,
                                 ElasticPoolInner innerObject,
                                 ElasticPoolsInner innerCollection,
                                 DatabasesInner databasesInner) {
        super(name, innerObject);
        this.innerCollection = innerCollection;
        this.databasesInner = databasesInner;
    }

    @Override
    public String sqlServerName() {
        return this.parentName;
    }

    @Override
    public DateTime creationDate() {
        return this.inner().creationDate();
    }

    @Override
    public ElasticPoolState state() {
        return this.inner().state();
    }

    @Override
    public ElasticPoolEditions edition() {
        return this.inner().edition();
    }

    @Override
    public int dtu() {
        return this.inner().dtu();
    }

    @Override
    public int databaseDtuMax() {
        return this.inner().databaseDtuMax();
    }

    @Override
    public int databaseDtuMin() {
        return this.inner().databaseDtuMin();
    }

    @Override
    public int storageMB() {
        return this.inner().storageMB();
    }

    @Override
    public List<ElasticPoolActivity> listActivity() {
        PagedListConverter<ElasticPoolActivityInner, ElasticPoolActivity> converter = new PagedListConverter<ElasticPoolActivityInner, ElasticPoolActivity>() {
            @Override
            public ElasticPoolActivity typeConvert(ElasticPoolActivityInner elasticPoolActivityInner) {

                return new ElasticPoolActivityImpl(elasticPoolActivityInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listActivity(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public List<ElasticPoolDatabaseActivity> listDatabaseActivity() {
        PagedListConverter<ElasticPoolDatabaseActivityInner, ElasticPoolDatabaseActivity> converter
                = new PagedListConverter<ElasticPoolDatabaseActivityInner, ElasticPoolDatabaseActivity>() {
            @Override
            public ElasticPoolDatabaseActivity typeConvert(ElasticPoolDatabaseActivityInner elasticPoolDatabaseActivityInner) {

                return new ElasticPoolDatabaseActivityImpl(elasticPoolDatabaseActivityInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listDatabaseActivity(
                        this.name(),
                        this.resourceGroupName(),
                        this.sqlServerName())));
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        final DatabasesInner databasesInner = this.databasesInner;
        PagedListConverter<DatabaseInner, SqlDatabase> converter
                = new PagedListConverter<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase typeConvert(DatabaseInner databaseInner) {

                return new SqlDatabaseImpl(databaseInner.name(), databaseInner, databasesInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listDatabases(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public SqlDatabase getDatabase(String databaseName) {
        DatabaseInner database = this.innerCollection.getDatabase(
                this.resourceGroupName(),
                this.sqlServerName(),
                this.name(),
                databaseName);
        return new SqlDatabaseImpl(database.name(), database, this.databasesInner);
    }

    @Override
    public SqlElasticPool refresh() {
        this.innerCollection.get(this.resourceGroupName(), this.sqlServerName(), this.name());
        return this;
    }

    @Override
    protected Observable<SqlElasticPool> createChildResourceAsync() {
        final SqlElasticPool self = this;
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.sqlServerName(), this.name(), this.inner())
                .map(new Func1<ElasticPoolInner, SqlElasticPool>() {
                    @Override
                    public SqlElasticPool call(ElasticPoolInner databaseInner) {
                        setInner(databaseInner);

                        return self;
                    }
                });
    }

    @Override
    public SqlElasticPoolImpl withEdition(ElasticPoolEditions edition) {
        this.inner().withEdition(edition);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMin(int databaseDtuMin) {
        this.inner().withDatabaseDtuMin(databaseDtuMin);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDatabaseDtuMax(int databaseDtuMax) {
        this.inner().withDatabaseDtuMax(databaseDtuMax);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withDtu(int dtu) {
        this.inner().withDtu(dtu);
        return this;
    }

    @Override
    public SqlElasticPoolImpl withStorageCapacity(int storageMB) {
        this.inner().withStorageMB(storageMB);
        return this;
    }
}
