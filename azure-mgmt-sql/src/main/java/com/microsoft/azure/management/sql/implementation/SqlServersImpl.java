package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.sql.ServerVersion;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlServers;
import rx.Completable;

/**
 * Implementation for SqlServers and its parent interfaces.
 */
@LangDefinition
class SqlServersImpl
        extends GroupableResourcesImpl<SqlServer, SqlServerImpl, ServerInner, ServersInner, SqlServerManager>
        implements SqlServers {

    private final ElasticPoolsInner elasticPoolsInner;
    private final DatabasesInner databasesInner;
    private final RecommendedElasticPoolsInner recommendedElasticPoolsInner;

    protected SqlServersImpl(
            ServersInner innerCollection,
            ElasticPoolsInner elasticPoolsInner,
            DatabasesInner databasesInner,
            RecommendedElasticPoolsInner recommendedElasticPoolsInner,
            SqlServerManager manager) {
        super(innerCollection, manager);
        this.elasticPoolsInner = elasticPoolsInner;
        this.databasesInner = databasesInner;
        this.recommendedElasticPoolsInner = recommendedElasticPoolsInner;
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected SqlServerImpl wrapModel(String name) {
        ServerInner inner = new ServerInner();
        inner.withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO);
        return new SqlServerImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager,
                this.elasticPoolsInner,
                this.databasesInner,
                this.recommendedElasticPoolsInner);
    }

    @Override
    public PagedList<SqlServer> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<SqlServer> listByGroup(String resourceGroupName) {
        return wrapList(this.innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    protected SqlServerImpl wrapModel(ServerInner inner) {
        if (inner == null) {
            return null;
        }

        return new SqlServerImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager,
                this.elasticPoolsInner,
                this.databasesInner,
                this.recommendedElasticPoolsInner);
    }

    @Override
    public SqlServer.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public SqlServer getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.getByResourceGroup(groupName, name));
    }
}
