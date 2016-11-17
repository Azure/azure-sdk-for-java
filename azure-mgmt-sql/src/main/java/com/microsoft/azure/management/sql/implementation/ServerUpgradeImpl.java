/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.sql.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.sql.RecommendedDatabaseProperties;
import com.microsoft.azure.management.sql.ServerUpgrade;
import com.microsoft.azure.management.sql.UpgradeRecommendedElasticPoolProperties;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation for ServerUpgrade interface.
 */
@LangDefinition
class ServerUpgradeImpl
        extends WrapperImpl<ServerUpgradeStartParametersInner>
        implements
        ServerUpgrade,
        ServerUpgrade.UpgradeDefinition {
    private final ServersInner serversInner;
    private final String resourceGroupName;
    private final String sqlServerName;
    private List<RecommendedDatabaseProperties> databaseUpgrades;
    private List<UpgradeRecommendedElasticPoolProperties> elasticPoolUpgrades;

    protected ServerUpgradeImpl(String resourceGroupName, String sqlServerName, ServersInner serversInner) {
        super(new ServerUpgradeStartParametersInner());
        this.serversInner = serversInner;
        this.resourceGroupName = resourceGroupName;
        this.sqlServerName = sqlServerName;
        this.databaseUpgrades = new ArrayList<>();
        this.elasticPoolUpgrades = new ArrayList<>();
    }

    @Override
    public ServerUpgradeImpl withVersion(String version) {
        this.inner().withVersion(version);
        return this;
    }

    @Override
    public ServerUpgradeImpl withScheduleUpgradeAfterUtcDateTime(DateTime scheduleUpgradeAfterUtcDateTime) {
        this.inner().withScheduleUpgradeAfterUtcDateTime(scheduleUpgradeAfterUtcDateTime);
        return this;
    }

    @Override
    public DatabaseUpgradeImpl updateDatabase(String databaseName) {
        return new DatabaseUpgradeImpl(this, databaseName);
    }

    @Override
    public ElasticPoolUpgradeImpl updateElasticPool(String elasticPoolName) {
        return new ElasticPoolUpgradeImpl(this, elasticPoolName);
    }

    @Override
    public void schedule() {
        this.inner().withDatabaseCollection(this.databaseUpgrades);
        this.inner().withElasticPoolCollection(this.elasticPoolUpgrades);
        this.serversInner.startUpgrade(this.resourceGroupName, this.sqlServerName, this.inner());
    }

    public void addDatabaseUpgrade(RecommendedDatabaseProperties databaseUpgrade) {
        this.databaseUpgrades.add(databaseUpgrade);
    }

    public void addElasticPoolUpgrade(UpgradeRecommendedElasticPoolProperties elasticPoolUpgrade) {
        this.elasticPoolUpgrades.add(elasticPoolUpgrade);
    }
}
