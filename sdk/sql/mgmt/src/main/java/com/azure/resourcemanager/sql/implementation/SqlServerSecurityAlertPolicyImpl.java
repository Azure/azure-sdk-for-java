// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyState;
import com.azure.resourcemanager.sql.models.SqlServer;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicy;
import com.azure.resourcemanager.sql.models.SqlServerSecurityAlertPolicyOperations;
import com.azure.resourcemanager.sql.fluent.inner.ServerSecurityAlertPolicyInner;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL Server Security Alert Policy interface. */
public class SqlServerSecurityAlertPolicyImpl
    extends ExternalChildResourceImpl<
        SqlServerSecurityAlertPolicy, ServerSecurityAlertPolicyInner, SqlServerImpl, SqlServer>
    implements SqlServerSecurityAlertPolicy,
        SqlServerSecurityAlertPolicy.Update,
        SqlServerSecurityAlertPolicyOperations.SqlServerSecurityAlertPolicyOperationsDefinition {

    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param parent reference to the parent of this external child resource
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerSecurityAlertPolicyImpl(
        SqlServerImpl parent, ServerSecurityAlertPolicyInner innerObject, SqlServerManager sqlServerManager) {
        super("Default", parent, innerObject);

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
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerSecurityAlertPolicyImpl(
        String resourceGroupName,
        String sqlServerName,
        ServerSecurityAlertPolicyInner innerObject,
        SqlServerManager sqlServerManager) {
        super("Default", null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
    }

    /**
     * Creates an instance of external child resource in-memory.
     *
     * @param innerObject reference to the inner object representing this external child resource
     * @param sqlServerManager reference to the SQL server manager that accesses firewall rule operations
     */
    SqlServerSecurityAlertPolicyImpl(ServerSecurityAlertPolicyInner innerObject, SqlServerManager sqlServerManager) {
        super("Default", null, innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        if (innerObject != null && innerObject.id() != null) {
            try {
                ResourceId resourceId = ResourceId.fromString(innerObject.id());
                this.resourceGroupName = resourceId.resourceGroupName();
                this.sqlServerName = resourceId.parent().name();
            } catch (NullPointerException e) {
            }
        }
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String name() {
        return "Default";
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
    public String parentId() {
        return ResourceUtils.parentResourceIdFromResourceId(this.inner().id());
    }

    @Override
    public SecurityAlertPolicyState state() {
        return this.inner().state();
    }

    @Override
    public List<String> disabledAlerts() {
        return Collections.unmodifiableList(this.inner().disabledAlerts());
    }

    @Override
    public List<String> emailAddresses() {
        return Collections.unmodifiableList(this.inner().emailAddresses());
    }

    @Override
    public boolean emailAccountAdmins() {
        return this.inner().emailAccountAdmins();
    }

    @Override
    public String storageEndpoint() {
        return this.inner().storageEndpoint();
    }

    @Override
    public String storageAccountAccessKey() {
        return this.inner().storageAccountAccessKey();
    }

    @Override
    public int retentionDays() {
        return this.inner().retentionDays();
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withExistingSqlServer(String resourceGroupName, String sqlServerName) {
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withExistingSqlServerId(String sqlServerId) {
        Objects.requireNonNull(sqlServerId);
        ResourceId resourceId = ResourceId.fromString(sqlServerId);
        this.resourceGroupName = resourceId.resourceGroupName();
        this.sqlServerName = resourceId.name();
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withExistingSqlServer(SqlServer sqlServer) {
        Objects.requireNonNull(sqlServer);
        this.resourceGroupName = sqlServer.resourceGroupName();
        this.sqlServerName = sqlServer.name();
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl update() {
        super.prepareUpdate();
        // storageAccountAccessKey parameter can not be null when storageEndpoint parameter is not null
        this.inner().withStorageEndpoint(null);
        return this;
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> createResourceAsync() {
        final SqlServerSecurityAlertPolicyImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getServerSecurityAlertPolicies()
            .createOrUpdateAsync(self.resourceGroupName, self.sqlServerName, self.inner())
            .map(
                serverSecurityAlertPolicyInner -> {
                    self.setInner(serverSecurityAlertPolicyInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlServerSecurityAlertPolicy> updateResourceAsync() {
        this.inner().withStorageEndpoint(null);
        return createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return null;
    }

    @Override
    protected Mono<ServerSecurityAlertPolicyInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getServerSecurityAlertPolicies()
            .getAsync(this.resourceGroupName, this.sqlServerName);
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withState(SecurityAlertPolicyState state) {
        this.inner().withState(state);
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withEmailAccountAdmins() {
        this.inner().withEmailAccountAdmins(true);
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withoutEmailAccountAdmins() {
        this.inner().withEmailAccountAdmins(false);
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withStorageEndpoint(String storageEndpointUri, String storageAccessKey) {
        this.inner().withStorageEndpoint(storageEndpointUri);
        this.inner().withStorageAccountAccessKey(storageAccessKey);
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withEmailAddresses(String... emailAddresses) {
        this.inner().withEmailAddresses(Arrays.asList(emailAddresses));
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withDisabledAlerts(String... disabledAlerts) {
        this.inner().withDisabledAlerts(Arrays.asList(disabledAlerts));
        return this;
    }

    @Override
    public SqlServerSecurityAlertPolicyImpl withRetentionDays(int days) {
        this.inner().withRetentionDays(days);
        return this;
    }
}
