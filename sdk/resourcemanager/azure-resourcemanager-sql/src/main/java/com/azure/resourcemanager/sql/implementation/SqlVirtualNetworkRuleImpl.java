// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRule;
import com.azure.resourcemanager.sql.models.SqlVirtualNetworkRuleOperations;
import com.azure.resourcemanager.sql.fluent.models.VirtualNetworkRuleInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Virtual Network Rule interface. */
public class SqlVirtualNetworkRuleImpl
    extends ExternalChildResourceImpl<SqlVirtualNetworkRule, VirtualNetworkRuleInner, SqlServerImpl, SqlServer>
    implements SqlVirtualNetworkRule,
        SqlVirtualNetworkRule.SqlVirtualNetworkRuleDefinition<SqlServer.DefinitionStages.WithCreate>,
        SqlVirtualNetworkRule.Update,
        SqlVirtualNetworkRuleOperations.SqlVirtualNetworkRuleOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param name the name of this external child resource
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses virtual network rule operations
     */
    SqlVirtualNetworkRuleImpl(
        String name, SqlServerImpl parent, VirtualNetworkRuleInner innerObject, SqlServerManager sqlServerManager) {
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
     * @param sqlServerManager reference to the SQL server manager that accesses virtual network rule operations
     */
    SqlVirtualNetworkRuleImpl(
        String resourceGroupName,
        String sqlServerName,
        String name,
        VirtualNetworkRuleInner innerObject,
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
     * @param sqlServerManager reference to the SQL server manager that accesses virtual network rule operations
     */
    SqlVirtualNetworkRuleImpl(String name, VirtualNetworkRuleInner innerObject, SqlServerManager sqlServerManager) {
        super(name, null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
    }

    @Override
    public Mono<SqlVirtualNetworkRule> createResourceAsync() {
        final SqlVirtualNetworkRuleImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getVirtualNetworkRules()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.name(), this.innerModel())
            .map(
                inner -> {
                    self.setInner(inner);
                    return self;
                });
    }

    @Override
    public Mono<SqlVirtualNetworkRule> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getVirtualNetworkRules()
            .deleteAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    protected Mono<VirtualNetworkRuleInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getVirtualNetworkRules()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.name());
    }

    @Override
    public SqlVirtualNetworkRuleImpl update() {
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
    public String subnetId() {
        return this.innerModel().virtualNetworkSubnetId();
    }

    @Override
    public String state() {
        return this.innerModel().state().toString();
    }

    @Override
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.id());
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
    public SqlVirtualNetworkRuleImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlVirtualNetworkRuleImpl withExistingSqlServerId(String sqlServerId) {
        Objects.requireNonNull(sqlServerId);
        ResourceId resourceId = ResourceId.fromString(sqlServerId);
        this.resourceGroupName = resourceId.resourceGroupName();
        this.sqlServerName = resourceId.name();
        return this;
    }

    @Override
    public SqlVirtualNetworkRuleImpl withExistingSqlServer(SqlServer sqlServer) {
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        return this;
    }

    @Override
    public SqlVirtualNetworkRuleImpl withSubnet(String networkId, String subnetName) {
        this.innerModel().withVirtualNetworkSubnetId(networkId + "/subnets/" + subnetName);
        this.innerModel().withIgnoreMissingVnetServiceEndpoint(false);
        return this;
    }

    @Override
    public SqlVirtualNetworkRuleImpl ignoreMissingSqlServiceEndpoint() {
        this.innerModel().withIgnoreMissingVnetServiceEndpoint(true);
        return this;
    }

    @Override
    public SqlServer.DefinitionStages.WithCreate attach() {
        return parent();
    }
}
