/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.sql.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.management.sql.CheckNameAvailabilityResult;
import com.azure.management.sql.RegionCapabilities;
import com.azure.management.sql.SqlDatabaseOperations;
import com.azure.management.sql.SqlElasticPoolOperations;
import com.azure.management.sql.SqlEncryptionProtectorOperations;
import com.azure.management.sql.SqlFirewallRuleOperations;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.SqlServerKeyOperations;
import com.azure.management.sql.SqlServerSecurityAlertPolicyOperations;
import com.azure.management.sql.SqlServers;
import com.azure.management.sql.SqlSubscriptionUsageMetric;
import com.azure.management.sql.SqlSyncGroupOperations;
import com.azure.management.sql.SqlSyncMemberOperations;
import com.azure.management.sql.SqlVirtualNetworkRuleOperations;
import com.azure.management.sql.models.LocationCapabilitiesInner;
import com.azure.management.sql.models.ServerInner;
import com.azure.management.sql.models.ServersInner;
import com.azure.management.sql.models.SubscriptionUsageInner;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Implementation for SqlServers and its parent interfaces.
 */
class SqlServersImpl
        extends TopLevelModifiableResourcesImpl<
                SqlServer,
                    SqlServerImpl,
                ServerInner,
                ServersInner,
                    SqlServerManager>
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

    protected SqlServersImpl(SqlServerManager manager) {
        super(manager.inner().servers(), manager);
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

        return new SqlServerImpl(inner.getName(), inner, this.manager());
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
        return this.inner().checkNameAvailabilityAsync(name)
            .map(checkNameAvailabilityResponseInner -> new CheckNameAvailabilityResultImpl(checkNameAvailabilityResponseInner));
    }

    @Override
    public RegionCapabilities getCapabilitiesByRegion(Region region) {
        LocationCapabilitiesInner capabilitiesInner = this.manager().inner().capabilities()
            .listByLocation(region.name());
        return capabilitiesInner != null ? new RegionCapabilitiesImpl(capabilitiesInner) : null;
    }

    @Override
    public Mono<RegionCapabilities> getCapabilitiesByRegionAsync(Region region) {
        return this.manager().inner().capabilities()
            .listByLocationAsync(region.name())
            .map(capabilitiesInner -> new RegionCapabilitiesImpl(capabilitiesInner));
    }

    @Override
    public List<SqlSubscriptionUsageMetric> listUsageByRegion(Region region) {
        Objects.requireNonNull(region);
        List<SqlSubscriptionUsageMetric> subscriptionUsages = new ArrayList<>();
        PagedIterable<SubscriptionUsageInner> subscriptionUsageInners = this.manager().inner().subscriptionUsages()
            .listByLocation(region.name());
        if (subscriptionUsageInners != null) {
            for (SubscriptionUsageInner inner : subscriptionUsageInners) {
                subscriptionUsages.add(new SqlSubscriptionUsageMetricImpl(region.name(), inner, this.manager()));
            }
        }
        return Collections.unmodifiableList(subscriptionUsages);
    }

    @Override
    public PagedFlux<SqlSubscriptionUsageMetric> listUsageByRegionAsync(final Region region) {
        Objects.requireNonNull(region);
        final SqlServers self = this;
        return this.manager().inner().subscriptionUsages()
            .listByLocationAsync(region.name())
            .mapPage(subscriptionUsageInner -> new SqlSubscriptionUsageMetricImpl(region.name(), subscriptionUsageInner, self.manager()));
    }
}
