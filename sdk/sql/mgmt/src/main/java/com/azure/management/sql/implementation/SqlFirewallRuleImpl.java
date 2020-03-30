/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.sql.implementation;

import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.ResourceId;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.management.sql.SqlFirewallRule;
import com.azure.management.sql.SqlFirewallRuleOperations;
import com.azure.management.sql.SqlServer;
import com.azure.management.sql.models.FirewallRuleInner;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Implementation for SqlFirewallRule.
 */
public class SqlFirewallRuleImpl
    extends
        ExternalChildResourceImpl<SqlFirewallRule, FirewallRuleInner, SqlServerImpl, SqlServer>
    implements
        SqlFirewallRule,
        SqlFirewallRule.SqlFirewallRuleDefinition<SqlServer.DefinitionStages.WithCreate>,
        SqlFirewallRule.Update,
        SqlFirewallRuleOperations.SqlFirewallRuleOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param parent      reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlFirewallRuleImpl(String name, SqlServerImpl parent, FirewallRuleInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);

        Objects.requireNonNull(parent);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.name();
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param resourceGroupName the resource group name
     * @param sqlServerName the parent SQL server name
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlFirewallRuleImpl(String resourceGroupName, String sqlServerName, String name, FirewallRuleInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name        the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlFirewallRuleImpl(String name, FirewallRuleInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    protected Mono<FirewallRuleInner> getInnerAsync() {
        return this.sqlServerManager.inner().firewallRules().getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public SqlFirewallRuleImpl update() {
        super.prepareUpdate();

        return this;
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String sqlServerName() {
        return this.sqlServerName;
    }

    @Override
    public String startIPAddress() {
        return this.inner().startIpAddress();
    }

    @Override
    public String endIPAddress() {
        return this.inner().endIpAddress();
    }

    @Override
    public String kind() {
        return this.inner().kind();
    }

    @Override
    public Region region() {
        return Region.fromName(this.inner().location());
    }

    @Override
    public void delete() {
        this.deleteResourceAsync().block();
    }

    @Override
    public Mono<Void> deleteAsync() {
        return this.deleteResourceAsync();
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
    }

    @Override
    public Mono<SqlFirewallRule> createResourceAsync() {
        final SqlFirewallRuleImpl self = this;
        return this.sqlServerManager.inner().firewallRules()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<SqlFirewallRule> updateResourceAsync() {
        final SqlFirewallRuleImpl self = this;
        return this.sqlServerManager.inner().firewallRules()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.inner())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.sqlServerManager.inner().firewallRules().deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public SqlFirewallRuleImpl withStartIPAddress(String startIPAddress) {
        this.inner().withStartIpAddress(startIPAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withEndIPAddress(String endIPAddress) {
        this.inner().withEndIpAddress(endIPAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withExistingSqlServer(SqlServer sqlServer) {
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withExistingSqlServerId(String sqlServerId) {
        Objects.requireNonNull(sqlServerId);
        ResourceId resourceId = ResourceId.fromString(sqlServerId);
        this.resourceGroupName = resourceId.resourceGroupName();
        this.sqlServerName = resourceId.name();
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withIPAddressRange(String startIPAddress, String endIPAddress) {
        this.inner().withStartIpAddress(startIPAddress);
        this.inner().withEndIpAddress(endIPAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withIPAddress(String ipAddress) {
        this.inner().withStartIpAddress(ipAddress);
        this.inner().withEndIpAddress(ipAddress);
        return this;
    }

    @Override
    public SqlServerImpl attach() {
        return parent();
    }
}
