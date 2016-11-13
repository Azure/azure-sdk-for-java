/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.ServerMetric;
import com.microsoft.azure.management.sql.ServerUpgradeResult;
import com.microsoft.azure.management.sql.ServerVersion;
import com.microsoft.azure.management.sql.ServiceObjective;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for SqlServer and its parent interfaces.
 */
@LangDefinition
public class SqlServerImpl
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
    private final RecommendedElasticPoolsInner recommendedElasticPoolsInner;

    protected SqlServerImpl(String name,
                            ServerInner innerObject,
                            ServersInner innerCollection,
                            SqlServerManager manager,
                            ElasticPoolsInner elasticPoolsInner,
                            DatabasesInner databasesInner,
                            RecommendedElasticPoolsInner recommendedElasticPoolsInner) {
        super(name, innerObject, manager);
        this.innerCollection = innerCollection;
        this.elasticPoolsInner = elasticPoolsInner;
        this.databasesInner = databasesInner;
        this.recommendedElasticPoolsInner = recommendedElasticPoolsInner;
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
        return new ElasticPoolsImpl(this.elasticPoolsInner, this.myManager, this.databasesInner, this.resourceGroupName(), this.name(), this.region());
    }

    @Override
    public Databases databases() {
        return new DatabasesImpl(this.databasesInner, myManager, this.resourceGroupName(), this.name(), this.region());
    }

    @Override
    public void cancelUpgrade() {
        this.innerCollection.cancelUpgrade(this.resourceGroupName(), this.name());
    }

    @Override
    public ServerUpgradeResult getUpgrade() {
        return new ServerUpgradeResultImpl(this.innerCollection.getUpgrade(this.resourceGroupName(), this.name()));
    }

    @Override
    public PagedList<ServerMetric> listUsages() {
        PagedListConverter<ServerMetricInner, ServerMetric> converter = new PagedListConverter<ServerMetricInner, ServerMetric>() {
            @Override
            public ServerMetric typeConvert(ServerMetricInner serverMetricInner) {

                return new ServerMetricImpl(serverMetricInner);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                this.innerCollection.listUsages(
                        this.resourceGroupName(),
                        this.name())));
    }

    @Override
    public PagedList<ServiceObjective> listServiceObjectives() {
        final ServersInner innerCollection = this.innerCollection;
        PagedListConverter<ServiceObjectiveInner, ServiceObjective> converter = new PagedListConverter<ServiceObjectiveInner, ServiceObjective>() {
            @Override
            public ServiceObjective typeConvert(ServiceObjectiveInner serviceObjectiveInner) {

                return new ServiceObjectiveImpl(serviceObjectiveInner, innerCollection);
            }
        };
        return converter.convert(Utils.convertToPagedList(
                        this.innerCollection.listServiceObjectives(
                        this.resourceGroupName(),
                        this.name())));
    }

    @Override
    public ServiceObjective getServiceObjective(String serviceObjectiveName) {
        return new ServiceObjectiveImpl(
                this.innerCollection.getServiceObjective(this.resourceGroupName(), this.name(), serviceObjectiveName),
                this.innerCollection);
    }

    @Override
    public RecommendedElasticPools recommendedElasticPools() {
        return new RecommendedElasticPoolsImpl(this.recommendedElasticPoolsInner, this.databasesInner, this.resourceGroupName(), this.name());
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
