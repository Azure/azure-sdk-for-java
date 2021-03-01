// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.dag.FunctionalTaskItem;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.RecommendedElasticPoolInner;
import com.azure.resourcemanager.sql.fluent.models.RestorableDroppedDatabaseInner;
import com.azure.resourcemanager.sql.fluent.models.ServerAutomaticTuningInner;
import com.azure.resourcemanager.sql.fluent.models.ServerAzureADAdministratorInner;
import com.azure.resourcemanager.sql.fluent.models.ServerInner;
import com.azure.resourcemanager.sql.fluent.models.ServerUsageInner;
import com.azure.resourcemanager.sql.fluent.models.ServiceObjectiveInner;
import com.azure.resourcemanager.sql.models.AdministratorName;
import com.azure.resourcemanager.sql.models.IdentityType;
import com.azure.resourcemanager.sql.models.RecommendedElasticPool;
import com.azure.resourcemanager.sql.models.ResourceIdentity;
import com.azure.resourcemanager.sql.models.ServerMetric;
import com.azure.resourcemanager.sql.models.ServiceObjective;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlElasticPoolOperations;
import com.azure.resourcemanager.sql.models.SqlEncryptionProtectorOperations;
import com.azure.resourcemanager.sql.models.SqlFailoverGroupOperations;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlFirewallRuleOperations;
import com.azure.resourcemanager.sql.models.SqlRestorableDroppedDatabase;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerAutomaticTuning;
import com.azure.resourcemanager.sql.models.SqlServerDnsAliasOperations;
import com.azure.resourcemanager.sql.models.SqlServerKeyOperations;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicyOperations;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRule;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRuleOperations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** Implementation for SqlServer and its parent interfaces. */
public class SqlServerImpl extends GroupableResourceImpl<SqlServer, ServerInner, SqlServerImpl, SqlServerManager>
    implements SqlServer, SqlServer.Definition, SqlServer.Update {

    private final ClientLogger logger = new ClientLogger(getClass());
    private FunctionalTaskItem sqlADAdminCreator;
    private boolean allowAzureServicesAccess;
    private SqlFirewallRulesAsExternalChildResourcesImpl sqlFirewallRules;
    private SqlFirewallRuleOperations.SqlFirewallRuleActionsDefinition sqlFirewallRuleOperations;
    private SqlVirtualNetworkRulesAsExternalChildResourcesImpl sqlVirtualNetworkRules;
    private SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleActionsDefinition sqlVirtualNetworkRuleOperations;
    private SqlElasticPoolsAsExternalChildResourcesImpl sqlElasticPools;
    private SqlElasticPoolOperations.SqlElasticPoolActionsDefinition sqlElasticPoolOperations;
    private SqlDatabasesAsExternalChildResourcesImpl sqlDatabases;
    private SqlDatabaseOperations.SqlDatabaseActionsDefinition sqlDatabaseOperations;
    private SqlServerDnsAliasOperations.SqlServerDnsAliasActionsDefinition sqlServerDnsAliasOperations;
    private SqlFailoverGroupOperations.SqlFailoverGroupActionsDefinition sqlFailoverGroupOperations;
    private SqlServerKeyOperations.SqlServerKeyActionsDefinition sqlServerKeyOperations;
    private SqlServerSecurityAlertPolicyOperationsImpl sqlServerSecurityAlertPolicyOperations;
    private SqlEncryptionProtectorOperations.SqlEncryptionProtectorActionsDefinition sqlEncryptionProtectorsOperations;

    protected SqlServerImpl(String name, ServerInner innerObject, SqlServerManager manager) {
        super(name, innerObject, manager);

        this.sqlADAdminCreator = null;
        this.allowAzureServicesAccess = true;
        this.sqlFirewallRules = new SqlFirewallRulesAsExternalChildResourcesImpl(this, "SqlFirewallRule");
        this.sqlVirtualNetworkRules =
            new SqlVirtualNetworkRulesAsExternalChildResourcesImpl(this, "SqlVirtualNetworkRule");
        this.sqlElasticPools = new SqlElasticPoolsAsExternalChildResourcesImpl(this, "SqlElasticPool");
        this.sqlDatabases = new SqlDatabasesAsExternalChildResourcesImpl(this, "SqlDatabase");
    }

    @Override
    protected Mono<ServerInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getServers()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<SqlServer> createResourceAsync() {
        return this
            .manager()
            .serviceClient()
            .getServers()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(
                serverInner -> {
                    setInner(serverInner);
                    return this;
                });
    }

    @Override
    public void beforeGroupCreateOrUpdate() {
        if (this.isInCreateMode()) {
            if (allowAzureServicesAccess) {
                this
                    .sqlFirewallRules
                    .defineInlineFirewallRule("AllowAllWindowsAzureIps")
                    .withStartIpAddress("0.0.0.0")
                    .withEndIpAddress("0.0.0.0");
            }
            if (sqlADAdminCreator != null) {
                this.addPostRunDependent(sqlADAdminCreator);
            }
        }
        if (this.sqlElasticPools != null && this.sqlDatabases != null) {
            // Databases must be deleted before the Elastic Pools (only an empty Elastic Pool can be deleted)
            List<SqlDatabaseImpl> dbToBeRemoved =
                this.sqlDatabases.getChildren(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
            List<SqlElasticPoolImpl> epToBeRemoved =
                this.sqlElasticPools.getChildren(ExternalChildResourceImpl.PendingOperation.ToBeRemoved);
            for (SqlElasticPoolImpl epItem : epToBeRemoved) {
                for (SqlDatabaseImpl dbItem : dbToBeRemoved) {
                    epItem.addParentDependency(dbItem);
                }
            }

            // Databases in a new Elastic Pool should be created after the Elastic Pool
            List<SqlDatabaseImpl> dbToBeCreated =
                this.sqlDatabases.getChildren(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
            List<SqlElasticPoolImpl> epToBeCreated =
                this.sqlElasticPools.getChildren(ExternalChildResourceImpl.PendingOperation.ToBeCreated);
            for (SqlElasticPoolImpl epItem : epToBeCreated) {
                for (SqlDatabaseImpl dbItem : dbToBeCreated) {
                    if (dbItem.elasticPoolId() != null
                        && ResourceUtils.nameFromResourceId(dbItem.elasticPoolId()).equals(epItem.name())) {
                        dbItem.addParentDependency(epItem);
                    }
                }
            }
        }
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        this.sqlADAdminCreator = null;
        this.sqlFirewallRules.clear();
        this.sqlElasticPools.clear();
        this.sqlDatabases.clear();
        return Mono.empty();
    }

    @Override
    public String fullyQualifiedDomainName() {
        return this.innerModel().fullyQualifiedDomainName();
    }

    @Override
    public String administratorLogin() {
        return this.innerModel().administratorLogin();
    }

    @Override
    public String kind() {
        return this.innerModel().kind();
    }

    @Override
    public String state() {
        return this.innerModel().state();
    }

    @Override
    public boolean isManagedServiceIdentityEnabled() {
        return this.innerModel().identity() != null
            && this.innerModel().identity().type().equals(IdentityType.SYSTEM_ASSIGNED);
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        return this.innerModel().identity() != null ? this.innerModel().identity().tenantId().toString() : null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        return this.innerModel().identity() != null ? this.innerModel().identity().principalId().toString() : null;
    }

    @Override
    public IdentityType managedServiceIdentityType() {
        return this.innerModel().identity() != null ? this.innerModel().identity().type() : null;
    }

    @Override
    public List<ServerMetric> listUsageMetrics() {
        List<ServerMetric> serverMetrics = new ArrayList<>();
        PagedIterable<ServerUsageInner> serverUsageInners =
            this.manager().serviceClient().getServerUsages().listByServer(this.resourceGroupName(), this.name());
        for (ServerUsageInner serverUsageInner : serverUsageInners) {
            serverMetrics.add(new ServerMetricImpl(serverUsageInner));
        }
        return Collections.unmodifiableList(serverMetrics);
    }

    @Override
    public List<ServiceObjective> listServiceObjectives() {
        List<ServiceObjective> serviceObjectives = new ArrayList<>();
        PagedIterable<ServiceObjectiveInner> serviceObjectiveInners =
            this.manager().serviceClient().getServiceObjectives().listByServer(this.resourceGroupName(), this.name());
        for (ServiceObjectiveInner inner : serviceObjectiveInners) {
            serviceObjectives.add(new ServiceObjectiveImpl(inner, this));
        }
        return Collections.unmodifiableList(serviceObjectives);
    }

    @Override
    public ServiceObjective getServiceObjective(String serviceObjectiveName) {
        ServiceObjectiveInner inner =
            this
                .manager()
                .serviceClient()
                .getServiceObjectives()
                .get(this.resourceGroupName(), this.name(), serviceObjectiveName);
        return (inner != null) ? new ServiceObjectiveImpl(inner, this) : null;
    }

    @Override
    public Map<String, RecommendedElasticPool> listRecommendedElasticPools() {
        Map<String, RecommendedElasticPool> recommendedElasticPoolMap = new HashMap<>();
        PagedIterable<RecommendedElasticPoolInner> recommendedElasticPoolInners =
            this
                .manager()
                .serviceClient()
                .getRecommendedElasticPools()
                .listByServer(this.resourceGroupName(), this.name());
        for (RecommendedElasticPoolInner inner : recommendedElasticPoolInners) {
            recommendedElasticPoolMap.put(inner.name(), new RecommendedElasticPoolImpl(inner, this));
        }

        return Collections.unmodifiableMap(recommendedElasticPoolMap);
    }

    @Override
    public List<SqlRestorableDroppedDatabase> listRestorableDroppedDatabases() {
        List<SqlRestorableDroppedDatabase> sqlRestorableDroppedDatabases = new ArrayList<>();
        PagedIterable<RestorableDroppedDatabaseInner> restorableDroppedDatabasesInners =
            this
                .manager()
                .serviceClient()
                .getRestorableDroppedDatabases()
                .listByServer(this.resourceGroupName(), this.name());
        for (RestorableDroppedDatabaseInner restorableDroppedDatabaseInner : restorableDroppedDatabasesInners) {
            sqlRestorableDroppedDatabases
                .add(
                    new SqlRestorableDroppedDatabaseImpl(
                        this.resourceGroupName(), this.name(), restorableDroppedDatabaseInner, this.manager()));
        }
        return Collections.unmodifiableList(sqlRestorableDroppedDatabases);
    }

    @Override
    public PagedFlux<SqlRestorableDroppedDatabase> listRestorableDroppedDatabasesAsync() {
        final SqlServerImpl self = this;
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getRestorableDroppedDatabases()
            .listByServerAsync(this.resourceGroupName(), this.name()),
                restorableDroppedDatabaseInner ->
                    new SqlRestorableDroppedDatabaseImpl(
                        self.resourceGroupName(), self.name(), restorableDroppedDatabaseInner, self.manager()));
    }

    @Override
    public String version() {
        return this.innerModel().version();
    }

    @Override
    public SqlFirewallRule enableAccessFromAzureServices() {
        SqlFirewallRule firewallRule = null;
        try {
            firewallRule =
                this
                    .manager()
                    .sqlServers()
                    .firewallRules()
                    .getBySqlServer(this.resourceGroupName(), this.name(), "AllowAllWindowsAzureIps");
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() != 404) {
                throw logger.logExceptionAsError(e);
            }
        }

        if (firewallRule == null) {
            firewallRule =
                this
                    .manager()
                    .sqlServers()
                    .firewallRules()
                    .define("AllowAllWindowsAzureIps")
                    .withExistingSqlServer(this.resourceGroupName(), this.name())
                    .withIpAddress("0.0.0.0")
                    .create();
        }

        return firewallRule;
    }

    @Override
    public void removeAccessFromAzureServices() {
        SqlFirewallRule firewallRule =
            this
                .manager()
                .sqlServers()
                .firewallRules()
                .getBySqlServer(this.resourceGroupName(), this.name(), "AllowAllWindowsAzureIps");
        if (firewallRule != null) {
            this
                .manager()
                .sqlServers()
                .firewallRules()
                .deleteBySqlServer(this.resourceGroupName(), this.name(), "AllowAllWindowsAzureIps");
        }
    }

    @Override
    public SqlActiveDirectoryAdministratorImpl setActiveDirectoryAdministrator(String userLogin, String objectId) {
        ServerAzureADAdministratorInner serverAzureADAdministratorInner =
            new ServerAzureADAdministratorInner()
                .withLogin(userLogin)
                .withSid(UUID.fromString(objectId))
                .withTenantId(UUID.fromString(this.manager().tenantId()));

        return new SqlActiveDirectoryAdministratorImpl(
            this
                .manager()
                .serviceClient()
                .getServerAzureADAdministrators()
                .createOrUpdate(
                    this.resourceGroupName(),
                    this.name(),
                    AdministratorName.ACTIVE_DIRECTORY,
                    serverAzureADAdministratorInner));
    }

    @Override
    public SqlActiveDirectoryAdministratorImpl getActiveDirectoryAdministrator() {
        ServerAzureADAdministratorInner serverAzureADAdministratorInner =
            this
                .manager()
                .serviceClient()
                .getServerAzureADAdministrators()
                .get(this.resourceGroupName(), this.name(), AdministratorName.ACTIVE_DIRECTORY);

        return serverAzureADAdministratorInner != null
            ? new SqlActiveDirectoryAdministratorImpl(serverAzureADAdministratorInner)
            : null;
    }

    @Override
    public void removeActiveDirectoryAdministrator() {
        this
            .manager()
            .serviceClient()
            .getServerAzureADAdministrators()
            .delete(this.resourceGroupName(), this.name(), AdministratorName.ACTIVE_DIRECTORY);
    }

    @Override
    public SqlServerAutomaticTuning getServerAutomaticTuning() {
        ServerAutomaticTuningInner serverAutomaticTuningInner =
            this.manager().serviceClient().getServerAutomaticTunings().get(this.resourceGroupName(), this.name());
        return serverAutomaticTuningInner != null
            ? new SqlServerAutomaticTuningImpl(this, serverAutomaticTuningInner)
            : null;
    }

    @Override
    public SqlFirewallRuleOperations.SqlFirewallRuleActionsDefinition firewallRules() {
        if (this.sqlFirewallRuleOperations == null) {
            this.sqlFirewallRuleOperations = new SqlFirewallRuleOperationsImpl(this, this.manager());
        }
        return this.sqlFirewallRuleOperations;
    }

    @Override
    public SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleActionsDefinition virtualNetworkRules() {
        if (this.sqlVirtualNetworkRuleOperations == null) {
            this.sqlVirtualNetworkRuleOperations = new SqlVirtualNetworkRuleOperationsImpl(this, this.manager());
        }
        return this.sqlVirtualNetworkRuleOperations;
    }

    @Override
    public SqlServerImpl withAdministratorLogin(String administratorLogin) {
        this.innerModel().withAdministratorLogin(administratorLogin);
        return this;
    }

    @Override
    public SqlServerImpl withAdministratorPassword(String administratorLoginPassword) {
        this.innerModel().withAdministratorLoginPassword(administratorLoginPassword);
        return this;
    }

    @Override
    public SqlServerImpl withoutAccessFromAzureServices() {
        allowAzureServicesAccess = false;
        return this;
    }

    @Override
    public SqlServer.DefinitionStages.WithCreate withActiveDirectoryAdministrator(
        final String userLogin, final String objectId) {
        final SqlServerImpl self = this;
        sqlADAdminCreator =
            context -> {
                ServerAzureADAdministratorInner serverAzureADAdministratorInner =
                    new ServerAzureADAdministratorInner()
                        .withLogin(userLogin)
                        .withSid(UUID.fromString(objectId))
                        .withTenantId(UUID.fromString(self.manager().tenantId()));

                return self
                    .manager()
                    .serviceClient()
                    .getServerAzureADAdministrators()
                    .createOrUpdateAsync(
                        self.resourceGroupName(),
                        self.name(),
                        AdministratorName.ACTIVE_DIRECTORY,
                        serverAzureADAdministratorInner)
                    .flatMap(serverAzureADAdministratorInner1 -> context.voidMono());
            };
        return this;
    }

    @Override
    public SqlFirewallRuleImpl defineFirewallRule(String name) {
        return this.sqlFirewallRules.defineInlineFirewallRule(name);
    }

    @Override
    public SqlServerImpl withoutFirewallRule(String firewallRuleName) {
        sqlFirewallRules.removeInlineFirewallRule(firewallRuleName);
        return this;
    }

    @Override
    public SqlVirtualNetworkRule.DefinitionStages.Blank<SqlServer.DefinitionStages.WithCreate> defineVirtualNetworkRule(
        String virtualNetworkRuleName) {
        return this.sqlVirtualNetworkRules.defineInlineVirtualNetworkRule(virtualNetworkRuleName);
    }

    @Override
    public SqlElasticPoolOperations.SqlElasticPoolActionsDefinition elasticPools() {
        if (this.sqlElasticPoolOperations == null) {
            this.sqlElasticPoolOperations = new SqlElasticPoolOperationsImpl(this, this.manager());
        }
        return this.sqlElasticPoolOperations;
    }

    @Override
    public SqlDatabaseOperations.SqlDatabaseActionsDefinition databases() {
        if (this.sqlDatabaseOperations == null) {
            this.sqlDatabaseOperations = new SqlDatabaseOperationsImpl(this, this.manager());
        }
        return this.sqlDatabaseOperations;
    }

    @Override
    public SqlServerDnsAliasOperations.SqlServerDnsAliasActionsDefinition dnsAliases() {
        if (this.sqlServerDnsAliasOperations == null) {
            this.sqlServerDnsAliasOperations = new SqlServerDnsAliasOperationsImpl(this, this.manager());
        }
        return this.sqlServerDnsAliasOperations;
    }

    @Override
    public SqlFailoverGroupOperations.SqlFailoverGroupActionsDefinition failoverGroups() {
        if (this.sqlFailoverGroupOperations == null) {
            this.sqlFailoverGroupOperations = new SqlFailoverGroupOperationsImpl(this, this.manager());
        }
        return this.sqlFailoverGroupOperations;
    }

    @Override
    public SqlServerKeyOperations.SqlServerKeyActionsDefinition serverKeys() {
        if (this.sqlServerKeyOperations == null) {
            this.sqlServerKeyOperations = new SqlServerKeyOperationsImpl(this, this.manager());
        }
        return this.sqlServerKeyOperations;
    }

    @Override
    public SqlEncryptionProtectorOperations.SqlEncryptionProtectorActionsDefinition encryptionProtectors() {
        if (this.sqlEncryptionProtectorsOperations == null) {
            this.sqlEncryptionProtectorsOperations = new SqlEncryptionProtectorOperationsImpl(this, this.manager());
        }
        return this.sqlEncryptionProtectorsOperations;
    }

    @Override
    public SqlServerSecurityAlertPolicyOperations.SqlServerSecurityAlertPolicyActionsDefinition
        serverSecurityAlertPolicies() {
        if (this.sqlServerSecurityAlertPolicyOperations == null) {
            this.sqlServerSecurityAlertPolicyOperations =
                new SqlServerSecurityAlertPolicyOperationsImpl(this, this.manager());
        }
        return this.sqlServerSecurityAlertPolicyOperations;
    }

    @Override
    public SqlElasticPoolImpl defineElasticPool(String name) {
        return this.sqlElasticPools.defineInlineElasticPool(name);
    }

    @Override
    public SqlServerImpl withoutElasticPool(String elasticPoolName) {
        sqlElasticPools.removeInlineElasticPool(elasticPoolName);
        return this;
    }

    @Override
    public SqlDatabaseImpl defineDatabase(String name) {
        return this.sqlDatabases.defineInlineDatabase(name);
    }

    @Override
    public SqlServerImpl withoutDatabase(String databaseName) {
        this.sqlDatabases.removeInlineDatabase(databaseName);
        return this;
    }

    @Override
    public SqlServerImpl withSystemAssignedManagedServiceIdentity() {
        this.innerModel().withIdentity(new ResourceIdentity().withType(IdentityType.SYSTEM_ASSIGNED));
        return this;
    }
}
