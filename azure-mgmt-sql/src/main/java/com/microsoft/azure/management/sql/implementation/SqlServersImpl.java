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

    protected SqlServersImpl(SqlServerManager manager) {
        super(manager.inner().servers(), manager);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected SqlServerImpl wrapModel(String name) {
        ServerInner inner = new ServerInner();
        inner.withVersion(ServerVersion.ONE_TWO_FULL_STOP_ZERO);
        return new SqlServerImpl(
                name,
                inner,
                this.inner(),
                this.manager(),
                this.manager().inner().elasticPools(),
                this.manager().inner().databases(),
                this.manager().inner().recommendedElasticPools());
    }

    @Override
    public PagedList<SqlServer> list() {
        return wrapList(this.inner().list());
    }

    @Override
    public PagedList<SqlServer> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    protected SqlServerImpl wrapModel(ServerInner inner) {
        if (inner == null) {
            return null;
        }

        return new SqlServerImpl(
                inner.name(),
                inner,
                this.inner(),
                this.manager(),
                this.manager().inner().elasticPools(),
                this.manager().inner().databases(),
                this.manager().inner().recommendedElasticPools());
    }

    @Override
    public SqlServer.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public SqlServer getByGroup(String groupName, String name) {
        return wrapModel(this.inner().getByResourceGroup(groupName, name));
    }
}
