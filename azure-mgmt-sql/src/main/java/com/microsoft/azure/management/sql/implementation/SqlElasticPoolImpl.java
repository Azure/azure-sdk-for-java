/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation for SqlElasticPool and its parent interfaces.
 */
@LangDefinition
class SqlElasticPoolImpl
        extends IndependentChildResourceImpl<
                            SqlElasticPool,
                            SqlServer,
                            ElasticPoolInner,
                            SqlElasticPoolImpl,
                            SqlServerManager>
        implements SqlElasticPool,
            SqlElasticPool.Definition,
            SqlElasticPool.Update,
        IndependentChild.DefinitionStages.WithParentResource<SqlElasticPool, SqlServer> {
    private final ElasticPoolsInner innerCollection;
    private final DatabasesInner databasesInner;
    private final DatabasesImpl databasesImpl;
    private final Map<String, SqlDatabaseImpl> databaseCreatableMap;

    protected SqlElasticPoolImpl(String name,
                                 ElasticPoolInner innerObject,
                                 ElasticPoolsInner innerCollection,
                                 DatabasesInner databasesInner,
                                 DatabasesImpl databasesImpl,
                                 SqlServerManager manager) {
        super(name, innerObject, manager);
        this.innerCollection = innerCollection;
        this.databasesInner = databasesInner;
        this.databasesImpl = databasesImpl;
        this.databaseCreatableMap = new HashMap<>();
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
        return Utils.toPrimitiveInt(this.inner().dtu());
    }

    @Override
    public int databaseDtuMax() {
        return Utils.toPrimitiveInt(this.inner().databaseDtuMax());
    }

    @Override
    public int databaseDtuMin() {
        return Utils.toPrimitiveInt(this.inner().databaseDtuMin());
    }

    @Override
    public int storageMB() {
        return Utils.toPrimitiveInt(this.inner().storageMB());
    }

    @Override
    public List<ElasticPoolActivity> listActivities() {
        PagedListConverter<ElasticPoolActivityInner, ElasticPoolActivity> converter = new PagedListConverter<ElasticPoolActivityInner, ElasticPoolActivity>() {
            @Override
            public ElasticPoolActivity typeConvert(ElasticPoolActivityInner elasticPoolActivityInner) {

                return new ElasticPoolActivityImpl(elasticPoolActivityInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.innerCollection.listActivity(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public List<ElasticPoolDatabaseActivity> listDatabaseActivities() {
        PagedListConverter<ElasticPoolDatabaseActivityInner, ElasticPoolDatabaseActivity> converter
                = new PagedListConverter<ElasticPoolDatabaseActivityInner, ElasticPoolDatabaseActivity>() {
            @Override
            public ElasticPoolDatabaseActivity typeConvert(ElasticPoolDatabaseActivityInner elasticPoolDatabaseActivityInner) {

                return new ElasticPoolDatabaseActivityImpl(elasticPoolDatabaseActivityInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.innerCollection.listDatabaseActivity(
                        this.resourceGroupName(),
                        this.sqlServerName(),
                        this.name())));
    }

    @Override
    public List<SqlDatabase> listDatabases() {
        final SqlElasticPoolImpl self = this;
        PagedListConverter<DatabaseInner, SqlDatabase> converter
                = new PagedListConverter<DatabaseInner, SqlDatabase>() {
            @Override
            public SqlDatabase typeConvert(DatabaseInner databaseInner) {

                return new SqlDatabaseImpl(databaseInner.name(), databaseInner, databasesInner, self.manager());
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
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
        return new SqlDatabaseImpl(database.name(), database, this.databasesInner, this.manager());
    }

    @Override
    public void delete() {
        this.innerCollection.delete(this.resourceGroupName(), this.sqlServerName(), this.name());
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
                    public SqlElasticPool call(ElasticPoolInner elasticPoolInner) {
                        setInner(elasticPoolInner);

                        createOrUpdateDatabase();
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

    @Override
    public SqlElasticPoolImpl withNewDatabase(String databaseName) {
        this.databaseCreatableMap.put(databaseName,
                (SqlDatabaseImpl) this.databasesImpl.define(databaseName).withExistingElasticPool(this.name()));
        return this;
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(String databaseName) {
        this.databaseCreatableMap.put(databaseName, (SqlDatabaseImpl) this.databasesImpl.get(databaseName).update().withExistingElasticPool(this.name()));
        return this;
    }

    @Override
    public SqlElasticPoolImpl withExistingDatabase(SqlDatabase database) {
        this.databaseCreatableMap.put(database.name(), (SqlDatabaseImpl) database.update().withExistingElasticPool(this.name()));
        return this;
    }

    private void createOrUpdateDatabase() {
        if (this.databaseCreatableMap.size() > 0) {
            this.databasesImpl.databases().create(new ArrayList<Creatable<SqlDatabase>>(this.databaseCreatableMap.values()));
            this.databaseCreatableMap.clear();
        }
    }
}
