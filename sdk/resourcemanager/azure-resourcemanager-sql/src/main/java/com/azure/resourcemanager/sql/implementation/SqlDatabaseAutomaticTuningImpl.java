// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AutomaticTuningMode;
import com.azure.resourcemanager.sql.models.AutomaticTuningOptionModeDesired;
import com.azure.resourcemanager.sql.models.AutomaticTuningOptions;
import com.azure.resourcemanager.sql.models.SqlDatabaseAutomaticTuning;
import com.azure.resourcemanager.sql.fluent.models.DatabaseAutomaticTuningInner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Implementation for Azure SQL Database automatic tuning. */
public class SqlDatabaseAutomaticTuningImpl
    extends RefreshableWrapperImpl<DatabaseAutomaticTuningInner, SqlDatabaseAutomaticTuning>
    implements SqlDatabaseAutomaticTuning, SqlDatabaseAutomaticTuning.Update {

    protected String key;
    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;
    private String sqlDatabaseName;

    private Map<String, AutomaticTuningOptions> automaticTuningOptionsMap;

    SqlDatabaseAutomaticTuningImpl(SqlDatabaseImpl database, DatabaseAutomaticTuningInner innerObject) {
        this(
            database.resourceGroupName,
            database.sqlServerName,
            database.name(),
            innerObject,
            database.sqlServerManager);
    }

    SqlDatabaseAutomaticTuningImpl(
        String resourceGroupName,
        String sqlServerName,
        String sqlDatabaseName,
        DatabaseAutomaticTuningInner innerObject,
        SqlServerManager sqlServerManager) {
        super(innerObject);
        Objects.requireNonNull(innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.sqlDatabaseName = sqlDatabaseName;

        this.key = UUID.randomUUID().toString();
        this.automaticTuningOptionsMap = new HashMap<String, AutomaticTuningOptions>();
    }

    @Override
    public AutomaticTuningMode desiredState() {
        return this.innerModel().desiredState();
    }

    @Override
    public AutomaticTuningMode actualState() {
        return this.innerModel().actualState();
    }

    @Override
    public Map<String, AutomaticTuningOptions> tuningOptions() {
        return Collections
            .unmodifiableMap(
                this.innerModel().options() != null
                    ? this.innerModel().options()
                    : new HashMap<String, AutomaticTuningOptions>());
    }

    @Override
    public SqlDatabaseAutomaticTuningImpl withAutomaticTuningMode(AutomaticTuningMode desiredState) {
        this.innerModel().withDesiredState(desiredState);
        return this;
    }

    @Override
    public SqlDatabaseAutomaticTuningImpl withAutomaticTuningOption(
        String tuningOptionName, AutomaticTuningOptionModeDesired desiredState) {
        if (this.automaticTuningOptionsMap == null) {
            this.automaticTuningOptionsMap = new HashMap<String, AutomaticTuningOptions>();
        }
        this
            .automaticTuningOptionsMap
            .put(tuningOptionName, new AutomaticTuningOptions().withDesiredState(desiredState));
        return this;
    }

    @Override
    public SqlDatabaseAutomaticTuningImpl withAutomaticTuningOptions(
        Map<String, AutomaticTuningOptionModeDesired> tuningOptions) {
        if (tuningOptions != null) {
            for (Map.Entry<String, AutomaticTuningOptionModeDesired> option : tuningOptions.entrySet()) {
                this.withAutomaticTuningOption(option.getKey(), option.getValue());
            }
        }
        return this;
    }

    @Override
    public SqlDatabaseAutomaticTuningImpl update() {
        return this;
    }

    @Override
    protected Mono<DatabaseAutomaticTuningInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabaseAutomaticTunings()
            .getAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName);
    }

    @Override
    public SqlDatabaseAutomaticTuning apply() {
        return this.applyAsync().block();
    }

    @Override
    public Mono<SqlDatabaseAutomaticTuning> applyAsync() {
        final SqlDatabaseAutomaticTuningImpl self = this;
        this.innerModel().withOptions(this.automaticTuningOptionsMap);
        return this
            .sqlServerManager
            .serviceClient()
            .getDatabaseAutomaticTunings()
            .updateAsync(this.resourceGroupName, this.sqlServerName, this.sqlDatabaseName, this.innerModel())
            .map(
                databaseAutomaticTuningInner -> {
                    self.setInner(databaseAutomaticTuningInner);
                    self.automaticTuningOptionsMap.clear();
                    return self;
                });
    }

    @Override
    public String key() {
        return this.key;
    }
}
