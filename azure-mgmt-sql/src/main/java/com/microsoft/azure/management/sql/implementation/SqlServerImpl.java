/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.ListToMapConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.sql.ElasticPoolEditions;
import com.microsoft.azure.management.sql.RecommendedElasticPool;
import com.microsoft.azure.management.sql.ServerMetric;
import com.microsoft.azure.management.sql.ServerVersion;
import com.microsoft.azure.management.sql.ServiceObjective;
import com.microsoft.azure.management.sql.SqlDatabase;
import com.microsoft.azure.management.sql.SqlElasticPool;
import com.microsoft.azure.management.sql.SqlFirewallRule;
import com.microsoft.azure.management.sql.SqlServer;
import rx.Completable;
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
    private final Map<String, SqlElasticPool.DefinitionStages.WithCreate> elasticPoolCreatableMap;
    private final Map<String, SqlFirewallRule.DefinitionStages.WithCreate> firewallRuleCreatableMap;
    private final Map<String, SqlDatabase.DefinitionStages.WithAllDifferentOptions> databaseCreatableMap;
    private FirewallRulesImpl firewallRulesImpl;
    private ElasticPoolsImpl elasticPoolsImpl;
    private DatabasesImpl databasesImpl;
    private final List<String> elasticPoolsToDelete;
    private final List<String> firewallRulesToDelete;
    private final List<String> databasesToDelete;

    protected SqlServerImpl(String name, ServerInner innerObject, SqlServerManager manager) {
        super(name, innerObject, manager);
        this.databaseCreatableMap = new HashMap<>();
        this.elasticPoolCreatableMap = new HashMap<>();
        this.firewallRuleCreatableMap = new HashMap<>();

        this.elasticPoolsToDelete = new ArrayList<>();
        this.databasesToDelete = new ArrayList<>();
        this.firewallRulesToDelete = new ArrayList<>();
    }

    @Override
    protected Observable<ServerInner> getInnerAsync() {
        return this.manager().inner().servers().getByResourceGroupAsync(
                this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<SqlServer> createResourceAsync() {
        final SqlServer self = this;

        return this.manager().inner().servers().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(new Func1<ServerInner, SqlServer>() {
                    @Override
                    public SqlServer call(ServerInner serverInner) {
                        setInner(serverInner);

                        deleteChildResources();
                        createOrUpdateChildResources();

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
            this.firewallRulesImpl = new FirewallRulesImpl(this.manager(), this.resourceGroupName(), this.name());
        }
        return this.firewallRulesImpl;
    }

    @Override
    public ElasticPools elasticPools() {
        if (this.elasticPoolsImpl == null) {
            this.elasticPoolsImpl = new ElasticPoolsImpl(
                    this.manager(),
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
            this.databasesImpl = new DatabasesImpl(
                    this.manager(),
                    this.resourceGroupName(),
                    this.name(),
                    this.region());
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
                this.manager().inner().servers().listUsages(
                        this.resourceGroupName(),
                        this.name())));
    }

    @Override
    public List<ServiceObjective> listServiceObjectives() {
        final ServersInner innerCollection = this.manager().inner().servers();
        PagedListConverter<ServiceObjectiveInner, ServiceObjective> converter = new PagedListConverter<ServiceObjectiveInner, ServiceObjective>() {
            @Override
            public ServiceObjective typeConvert(ServiceObjectiveInner serviceObjectiveInner) {

                return new ServiceObjectiveImpl(serviceObjectiveInner, innerCollection);
            }
        };
        return converter.convert(ReadableWrappersImpl.convertToPagedList(
                        this.manager().inner().servers().listServiceObjectives(
                        this.resourceGroupName(),
                        this.name())));
    }

    @Override
    public ServiceObjective getServiceObjective(String serviceObjectiveName) {
        return new ServiceObjectiveImpl(
                this.manager().inner().servers().getServiceObjective(this.resourceGroupName(), this.name(), serviceObjectiveName),
                this.manager().inner().servers());
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
                return new RecommendedElasticPoolImpl(
                        recommendedElasticPoolInner,
                        self.manager());
            }
        };
        return converter.convertToUnmodifiableMap(this.manager().inner().recommendedElasticPools().list(
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
    public SqlServerImpl withNewDatabase(String databaseName) {
        this.databaseCreatableMap.remove(databaseName);

        this.databaseCreatableMap.put(databaseName,
                this.databases().define(databaseName));
        return this;
    }

    @Override
    public SqlServerImpl withoutDatabase(String databaseName) {
        this.databasesToDelete.add(databaseName);
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
        return withNewElasticPool(elasticPoolName, elasticPoolEdition, (String[]) null);
    }

    @Override
    public SqlServerImpl withoutElasticPool(String elasticPoolName) {
        this.elasticPoolsToDelete.add(elasticPoolName);
        return this;
    }

    private void withDatabaseInElasticPool(String databaseName, String elasticPoolName) {
        this.databaseCreatableMap.remove(databaseName);

        this.databaseCreatableMap.put(databaseName,
                (SqlDatabase.DefinitionStages.WithAllDifferentOptions) this.databases().define(databaseName)
                    .withExistingElasticPool(elasticPoolName));
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String ipAddress) {
        return this.withNewFirewallRule(ipAddress, ipAddress);
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String startIPAddress, String endIPAddress) {
        return this.withNewFirewallRule(startIPAddress, endIPAddress, SdkContext.randomResourceName("firewall_", 15));
    }

    @Override
    public SqlServerImpl withNewFirewallRule(String startIPAddress, String endIPAddress, String firewallRuleName) {
        this.firewallRuleCreatableMap.remove(firewallRuleName);

        this.firewallRuleCreatableMap.put(firewallRuleName,
                this.firewallRules().define(firewallRuleName).withIPAddressRange(startIPAddress, endIPAddress));
        return this;
    }

    @Override
    public SqlServerImpl withoutFirewallRule(String firewallRuleName) {
        this.firewallRulesToDelete.add(firewallRuleName);
        return this;
    }

    private Observable<Indexable> createOrUpdateFirewallRulesAsync() {
        final SqlServerImpl self = this;
        if (this.firewallRuleCreatableMap.size() > 0) {
            return Utils.rootResource(this.firewallRulesImpl
                    .sqlFirewallRules()
                    .createAsync(new ArrayList<Creatable<SqlFirewallRule>>(this.firewallRuleCreatableMap.values())))
                    .map(new Func1<Indexable, Indexable>() {
                        @Override
                        public Indexable call(Indexable indexable) {
                            self.firewallRuleCreatableMap.clear();
                            return indexable;
                        }
                    });
        }
        return Observable.empty();
    }

    private Observable<Indexable> createOrUpdateElasticPoolsAndDatabasesAsync() {
        if (this.elasticPoolCreatableMap.size() > 0) {
            this.elasticPoolsImpl.elasticPools().create(new ArrayList<Creatable<SqlElasticPool>>(this.elasticPoolCreatableMap.values()));
            this.elasticPoolCreatableMap.clear();
        }

        final SqlServerImpl self = this;
        if (this.databaseCreatableMap.size() > 0) {
            return Utils.rootResource(this.databasesImpl
                    .databases()
                    .createAsync(new ArrayList<Creatable<SqlDatabase>>(this.databaseCreatableMap.values())))
                    .map(new Func1<Indexable, Indexable>() {
                        @Override
                        public Indexable call(Indexable indexable) {
                            self.databaseCreatableMap.clear();
                            return indexable;
                        }
                    });
        }
        return Observable.empty();
    }

    private void createOrUpdateChildResources() {
        Observable<Indexable> createFirewallRules = createOrUpdateFirewallRulesAsync();
        Observable<Indexable> createDatabases = createOrUpdateElasticPoolsAndDatabasesAsync();
        Observable.merge(createFirewallRules, createDatabases).defaultIfEmpty(null).toBlocking().last();
    }

    private void deleteChildResources() {
        Completable deleteFirewallRules = deleteFirewallRule();
        Completable deleteDatabasesAndElasticPools = deleteDatabasesAndElasticPools();
        Completable.merge(deleteFirewallRules, deleteDatabasesAndElasticPools).await();
    }

    private Completable deleteDatabasesAndElasticPools() {
        List<Completable> deleteDBList = new ArrayList<>();
        for (String databaseName : this.databasesToDelete) {
            deleteDBList.add(this.databases().deleteAsync(databaseName));
        }
        Completable deleteDBs = Completable.merge(deleteDBList);

        List<Completable> deleteElasticPoolList = new ArrayList<>();
        for (String elasticPoolName : this.elasticPoolsToDelete) {
            deleteElasticPoolList.add(this.elasticPools().deleteAsync(elasticPoolName));
        }
        Completable deletePools = Completable.merge(deleteElasticPoolList);
        return Completable.concat(deleteDBs, deletePools);
    }

    private Completable deleteFirewallRule() {
        List<Completable> deleteTaskList = new ArrayList<>();

        for (String firewallRuleName : this.firewallRulesToDelete) {
            deleteTaskList.add(this.firewallRules().deleteAsync(firewallRuleName));
        }
        return Completable.merge(deleteTaskList);
    }

}
