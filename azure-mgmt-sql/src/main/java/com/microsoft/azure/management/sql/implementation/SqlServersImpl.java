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

    protected SqlServersImpl(
            ServersInner innerCollection,
            SqlServerManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    protected SqlServerImpl wrapModel(String name) {
        ServerInner inner = new ServerInner();

        return new SqlServerImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
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
        return new SqlServerImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
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
