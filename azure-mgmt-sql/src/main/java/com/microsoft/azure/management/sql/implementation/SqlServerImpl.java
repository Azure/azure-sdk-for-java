/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.sql.ServerVersion;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for SqlServer and its parent interfaces.
 */
@LangDefinition
class SqlServerImpl
        extends
            GroupableResourceImpl<
                    SqlServer,
                    ServerInner,
                    SqlServerImpl,
                    SqlServerManager>
        implements
            SqlServer,
            SqlServer.Definition,
            SqlServer.Update {
    private final ServersInner innerCollection;
    private final ElasticPoolsInner elasticPoolsInner;
    private final DatabasesInner databasesInner;

    protected SqlServerImpl(String name,
                            ServerInner innerObject,
                            ServersInner innerCollection,
                            SqlServerManager manager,
                            ElasticPoolsInner elasticPoolsInner,
                            DatabasesInner databasesInner) {
        super(name, innerObject, manager);
        this.innerCollection = innerCollection;
        this.elasticPoolsInner = elasticPoolsInner;
        this.databasesInner = databasesInner;
    }

    @Override
    public SqlServer refresh() {
        ServerInner response =
                this.innerCollection.getByResourceGroup(this.resourceGroupName(), this.name());
        this.setInner(response);

        return this;
    }

    @Override
    public Observable<SqlServer> createResourceAsync() {
        final SqlServer self = this;
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(new Func1<ServerInner, SqlServer>() {
                    @Override
                    public SqlServer call(ServerInner serverInner) {
                        setInner(serverInner);

                        return self;
                    }
                });
    }

    @Override
    public String fullyQualifiedDomainName() {
        return this.inner().fullyQualifiedDomainName();
    }

    @Override
    public ServerVersion version() {
        return this.inner().version();
    }

    @Override
    public String administratorLogin() {
        return this.inner().administratorLogin();
    }

    @Override
    public FirewallRules firewallRules() {
        return new FirewallRulesImpl(this.innerCollection, this.myManager, this.resourceGroupName(), this.name());
    }

    @Override
    public ElasticPools elasticPools() {
        return new ElasticPoolsImpl(this.elasticPoolsInner, this.myManager, this.resourceGroupName(), this.name(), this.region());
    }

    @Override
    public Databases databases() {
        return new DatabasesImpl(this.databasesInner, myManager, this.resourceGroupName(), this.name(), this.region());
    }

    @Override
    public SqlServerImpl withAdministratorLogin(String administratorLogin) {
        this.inner().withAdministratorLogin(administratorLogin);
        return this;
    }

    @Override
    public SqlServerImpl withAdministratorPassword(String administratorLoginPassword) {
        this.inner().withAdministratorLoginPassword(administratorLoginPassword);
        return this;
    }

    @Override
    public SqlServerImpl withVersion(ServerVersion version) {
        this.inner().withVersion(version);
        return this;
    }
}
