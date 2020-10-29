// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyEmailAccountAdmins;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyName;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyState;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyUseServerDefault;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseThreatDetectionPolicy;
import com.azure.resourcemanager.sql.fluent.models.DatabaseSecurityAlertPolicyInner;
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

        this.innerModel().withLocation(parent.regionName());
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String resourceGroupName() {
        return this.resourceGroupName;
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public Region region() {
        return Region.fromName(this.innerModel().location());
    }

    @Override
    public String kind() {
        return this.innerModel().kind();
    }

    @Override
    public SecurityAlertPolicyState currentState() {
        return this.innerModel().state();
    }

    @Override
    public String disabledAlerts() {
        return this.innerModel().disabledAlerts();
    }

    @Override
    public String emailAddresses() {
        return this.innerModel().emailAddresses();
    }

    @Override
    public boolean emailAccountAdmins() {
        return this.innerModel().emailAccountAdmins() == SecurityAlertPolicyEmailAccountAdmins.ENABLED;
    }

    @Override
    public String storageEndpoint() {
        return this.innerModel().storageEndpoint();
    }

    @Override
    public String storageAccountAccessKey() {
        return this.innerModel().storageAccountAccessKey();
    }

    @Override
    public int retentionDays() {
        return this.innerModel().retentionDays();
    }

    @Override
    public boolean isDefaultSecurityAlertPolicy() {
        return this.innerModel().useServerDefault() == SecurityAlertPolicyUseServerDefault.ENABLED;
    }

    @Override
    protected Mono<DatabaseSecurityAlertPolicyInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabaseThreatDetectionPolicies()
            .getAsync(
                this.resourceGroupName, this.sqlServerName, this.parent().name(), SecurityAlertPolicyName.DEFAULT);
    }

    @Override
    public Mono<SqlDatabaseThreatDetectionPolicy> createResourceAsync() {
        final SqlDatabaseThreatDetectionPolicyImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabaseThreatDetectionPolicies()
            .createOrUpdateAsync(
                this.resourceGroupName,
                this.sqlServerName,
                this.parent().name(),
                SecurityAlertPolicyName.DEFAULT,
                this.innerModel())
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
        this.innerModel().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.innerModel().withState(SecurityAlertPolicyState.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyDisabled() {
        this.innerModel().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.innerModel().withState(SecurityAlertPolicyState.DISABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyNew() {
        this.innerModel().withUseServerDefault(SecurityAlertPolicyUseServerDefault.DISABLED);
        this.innerModel().withState(SecurityAlertPolicyState.NEW);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withDefaultSecurityAlertPolicy() {
        this.innerModel().withUseServerDefault(SecurityAlertPolicyUseServerDefault.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withStorageEndpoint(String storageEndpoint) {
        this.innerModel().withStorageEndpoint(storageEndpoint);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withStorageAccountAccessKey(String storageAccountAccessKey) {
        this.innerModel().withStorageAccountAccessKey(storageAccountAccessKey);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withAlertsFilter(String alertsFilter) {
        this.innerModel().withDisabledAlerts(alertsFilter);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailAddresses(String addresses) {
        this.innerModel().withEmailAddresses(addresses);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withRetentionDays(int retentionDays) {
        this.innerModel().withRetentionDays(retentionDays);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailToAccountAdmins() {
        this.innerModel().withEmailAccountAdmins(SecurityAlertPolicyEmailAccountAdmins.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withoutEmailToAccountAdmins() {
        this.innerModel().withEmailAccountAdmins(SecurityAlertPolicyEmailAccountAdmins.DISABLED);
        return this;
    }
}
