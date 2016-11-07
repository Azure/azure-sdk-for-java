/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.ElasticPoolState;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlServer;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

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

    protected SqlElasticPoolImpl(String name,
                                 ElasticPoolInner innerObject,
                                 ElasticPoolsInner innerCollection) {
        super(name, innerObject);
        this.innerCollection = innerCollection;
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
