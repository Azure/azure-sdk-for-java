// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.core.management.Region;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.DatabaseSecurityAlertPolicyInner;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyName;
import com.azure.resourcemanager.sql.models.SecurityAlertPolicyState;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseThreatDetectionPolicy;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Implementation for SQL database threat detection policy. */
public class SqlDatabaseThreatDetectionPolicyImpl extends
    ExternalChildResourceImpl<SqlDatabaseThreatDetectionPolicy, DatabaseSecurityAlertPolicyInner, SqlDatabaseImpl, SqlDatabase>
    implements SqlDatabaseThreatDetectionPolicy,
    SqlDatabaseThreatDetectionPolicy.SqlDatabaseThreatDetectionPolicyDefinition,
    SqlDatabaseThreatDetectionPolicy.Update {
    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String policyName;

    protected SqlDatabaseThreatDetectionPolicyImpl(String name, SqlDatabaseImpl parent,
        DatabaseSecurityAlertPolicyInner innerObject, SqlServerManager sqlServerManager) {
        super(name, parent, innerObject);
        Objects.requireNonNull(parent);
        Objects.requireNonNull(innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = parent.resourceGroupName();
        this.sqlServerName = parent.sqlServerName();
        this.policyName = name;
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
        return Region.fromName(parent().regionName());
    }

    @Override
    public String kind() {
        return parent().parent().kind();
    }

    @Override
    public SecurityAlertPolicyState currentState() {
        return this.innerModel().state() == null
            ? null
            : SecurityAlertPolicyState.fromString(this.innerModel().state().toString());
    }

    @Override
    public String disabledAlerts() {
        List<String> disabledAlerts = this.innerModel().disabledAlerts();
        if (disabledAlerts == null) {
            return null;
        }
        return String.join(";", disabledAlerts);
    }

    @Override
    public List<String> disabledAlertList() {
        return CoreUtils.isNullOrEmpty(this.innerModel().disabledAlerts())
            ? Collections.emptyList()
            : this.innerModel().disabledAlerts();
    }

    @Override
    public String emailAddresses() {
        List<String> emailAddresses = this.innerModel().emailAddresses();
        if (emailAddresses == null) {
            return null;
        }
        return String.join(";", emailAddresses);
    }

    @Override
    public List<String> emailAddressList() {
        return CoreUtils.isNullOrEmpty(this.innerModel().emailAddresses())
            ? Collections.emptyList()
            : this.innerModel().emailAddresses();
    }

    @Override
    public boolean emailAccountAdmins() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().emailAccountAdmins());
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
        return SecurityAlertPolicyName.DEFAULT.toString().equalsIgnoreCase(this.name());
    }

    @Override
    protected Mono<DatabaseSecurityAlertPolicyInner> getInnerAsync() {
        return this.sqlServerManager.serviceClient()
            .getDatabaseSecurityAlertPolicies()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.parent().name(),
                SecurityAlertPolicyName.fromString(this.name()));
    }

    @Override
    public Mono<SqlDatabaseThreatDetectionPolicy> createResourceAsync() {
        final SqlDatabaseThreatDetectionPolicyImpl self = this;
        return this.sqlServerManager.serviceClient()
            .getDatabaseSecurityAlertPolicies()
            .createOrUpdateAsync(this.resourceGroupName, this.sqlServerName, this.parent().name(),
                SecurityAlertPolicyName.fromString(this.policyName), this.innerModel())
            .map(databaseSecurityAlertPolicyInner -> {
                self.setInner(databaseSecurityAlertPolicyInner);
                this.policyName = databaseSecurityAlertPolicyInner.name();
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
        this.innerModel().withState(SecurityAlertPolicyState.ENABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withPolicyDisabled() {
        this.innerModel().withState(SecurityAlertPolicyState.DISABLED);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withDefaultSecurityAlertPolicy() {
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
        if (alertsFilter != null) {
            this.innerModel()
                .withDisabledAlerts(Stream.of(alertsFilter.split(Pattern.quote(";"))).collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withAlertsFilter(List<String> alertsFilter) {
        if (alertsFilter != null) {
            this.innerModel().withDisabledAlerts(alertsFilter);
        }
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailAddresses(String addresses) {
        if (addresses != null) {
            this.innerModel()
                .withEmailAddresses(Stream.of(addresses.split(Pattern.quote(";"))).collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailAddresses(List<String> addresses) {
        if (addresses != null) {
            this.innerModel().withEmailAddresses(addresses);
        }
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withRetentionDays(int retentionDays) {
        this.innerModel().withRetentionDays(retentionDays);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withEmailToAccountAdmins() {
        this.innerModel().withEmailAccountAdmins(true);
        return this;
    }

    @Override
    public SqlDatabaseThreatDetectionPolicyImpl withoutEmailToAccountAdmins() {
        this.innerModel().withEmailAccountAdmins(false);
        return this;
    }
}
