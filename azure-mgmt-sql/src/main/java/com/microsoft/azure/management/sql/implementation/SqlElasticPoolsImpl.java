/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsGettingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.SupportsListingByParent;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlElasticPools;
import rx.Completable;

import java.util.List;

/**
 * Implementation for SQLElasticPools and its parent interfaces.
 */
@LangDefinition
class SqlElasticPoolsImpl extends IndependentChildResourcesImpl<
            SqlElasticPool,
            SqlElasticPoolImpl,
            ElasticPoolInner,
            ElasticPoolsInner,
            SqlServerManager>
        implements SqlElasticPools.SqlElasticPoolsCreatable,
        SupportsGettingByParent<SqlElasticPool>,
        SupportsListingByParent<SqlElasticPool> {
    private final DatabasesInner databasesInner;
    private final DatabasesImpl databasesImpl;

    protected SqlElasticPoolsImpl(ElasticPoolsInner innerCollection, SqlServerManager manager, DatabasesInner databasesInner, DatabasesImpl databasesImpl) {
        super(innerCollection, manager);
        this.databasesInner = databasesInner;
        this.databasesImpl = databasesImpl;
    }

    @Override
    protected SqlElasticPoolImpl wrapModel(String name) {
        ElasticPoolInner inner = new ElasticPoolInner();
        return new SqlElasticPoolImpl(
                name,
                inner,
                this.innerCollection,
                this.databasesInner,
                this.databasesImpl);
    }

    @Override
    public SqlElasticPool getByParent(String resourceGroup, String parentName, String name) {
        return wrapModel(this.innerCollection.get(resourceGroup, parentName, name));
    }

    @Override
    public PagedList<SqlElasticPool> listByParent(String resourceGroupName, String parentName) {
        return wrapList(this.innerCollection.listByServer(resourceGroupName, parentName));
    }

    @Override
    protected SqlElasticPoolImpl wrapModel(ElasticPoolInner inner) {
        if (inner == null) {
            return null;
        }

        return new SqlElasticPoolImpl(inner.name(), inner, this.innerCollection, this.databasesInner, this.databasesImpl);
    }

    @Override
    public SqlElasticPool.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return this.innerCollection.deleteAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public SqlElasticPool getBySqlServer(String resourceGroup, String sqlServerName, String name) {
        return this.getByParent(resourceGroup, sqlServerName, name);
    }

    @Override
    public SqlElasticPool getBySqlServer(GroupableResource sqlServer, String name) {
        return this.getByParent(sqlServer, name);
    }

    @Override
    public List<SqlElasticPool> listBySqlServer(String resourceGroupName, String sqlServerName) {
        return this.listByParent(resourceGroupName, sqlServerName);
    }

    @Override
    public List<SqlElasticPool> listBySqlServer(GroupableResource sqlServer) {
        return this.listByParent(sqlServer);
    }

    @Override
    public SqlElasticPool.DefinitionStages.Blank definedWithSqlServer(String resourceGroupName, String sqlServerName, String elasticPoolName, Region region) {
        ElasticPoolInner inner = new ElasticPoolInner();
        inner.withLocation(region.name());

        return new SqlElasticPoolImpl(
                elasticPoolName,
                inner,
                this.innerCollection,
                this.databasesInner,
                this.databasesImpl).withExistingParentResource(resourceGroupName, sqlServerName);
    }
}
