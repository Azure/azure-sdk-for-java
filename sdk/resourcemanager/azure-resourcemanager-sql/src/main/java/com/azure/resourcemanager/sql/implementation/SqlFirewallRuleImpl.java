// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.FirewallRuleInner;
import com.azure.resourcemanager.sql.models.SqlFirewallRule;
import com.azure.resourcemanager.sql.models.SqlFirewallRuleOperations;
import com.azure.resourcemanager.sql.models.SqlServer;
import reactor.core.publisher.Mono;

import java.util.Objects;

/** Implementation for SqlFirewallRule. */
public class SqlFirewallRuleImpl
    extends ExternalChildResourceImpl<SqlFirewallRule, FirewallRuleInner, SqlServerImpl, SqlServer>
    implements SqlFirewallRule,
        SqlFirewallRule.SqlFirewallRuleDefinition<SqlServerImpl>,
        SqlFirewallRule.Update,
        SqlFirewallRuleOperations.SqlFirewallRuleOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlFirewallRuleImpl(
        String name, SqlServerImpl parent, FirewallRuleInner innerObject, SqlServerManager sqlServerManager) {
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
     * @param name the name of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlFirewallRuleImpl(
        String resourceGroupName,
        String sqlServerName,
        String name,
        FirewallRuleInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
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
        return this
            .sqlServerManager
            .serviceClient()
            .getFirewallRules()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public SqlFirewallRuleImpl update() {
        super.prepareUpdate();

        return this;
    }

    @Override
    public String id() {
        return this.innerModel().id();
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
    public String startIpAddress() {
        return this.innerModel().startIpAddress();
    }

    @Override
    public String endIpAddress() {
        return this.innerModel().endIpAddress();
    }

    @Override
    public String kind() {
        return this.innerModel().kind();
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().location());
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
        return this
            .sqlServerManager
            .serviceClient()
            .getFirewallRules()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.innerModel())
            .map(
                inner -> {
                    self.setInner(inner);
                    return self;
                });
    }

    @Override
    public Mono<SqlFirewallRule> updateResourceAsync() {
        final SqlFirewallRuleImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getFirewallRules()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.innerModel())
            .map(
                inner -> {
                    self.setInner(inner);
                    return self;
                });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getFirewallRules()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public SqlFirewallRuleImpl withStartIpAddress(String startIpAddress) {
        this.innerModel().withStartIpAddress(startIpAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withEndIpAddress(String endIpAddress) {
        this.innerModel().withEndIpAddress(endIpAddress);
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
    public SqlFirewallRuleImpl withIpAddressRange(String startIpAddress, String endIpAddress) {
        this.innerModel().withStartIpAddress(startIpAddress);
        this.innerModel().withEndIpAddress(endIpAddress);
        return this;
    }

    @Override
    public SqlFirewallRuleImpl withIpAddress(String ipAddress) {
        this.innerModel().withStartIpAddress(ipAddress);
        this.innerModel().withEndIpAddress(ipAddress);
        return this;
    }

    @Override
    public SqlServerImpl attach() {
        return parent();
    }
}
