package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.sql.SqlServer;
import com.microsoft.azure.management.sql.SqlServers;
import rx.Observable;

/**
 * Implementation for SqlServers and its parent interfaces.
 */
@LangDefinition
public class SqlServersImpl
        extends GroupableResourcesImpl<SqlServer, SqlServerImpl, ServerInner, ServersInner, SqlServerManager>
        implements SqlServers {

    private final ElasticPoolsInner elasticPoolsInner;

    protected SqlServersImpl(
            ServersInner innerCollection,
            ElasticPoolsInner elasticPoolsInner, SqlServerManager manager) {
        super(innerCollection, manager);
        this.elasticPoolsInner = elasticPoolsInner;
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    protected SqlServerImpl wrapModel(String name) {
        ServerInner inner = new ServerInner();

        return new SqlServerImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager, this.elasticPoolsInner);
    }

    @Override
    public PagedList<SqlServer> list() {
        // TODO - ans - Implement this once Swagger has this method.
        return null;
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
                this.myManager, this.elasticPoolsInner);
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
