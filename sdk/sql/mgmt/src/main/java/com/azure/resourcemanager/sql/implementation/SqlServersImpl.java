// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.ServersClient;
import com.azure.resourcemanager.sql.fluent.inner.LocationCapabilitiesInner;
import com.azure.resourcemanager.sql.fluent.inner.ServerInner;
import com.azure.resourcemanager.sql.fluent.inner.SubscriptionUsageInner;
import com.azure.resourcemanager.sql.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.sql.models.RegionCapabilities;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlElasticPoolOperations;
import com.azure.resourcemanager.sql.models.SqlEncryptionProtectorOperations;
import com.azure.resourcemanager.sql.models.SqlFirewallRuleOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerKeyOperations;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicyOperations;
import com.azure.resourcemanager.sql.models.SqlServers;
import com.azure.resourcemanager.sql.models.SqlSubscriptionUsageMetric;
import com.azure.resourcemanager.sql.models.SqlSyncGroupOperations;
import com.azure.resourcemanager.sql.models.SqlSyncMemberOperations;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRuleOperations;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Implementation for SqlServers and its parent interfaces. */
public class SqlServersImpl
    extends TopLevelModifiableResourcesImpl<SqlServer, SqlServerImpl, ServerInner, ServersClient, SqlServerManager>
    implements SqlServers {

    private SqlFirewallRuleOperations firewallRules;
    private SqlVirtualNetworkRuleOperations virtualNetworkRules;
    private SqlElasticPoolOperations elasticPools;
    private SqlDatabaseOperations databases;
    private SqlServerDnsAliasOperationsImpl dnsAliases;
    private SqlFailoverGroupOperationsImpl failoverGroups;
    private SqlServerKeyOperationsImpl serverKeys;
    private SqlEncryptionProtectorOperationsImpl encryptionProtectors;
    private SqlSyncGroupOperationsImpl syncGroups;
    private SqlSyncMemberOperationsImpl syncMembers;
    private SqlServerSecurityAlertPolicyOperationsImpl serverSecurityAlertPolicies;

    public SqlServersImpl(SqlServerManager manager) {
        super(manager.inner().getServers(), manager);
    }

    @Override
    protected SqlServerImpl wrapModel(String name) {
        ServerInner inner = new ServerInner();
        return new SqlServerImpl(name, inner, this.manager());
    }

    @Override
    protected SqlServerImpl wrapModel(ServerInner inner) {
        if (inner == null) {
            return null;
        }

        return new SqlServerImpl(inner.name(), inner, this.manager());
    }

    @Override
    public SqlServer.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public SqlFirewallRuleOperations firewallRules() {
        if (this.firewallRules == null) {
            this.firewallRules = new SqlFirewallRuleOperationsImpl(this.manager());
        }

        return this.firewallRules;
    }

    @Override
    public SqlVirtualNetworkRuleOperations virtualNetworkRules() {
        if (this.virtualNetworkRules == null) {
            this.virtualNetworkRules = new SqlVirtualNetworkRuleOperationsImpl(this.manager());
        }

        return this.virtualNetworkRules;
    }

    @Override
    public SqlServerDnsAliasOperationsImpl dnsAliases() {
        if (this.dnsAliases == null) {
            this.dnsAliases = new SqlServerDnsAliasOperationsImpl(this.manager());
        }

        return this.dnsAliases;
    }

    @Override
    public SqlFailoverGroupOperationsImpl failoverGroups() {
        if (this.failoverGroups == null) {
            this.failoverGroups = new SqlFailoverGroupOperationsImpl(this.manager());
        }

        return this.failoverGroups;
    }

    @Override
    public SqlServerKeyOperations serverKeys() {
        if (this.serverKeys == null) {
            this.serverKeys = new SqlServerKeyOperationsImpl(this.manager());
        }

        return this.serverKeys;
    }

    @Override
    public SqlEncryptionProtectorOperations encryptionProtectors() {
        if (this.encryptionProtectors == null) {
            this.encryptionProtectors = new SqlEncryptionProtectorOperationsImpl(this.manager());
        }

        return this.encryptionProtectors;
    }

    @Override
    public SqlServerSecurityAlertPolicyOperations serverSecurityAlertPolicies() {
        if (this.serverSecurityAlertPolicies == null) {
            this.serverSecurityAlertPolicies = new SqlServerSecurityAlertPolicyOperationsImpl(this.manager());
        }

        return this.serverSecurityAlertPolicies;
    }

    @Override
    public SqlSyncGroupOperations syncGroups() {
        if (this.syncGroups == null) {
            this.syncGroups = new SqlSyncGroupOperationsImpl(this.manager());
        }

        return this.syncGroups;
    }

    @Override
    public SqlSyncMemberOperations syncMembers() {
        if (this.syncMembers == null) {
            this.syncMembers = new SqlSyncMemberOperationsImpl(this.manager());
        }

        return this.syncMembers;
    }

    @Override
    public SqlElasticPoolOperations elasticPools() {
        if (this.elasticPools == null) {
            this.elasticPools = new SqlElasticPoolOperationsImpl(this.manager());
        }

        return this.elasticPools;
    }

    @Override
    public SqlDatabaseOperations databases() {
        if (this.databases == null) {
            this.databases = new SqlDatabaseOperationsImpl(this.manager());
        }

        return this.databases;
    }

    @Override
    public CheckNameAvailabilityResult checkNameAvailability(String name) {
        return new CheckNameAvailabilityResultImpl(this.inner().checkNameAvailability(name));
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkNameAvailabilityAsync(String name) {
        return this
            .inner()
            .checkNameAvailabilityAsync(name)
            .map(
                CheckNameAvailabilityResultImpl::new);
    }

    @Override
    public RegionCapabilities getCapabilitiesByRegion(Region region) {
        LocationCapabilitiesInner capabilitiesInner =
            this.manager().inner().getCapabilities().listByLocation(region.name());
        return capabilitiesInner != null ? new RegionCapabilitiesImpl(capabilitiesInner) : null;
    }

    @Override
    public Mono<RegionCapabilities> getCapabilitiesByRegionAsync(Region region) {
        return this
            .manager()
            .inner()
            .getCapabilities()
            .listByLocationAsync(region.name())
            .map(RegionCapabilitiesImpl::new);
    }

    @Override
    public List<SqlSubscriptionUsageMetric> listUsageByRegion(Region region) {
        Objects.requireNonNull(region);
        List<SqlSubscriptionUsageMetric> subscriptionUsages = new ArrayList<>();
        PagedIterable<SubscriptionUsageInner> subscriptionUsageInners =
            this.manager().inner().getSubscriptionUsages().listByLocation(region.name());
        for (SubscriptionUsageInner inner : subscriptionUsageInners) {
            subscriptionUsages.add(new SqlSubscriptionUsageMetricImpl(region.name(), inner, this.manager()));
        }
        return Collections.unmodifiableList(subscriptionUsages);
    }

    @Override
    public PagedFlux<SqlSubscriptionUsageMetric> listUsageByRegionAsync(final Region region) {
        Objects.requireNonNull(region);
        final SqlServers self = this;
        return this
            .manager()
            .inner()
            .getSubscriptionUsages()
            .listByLocationAsync(region.name())
            .mapPage(
                subscriptionUsageInner ->
                    new SqlSubscriptionUsageMetricImpl(region.name(), subscriptionUsageInner, self.manager()));
    }
}
