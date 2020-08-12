// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyEmailAccountAdmins;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyName;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyState;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyUseServerDefault;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseThreatDetectionPolicy;
import com.azure.resourcemanager.sql.fluent.inner.DatabaseSecurityAlertPolicyInner;
import java.util.Objects;
import reactor.core.publisher.Mono;

/** Implementation for SQL database threat detection policy. */
public class SqlDatabaseThreatDetectionPolicyImpl
    extends ExternalChildResourceImpl<
        SqlDatabaseThreatDetectionPolicy, DatabaseSecurityAlertPolicyInner, SqlDatabaseImpl, SqlDatabase>
    implements SqlDatabaseThreatDetectionPolicy,
        SqlDatabaseThreatDetectionPolicy.SqlDatabaseThreatDetectionPolicyDefinition,
        SqlDatabaseThreatDetectionPolicy.Update {
    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    protected SqlDatabaseThreatDetectionPolicyImpl(
        String name,
        SqlDatabaseImpl parent,
        DatabaseSecurityAlertPolicyInner innerObject,
        SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);
        Objects.requireNonNull(parent);
        Objects.requireNonNull(innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.sqlServerName();

        this.inner().withLocation(parent.regionName());
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public Region region() {
        return Region.fromName(this.inner().location());
    }

    @Override
    public String kind() {
        return this.inner().kind();
    }

    @Override
    public SecurityAlertPolicyState currentState() {
        return this.inner().state();
    }

    @Override
    public String disabledAlerts() {
        return this.inner().disabledAlerts();
    }

    @Override
    public String emailAddresses() {
        return this.inner().emailAddresses();
    }

    @Override
    public boolean emailAccountAdmins() {
        return this.inner().emailAccountAdmins() == SecurityAlertPolicyEmailAccountAdmins.ENABLED;
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
    public boolean isDefaultSecurityAlertPolicy() {
        return this.inner().useServerDefault() == SecurityAlertPolicyUseServerDefault.ENABLED;
    }

    @Override
    protected Mono<DatabaseSecurityAlertPolicyInner> getInnerAsync() {
        return this
            .sqlServerManager
            .inner()
            .getDatabaseThreatDetectionPolicies()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.parent().name(),
                SecurityAlertPolicyName.DEFAULT);
    }

    @Override
    public Mono<SqlDatabaseThreatDetectionPolicy> createResourceAsync() {
        final SqlDatabaseThreatDetectionPolicyImpl self = this;
        return this
            .sqlServerManager
            .inner()
            .getDatabaseThreatDetectionPolicies()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.parent().name(),
                SecurityAlertPolicyName.DEFAULT, this.inner())
            .map(
                databaseSecurityAlertPolicyInner -> {
                    self.setInner(databaseSecurityAlertPolicyInner);
                    return self;
                });
    }

    @Override
    public Mono<SqlDatabaseThreatDetectionPolicy> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public Update update() {
        super.prepareUpdate();
        return this;
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return null;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyEnabled() {
        this.inner().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.inner().withState(SecurityAlertPolicyState.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyDisabled() {
        this.inner().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.inner().withState(SecurityAlertPolicyState.DISABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyNew() {
        this.inner().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.inner().withState(SecurityAlertPolicyState.NEW);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withDefaultSecurityAlertPolicy() {
        this.inner().withUseServerDefault(SecurityAlertPolicyUseServerDefault.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withStorageEndpoint(String storageEndpoint) {
        this.inner().withStorageEndpoint(storageEndpoint);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withStorageAccountAccessKey(String storageAccountAccessKey) {
        this.inner().withStorageAccountAccessKey(storageAccountAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withAlertsFilter(String alertsFilter) {
        this.inner().withDisabledAlerts(alertsFilter);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailAddresses(String addresses) {
        this.inner().withEmailAddresses(addresses);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withRetentionDays(int retentionDays) {
        this.inner().withRetentionDays(retentionDays);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailToAccountAdmins() {
        this.inner().withEmailAccountAdmins(SecurityAlertPolicyEmailAccountAdmins.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withoutEmailToAccountAdmins() {
        this.inner().withEmailAccountAdmins(SecurityAlertPolicyEmailAccountAdmins.DISABLED);
        return this;
    }
}
