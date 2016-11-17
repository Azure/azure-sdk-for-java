/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.ListToMapConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.RecommendedElasticPool;
import com.microsoft.azure.management.sql.ServerMetric;
import com.microsoft.azure.management.sql.ServerVersion;
import com.microsoft.azure.management.sql.ServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final Map<String, SqlElasticPool.DefinitionStages.WithCreate> elasticPoolCreatableMap;
    private final Map<String, SqlFirewallRule.DefinitionStages.WithCreate> firewallRuleCreatableMap;
    private final Map<String, SqlDatabase.DefinitionStages.WithCreate> databaseCreatableMap;
    private FirewallRulesImpl firewallRulesImpl;
    private ElasticPoolsImpl elasticPoolsImpl;
    private DatabasesImpl databasesImpl;

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

        this.databaseCreatableMap = new HashMap<>();
        this.elasticPoolCreatableMap = new HashMap<>();
        this.firewallRuleCreatableMap = new HashMap<>();
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

                        createOrUpdateFirewallRules();
                        createOrUpdateElasticPools();
                        createOrUpdateDatabases();

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
        if (this.firewallRulesImpl == null) {
            this.firewallRulesImpl = new FirewallRulesImpl(this.innerCollection, this.myManager, this.resourceGroupName(), this.name());
        }
        return this.firewallRulesImpl;
    }

    @Override
    public ElasticPools elasticPools() {
        if (this.elasticPoolsImpl == null) {
            this.elasticPoolsImpl = new ElasticPoolsImpl(
                    this.elasticPoolsInner,
                    this.myManager,
                    this.databasesInner,
                    (DatabasesImpl) this.databases(),
                    this.resourceGroupName(),
                    this.name(),
                    this.region());
        }
        return this.elasticPoolsImpl;
    }

    @Override
    public Databases databases() {
        if (this.databasesImpl == null) {
            this.databasesImpl = new DatabasesImpl(this.databasesInner, myManager, this.resourceGroupName(), this.name(), this.region());
        }
        return this.databasesImpl;
    }

    @Override
    public List<ServerMetric> listUsages() {
        PagedListConverter<ServerMetricInner, ServerMetric> converter = new PagedListConverter<ServerMetricInner, ServerMetric>() {
            @Override
            public ServerMetric typeConvert(ServerMetricInner serverMetricInner) {

                return new ServerMetricImpl(serverMetricInner);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                this.innerCollection.listUsages(
                        this.resourceGroupName(),
                        this.name())));
    }

    @Override
    public List<ServiceObjective> listServiceObjectives() {
        final ServersInner innerCollection = this.innerCollection;
        PagedListConverter<ServiceObjectiveInner, ServiceObjective> converter = new PagedListConverter<ServiceObjectiveInner, ServiceObjective>() {
            @Override
            public ServiceObjective typeConvert(ServiceObjectiveInner serviceObjectiveInner) {

                return new ServiceObjectiveImpl(serviceObjectiveInner, innerCollection);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
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
    public Map<String, RecommendedElasticPool> listRecommendedElasticPools() {
        final SqlServerImpl self = this;
        ListToMapConverter<RecommendedElasticPool, RecommendedElasticPoolInner> converter = new ListToMapConverter<RecommendedElasticPool, RecommendedElasticPoolInner>() {
            @Override
            protected String name(RecommendedElasticPoolInner recommendedElasticPoolInner) {
                return recommendedElasticPoolInner.name();
            }

            @Override
            protected RecommendedElasticPool impl(RecommendedElasticPoolInner recommendedElasticPoolInner) {
                return new RecommendedElasticPoolImpl(recommendedElasticPoolInner,
                        self.databasesInner, self.recommendedElasticPoolsInner);
            }
        };
        return converter.convertToUnmodifiableMap(this.recommendedElasticPoolsInner.list(
                this.resourceGroupName(),
                this.name()));
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

    @Override
    public SqlServerManager manager() {
        return this.myManager;
    }

    @Override
    public SqlServerImpl withNewDatabase(String databaseName) {
        this.withDatabaseInElasticPool(databaseName, null);
        return this;
    }

    @Override
    public SqlServerImpl withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition, String... databaseNames) {
        if (this.elasticPoolCreatableMap.get(elasticPoolName) == null) {
            this.elasticPoolCreatableMap.put(elasticPoolName, this.elasticPools().define(elasticPoolName).withEdition(elasticPoolEdition));
        }

        if (databaseNames != null) {
            for (String databaseName : databaseNames) {
                this.withDatabaseInElasticPool(databaseName, elasticPoolName);
            }
        }

        return this;
    }

    @Override
    public SqlServerImpl withNewElasticPool(String elasticPoolName, ElasticPoolEditions elasticPoolEdition) {
        return withNewElasticPool(elasticPoolName, elasticPoolEdition, null);
    }

    private void withDatabaseInElasticPool(String databaseName, String elasticPoolName) {
        SqlDatabase.DefinitionStages.WithCreate existingDatabaseCreatable = this.databaseCreatableMap.get(databaseName);
        if (existingDatabaseCreatable != null) {
            this.databaseCreatableMap.remove(databaseName);
        }

        this.databaseCreatableMap.put(databaseName, this.databases().define(databaseName).withExistingElasticPool(elasticPoolName).withoutSourceDatabaseId());
    }


    private void createOrUpdateFirewallRules() {
        if (this.firewallRuleCreatableMap.size() > 0) {
            this.firewallRulesImpl.sqlFirewallRules().create(new ArrayList(this.firewallRuleCreatableMap.values()));
            this.firewallRuleCreatableMap.clear();
        }
    }

    private void createOrUpdateElasticPools() {
        if (this.elasticPoolCreatableMap.size() > 0) {
            this.elasticPoolsImpl.elasticPools().create(new ArrayList(this.elasticPoolCreatableMap.values()));
            this.elasticPoolCreatableMap.clear();
        }
    }

    private void createOrUpdateDatabases() {
        if (this.databaseCreatableMap.size() > 0) {
            this.databasesImpl.databases().create(new ArrayList(this.databaseCreatableMap.values()));
            this.databaseCreatableMap.clear();
        }
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String ipAddress) {
        return this.withNewFirewallRule(ipAddress, ipAddress);
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String startIpAddress, String endIpAddress) {
        return this.withNewFirewallRule(startIpAddress, endIpAddress, ResourceNamer.randomResourceName("firewall_", 15));
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String startIpAddress, String endIpAddress, String firewallRuleName) {
        if (this.firewallRuleCreatableMap.get(firewallRuleName) != null) {
            this.firewallRuleCreatableMap.remove(firewallRuleName);
        }
        this.firewallRuleCreatableMap.put(firewallRuleName,
                this.firewallRules().define(firewallRuleName).withIpAddressRange(startIpAddress, endIpAddress));
        return this;
    }
}
