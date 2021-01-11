// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.sql.implementation;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.RefreshableWrapperImpl;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.models.AutomaticTuningOptionModeDesired;
import com.azure.resourcemanager.sql.models.AutomaticTuningServerMode;
import com.azure.resourcemanager.sql.models.AutomaticTuningServerOptions;
import com.azure.resourcemanager.sql.models.SqlServerAutomaticTuning;
import com.azure.resourcemanager.sql.fluent.models.ServerAutomaticTuningInner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import reactor.core.publisher.Mono;

/** Implementation for Azure SQL server automatic tuning. */
public class SqlServerAutomaticTuningImpl
    extends RefreshableWrapperImpl<ServerAutomaticTuningInner, SqlServerAutomaticTuning>
    implements SqlServerAutomaticTuning, SqlServerAutomaticTuning.Update {

    protected String key;
    private SqlServerManager sqlServerManager;
    private String resourceGroupName;
    private String sqlServerName;

    SqlServerAutomaticTuningImpl(SqlServerImpl server, ServerAutomaticTuningInner innerObject) {
        this(server.resourceGroupName(), server.name(), innerObject, server.manager());
    }

    SqlServerAutomaticTuningImpl(
        String resourceGroupName,
        String sqlServerName,
        ServerAutomaticTuningInner innerObject,
        SqlServerManager sqlServerManager) {
        super(innerObject);
        Objects.requireNonNull(innerObject);
        Objects.requireNonNull(sqlServerManager);
        this.sqlServerManager = sqlServerManager;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;

        this.key = UUID.randomUUID().toString();
    }

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public AutomaticTuningServerMode desiredState() {
        return this.innerModel().desiredState();
    }

    @Override
    public AutomaticTuningServerMode actualState() {
        return this.innerModel().actualState();
    }

    @Override
    public Map<String, AutomaticTuningServerOptions> tuningOptions() {
        return Collections
            .unmodifiableMap(
                this.innerModel().options() != null
                    ? this.innerModel().options()
                    : new HashMap<String, AutomaticTuningServerOptions>());
    }

    @Override
    public SqlServerAutomaticTuningImpl withAutomaticTuningMode(AutomaticTuningServerMode desiredState) {
        this.innerModel().withDesiredState(desiredState);
        return this;
    }

    @Override
    public SqlServerAutomaticTuningImpl withAutomaticTuningOption(
        String tuningOptionName, AutomaticTuningOptionModeDesired desiredState) {
        if (this.innerModel().options() == null) {
            this.innerModel().withOptions(new HashMap<String, AutomaticTuningServerOptions>());
        }
        AutomaticTuningServerOptions item = this.innerModel().options().get(tuningOptionName);
        this
            .innerModel()
            .options()
            .put(
                tuningOptionName,
                item != null
                    ? item.withDesiredState(desiredState)
                    : new AutomaticTuningServerOptions().withDesiredState(desiredState));
        return this;
    }

    @Override
    public SqlServerAutomaticTuningImpl withAutomaticTuningOptions(
        Map<String, AutomaticTuningOptionModeDesired> tuningOptions) {
        if (tuningOptions != null) {
            for (Map.Entry<String, AutomaticTuningOptionModeDesired> option : tuningOptions.entrySet()) {
                this.withAutomaticTuningOption(option.getKey(), option.getValue());
            }
        }
        return this;
    }

    @Override
    protected Mono<ServerAutomaticTuningInner> getInnerAsync() {
        return this
            .sqlServerManager
            .serviceClient()
            .getServerAutomaticTunings()
            .getAsync(this.resourceGroupName, this.sqlServerName);
    }

    @Override
    public SqlServerAutomaticTuning apply() {
        return this.applyAsync().block();
    }

    @Override
    public Mono<SqlServerAutomaticTuning> applyAsync() {
        final SqlServerAutomaticTuningImpl self = this;
        return this
            .sqlServerManager
            .serviceClient()
            .getServerAutomaticTunings()
            .updateAsync(this.resourceGroupName, this.sqlServerName, this.innerModel())
            .map(
                serverAutomaticTuningInner -> {
                    self.setInner(serverAutomaticTuningInner);
                    return self;
                });
    }

    @Override
    public Update update() {
        return this;
    }
}
